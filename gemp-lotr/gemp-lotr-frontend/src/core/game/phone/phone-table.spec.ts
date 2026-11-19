import { CardRef, GameEvent } from '../protocol/game-events';
import { applyEvents } from '../state/apply-event';
import { emptyGame, GameModel } from '../state/game-model';
import { gameTable } from '../table/game-table';
import { isShadowCard } from './card-sides';
import { PhoneCard, phoneFolding, phoneTable } from './phone-table';

// Blueprints whose side the card definitions give: 1_7 and 1_2 Free Peoples, 1_121 and 1_151 Shadow
const FREE = '1_7';
const SHADOW = '1_121';

const put = (cardId: string, extra: Partial<CardRef> = {}): GameEvent => ({
  type: 'PCIP',
  cardId,
  blueprintId: FREE,
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
const players: GameEvent = {
  type: 'P',
  participantId: 'alice',
  allParticipantIds: ['alice', 'bob'],
  discardPublic: false,
};
const play = (...events: GameEvent[]): GameModel => applyEvents(emptyGame(), [players, ...events]);
const phone = (model: GameModel) => phoneTable(gameTable(model), model);
const ids = (cards: readonly PhoneCard[]) => cards.map((card) => card.card.id);

describe('isShadowCard', () => {
  it('knows the Shadow cards from the card definitions, foils and alternate images as the card', () => {
    expect(isShadowCard(SHADOW)).toBe(true);
    expect(isShadowCard(`${SHADOW}*`)).toBe(true);
    expect(isShadowCard(FREE)).toBe(false);
    // A promo mapped to a Shadow card (blueprintMapping.txt: 0_10 is 2_51): Shadow too
    expect(isShadowCard('0_10')).toBe(true);
  });
});

describe('phoneTable', () => {
  it('lays out the rows from the opponent down to the player, with the players named', () => {
    const table = phone(
      play(
        put('1'),
        put('2', { participantId: 'bob' }),
        put('3', { zone: 'SUPPORT' }),
        put('4', { zone: 'SUPPORT', participantId: 'bob' }),
        put('5', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
        put('6', { zone: 'HAND' }),
      ),
    );
    expect(table.above.map((row) => [row.key, row.side, row.label, ids(row.cards)])).toEqual([
      ['support-opponent', 'them', "bob's support", ['4']],
      ['characters-opponent', 'them', "bob's characters", ['2']],
      ['shadow', 'minions', 'Minions', ['5']],
    ]);
    expect(table.below.map((row) => [row.key, row.side, row.label, ids(row.cards)])).toEqual([
      ['characters-player', 'mine', 'Your characters', ['1']],
      ['support-player', 'mine', 'Your support', ['3']],
    ]);
    expect(ids(table.hand)).toEqual(['6']);
  });

  it('puts the Free Peoples cards of a support row first, then the Shadow ones, and marks where they start', () => {
    const table = phone(
      play(
        put('1', { zone: 'SUPPORT', blueprintId: SHADOW }),
        put('2', { zone: 'SUPPORT' }),
        put('3', { zone: 'SUPPORT', blueprintId: '1_151' }),
        put('4', { zone: 'SUPPORT', blueprintId: '1_2' }),
      ),
    );
    const support = table.below[1];
    expect(ids(support?.cards ?? [])).toEqual(['2', '4', '1', '3']);
    expect(support?.shadowFrom).toBe(2);
  });

  it('marks no start of the Shadow cards when a row holds only one side', () => {
    const table = phone(play(put('1', { zone: 'SUPPORT', blueprintId: SHADOW })));
    expect(table.below[1]?.shadowFrom).toBeNull();
  });

  it('keeps the cards under a card with it, in the order they came', () => {
    const table = phone(
      play(
        put('1'),
        put('2', { zone: 'ATTACHED', targetCardId: '1' }),
        put('3', { zone: 'ATTACHED', targetCardId: '1' }),
      ),
    );
    const [frodo] = table.below[0]?.cards ?? [];
    expect(frodo?.card.id).toBe('1');
    expect(frodo?.under.map((card) => card.id)).toEqual(['2', '3']);
  });

  it('shows the whole path: the sites played, the empty places as numbers, who stands where', () => {
    const model = play(
      put('s1', { zone: 'ADVENTURE_PATH', index: 1, blueprintId: '1_319' }),
      put('s2', { zone: 'ADVENTURE_PATH', index: 2, blueprintId: '1_327' }),
      { type: 'PP', participantId: 'alice', index: 2 },
      { type: 'PP', participantId: 'bob', index: 1 },
      { type: 'TC', participantId: 'alice', inactiveCardIds: [] },
    );
    const path = phone(model).path;
    expect(path.length).toBe(9);
    expect(path.map((site) => site.card?.id ?? null)).toEqual(['s1', 's2', null, null, null, null, null, null, null]);
    expect(path[1]?.players).toEqual([{ number: 1, name: 'alice', current: true }]);
    expect(path[0]?.players).toEqual([{ number: 2, name: 'bob', current: false }]);
    expect(path.map((site) => site.here)).toEqual([false, true, false, false, false, false, false, false, false]);
    expect(path[2]?.ahead).toBe(true);
  });

  it("marks the site where the turn's player stands, whoever looks", () => {
    const model = play(
      put('s1', { zone: 'ADVENTURE_PATH', index: 1, blueprintId: '1_319' }),
      put('s2', { zone: 'ADVENTURE_PATH', index: 2, blueprintId: '1_327' }),
      { type: 'PP', participantId: 'alice', index: 1 },
      { type: 'PP', participantId: 'bob', index: 2 },
      { type: 'TC', participantId: 'bob', inactiveCardIds: [] },
    );
    const path = phone(model).path;
    // Bob's turn at site 2; Alice looks from site 1: the game happens at site 2
    expect(path.map((site) => site.here)).toEqual([false, true, false, false, false, false, false, false, false]);
  });

  it('shows the skirmish first, then the assignments, as pairs; the minions fighting leave their row', () => {
    const model = play(
      put('1'),
      put('2'),
      put('10', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
      put('11', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
      put('12', { zone: 'SHADOW_CHARACTERS', participantId: 'bob' }),
      { type: 'AA', cardId: '2', opposingCardIds: ['11'] },
      { type: 'SS', cardId: '1', opposingCardIds: ['10'] },
    );
    const table = phone(model);
    expect(table.pairs.map((pair) => [pair.kind, ids(pair.characters), ids(pair.minions)])).toEqual([
      ['skirmish', ['1'], ['10']],
      ['assignment', ['2'], ['11']],
    ]);
    expect(ids(table.above[2]?.cards ?? [])).toEqual(['12']);
    expect(ids(table.below[0]?.cards ?? [])).toEqual([]);
  });

  it("names a spectator's rows after the player watched", () => {
    const model = applyEvents(emptyGame(), [
      { type: 'P', participantId: 'alice', allParticipantIds: ['alice', 'bob'], discardPublic: false },
    ]);
    const table = phoneTable(gameTable({ ...model, spectator: true }), { ...model, spectator: true });
    expect(table.below.map((row) => row.label)).toEqual(["alice's characters", "alice's support"]);
  });
});

describe('phoneFolding', () => {
  // Alice's Free Peoples turn: her Shadow condition (3) and bob's Free Peoples cards (2, 4) are not active
  const turn = play(
    put('1'),
    put('2', { participantId: 'bob' }),
    put('3', { zone: 'SUPPORT', blueprintId: SHADOW }),
    put('4', { zone: 'SUPPORT', participantId: 'bob' }),
    put('5', { zone: 'SHADOW_CHARACTERS', participantId: 'bob', blueprintId: SHADOW }),
    { type: 'TC', participantId: 'alice', inactiveCardIds: ['2', '3', '4'] },
  );
  const table = phone(turn);
  const row = (key: string) => [...table.above, ...table.below].find((r) => r.key === key)!;

  it("shrinks the player's own cards, hides the opponent's, never folds the minions", () => {
    expect([...table.above, ...table.below].map((r) => [r.key, r.fold])).toEqual([
      ['support-opponent', 'hide'],
      ['characters-opponent', 'hide'],
      ['shadow', null],
      ['characters-player', 'shrink'],
      ['support-player', 'shrink'],
    ]);
  });

  it('folds only the cards not active this turn, until the player opens them', () => {
    const own = row('support-player');
    expect(phoneFolding(own.cards, own.fold, false, {})).toEqual({
      how: 'shrink',
      ids: new Set(['3']),
      folded: true,
      offered: false,
    });
    expect(phoneFolding(own.cards, own.fold, true, {})?.folded).toBe(false);
    const theirs = row('characters-opponent');
    expect(phoneFolding(theirs.cards, theirs.fold, false, {})?.how).toBe('hide');
  });

  it('has nothing to fold in a row of active cards, or one that never folds', () => {
    const own = row('characters-player');
    expect(phoneFolding(own.cards, own.fold, false, {})).toBeNull();
    const minions = row('shadow');
    expect(phoneFolding(minions.cards, minions.fold, false, {})).toBeNull();
  });

  it('shows the folded cards while the waiting decision offers one of them', () => {
    const theirs = row('support-opponent');
    expect(phoneFolding(theirs.cards, theirs.fold, false, { '4': 'selectable' })).toMatchObject({
      folded: false,
      offered: true,
    });
    expect(phoneFolding(theirs.cards, theirs.fold, false, { '4': 'actionable' })?.folded).toBe(false);
  });
});
