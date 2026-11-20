import { draftState, position } from '../../../testing/authoring';
import { DraftState, DraftStep } from './authoring-state';
import { diff, working, written } from './step-model';
import {
  afterSelected,
  alikeAfter,
  childInsertOp,
  childMoveOp,
  childPause,
  childStep,
  convertOp,
  earlierOp,
  intoBlockOp,
  joinable,
  joinOps,
  laterOp,
  moveMessageOps,
  nearSkirmishes,
  outOfBlockOp,
  skirmishy,
  splitToPauseOps,
  beforeSelected,
  blankStep,
  blockGoto,
  blockStep,
  coversUncovered,
  insertOp,
  mayAddOpening,
  mayRewrite,
  openingStep,
  pauseStep,
  rewriteOps,
  stepsGoto,
} from './step-ops';

describe('step ops', () => {
  const state = draftState();

  it('puts a step in before the step at a place, or at the end; waiting for its answer if asked', () => {
    expect(insertOp(state, 1, { pause: true })).toEqual({ op: 'insert', before: 's2', step: { pause: true } });
    expect(insertOp(state, 3, { pause: true }, true)).toEqual({
      op: 'insert',
      before: null,
      step: { pause: true },
      awaiting: true,
    });
  });

  it('has the steps the buttons put in', () => {
    expect(openingStep()).toEqual({ when: 'opening', messages: [{ text: '...' }] });
    expect(pauseStep()).toEqual({ pause: true, messages: [{ text: '...' }] });
    expect(blankStep(state)).toEqual({ when: 'freeps, action' });
    expect(blankStep(draftState({ draft: { id: 'x' } }))).toEqual({ when: 'freeps, action' });
    expect(blankStep(draftState({ draft: { id: 'x', human: 'shadow' } }))).toEqual({ when: 'shadow, action' });
    expect(blockStep(3, false)).toEqual({ until: 'phase(regroup)', anyOrder: [], when: 'turn(3)' });
    expect(blockStep(0, true)).toEqual({ skirmishes: true, until: 'phase(regroup)', anyOrder: [] });
  });

  it('knows where a step put in before or after the selected one goes', () => {
    expect(beforeSelected(state, 2)).toBe(1);
    expect(beforeSelected(state, null)).toBe(3);
    expect(afterSelected(state, 2)).toBe(2);
    expect(afterSelected(state, null)).toBe(3);
    expect(mayAddOpening(state)).toBe(true);
    expect(mayAddOpening(draftState({ draft: { id: 'x', steps: [{ when: 'opening' }] } }))).toBe(false);
  });

  it("takes the decision no step covers for itself where a step put in there would be that one's", () => {
    const open = {
      seat: 'human' as const,
      after: 1,
      when: 'freeps, action',
      repeating: null,
      until: null,
      becomes: null,
      problem: null,
      block: null,
      sub: 0,
      reuses: null,
    };
    expect(coversUncovered(draftState({ uncovered: open }), 1)).toBe(true);
    expect(coversUncovered(draftState({ uncovered: open }), 2)).toBe(false);
    expect(coversUncovered(draftState({ uncovered: { ...open, problem: 'no' } }), 1)).toBe(false);
    expect(coversUncovered(draftState({ uncovered: { ...open, reuses: 2 } }), 1)).toBe(false);
    expect(coversUncovered(draftState({ uncovered: { ...open, block: 2 } }), 1)).toBe(false);
    expect(coversUncovered(draftState({ uncovered: open, position: position({ over: true }) }), 1)).toBe(false);
    expect(coversUncovered(state, 1)).toBe(false);
  });

  it('takes every step from where the game is out, the last first, to rewrite them', () => {
    expect(rewriteOps(state, 2)).toEqual([
      { op: 'delete', uid: 's3' },
      { op: 'delete', uid: 's2' },
    ]);
    expect(rewriteOps(draftState({ uids: ['s1'] }), 1)).toEqual([{ op: 'delete', uid: 's1' }]);
    expect(mayRewrite(state)).toBe(true);
    expect(mayRewrite(draftState({ position: position({ step: 4 }) }))).toBe(false);
    expect(mayRewrite(draftState({ position: position({ over: true }) }))).toBe(false);
  });

  it('plays to a step changed at or before where the game is again; one after it, or in its block, carries on', () => {
    expect(stepsGoto(2, 3)).toBe(2);
    expect(stepsGoto(3, 3)).toBe(3);
    expect(stepsGoto(0, 3)).toBe(1);
    expect(stepsGoto(4, 3)).toBeNull();
    expect(blockGoto(1, 3)).toBe(2);
    expect(blockGoto(2, 3)).toBeNull();
  });
});

describe("the Step tab's ops", () => {
  const withSteps = (steps: DraftStep[], extra: Partial<DraftState> = {}) =>
    draftState({ ...extra, draft: { ...draftState().draft, steps } });

  it('finds the steps after one that wait for the same, and could be joined into it', () => {
    const alike = withSteps([
      { when: 'freeps, action', allow: 'heal(a)' },
      { when: 'freeps, action', allow: 'heal(b)?' },
      { when: 'freeps, action', answer: 'heal(c)' },
      { when: 'freeps, action', messages: [{ text: 'x' }] },
    ]);
    expect(alikeAfter(alike, 0)).toBe(2);
    expect(alikeAfter(alike, 3)).toBe(0);
    expect(alikeAfter(withSteps([{ pause: true }, { pause: true }]), 0)).toBe(0);
    expect(alikeAfter(withSteps([{ when: 'opening' }, { when: 'opening' }]), 0)).toBe(0);
    expect(alikeAfter(alike, 9)).toBe(0);
    expect(joinable(undefined)).toBe(false);
    for (const odd of [
      { until: 'x' },
      { do: 'draw(1)' },
      { checkpoint: true },
      { end: 'fail' as const },
      { if: 'a' },
    ]) {
      expect(joinable({ when: 'x', ...odd })).toBe(false);
    }
    expect(joinable({ when: 'x', until: '  ' })).toBe(true);
  });

  it('joins them: what each allowed, the answers in order when each had one, until the step after them', () => {
    const steps: DraftStep[] = [
      { when: 'freeps, action, phase(fellowship)', allow: 'heal(a)' },
      { when: 'freeps, action, phase(fellowship)', allow: 'heal(b)?, heal(a)' },
      { when: 'shadow, action' },
    ];
    expect(joinOps(withSteps(steps), 0, 1)).toEqual([
      { op: 'merge', uid: 's1', set: { until: 'shadow, action', allow: 'heal(a), heal(b)' }, unset: ['answer'] },
      { op: 'delete', uid: 's2' },
    ]);
    const answered: DraftStep[] = [
      { when: 'freeps, phase(regroup)', allow: { answers: 'heal(a)', otherwise: 'No' } },
      { when: 'freeps, phase(regroup)', answer: 'heal(b)' },
    ];
    expect(joinOps(withSteps(answered), 0, 1)[0]).toEqual({
      op: 'merge',
      uid: 's1',
      set: { until: '', allow: { answers: 'heal(a)', otherwise: 'No' }, answer: 'heal(a), heal(b)' },
      unset: [],
    });
    expect(joinOps(withSteps([{ when: 'a, phase(shadow)' }, { when: 'a, phase(shadow)' }]), 0, 1)[0]).toEqual({
      op: 'merge',
      uid: 's1',
      set: { until: 'phase(maneuver)' },
      unset: ['answer'],
    });
    expect(joinOps(withSteps([]), 0, 1)).toEqual([]);
  });

  it('tells a step of the assignment or the skirmishes, and one beside one', () => {
    expect(skirmishy({ when: 'freeps, assign' })).toBe(true);
    expect(skirmishy({ until: 'phase(skirmish)' })).toBe(true);
    expect(skirmishy({ when: 'freeps, action, inSkirmish(frodo)' })).toBe(true);
    expect(skirmishy({ when: { seat: 'freeps', until: 'inSkirmish(sam)' } as never })).toBe(true);
    expect(skirmishy({ when: { type: 'assign' } as never })).toBe(true);
    expect(skirmishy({ when: { seat: 'freeps' } })).toBe(false);
    expect(skirmishy({ when: 'freeps, action, phase(shadow)' })).toBe(false);
    expect(skirmishy(undefined)).toBe(false);
    expect(skirmishy({ when: 'freeps, cards, nextSkirmish' })).toBe(true);
    expect(skirmishy({ when: 'freeps, cards, not(nextSkirmish)' })).toBe(false);
    const state = withSteps([{ when: 'a' }, { when: 'b' }, { when: 'freeps, assign' }]);
    expect(nearSkirmishes(state, 0)).toBe(false);
    expect(nearSkirmishes(state, 1)).toBe(true);
  });

  it('turns a step into a block with it as its first step', () => {
    const state = withSteps(
      [
        { when: 'freeps, action, phase(fellowship), turn(2)', checkpoint: true, if: 'brave', allow: 'pass' },
        { when: 'shadow, action, phase(shadow), turn(2)' },
      ],
      { position: position({ turn: 2 }) },
    );
    expect(convertOp(state, 0, false)).toEqual({
      op: 'set',
      uid: 's1',
      step: {
        when: 'phase(fellowship), turn(2)',
        until: 'shadow, action, phase(shadow), turn(2)',
        checkpoint: true,
        if: 'brave',
        anyOrder: [{ when: 'freeps, action', allow: 'pass', once: true }],
      },
    });
    expect(convertOp(state, 1, true)).toEqual({
      op: 'set',
      uid: 's2',
      step: {
        when: 'turn(2)',
        until: 'phase(regroup)',
        skirmishes: true,
        anyOrder: [{ when: 'shadow, action, phase(shadow)', once: true }],
      },
    });
    const repeating = withSteps([{ when: 'turn(1)', until: 'phase(regroup)' }]);
    expect(convertOp(repeating, 0, false)).toEqual({
      op: 'set',
      uid: 's1',
      step: { when: 'turn(1)', until: 'phase(regroup)', anyOrder: [{}] },
    });
    const pause = withSteps([{ pause: true, when: { seat: 'freeps' } }], { position: position({ turn: 0 }) });
    expect(convertOp(pause, 0, true)).toEqual({
      op: 'set',
      uid: 's1',
      step: { until: 'phase(regroup)', skirmishes: true, anyOrder: [{ pause: true, when: { seat: 'freeps' } }] },
    });
    expect(convertOp(withSteps([{ when: 'freeps, phase(regroup)' }]), 0, false)?.op).toBe('set');
    expect(convertOp(draftState(), 2, false)).toBeNull();
  });

  it("moves a step one earlier or later, into a block beside it, and a block's step about", () => {
    const state = draftState();
    expect(earlierOp(state, 1)).toEqual({ op: 'move', uid: 's2', before: 's1' });
    expect(laterOp(state, 0)).toEqual({ op: 'move', uid: 's1', before: 's3' });
    expect(laterOp(state, 1)).toEqual({ op: 'move', uid: 's2', before: null });
    expect(intoBlockOp(state, 1, 2)).toEqual({
      op: { op: 'move', uid: 's2', before: 's3k1' },
      blockAt: 1,
      subAt: 0,
      firstChanged: 2,
    });
    const after = withSteps([{ until: 'x', anyOrder: [] }, { when: 'a' }]);
    expect(intoBlockOp(after, 1, 0)).toEqual({
      op: { op: 'move', uid: 's2', into: 's1' },
      blockAt: 0,
      subAt: 0,
      firstChanged: 1,
    });
    expect(intoBlockOp(withSteps([{ when: 'a' }, { until: 'x', anyOrder: [] }]), 0, 1).op).toEqual({
      op: 'move',
      uid: 's1',
      into: 's2',
    });
    expect(childMoveOp(state, 2, 's3k2', 0)).toEqual({ op: 'move', uid: 's3k2', before: 's3k1' });
    expect(childMoveOp(state, 2, 's3k1', 2)).toEqual({ op: 'move', uid: 's3k1', into: 's3' });
    expect(outOfBlockOp(state, 2, 's3k1')).toEqual({ op: 'move', uid: 's3k1', before: null });
    expect(outOfBlockOp(state, 1, 'x')).toEqual({ op: 'move', uid: 'x', before: 's3' });
  });

  it('puts a step into a block: before one of its steps, or last; a blank one, or a pause', () => {
    const state = draftState();
    expect(childStep(state)).toEqual({ once: true, when: 'freeps, action' });
    expect(childPause(state)).toEqual({ pause: true, when: 'freeps, action', messages: [{ text: '...' }] });
    expect(childInsertOp(state, 2, 1, { pause: true })).toEqual({
      op: 'insert',
      step: { pause: true },
      before: 's3k2',
    });
    expect(childInsertOp(state, 2, 2, { pause: true }, true)).toEqual({
      op: 'insert',
      step: { pause: true },
      into: 's3',
      awaiting: true,
    });
  });

  it('moves a message to the step before (last there) or after (first there), with the rest unsent', () => {
    const state = draftState();
    const work = { ...working(state.draft.steps![0]!), id: 'named' };
    const now = written(work);
    const dirty = diff(now, written(working(state.draft.steps![0]!)));
    expect(moveMessageOps(state, 's1', now, dirty, 0, 0, 1)).toEqual({
      ops: [
        { op: 'merge', uid: 's1', set: { id: 'named', messages: [{ text: 'Two' }] }, unset: [] },
        { op: 'merge', uid: 's2', set: { messages: [{ text: 'Hello' }] }, unset: [] },
      ],
      at: 0,
    });
    const second = draftState({
      draft: {
        ...state.draft,
        steps: [
          { when: 'a', messages: ['x'] },
          { when: 'b', messages: ['y'] },
        ],
      },
    });
    const only = written(working(second.draft.steps![1]!));
    expect(moveMessageOps(second, 's2', only, diff(only, only), 0, 1, 0)).toEqual({
      ops: [
        { op: 'merge', uid: 's2', set: {}, unset: ['messages'] },
        { op: 'merge', uid: 's1', set: { messages: [{ text: 'x' }, { text: 'y' }] }, unset: [] },
      ],
      at: 1,
    });
    expect(moveMessageOps(state, 's1', now, dirty, 5, 0, 1)).toBeNull();
    expect(moveMessageOps(state, 's1', now, dirty, 0, 0, 9)).toBeNull();
  });

  it("makes a step's first messages a pause before it", () => {
    const now = {
      when: 'freeps, action, offers(sword), text(Play)',
      if: 'brave',
      unless: 'timid',
      messages: [{ text: 'a', on: 'ready', advance: 'decision' }, { text: 'b', advance: 'next' }, { text: 'c' }],
    };
    const dirty = { set: { id: 'x', messages: [] }, unset: ['note'], any: true };
    expect(splitToPauseOps('s1', now, dirty, 1)).toEqual([
      { op: 'merge', uid: 's1', set: { id: 'x', messages: [{ text: 'c' }] }, unset: ['note'] },
      {
        op: 'insert',
        before: 's1',
        step: {
          pause: true,
          messages: [{ text: 'a' }, { text: 'b', advance: 'next' }],
          if: 'brave',
          unless: 'timid',
          when: 'freeps, action',
        },
      },
    ]);
    const all = splitToPauseOps(
      's1',
      { when: '{ seat: freeps }', messages: [{ text: 'a' }] },
      { set: {}, unset: [], any: false },
      0,
    );
    expect(all[0]).toEqual({ op: 'merge', uid: 's1', set: {}, unset: ['messages'] });
    expect(all[1]).toEqual({ op: 'insert', before: 's1', step: { pause: true, messages: [{ text: 'a' }] } });
  });
});
