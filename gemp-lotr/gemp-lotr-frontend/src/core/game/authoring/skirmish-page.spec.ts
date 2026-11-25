import { draftState, position } from '../../../testing/authoring';
import { AuthoringBoard, BoardCard, DraftStep } from './authoring-state';
import { fitsHere, skirmishGroups, skirmishPart, uncoveredIn } from './skirmish-page';

const UNCOVERED = {
  seat: 'human' as const,
  after: 1,
  when: 'x',
  repeating: null,
  until: null,
  becomes: null,
  problem: null,
  block: null,
  sub: 0,
  reuses: null,
};

const card = (alias: string, name: string, owner: 'freeps' | 'shadow', type: string): BoardCard => ({
  alias,
  blueprintId: '1_1',
  name,
  type,
  owner,
  cardId: 1,
  zone: 'characters',
  at: null,
  wounds: 0,
  tokens: 0,
  on: null,
});

const board = (cards: BoardCard[]): AuthoringBoard => ({
  cards,
  twilight: 0,
  burdens: 0,
  threats: 0,
  sites: { freeps: [], shadow: [] },
});

describe('the skirmishes page', () => {
  it("reads where a step belongs off its When: before, first choice, every skirmish, one's fight, after one", () => {
    expect(skirmishPart({ when: 'freeps, assign' })).toEqual({ part: 'before', who: null });
    expect(skirmishPart({ when: 'freeps, cards, text(next skirmish)' })).toEqual({ part: 'first', who: null });
    expect(skirmishPart({ when: 'freeps, action, phase(skirmish)' })).toEqual({ part: 'every', who: null });
    expect(skirmishPart({ when: 'freeps, action, inSkirmish(frodo)' })).toEqual({ part: 'fight', who: 'frodo' });
    expect(skirmishPart({ when: 'freeps, fighting( sam )' })).toEqual({ part: 'fight', who: 'sam' });
    expect(skirmishPart({ when: 'freeps, after(frodo), text(other)' })).toEqual({ part: 'after', who: 'frodo' });
    expect(skirmishPart({ when: { seat: 'freeps', fighting: 'sam' } })).toEqual({ part: 'fight', who: 'sam' });
    expect(skirmishPart({ when: { after: 'sam' } })).toEqual({ part: 'after', who: 'sam' });
    expect(skirmishPart({ when: { text: 'Choose next skirmish' } })).toEqual({ part: 'first', who: null });
    expect(skirmishPart({ when: { phase: 'Skirmish' } })).toEqual({ part: 'every', who: null });
    expect(skirmishPart({ when: [{ type: 'x' }] })).toEqual({ part: 'before', who: null });
    expect(skirmishPart({})).toEqual({ part: 'before', who: null });
  });

  it('knows the choice of the next skirmish said as what the decision is for, and not(...) as any other', () => {
    expect(skirmishPart({ when: 'freeps, cards, nextSkirmish' })).toEqual({ part: 'first', who: null });
    expect(skirmishPart({ when: 'freeps, cards, not(nextSkirmish), phase(skirmish)' })).toEqual({
      part: 'every',
      who: null,
    });
    expect(skirmishPart({ when: 'freeps, cards, nextSkirmish, after(gandalf)' })).toEqual({
      part: 'after',
      who: 'gandalf',
    });
    expect(skirmishPart({ when: { seat: 'freeps', kind: 'nextSkirmish' } })).toEqual({ part: 'first', who: null });
    expect(skirmishPart({ when: { seat: 'freeps', kind: 'not(nextSkirmish)' } })).toEqual({
      part: 'before',
      who: null,
    });
    expect(skirmishPart({ when: { seat: 'freeps', kind: 3 } as never })).toEqual({ part: 'before', who: null });
    // (the server writes the step for the first choice so: it is for the page's first-choice group)
    const state = draftState({
      uncovered: { ...UNCOVERED, when: 'freeps, cards, nextSkirmish, phase(assignment), turn(1)' },
    });
    expect(fitsHere(state, { when: 'freeps, cards, nextSkirmish' })).toBe(true);
  });

  it('says whether the decision no step covers is the one a new step would be for', () => {
    const open = { ...draftState().uncovered, when: 'freeps, action, inSkirmish(frodo)' } as never;
    const state = draftState({ uncovered: open });
    expect(fitsHere(state, { when: 'freeps, action, inSkirmish(frodo)' })).toBe(true);
    expect(fitsHere(state, { when: 'freeps, action, inSkirmish(sam)' })).toBe(false);
    expect(fitsHere(draftState(), { when: 'x' })).toBe(false);
  });

  it("lays the block out by part: each character's skirmish, then the minions', with what comes after each", () => {
    const steps: DraftStep[] = [
      { when: 'freeps, assign', once: true },
      { when: 'shadow, action, inSkirmish(orc)' },
      { when: 'freeps, action, inSkirmish(frodo)' },
      { when: 'freeps, action, phase(skirmish)' },
      { when: 'freeps, after(frodo)', pause: true },
      { when: 'freeps, cards, text(next skirmish)' },
    ];
    const state = draftState({
      board: board([
        card('frodo', 'Frodo, Son of Drogo', 'freeps', 'COMPANION'),
        card('orc', 'Goblin Runner', 'shadow', 'MINION'),
        card('sam', 'Sam, Faithful Companion', 'freeps', 'COMPANION'),
        card('troll', 'Cave Troll', 'shadow', 'MINION'),
        card('ring', 'The One Ring', 'freeps', 'THE_ONE_RING'),
        card('gandalf', 'Gandalf', 'freeps', 'ALLY'),
      ]),
    });
    const { groups, offers } = skirmishGroups(state, { anyOrder: steps }, ['sam', 'nobody']);
    expect(groups.map((group) => [group.title, group.subs, group.add?.at ?? null])).toEqual([
      ['Before the skirmishes, or at any moment', [0], 1],
      ['Choosing the first skirmish', [5], null],
      ['In every skirmish', [3], null],
      ["Frodo's skirmish", [2], 3],
      ["After Frodo's skirmish", [4], null],
      ["Sam's skirmish", [], 3],
      ["After Sam's skirmish", [], 5],
      ['Any skirmish Goblin Runner (orc) is in', [1], 2],
      ["After Goblin Runner's skirmish", [], 5],
    ]);
    expect(groups[7]?.add?.step).toEqual({ once: true, when: 'shadow, action, inSkirmish(orc)' });
    expect(groups[6]?.add?.step).toEqual({
      once: true,
      pause: true,
      when: 'freeps, after(sam)',
      messages: [{ text: '...' }],
    });
    expect(offers).toEqual([
      { alias: 'gandalf', label: 'gandalf (Gandalf)' },
      { alias: 'troll', label: 'troll (Cave Troll, minion)' },
    ]);
  });

  it('starts an empty block with its first two groups, and adds them at the end', () => {
    const { groups, offers } = skirmishGroups(draftState(), { anyOrder: [] }, []);
    expect(groups.map((group) => [group.kind, group.add?.at, group.add?.step])).toEqual([
      ['before', 0, { once: true, when: 'freeps, assign' }],
      ['first', 0, { once: true, when: 'freeps, cards, nextSkirmish' }],
    ]);
    expect(offers).toEqual([]);
    // (a fight with its own steps after the ones for every skirmish still has its next step before them)
    const late = skirmishGroups(
      draftState({ draft: { ...draftState().draft, human: 'shadow' } }),
      { anyOrder: [{ when: 'phase(skirmish)' }, { when: 'shadow, inSkirmish(x)' }] },
      [],
    );
    expect(late.groups.find((group) => group.kind === 'fight')?.add).toMatchObject({
      at: 0,
      step: { when: 'shadow, action, inSkirmish(x)' },
    });
  });

  it('says whether the decision no step covers is for a block to have a step for', () => {
    const open = { ...UNCOVERED, block: 2 };
    expect(uncoveredIn(draftState({ uncovered: open }), 1)).toBe(true);
    expect(uncoveredIn(draftState({ uncovered: open }), 0)).toBe(false);
    expect(uncoveredIn(draftState({ uncovered: { ...open, problem: 'No.' } }), 1)).toBe(false);
    expect(uncoveredIn(draftState({ uncovered: open, position: position({ over: true }) }), 1)).toBe(false);
    expect(uncoveredIn(draftState(), 1)).toBe(false);
  });
});
