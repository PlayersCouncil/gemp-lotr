import { draftState, position } from '../../../testing/authoring';
import { currentOf, Selection, settle } from './selection';

const none: Selection = {
  selected: null,
  selectedUid: null,
  sub: null,
  subAt: null,
  follow: true,
  atUid: null,
  hold: false,
  jump: false,
};

describe('currentOf', () => {
  it('is the step in hand, one under way, or else the last played; in a block, its step in hand', () => {
    expect(currentOf(draftState())).toEqual({ current: 1, sub: null, here: 's1' });
    expect(currentOf(draftState({ position: position({ step: 2, inHand: false }) }))).toEqual({
      current: 1,
      sub: null,
      here: 's1',
    });
    expect(currentOf(draftState({ position: position({ step: 2, inHand: false, underWay: true }) })).current).toBe(2);
    expect(currentOf(draftState({ position: position({ step: 3, sub: 2 }) }))).toEqual({
      current: 3,
      sub: 's3k2',
      here: 's3/s3k2',
    });
    expect(currentOf(draftState({ position: position({ step: 9, inHand: false }) })).current).toBe(3);
    expect(currentOf(draftState({ draft: { id: 'x', steps: [] } }))).toEqual({ current: null, sub: null, here: null });
    expect(currentOf(draftState({ uids: [] })).here).toBeNull();
  });
});

describe('settle', () => {
  it('selects the step the game is at when nothing is selected, and remembers where the game was', () => {
    expect(settle(draftState(), none, false)).toEqual({
      selection: { ...none, selected: 1, selectedUid: 's1', atUid: 's1' },
      due: false,
    });
  });

  it('follows the game to another step, unless the author is busy in the Step tab (then it is due)', () => {
    const was = { ...none, selected: 2, selectedUid: 's2', atUid: 's1' };
    const moved = draftState({ position: position({ step: 3, sub: 1 }) });
    expect(settle(moved, was, false).selection).toMatchObject({ selected: 3, sub: 's3k1', atUid: 's3/s3k1' });
    expect(settle(moved, was, true)).toEqual({ selection: was, due: true });
    // (not following: the selection stays, and where the game is is remembered)
    expect(settle(moved, { ...was, follow: false }, false).selection).toMatchObject({ selected: 2, atUid: 's3/s3k1' });
    // (the game where it was: nothing to follow)
    expect(settle(draftState(), was, false).selection.selected).toBe(2);
  });

  it("turns to the game's step at once when asked to (jump), busy or not", () => {
    const was = { ...none, selected: 2, selectedUid: 's2', atUid: 's1', jump: true };
    expect(settle(draftState(), was, true)).toEqual({
      selection: { ...was, selected: 1, selectedUid: 's1', jump: false },
      due: false,
    });
  });

  it("finds the step selected by its name where the steps have moved, and a block's step by its name", () => {
    const was = { ...none, selected: 1, selectedUid: 's2', atUid: 's1' };
    expect(settle(draftState(), was, false).selection.selected).toBe(2);
    const inBlock = { ...none, selected: 1, selectedUid: 's1', sub: 's3k2', atUid: 's1' };
    expect(settle(draftState(), inBlock, false).selection).toMatchObject({
      selected: 3,
      selectedUid: 's3',
      sub: 's3k2',
    });
    // (a block's step taken out of its block is a step of the script)
    const out = draftState({ uids: ['s1', 's3k2', 's3'], kids: [[], [], ['s3k1']] });
    expect(settle(out, inBlock, false).selection).toMatchObject({ selected: 2, sub: null });
    // (gone: the game's step)
    expect(
      settle(draftState(), { ...none, selected: 9, selectedUid: 'gone', atUid: 's1' }, false).selection.selected,
    ).toBe(1);
  });

  it("keeps the step a change asked for (hold), with the block's step at the place asked for", () => {
    const held = { ...none, selected: 3, selectedUid: null, hold: true, subAt: 1, atUid: 'elsewhere' };
    expect(settle(draftState(), held, false)).toEqual({
      selection: { ...held, selectedUid: 's3', sub: 's3k2', hold: false, subAt: null, atUid: 's1' },
      due: false,
    });
    expect(settle(draftState(), { ...held, subAt: null }, false).selection).toMatchObject({ selected: 3, sub: null });
    expect(settle(draftState(), { ...held, subAt: 7 }, false).selection.sub).toBeNull();
    // (a held selection past the end is lost: the game's step)
    expect(settle(draftState(), { ...held, selected: 9 }, false).selection.selected).toBe(1);
  });

  it("drops a block's step that is not the selected step's", () => {
    const odd = { ...none, selected: 1, selectedUid: 's1', sub: 'nope', atUid: 's1' };
    expect(settle(draftState(), odd, false).selection.sub).toBeNull();
    const held = { ...none, selected: 1, selectedUid: null, hold: true, subAt: 0, atUid: 's1' };
    expect(settle(draftState(), held, false).selection.sub).toBeNull();
    expect(settle(draftState({ draft: { id: 'x', steps: [] }, uids: [] }), odd, false).selection).toMatchObject({
      selected: null,
      selectedUid: null,
      sub: null,
    });
  });
});
