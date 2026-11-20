import { CardRef, Decision, GameEvent, GameStats } from '../protocol/game-events';
import { ScenarioInfo } from '../scenario/scenario-events';
import { applyEvent, applyEvents } from './apply-event';
import { emptyGame, NO_SKIRMISH_NUMBERS, GameModel } from './game-model';

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
  shadowOverwhelmed: null,
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

  it('changes nothing for the game being taken back: the table was sent again before it', () => {
    const before = play({ type: 'TP', count: 3 });
    expect(applyEvent(before, { type: 'GR', participantId: 'bob', tier: 'RED', message: 'm' })).toEqual({
      ...before,
      eventCount: before.eventCount + 1,
    });
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
    const other = applyEvent(model, { type: 'TC', participantId: 'alice', inactiveCardIds: ['1'] });
    expect([other.cards['1']?.inactive, other.cards['2']?.inactive]).toEqual([true, false]);
    // None named: the cards stay as they were, as on the legacy page
    const none = applyEvent(model, { type: 'TC', participantId: 'alice', inactiveCardIds: [] });
    expect(none.currentPlayer).toBe('alice');
    expect([none.cards['1']?.inactive, none.cards['2']?.inactive]).toEqual([false, true]);
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
    expect(model.skirmish).toEqual({ characterId: '1', minionIds: ['7'], shown: NO_SKIRMISH_NUMBERS });
    model = applyEvent(model, { type: 'ATS', cardId: '8' });
    expect(model.cards['8']?.inSkirmish).toBe(true);
    model = applyEvent(model, { type: 'RFS', cardId: '7' });
    expect(model.cards['7']?.inSkirmish).toBe(false);
    model = applyEvent(model, { type: 'ES' });
    expect(model.skirmish).toBeNull();
    expect(Object.values(model.cards).some((c) => c.inSkirmish)).toBe(false);
  });

  it("shows in the skirmish's boxes only the numbers of stats sent since it started", () => {
    const before = stats({ fellowshipStrength: '6', shadowStrength: '6', fellowshipDamageBonus: '1' });
    let model = play(put('1'), put('7'), { type: 'GS', stats: before });
    model = applyEvent(model, { type: 'SS', cardId: '1', opposingCardIds: ['7'] });
    expect(model.skirmish?.shown).toEqual(NO_SKIRMISH_NUMBERS);
    model = applyEvent(model, {
      type: 'GS',
      stats: stats({
        fellowshipStrength: '8',
        shadowStrength: '5',
        fellowshipDamageBonus: '2',
        fpOverwhelmed: 'true',
        shadowOverwhelmed: 'false',
      }),
    });
    // What the next stats leave out stays as it was; the strengths are always as sent
    model = applyEvent(model, { type: 'GS', stats: stats({ fellowshipStrength: '9', shadowDamageBonus: '0' }) });
    expect(model.skirmish?.shown).toEqual({
      fellowshipStrength: '9',
      shadowStrength: null,
      fellowshipDamageBonus: '2',
      shadowDamageBonus: '0',
      fpOverwhelmed: 'true',
      // Kept as it was when the stats leave it out, as fpOverwhelmed (gameAnimations.js, the new master)
      shadowOverwhelmed: 'false',
    });
    model = applyEvent(model, { type: 'GS', stats: stats({ shadowOverwhelmed: 'true' }) });
    expect(model.skirmish?.shown.shadowOverwhelmed).toBe('true');
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

  it("keeps a scripted game's own state, and its messages with the scripted opponent under its shown name", () => {
    const info: ScenarioInfo = {
      type: 'info',
      id: 'basics-01',
      kind: 'tutorial',
      title: 'The Basics',
      humanSeat: 'freeps',
      opponent: '~Scenario',
      opponentName: 'Basic Shadow',
      settings: { autoAccept: null, autoDismissDeckReveal: null, animationSpeed: null },
      bubbleGap: null,
      resumedAt: null,
      campaign: null,
      author: false,
      draftTest: false,
      twoSided: false,
      opening: 'board',
    };
    expect(play({ type: 'M', message: '~Scenario passes' }).scenario).toBeNull();
    const model = play(
      { type: 'S', scenario: info },
      // (one the page can't read changes nothing)
      { type: 'S', scenario: null },
      { type: 'M', message: '~Scenario plays an orc' },
      { type: 'W', message: '~Scenario is waiting' },
      // (a decision's text can name the opponent too)
      {
        type: 'D',
        decision: { id: '1', participantId: 'asdf', type: 'ARBITRARY_CARDS', text: 'Hand of ~Scenario', params: {} },
      },
    );
    expect(model.scenario).toEqual({
      info,
      step: null,
      shown: 0,
      notice: null,
      end: null,
      lastCheckpoint: null,
      role: null,
    });
    expect(model.eventCount).toBe(5);
    expect(model.decision?.decision.text).toBe('Hand of Basic Shadow');
    expect(model.messages).toEqual([
      { kind: 'message', text: 'Basic Shadow plays an orc' },
      { kind: 'warning', text: 'Basic Shadow is waiting' },
    ]);
    // A game that played on before the page opened it: its log comes before the scenario says who it is
    const late = play(
      { type: 'M', message: '~Scenario plays a goblin' },
      { type: 'S', scenario: info },
      { type: 'S', scenario: { ...info, opponentName: 'Someone else' } },
    );
    expect(late.messages).toEqual([{ kind: 'message', text: 'Basic Shadow plays a goblin' }]);
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
    model = { ...model, local: { assignments: {}, selectedCardIds: ['1'], markedCardIds: [], minion: null } };
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

  it("hides a card's numbers from when it is turned face down until stats name it face up", () => {
    let model = play(put('1'), put('2'), { type: 'FCIP', cardIds: ['1'], hindered: true });
    expect([model.cards['1']?.numbersHidden, model.cards['2']?.numbersHidden]).toEqual([true, false]);
    // Stats while it lies face down, then turned up: still hidden
    model = applyEvent(model, { type: 'GS', stats: stats({ charStats: { '1': ['7', '3'] } }) });
    model = applyEvent(model, { type: 'FCIP', cardIds: ['1'], hindered: false });
    expect(model.cards['1']?.numbersHidden).toBe(true);
    // Stats naming another card don't show them
    model = applyEvent(model, { type: 'GS', stats: stats({ charStats: { '2': ['5', '2'] } }) });
    expect(model.cards['1']?.numbersHidden).toBe(true);
    model = applyEvent(model, { type: 'GS', stats: stats({ charStats: { '1': ['7', '3'] } }) });
    expect(model.cards['1']?.numbersHidden).toBe(false);
  });

  it('remembers that the alert box asked something, through the decisions after', () => {
    let model = play({ type: 'D', decision: decision('CARD_SELECTION') });
    expect(model.alertAsked).toBe(true);
    model = applyEvent(model, { type: 'D', decision: decision('INTEGER') });
    expect(model.alertAsked).toBe(true);
    expect(play({ type: 'D', decision: decision('INTEGER') }).alertAsked).toBe(false);
  });

  it("takes the revealed hands from stats naming one player's zones too", () => {
    const zones = { hand: 1, discard: 0, adventureDeck: 0, dead: 0, deck: 0, removed: 0 };
    const model = play({
      type: 'GS',
      stats: stats({ playerZones: { alice: { ...zones, handRevealed: true } } }),
    });
    expect(model.revealedHands).toEqual(['alice']);
  });

  it("a split skirmish's see-through copy is face up, active, out of the skirmish, first under nothing", () => {
    const model = play(put('1'), put('7'), { type: 'AA', cardId: '-1', opposingCardIds: ['7'] });
    expect(model.cards['extra-1']).toMatchObject({
      hindered: false,
      inactive: false,
      inSkirmish: false,
      attachedAt: 0,
      zone: 'EXTRA',
    });
    // A new card isn't inactive unless the turn named it
    expect(model.cards['1']?.inactive).toBe(false);
  });

  it('keeps the numbers of a character the server leaves out, until the card leaves play', () => {
    let model = play(put('1'), put('2'), {
      type: 'GS',
      stats: stats({ charStats: { '1': ['7', '3'], '2': ['5', '2'] } }),
    });
    model = applyEvent(model, { type: 'GS', stats: stats({ charStats: { '2': ['6', '2'] } }) });
    expect(model.stats?.charStats).toEqual({ '1': ['7', '3'], '2': ['6', '2'] });
    model = applyEvent(model, { type: 'RCFP', cardIds: ['1'], participantId: 'alice' });
    expect(model.stats?.charStats).toEqual({ '2': ['6', '2'] });
  });

  it('remembers the last Rule of 4 count sent while the rule applied', () => {
    let model = play({ type: 'GS', stats: stats({ ruleOf4: '-1' }) });
    expect(model.lastRuleOf4).toBeNull();
    model = applyEvent(model, { type: 'GS', stats: stats({ ruleOf4: '2' }) });
    model = applyEvent(model, { type: 'GS', stats: stats({ ruleOf4: '-1' }) });
    model = applyEvent(model, { type: 'GS', stats: stats({ ruleOf4: null }) });
    expect(model.lastRuleOf4).toBe('2');
  });

  it('notes when a card was attached: again when it is moved', () => {
    const model = play(put('1'), put('2', { targetCardId: '1', targetType: 'attached' }), {
      type: 'MCIP',
      ...card('2', { targetCardId: '1', targetType: 'stacked' }),
    });
    expect(model.cards['2']).toMatchObject({ attachedAt: 2, stackedOn: '1', attachedTo: null });
    expect(model.cards['1']?.attachedAt).toBe(0);
  });

  it('keeps the players of the pre-game information', () => {
    const model = play({
      type: 'PGS',
      participantId: 'bob',
      allParticipantIds: ['alice', 'bob'],
      summary: 's',
      notes: '',
      maps: '',
      metaSites: '',
    });
    expect(model.preGame).toMatchObject({ participantId: 'bob', players: ['alice', 'bob'] });
  });

  it('marks a card put into play inactive when the turn named it so before (as the full state does)', () => {
    const model = play({ type: 'TC', participantId: 'alice', inactiveCardIds: ['1'] }, put('1'), put('2'));
    expect(model.cards['1']?.inactive).toBe(true);
    expect(model.cards['2']?.inactive).toBe(false);
  });

  it('notes the kind of decision the end of the game closed', () => {
    const model = play({ type: 'D', decision: decision('CARD_SELECTION') }, { type: 'EG' });
    expect(model.closedByEnd).toBe('CARD_SELECTION');
    expect(play({ type: 'EG' }).closedByEnd).toBeNull();
  });
});
