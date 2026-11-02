import { DOCUMENT } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { CollectionItem } from '../core/api/models';
import { CardImageService } from '../core/cards/card-image.service';
import { foilOverlayImage, readFoilPresentation } from '../core/cards/foil-presentation';
import { CollectionService } from '../core/collection/collection.service';
import { PageLayoutService } from '../layout/page-layout.service';
import { CardComponent, CardFace } from '../cards/card.component';
import { CardRect, groupArea, GroupBounds, layoutNormalGroup } from '../cards/card-group-layout';

/** Space the legacy layoutUI kept for the filter above the cards: the group is the pane's height minus 400. */
const FILTER_ALLOWANCE = 400;
const PADDING = 5;
/** NormalCardGroup.maxCardHeight of the collection. */
const MAX_CARD_HEIGHT = 200;

/** The role class of an item, which the deck builder's click handling keys on (deckBuildingUi.addCardToCollection). */
export type CollectionItemRole =
  'cardInCollection' | 'packInCollection' | 'selectionInCollection' | 'placeholderInCollection';

export interface CollectionCardView {
  item: CollectionItem;
  role: CollectionItemRole;
  face: CardFace;
  rect: CardRect;
}

/**
 * The cards of the current collection page (#collection-display): the group's background box and the cards laid out
 * like the legacy NormalCardGroup, packs first, each with the copies still available.
 */
@Component({
  selector: 'div[gempCollectionDisplay]',
  imports: [CardComponent],
  template: `
    <div class="ui-widget-content" [style]="boxStyle()"></div>
    @for (view of views(); track $index) {
      <div [gempCard]="view.face" [rect]="view.rect" [class]="view.role"></div>
    }
  `,
})
export class CollectionDisplayComponent {
  private readonly collection = inject(CollectionService);
  private readonly images = inject(CardImageService);
  private readonly layout = inject(PageLayoutService);
  private readonly foilImage = foilOverlayImage(readFoilPresentation(inject(DOCUMENT).cookie));

  /** layoutUI: normalCollectionGroup.setBounds(0, 0, collectionWidth - padding * 2, collectionHeight - 400). */
  private readonly bounds = computed<GroupBounds>(() => {
    const page = this.layout.page();
    return {
      left: 0,
      top: 0,
      width: page.eastContentWidth - PADDING * 2,
      height: page.eastContentHeight - FILTER_ALLOWANCE,
    };
  });

  protected readonly boxStyle = computed(() => {
    const bounds = this.bounds();
    // jQuery ignored negative sizes; such a short window leaves the box empty
    return {
      'border-radius': '7px',
      left: `${bounds.left}px`,
      top: `${bounds.top}px`,
      width: `${Math.max(0, bounds.width)}px`,
      height: `${Math.max(0, bounds.height)}px`,
      position: 'absolute',
    };
  });

  protected readonly views = computed<CollectionCardView[]>(() => {
    const page = this.collection.page();
    if (!page) {
      return [];
    }
    // The legacy page added the packs first, then the cards
    const items = [...page.items.filter((i) => i.kind === 'pack'), ...page.items.filter((i) => i.kind === 'card')];
    const faces = items.map((item) => this.faceOf(item));
    const rects = layoutNormalGroup(
      faces.map((f) => ({ horizontal: f.horizontal })),
      groupArea(this.bounds()),
      { maxCardHeight: MAX_CARD_HEIGHT },
    );
    return items.map((item, i) => ({
      item,
      role: faces[i].role,
      face: faces[i].face,
      rect: rects[i].card,
    }));
  });

  private faceOf(item: CollectionItem): {
    role: CollectionItemRole;
    face: CardFace;
    horizontal: boolean;
  } {
    if (item.kind === 'pack') {
      const image = this.images.resolve(item.blueprintId, 'pack');
      return {
        role: item.blueprintId.startsWith('(S)') ? 'selectionInCollection' : 'packInCollection',
        horizontal: image.horizontal,
        face: {
          imageUrl: image.imageUrl,
          errata: false,
          foilImage: null,
          tokens: true,
          noBorder: true,
          count: item.count,
        },
      };
    }
    const image = this.images.resolve(item.blueprintId, item.side);
    if (image.placeholder) {
      // A promised prize: shown, but not something to put in a deck, so it has no deck count
      return {
        role: 'placeholderInCollection',
        horizontal: image.horizontal,
        face: {
          imageUrl: image.imageUrl,
          errata: false,
          foilImage: null,
          tokens: true,
          noBorder: false,
          count: item.count,
        },
      };
    }
    return {
      role: 'cardInCollection',
      horizontal: image.horizontal,
      face: {
        imageUrl: image.imageUrl,
        errata: image.errata,
        foilImage: image.foil ? this.foilImage : null,
        tokens: true,
        noBorder: image.pack,
        count: this.collection.availableCount(item.blueprintId, item.count),
      },
    };
  }
}
