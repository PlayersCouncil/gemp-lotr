import { inject, Injectable, signal } from '@angular/core';
import { ApiError } from '../../api/api-error';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { GameApi } from '../api/game-api.service';
import { GameUpdate } from '../protocol/game-events';
import { applyEvents } from '../state/apply-event';
import { emptyGame, GameModel } from '../state/game-model';
import { assignMinion, clearSelection, decisionAnswered, selectCard, unassignMinion } from '../state/local-choices';
import { GameLog } from './game-log';
import { GameRestore } from './game-restore';

/** How long after an answer the page asks for news again (the legacy page's 100 ms). */
export const POLL_DELAY_MS = 100;

/** Where the page may send the player when a game can't go on (the legacy error dialog's buttons). */
export type ProblemAction = 'reload' | 'mainPage' | 'hall';

/**
 * Something that stops the game on this page, as the legacy page said it: a dialog with a title, or (`title` null)
 * warnings in the chat.
 */
export interface GameProblem {
  status: number;
  title: string | null;
  lines: readonly string[];
  actions: readonly ProblemAction[];
}

/** The legacy page's answers to a failed game call (gameUi.js gameErrorMap and its communication failure). */
export function problemFor(status: number): GameProblem {
  switch (status) {
    case 0:
      return dialog(
        status,
        'Server connection error',
        'Unable to connect to server. Either server is down or there is a problem with your internet connection.',
        ['reload'],
      );
    case 401:
      return dialog(status, 'Authentication error', 'You are not logged in', ['mainPage']);
    case 403:
      return dialog(status, 'Game access forbidden', 'This game is private and does not allow spectators.', ['hall']);
    case 409:
      return dialog(
        status,
        'Concurrent access error',
        'You are observing this Game Hall from another browser or window. Close this window or if you wish to observe it here, click "Refresh page".',
        ['reload'],
      );
    case 410:
      return dialog(
        status,
        'Inactivity error',
        'You were inactive for too long and have been removed from observing this game. If you wish to start again, click "Refresh page".',
        ['reload'],
      );
    default:
      return {
        status,
        title: null,
        lines: [
          `There was a problem communicating with the server (${status}), if the game is finished, it has been removed, otherwise you have lost connection to the server.`,
          'Refresh the page (press F5) to resume the game, or press back on your browser to get back to the Game Hall.',
        ],
        actions: [],
      };
  }
}

function dialog(status: number, title: string, text: string, actions: ProblemAction[]): GameProblem {
  return { status, title, lines: [text], actions };
}

/**
 * One game as the page plays (or watches) it: the game the server sends, the player's answers and choices, and the
 * long poll that keeps it current. Like the legacy page, it asks for news only while no decision waits for the
 * player: the answer to the decision brings what happened next. Page-scoped.
 */
@Injectable()
export class GameSession {
  private readonly api = inject(GameApi);
  private readonly lifetime = inject(PageLifetime);
  private readonly restore = inject(GameRestore);
  private readonly log = inject(GameLog);

  private readonly state = signal<GameModel>(emptyGame());
  /** The game as the page holds it. */
  readonly model = this.state.asReadonly();
  readonly problem = signal<GameProblem | null>(null);

  private gameId: string | null = null;
  private channel: string | null = null;
  private polling = false;
  /** The view's own state, kept with the game for a reload (see GameRestore). */
  private view: Readonly<Record<string, unknown>> = {};
  /** True once the game is open and what the page held before was read back: only then is anything saved over it. */
  private ready = false;

  /**
   * Opens the game: its whole state from the server, then what only the page held before a reload. `opened` runs
   * once the game is there, before the page asks for news (the legacy page joins the game's chat then).
   */
  open(gameId: string, opened?: () => void): void {
    this.gameId = gameId;
    this.lifetime.read(this.api.open(gameId), {
      next: (update) => {
        // What the page held is read back before anything is saved over it
        this.receive(update, false);
        this.state.update((model) => this.restore.restore(gameId, model));
        // With the messages the page had shown before a reload in front
        this.log.restart(this.state().messages);
        this.ready = true;
        this.save();
        opened?.();
        this.keepCurrent(update);
      },
      error: (error: unknown) => this.fail(error),
    });
  }

  /** What the view saved with the game before a reload, for it to take up again. */
  savedView(gameId: string): Readonly<Record<string, unknown>> {
    return this.restore.saved(gameId)?.view ?? {};
  }

  /** The view's own state changed: kept with the game. */
  keepView(view: Readonly<Record<string, unknown>>): void {
    this.view = view;
    this.save();
  }

  /** The player's answer to the waiting decision. */
  answer(value: string): void {
    const waiting = this.state().decision;
    if (!waiting || !this.gameId || this.channel === null) return;
    this.state.update(decisionAnswered);
    this.save();
    this.lifetime.write(this.api.answer(this.gameId, this.channel, waiting.decision.id, value), {
      next: (update) => {
        this.receive(update);
        this.keepCurrent(update);
      },
      error: (error: unknown) => this.fail(error),
    });
  }

  assign(minionId: string, characterId: string): void {
    this.change((model) => assignMinion(model, minionId, characterId));
  }

  unassign(minionId: string): void {
    this.change((model) => unassignMinion(model, minionId));
  }

  select(cardId: string): void {
    this.change((model) => selectCard(model, cardId));
  }

  clearSelection(): void {
    this.change(clearSelection);
  }

  /**
   * Concedes. The page doesn't ask for news while a decision waits, so it asks once after conceding, to learn that the
   * game ended (as the legacy page now does).
   */
  concede(): void {
    if (!this.gameId) return;
    this.lifetime.write(this.api.concede(this.gameId), {
      next: () => {
        if (!this.polling) this.poll();
      },
      error: (error: unknown) => this.fail(error),
    });
  }

  /** Asks the opponent to cancel the game. */
  cancel(): void {
    if (!this.gameId) return;
    this.lifetime.write(this.api.cancel(this.gameId), { error: (error: unknown) => this.fail(error) });
  }

  /** What affects a card, as the server writes it (HTML), for the card's information. */
  cardInfo(cardId: string, show: (html: string) => void): void {
    if (!this.gameId) return;
    this.lifetime.read(this.api.cardInfo(this.gameId, cardId), { next: show, error: () => show('') });
  }

  private change(apply: (model: GameModel) => GameModel): void {
    this.state.update(apply);
    this.save();
  }

  private receive(update: GameUpdate, save = true): void {
    this.channel = update.channel ?? this.channel;
    const shown = this.state().messages.length;
    this.state.update((model) => ({
      ...applyEvents(model, update.events),
      clocks: { ...model.clocks, ...update.clocks },
    }));
    this.log.game(this.state().messages.slice(shown));
    if (save) this.save();
  }

  /** After an answer: news is asked for again unless a decision now waits for the player or the game is over. */
  private keepCurrent(update: GameUpdate): void {
    const decides = update.events.some((event) => event.type === 'D');
    if (decides || this.state().ended) return;
    // A moment later, as the legacy page did: what the page starts on an answer (joining the chat) is asked first
    this.polling = true;
    this.lifetime.later(() => this.poll(), POLL_DELAY_MS);
  }

  private poll(): void {
    if (!this.gameId || this.channel === null) return;
    this.polling = true;
    this.lifetime.read(this.api.poll(this.gameId, this.channel), {
      next: (update) => {
        this.polling = false;
        this.receive(update);
        this.keepCurrent(update);
      },
      error: (error: unknown) => {
        this.polling = false;
        this.fail(error);
      },
    });
  }

  private fail(error: unknown): void {
    const problem = problemFor(ApiError.from(error).status);
    this.problem.set(problem);
    // A problem with a title is a dialog; the others are warnings in the chat box, as on the legacy page
    if (problem.title === null) this.log.warn(problem.lines);
  }

  private save(): void {
    if (this.gameId && this.ready) this.restore.save(this.gameId, this.state(), this.view);
  }
}
