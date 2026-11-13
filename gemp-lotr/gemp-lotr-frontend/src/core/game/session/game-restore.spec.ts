import { TestBed } from '@angular/core/testing';
import { SessionStore } from '../../platform/session-store';
import { Decision } from '../protocol/game-events';
import { emptyGame, GameModel } from '../state/game-model';
import { decisionKey, GameRestore, mergeMessages } from './game-restore';

class FakeStore {
  readonly values = new Map<string, unknown>();
  read(key: string): unknown {
    return this.values.get(key) ?? null;
  }
  write(key: string, value: unknown): void {
    this.values.set(key, JSON.parse(JSON.stringify(value)));
  }
  remove(key: string): void {
    this.values.delete(key);
  }
}

const decision: Decision = {
  id: '1',
  participantId: 'alice',
  type: 'ASSIGN_MINIONS',
  text: 'Assign',
  params: { freeCharacters: ['10'], minions: ['20'] },
};
const waiting = (d: Decision = decision): GameModel => ({ ...emptyGame(), decision: { decision: d, arrivedAt: 3 } });
const m = (text: string) => ({ kind: 'message' as const, text });

function setUp() {
  const store = new FakeStore();
  TestBed.configureTestingModule({ providers: [GameRestore, { provide: SessionStore, useValue: store }] });
  return { restore: TestBed.inject(GameRestore), store };
}

describe('GameRestore', () => {
  it('brings back the choices when the same decision waits after the reload (it may have come at another moment)', () => {
    const { restore } = setUp();
    const before = { ...waiting(), local: { assignments: { '20': '10' }, selectedCardIds: [] }, messages: [m('a')] };
    restore.save('7', before, { tab: 'settings' });
    const after = restore.restore('7', { ...waiting(), decision: { decision, arrivedAt: 40 } });
    expect(after.local.assignments).toEqual({ '20': '10' });
    expect(after.messages).toEqual([m('a')]);
    expect(restore.saved('7')?.view).toEqual({ tab: 'settings' });
  });

  it('does not bring back choices for another decision, or ones that no longer fit', () => {
    const { restore } = setUp();
    restore.save('7', { ...waiting(), local: { assignments: { '20': '10' }, selectedCardIds: [] } }, {});
    expect(restore.restore('7', waiting({ ...decision, text: 'Other' })).local.assignments).toEqual({});
    expect(restore.restore('7', emptyGame()).local.assignments).toEqual({});
    restore.save('7', { ...waiting(), local: { assignments: { '99': '10' }, selectedCardIds: [] } }, {});
    expect(restore.restore('7', waiting()).local.assignments).toEqual({});
  });

  it('leaves a game it has nothing for as it is, and forgets a game', () => {
    const { restore } = setUp();
    const model = waiting();
    expect(restore.restore('8', model)).toBe(model);
    restore.save('8', model, {});
    restore.forget('8');
    expect(restore.saved('8')).toBeNull();
  });

  it('tells decisions apart by what they ask', () => {
    expect(decisionKey(decision)).toBe(decisionKey({ ...decision }));
    expect(decisionKey(decision)).not.toBe(decisionKey({ ...decision, params: { minions: ['21'] } }));
  });
});

describe('mergeMessages', () => {
  it('keeps the part the page and the server share once', () => {
    expect(mergeMessages([m('1'), m('2'), m('3')], [m('2'), m('3'), m('4')])).toEqual([m('1'), m('2'), m('3'), m('4')]);
  });

  it('keeps repeated messages as often as they came', () => {
    expect(mergeMessages([m('x'), m('x')], [m('x'), m('x'), m('y')])).toEqual([m('x'), m('x'), m('y')]);
  });

  it('keeps both when they share nothing, or when one is empty', () => {
    expect(mergeMessages([m('1')], [m('5')])).toEqual([m('1'), m('5')]);
    expect(mergeMessages([], [m('5')])).toEqual([m('5')]);
    expect(mergeMessages([m('1')], [])).toEqual([m('1')]);
  });

  it('a warning and a message of the same text are different', () => {
    expect(mergeMessages([m('a')], [{ kind: 'warning', text: 'a' }])).toHaveLength(2);
  });
});
