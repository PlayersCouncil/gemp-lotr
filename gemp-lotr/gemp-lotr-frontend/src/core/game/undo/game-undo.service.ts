import { computed, effect, inject, Injectable, signal, untracked } from '@angular/core';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { GameApi } from '../api/game-api.service';
import { DecisionGates } from '../decision/decision-gate';
import { GameSession } from '../session/game-session.service';
import {
  GameNotice,
  PendingUndo,
  pendingOf,
  RestorePoint,
  RestorePoints,
  UNDO_TIERS,
  UndoAnswer,
  UndoPolicy,
  UndoTier,
} from './undo-protocol';

/** How long the banner stays (ms), and how long it then takes to fade (the legacy page's 10 s and 1 s). */
export const BANNER_MS = 10000;
export const BANNER_FADE_MS = 1000;
/** After a failed notices poll the page asks again after this long, not at once (the server may be restarting). */
export const NOTICES_RETRY_MS = 5000;

/** The banner that says what happened: a game taken back (with its tier's mark), or a request refused or declined. */
export interface UndoBanner {
  message: string;
  tier: UndoTier | null;
  /** Counts the banners, so that a new one with the same words is new. */
  id: number;
  /** True for its last second, while it fades. */
  fading: boolean;
}

/**
 * Taking a game back (undo and revert), as the legacy page's undo.js does it, so the two pages work alike and with each
 * other (the server: LotroGameMediator.requestUndo):
 *
 * - a second long poll, for the game's notices, that runs all the time: the page doesn't ask for the game's news while
 *   its player looks at a decision, so a request, an answer to one, the game taken back or ended comes as a notice;
 * - what the player may take back, asked again whenever the game moves: the latest place, if it is green (the player's
 *   own choice, nobody else has answered or seen anything since), is what the Undo button takes back at once;
 * - the list of every place (Revert...), each green one an undo and each yellow or red one asked of the opponent;
 * - a request waiting: the asker may withdraw it, the player asked approves, declines or declines everything; while it
 *   waits nothing in the game is answered (`frozen`: it is one of the page's DecisionGates), and it counts down;
 * - the game taken back: GameSession reads it again from nothing, and the banner says so.
 *
 * Started by the page once the game has opened (`start`), never for a replay. Page-scoped.
 */
@Injectable()
export class GameUndo {
  private readonly api = inject(GameApi);
  private readonly session = inject(GameSession);
  private readonly lifetime = inject(PageLifetime);

  /** What the game allows, as last heard (null until then). */
  readonly policy = signal<UndoPolicy | null>(null);
  /** Whether this player declines every request in this game without being asked. */
  readonly autoDeclining = signal(false);
  /** The player's last choice, while it is theirs to undo (green): what the Undo button takes back. */
  readonly latest = signal<RestorePoint | null>(null);
  /** The Undo button was pressed and its answer hasn't come: it is off meanwhile. */
  readonly sending = signal(false);
  /** The open list of places to go back to, as the server gave it when it was opened; null while closed. */
  readonly list = signal<RestorePoints | null>(null);
  /** The request waiting for an answer, if any, and its seconds left. */
  readonly pending = signal<PendingUndo | null>(null);
  readonly secondsLeft = signal(0);
  readonly banner = signal<UndoBanner | null>(null);

  private readonly model = this.session.model;
  /** Somebody playing (not watching, not a replay): only they take anything back or answer requests. */
  readonly playing = computed(() => !this.model().spectator && !this.session.replaying());
  /** Something can be taken back in this game (not, say, a scenario that is being written as it is played). */
  readonly allowed = computed(() => {
    const policy = this.policy();
    return !!policy && (policy.green || policy.yellow || policy.red);
  });
  /** The request waiting is this player's own (they may withdraw it). */
  readonly mine = computed(() => {
    const requester = this.pending()?.requester;
    // (a page flipped to the other seat is that seat's player's: the person is still the scenario's `human`)
    const person = this.model().scenario?.info?.human;
    return (
      this.playing() && requester !== undefined && (requester === this.model().participantId || requester === person)
    );
  });
  /** The request waiting asks this player. */
  readonly asked = computed(() => this.playing() && this.pending() !== null && !this.mine());
  /** While a request waits no decision may be answered: the server would refuse the answer. */
  readonly frozen = computed(() => this.playing() && this.pending() !== null);
  /**
   * The Undo button, while the player's last choice is theirs to take back: what its title says (what it undoes, or why
   * it can't), and whether it can be pressed (not while a request waits or its own answer is on its way).
   */
  readonly button = computed<{ title: string; enabled: boolean } | null>(() => {
    const latest = this.latest();
    // (nor in a game that allows no taking back at all: a puzzle's, as its scenario has it)
    if (!latest || !this.allowed()) return null;
    const off = latest.unavailable;
    return {
      title: off ?? `Undo: ${latest.label}`,
      enabled: off === null && !this.frozen() && !this.sending(),
    };
  });

  private gameId: string | null = null;
  /** The game's answers with events already asked about (GameSession.moved). */
  private movesSeen = 0;
  private lastNotice = -1;
  private refreshing = false;
  private refreshAgain = false;
  private banners = 0;
  /** Counts the requests shown, so that the countdown of one that has gone stops. */
  private requests = 0;

  constructor() {
    // While a request waits nothing in the game is answered: no gesture gets through to the decision (the page's
    // gates, which a scripted game's step uses too)
    inject(DecisionGates).add({ allows: () => !this.frozen() });
    // What the player may take back changes as the game moves on (the legacy page asks on every answer with events
    // after it has started; the game's first answer is asked about by start)
    effect(() => {
      const moved = this.session.moved();
      untracked(() => {
        if (this.gameId === null || moved <= this.movesSeen) return;
        this.movesSeen = moved;
        this.refresh();
      });
    });
    // The game event that marks the game as taken back, in a game and in a replay
    effect(() => {
      const reverted = this.session.reverted();
      if (reverted) untracked(() => this.showBanner(reverted.message, reverted.tier));
    });
  }

  /** Starts taking part, once the game has opened: the notices' poll, and what can be taken back now. */
  start(gameId: string): void {
    if (this.gameId !== null) return;
    this.gameId = gameId;
    this.movesSeen = this.session.moved();
    this.pollNotices();
    this.refresh();
  }

  /**
   * Asks the server again what the player may go back to. Asked while an answer is on its way, it asks once more when
   * that has come.
   */
  refresh(): void {
    const gameId = this.gameId;
    if (gameId === null) return;
    if (this.refreshing) {
      this.refreshAgain = true;
      return;
    }
    this.refreshing = true;
    const done = () => {
      this.refreshing = false;
      if (this.refreshAgain) {
        this.refreshAgain = false;
        this.refresh();
      }
    };
    this.lifetime.read(this.api.restorePoints(gameId), {
      next: (answer) => {
        this.policy.set(answer.policy);
        this.autoDeclining.set(answer.autoDecline);
        const [last] = answer.points;
        this.latest.set(this.playing() && !answer.pending && last?.tier === 'GREEN' ? last : null);
        this.sending.set(false);
        // A request may have been waiting since before this page opened
        if (answer.pending && !this.pending()) this.showPending(answer.pending);
        done();
      },
      error: () => done(),
    });
  }

  /**
   * The Undo button: takes the player's last choice back at once. Sent as `ownOnly`: if the opponent has answered in
   * the meantime it is no longer an undo, and a button that says Undo asks nobody for a revert.
   */
  undoLatest(): void {
    const latest = this.latest();
    if (!latest || !this.gameId || this.frozen() || this.sending()) return;
    this.sending.set(true);
    this.lifetime.write(this.api.undo(this.gameId, latest.entryCount, true), {
      next: (reply) => {
        if (reply.status === 'refused') {
          this.showBanner(reply.message, null);
          this.refresh();
        }
      },
      error: () => {
        this.showBanner('Undo could not be sent', null);
        this.refresh();
      },
    });
  }

  /** Revert...: the list of every place the player may go back to, fetched afresh. */
  openList(): void {
    if (!this.gameId) return;
    this.lifetime.read(this.api.restorePoints(this.gameId), {
      next: (answer) => {
        this.policy.set(answer.policy);
        this.list.set(answer);
      },
      error: () => this.showBanner('The list could not be fetched', null),
    });
  }

  closeList(): void {
    this.list.set(null);
  }

  /** A place of the list: taken back at once if green, otherwise asked of the opponent (the notice shows the request). */
  ask(entryCount: number): void {
    if (!this.gameId) return;
    this.lifetime.write(this.api.undo(this.gameId, entryCount, false), {
      next: (reply) => {
        this.closeList();
        if (reply.status === 'refused') this.showBanner(reply.message, null);
      },
      error: () => this.showBanner('The request could not be sent', null),
    });
  }

  /** Answers the request waiting: the dialog closes when the server says it was resolved (a notice). */
  reply(answer: UndoAnswer): void {
    const pending = this.pending();
    if (!pending || !this.gameId) return;
    this.lifetime.write(this.api.undoReply(this.gameId, pending.requestId, answer), {
      next: (reply) => {
        if (reply.status === 'refused' && answer === 'approve') this.showBanner(reply.message, null);
      },
    });
  }

  /** "Auto-decline revert requests", for this game only. */
  setAutoDecline(on: boolean): void {
    if (!this.gameId) return;
    this.autoDeclining.set(on);
    this.lifetime.write(this.api.autoDecline(this.gameId, on), {});
  }

  dismissBanner(): void {
    this.banner.set(null);
  }

  private pollNotices(): void {
    const gameId = this.gameId;
    if (gameId === null) return;
    this.lifetime.read(this.api.notices(gameId, this.lastNotice), {
      next: (answer) => {
        if (answer.latest !== null && this.lastNotice < 0) this.lastNotice = answer.latest;
        for (const notice of answer.notices) {
          this.lastNotice = Math.max(this.lastNotice, notice.number);
          this.notice(notice);
        }
        this.pollNotices();
      },
      error: () => this.lifetime.later(() => this.pollNotices(), NOTICES_RETRY_MS),
    });
  }

  private notice(notice: GameNotice): void {
    const fields = notice.fields;
    switch (notice.type) {
      case 'resync':
        if (fields['reason'] === 'revert') {
          // The game is no longer the game this page has drawn: it is read again from nothing
          this.closeRequest();
          this.closeList();
          this.showBanner(fields['message'] ?? null, fields['tier'] ?? null);
          this.session.reopen(fields['message'] ?? null);
        } else if (fields['reason'] === 'flip') {
          // The table flipped (TableFlip): this page is now the other seat's player's, on a new channel; read from nothing
          this.closeList();
          this.session.reopen(null);
        } else {
          // Something the player must hear of while looking at a decision (the game ended): the game's news, once
          this.session.pollOnce();
        }
        break;
      case 'undoRequested':
        this.showPending(pendingOf(notice));
        break;
      case 'undoResolved':
        this.closeRequest();
        if (fields['outcome'] !== 'done') this.showBanner(fields['message'] ?? null, null);
        this.refresh();
        break;
    }
  }

  private showPending(pending: PendingUndo): void {
    this.closeRequest();
    this.closeList();
    const request = ++this.requests;
    this.pending.set(pending);
    this.secondsLeft.set(pending.secondsLeft);
    const tick = () => {
      if (request !== this.requests) return;
      this.secondsLeft.update((left) => left - 1);
      this.lifetime.later(tick, 1000);
    };
    this.lifetime.later(tick, 1000);
  }

  private closeRequest(): void {
    this.requests++;
    this.pending.set(null);
  }

  private showBanner(message: string | null, tier: string | null): void {
    if (!message) return;
    const id = ++this.banners;
    this.banner.set({ message, tier: UNDO_TIERS.find((known) => known === tier) ?? null, id, fading: false });
    this.lifetime.later(() => {
      if (this.banner()?.id === id) this.banner.update((shown) => shown && { ...shown, fading: true });
    }, BANNER_MS);
    this.lifetime.later(() => {
      if (this.banner()?.id === id) this.banner.set(null);
    }, BANNER_MS + BANNER_FADE_MS);
  }
}
