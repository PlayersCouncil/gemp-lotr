import { DeckbuilderCardRef, DeckbuilderCardView } from '../actions/deckbuilder-card-ref';
import { CardRole } from '../cards/card-view';
import { CardViews, CardViewSpec } from '../cards/card-views.service';
import { DeckSlot, DeckState, drawPile, DrawSide } from './deck-model';
import type { RaceCard } from './deck-format-selection.service';

/** A card of a draw deck pile with its other copies, drawn behind it (oldest first). */
export interface DeckStack {
  card: DeckbuilderCardView;
  behind: DeckbuilderCardView[];
}

/** The cards of a slot (Ring-bearer, Ring, Map) or the sites, as the legacy page showed them. */
export function slotCards(deck: DeckState, slot: DeckSlot, views: CardViews): DeckbuilderCardView[] {
  return deck[slot].map((card) =>
    // The theme puts the sites on whole pixels: the hover zoom is placed from those
    deckCard({ kind: 'slot', slot, blueprintId: card.blueprintId, card }, 'card', 'deck', views, {
      wholePixels: slot === 'sites',
    }),
  );
}

/** A draw deck pile: one stack per card, in the order their first copy was added. */
export function pileStacks(deck: DeckState, side: DrawSide, views: CardViews): DeckStack[] {
  return drawPile(deck, side).map((entry) => ({
    card: deckCard({ kind: 'draw', blueprintId: entry.card.blueprintId, card: entry.card }, 'card', side, views),
    behind: entry.behind.map((copy) =>
      deckCard({ kind: 'behind', blueprintId: copy.blueprintId, card: copy }, 'copy', 'attached', views),
    ),
  }));
}

/** The Race to Mount Doom meta-site: its position card with the modifier's lower part over it (display only). */
export function raceCardView(race: RaceCard | null, views: CardViews): DeckbuilderCardView | null {
  if (!race) {
    return null;
  }
  return deckCard(
    { kind: 'race', blueprintId: race.blueprintId, overlayBlueprintId: race.overlayBlueprintId },
    'card',
    'deck',
    views,
    { overlayBlueprintId: race.overlayBlueprintId },
  );
}

/** A deck card (Card.CreateCardDiv in a deck zone, without tokens). */
function deckCard(
  ref: DeckbuilderCardRef,
  role: CardRole,
  zone: string,
  views: CardViews,
  extra: Pick<CardViewSpec<unknown>, 'overlayBlueprintId' | 'wholePixels'> = {},
): DeckbuilderCardView {
  return views.create({ ref, blueprintId: ref.blueprintId, zone, role, ...extra });
}
