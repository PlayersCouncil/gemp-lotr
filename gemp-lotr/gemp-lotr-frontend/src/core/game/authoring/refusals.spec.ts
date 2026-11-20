import { decision } from '../../../testing/authoring';
import { refusalBadge, refusalTerm, refuses, withRefusal } from './refusals';

describe("a step's refusals", () => {
  it('says in a word how each stands at the decision in hand, and why in its title', () => {
    expect(refusalBadge({ refusal: '2: choose(boromir)', at: 2, now: 'refused' })).toEqual({
      word: 'refused',
      title: '2: choose(boromir) (decision 2 of the step): Offered here, and Allow refuses it, as it should.',
    });
    expect(refusalBadge({ refusal: 'pass', at: 0, now: 'allowed' }).word).toBe('allowed!');
    expect(refusalBadge({ refusal: 'pass', at: 0, now: 'notOffered' }).word).toBe('not offered');
    expect(refusalBadge({ refusal: 'pass', at: 0, now: 'elsewhere' }).title).toBe(
      "pass: It is about another of the step's decisions.",
    );
  });

  it('reads a click the step did not take as the answer it would have been', () => {
    const actions = decision({
      options: [
        { term: 'play(sword)', label: 'Sword', alias: 'sword', id: '12' },
        { term: null, label: 'Someone', alias: null, id: '13' },
      ],
    });
    expect(refusalTerm(actions, { cardId: '12', button: null, option: null })).toBe('play(sword)');
    expect(refusalTerm(actions, { cardId: 'extra12', button: null, option: null })).toBe('play(sword)');
    expect(refusalTerm(actions, { cardId: '13', button: null, option: null })).toBeNull();
    expect(refusalTerm(actions, { cardId: '99', button: null, option: null })).toBeNull();
    expect(refusalTerm(actions, { cardId: null, button: 'Pass', option: null })).toBe('pass');
    expect(refusalTerm(actions, { cardId: null, button: 'Done', option: null })).toBeNull();
    expect(refusalTerm(decision({ pass: false }), { cardId: null, button: 'Pass', option: null })).toBeNull();
    expect(refusalTerm(actions, { cardId: null, button: null, option: null })).toBeNull();
    const cards = decision({ type: 'cards', options: [{ term: null, label: 'Boromir', alias: 'boromir', id: '8' }] });
    expect(refusalTerm(cards, { cardId: '8', button: null, option: null })).toBe('choose(boromir)');
    const choice = decision({
      type: 'choice',
      options: [{ term: 'option(Yes)', label: 'Yes', alias: null, id: null }],
    });
    expect(refusalTerm(choice, { cardId: null, button: null, option: 0 })).toBe('option(Yes)');
    expect(refusalTerm(choice, { cardId: null, button: null, option: 3 })).toBeNull();
    expect(refusalTerm(null, { cardId: '8', button: null, option: null })).toBeNull();
    expect(refusalTerm(cards, null)).toBeNull();
  });

  it('adds a refusal on a line of its own, once', () => {
    expect(withRefusal(undefined, 'pass')).toBe('pass');
    expect(withRefusal('2: choose(boromir)', 'pass')).toBe('2: choose(boromir)\npass');
    expect(withRefusal('2: choose(boromir), pass\n', 'pass')).toBe('2: choose(boromir), pass');
    expect(refuses('choose(a)\n3: choose(b)', 'choose(b)')).toBe(true);
    expect(refuses('choose(a)', 'choose(b)')).toBe(false);
  });
});
