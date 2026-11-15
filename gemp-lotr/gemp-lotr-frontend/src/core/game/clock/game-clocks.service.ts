import { computed, DestroyRef, effect, inject, Injectable, signal, untracked } from '@angular/core';
import { PageCues } from '../../platform/page-cues';
import { GameSession } from '../session/game-session.service';
import { GameSettings } from '../settings/game-settings';
import { bankBand, bankCue, ClockBand, decisionBand, decisionCue } from './timer-cues';

export interface ClocksView {
  /** Each player's bank, in seconds, as last known (the player's own counts down while they decide). */
  banks: Readonly<Record<string, number>>;
  /** The page's player's bank band, if the timer applies to them. */
  ownBand: ClockBand;
  /** Players whose clock ran out and decided the game. */
  expired: readonly string[];
  /** Seconds the decision has taken, and its limit (0: none known); null before the server said. */
  decision: { elapsed: number; limit: number } | null;
  decisionBand: ClockBand;
  decisionExpired: boolean;
}

/**
 * The game's clocks as the page shows them (the legacy page's timer). The server sends them with each answer; while a
 * decision waits for the page's player, their bank clock and the decision's clock also count each second on the page,
 * coloured as they run low, and call for attention as the settings say (a pulse, the alert sound unless muted). A
 * spectator gets no cues. Page-scoped.
 */
@Injectable()
export class GameClocks {
  private readonly session = inject(GameSession);
  private readonly settings = inject(GameSettings);
  private readonly cues = inject(PageCues);

  /** The player's bank and the decision's elapsed time as counted here since the server's last values. */
  private readonly counted = signal<{ bank: number | null; elapsed: number | null }>({ bank: null, elapsed: null });
  private readonly expiredLocally = signal(false);
  /** Grows each time a clock should pulse: the view replays its animation. */
  readonly bankPulse = signal(0);
  readonly decisionPulse = signal(0);

  private timer: ReturnType<typeof setInterval> | null = null;

  /** The timer's cues apply to a player of the game (not a spectator). */
  private readonly applies = computed(() => {
    const model = this.session.model();
    return model.started && !model.spectator && !this.session.replaying();
  });

  readonly view = computed<ClocksView>(() => {
    const model = this.session.model();
    const own = model.participantId;
    const counted = this.counted();
    const banks: Record<string, number> = {};
    for (const player of model.players) {
      const value = player === own && this.applies() && counted.bank !== null ? counted.bank : model.clocks[player];
      if (value !== undefined) banks[player] = value;
    }
    const limit = model.clocks['decisionLimit'] ?? 0;
    const elapsed = counted.elapsed ?? model.clocks['decisionClock'];
    const remaining = limit > 0 && elapsed !== undefined ? limit - elapsed : null;
    const ended = model.ended || this.expiredLocally();
    const ownBank = own !== null ? banks[own] : undefined;
    return {
      banks,
      ownBand: this.applies() && ownBank !== undefined && !ended ? bankBand(ownBank) : null,
      expired: ended ? model.players.filter((player) => (banks[player] ?? 1) <= 0) : [],
      decision: elapsed === undefined ? null : { elapsed, limit },
      decisionBand: this.applies() && !ended ? decisionBand(remaining, limit) : null,
      decisionExpired: ended && limit > 0 && elapsed !== undefined && elapsed >= limit,
    };
  });

  constructor() {
    // The server's values replace what was counted (each answer's clocks are a new object)
    let seen: unknown = null;
    effect(() => {
      const clocks = this.session.model().clocks;
      if (clocks === seen) return;
      seen = clocks;
      untracked(() => {
        this.counted.set({ bank: null, elapsed: null });
        // The server disagreed with a clock that ran out here (or the game's end will say so)
        this.expiredLocally.set(false);
      });
    });
    // A decision for the player starts the count; an answer or the end stops it
    effect(() => {
      const model = this.session.model();
      const waiting = model.decision?.arrivedAt ?? null;
      const ended = model.ended;
      untracked(() => (waiting !== null && !ended && this.applies() ? this.start() : this.stop()));
    });
    inject(DestroyRef).onDestroy(() => this.stop());
  }

  private start(): void {
    this.stop();
    this.timer = setInterval(() => this.tick(), 1000);
  }

  private stop(): void {
    if (this.timer !== null) clearInterval(this.timer);
    this.timer = null;
  }

  private tick(): void {
    const model = this.session.model();
    const own = model.participantId ?? '';
    const view = untracked(this.view);
    const previousBank = view.banks[own];
    const previousElapsed = view.decision?.elapsed;
    if (previousBank === undefined || previousElapsed === undefined) return;
    const bank = previousBank - 1;
    const elapsed = previousElapsed + 1;
    const limit = view.decision?.limit ?? 0;
    this.counted.set({ bank, elapsed });
    // A clock that ran out has decided the game: it stops and turns red without waiting for the server
    if (bank <= -1 || (limit > 0 && limit - elapsed <= -1)) {
      this.expiredLocally.set(true);
      this.stop();
      return;
    }
    const cue = bankCue(previousBank, bank, this.settings.timerAlertRules());
    if (cue) this.bankPulse.update((n) => n + 1);
    if (cue === 'sound' && !this.settings.timerAudioMuted()) this.cues.play('timerAlert');
    if (limit > 0 && decisionCue(limit - previousElapsed, limit - elapsed)) {
      this.decisionPulse.update((n) => n + 1);
      if (!this.settings.timerAudioMuted()) this.cues.play('timerAlert');
    }
  }
}
