/**
 * The piles of a sandbox, where every pile of both seats opens (Ketura: "All of the pile icons should be clickable to
 * view what is currently in them ... including the opponent's hand"): which a player's page may open, where each
 * pile's cards come from (the page's own game view where it has them, else the table as the level knows it, read
 * from the server: the hidden piles), the board's cards of a pile in order as the page's cards, and where a card added
 * in a pile's window goes. Pure.
 */
import { GameCard } from '../state/game-model';
import { PileName } from '../table/game-table';
import { EditBoard, EditCard } from './edit-board';
import { EditSeat, EditZone } from './edit-ops';

/** Each pile as the board names its zone. */
export const PILE_BOARD_ZONES: Readonly<Record<PileName, string>> = {
  deck: 'deck',
  hand: 'hand',
  revealedHand: 'hand',
  discard: 'discard',
  deadPile: 'deadPile',
  adventureDeck: 'adventureDeck',
  removedPile: 'removed',
};

/** Each pile as the page's game view names its zone. */
const PILE_PAGE_ZONES: Readonly<Record<PileName, string>> = {
  deck: 'DECK',
  hand: 'HAND',
  revealedHand: 'HAND',
  discard: 'DISCARD',
  deadPile: 'DEAD',
  adventureDeck: 'ADVENTURE_DECK',
  removedPile: 'REMOVED',
};

/** The piles a counter of the game state panel opens. */
export interface PileAccess {
  deck: boolean;
  hand: boolean;
  discard: boolean;
  dead: boolean;
  adventureDeck: boolean;
  removed: boolean;
}

/**
 * Which of a player's piles the page of a sandbox opens: every one, but the hidden ones (both draw decks, the other
 * seat's hand and adventure deck) only with the `piles` tool, as the server lists them only then.
 */
export function sandboxPileAccess(own: boolean, piles: boolean): PileAccess {
  return { deck: piles, hand: own || piles, discard: true, dead: true, adventureDeck: own || piles, removed: true };
}

/**
 * Whether the page's own game view has a pile's cards (the server sends it the player's own hand, discard pile and
 * adventure deck, every dead and removed pile, a public discard pile, a revealed hand); the others come from the
 * board.
 */
export function pageHasPile(pile: PileName, own: boolean, discardPublic: boolean, revealed: boolean): boolean {
  switch (pile) {
    case 'hand':
    case 'revealedHand':
      return own || revealed;
    case 'discard':
      return own || discardPublic;
    case 'adventureDeck':
      return own;
    case 'deck':
      return false;
    case 'deadPile':
    case 'removedPile':
      return true;
  }
}

/** A card of the board as the page's game view has a card (for the piles the page itself is not shown). */
export function asGameCard(card: EditCard, pile: PileName, player: string): GameCard {
  return {
    id: card.cardId ?? '',
    blueprintId: card.blueprintId,
    zone: PILE_PAGE_ZONES[pile],
    owner: player,
    hindered: false,
    numbersHidden: false,
    index: null,
    attachedTo: null,
    stackedOn: null,
    attachedAt: 0,
    tokens: {},
    inactive: false,
    assignedTo: null,
    inSkirmish: false,
    testingText: null,
    backSideTestingText: null,
  };
}

/**
 * A seat's pile as the board lists it, in its order (a pile's top first, a hand as it lies), as the page's cards of
 * the player; a card the board gives no card id is left out (nothing could name it).
 */
export function boardPileCards(board: EditBoard | null, pile: PileName, seat: EditSeat, player: string): GameCard[] {
  const zone = PILE_BOARD_ZONES[pile];
  const place = (card: EditCard) => card.at ?? Number.MAX_SAFE_INTEGER;
  return (board?.cards ?? [])
    .filter((card) => card.zone === zone && card.owner === seat && card.cardId !== null)
    .map((card, order) => ({ card, order }))
    .sort((a, b) => place(a.card) - place(b.card) || a.order - b.order)
    .map(({ card }) => asGameCard(card, pile, player));
}

/** Where a card chosen at a pile window's open spots goes: a draw deck's top and bottom; none for an adventure deck. */
export function pileSpots(pile: PileName): readonly EditZone[] {
  switch (pile) {
    case 'deck':
      return ['deck.top', 'deck.bottom'];
    case 'hand':
    case 'revealedHand':
      return ['hand'];
    case 'discard':
      return ['discard'];
    case 'deadPile':
      return ['deadPile'];
    case 'removedPile':
      return ['removed'];
    case 'adventureDeck':
      // (the server adds no card to an adventure deck: a site goes onto the path, see edit-path.ts)
      return [];
  }
}

/**
 * A pile of the page's own game view in the order the board gives it (a pile's top first, as a pile from the board
 * is), so every window of a sandbox reads the same way; cards the board does not place keep the page's order, after.
 */
export function inBoardOrder(cards: readonly GameCard[], board: EditBoard | null): GameCard[] {
  const places = new Map((board?.cards ?? []).map((card) => [card.cardId, card.at ?? null]));
  const place = (card: GameCard) => places.get(card.id) ?? Number.MAX_SAFE_INTEGER;
  return cards
    .map((card, order) => ({ card, order }))
    .sort((a, b) => place(a.card) - place(b.card) || a.order - b.order)
    .map(({ card }) => card);
}
