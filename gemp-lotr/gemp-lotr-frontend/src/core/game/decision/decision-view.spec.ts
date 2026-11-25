import { Decision } from '../protocol/game-events';
import { emptyGame, GameModel, NO_CHOICES } from '../state/game-model';
import { actionCardIds, decisionView, minimum, minionUnassigned, NO_DECISION, noEffectReason } from './decision-view';

const SETTINGS = { autoAccept: true, alwaysDropDown: false };

const decision = (type: Decision['type'], params: Record<string, string[]> = {}): Decision => ({
  id: '1',
  participantId: 'alice',
  type,
  text: 'Do it',
  params,
});

function waiting(d: Decision, local: Partial<GameModel['local']> = {}, extra: Partial<GameModel> = {}): GameModel {
  return { ...emptyGame(), ...extra, decision: { decision: d, arrivedAt: 0 }, local: { ...NO_CHOICES, ...local } };
}

describe('decisionView', () => {
  it('shows nothing while no decision waits', () => {
    expect(decisionView(emptyGame(), SETTINGS)).toEqual(NO_DECISION);
  });

  it("shows nothing while a scripted game's table is changed by hand (a sandbox stopped): nobody is asked", () => {
    const asked = waiting(decision('CARD_ACTION_CHOICE', { actionId: ['1'], cardId: ['5'], actionText: ['Play'] }));
    const editing = { ...asked, scenario: { info: { mode: 'edit' } } } as unknown as GameModel;
    expect(decisionView(editing, SETTINGS)).toEqual(NO_DECISION);
    // (and while the page's table is edited by a level the info does not say: the author's Edit table)
    expect(decisionView(asked, { ...SETTINGS, editing: true })).toEqual(NO_DECISION);
    const playing = { ...asked, scenario: { info: { mode: 'play' } } } as unknown as GameModel;
    expect(decisionView(playing, SETTINGS).alert).toBe('Do it');
  });

  it('shows nothing on a table flipped onto a seat that plays itself: the page watches (flip table)', () => {
    const asked = waiting(decision('CARD_ACTION_CHOICE', { actionId: ['1'], cardId: ['5'], actionText: ['Play'] }));
    const info = { mode: 'play', humanSeat: 'freeps', seat: 'shadow', otherSeat: 'script' };
    const watching = { ...asked, scenario: { info } } as unknown as GameModel;
    expect(decisionView(watching, SETTINGS)).toEqual(NO_DECISION);
    const playing = { ...asked, scenario: { info: { ...info, otherSeat: 'person' } } } as unknown as GameModel;
    expect(decisionView(playing, SETTINGS).alert).toBe('Do it');
  });

  it('keeps the alert frame when the game ended while the alert box asked, as the legacy page did', () => {
    const ended = { ...emptyGame(), ended: true };
    expect(decisionView({ ...ended, closedByEnd: 'CARD_SELECTION' }, SETTINGS).frame).toBe('ended');
    expect(decisionView({ ...ended, closedByEnd: 'INTEGER' }, SETTINGS).frame).toBeNull();
  });

  it('asks for a number in a dialog, between its limits (none said: 0 to 1000)', () => {
    expect(
      decisionView(waiting(decision('INTEGER', { min: ['1'], max: ['3'], defaultValue: ['2'] })), SETTINGS).dialog,
    ).toEqual({
      kind: 'integer',
      text: 'Do it',
      min: 1,
      max: 3,
      value: 2,
      answerable: true,
    });
    expect(decisionView(waiting(decision('INTEGER')), SETTINGS).dialog).toMatchObject({ min: 0, max: 1000, value: 0 });
  });

  it('asks for one of two answers with buttons, of more (or always, if set) with a list', () => {
    const two = decision('MULTIPLE_CHOICE', { results: ['Yes', 'No'] });
    expect(decisionView(waiting(two), SETTINGS).dialog).toMatchObject({ kind: 'choice', dropDown: false });
    expect(decisionView(waiting(two), { ...SETTINGS, alwaysDropDown: true }).dialog).toMatchObject({ dropDown: true });
    const three = decision('MULTIPLE_CHOICE', { results: ['a', 'b', 'c'] });
    expect(decisionView(waiting(three), SETTINGS).dialog).toMatchObject({ dropDown: true, options: ['a', 'b', 'c'] });
  });

  it('shows cards to choose from in a dialog: the selectable ones offered, the chosen marked, the buttons as allowed', () => {
    const d = decision('ARBITRARY_CARDS', {
      cardId: ['a', 'b', 'c'],
      blueprintId: ['1_1', '1_2', '1_3'],
      selectable: ['true', 'true', 'false'],
      testingText: ['null', 'Test', 'null'],
      min: ['1'],
      max: ['2'],
    });
    const none = decisionView(waiting(d), SETTINGS);
    expect(none.marks).toEqual({ a: 'selectable', b: 'selectable' });
    expect(none.dialog).toEqual({
      kind: 'cards',
      title: 'Do it',
      cards: [
        { id: 'a', blueprintId: '1_1', text: null, testingText: null },
        { id: 'b', blueprintId: '1_2', text: null, testingText: 'Test' },
        { id: 'c', blueprintId: '1_3', text: null, testingText: null },
      ],
      buttons: [],
    });
    const one = decisionView(waiting(d, { selectedCardIds: ['a'] }), SETTINGS);
    expect(one.marks).toEqual({ a: 'selected', b: 'selectable' });
    expect(one.dialog).toMatchObject({ buttons: ['Clear selection', 'Done'] });
    // The most chosen: nothing more is offered
    expect(decisionView(waiting(d, { selectedCardIds: ['a', 'b'] }), SETTINGS).marks).toEqual({
      a: 'selected',
      b: 'selected',
    });
  });

  it("shows a choice of actions as cards with their texts, the chosen one's card marked", () => {
    const d = decision('ACTION_CHOICE', {
      blueprintId: ['1_1', '1_2'],
      actionId: ['7', '8'],
      actionText: ['First', 'Second'],
    });
    const none = decisionView(waiting(d), SETTINGS);
    expect(none.marks).toEqual({ temp0: 'selectable', temp1: 'selectable' });
    expect(none.dialog).toMatchObject({
      cards: [
        { id: 'temp0', text: 'First' },
        { id: 'temp1', text: 'Second' },
      ],
      buttons: [],
    });
    const chosen = decisionView(waiting(d, { selectedCardIds: ['8'], markedCardIds: ['temp1'] }), SETTINGS);
    expect(chosen.marks).toEqual({ temp1: 'selected' });
    expect(chosen.dialog).toMatchObject({ buttons: ['Clear selection', 'Done'] });
  });

  it("offers the cards' actions in the alert box, a card not on the table as a stand-in in the hand's row", () => {
    const d = decision('CARD_ACTION_CHOICE', {
      cardId: ['5', '5', '9'],
      blueprintId: ['inPlay', 'inPlay', '1_9'],
      actionId: ['1', '2', '3'],
      actionText: ['Use', 'Exert', 'Play'],
      testingText: ['null', 'null', 'null'],
    });
    expect(actionCardIds(d)).toEqual(['5', '5', 'extra9']);
    const none = decisionView(waiting(d), SETTINGS);
    expect(none).toMatchObject({ alert: 'Do it', frame: 'action', buttons: ['Pass'], doneOnRight: true });
    expect(none.marks).toEqual({ '5': 'actionable', extra9: 'actionable' });
    expect(none.actions).toEqual({
      '5': [
        { actionId: '1', text: 'Use', noEffect: null },
        { actionId: '2', text: 'Exert', noEffect: null },
      ],
      extra9: [{ actionId: '3', text: 'Play', noEffect: null }],
    });
    // A server before 2026-11-17 sends no noEffect: nothing is marked
    expect(none.noEffect).toEqual({});
    expect(none.extraCards.map((card) => [card.id, card.blueprintId, card.zone])).toEqual([['extra9', '1_9', 'EXTRA']]);
    const chosen = decisionView(waiting(d, { selectedCardIds: ['2'], markedCardIds: ['5'] }), SETTINGS);
    expect(chosen).toMatchObject({ buttons: ['Reset choice', 'Done'], marks: { '5': 'selected' } });
  });

  it('marks a card whose every action would do nothing, and says why each one would (the new master)', () => {
    const d = decision('CARD_ACTION_CHOICE', {
      cardId: ['5', '5', '7', '9'],
      blueprintId: ['inPlay', 'inPlay', 'inPlay', '1_9'],
      actionId: ['1', '2', '3', '4'],
      actionText: ['Use', 'Exert', 'Pump', 'Play'],
      testingText: ['null', 'null', 'null', 'null'],
      noEffect: ['limit', 'false', 'skirmish', 'effect'],
    });
    const view = decisionView(waiting(d), SETTINGS);
    expect(view.actions['5']?.map((action) => action.noEffect)).toEqual(['limit', null]);
    expect(view.actions['7']?.[0]?.noEffect).toBe('skirmish');
    // 5 has an action that does something: blue, as game.html's markNoEffectCards; 7 and the stand-in for 9 yellow
    expect(view.noEffect).toEqual({ '7': true, extra9: true });
    expect(view.marks).toEqual({ '5': 'actionable', '7': 'actionable', extra9: 'actionable' });
    // Once a card is chosen nothing is offered: nothing is marked either
    expect(decisionView(waiting(d, { selectedCardIds: ['3'], markedCardIds: ['7'] }), SETTINGS).noEffect).toEqual({});
    expect(['effect', 'limit', 'skirmish'].map((why) => noEffectReason(why as 'effect'))).toEqual([
      'no effect',
      'limit reached',
      'no one in this skirmish',
    ]);
  });

  it('asks to choose cards in the alert box: Done once enough are chosen', () => {
    const d = decision('CARD_SELECTION', { cardId: ['1', '2'], min: ['1'], max: ['1'] });
    expect(decisionView(waiting(d), SETTINGS)).toMatchObject({
      frame: 'selection',
      buttons: [],
      marks: { '1': 'selectable', '2': 'selectable' },
    });
    expect(decisionView(waiting(d, { selectedCardIds: ['2'] }), SETTINGS)).toMatchObject({
      buttons: ['Reset choice', 'Done'],
      marks: { '2': 'selected' },
    });
  });

  it('offers the minions to assign, then the characters for the one clicked (and itself, to take it back)', () => {
    const d = decision('ASSIGN_MINIONS', { freeCharacters: ['1', '2'], minions: ['5', '6'] });
    expect(decisionView(waiting(d), SETTINGS)).toMatchObject({
      buttons: ['Done'],
      doneOnRight: false,
      marks: { '5': 'selectable', '6': 'selectable' },
    });
    expect(decisionView(waiting(d, { minion: '6' }), SETTINGS).marks).toEqual({
      '1': 'selectable',
      '2': 'selectable',
      '6': 'selectable',
    });
  });

  it("shows a replay's decisions without anything to answer them with", () => {
    const replay = { ...SETTINGS, replay: true };
    expect(decisionView(waiting(decision('INTEGER')), replay).dialog).toMatchObject({ answerable: false });
    const d = decision('CARD_SELECTION', { cardId: ['1'], min: ['0'], max: ['1'] });
    expect(decisionView(waiting(d), replay).buttons).toEqual([]);
    const cards = decision('ARBITRARY_CARDS', { cardId: ['a'], selectable: ['true'], min: ['0'], max: ['1'] });
    expect(decisionView(waiting(cards), replay).dialog).toMatchObject({ buttons: [] });
  });

  it('reads the least number of cards (none said: 0)', () => {
    expect(minimum(decision('CARD_SELECTION', { min: ['2'] }))).toBe(2);
    expect(minimum(decision('CARD_SELECTION'))).toBe(0);
  });

  it('tells whether a minion of the waiting assignment has no character', () => {
    const d = decision('ASSIGN_MINIONS', { freeCharacters: ['1'], minions: ['5'] });
    const card = { ...emptyGame().cards, '5': { id: '5' } } as unknown as GameModel['cards'];
    expect(minionUnassigned(waiting(d, {}, { cards: card }))).toBe(true);
    expect(minionUnassigned(waiting(d, { assignments: { '5': '1' } }, { cards: card }))).toBe(false);
    expect(minionUnassigned(emptyGame())).toBe(false);
  });

  it('asks about a minion left over only when more could have been assigned than were (the server says how many)', () => {
    const cards = { '5': { id: '5' }, '6': { id: '6' } } as unknown as GameModel['cards'];
    const d = (most: string) =>
      decision('ASSIGN_MINIONS', { freeCharacters: ['1'], minions: ['5', '6', '9'], maxAssignable: [most] });
    // One character for two minions: one assigned is as many as can be
    expect(minionUnassigned(waiting(d('1'), { assignments: { '5': '1' } }, { cards }))).toBe(false);
    expect(minionUnassigned(waiting(d('1'), {}, { cards }))).toBe(true);
    expect(minionUnassigned(waiting(d('2'), { assignments: { '5': '1' } }, { cards }))).toBe(true);
    // A server that doesn't say (or says nonsense) is asked about any minion left
    expect(minionUnassigned(waiting(d('many'), { assignments: { '5': '1' } }, { cards }))).toBe(true);
    // (a minion not on the table, 9, doesn't count)
    expect(minionUnassigned(waiting(d('1'), { assignments: { '5': '1', '6': '1' } }, { cards }))).toBe(false);
  });
});
