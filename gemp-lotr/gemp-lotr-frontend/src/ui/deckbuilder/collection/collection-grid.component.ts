import { Component, computed, inject } from '@angular/core';
import { CardViews } from '../../../core/cards/card-views.service';
import { collectionCards } from '../../../core/collection/collection-cards';
import { CollectionBrowser } from '../../../core/collection/collection-browser.service';
import { CollectionCounts } from '../../../core/collection/collection-counts.service';
import { CardComponent } from '../../shared/card/card.component';
import { cardFits } from '../../shared/fit-cards/fit-cards';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';

/**
 * The cards of the current collection page: packs first, then the cards, each with the copies still available. The
 * cards are sized to fit the box (fitCards).
 *
 * DOM contract: `.gd-collection` holding `.gd-collection__cards` (the fitCards box, `data-count` = number of cards)
 * with one `button.gd-card` per item.
 */
@Component({
  selector: 'gd-collection-grid',
  imports: [CardComponent, FitCardsDirective],
  host: { class: 'gd-collection' },
  template: `
    <div class="gd-collection__cards" [attr.data-count]="cards().length" [gdFitCards]="fitCards()">
      @for (card of cards(); track $index) {
        <button [gdCard]="card"></button>
      }
    </div>
  `,
})
export class CollectionGridComponent {
  private readonly browser = inject(CollectionBrowser);
  private readonly counts = inject(CollectionCounts);
  private readonly views = inject(CardViews);

  protected readonly cards = computed(() =>
    collectionCards(this.browser.page(), {
      views: this.views,
      available: (blueprintId, count) => this.counts.available(blueprintId, count),
    }),
  );

  protected readonly fitCards = computed(() => cardFits(this.cards()));
}
