import { Decision } from '../protocol/game-events';
import { emptyGame, GameModel } from './game-model';
import {
  assignMinion,
  assignmentAnswer,
  assignmentOf,
  choicesFit,
  clearSelection,
  decisionAnswered,
  selectCard,
  unassignMinion,
} from './local-choices';

const waiting = (type: Decision['type'], params: Record<string, string[]> = {}): GameModel => ({
  ...emptyGame(),
  decision: { decision: { id: '1', participantId: 'alice', type, text: 'Do it', params }, arrivedAt: 0 },
});
const assignment = () => waiting('ASSIGN_MINIONS', { freeCharacters: ['10', '11'], minions: ['20', '21', '22'] });

describe('local choices', () => {
  it('assigns minions only while an assignment waits; again moves it; unassigning takes it off', () => {
    expect(assignMinion(waiting('CARD_SELECTION'), '20', '10').local.assignments).toEqual({});
    let model = assignMinion(assignment(), '20', '10');
    model = assignMinion(model, '20', '11');
    expect(model.local.assignments).toEqual({ '20': '11' });
    expect(unassignMinion(model, '21')).toBe(model);
    expect(unassignMinion(model, '20').local.assignments).toEqual({});
  });

  it("the player's assignment comes before the server's", () => {
    let model = assignment();
    model = {
      ...model,
      cards: {
        '21': {
          id: '21',
          blueprintId: 'x',
          zone: 'SHADOW_CHARACTERS',
          owner: 'bob',
          hindered: false,
          index: null,
          attachedTo: null,
          stackedOn: null,
          tokens: {},
          inactive: false,
          assignedTo: '10',
          inSkirmish: false,
          testingText: null,
          backSideTestingText: null,
        },
      },
    };
    expect(assignmentOf(model, '21')).toBe('10');
    expect(assignmentOf(assignMinion(model, '21', '11'), '21')).toBe('11');
    expect(assignmentOf(model, '99')).toBeNull();
  });

  it("answers an assignment as the server takes it: each character with its minions, in the decision's order", () => {
    let model = assignMinion(assignment(), '22', '10');
    model = assignMinion(model, '20', '10');
    expect(assignmentAnswer(model)).toBe('10 20 22,11');
    expect(assignmentAnswer(waiting('INTEGER'))).toBe('');
  });

  it('selects each card once while a decision waits, and clears the selection', () => {
    expect(selectCard(emptyGame(), '1').local.selectedCardIds).toEqual([]);
    let model = selectCard(waiting('CARD_SELECTION'), '1');
    model = selectCard(model, '1');
    model = selectCard(model, '2');
    expect(model.local.selectedCardIds).toEqual(['1', '2']);
    expect(clearSelection(model).local.selectedCardIds).toEqual([]);
  });

  it('an answered decision waits no more, and what was chosen for it is gone', () => {
    const model = decisionAnswered(selectCard(waiting('CARD_SELECTION'), '1'));
    expect(model.decision).toBeNull();
    expect(model.local).toEqual({ assignments: {}, selectedCardIds: [] });
  });

  it('tells whether restored choices still fit the waiting decision', () => {
    const cards = waiting('CARD_SELECTION', { cardId: ['1', '2'] });
    expect(choicesFit(cards, { assignments: {}, selectedCardIds: ['2'] })).toBe(true);
    expect(choicesFit(cards, { assignments: {}, selectedCardIds: ['3'] })).toBe(false);
    expect(choicesFit(assignment(), { assignments: { '20': '10' }, selectedCardIds: [] })).toBe(true);
    expect(choicesFit(assignment(), { assignments: { '20': '99' }, selectedCardIds: [] })).toBe(false);
    expect(choicesFit(emptyGame(), { assignments: {}, selectedCardIds: [] })).toBe(false);
  });
});
