import { addTo, byId, EDIT_TOOLS, EditOp, otherSeat, placeTarget, toolOf } from './edit-ops';

describe('edit ops', () => {
  it('names a place as the sandbox does: the seat, then the zone', () => {
    expect(placeTarget({ seat: 'freeps', zone: 'characters' })).toBe('freeps.characters');
    expect(placeTarget({ seat: 'shadow', zone: 'deck.bottom' })).toBe('shadow.deck.bottom');
    expect(addTo('1_50', { seat: 'freeps', zone: 'hand' })).toEqual({
      op: 'add',
      blueprint: '1_50',
      to: 'freeps.hand',
    });
  });

  it('knows the other seat', () => {
    expect(otherSeat('freeps')).toBe('shadow');
    expect(otherSeat('shadow')).toBe('freeps');
  });

  it("names a card of the page by its id as a number, and nothing that isn't one", () => {
    expect(byId('30')).toEqual({ cardId: 30 });
    expect(byId('extra30')).toBeNull();
    expect(byId('')).toBeNull();
    expect(byId('99999999999999999999')).toBeNull();
  });

  it('says the tool each edit needs, as the server checks it', () => {
    const cases: [EditOp, string][] = [
      [{ op: 'add', blueprint: '1_50', to: 'freeps.hand' }, 'add'],
      [{ op: 'copy', cardId: 1 }, 'copy'],
      [{ op: 'delete', card: 'legolas' }, 'delete'],
      [{ op: 'move', cardId: 1, to: 'freeps.discard' }, 'move'],
      [{ op: 'attach', cardId: 1, toId: 2 }, 'attach'],
      [{ op: 'stack', cardId: 1, toId: 2 }, 'stack'],
      [{ op: 'wounds', cardId: 1, amount: 2 }, 'wounds'],
      [{ op: 'tokens', cardId: 1, amount: 2, culture: 'gondor' }, 'tokens'],
      [{ op: 'twilight', set: 3 }, 'counters'],
      [{ op: 'burdens', amount: 1 }, 'counters'],
      [{ op: 'threats', amount: -1 }, 'counters'],
      [{ op: 'marker', seat: 'freeps', site: 3 }, 'markers'],
      [{ op: 'control', site: 1, seat: 'shadow' }, 'markers'],
      [{ op: 'liberate', site: 1 }, 'markers'],
      [{ op: 'site', number: 4, from: 'shadow' }, 'sites'],
      [{ op: 'modify', cardId: 1, what: 'fierce' }, 'modify'],
      [{ op: 'unmodify', id: 2 }, 'modify'],
    ];
    for (const [op, tool] of cases) expect(toolOf(op)).toBe(tool);
    expect(EDIT_TOOLS).toContain('piles');
  });
});
