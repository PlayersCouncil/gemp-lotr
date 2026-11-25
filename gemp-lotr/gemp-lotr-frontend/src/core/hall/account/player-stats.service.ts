import { inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { AccountApi } from './account-api.service';
import { PlayerStats } from './account-models';

/** What My Stats shows: nothing yet, the two tables, or that they could not be loaded. */
export type PlayerStatsState =
  { readonly kind: 'loading' } | { readonly kind: 'loaded'; readonly stats: PlayerStats } | { readonly kind: 'failed' };

/**
 * My Account > My Stats (playerStatsUi.js PlayerStatsUI): the player's wins and losses by format and deck, casual and
 * competitive, fetched once, the first time the sub-tab shows (it is not refreshed on coming back, as in the legacy
 * tab).
 */
@Injectable()
export class PlayerStatsView {
  private readonly api = inject(AccountApi);
  private readonly state_ = signal<PlayerStatsState>({ kind: 'loading' });
  private started = false;

  readonly state = this.state_.asReadonly();

  /** loadPlayerStats. The legacy page showed nothing on a failure or another root; here it says so. */
  async start(): Promise<void> {
    if (this.started) {
      return;
    }
    this.started = true;
    try {
      const stats = await firstValueFrom(this.api.playerStats());
      this.state_.set(stats ? { kind: 'loaded', stats } : { kind: 'failed' });
    } catch {
      this.state_.set({ kind: 'failed' });
    }
  }
}
