import { Component, computed, inject } from '@angular/core';
import { GameSession } from '../../../core/game/session/game-session.service';

/**
 * The stats bar under the adventure path: the fellowship's and the shadow's archery totals, and how many times the
 * fellowship has moved this turn out of how many it may.
 *
 * DOM contract: `gd-game-stats` holding `[data-game]` fp-archery, shadow-archery and move.
 */
@Component({
  selector: 'gd-game-stats',
  styleUrl: './game-stats-bar.component.css',
  template: `
    <span class="gd-game-stats__item" data-game="fp-archery">{{ archery(stats()?.fellowshipArchery) }}</span>
    <span class="gd-game-stats__item" data-game="shadow-archery">{{ archery(stats()?.shadowArchery) }}</span>
    <span class="gd-game-stats__item" data-game="move">{{ move() }}</span>
  `,
})
export class GameStatsBarComponent {
  private readonly session = inject(GameSession);
  protected readonly stats = computed(() => this.session.model().stats);
  /** The server writes "null" outside the archery phase. */
  protected archery(total: string | null | undefined): string {
    return total === null || total === undefined || total === 'null' ? '' : total;
  }

  protected readonly move = computed(() => {
    const stats = this.stats();
    return stats ? `${stats.moveCount}/${stats.moveLimit}` : '';
  });
}
