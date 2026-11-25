import { formatUsesMap, serializeDeckContents } from './deck-contents';

describe('serializeDeckContents', () => {
  const deck = {
    ringBearer: '1_2',
    ring: '1_1',
    sites: ['1_319', '1_327'],
    drawDeck: ['1_3', '1_3', '1_7'],
    map: '100_1',
  };

  it('writes ringBearer|ring|sites|cards', () => {
    expect(serializeDeckContents(deck, false)).toBe('1_2|1_1|1_319,1_327|1_3,1_3,1_7');
  });

  it('adds the map for map formats', () => {
    expect(serializeDeckContents(deck, true)).toBe('1_2|1_1|1_319,1_327|1_3,1_3,1_7|100_1');
  });

  it('leaves empty parts for an empty deck, like the legacy client', () => {
    const empty = { ringBearer: null, ring: null, sites: [], drawDeck: [], map: null };
    expect(serializeDeckContents(empty, false)).toBe('|||');
    expect(serializeDeckContents(empty, true)).toBe('||||');
  });
});

describe('formatUsesMap', () => {
  it('is true only for the movie formats', () => {
    expect(formatUsesMap('pc_movie')).toBe(true);
    expect(formatUsesMap('test_pc_movie')).toBe(true);
    expect(formatUsesMap('fotr_block')).toBe(false);
    expect(formatUsesMap(null)).toBe(false);
  });
});
