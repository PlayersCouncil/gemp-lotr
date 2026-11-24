import { readEditBoard } from './edit-board';
import {
  asGameCard,
  boardPileCards,
  inBoardOrder,
  pageHasPile,
  PILE_BOARD_ZONES,
  pileSpots,
  sandboxPileAccess,
} from './edit-piles';
import { SANDBOX_PILES_STATE } from '../../../testing/table-edit-piles';
import { PileName } from '../table/game-table';

const PILES: PileName[] = ['deck', 'hand', 'revealedHand', 'discard', 'deadPile', 'adventureDeck', 'removedPile'];

describe('edit piles', () => {
  const board = readEditBoard(SANDBOX_PILES_STATE);

  it("opens every pile of a sandbox, the hidden ones (draw decks, the other's hand and adventure deck) with piles", () => {
    expect(sandboxPileAccess(true, true)).toEqual({
      deck: true,
      hand: true,
      discard: true,
      dead: true,
      adventureDeck: true,
      removed: true,
    });
    expect(sandboxPileAccess(true, false)).toMatchObject({ deck: false, hand: true, adventureDeck: true });
    expect(sandboxPileAccess(false, false)).toMatchObject({
      deck: false,
      hand: false,
      discard: true,
      dead: true,
      adventureDeck: false,
      removed: true,
    });
  });

  it("knows which piles the page's own game view has", () => {
    const own = PILES.filter((pile) => pageHasPile(pile, true, false, false));
    expect(own).toEqual(['hand', 'revealedHand', 'discard', 'deadPile', 'adventureDeck', 'removedPile']);
    const other = PILES.filter((pile) => pageHasPile(pile, false, false, false));
    expect(other).toEqual(['deadPile', 'removedPile']);
    expect(pageHasPile('discard', false, true, false)).toBe(true);
    expect(pageHasPile('hand', false, false, true)).toBe(true);
    expect(PILES.map((pile) => PILE_BOARD_ZONES[pile])).toEqual([
      'deck',
      'hand',
      'hand',
      'discard',
      'deadPile',
      'adventureDeck',
      'removed',
    ]);
  });

  it("gives a seat's pile from the board in its order, top first, as the player's cards; a card with no id is left out", () => {
    const deck = boardPileCards(board, 'deck', 'freeps', 'alice');
    expect(deck.map((card) => [card.id, card.blueprintId, card.zone, card.owner])).toEqual([
      ['40', '1_50', 'DECK', 'alice'],
      ['41', '1_97', 'DECK', 'alice'],
    ]);
    expect(boardPileCards(board, 'hand', 'shadow', 'bob').map((card) => card.id)).toEqual(['32']);
    expect(boardPileCards(board, 'adventureDeck', 'shadow', 'bob').map((card) => card.id)).toEqual(['33', '34']);
    expect(boardPileCards(board, 'removedPile', 'shadow', 'bob')).toEqual([]);
    expect(boardPileCards(null, 'deck', 'freeps', 'alice')).toEqual([]);
  });

  it('keeps the board order where cards have no place', () => {
    const cards = boardPileCards(board, 'adventureDeck', 'freeps', 'alice');
    expect(cards.map((card) => card.id)).toEqual(['15']);
    expect(asGameCard({ ...board.cards[0]!, cardId: null }, 'hand', 'alice')).toMatchObject({ id: '', zone: 'HAND' });
  });

  it("puts a card added in a pile's window in that pile: a draw deck's top or bottom, nothing in an adventure deck", () => {
    expect(PILES.map((pile) => pileSpots(pile))).toEqual([
      ['deck.top', 'deck.bottom'],
      ['hand'],
      ['hand'],
      ['discard'],
      ['deadPile'],
      [],
      ['removed'],
    ]);
  });

  it("orders the page's own cards of a pile as the board places them, top first; others keep their order, after", () => {
    const deck = boardPileCards(board, 'deck', 'freeps', 'alice');
    const stray = { ...deck[0]!, id: '99' };
    const other = { ...deck[0]!, id: '98' };
    expect(inBoardOrder([stray, deck[1]!, other, deck[0]!], board).map((card) => card.id)).toEqual([
      '40',
      '41',
      '99',
      '98',
    ]);
    expect(inBoardOrder([deck[1]!, deck[0]!], null).map((card) => card.id)).toEqual(['41', '40']);
  });
});
