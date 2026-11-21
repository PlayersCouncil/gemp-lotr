import { AuthoringBoard, BoardCard } from './authoring-state';
import {
  addAnchor,
  aliasesOf,
  cardAnchor,
  cardIdsOf,
  cardPartOf,
  fightingAnchor,
  fightingSide,
  labelPlace,
  PARTS,
  PLACES,
  smallestAt,
  withPart,
} from './picking';

const card = (alias: string, cardId: number): BoardCard => ({
  alias,
  blueprintId: '1_1',
  name: alias,
  type: 'COMPANION',
  owner: 'freeps',
  cardId,
  zone: 'characters',
  at: null,
  wounds: 0,
  tokens: 0,
  on: null,
});

const board: AuthoringBoard = {
  cards: [card('frodo', 25), card('orc', 30)],
  twilight: 0,
  burdens: 0,
  threats: 0,
  sites: { freeps: [], shadow: [] },
};

describe('picking', () => {
  it('lists the places and parts the scenario format names', () => {
    expect(PLACES).toContain('zone:self.hand');
    expect(PLACES).toContain('ui:button:reset');
    expect(new Set(PLACES).size).toBe(PLACES.length);
    expect(PARTS[0]).toBe('strength');
    expect(PARTS).toContain('shadowNumber');
  });

  it('takes the smallest thing under the point, the first of the same size, and draws its box', () => {
    const row = { left: 0, top: 0, width: 100, height: 50 };
    const cardRect = { left: 10, top: 10, width: 20, height: 30 };
    const shown = { left: 10, top: 10, width: 20, height: 10 };
    const candidates = [
      { anchor: 'zone:self.characters', rect: row },
      { anchor: 'card:frodo', rect: cardRect, box: shown },
      { anchor: 'ui:gameLog', rect: { ...cardRect } },
      { anchor: 'ui:nowhere', rect: null },
    ];
    expect(smallestAt(15, 15, candidates)).toEqual({ anchor: 'card:frodo', box: shown });
    expect(smallestAt(50, 40, candidates)).toEqual({ anchor: 'zone:self.characters', box: row });
    expect(smallestAt(100, 50, candidates)?.anchor).toBe('zone:self.characters');
    expect(smallestAt(101, 20, candidates)).toBeNull();
    expect(smallestAt(-1, 20, candidates)).toBeNull();
    expect(smallestAt(20, -1, candidates)).toBeNull();
    expect(smallestAt(20, 51, candidates)).toBeNull();
    expect(smallestAt(15, 15, [{ anchor: 'a', rect: cardRect, box: null }])).toEqual({ anchor: 'a', box: cardRect });
    expect(smallestAt(0, 0, [])).toBeNull();
  });

  it("maps the board's cards by id and by alias", () => {
    expect(aliasesOf(board)).toEqual({ '25': 'frodo', '30': 'orc' });
    expect(cardIdsOf(board)).toEqual({ frodo: '25', orc: '30' });
    expect(aliasesOf(null)).toEqual({});
    expect(cardIdsOf(undefined)).toEqual({});
  });

  it('names a card under the pointer by its alias, a site by its number, and nothing else', () => {
    const aliases = aliasesOf(board);
    expect(cardAnchor('25', aliases, null)).toBe('card:frodo');
    expect(cardAnchor('extra30', aliases, null)).toBe('card:orc');
    expect(cardAnchor('40', aliases, 3)).toBe('ui:site:3');
    expect(cardAnchor('40', aliases, null)).toBeNull();
  });

  it('says which side of the skirmish being fought a card is on', () => {
    const ids = cardIdsOf(board);
    const skirmish = { characterId: '25', minionIds: ['30'], shown: {} as never };
    expect(fightingSide('card:frodo', ids, skirmish)).toBe('freeps');
    expect(fightingSide(' card:orc ', ids, skirmish)).toBe('shadow');
    expect(fightingSide('card:frodo', ids, null)).toBeNull();
    expect(fightingSide('card:frodo', ids, { ...skirmish, characterId: null, minionIds: [] })).toBeNull();
    expect(fightingSide('card:gandalf', ids, skirmish)).toBeNull();
    expect(fightingSide('card:frodo:strength', ids, skirmish)).toBeNull();
    expect(fightingSide('zone:minions', ids, skirmish)).toBeNull();
    expect(fightingAnchor('shadow')).toBe('ui:skirmish:shadow');
  });

  it('reads and changes the part of the card a field holds', () => {
    expect(cardPartOf('card:frodo')).toEqual({ alias: 'frodo', part: '' });
    expect(cardPartOf(' card:frodo:strength ')).toEqual({ alias: 'frodo', part: 'strength' });
    expect(cardPartOf('card:frodo, card:sam')).toBeNull();
    expect(cardPartOf('card:')).toBeNull();
    expect(cardPartOf('card')).toBeNull();
    expect(cardPartOf('zone:minions')).toBeNull();
    expect(cardPartOf('')).toBeNull();
    expect(withPart('card:frodo', 'vitality')).toBe('card:frodo:vitality');
    expect(withPart('card:frodo:vitality', '')).toBe('card:frodo');
    expect(withPart('ui:phase', 'cost')).toBeNull();
  });

  it('adds an anchor to a list once', () => {
    expect(addAnchor('', 'card:frodo')).toBe('card:frodo');
    expect(addAnchor('card:frodo,  ui:phase ', 'zone:minions')).toBe('card:frodo, ui:phase, zone:minions');
    expect(addAnchor('card:frodo, ui:phase', 'ui:phase')).toBe('card:frodo, ui:phase');
  });

  it('puts the label above the box, inside the window', () => {
    expect(labelPlace({ left: 5, top: 100, width: 1, height: 1 })).toEqual({ left: 5, top: 78 });
    expect(labelPlace({ left: 5, top: 10, width: 1, height: 1 })).toEqual({ left: 5, top: 0 });
  });
});
