import { draftState, STEPS } from '../../../testing/authoring';
import {
  isAwaiting,
  isBlock,
  isHumanSeat,
  isOpening,
  isThisGame,
  kidsOf,
  leftOutOf,
  locate,
  messagesOf,
  numberOf,
  own,
  settingsOf,
  stepsOf,
} from './steps';

describe('the steps of a draft', () => {
  const state = draftState();

  it('finds a step by its name, in the script or in a block', () => {
    expect(locate(state, 's2')).toEqual({ index: 1, sub: -1, step: STEPS[1] });
    expect(locate(state, 's3k2')).toEqual({ index: 2, sub: 1, step: STEPS[2]!.anyOrder![1] });
    expect(locate(state, 'nobody')).toBeNull();
    expect(locate(state, null)).toBeNull();
    // (a name the state has for a block's step the draft doesn't have)
    expect(locate(draftState({ kids: [['ghost'], [], []] }), 'ghost')).toBeNull();
    expect(kidsOf(state, 2)).toEqual(['s3k1', 's3k2']);
    expect(kidsOf(state, 9)).toEqual([]);
  });

  it('numbers steps, and a block says what it is without its steps', () => {
    expect(numberOf(33)).toBe('34');
    expect(numberOf(33, 1)).toBe('34.2');
    expect(own(STEPS[2]!)).toEqual({ when: 'turn(1)', until: 'phase(regroup)' });
    expect(own(STEPS[0]!)).toBe(STEPS[0]);
    expect(isBlock(STEPS[2])).toBe(true);
    expect(isBlock(STEPS[0])).toBe(false);
    expect(isBlock(undefined)).toBe(false);
  });

  it('knows the opening, the messages, the seats, the steps waiting for an answer and what was left out', () => {
    expect(isOpening({ when: ' Opening ' })).toBe(true);
    expect(isOpening({ when: 'freeps' })).toBe(false);
    expect(isOpening(undefined)).toBe(false);
    expect(messagesOf(STEPS[0]!)).toHaveLength(2);
    expect(messagesOf({ messages: 'One' })).toEqual(['One']);
    expect(messagesOf({ messages: { text: 'One' } })).toEqual([{ text: 'One' }]);
    expect(messagesOf({})).toEqual([]);
    expect(isHumanSeat(state, 'human')).toBe(true);
    expect(isHumanSeat(state, 'freeps')).toBe(true);
    expect(isHumanSeat(state, 'shadow')).toBe(false);
    expect(isHumanSeat(draftState({ draft: { id: 'x', human: 'shadow' } }), 'shadow')).toBe(true);
    expect(isHumanSeat(draftState({ draft: { id: 'x' } }), 'freeps')).toBe(true);
    const awaiting = draftState({ awaiting: ['s2'] });
    expect(isAwaiting(awaiting, 's2')).toBe(true);
    expect(isAwaiting(awaiting, null)).toBe(false);
    const part = { where: 'step 2', message: 'm', was: 'w', step: 2, uid: 's2' };
    expect(leftOutOf(draftState({ leftOut: [part] }), 's2')).toEqual([part]);
    expect(leftOutOf(draftState({ leftOut: [part] }), null)).toEqual([]);
    expect(stepsOf(draftState({ draft: { id: 'x' } }))).toEqual([]);
  });

  it("says whether the state is this page's game", () => {
    expect(isThisGame(state, '7')).toBe(true);
    expect(isThisGame(draftState({ gameId: 7 }), '7')).toBe(true);
    expect(isThisGame(state, '8')).toBe(false);
    expect(isThisGame(state, null)).toBe(false);
  });

  it("gives the player's settings the draft fixes, by its kind where it says nothing", () => {
    expect(settingsOf({ id: 'x' })).toEqual({ autoAccept: true, autoDismissDeckReveal: true, animationSpeed: null });
    expect(settingsOf({ id: 'x', kind: 'puzzle', options: { autoAccept: false, animationSpeed: 2 } })).toEqual({
      autoAccept: false,
      autoDismissDeckReveal: false,
      animationSpeed: 2,
    });
    expect(
      settingsOf({ id: 'x', kind: 'puzzle', options: { autoDismissDeckReveal: true } }).autoDismissDeckReveal,
    ).toBe(true);
  });
});
