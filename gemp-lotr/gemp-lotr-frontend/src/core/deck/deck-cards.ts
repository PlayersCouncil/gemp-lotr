import { CardImageService } from '../cards/card-image.service';
import { CardRef } from '../cards/card-ref';
import { CardRole, CardView } from '../cards/card-view';
import { DeckSlot, DeckState, drawPile, DrawSide } from './deck-model';
import type { RaceCard } from './deck.service';

export interface DeckCardsOptions {
  images: CardImageService;
  /** Foil layer image for foil cards (from the player's foil setting), or null to show foils plain. */
  foilImage: string | null;
}

/** A card of a draw deck pile with its other copies, drawn behind it (oldest first). */
export interface DeckStack {
  card: CardView;
  behind: CardView[];
}

/** The cards of a slot (Ring-bearer, Ring, Map) or the sites, as the legacy page showed them. */
export function slotCards(deck: DeckState, slot: DeckSlot, options: DeckCardsOptions): CardView[] {
  return deck[slot].map((card) =>
    deckCard({ kind: 'slot', slot, blueprintId: card.blueprintId, card }, 'card', 'deck', options),
  );
}

/** A draw deck pile: one stack per card, in the order their first copy was added. */
export function pileStacks(deck: DeckState, side: DrawSide, options: DeckCardsOptions): DeckStack[] {
  return drawPile(deck, side).map((entry) => ({
    card: deckCard({ kind: 'draw', blueprintId: entry.card.blueprintId, card: entry.card }, 'card', side, options),
    behind: entry.behind.map((copy) =>
      deckCard({ kind: 'behind', blueprintId: copy.blueprintId, card: copy }, 'copy', 'attached', options),
    ),
  }));
}

/** The Race to Mount Doom meta-site: its position card with the modifier's lower part over it (display only). */
export function raceCardView(race: RaceCard | null, options: DeckCardsOptions): CardView | null {
  if (!race) {
    return null;
  }
  const view = deckCard(
    { kind: 'race', blueprintId: race.blueprintId, overlayBlueprintId: race.overlayBlueprintId },
    'card',
    'deck',
    options,
  );
  return { ...view, overlayImage: race.overlayBlueprintId ? options.images.imageUrl(race.overlayBlueprintId) : null };
}

/** A deck card (Card.CreateCardDiv in a deck zone, without tokens). */
function deckCard(ref: CardRef, role: CardRole, zone: string, { images, foilImage }: DeckCardsOptions): CardView {
  const image = images.resolve(ref.blueprintId, zone);
  return {
    ref,
    role,
    imageUrl: image.imageUrl,
    horizontal: image.horizontal,
    errata: image.errata,
    foilImage: image.foil ? foilImage : null,
    border: !image.pack,
    count: null,
    overlayImage: null,
  };
}
