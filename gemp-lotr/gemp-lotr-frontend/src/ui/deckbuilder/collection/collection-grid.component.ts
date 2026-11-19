import { Component, computed, inject } from '@angular/core';
import { DeckbuilderCardView } from '../../../core/actions/deckbuilder-card-ref';
import { CardSize } from '../../../core/collection/card-size';
import { CompactLayout } from '../../../core/platform/compact-layout.service';
import { CardViews } from '../../../core/cards/card-views.service';
import { collectionCards } from '../../../core/collection/collection-cards';
import { CollectionBrowser } from '../../../core/collection/collection-browser.service';
import { CollectionCounts } from '../../../core/collection/collection-counts.service';
import { CardComponent } from '../../shared/card/card.component';
import { cardFits } from '../../shared/fit-cards/fit-cards';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';

/**
 * The cards of the current collection page: packs first, then the cards, each with the copies still available. The
 * cards are sized to fit the box (fitCards); in the compact layout, at the player's card size (CardSize).
 *
 * DOM contract: `.gd-collection` holding `.gd-collection__cards` (the fitCards box, `data-count` = number of cards)
 * with one `button.gd-card` per item.
 */
@Component({
  selector: 'gd-collection-grid',
  styleUrl: './collection-grid.component.css',
  imports: [CardComponent, FitCardsDirective],
  host: { class: 'gd-collection' },
  template: `
    <div
      class="gd-collection__cards"
      [attr.data-count]="cards().length"
      [gdFitCards]="fitCards()"
      [gdFitCardsScale]="scale()"
    >
      <!-- New elements for each page, as on the legacy page: the browser then reports the card under a still mouse -->
      @for (card of cards(); track itemOf(card)) {
        <button [gdCard]="card"></button>
      }
    </div>
  `,
})
export class CollectionGridComponent {
  private readonly browser = inject(CollectionBrowser);
  private readonly counts = inject(CollectionCounts);
  private readonly views = inject(CardViews);
  private readonly cardSize = inject(CardSize);
  private readonly compact = inject(CompactLayout).active;

  /** The player's card size, in the compact layout only (the slider sets the number of cards on a page elsewhere). */
  protected readonly scale = computed(() => (this.compact() ? this.cardSize.percent() / 100 : 1));

  protected readonly cards = computed(() =>
    collectionCards(this.browser.page(), {
      views: this.views,
      available: (blueprintId, count) => this.counts.available(blueprintId, count),
    }),
  );

  /** The server's item: the same object while only the copies left change, a new one on each page. */
  protected itemOf(card: DeckbuilderCardView): unknown {
    return card.ref.kind === 'collection' ? card.ref.item : card;
  }

  protected readonly fitCards = computed(() => cardFits(this.cards()));
}
