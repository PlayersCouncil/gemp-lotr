import { CardImageService } from '../cards/card-image.service';
import { CardView } from '../cards/card-view';
import { ShownCard } from './packs.service';

export interface ShownCardsOptions {
  images: CardImageService;
  /** Foil layer image for foil cards (from the player's foil setting), or null to show foils plain. */
  foilImage: string | null;
}

/**
 * The choices of a selection pack ("Choose one"): drawn like collection cards, without counts; using one picks it
 * (`choosable`).
 */
export function selectionChoiceCards(choices: readonly string[], { images, foilImage }: ShownCardsOptions): CardView[] {
  return choices.map((blueprintId) => {
    const image = images.resolve(blueprintId, 'selection');
    return {
      ref: { kind: 'shown', blueprintId, zone: 'selection', choosable: true },
      role: 'card',
      imageUrl: image.imageUrl,
      horizontal: image.horizontal,
      errata: image.errata,
      foilImage: image.foil ? foilImage : null,
      border: !image.pack,
      count: null,
      overlayImage: null,
    };
  });
}

/**
 * What was opened ("New items"): packs, then cards, each with its count; never borderless, and packs never marked as
 * errata (deckbuilderDeliveryService.js).
 */
export function deliveredCards(cards: readonly ShownCard[], { images, foilImage }: ShownCardsOptions): CardView[] {
  return cards.map((card) => {
    const image = images.resolve(card.blueprintId, 'player');
    return {
      ref: { kind: 'shown', blueprintId: card.blueprintId, zone: 'player', choosable: false },
      role: card.pack ? 'pack' : 'card',
      imageUrl: image.imageUrl,
      horizontal: image.horizontal,
      errata: !card.pack && image.errata,
      foilImage: image.foil ? foilImage : null,
      border: true,
      count: card.count,
      overlayImage: null,
    };
  });
}
