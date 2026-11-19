import { draftState, position } from '../../../testing/authoring';
import {
  afterSelected,
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
