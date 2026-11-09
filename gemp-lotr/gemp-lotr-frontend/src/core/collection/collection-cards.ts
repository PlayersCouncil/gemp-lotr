import { CollectionItem, CollectionPage } from '../api/models';
import { CardImageService } from '../cards/card-image.service';
import { CollectionRole } from '../cards/card-ref';
import { CardView } from '../cards/card-view';

export interface CollectionCardsOptions {
  images: CardImageService;
  /** Copies of a card left once the deck's are taken out (CollectionService.availableCount). */
  available: (blueprintId: string, count: number) => number;
  /** Foil layer image for foil cards (from the player's foil setting), or null to show foils plain. */
  foilImage: string | null;
}

/** The cards of a collection page, as the legacy page showed them: packs first, then the cards, in server order. */
export function collectionCards(page: CollectionPage | null, options: CollectionCardsOptions): CardView[] {
  if (!page) {
    return [];
  }
  const items = [...page.items.filter((i) => i.kind === 'pack'), ...page.items.filter((i) => i.kind === 'card')];
  return items.map((item) => collectionCard(item, options));
}

function collectionCard(item: CollectionItem, { images, available, foilImage }: CollectionCardsOptions): CardView {
  const view = (role: CollectionRole, fields: Omit<CardView, 'ref' | 'role' | 'overlayImage'>): CardView => ({
    ref: { kind: 'collection', role, blueprintId: item.blueprintId, item },
    role,
    overlayImage: null,
    ...fields,
  });
  if (item.kind === 'pack') {
    const image = images.resolve(item.blueprintId, 'pack');
    return view(item.blueprintId.startsWith('(S)') ? 'selection' : 'pack', {
      imageUrl: image.imageUrl,
      horizontal: image.horizontal,
      errata: false,
      foilImage: null,
      border: false,
      count: item.count,
    });
  }
  const image = images.resolve(item.blueprintId, item.side);
  if (image.placeholder) {
    // A promised prize: shown, but not something to put in a deck, so it has no deck count
    return view('placeholder', {
      imageUrl: image.imageUrl,
      horizontal: image.horizontal,
      errata: false,
      foilImage: null,
      border: true,
      count: item.count,
    });
  }
  return view('card', {
    imageUrl: image.imageUrl,
    horizontal: image.horizontal,
    errata: image.errata,
    foilImage: image.foil ? foilImage : null,
    border: !image.pack,
    count: available(item.blueprintId, item.count),
  });
}
