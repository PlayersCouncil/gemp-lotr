import { Component, inject } from '@angular/core';
import {
  CASUAL_STATS_HEADING,
  COMPETITIVE_STATS_HEADING,
  NO_STATS,
  STATS_FAILED,
  STATS_HEADERS,
  STATS_INTRO,
} from '../../../core/hall/account/account-messages';
import { PlayerStatsView } from '../../../core/hall/account/player-stats.service';

/**
 * My Account > My Stats (yourStats.html, playerStatsUi.js PlayerStatsUI): "All times are GMT based.", then the casual
 * and the competitive statistics, each a table (hall.css table.tables) or a line in italics when there are none.
 *
 * DOM contract: `gd-account-stats.gd-account-stats` (`data-state` = loading | loaded | failed) holding
 * `p.gd-account-stats__intro`, and once loaded, per group (`data-group` = casual | competitive)
 * `div.gd-account-stats__heading` and `table.gd-account-stats__table` or `i.gd-account-stats__none`; on a failure
 * `p.gd-account-stats__failed`.
 */
@Component({
  selector: 'gd-account-stats',
  styleUrl: './player-stats.component.css',
  host: { class: 'gd-account-stats', '[attr.data-state]': 'stats.state().kind' },
  template: `
    <div class="gd-account-stats__article">
      <p class="gd-account-stats__intro">{{ intro }}</p>
      @switch (stats.state().kind) {
        @case ('loaded') {
          @for (group of groups(); track group.key) {
            <div class="gd-account-stats__heading" [attr.data-group]="group.key">{{ group.heading }}</div>
            @if (group.entries.length === 0) {
              <i class="gd-account-stats__none" [attr.data-group]="group.key">{{ none }}</i>
            } @else {
              <table class="gd-account-stats__table" [attr.data-group]="group.key">
                <tr>
                  @for (header of headers; track header) {
                    <th>{{ header }}</th>
                  }
                </tr>
                @for (entry of group.entries; track $index) {
                  <tr>
                    <td>{{ entry.format }}</td>
                    <td>{{ entry.deckName }}</td>
                    <td>{{ entry.wins }}</td>
                    <td>{{ entry.losses }}</td>
                    <td>{{ entry.percentage }}</td>
                  </tr>
                }
              </table>
            }
          }
        }
        @case ('failed') {
          <p class="gd-account-stats__failed">{{ failed }}</p>
        }
      }
    </div>
  `,
})
export class PlayerStatsComponent {
  protected readonly stats = inject(PlayerStatsView);
  protected readonly intro = STATS_INTRO;
  protected readonly headers = STATS_HEADERS;
  protected readonly none = NO_STATS;
  protected readonly failed = STATS_FAILED;

  protected groups() {
    const state = this.stats.state();
    if (state.kind !== 'loaded') {
      return [];
    }
    return [
      { key: 'casual', heading: CASUAL_STATS_HEADING, entries: state.stats.casual },
      { key: 'competitive', heading: COMPETITIVE_STATS_HEADING, entries: state.stats.competitive },
    ];
  }
}
