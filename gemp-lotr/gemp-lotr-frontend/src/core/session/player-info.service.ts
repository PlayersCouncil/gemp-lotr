import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, Observable, shareReplay, tap } from 'rxjs';
import { ApiError } from '../api/api-error';
import { GempHttp } from '../api/gemp-http.service';
import { parsePlayer, Player } from './player';

/** What is known about who is logged in: not asked yet or waiting, a player, or nobody (not logged in). */
export type PlayerState = { status: 'unknown' } | { status: 'player'; player: Player } | { status: 'anonymous' };

/**
 * Who is logged in (GET /player, what the legacy hall asks: communication.js getPlayerInfoShared): the session's
 * player. Asked once per page: the login guard asks it to
 * tell whether anyone is, the hall whether to start (and whether the player is an admin), and a chat wants the name
 * (it marks messages that mention the player).
 */
@Injectable({ providedIn: 'root' })
export class PlayerInfo {
  private readonly http = inject(GempHttp);
  private request: Observable<string> | null = null;
  private readonly state_ = signal<PlayerState>({ status: 'unknown' });

  readonly state = this.state_.asReadonly();

  /** The logged-in player, or null (not logged in, or not known yet). */
  readonly player = computed(() => {
    const state = this.state_();
    return state.status === 'player' ? state.player : null;
  });

  /** The player's name, once the server has answered; null before, and if it didn't say. */
  readonly name = computed(() => this.player()?.name ?? null);

  /** The server's answer, as text; asked once, shared by everyone who asks. A failure is passed on. */
  load(): Observable<string> {
    this.request ??= this.http.getText('/player').pipe(
      tap({
        next: (text) => {
          const player = parsePlayer(jsonIn(text));
          this.state_.set(player ? { status: 'player', player } : { status: 'anonymous' });
        },
        error: (error: unknown) => {
          if (ApiError.from(error).is(401)) {
            this.state_.set({ status: 'anonymous' });
          }
        },
      }),
      shareReplay(1),
    );
    return this.request;
  }

  /**
   * The logged-in player, or null for nobody (401, or an answer without a name). Any other failure rejects and
   * leaves it unknown, so a later call asks again.
   */
  whoAmI(): Promise<Player | null> {
    return firstValueFrom(this.load()).then(
      () => this.player(),
      (error: unknown) => {
        if (ApiError.from(error).is(401)) {
          return null;
        }
        this.request = null;
        throw error;
      },
    );
  }

  /** Forgets the answer and asks again (after logging in or out); the state keeps the old answer until then. */
  refresh(): Promise<Player | null> {
    this.request = null;
    return this.whoAmI();
  }
}

function jsonIn(text: string): unknown {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}
