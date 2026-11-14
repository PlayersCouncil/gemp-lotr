import { CardRef, Decision, GameEvent } from '../protocol/game-events';
import { applyEvents } from '../state/apply-event';
import { emptyGame, GameModel } from '../state/game-model';
import { assignMinion } from '../state/local-choices';
import { gameTable, TableStack } from './game-table';

const ref = (cardId: string, extra: Partial<CardRef> = {}): CardRef => ({
  cardId,
  blueprintId: '1_7',
  participantId: 'alice',
  controllerId: null,
  zone: 'FREE_CHARACTERS',
  hindered: false,
  index: null,
  targetCardId: null,
  targetType: null,
  testingText: null,
  backSideTestingText: null,
  ...extra,
});
const put = (cardId: string, extra: Partial<CardRef> = {}): GameEvent => ({ type: 'PCIP', ...ref(cardId, extra) });
const players: GameEvent = {
  type: 'P',
  participantId: 'alice',
  allParticipantIds: ['alice', 'bob'],
  discardPublic: false,
};
const play = (...events: GameEvent[]): GameModel => applyEvents(emptyGame(), [players, ...events]);
const ids = (stacks: readonly TableStack[]) => stacks.map((stack) => stack.card.id);

describe('gameTable', () => {
  it('puts each card in the row of its zone and its side of the table', () => {
    const table = gameTable(
      play(
        put('1'),
        put('2', { participantId: 'bob' }),
        put('3', { zone: 'SUPPORT' }),
        put('4', { zone: 'SUPPORT', participantId: 'bob' }),
        put('5', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
        put('6', { zone: 'HAND' }),
        put('7', { zone: 'HAND', participantId: 'bob' }),
        put('8', { zone: 'DISCARD' }),
        // A card held by the other player lies on their side
        put('9', { controllerId: 'bob' }),
      ),
    );
    expect(table.bottom).toBe('alice');
    expect(ids(table.charactersPlayer)).toEqual(['1']);
    expect(ids(table.charactersOpponent)).toEqual(['2', '9']);
    expect(ids(table.supportPlayer)).toEqual(['3']);
    expect(ids(table.supportOpponent)).toEqual(['4']);
    expect(ids(table.shadow)).toEqual(['5']);
    // Only the player's own hand; a revealed hand and the piles are shown elsewhere
    expect(ids(table.hand)).toEqual(['6']);
    expect(table.skirmish).toBeNull();
    expect(table.assignments).toEqual([]);
  });

  it('keeps the order the server put the cards in, unless the player arranged them', () => {
    const model = play(put('1', { zone: 'HAND' }), put('2', { zone: 'HAND' }), put('3', { zone: 'HAND' }));
    expect(ids(gameTable(model).hand)).toEqual(['1', '2', '3']);
    // Cards the player moved first, in their order; an id that is gone is ignored
    expect(ids(gameTable(model, ['3', '99', '1']).hand)).toEqual(['3', '1', '2']);
  });

  it('draws what lies under a card before it: stacked cards, then attached ones, each after its own', () => {
    const table = gameTable(
      play(
        put('1'),
        put('2', { zone: 'ATTACHED', targetCardId: '1', targetType: 'attached' }),
        put('3', { zone: 'STACKED', targetCardId: '1', targetType: 'stacked' }),
        put('4', { zone: 'ATTACHED', targetCardId: '2', targetType: 'attached' }),
      ),
    );
    expect(ids(table.charactersPlayer)).toEqual(['1']);
    expect(table.charactersPlayer[0]?.under.map(({ card, kind }) => `${card.id}:${kind}`)).toEqual([
      '3:stacked',
      '4:attached',
      '2:attached',
    ]);
  });

  it('puts a card moved onto another after the ones already there', () => {
    const table = gameTable(
      play(
        put('1'),
        put('2', { zone: 'ATTACHED', targetCardId: '9', targetType: 'attached' }),
        put('3', { zone: 'ATTACHED', targetCardId: '1', targetType: 'attached' }),
        { type: 'MCIP', ...ref('2', { zone: 'ATTACHED', targetCardId: '1', targetType: 'attached' }) },
      ),
    );
    expect(table.charactersPlayer[0]?.under.map(({ card }) => card.id)).toEqual(['3', '2']);
  });

  it('lists the sites in their order, with the players standing on them', () => {
    const table = gameTable(
      play(
        put('12', { zone: 'ADVENTURE_PATH', index: 2 }),
        put('11', { zone: 'ADVENTURE_PATH', index: 1 }),
        { type: 'PP', participantId: 'alice', index: 2 },
        { type: 'PP', participantId: 'bob', index: 1 },
        { type: 'TC', participantId: 'bob', inactiveCardIds: [] },
      ),
    );
    expect(table.sites.map((site) => [site.card.id, site.players])).toEqual([
      ['11', [{ number: 2, current: true }]],
      ['12', [{ number: 1, current: false }]],
    ]);
    expect(table.bottomTurn).toBe(false);
  });

  it('takes assigned minions and their character out of their rows, into a box of their own', () => {
    const table = gameTable(
      play(
        put('1'),
        put('2'),
        put('5', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
        put('6', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
        put('7', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
        { type: 'AA', cardId: '2', opposingCardIds: ['5', '6'] },
        { type: 'TC', participantId: 'alice', inactiveCardIds: [] },
      ),
    );
    expect(ids(table.charactersPlayer)).toEqual(['1']);
    expect(ids(table.shadow)).toEqual(['7']);
    expect(table.assignments).toHaveLength(1);
    expect(table.assignments[0]?.characterId).toBe('2');
    expect(ids(table.assignments[0]?.minions ?? [])).toEqual(['5', '6']);
    expect(ids(table.assignments[0]?.characters ?? [])).toEqual(['2']);
    expect(table.bottomTurn).toBe(true);
  });

  it('shows the assignments the player has made but not sent, in the order of the characters', () => {
    const decision: Decision = {
      id: '1',
      participantId: 'alice',
      type: 'ASSIGN_MINIONS',
      text: 'Assign',
      params: { freeCharacters: ['1', '10'], minions: ['5', '6'] },
    };
    let model = play(
      put('10'),
      put('1'),
      put('5', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
      put('6', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
      { type: 'D', decision },
    );
    model = assignMinion(assignMinion(model, '5', '10'), '6', '1');
    const table = gameTable(model);
    expect(table.assignments.map((assignment) => assignment.characterId)).toEqual(['1', '10']);
    expect(ids(table.shadow)).toEqual([]);
    expect(ids(table.charactersPlayer)).toEqual([]);
  });

  it('gives a skirmish split into waiting ones a see-through copy of the character, after the others', () => {
    const table = gameTable(
      play(
        put('1'),
        put('2'),
        put('5', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
        put('6', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
        { type: 'AA', cardId: '-1', opposingCardIds: ['5'] },
        { type: 'AA', cardId: '2', opposingCardIds: ['6'] },
      ),
    );
    expect(table.assignments.map((assignment) => assignment.characterId)).toEqual(['2', '-1']);
    expect(ids(table.assignments[1]?.characters ?? [])).toEqual(['extra-1']);
    // The copy isn't one of the hand's extra cards
    expect(ids(table.hand)).toEqual([]);
    // The real character stays in its row
    expect(ids(table.charactersPlayer)).toEqual(['1']);
  });

  it('puts the cards of the skirmish being fought in the skirmish box', () => {
    const table = gameTable(
      play(put('1'), put('5', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }), {
        type: 'SS',
        cardId: '1',
        opposingCardIds: ['5'],
      }),
    );
    expect(ids(table.skirmish?.characters ?? [])).toEqual(['1']);
    expect(ids(table.skirmish?.minions ?? [])).toEqual(['5']);
    expect(ids(table.charactersPlayer)).toEqual([]);
    expect(ids(table.shadow)).toEqual([]);
  });

  it("shows a spectator the second player's side, without a hand", () => {
    const table = gameTable(
      applyEvents(emptyGame(), [
        { type: 'P', participantId: 'carol', allParticipantIds: ['alice', 'bob'], discardPublic: false },
        put('1'),
        put('2', { participantId: 'bob' }),
        put('3', { zone: 'HAND', participantId: 'bob' }),
        put('4', { zone: 'EXTRA' }),
      ]),
    );
    expect(table.spectator).toBe(true);
    expect(table.bottom).toBe('bob');
    expect(ids(table.charactersPlayer)).toEqual(['2']);
    expect(ids(table.charactersOpponent)).toEqual(['1']);
    expect(table.hand).toEqual([]);
  });

  it('shows the cards a decision adds to the hand row (zone EXTRA)', () => {
    expect(ids(gameTable(play(put('4', { zone: 'EXTRA' }))).hand)).toEqual(['4']);
  });
});
