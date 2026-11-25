import { Component, inject } from '@angular/core';
import { DeckStatsRefresher } from '../../../core/deck/deck-stats-refresher.service';

/**
 * The deck stats from the server (card counts and the format's verdict), rendered from data: the server's markup is
 * never put on the page.
 *
 * DOM contract: `.gd-stats` (`aria-live=polite`) holding a `.gd-stats__line` per line (an empty one is a blank line),
 * each with `strong` (labels) and `span` parts, `data-tone` = error, valid or warning on the parts that have one.
 */
@Component({
  selector: 'gd-deck-stats',
  styleUrl: './deck-stats.component.css',
  host: { class: 'gd-stats', 'aria-live': 'polite' },
  template: `
    @for (line of deckStats.stats().lines; track $index) {
      <div class="gd-stats__line">
        @for (part of line.segments; track $index) {
          @if (part.strong) {
            <strong [attr.data-tone]="part.tone">{{ part.text }}</strong>
          } @else {
            <span [attr.data-tone]="part.tone">{{ part.text }}</span>
          }
        }
      </div>
    }
  `,
})
export class DeckStatsComponent {
  protected readonly deckStats = inject(DeckStatsRefresher);
}
