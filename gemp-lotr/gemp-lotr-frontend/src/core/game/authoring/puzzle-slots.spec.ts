import { puzzleSlot } from '../../../testing/authoring';
import { Draft } from './authoring-state';
import {
  addSlotPatch,
  boundQuery,
  onACard,
  removeSlotPatch,
  slotBoundPatch,
  slotChanged,
  slotIdProblem,
  slotNumber,
  slotOf,
  slotPatch,
  slotRows,
  slotSeatOptions,
  slotsWith,
  slotWritten,
} from './puzzle-slots';

/** Puzzle 10's slots, and a hand slot that says only where (03's `hand1: hand`), and one solution. */
const DRAFT: Draft = {
  id: 'test',
  human: 'freeps',
  steps: [],
  pool: 'set:1-6',
  slots: {
    condition: { zone: 'support', filter: 'culture:ROHAN cardtype:CONDITION', label: 'a Rohan condition' },
    hand1: 'hand',
  },
  solutions: [{ name: 'published', picks: { condition: '5_87', hand1: '1_2' }, answers: ['use(condition)'] }],
};

describe('puzzle-slots', () => {
  it('lists the slots in the file’s order, each spelt out, with the state’s word on it', () => {
    const info = puzzleSlot('condition');
    expect(slotRows(DRAFT, [info])).toEqual([
      { id: 'condition', slot: DRAFT.slots!['condition'], info },
      { id: 'hand1', slot: { zone: 'hand' }, info: null },
    ]);
    expect(slotRows({ id: 'x' }, [])).toEqual([]);
    expect(slotOf(undefined)).toEqual({ zone: 'hand' });
  });

  it('writes a slot as the writer does: what is not the default, in its order; a zone alone as that', () => {
    expect(slotWritten({ zone: 'hand' }, 'freeps')).toBe('hand');
    expect(
      slotWritten(
        {
          label: ' a trap ',
          wounds: 1,
          tokens: 3,
          filter: ' side:FREE_PEOPLE ',
          position: 3,
          on: 'frodo',
          seat: 'shadow',
          zone: 'attached',
        },
        'freeps',
      ),
    ).toEqual({
      zone: 'attached',
      seat: 'shadow',
      on: 'frodo',
      filter: 'side:FREE_PEOPLE',
      tokens: 3,
      wounds: 1,
      label: 'a trap',
    });
    // (the player's seat, the top of the deck, nothing on a card, no tokens are the defaults)
    expect(slotWritten({ zone: 'deck', seat: 'freeps', position: 1, tokens: 0, filter: ' ' }, 'freeps')).toBe('deck');
    expect(slotWritten({ zone: 'deck', position: 4 }, 'freeps')).toEqual({ zone: 'deck', position: 4 });
    expect(slotWritten({ zone: 'support', on: 'frodo', position: 2 }, 'freeps')).toBe('support');
    expect(slotWritten({ zone: 'site', seat: 'shadow' }, 'freeps')).toBe('site');
    expect(slotWritten({ zone: 'hand', seat: 'freeps' }, 'shadow')).toEqual({ zone: 'hand', seat: 'freeps' });
  });

  it('changes a part (undefined takes it out) and drops what the new zone has no use for', () => {
    expect(slotChanged({ zone: 'attached', on: 'frodo', label: 'x' }, { label: undefined })).toEqual({
      zone: 'attached',
      on: 'frodo',
    });
    expect(slotChanged({ zone: 'attached', on: 'frodo' }, { zone: 'support' })).toEqual({ zone: 'support' });
    expect(slotChanged({ zone: 'deck', position: 3 }, { zone: 'hand' })).toEqual({ zone: 'hand' });
    expect(slotChanged({ zone: 'hand', seat: 'shadow' }, { zone: 'site' })).toEqual({ zone: 'site' });
    expect(onACard('stacked')).toBe(true);
    expect(onACard('deck')).toBe(false);
  });

  it('sends the whole slots part with one slot changed (ep: top.slots)', () => {
    expect(slotPatch(DRAFT, 'hand1', { zone: 'deck', position: 3 })).toEqual({
      top: {
        slots: {
          condition: { zone: 'support', filter: 'culture:ROHAN cardtype:CONDITION', label: 'a Rohan condition' },
          hand1: { zone: 'deck', position: 3 },
        },
      },
    });
    expect(slotPatch(DRAFT, 'condition', { label: undefined, tokens: 3 }).top.slots).toEqual({
      condition: { zone: 'support', filter: 'culture:ROHAN cardtype:CONDITION', tokens: 3 },
      hand1: 'hand',
    });
  });

  it('sets a bound from a filter (handed in from outside, or typed), blank taking it out', () => {
    expect(slotBoundPatch(DRAFT, 'hand1', ' culture:GONDOR ').top.slots?.['hand1']).toEqual({
      zone: 'hand',
      filter: 'culture:GONDOR',
    });
    expect(slotBoundPatch(DRAFT, 'condition', '').top.slots?.['condition']).toEqual({
      zone: 'support',
      label: 'a Rohan condition',
    });
  });

  it('adds a slot last, with each solution’s card for it', () => {
    expect(addSlotPatch(DRAFT, 'ally', { zone: 'support' }, '4_262')).toEqual({
      top: {
        slots: { ...DRAFT.slots, ally: 'support' },
        solutions: [
          { name: 'published', picks: { condition: '5_87', hand1: '1_2', ally: '4_262' }, answers: ['use(condition)'] },
        ],
      },
    });
    // (with no card for the solutions: the slot alone, which the server refuses with its words)
    expect(addSlotPatch(DRAFT, 'ally', { zone: 'support' }, null)).toEqual({
      top: { slots: { ...DRAFT.slots, ally: 'support' } },
    });
    expect(addSlotPatch({ id: 'x', steps: [] }, 'ally', { zone: 'support' }, '4_262')).toEqual({
      top: { slots: { ally: 'support' } },
    });
  });

  it('takes a slot out, and its card out of every solution', () => {
    expect(removeSlotPatch(DRAFT, 'hand1')).toEqual({
      top: {
        slots: { condition: DRAFT.slots!['condition'] },
        solutions: [{ name: 'published', picks: { condition: '5_87' }, answers: ['use(condition)'] }],
      },
    });
    const one: Draft = { id: 'x', slots: { a: 'hand' }, solutions: [{ name: 's', picks: { a: '1_2' } }] };
    expect(removeSlotPatch(one, 'a')).toEqual({ top: { slots: null, solutions: [{ name: 's' }] } });
    expect(removeSlotPatch({ id: 'x', slots: { a: 'hand' } }, 'a')).toEqual({ top: { slots: null } });
    expect(slotsWith({ id: 'x' }, 'a', null)).toBeNull();
  });

  it('checks a new slot’s id: a card’s name, not taken; a site slot’s, one of the sites', () => {
    expect(slotIdProblem('trap', 'support', ['frodo'], ['site1'])).toBeNull();
    expect(slotIdProblem('2trap', 'support', [], [])).toMatch(/^Letters/);
    expect(slotIdProblem('frodo', 'hand', ['frodo'], [])).toMatch(/frodo is taken/);
    expect(slotIdProblem('site2', 'site', ['site2'], ['site1', 'site2'])).toBeNull();
    expect(slotIdProblem('trap', 'site', [], ['site1'])).toMatch(/one of the scenario’s sites/);
  });

  it('reads numbers typed, and offers the seats', () => {
    expect(slotNumber('3', 1)).toBe(3);
    expect(slotNumber('0', 1)).toBeUndefined();
    expect(slotNumber('x', 0)).toBeUndefined();
    expect(slotSeatOptions('freeps')).toEqual([
      ['', "player's"],
      ['shadow', "opponent's"],
    ]);
    expect(slotSeatOptions('shadow')[1]![0]).toBe('freeps');
  });

  it('counts a bound with the pool, or the filter alone when they say the same key', () => {
    const info = puzzleSlot('condition');
    expect(boundQuery({ zone: 'support', filter: 'culture:ROHAN' }, info, 'set:1-6')).toEqual({
      filter: 'set:1-6 culture:ROHAN',
      exact: true,
    });
    expect(boundQuery({ zone: 'hand' }, null, null)).toEqual({ filter: '', exact: true });
    expect(boundQuery({ zone: 'hand', filter: 'set:4' }, { ...info, overlap: ['set'] }, 'set:1-6')).toEqual({
      filter: 'set:4',
      exact: false,
    });
  });
});
