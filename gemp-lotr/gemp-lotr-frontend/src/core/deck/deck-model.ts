import { CollectionItem, Deck } from '../api/models';
import { isPlaceholder } from '../cards/card-blueprint';
import { serializeDeckContents } from './deck-contents';

/**
 * The deck being edited, as the legacy deckBuildingUi.js kept it in the page's card elements: the special slots, the
 * sites and the draw deck. Pure functions; DeckDraft holds the state.
 */

export interface DeckCard {
  /** Unique within the deck. */
  id: number;
  blueprintId: string;
}

export type DrawSide = 'FREE_PEOPLE' | 'SHADOW';

export interface DrawCopy extends DeckCard {
  side: DrawSide;
}

/** The slots that hold whole cards. The legacy page let a slot hold more than one card (e.g. from an import). */
export type DeckSlot = 'ringBearer' | 'ring' | 'map' | 'sites';

export interface DeckState {
  /** The id of the card added last. */
  lastCardId: number;
  ringBearer: readonly DeckCard[];
  ring: readonly DeckCard[];
  map: readonly DeckCard[];
  sites: readonly DeckCard[];
  /**
   * Every copy in the draw deck, in the order added. The first copy of a blueprint is the card shown in its pile; the
   * later ones are drawn behind it (the legacy "attached" cards).
   */
  draw: readonly DrawCopy[];
}

export const EMPTY_DECK: DeckState = { lastCardId: 0, ringBearer: [], ring: [], map: [], sites: [], draw: [] };

/**
 * Adds a card to a slot. A future-prize placeholder never goes into a deck (it is dropped, as the legacy page did).
 */
export function addToSlot(deck: DeckState, slot: DeckSlot, blueprintId: string): DeckState {
  if (isPlaceholder(blueprintId)) {
    return deck;
  }
  const id = deck.lastCardId + 1;
  return { ...deck, lastCardId: id, [slot]: [...deck[slot], { id, blueprintId }] };
}

/** Adds one copy to the draw deck (deckBuildingUi.addCardToDeck). */
export function addDrawCopy(deck: DeckState, blueprintId: string, side: DrawSide): DeckState {
  if (isPlaceholder(blueprintId)) {
    return deck;
  }
  const id = deck.lastCardId + 1;
  return { ...deck, lastCardId: id, draw: [...deck.draw, { id, blueprintId, side }] };
}

/**
 * Adds a collection card where its side says (addCardToDeckAndLayout): Free Peoples or Shadow pile, or the sites
 * when it has no side. Other sides are ignored.
 */
export function addBySide(deck: DeckState, blueprintId: string, side: string | null): DeckState {
  if (side === 'FREE_PEOPLE' || side === 'SHADOW') {
    return addDrawCopy(deck, blueprintId, side);
  }
  if (side === null) {
    return addToSlot(deck, 'sites', blueprintId);
  }
  return deck;
}

/** Removes a card from a slot (removeCardFromDeck). */
export function removeFromSlot(deck: DeckState, slot: DeckSlot, id: number): DeckState {
  return { ...deck, [slot]: deck[slot].filter((card) => card.id !== id) };
}

/**
 * Removes one copy of a draw deck card, given its shown card: the oldest copy behind it, or the card itself when it
 * is the last one (removeCardFromDeck).
 */
export function removeDrawCopy(deck: DeckState, shownId: number): DeckState {
  const shown = deck.draw.find((copy) => copy.id === shownId);
  if (!shown) {
    return deck;
  }
  const behind = deck.draw.find((copy) => copy.blueprintId === shown.blueprintId && copy.id !== shown.id);
  const removed = behind ?? shown;
  return { ...deck, draw: deck.draw.filter((copy) => copy !== removed) };
}

export interface DrawPileCard {
  /** The shown card. */
  card: DrawCopy;
  /** The copies behind it, oldest first. */
  behind: DrawCopy[];
}

/** The cards shown in one pile, in the order their first copy was added. */
export function drawPile(deck: DeckState, side: DrawSide): DrawPileCard[] {
  const piles = new Map<string, DrawPileCard>();
  for (const copy of deck.draw) {
    const pile = piles.get(copy.blueprintId);
    if (pile) {
      pile.behind.push(copy);
    } else {
      piles.set(copy.blueprintId, { card: copy, behind: [] });
    }
  }
  return [...piles.values()].filter((pile) => pile.card.side === side);
}

/**
 * The deckContents string (getDeckContents): the first Ring-bearer and Ring, the sites, every draw deck copy, and the
 * Map for formats that use one.
 */
export function deckContentsOf(deck: DeckState, includeMap: boolean): string {
  return serializeDeckContents(
    {
      ringBearer: deck.ringBearer[0]?.blueprintId ?? null,
      ring: deck.ring[0]?.blueprintId ?? null,
      sites: deck.sites.map((card) => card.blueprintId),
      drawDeck: deck.draw.map((copy) => copy.blueprintId),
      map: deck.map[0]?.blueprintId ?? null,
    },
    includeMap,
  );
}

/** Copies of each blueprint on the deck side of the page, including a Race card (subtracted from the collection). */
export function deckCardCounts(deck: DeckState, extra: readonly string[] = []): Map<string, number> {
  const counts = new Map<string, number>();
  const all = [deck.ringBearer, deck.ring, deck.map, deck.sites, deck.draw].flat().map((c) => c.blueprintId);
  for (const blueprintId of [...all, ...extra]) {
    counts.set(blueprintId, (counts.get(blueprintId) ?? 0) + 1);
  }
  return counts;
}

/**
 * Blueprint IDs of the cards in the deck, one per shown card (getCardsInDeck: the copies behind a card are not
 * counted), in page order. Used by the "Special in Deck" product filter.
 */
export function shownDeckBlueprints(deck: DeckState): string[] {
  const shownDraw = drawPile(deck, 'FREE_PEOPLE')
    .concat(drawPile(deck, 'SHADOW'))
    .map((pile) => pile.card)
    .sort((a, b) => deck.draw.indexOf(a) - deck.draw.indexOf(b));
  return [...deck.ringBearer, ...deck.ring, ...deck.map, ...deck.sites, ...shownDraw].map((card) => card.blueprintId);
}

/** The deck state of a deck from the server; draw cards of neither side are left out. */
export function deckStateOf(deck: Deck): DeckState {
  let state = EMPTY_DECK;
  for (const blueprintId of deck.ringBearer ? [deck.ringBearer] : []) {
    state = addToSlot(state, 'ringBearer', blueprintId);
  }
  for (const blueprintId of deck.ring ? [deck.ring] : []) {
    state = addToSlot(state, 'ring', blueprintId);
  }
  for (const blueprintId of deck.map ? [deck.map] : []) {
    state = addToSlot(state, 'map', blueprintId);
  }
  for (const blueprintId of deck.sites) {
    state = addToSlot(state, 'sites', blueprintId);
  }
  for (const { blueprintId, side } of deck.drawDeck) {
    if (side === 'FREE_PEOPLE' || side === 'SHADOW') {
      state = addDrawCopy(state, blueprintId, side as DrawSide);
    }
  }
  return state;
}

/** Adds the cards the server recognised in a decklist: each by its group or, for the draw deck, its side. */
export function addImportedCards(state: DeckState, items: readonly CollectionItem[]): DeckState {
  for (const item of items) {
    if (item.kind !== 'card') {
      continue;
    }
    for (let copy = 0; copy < item.count; copy++) {
      state =
        item.group === 'ringBearer' || item.group === 'ring' || item.group === 'map'
          ? addToSlot(state, item.group, item.blueprintId)
          : addBySide(state, item.blueprintId, item.side);
    }
  }
  return state;
}
