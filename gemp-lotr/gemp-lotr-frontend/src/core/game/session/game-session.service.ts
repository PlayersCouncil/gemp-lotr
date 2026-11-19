import { effect, inject, Injectable, signal, untracked } from '@angular/core';
import { ApiError } from '../../api/api-error';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { GameApi } from '../api/game-api.service';
import { GameEvent, GameUpdate } from '../protocol/game-events';
import { applyEvent } from '../state/apply-event';
import { GameSettings } from '../settings/game-settings';
import { eventPauses, EventPause, GameAnimation, speedFactor } from './event-pacing';
import { emptyGame, GameModel } from '../state/game-model';
import { assignMinion, clearSelection, decisionAnswered, selectCard, unassignMinion } from '../state/local-choices';
import { noticeRead, scenarioAnswered, stepRead } from '../scenario/scenario-state';
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
  /** Whether the page shows a replay (a finished game played back) rather than a game. */
  readonly replaying = signal(false);
  /** The animation the page draws while an event holds it, and for how long (ms); null when nothing animates. */
  readonly animation = signal<{ animation: GameAnimation; duration: number } | null>(null);
  /** True while events wait to be applied (an animation holds them). */
  readonly busy = signal(false);
  /**
   * The factor of the animations' durations: from the animation speed setting, and in a replay also its Slower and
   * Faster buttons (as the legacy page shared one value).
   */
  readonly speed = signal(1);
  /** A replay: whether it plays, and whether it has played to its end. */
  readonly replayPlaying = signal(false);
  readonly replayDone = signal(false);

  private readonly settings = inject(GameSettings);
  /** Events waiting to be applied, each with whether it animates, its pauses, and how far it got. */
  private readonly queue: {
    event: GameEvent;
    animate: boolean;
    pauses?: EventPause[];
    stage: number;
    applied: boolean;
  }[] = [];
  /** What runs once the queue is empty (asking for news). */
  private afterQueue: (() => void)[] = [];
  private holding = false;
  private replayEvents: readonly GameEvent[] = [];
  private replayNext = 0;

  constructor() {
    // The setting changes the animations' speed (also during a replay, as the legacy slider did)
    effect(() => {
      const factor = speedFactor(this.settings.animationSpeed());
      untracked(() => this.speed.set(factor));
    });
  }

  private gameId: string | null = null;
  /** The game the page has open, or null (none yet, or a replay). */
  get openGame(): string | null {
    return this.gameId;
  }
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
        this.receive(update, false, false);
        this.state.update((model) => this.restore.restore(gameId, model));
        // With the messages the page had shown before a reload in front
        this.log.restart(gameId, this.state().messages);
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
    // (a scripted game's step about this decision has had its say)
    this.state.update((model) => ({ ...decisionAnswered(model), scenario: scenarioAnswered(model.scenario) }));
    this.save();
    this.lifetime.write(this.api.answer(this.gameId, this.channel, waiting.decision.id, value), {
      next: (update) => {
        this.receive(update);
        this.afterEvents(() => this.keepCurrent(update));
      },
      error: (error: unknown) => this.fail(error),
    });
  }

  /**
   * Opens a replay: every event of a finished game, played one after the other when the player presses play (the
   * legacy replay page), each held as the animations say. A replay has no chat and nothing to answer.
   */
  openReplay(replayId: string): void {
    this.replaying.set(true);
    this.log.restart(null, []);
    this.lifetime.read(this.api.replay(replayId), {
      next: (events) => {
        this.replayEvents = events;
        this.replayNext = 0;
        this.replayDone.set(events.length === 0);
      },
      error: (error: unknown) => this.fail(error),
    });
  }

  /** The replay's play/pause button. */
  togglePlay(): void {
    if (!this.replaying()) return;
    const playing = !this.replayPlaying();
    this.replayPlaying.set(playing);
    if (playing && !this.holding && this.queue.length === 0) this.playNext();
  }

  /** Slower: everything takes twice as long (at most 16 times as long as normal). */
  slower(): void {
    this.speed.update((factor) => Math.min(16, factor * 2));
  }

  /** Faster: everything takes half as long (at least a sixteenth of normal). */
  faster(): void {
    this.speed.update((factor) => Math.max(0.0625, factor / 2));
  }

  /** The player's choice for the waiting decision changed (see local-choices.ts): kept with the game. */
  choose(apply: (model: GameModel) => GameModel): void {
    this.change(apply);
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

  /**
   * Asks for news once, though a decision may be waiting (when nothing else asks): something outside the game's own
   * to and fro has made the server send (a scripted game's step read, or rewritten by its author). With news already
   * asked for, it comes that way.
   */
  refresh(): void {
    if (!this.polling) this.poll(true);
  }

  /** A scripted game's step that only waited to be read has been read (and the server told). */
  stepRead(): void {
    this.change((model) => ({ ...model, scenario: stepRead(model.scenario) }));
  }

  /** A scripted game's notice that it has left its script has been read. */
  noticeRead(): void {
    this.change((model) => ({ ...model, scenario: noticeRead(model.scenario) }));
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

  /**
   * An answer of the server: its clocks at once, its events one after the other, each held as long as its animation
   * (eventPauses). The game's whole state, as the page opens, animates nothing and is applied at once.
   */
  private receive(update: GameUpdate, save = true, animate = true): void {
    this.channel = update.channel ?? this.channel;
    this.state.update((model) => ({ ...model, clocks: { ...model.clocks, ...update.clocks } }));
    for (const event of update.events) this.queue.push({ event, animate, stage: 0, applied: false });
    this.pump();
    if (save) this.afterEvents(() => this.save());
  }

  /** Runs once every event received so far has been applied (at once if none waits). */
  private afterEvents(action: () => void): void {
    if (this.queue.length === 0 && !this.holding) action();
    else this.afterQueue.push(action);
  }

  /** Applies the waiting events in order, stopping while one holds the page. */
  private pump(): void {
    if (this.holding) return;
    for (;;) {
      const item = this.queue[0];
      if (!item) break;
      const model = this.state();
      item.pauses ??= eventPauses(item.event, {
        animate: item.animate,
        replay: this.replaying(),
        spectator: model.spectator,
        bottom: model.participantId,
      });
      const before = item.pauses.filter((pause) => pause.when === 'before');
      const after = item.pauses.filter((pause) => pause.when === 'after');
      if (item.stage < before.length) {
        const pause = before[item.stage];
        item.stage++;
        if (pause) this.hold(pause);
        return;
      }
      if (!item.applied) {
        item.applied = true;
        this.apply(item.event);
      }
      const next = after[item.stage - before.length];
      if (next) {
        item.stage++;
        this.hold(next);
        return;
      }
      this.queue.shift();
    }
    this.busy.set(false);
    const waiting = this.afterQueue;
    this.afterQueue = [];
    for (const action of waiting) action();
    // A replay goes on with its next event while it plays
    if (this.replaying()) this.lifetime.later(() => this.playNext(), 0);
  }

  private hold(pause: EventPause): void {
    const duration = pause.duration * this.speed();
    this.holding = true;
    this.busy.set(true);
    this.animation.set({ animation: pause.animation, duration });
    this.lifetime.later(() => {
      this.holding = false;
      this.animation.set(null);
      this.pump();
    }, duration);
  }

  private apply(event: GameEvent): void {
    const shown = this.state().messages.length;
    this.state.update((model) => applyEvent(model, event));
    this.log.game(this.state().messages.slice(shown));
  }

  /** The replay's next event, while it plays: what the last one asked is put away first (legacy cleanupDecision). */
  private playNext(): void {
    if (!this.replayPlaying() || this.holding || this.queue.length > 0) return;
    const event = this.replayEvents[this.replayNext];
    if (!event) {
      if (this.replayEvents.length > 0) this.replayDone.set(true);
      return;
    }
    this.replayNext++;
    if (this.state().decision) this.state.update(decisionAnswered);
    this.queue.push({ event, animate: true, stage: 0, applied: false });
    this.busy.set(true);
    this.pump();
  }

  /**
   * After an answer: news is asked for again unless a decision now waits for the player. Also after the game's end, as
   * the legacy page did: the server then still sends what it shows of the game (the player's draw deck).
   */
  private keepCurrent(update: GameUpdate): void {
    const decides = update.events.some((event) => event.type === 'D');
    if (decides) return;
    // A moment later, as the legacy page did: what the page starts on an answer (joining the chat) is asked first
    this.polling = true;
    this.lifetime.later(() => this.poll(), POLL_DELAY_MS);
  }

  /** `once`: asked for over a waiting decision (refresh); the asking doesn't go on while that decision still waits. */
  private poll(once = false): void {
    if (!this.gameId || this.channel === null) return;
    this.polling = true;
    this.lifetime.read(this.api.poll(this.gameId, this.channel), {
      next: (update) => {
        this.polling = false;
        this.receive(update);
        this.afterEvents(() => {
          if (!once || !this.state().decision) this.keepCurrent(update);
        });
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
