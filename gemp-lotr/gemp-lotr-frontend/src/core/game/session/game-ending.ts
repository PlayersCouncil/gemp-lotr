import { computed, effect, inject, Injectable, linkedSignal, signal, untracked } from '@angular/core';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { GameApi, LatestGame } from '../api/game-api.service';
import { GameModel } from '../state/game-model';
import { GameSession } from './game-session.service';

/** Where the app's game page is: a replay is it with `?replayId=`. */
export const GAME_PAGE_URL = '/gemp-lotr/ng/game';

/** How many times, and how far apart (ms), the page asks for the player's last game before giving up. */
export const REPLAY_TRIES = 4;
export const REPLAY_RETRY_MS = 1500;
/** A last game that ended longer ago than this (ms) is an earlier one: this game's isn't recorded yet. */
const RECENT_MS = 15 * 60 * 1000;

/** Who won the game and why, as the game's log said it ("Bob is the winner due to: …"); null fields when it didn't. */
export interface GameOutcome {
  winner: string | null;
  reason: string | null;
}

/**
 * The player's replay of the game that ended: none for this viewer (a spectator, or the game goes on), being looked
 * for, found (its id, "player$recording"), or not found.
 */
export type ReplayLink =
  { state: 'none' } | { state: 'looking' } | { state: 'found'; replayId: string } | { state: 'missing' };

const WINNER = /^(.+?) is the winner due to: (.*)$/s;

/** The winner and the reason in the game's messages, if it said who won. */
export function gameOutcome(model: GameModel): GameOutcome {
  for (let i = model.messages.length - 1; i >= 0; i--) {
    const message = model.messages[i];
    const found = message?.kind === 'message' ? WINNER.exec(message.text) : null;
    if (found) return { winner: found[1] ?? null, reason: found[2] ?? null };
  }
  return { winner: null, reason: null };
}

/** The replay's page, whole (to open, or copy and share). */
export function replayUrl(origin: string, replayId: string): string {
  return `${origin}${GAME_PAGE_URL}?replayId=${encodeURIComponent(replayId)}`;
}

/**
 * Whether the player's last game in their history is this one: against one of this game's players, and just ended.
 * The history has no game id, so this is as close as it gets.
 */
function isThisGame(latest: LatestGame | null, model: GameModel, now: number): latest is LatestGame {
  if (!latest?.recordingId || !model.players.includes(latest.opponent)) return false;
  return latest.endMs === null || now - latest.endMs < RECENT_MS;
}

/**
 * The game's end as the page shows it (the owner, 2026-11-20): who won, and for a player (not a spectator, not in a
 * replay) their own replay of it, to watch or to copy its link. The server records the game as it ends, so the page
 * asks for the player's last game a few times until it is there.
 */
@Injectable()
export class GameEnding {
  private readonly session = inject(GameSession);
  private readonly api = inject(GameApi);
  private readonly lifetime = inject(PageLifetime);

  /** The game ended while the page shows it live (a replay's end is not this). */
  readonly ended = computed(() => this.session.model().ended && !this.session.replaying());
  readonly outcome = computed(() => (this.ended() ? gameOutcome(this.session.model()) : null));
  /** The player's replay; `none` for a spectator. */
  readonly replay = signal<ReplayLink>({ state: 'none' });
  /**
   * Whether the game's end is shown: opens as the game ends, until the player closes it. Not by itself in a scripted
   * game (a tutorial, a puzzle), whose end is the scenario's own panel; `show` still opens it there.
   */
  readonly dialogOpen = linkedSignal(() => this.ended() && this.session.model().scenario === null);
  private asked = false;
  /** Whether a view shows the replay (the modern theme's dialog): only then does the page ask for it. */
  private readonly wanted = signal(false);

  constructor() {
    effect(() => {
      if (!this.ended() || !this.wanted()) return;
      untracked(() => {
        const model = this.session.model();
        if (model.spectator || model.participantId === null || this.asked) return;
        this.asked = true;
        this.replay.set({ state: 'looking' });
        this.look(model.participantId, REPLAY_TRIES);
      });
    });
  }

  /**
   * A view that shows the replay is there: the page looks for it once the game ends. Classic shows game.html's end,
   * which asked for nothing more.
   */
  showReplay(): void {
    this.wanted.set(true);
  }

  /** Shows the game's end again (the phone layout's menu and divider). */
  show(): void {
    if (this.ended()) this.dialogOpen.set(true);
  }

  close(): void {
    this.dialogOpen.set(false);
  }

  private look(participantId: string, tries: number): void {
    const again = () => {
      if (tries > 1) this.lifetime.later(() => this.look(participantId, tries - 1), REPLAY_RETRY_MS);
      else this.replay.set({ state: 'missing' });
    };
    this.lifetime.read(this.api.latestGame(), {
      next: (latest) => {
        if (isThisGame(latest, this.session.model(), Date.now())) {
          this.replay.set({ state: 'found', replayId: `${participantId}$${latest.recordingId}` });
        } else {
          again();
        }
      },
      error: () => again(),
    });
  }
}
