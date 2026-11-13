import { parseXml } from '../../api/xml';
import { GameEvent } from './game-events';
import { parseGameEvent, parseGameUpdate, parseReplay } from './game-xml';

const ge = (attributes: string, children = '') =>
  parseXml(`<x><ge ${attributes}>${children}</ge></x>`).documentElement.firstElementChild as Element;
const parse = (attributes: string, children = '') => parseGameEvent(ge(attributes, children));

describe('game XML', () => {
  it('reads an update: its channel, its events in order and the clocks', () => {
    const update = parseGameUpdate(
      parseXml(
        '<update cn="7"><ge type="TP" count="3"/><ge type="GPC" phase="Shadow"/>' +
          '<clocks><clock participantId="alice">2600</clock><clock participantId="decisionClock">4</clock></clocks></update>',
      ),
    );
    expect(update).toEqual({
      channel: '7',
      events: [
        { type: 'TP', count: 3 },
        { type: 'GPC', phase: 'Shadow' },
      ],
      clocks: { alice: 2600, decisionClock: 4 },
    });
  });

  it('reads a whole game state the same way, and nothing else', () => {
    expect(parseGameUpdate(parseXml('<gameState cn="1"/>'))?.channel).toBe('1');
    expect(parseGameUpdate(parseXml('<error/>'))).toBeNull();
  });

  it('reads a replay, and nothing else', () => {
    expect(parseReplay(parseXml('<gameReplay><info/><ge type="ES"/><ge type="EG"/></gameReplay>'))).toEqual([
      { type: 'ES' },
      { type: 'EG' },
    ]);
    expect(parseReplay(parseXml('<update/>'))).toBeNull();
  });

  it('reads a card put into play, attached or stacked', () => {
    expect(
      parse(
        'type="PCIP" cardId="5" blueprintId="1_9" participantId="alice" controllerId="bob" zone="ATTACHED" targetCardId="4" targetType="attached" hindered="true"',
      ),
    ).toEqual<GameEvent>({
      type: 'PCIP',
      cardId: '5',
      blueprintId: '1_9',
      participantId: 'alice',
      controllerId: 'bob',
      zone: 'ATTACHED',
      hindered: true,
      index: null,
      targetCardId: '4',
      targetType: 'attached',
      testingText: null,
      backSideTestingText: null,
    });
    expect(parse('type="MCIP" cardId="5" targetCardId="4" targetType="stacked"')).toMatchObject({
      targetType: 'stacked',
    });
    expect(parse('type="PCIP" cardId="5" targetType="other"')).toMatchObject({ targetType: null });
    expect(parse('type="CA" cardId="5" zone="ADVENTURE_PATH" index="3"')).toMatchObject({ type: 'CA', index: 3 });
    expect(parse('type="EP" cardId="5" index="x"')).toMatchObject({ type: 'EP', index: null });
  });

  it('reads the events about cards and skirmishes', () => {
    expect(parse('type="FCIP" otherCardIds="1,2" hindered="true"')).toEqual({
      type: 'FCIP',
      cardIds: ['1', '2'],
      hindered: true,
    });
    expect(parse('type="RCFP" otherCardIds="3" participantId="alice"')).toEqual({
      type: 'RCFP',
      cardIds: ['3'],
      participantId: 'alice',
    });
    expect(parse('type="AA" cardId="1" otherCardIds="7,8"')).toEqual({
      type: 'AA',
      cardId: '1',
      opposingCardIds: ['7', '8'],
    });
    expect(parse('type="RA" cardId="1"')).toEqual({ type: 'RA', cardId: '1', opposingCardIds: [] });
    expect(parse('type="SS" otherCardIds="7"')).toEqual({ type: 'SS', cardId: null, opposingCardIds: ['7'] });
    expect(parse('type="ATS" cardId="9"')).toEqual({ type: 'ATS', cardId: '9' });
    expect(parse('type="RFS" cardId="9"')).toEqual({ type: 'RFS', cardId: '9' });
    expect(parse('type="AT" cardId="9" token="WOUND" count="2"')).toEqual({
      type: 'AT',
      cardId: '9',
      token: 'WOUND',
      count: 2,
    });
    expect(parse('type="RT" cardId="9" token="WOUND"')).toEqual({ type: 'RT', cardId: '9', token: 'WOUND', count: 0 });
    expect(parse('type="CAC" cardId="1" otherCardIds="2,3"')).toMatchObject({
      type: 'CAC',
      affectedCardIds: ['2', '3'],
    });
  });

  it('reads the events about the game and the players', () => {
    expect(parse('type="P" participantId="alice" allParticipantIds="alice,bob" discardPublic="true"')).toEqual({
      type: 'P',
      participantId: 'alice',
      allParticipantIds: ['alice', 'bob'],
      discardPublic: true,
    });
    expect(parse('type="PGS" allParticipantIds="alice,bob" summary="s" notes="n"')).toEqual({
      type: 'PGS',
      participantId: null,
      allParticipantIds: ['alice', 'bob'],
      summary: 's',
      notes: 'n',
      maps: '',
      metaSites: '',
    });
    expect(parse('type="TC" participantId="bob" otherCardIds=""')).toEqual({
      type: 'TC',
      participantId: 'bob',
      inactiveCardIds: [],
    });
    expect(parse('type="PP" participantId="bob" index="2"')).toEqual({ type: 'PP', participantId: 'bob', index: 2 });
    expect(parse('type="PP" participantId="bob"')).toMatchObject({ index: 0 });
    expect(parse('type="TP"')).toEqual({ type: 'TP', count: 0 });
    expect(parse('type="M" message="Hi"')).toEqual({ type: 'M', message: 'Hi' });
    expect(parse('type="W" message="No"')).toEqual({ type: 'W', message: 'No' });
    expect(parse('type="EG"')).toEqual({ type: 'EG' });
  });

  it('reads game stats: card stats, counters and the threats with their limits paired in order', () => {
    const event = parse(
      'type="GS" charStats="5=6|3|R4,7=9|2" initiative="FREE_PEOPLE" ruleof4="1" moveCount="1" moveLimit="2" wearingRing="true"',
      '<playerZones name="alice" HAND="8" DECK="51" DISCARD="0" ADVENTURE_DECK="8" DEAD="1" REMOVED="0" handRevealed="true"/>' +
        '<playerZones name="bob" HAND="7"/>' +
        '<threats name="alice" value="0"/><threats name="bob" value="1"/><threatTotals name="alice" value="2"/>',
    );
    expect(event).toMatchObject({
      type: 'GS',
      stats: {
        charStats: { '5': ['6', '3', 'R4'], '7': ['9', '2'] },
        initiative: 'FREE_PEOPLE',
        ruleOf4: '1',
        wearingRing: true,
        playerZones: {
          alice: { hand: 8, deck: 51, discard: 0, adventureDeck: 8, dead: 1, removed: 0, handRevealed: true },
          bob: { hand: 7, deck: null, discard: null, handRevealed: false },
        },
        threats: { alice: { value: '0', total: '2' }, bob: { value: '1', total: '-1' } },
      },
    });
  });

  it('reads a decision with its parameters as lists', () => {
    expect(
      parse(
        'type="D" id="1" decisionType="CARD_SELECTION" participantId="alice" text="Choose"',
        '<parameter name="min" value="1"/><parameter name="cardId" value="4"/><parameter name="cardId" value="5"/>',
      ),
    ).toEqual({
      type: 'D',
      decision: {
        id: '1',
        participantId: 'alice',
        type: 'CARD_SELECTION',
        text: 'Choose',
        params: { min: ['1'], cardId: ['4', '5'] },
      },
    });
  });

  it('skips what it does not know: an event type, a decision type', () => {
    expect(parse('type="CM"')).toBeNull();
    expect(parse('type="D" decisionType="SOMETHING_NEW"')).toBeNull();
    expect(parse('type="D"')).toBeNull();
    expect(parseGameUpdate(parseXml('<update cn="2"><ge type="CM"/></update>'))?.events).toEqual([]);
  });
});
