import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { Observable, Subject, Subscription } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../api/api-error';
import { PollTimeoutError } from '../api/poll-timeout';
import { TimerHandle, Timers } from '../platform/timers';
import { HallApi } from './hall-api.service';
import {
  NOT_LOGGED_IN,
  OPENED_ELSEWHERE,
  REASON_DROPPED,
  REASON_TIMEOUT,
  REASON_UNREACHABLE,
  REASON_UNREADABLE,
  reasonStatus,
  RESUBSCRIBED,
  retrying,
  spokenConnection,
  stillRetrying,
  stoppedWithError,
} from './hall-messages';
import {
  HallConnection,
  HallConnectionAction,
  HallConnectionDetail,
  HallConnectionState,
  HallUpdate,
} from './hall-models';
import { formatServerTime } from './hall-parsers';

/** Pause between a hall answer and the next poll (hallUi.js processHall: schedulePoll(100)). */
export const NEXT_POLL_DELAY_MS = 100;
/** Retry delays after failed polls: 1 s, 2 s, 4 s, ... up to 30 s. */
export const RETRY_BASE_MS = 1000;
export const RETRY_CAP_MS = 30_000;
/** Consecutive failures after which the readout turns red "Disconnected" (retrying goes on). */
export const FAILURES_BEFORE_DISCONNECTED = 5;

/**
 * The hall long-poll (hallUi.js getHall / updateHall / schedulePoll / pollFailed), with the connection readout's
 * state. start() signs up for a channel (GET /hall) and then polls it (POST /hall/update) until stop(); every answer
 * goes out on `updates$` (the parts of the hall draw from it: tables 4.2, My Account's pocket 4.4) and schedules the
 * next poll. Lost answers (no answer, time-out, 5xx, unreadable) retry with exponential backoff; 410 (channel
 * dropped) signs up again with a fresh snapshot; 401 (not logged in), 409 (hall opened elsewhere) and other 4xx stop.
 */
@Injectable()
export class HallComms {
  private readonly api = inject(HallApi);
  private readonly timers = inject(Timers);

  private readonly connection_ = signal<HallConnection>({ state: 'connecting', detail: null, lastUpdate: null });
  /** The connection readout: state, its message and action, the server time of the last good update. */
  readonly connection = this.connection_.asReadonly();
  /** What the readout's live region last announced (it changes with the state only). */
  readonly spoken = signal(spokenConnection('connecting', null));

  /** The player's silver from the latest answer (My Account's pocket), null before the first. */
  readonly currency = signal<number | null>(null);
  /** The message of the day (server markup), kept until it changes. */
  readonly motd = signal<string | null>(null);
  /** The server clock of the latest answer, as the main bar shows it. */
  readonly serverTime = signal<string | null>(null);
  /** Server clock minus ours (ms), for the waiting tables' timers. */
  readonly serverClockOffset = signal(0);
  /** A poll answered 401, or the session found the viewer not logged in. */
  readonly loggedOut = signal(false);
  readonly shutdown = computed(() => this.connection_().state === 'shutdown');

  private readonly updates = new Subject<HallUpdate>();
  /** Every hall answer, in order. A `full` one is a snapshot: whatever was drawn before is gone. */
  readonly updates$: Observable<HallUpdate> = this.updates.asObservable();

  private readonly startedGames = new Subject<string>();
  /** Each game that started for this player, once (a re-signup after 410 repeats the running ones). */
  readonly startedGames$: Observable<string> = this.startedGames.asObservable();

  private running = false;
  private channelNumber: number | null = null;
  /** The next poll must sign up again (first poll, or the server dropped the channel). */
  private full = true;
  private resubscribed = false;
  private failures = 0;
  private timer: TimerHandle | null = null;
  private request: Subscription | null = null;
  private readonly offeredGames = new Set<string>();

  constructor() {
    inject(DestroyRef).onDestroy(() => this.stop());
  }

  /** Whether the poll is going (started and not stopped). */
  get isRunning(): boolean {
    return this.running;
  }

  /** Starts the poll with a fresh sign-up; nothing when it is already going. */
  start(): void {
    if (this.running) {
      return;
    }
    this.running = true;
    this.full = true;
    this.channelNumber = null;
    this.failures = 0;
    this.setState('connecting', null);
    this.poll();
  }

  /** Stops the poll: the pending timer and the request in flight are dropped. The readout is left as it is. */
  stop(): void {
    this.running = false;
    this.timers.clear(this.timer);
    this.timer = null;
    this.request?.unsubscribe();
    this.request = null;
  }

  /** A visitor who is not logged in: nothing polls, the readout says "Not logged in" with a login link. */
  markLoggedOut(): void {
    this.loggedOut.set(true);
    if (!this.running) {
      this.setState('loggedout', { message: null, action: 'login' });
    }
  }

  private poll(): void {
    this.timer = null;
    const channelNumber = this.full ? null : this.channelNumber;
    const full = channelNumber === null;
    const call$ = full ? this.api.signUp() : this.api.update(channelNumber);
    this.request = call$.subscribe({
      next: (update) => this.succeeded(update),
      error: (error: unknown) => this.failed(error),
    });
  }

  private schedule(delayMs: number): void {
    if (!this.running) {
      return;
    }
    this.timers.clear(this.timer);
    this.timer = this.timers.after(delayMs, () => this.poll());
  }

  private succeeded(update: HallUpdate): void {
    this.request = null;
    try {
      const detail = this.full && this.resubscribed ? { message: RESUBSCRIBED, action: null } : null;
      this.full = false;
      this.resubscribed = false;
      this.failures = 0;
      if (update.channelNumber !== null) {
        this.channelNumber = update.channelNumber;
      }
      if (update.serverTimeMs !== null) {
        this.serverClockOffset.set(update.serverTimeMs - this.timers.now());
      }
      if (update.currency !== null) {
        this.currency.set(update.currency);
      }
      if (update.motd !== null) {
        this.motd.set(update.motd);
      }
      const serverTime = this.serverTimeOf(update);
      this.serverTime.set(serverTime);
      this.updated(detail, serverTime, update.shutdown);
      this.updates.next(update);
      for (const gameId of update.newGames) {
        if (!this.offeredGames.has(gameId)) {
          this.offeredGames.add(gameId);
          this.startedGames.next(gameId);
        }
      }
    } finally {
      this.schedule(NEXT_POLL_DELAY_MS);
    }
  }

  /** The answer's serverTime, else its serverTimeMs formatted the same way, else our clock corrected. */
  private serverTimeOf(update: HallUpdate): string {
    if (update.serverTime) {
      return update.serverTime;
    }
    return formatServerTime(update.serverTimeMs ?? this.timers.now() + this.serverClockOffset());
  }

  private failed(error: unknown): void {
    this.request = null;
    const status = error instanceof ApiError ? error.status : INVALID_RESPONSE_STATUS;
    if (status === 401) {
      this.loggedOut.set(true);
      this.halt(NOT_LOGGED_IN, 'login', 'loggedout');
    } else if (status === 409) {
      this.halt(OPENED_ELSEWHERE, 'reload');
    } else if (status === 410) {
      this.full = true;
      this.resubscribed = true;
      this.channelNumber = null;
      this.retry(REASON_DROPPED);
    } else if (status === 0) {
      this.retry(error instanceof PollTimeoutError ? REASON_TIMEOUT : REASON_UNREACHABLE);
    } else if (status === INVALID_RESPONSE_STATUS) {
      this.retry(REASON_UNREADABLE);
    } else if (status >= 500) {
      this.retry(reasonStatus(status));
    } else {
      this.halt(stoppedWithError(status), 'reload');
    }
  }

  private retry(reason: string): void {
    this.failures++;
    const delay = Math.min(RETRY_CAP_MS, RETRY_BASE_MS * Math.pow(2, this.failures - 1));
    const seconds = Math.round(delay / 1000);
    if (this.failures >= FAILURES_BEFORE_DISCONNECTED) {
      this.setState('disconnected', { message: stillRetrying(reason, seconds), action: 'reload' });
    } else {
      this.setState('reconnecting', { message: retrying(reason, seconds), action: null });
    }
    this.schedule(delay);
  }

  private halt(message: string, action: HallConnectionAction, state: HallConnectionState = 'disconnected'): void {
    this.stop();
    this.setState(state, { message, action });
  }

  /** A good update: connected (or shutdown); a message given while connected stays until the connection changes. */
  private updated(detail: HallConnectionDetail | null, serverTime: string, shutdown: boolean): void {
    const current = this.connection_();
    const connected = current.state === 'connected' || current.state === 'shutdown';
    this.connection_.set({ ...current, lastUpdate: serverTime });
    this.setState(shutdown ? 'shutdown' : 'connected', detail ?? (connected ? current.detail : null));
  }

  private setState(state: HallConnectionState, detail: HallConnectionDetail | null): void {
    const current = this.connection_();
    this.connection_.set({ state, detail, lastUpdate: current.lastUpdate });
    if (state !== current.state) {
      this.spoken.set(spokenConnection(state, detail?.message ?? null));
    }
  }
}
