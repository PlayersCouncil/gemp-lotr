import { DOCUMENT } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { CardImageService } from '../../../core/cards/card-image.service';
import { foilOverlayImage, readFoilPresentation } from '../../../core/cards/foil-presentation';
import { collectionCards } from '../../../core/collection/collection-cards';
import { CollectionService } from '../../../core/collection/collection.service';
import { CardComponent } from '../../shared/card/card.component';
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
    <div class="gd-collection__cards" [attr.data-count]="cards().length" [gdFitCards]="horizontal()">
      @for (card of cards(); track $index) {
        <button [gdCard]="card"></button>
      }
    </div>
  `,
})
export class CollectionGridComponent {
  private readonly collection = inject(CollectionService);
  private readonly images = inject(CardImageService);
  private readonly foilImage = foilOverlayImage(readFoilPresentation(inject(DOCUMENT).cookie));

  protected readonly cards = computed(() =>
    collectionCards(this.collection.page(), {
      images: this.images,
      available: (blueprintId, count) => this.collection.availableCount(blueprintId, count),
      foilImage: this.foilImage,
    }),
  );

  protected readonly horizontal = computed(() => this.cards().map((card) => card.horizontal));
}
