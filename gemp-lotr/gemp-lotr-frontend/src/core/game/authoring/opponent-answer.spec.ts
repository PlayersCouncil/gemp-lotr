import { decision } from '../../../testing/authoring';
import { DecisionOffer } from './authoring-state';
import {
  answeredBy,
  answersByButton,
  answersByCards,
  assignTerm,
  cardsTerm,
  chooseLine,
  decisionKey,
  fallbackOf,
  humanLine,
  numberTerm,
  offerKey,
  offerLabel,
  offersPass,
} from './opponent-answer';

const offer = (alias: string | null, id: string | null, label = alias ?? `card ${id}`): DecisionOffer => ({
  term: null,
  label,
  alias,
  id,
});

describe('opponent-answer', () => {
  it('says how each kind of decision is answered', () => {
    for (const type of ['action', 'actionChoice', 'choice'] as const) {
      expect(answersByButton(decision({ type }))).toBe(true);
      expect(answersByCards(decision({ type }))).toBe(false);
    }
    for (const type of ['cards', 'arbitrary'] as const) {
      expect(answersByButton(decision({ type }))).toBe(false);
      expect(answersByCards(decision({ type }))).toBe(true);
    }
    expect(answersByButton(decision({ type: 'number' }))).toBe(false);
    expect(offersPass(decision({ type: 'action', pass: true }))).toBe(true);
    expect(offersPass(decision({ type: 'assign', pass: true }))).toBe(false);
    expect(offersPass(decision({ type: 'cards', pass: undefined }))).toBe(false);
  });

  it('tells decisions apart by everything they say and offer', () => {
    const one = decision({ options: [{ term: 'play(orc)', label: 'Play', alias: 'orc', id: '4' }] });
    expect(decisionKey(one)).toBe(decisionKey(decision({ options: [...one.options] })));
    expect(decisionKey(one)).not.toBe(decisionKey(decision({ options: [] })));
    expect(decisionKey(one)).not.toBe(decisionKey({ ...one, text: 'Other' }));
    expect(decisionKey(decision({ type: 'assign', characters: [offer('frodo', '1')] }))).not.toBe(
      decisionKey(decision({ type: 'assign', characters: [offer('sam', '2')] })),
    );
    expect(decisionKey(decision({ min: 1, max: 2 }))).toContain('|1|2|');
    expect(decisionKey(decision({ options: [{ term: null, label: 'x', alias: null, id: null }] }))).toContain('//');
    expect(decisionKey(null)).toBe('');
  });

  it('names offers by label and alias, and keys them by alias, else id, else place', () => {
    expect(offerLabel(offer('orc', '4', 'Goblin Runner'))).toBe('Goblin Runner (orc)');
    expect(offerLabel(offer(null, '4', 'Goblin Runner'))).toBe('Goblin Runner');
    expect(offerKey(offer('orc', '4'), 0)).toBe('orc');
    expect(offerKey(offer(null, '4'), 0)).toBe('#4');
    expect(offerKey(offer(null, null), 3)).toBe('@3');
  });

  it('says how many cards to choose', () => {
    expect(chooseLine(decision({ type: 'cards', min: 1, max: 1 }))).toBe('Choose 1:');
    expect(chooseLine(decision({ type: 'cards', min: 0, max: 2 }))).toBe('Choose 0 to 2:');
    expect(chooseLine(decision({ type: 'cards', min: 2 }))).toBe('Choose 2:');
    expect(chooseLine(decision({ type: 'cards' }))).toBe('Choose 0:');
  });

  it('chooses cards by their names, by their ids when one has none, and passes with none', () => {
    expect(cardsTerm([])).toBe('pass');
    expect(cardsTerm([offer('orc', '4'), offer('troll', '5')])).toBe('choose(orc, troll)');
    expect(cardsTerm([offer('orc', '4'), offer(null, '9')])).toBe('raw(4,9)');
    expect(cardsTerm([offer(null, null)])).toBe('raw()');
  });

  it('assigns minions to characters, grouped in the order of their first minion', () => {
    const minions = [offer('orc', '4'), offer('troll', '5'), offer('goblin', '6')];
    const characters = [offer('frodo', '1'), offer('sam', '2')];
    expect(assignTerm(minions, characters, {})).toBe('pass');
    expect(assignTerm(minions, characters, { orc: '', troll: 'nobody' })).toBe('pass');
    expect(assignTerm(minions, characters, { orc: 'sam', troll: 'frodo', goblin: 'sam' })).toBe(
      'assign(sam: orc + goblin, frodo: troll)',
    );
    expect(assignTerm(minions, characters, { troll: 'frodo' })).toBe('assign(frodo: troll)');
  });

  it("assigns by the game's ids when a card has no name in the scenario", () => {
    const minions = [offer(null, '4'), offer('troll', '5')];
    const characters = [offer('frodo', '1'), offer(null, '2')];
    expect(assignTerm(minions, characters, { '#4': 'frodo', troll: 'frodo' })).toBe('raw(1 4 5)');
    expect(assignTerm(minions, characters, { troll: '#2' })).toBe('raw(2 5)');
    expect(assignTerm([offer('orc', null)], [offer(null, null)], { orc: '@0' })).toBe('raw( )');
  });

  it('gives a number only for a whole number', () => {
    expect(numberTerm(' 3 ')).toBe('number(3)');
    expect(numberTerm('-1')).toBe('number(-1)');
    expect(numberTerm('')).toBeNull();
    expect(numberTerm('2.5')).toBeNull();
    expect(numberTerm('x')).toBeNull();
  });

  it('says what answers the opponent, and what applies to the player', () => {
    expect(answeredBy(decision({ step: 3, sub: 0 }))).toEqual({
      text: 'Step 3 answers it.',
      title: 'Once what it says has been read',
    });
    expect(answeredBy(decision({ step: 3, sub: 2 })).text).toBe('Step 3.2 answers it.');
    expect(answeredBy(decision({ step: null })).text).toBe('It answers by itself in a moment.');
    expect(humanLine(decision({ step: 4, sub: 1 }), true)).toEqual({ text: 'Step 4.1 applies to it.', title: '' });
    expect(humanLine(decision({ step: null }), false)).toEqual({ text: 'No step applies to it.', title: '' });
    expect(humanLine(decision({ step: null }), true).text).toBe('No step applies to it. Your answer becomes one.');
  });

  it("names the opponent's fallback", () => {
    expect(fallbackOf({ id: 'x' })).toBe('fail');
    expect(fallbackOf({ id: 'x', opponent: { fallback: 'pass' } })).toBe('pass');
  });
});
