import { SANDBOX_STATE } from '../../../testing/table-edit';
import { cardByAlias, EditBoard, EditCard, readEditBoard } from './edit-board';
import {
  cardMenu,
  cardsInGame,
  cultureLabel,
  fits,
  menuHasEdits,
  modifyAmount,
  modifyOp,
  nameOf,
  placeBound,
  placeLabel,
} from './edit-menu';
import { EDIT_TOOLS, EditTool } from './edit-ops';

const board: EditBoard = readEditBoard(SANDBOX_STATE);
const card = (alias: string): EditCard => cardByAlias(board, alias)!;
const all = (): boolean => true;
const only =
  (...tools: EditTool[]) =>
  (tool: EditTool) =>
    tools.includes(tool);

describe('edit menu', () => {
  it('says which cards fit a place, as the server takes them', () => {
    expect(fits('COMPANION', 'characters')).toBe(true);
    expect(fits('ally', 'characters')).toBe(false);
    expect(fits('MINION', 'minions')).toBe(true);
    expect(fits('companion', 'minions')).toBe(false);
    expect(fits('POSSESSION', 'support')).toBe(true);
    expect(fits('ALLY', 'support')).toBe(true);
    expect(fits('EVENT', 'support')).toBe(false);
    expect(fits('MINION', 'support')).toBe(false);
    expect(fits('EVENT', 'hand')).toBe(true);
    expect(fits('SITE', 'hand')).toBe(false);
    expect(fits('THE_ONE_RING', 'discard')).toBe(false);
  });

  it("bounds a place's search by the card types it takes", () => {
    expect(placeBound('characters')).toEqual({ filter: 'cardType:COMPANION', label: 'Companions' });
    expect(placeBound('minions').filter).toBe('cardType:MINION');
    expect(placeBound('support').filter).toBe('cardType:-COMPANION,-MINION,-EVENT,-SITE,-THE_ONE_RING');
    expect(placeBound('hand')).toEqual({ filter: 'cardType:-SITE,-THE_ONE_RING', label: 'No sites' });
    expect(placeLabel({ seat: 'shadow', zone: 'deadPile' })).toBe('Dead pile');
  });

  it('names a culture as a menu shows it', () => {
    expect(cultureLabel('gondor')).toBe('Gondor');
    expect(cultureLabel('uruk_hai')).toBe('Uruk-hai');
  });

  it('names a card by its id, else by its name in the scenario', () => {
    expect(nameOf(card('boromir'))).toEqual({ cardId: 30 });
    expect(nameOf({ ...card('boromir'), cardId: null })).toEqual({ card: 'boromir' });
    expect(nameOf({ ...card('boromir'), cardId: 'extra30' })).toEqual({ card: 'boromir' });
  });

  it("offers a companion in play everything but Attach: its owner's other places, wounds, tokens, its modifiers", () => {
    const menu = cardMenu(board, card('boromir'), all);
    expect(menu.sendTo.map((place) => place.to)).toEqual([
      'freeps.hand',
      'freeps.deck.top',
      'freeps.deck.bottom',
      'freeps.discard',
      'freeps.deadPile',
      'freeps.removed',
    ]);
    expect(menu.sendTo[0]?.label).toBe('Hand');
    expect(menu.attach).toBe(false);
    expect(menu.stack).toBe(true);
    expect(menu.add?.wounds).toBe(true);
    // (its own culture first, then the others in the state's order)
    expect(menu.add?.tokens.map((token) => [token.culture, token.count, token.own])).toEqual([
      ['gondor', 2, true],
      ['dwarven', 0, false],
      ['elven', 0, false],
      ['shire', 0, false],
      ['moria', 0, false],
      ['uruk_hai', 0, false],
    ]);
    expect(menu.modify?.words.map((word) => word.what)).toEqual(['strength', 'resistance', 'damage']);
    expect(menu.modify?.present.map((modifier) => modifier.text)).toEqual(['Strength +2']);
    expect(menu.copy && menu.delete).toBe(true);
    expect(menuHasEdits(menu)).toBe(true);
  });

  it('offers a possession Attach and support, a minion in hand neither wounds nor tokens nor modifiers', () => {
    const armor = cardMenu(board, card('armor'), all);
    expect(armor.attach).toBe(true);
    expect(armor.sendTo.map((place) => place.zone)).toContain('support');
    expect(armor.add?.wounds).toBe(false);
    const runner = cardMenu(board, card('goblinrunner'), all);
    expect(runner.sendTo.map((place) => place.to)).toEqual([
      'shadow.minions',
      'shadow.deck.top',
      'shadow.deck.bottom',
      'shadow.discard',
      'shadow.deadPile',
      'shadow.removed',
    ]);
    expect(runner.add).toBeNull();
    expect(runner.modify).toBeNull();
  });

  it('offers a site nothing (it has edits of its own), and the Ring and the Ring-bearer nothing that moves them', () => {
    const site = cardMenu(board, card('site1'), all);
    expect(menuHasEdits(site)).toBe(false);
    const ring = cardMenu(board, card('ring'), all);
    expect([ring.sendTo, ring.attach, ring.stack, ring.copy, ring.delete]).toEqual([[], false, false, false, false]);
    const frodo = cardMenu(board, card('frodo'), all);
    expect([frodo.sendTo, frodo.stack, frodo.copy, frodo.delete]).toEqual([[], false, false, false]);
    // (what the server takes on him is still offered: wounds, tokens, modifiers)
    expect(frodo.add?.wounds).toBe(true);
    expect(frodo.modify).not.toBeNull();
    // A card the server does not call either is offered them as before
    const plain = { ...card('frodo'), ringBearer: false };
    expect(cardMenu(board, plain, all).delete).toBe(true);
  });

  it('offers each entry only where the level allows its tool', () => {
    const none = cardMenu(board, card('boromir'), () => false);
    expect(menuHasEdits(none)).toBe(false);
    const some = cardMenu(board, card('boromir'), only('tokens', 'copy'));
    expect(some.sendTo).toEqual([]);
    expect(some.add?.wounds).toBe(false);
    expect(some.add?.tokens.length).toBe(6);
    expect(some.modify).toBeNull();
    expect(some.copy).toBe(true);
    expect(some.delete).toBe(false);
    expect(EDIT_TOOLS.every((tool) => only(tool)(tool))).toBe(true);
  });

  it('offers no tokens for a culture the state does not name, and no Modify where nothing suits the card', () => {
    const odd: EditCard = { ...card('armor'), culture: 'nowhere' };
    expect(cardMenu(board, odd, all).add?.tokens.map((token) => token.own)).toEqual([
      false,
      false,
      false,
      false,
      false,
      false,
    ]);
    const bare: EditBoard = { ...board, cultures: [], modify: { words: [], until: ['removed'] } };
    expect(cardMenu(bare, card('armor'), all).add).toBeNull();
    expect(cardMenu(bare, card('armor'), all).modify).toBeNull();
    // (what is on a card can still be taken off)
    expect(cardMenu(bare, card('boromir'), all).modify?.present.length).toBe(1);
  });

  it('puts a modifier on with the number its word takes, and how long', () => {
    const [strength, , damage, fierce] = board.modify.words;
    expect(modifyAmount(strength!, 2.7)).toBe(2);
    expect(modifyAmount(strength!, 0)).toBeNull();
    expect(modifyAmount(damage!, -3)).toBe(1);
    expect(modifyAmount(fierce!, 5)).toBeNull();
    const boromir = card('boromir');
    expect(modifyOp(boromir, strength!, -1, 'turn')).toEqual({
      op: 'modify',
      cardId: 30,
      what: 'strength',
      amount: -1,
      until: 'turn',
    });
    expect(modifyOp(boromir, strength!, 0, 'turn')).toBeNull();
    expect(modifyOp(boromir, damage!, 2, 'removed')).toEqual({ op: 'modify', cardId: 30, what: 'damage', amount: 2 });
    expect(modifyOp(boromir, fierce!, 1, 'phase')).toEqual({
      op: 'modify',
      cardId: 30,
      what: 'fierce',
      until: 'phase',
    });
  });

  it('lists one card of each kind in the game, by name, not the sites or the Ring', () => {
    expect(cardsInGame(board).map((found) => found.name)).toEqual([
      'Armor',
      'Boromir, Son of Denethor',
      'Frodo, Son of Drogo',
      'Goblin Runner',
    ]);
    const twice: EditBoard = { ...board, cards: [...board.cards, { ...card('boromir'), alias: 'boromir2' }] };
    expect(cardsInGame(twice)).toHaveLength(4);
    expect(cardsInGame(null)).toEqual([]);
  });
});
