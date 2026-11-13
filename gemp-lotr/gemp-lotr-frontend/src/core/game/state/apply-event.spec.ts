import { CardRef, Decision, GameEvent, GameStats } from '../protocol/game-events';
import { applyEvent, applyEvents } from './apply-event';
import { emptyGame, GameModel } from './game-model';

const card = (cardId: string, extra: Partial<CardRef> = {}): CardRef => ({
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
const put = (cardId: string, extra: Partial<CardRef> = {}): GameEvent => ({ type: 'PCIP', ...card(cardId, extra) });
const decision = (type: Decision['type'] = 'CARD_SELECTION'): Decision => ({
  id: '1',
  participantId: 'alice',
  type,
  text: 'Choose',
  params: {},
});
const stats = (extra: Partial<GameStats> = {}): GameStats => ({
  charStats: {},
  fellowshipArchery: null,
  shadowArchery: null,
  moveCount: null,
  moveLimit: null,
  initiative: null,
  ruleOf4: null,
  fellowshipStrength: null,
  shadowStrength: null,
  fellowshipDamageBonus: null,
  shadowDamageBonus: null,
  fpOverwhelmed: null,
  wearingRing: false,
  playerZones: {},
  threats: {},
  ...extra,
});
const play = (...events: GameEvent[]): GameModel => applyEvents(emptyGame(), events);

describe('applyEvent', () => {
  it("starts the game for a player, or for a spectator from the second player's side", () => {
    expect(
      play({ type: 'P', participantId: 'alice', allParticipantIds: ['alice', 'bob'], discardPublic: true }),
    ).toMatchObject({
      participantId: 'alice',
      spectator: false,
      players: ['alice', 'bob'],
      discardPublic: true,
      started: true,
    });
    expect(
      play({ type: 'P', participantId: 'carol', allParticipantIds: ['alice', 'bob'], discardPublic: false }),
    ).toMatchObject({
      participantId: 'bob',
      spectator: true,
    });
  });

  it('counts the events it applied', () => {
    expect(play({ type: 'ES' }, { type: 'ES' }).eventCount).toBe(2);
  });

  it('keeps the pre-game information, the phase, the twilight, the turn and positions', () => {
    const model = play(
      { type: 'PGS', participantId: null, allParticipantIds: [], summary: 's', notes: 'n', maps: 'm', metaSites: 'x' },
      { type: 'GPC', phase: 'Shadow' },
      { type: 'TP', count: 4 },
      { type: 'PP', participantId: 'alice', index: 3 },
    );
    expect(model).toMatchObject({
      preGame: { summary: 's', notes: 'n', maps: 'm', metaSites: 'x' },
      phase: 'Shadow',
      twilight: 4,
      positions: { alice: 3 },
    });
  });

  it('puts a card into play under its controller, attached or stacked, and a site with its number', () => {
    const model = play(
      put('1'),
      put('2', { controllerId: 'bob', zone: 'ATTACHED', targetCardId: '1', targetType: 'attached' }),
      put('3', { zone: 'STACKED', targetCardId: '1', targetType: 'stacked' }),
      put('4', { zone: 'ADVENTURE_PATH', index: 2 }),
    );
    expect(model.cardOrder).toEqual(['1', '2', '3', '4']);
    expect(model.cards['2']).toMatchObject({ owner: 'bob', attachedTo: '1', stackedOn: null });
    expect(model.cards['3']).toMatchObject({ attachedTo: null, stackedOn: '1' });
    expect(model.cards['4']?.index).toBe(2);
    expect(model.cards['1']?.index).toBeNull();
  });

  it('a card put into play again keeps its place', () => {
    expect(play(put('1'), put('2'), put('1', { zone: 'HAND' })).cardOrder).toEqual(['1', '2']);
  });

  it('moves a card, and ignores moving one it does not know', () => {
    const model = play(
      put('1'),
      { type: 'MCIP', ...card('1', { zone: 'SUPPORT', controllerId: 'bob' }) },
      {
        type: 'MCIP',
        ...card('9'),
      },
    );
    expect(model.cards['1']).toMatchObject({ zone: 'SUPPORT', owner: 'bob' });
    expect(model.cards['9']).toBeUndefined();
  });

  it('hinders and restores cards, and removes them', () => {
    let model = play(put('1'), put('2'), { type: 'FCIP', cardIds: ['1', '9'], hindered: true });
    expect(model.cards['1']?.hindered).toBe(true);
    model = applyEvent(model, { type: 'RCFP', cardIds: ['2'], participantId: null });
    expect(Object.keys(model.cards)).toEqual(['1']);
    expect(model.cardOrder).toEqual(['1']);
  });

  it('marks the cards inactive this turn', () => {
    const model = play(put('1'), put('2'), { type: 'TC', participantId: 'bob', inactiveCardIds: ['2'] });
    expect(model.currentPlayer).toBe('bob');
    expect([model.cards['1']?.inactive, model.cards['2']?.inactive]).toEqual([false, true]);
    const again = applyEvent(model, { type: 'TC', participantId: 'alice', inactiveCardIds: [] });
    expect(again.cards['2']?.inactive).toBe(false);
  });

  it('assigns minions, moving one assigned elsewhere, and takes them off', () => {
    let model = play(put('1'), put('2'), put('7'), { type: 'AA', cardId: '1', opposingCardIds: ['7'] });
    expect(model.cards['7']?.assignedTo).toBe('1');
    model = applyEvent(model, { type: 'AA', cardId: '2', opposingCardIds: ['7'] });
    expect(model.cards['7']?.assignedTo).toBe('2');
    model = applyEvent(model, { type: 'RA', cardId: '2', opposingCardIds: [] });
    expect(model.cards['7']?.assignedTo).toBeNull();
  });

  it('splits a skirmish into waiting ones: the minions go to a see-through copy of the character', () => {
    let model = play(put('1', { blueprintId: '1_12' }), put('7'), put('8'), {
      type: 'AA',
      cardId: '-1',
      opposingCardIds: ['7', '8'],
    });
    expect(model.cards['extra-1']).toMatchObject({ blueprintId: '1_12', zone: 'EXTRA', owner: null });
    expect(model.cards['8']?.assignedTo).toBe('-1');
    // The copy is made once
    model = applyEvent(model, { type: 'AA', cardId: '-1', opposingCardIds: ['7'] });
    expect(model.cardOrder.filter((id) => id === 'extra-1')).toHaveLength(1);
    model = applyEvent(model, { type: 'RA', cardId: '-1', opposingCardIds: ['7'] });
    expect([model.cards['7']?.assignedTo, model.cards['8']?.assignedTo]).toEqual([null, '-1']);
  });

  it('a split skirmish of an unknown character makes no copy', () => {
    expect(play({ type: 'AA', cardId: '-5', opposingCardIds: [] }).cards['extra-5']).toBeUndefined();
  });

  it('follows a skirmish: started, a card added and removed, ended', () => {
    let model = play(put('1'), put('7'), put('8'), { type: 'SS', cardId: '1', opposingCardIds: ['7'] });
    expect(model.skirmish).toEqual({ characterId: '1', minionIds: ['7'] });
    model = applyEvent(model, { type: 'ATS', cardId: '8' });
    expect(model.cards['8']?.inSkirmish).toBe(true);
    model = applyEvent(model, { type: 'RFS', cardId: '7' });
    expect(model.cards['7']?.inSkirmish).toBe(false);
    model = applyEvent(model, { type: 'ES' });
    expect(model.skirmish).toBeNull();
    expect(Object.values(model.cards).some((c) => c.inSkirmish)).toBe(false);
  });

  it('a skirmish without a character marks only the minions', () => {
    const model = play(put('7'), { type: 'SS', cardId: null, opposingCardIds: ['7'] });
    expect(model.cards['7']?.inSkirmish).toBe(true);
  });

  it('adds and removes tokens', () => {
    const model = play(
      put('1'),
      { type: 'AT', cardId: '1', token: 'WOUND', count: 2 },
      { type: 'RT', cardId: '1', token: 'WOUND', count: 1 },
      { type: 'RT', cardId: '1', token: 'BURDEN', count: 1 },
    );
    expect(model.cards['1']?.tokens).toEqual({ WOUND: 1, BURDEN: -1 });
  });

  it('keeps the messages and warnings', () => {
    expect(play({ type: 'M', message: 'a' }, { type: 'W', message: 'b' }).messages).toEqual([
      { kind: 'message', text: 'a' },
      { kind: 'warning', text: 'b' },
    ]);
  });

  it('keeps game stats; the revealed hands only when the stats name the players', () => {
    let model = play({
      type: 'GS',
      stats: stats({
        playerZones: {
          alice: { hand: 1, discard: 0, adventureDeck: 0, dead: 0, deck: 0, removed: 0, handRevealed: true },
          bob: { hand: 1, discard: 0, adventureDeck: 0, dead: 0, deck: 0, removed: 0, handRevealed: false },
        },
      }),
    });
    expect(model.revealedHands).toEqual(['alice']);
    model = applyEvent(model, { type: 'GS', stats: stats() });
    expect(model.revealedHands).toEqual(['alice']);
    expect(model.stats?.playerZones).toEqual({});
  });

  it('a decision waits until the game ends; each clears what was chosen for the one before', () => {
    let model = play(put('1'), { type: 'D', decision: decision() });
    expect(model.decision).toEqual({ decision: decision(), arrivedAt: 1 });
    model = { ...model, local: { assignments: {}, selectedCardIds: ['1'] } };
    model = applyEvent(model, { type: 'D', decision: decision('INTEGER') });
    expect(model.local.selectedCardIds).toEqual([]);
    model = applyEvent(model, { type: 'EG' });
    expect(model).toMatchObject({ ended: true, decision: null });
  });

  it('changes nothing for what is only drawn: a card flashed, shown, affecting another', () => {
    const before = play(put('1'));
    for (const event of [
      { type: 'CA', ...card('1') },
      { type: 'EP', ...card('1') },
      { type: 'CAC', ...card('1'), affectedCardIds: ['2'] },
    ] as GameEvent[]) {
      const after = applyEvent(before, event);
      expect({ ...after, eventCount: 0 }).toEqual({ ...before, eventCount: 0 });
    }
  });
});
