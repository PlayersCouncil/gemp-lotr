import { draftState } from '../../../testing/authoring';
import { START_DEFAULT } from './board-start';
import { openingOf, openingStepUid, openingSteps, setupForBoard, setupForGame, stepWords } from './opening';

describe('how the game opens (setup.opening)', () => {
  it('reads it from the state’s start, else from the draft', () => {
    expect(openingOf(draftState())).toBe('board');
    expect(openingOf(draftState({ draft: { id: 'x', setup: { opening: 'game' } } }))).toBe('game');
    const start = { ...START_DEFAULT, opening: 'game' as const };
    expect(openingOf(draftState({ start }))).toBe('game');
  });

  it('keeps only the decks for the game’s own opening, saying what else goes', () => {
    expect(setupForGame(undefined)).toEqual({ setup: { opening: 'game' }, lost: [] });
    expect(
      setupForGame({
        freeps: { hand: 'gandalf', deck: 'sam, merry' },
        shadow: { minions: 'orc' },
        startAt: { site: 3 },
        wounds: { frodo: 1 },
        burdens: 2,
        controlled: {},
        twilight: 0,
      }),
    ).toEqual({
      setup: { opening: 'game', freeps: { deck: 'sam, merry' } },
      lost: [
        'Free Peoples hand: gandalf',
        'Shadow minions: orc',
        'where the story starts',
        'the twilight pool',
        'wounds',
        'burdens',
      ],
    });
  });

  it('takes `opening` out for a board laid out, keeping the rest (nothing left: no setup)', () => {
    expect(setupForBoard({ opening: 'game', freeps: { deck: 'sam' } })).toEqual({ freeps: { deck: 'sam' } });
    expect(setupForBoard({ opening: 'game' })).toBeNull();
    expect(setupForBoard(undefined)).toBeNull();
  });

  it('finds the steps that answer the opening, and the opening step a game of its own opening cannot have', () => {
    const state = draftState({
      draft: {
        id: 'x',
        steps: [
          { when: 'opening', messages: ['Hello'] },
          { when: 'human, bid', allow: 'number(1)' },
          { when: 'opponent, not(mulligan)', answer: 'pass' },
          { when: 'shadow, choice, mulligan', answer: 'option(No)' },
          { when: { seat: 'human', require: [] }, allow: 'pass' },
        ],
      },
    });
    expect(openingSteps(state)).toEqual([2, 4]);
    expect(openingStepUid(state)).toBe('s1');
    expect(openingStepUid(draftState())).toBeNull();
    expect(openingStepUid(draftState({ draft: { id: 'x', steps: [] } }))).toBeNull();
  });

  it('says step numbers as a person would', () => {
    expect(stepWords([3])).toBe('step 3');
    expect(stepWords([1, 2, 4])).toBe('steps 1, 2 and 4');
  });
});
