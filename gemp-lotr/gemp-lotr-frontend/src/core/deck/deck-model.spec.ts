import {
  addBySide,
  addDrawCopy,
  addToSlot,
  deckCardCounts,
  deckContentsOf,
  DeckState,
  drawPile,
  EMPTY_DECK,
  removeDrawCopy,
  removeFromSlot,
  shownDeckBlueprints,
} from './deck-model';

const build = (...steps: ((deck: DeckState) => DeckState)[]) => steps.reduce((deck, step) => step(deck), EMPTY_DECK);

describe('deck model', () => {
  it('stacks extra copies behind the first one, per pile, in the order added', () => {
    const deck = build(
      (d) => addDrawCopy(d, '1_2', 'FREE_PEOPLE'),
      (d) => addDrawCopy(d, '1_3', 'SHADOW'),
      (d) => addDrawCopy(d, '1_2', 'FREE_PEOPLE'),
      (d) => addDrawCopy(d, '1_4', 'FREE_PEOPLE'),
      (d) => addDrawCopy(d, '1_2', 'FREE_PEOPLE'),
    );
    const fp = drawPile(deck, 'FREE_PEOPLE');
    expect(fp.map((p) => [p.card.blueprintId, p.behind.length])).toEqual([
      ['1_2', 2],
      ['1_4', 0],
    ]);
    expect(drawPile(deck, 'SHADOW').map((p) => p.card.blueprintId)).toEqual(['1_3']);
  });

  it('removes the oldest copy behind a card first, then the card', () => {
    let deck = build(
      (d) => addDrawCopy(d, '1_2', 'FREE_PEOPLE'),
      (d) => addDrawCopy(d, '1_2', 'FREE_PEOPLE'),
      (d) => addDrawCopy(d, '1_2', 'FREE_PEOPLE'),
    );
    const [first, second, third] = deck.draw;
    deck = removeDrawCopy(deck, first.id);
    expect(deck.draw).toEqual([first, third]);
    deck = removeDrawCopy(deck, first.id);
    deck = removeDrawCopy(deck, first.id);
    expect(deck.draw).toEqual([]);
    expect(second).toBeDefined();
  });

  it('adds collection cards by side: piles, or the sites when sideless', () => {
    const deck = build(
      (d) => addBySide(d, '1_2', 'FREE_PEOPLE'),
      (d) => addBySide(d, '1_319', null),
      (d) => addBySide(d, '1_9', 'SOMETHING_ELSE'),
    );
    expect(deck.draw.map((c) => c.blueprintId)).toEqual(['1_2']);
    expect(deck.sites.map((c) => c.blueprintId)).toEqual(['1_319']);
  });

  it('never puts a future-prize placeholder into the deck', () => {
    const deck = build(
      (d) => addBySide(d, '404_1', 'FREE_PEOPLE'),
      (d) => addToSlot(d, 'ringBearer', '404_2'),
    );
    expect(deck).toEqual(EMPTY_DECK);
  });

  it('serializes the deck like getDeckContents', () => {
    let deck = build(
      (d) => addToSlot(d, 'ringBearer', '1_290'),
      (d) => addToSlot(d, 'ring', '1_2'),
      (d) => addToSlot(d, 'map', '101_1'),
      (d) => addToSlot(d, 'sites', '1_319'),
      (d) => addToSlot(d, 'sites', '1_327'),
      (d) => addDrawCopy(d, '1_12', 'FREE_PEOPLE'),
      (d) => addDrawCopy(d, '1_131', 'SHADOW'),
      (d) => addDrawCopy(d, '1_12', 'FREE_PEOPLE'),
    );
    expect(deckContentsOf(deck, false)).toBe('1_290|1_2|1_319,1_327|1_12,1_131,1_12');
    expect(deckContentsOf(deck, true)).toBe('1_290|1_2|1_319,1_327|1_12,1_131,1_12|101_1');
    deck = removeFromSlot(deck, 'sites', deck.sites[0].id);
    expect(deckContentsOf(deck, false)).toBe('1_290|1_2|1_327|1_12,1_131,1_12');
    expect(deckContentsOf(EMPTY_DECK, false)).toBe('|||');
  });

  it('counts every copy, plus extra cards such as the Race card', () => {
    const deck = build(
      (d) => addToSlot(d, 'ringBearer', '1_290'),
      (d) => addDrawCopy(d, '1_12', 'FREE_PEOPLE'),
      (d) => addDrawCopy(d, '1_12', 'FREE_PEOPLE'),
    );
    expect(Object.fromEntries(deckCardCounts(deck, ['90_1']))).toEqual({ '1_290': 1, '1_12': 2, '90_1': 1 });
  });

  it('lists each shown card once for "Special in Deck"', () => {
    const deck = build(
      (d) => addDrawCopy(d, '1_131', 'SHADOW'),
      (d) => addToSlot(d, 'ring', '1_2'),
      (d) => addDrawCopy(d, '1_12', 'FREE_PEOPLE'),
      (d) => addDrawCopy(d, '1_131', 'SHADOW'),
    );
    expect(shownDeckBlueprints(deck)).toEqual(['1_2', '1_131', '1_12']);
  });
});
