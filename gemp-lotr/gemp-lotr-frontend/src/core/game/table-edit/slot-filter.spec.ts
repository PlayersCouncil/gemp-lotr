import { slotKinds, slotTakes } from './slot-filter';

describe('slotKinds', () => {
  it("says the kinds of card each place takes, as the server's slot check has them", () => {
    expect(slotKinds('support')).toBe('cardtype:CONDITION,POSSESSION,ARTIFACT,ALLY,FOLLOWER');
    expect(slotKinds('characters')).toBe('cardtype:COMPANION,ALLY');
    expect(slotKinds('minions')).toBe('cardtype:MINION');
    expect(slotKinds('attached')).toBe('cardtype:CONDITION,POSSESSION,ARTIFACT,FOLLOWER');
    // (a hand, a deck, a discard pile: anything but the Ring, a site and the cards that never go in a deck)
    expect(slotKinds('hand')).toBe('cardtype:-THE_ONE_RING,-SITE,-MAP,-METASITE,-ADVENTURE');
  });

  it('holds a site slot to its number on the path, when it is known', () => {
    expect(slotKinds('site', 3)).toBe('cardtype:SITE sitenumber:3');
    expect(slotKinds('site')).toBe('cardtype:SITE');
    // (a number means nothing for any other place)
    expect(slotKinds('hand', 3)).toBe('cardtype:-THE_ONE_RING,-SITE,-MAP,-METASITE,-ADVENTURE');
  });
});

describe('slotTakes', () => {
  it('takes a card into a place only of the kinds the place holds, in any case', () => {
    expect(slotTakes('characters', 'ALLY')).toBe(true);
    expect(slotTakes('characters', 'minion')).toBe(false);
    expect(slotTakes('hand', 'EVENT')).toBe(true);
    expect(slotTakes('hand', 'SITE')).toBe(false);
    expect(slotTakes('site', 'SITE')).toBe(true);
  });
});
