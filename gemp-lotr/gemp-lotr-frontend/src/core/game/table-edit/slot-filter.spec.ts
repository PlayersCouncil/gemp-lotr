import { FINDS_NOTHING, slotFilter, slotTakes } from './slot-filter';

describe('slotFilter', () => {
  it('joins a pool and a filter that say different keys, held to the kinds of card the place takes', () => {
    expect(slotFilter('set:1-6', 'culture:ROHAN cardtype:CONDITION', 'support')).toEqual({
      filter: 'set:1-6 culture:ROHAN cardtype:CONDITION',
      exact: true,
    });
    // (no card type said: the support area's kinds)
    expect(slotFilter('set:1-6', 'side:FREE_PEOPLE', 'support').filter).toBe(
      'set:1-6 side:FREE_PEOPLE cardtype:CONDITION,POSSESSION,ARTIFACT,ALLY,FOLLOWER',
    );
    // (a hand takes anything but the Ring, a site and the cards that never go in a deck)
    expect(slotFilter('set:1-5', null, 'hand').filter).toBe(
      'set:1-5 cardtype:-THE_ONE_RING,-SITE,-MAP,-METASITE,-ADVENTURE',
    );
    expect(slotFilter(null, null, 'minions').filter).toBe('cardtype:MINION');
  });

  it('reads keys in any case, as the server does', () => {
    expect(slotFilter('Set:1-6', 'CardType:CONDITION', 'support').filter).toBe('set:1-6 cardtype:CONDITION');
  });

  it('intersects the sets both name, never widening them', () => {
    expect(slotFilter('set:1-6', 'set:4-8', 'hand').filter).toMatch(/^set:4-6 /);
    expect(slotFilter('set:1-6', 'set:2,4,9', 'hand').filter).toMatch(/^set:2,4 /);
    expect(slotFilter('set:1-3', 'set:4', 'hand')).toEqual({ filter: FINDS_NOTHING, exact: true });
    // (a block by a format's name: kept only where both name it)
    expect(slotFilter('block:fotr_block', 'block:fotr_block,ttt_block', 'hand').filter).toMatch(/^block:fotr_block /);
    // (a range too long to be read as numbers is kept as written, where both write it)
    expect(slotFilter('set:1-5000', 'set:1-5000', 'hand').filter).toMatch(/^set:1-5000 /);
  });

  it('keeps a value of a single-valued key only where both allow it', () => {
    expect(slotFilter('culture:ROHAN,GONDOR', 'culture:ROHAN', 'support').filter).toMatch(/^culture:ROHAN /);
    expect(slotFilter('culture:ROHAN', 'culture:GONDOR', 'support')).toEqual({ filter: FINDS_NOTHING, exact: true });
    expect(slotFilter('culture:-SAURON', 'culture:GONDOR,SAURON', 'support').filter).toMatch(
      /^culture:GONDOR,-SAURON /,
    );
    expect(slotFilter('culture:-SAURON', 'culture:-ISENGARD', 'support').filter).toMatch(/^culture:-SAURON,-ISENGARD /);
    expect(slotFilter('culture:ROHAN', 'culture:-ROHAN', 'support')).toEqual({ filter: FINDS_NOTHING, exact: true });
    // (the card type, with the place's kinds too)
    expect(slotFilter('cardtype:ALLY,COMPANION', null, 'support').filter).toBe('cardtype:ALLY');
    expect(slotFilter('cardtype:EVENT', null, 'support')).toEqual({ filter: FINDS_NOTHING, exact: true });
  });

  it('keeps a key a card may have several of only where both say it, and its left-out values beside them', () => {
    expect(slotFilter('keyword:ARCHER,RANGER', 'keyword:RANGER', 'hand').filter).toMatch(/^keyword:RANGER /);
    expect(slotFilter('keyword:ARCHER', 'keyword:RANGER', 'hand')).toEqual({ filter: FINDS_NOTHING, exact: true });
    // (values left out alone say nothing to the server for such a key)
    expect(slotFilter('keyword:-ARCHER', 'keyword:-RANGER', 'hand').filter).not.toMatch(/keyword/);
  });

  it('keeps both of the words every card has to have', () => {
    expect(slotFilter('name:Aldor', 'name:Soldier gametext:archery', 'hand').filter).toMatch(
      /^name:Aldor,Soldier gametext:archery /,
    );
  });

  it('keeps the side only where both say the same', () => {
    expect(slotFilter('side:FREE_PEOPLE', 'side:FREE_PEOPLE', 'hand').filter).toMatch(/^side:FREE_PEOPLE /);
    expect(slotFilter('side:FREE_PEOPLE', 'side:SHADOW', 'hand')).toEqual({ filter: FINDS_NOTHING, exact: true });
  });

  it('says a number or a phase in both is not exact, keeping the slot filter’s', () => {
    expect(slotFilter('strength:5', 'strength:6', 'characters')).toEqual({
      filter: 'strength:6 cardtype:COMPANION,ALLY',
      exact: false,
    });
    // (the sort is no bound)
    expect(slotFilter('sort:name', 'sort:twilight', 'hand').exact).toBe(true);
  });

  it('holds a site slot to its number on the path', () => {
    expect(slotFilter('set:1-5', 'set:4 cardtype:SITE', 'site', 2)).toEqual({
      filter: 'set:4 cardtype:SITE sitenumber:2',
      exact: true,
    });
    expect(slotFilter(null, 'siteNumber:3', 'site', 2)).toEqual({
      filter: 'sitenumber:2 cardtype:SITE',
      exact: false,
    });
    expect(slotFilter(null, null, 'site').filter).toBe('cardtype:SITE');
  });

  it('leaves out what is no key and value', () => {
    expect(slotFilter('  set:1  junk :x y: ', null, 'minions').filter).toBe('set:1 cardtype:MINION');
  });
});

describe('slotTakes', () => {
  it('says which kinds of card a slot of a zone can hold at all', () => {
    expect(slotTakes('support', 'condition')).toBe(true);
    expect(slotTakes('support', 'COMPANION')).toBe(false);
    expect(slotTakes('characters', 'ALLY')).toBe(true);
    expect(slotTakes('hand', 'EVENT')).toBe(true);
    expect(slotTakes('hand', 'THE_ONE_RING')).toBe(false);
    expect(slotTakes('deck', 'SITE')).toBe(false);
    expect(slotTakes('site', 'SITE')).toBe(true);
  });
});
