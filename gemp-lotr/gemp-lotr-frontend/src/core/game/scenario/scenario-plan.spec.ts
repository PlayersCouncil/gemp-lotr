import { CardAction } from '../decision/decision-view';
import { Decision, DecisionType } from '../protocol/game-events';
import { LocalChoices, NO_CHOICES } from '../state/game-model';
import { ScenarioAllow, ScenarioMessage, ScenarioStep } from './scenario-events';
import { characterComplete, openingMessages, planAllows, PlanContext, scenarioPlan } from './scenario-plan';

const decision = (type: DecisionType, params: Record<string, string[]> = {}): Decision => ({
  id: '1',
  participantId: 'asdf',
  type,
  text: 'Do it',
  params,
});
const allow = (answers: string[], extra: Partial<ScenarioAllow> = {}): ScenarioAllow => ({
  answers,
  pass: false,
  otherwise: null,
  hints: {},
  ...extra,
});
const action = (actionId: string): CardAction => ({ actionId, text: 'Use', noEffect: null });

function context(
  type: DecisionType,
  answers: ScenarioAllow,
  local: Partial<LocalChoices> = {},
  extra: { params?: Record<string, string[]>; actions?: Record<string, CardAction[]> } = {},
): PlanContext {
  return {
    decision: decision(type, extra.params),
    allow: answers,
    local: { ...NO_CHOICES, ...local },
    actions: extra.actions ?? {},
  };
}

const message = (on: ScenarioMessage['on'], html = ''): ScenarioMessage => ({
  index: 0,
  header: null,
  html,
  anchors: [],
  gap: null,
  highlight: [],
  highlightStyle: 'gold',
  avoid: [],
  side: 'auto',
  advance: 'next',
  advanceCard: null,
  choices: [],
  on,
  zoom: null,
});

describe('scenario plan', () => {
  it('opens a step with its messages for the start, not those of a moment inside the answering', () => {
    const first = message(null, 'first');
    const start = message({ moment: 'start', card: null }, 'start');
    const picked = message({ moment: 'picked', card: 'frodo' }, 'picked');
    const step = { messages: [first, picked, start] } as unknown as ScenarioStep;
    expect(openingMessages(step)).toEqual([first, start]);
  });

  describe('a choice of card actions', () => {
    const actions = { '5': [action('10'), action('11')], '6': [action('12')] };

    it('takes only the cards with an allowed action, and once one is taken, Done but no Pass', () => {
      const open = context('CARD_ACTION_CHOICE', allow(['11'], { pass: true }), {}, { actions });
      expect(scenarioPlan(open)).toEqual({ liveCards: ['5'], ready: false, pass: true, options: [], numbers: [] });
      expect(planAllows(open, { kind: 'card', cardId: '5' })).toBe(true);
      expect(planAllows(open, { kind: 'card', cardId: '6' })).toBe(false);
      expect(planAllows(open, { kind: 'action', actionId: '11', cardId: '5' })).toBe(true);
      expect(planAllows(open, { kind: 'action', actionId: '10', cardId: '5' })).toBe(false);
      expect(planAllows(open, { kind: 'button', button: 'Pass' })).toBe(true);
      expect(planAllows(open, { kind: 'button', button: 'Done' })).toBe(false);

      const taken = context(
        'CARD_ACTION_CHOICE',
        allow(['11'], { pass: true }),
        { selectedCardIds: ['11'], markedCardIds: ['5'] },
        { actions },
      );
      expect(scenarioPlan(taken)).toEqual({ liveCards: [], ready: true, pass: false, options: [], numbers: [] });
      expect(planAllows(taken, { kind: 'button', button: 'Done' })).toBe(true);
      expect(planAllows(taken, { kind: 'button', button: 'Pass' })).toBe(false);
      // Taking it back is always fine
      expect(planAllows(taken, { kind: 'button', button: 'Reset choice' })).toBe(true);
      expect(planAllows(taken, { kind: 'button', button: 'OK' })).toBe(true);
    });

    it('takes only passing when that is all it allows', () => {
      const passing = context('CARD_ACTION_CHOICE', allow([], { pass: true }), {}, { actions });
      expect(scenarioPlan(passing)).toMatchObject({ liveCards: [], pass: true });
      expect(scenarioPlan(context('CARD_ACTION_CHOICE', allow(['12']), {}, { actions })).pass).toBe(false);
    });
  });

  it('takes a choice of actions shown as cards by the stand-in card of the allowed action', () => {
    const params = { actionId: ['7', '8', '9'] };
    const open = context('ACTION_CHOICE', allow(['8', '9']), {}, { params });
    expect(scenarioPlan(open).liveCards).toEqual(['temp1', 'temp2']);
    expect(planAllows(open, { kind: 'card', cardId: 'temp0' })).toBe(false);
    const taken = context('ACTION_CHOICE', allow(['8']), { selectedCardIds: ['8'] }, { params });
    expect(scenarioPlan(taken)).toMatchObject({ liveCards: [], ready: true });
    // A decision without its list of actions offers nothing
    expect(scenarioPlan(context('ACTION_CHOICE', allow(['8']))).liveCards).toEqual([]);
  });

  describe('a choice of cards', () => {
    for (const type of ['CARD_SELECTION', 'ARBITRARY_CARDS'] as const) {
      it(`${type}: takes the cards of any allowed answer the choice so far could still become, Done once it is one`, () => {
        const answers = allow(['1,2', '3']);
        expect(scenarioPlan(context(type, answers)).liveCards).toEqual(['1', '2', '3']);
        const one = context(type, answers, { selectedCardIds: ['1'] });
        expect(scenarioPlan(one)).toMatchObject({ liveCards: ['2'], ready: false });
        expect(planAllows(one, { kind: 'card', cardId: '3' })).toBe(false);
        expect(planAllows(one, { kind: 'escape' })).toBe(false);
        // In any order
        const both = context(type, answers, { selectedCardIds: ['2', '1'] });
        expect(scenarioPlan(both)).toMatchObject({ liveCards: [], ready: true });
        expect(planAllows(both, { kind: 'button', button: 'Done' })).toBe(true);
        expect(planAllows(both, { kind: 'escape' })).toBe(true);
        // A card no answer has: nothing more may be clicked
        expect(scenarioPlan(context(type, answers, { selectedCardIds: ['4'] }))).toMatchObject({
          liveCards: [],
          ready: false,
        });
      });
    }

    it('counts choosing none as an answer when the step allows passing, or names "" as one', () => {
      expect(scenarioPlan(context('CARD_SELECTION', allow(['1'], { pass: true })))).toMatchObject({
        liveCards: ['1'],
        ready: true,
        pass: true,
      });
      expect(scenarioPlan(context('ARBITRARY_CARDS', allow([''])))).toMatchObject({ liveCards: [], ready: true });
      expect(scenarioPlan(context('CARD_SELECTION', allow(['1']))).ready).toBe(false);
    });
  });

  describe('an assignment', () => {
    // Frodo (10) takes the two orcs (20, 21), Sam (11) the troll (22): in that order
    const answers = allow(['10 20 21,11 22']);

    it('is made one pairing at a time in the written order: the minion first, then its character', () => {
      expect(scenarioPlan(context('ASSIGN_MINIONS', answers))).toMatchObject({ liveCards: ['20'], ready: false });
      const picked = context('ASSIGN_MINIONS', answers, { minion: '20' });
      expect(scenarioPlan(picked).liveCards).toEqual(['10']);
      expect(planAllows(picked, { kind: 'card', cardId: '11' })).toBe(false);
      expect(scenarioPlan(context('ASSIGN_MINIONS', answers, { assignments: { '20': '10' } })).liveCards).toEqual([
        '21',
      ]);
      expect(
        scenarioPlan(context('ASSIGN_MINIONS', answers, { assignments: { '20': '10', '21': '10' } })).liveCards,
      ).toEqual(['22']);
      const done = context('ASSIGN_MINIONS', answers, { assignments: { '20': '10', '21': '10', '22': '11' } });
      expect(scenarioPlan(done)).toMatchObject({ liveCards: [], ready: true });
      expect(planAllows(done, { kind: 'button', button: 'Done' })).toBe(true);
      // ...but not with a minion picked up again, waiting for its character
      expect(
        scenarioPlan(
          context('ASSIGN_MINIONS', answers, { assignments: { '20': '10', '21': '10', '22': '11' }, minion: '22' }),
        ).ready,
      ).toBe(false);
    });

    it('takes nothing more once a pairing no answer has is made', () => {
      expect(scenarioPlan(context('ASSIGN_MINIONS', answers, { assignments: { '20': '11' } }))).toMatchObject({
        liveCards: [],
        ready: false,
      });
    });

    it('offers every character some answer gives the minion, and Done with none assigned when it may pass', () => {
      const either = allow(['10 20', '11 20'], { pass: true });
      expect(scenarioPlan(context('ASSIGN_MINIONS', either))).toMatchObject({ liveCards: ['20'], ready: true });
      expect(scenarioPlan(context('ASSIGN_MINIONS', either, { minion: '20' })).liveCards).toEqual(['10', '11']);
      // A character with no minion in the answer ("10") and an empty answer are fine
      expect(scenarioPlan(context('ASSIGN_MINIONS', allow(['10,11 20', '']))).liveCards).toEqual(['20']);
    });

    it("tells when a character has every minion that every answer still possible gives it (the moment 'assigned')", () => {
      const local = (assignments: Record<string, string>): LocalChoices => ({ ...NO_CHOICES, assignments });
      expect(characterComplete(answers, local({ '20': '10' }), '10')).toBe(false);
      expect(characterComplete(answers, local({ '20': '10', '21': '10' }), '10')).toBe(true);
      expect(characterComplete(answers, local({ '20': '10', '21': '10' }), '11')).toBe(false);
      // A character no answer gives a minion has them all; and once nothing is possible, nobody has
      expect(characterComplete(answers, local({}), '12')).toBe(true);
      expect(characterComplete(answers, local({ '20': '11' }), '11')).toBe(false);
    });
  });

  it('takes the allowed places of a multiple choice', () => {
    const choice = context('MULTIPLE_CHOICE', allow(['0', '2', 'x']));
    expect(scenarioPlan(choice)).toEqual({ liveCards: [], ready: false, pass: false, options: [0, 2], numbers: [] });
    expect(planAllows(choice, { kind: 'choice', index: 2 })).toBe(true);
    expect(planAllows(choice, { kind: 'choice', index: 1 })).toBe(false);
  });

  it('takes the allowed numbers, as the game reads them', () => {
    const number = context('INTEGER', allow(['3']));
    expect(scenarioPlan(number)).toEqual({ liveCards: [], ready: false, pass: false, options: [], numbers: [3] });
    expect(planAllows(number, { kind: 'number', value: '03' })).toBe(true);
    expect(planAllows(number, { kind: 'number', value: '3' })).toBe(true);
    expect(planAllows(number, { kind: 'number', value: '4' })).toBe(false);
    expect(planAllows(number, { kind: 'card', cardId: '3' })).toBe(false);
  });
});
