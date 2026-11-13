/**
 * Reads the game server's XML (`<gameState>` and `<update>` of /game, `<gameReplay>` of /replay) into typed events.
 * The attribute names are the server's (EventSerializer); an attribute the server leaves out reads as null.
 */
import { attr, childElements } from '../../api/xml';
import { CardRef, Clocks, Decision, DecisionType, GameEvent, GameStats, GameUpdate, PlayerZones } from './game-events';

const DECISION_TYPES: readonly DecisionType[] = [
  'INTEGER',
  'MULTIPLE_CHOICE',
  'ARBITRARY_CARDS',
  'CARD_ACTION_CHOICE',
  'ACTION_CHOICE',
  'CARD_SELECTION',
  'ASSIGN_MINIONS',
];

/** A comma-separated list; an absent or empty attribute is an empty list. */
function ids(element: Element, name: string): string[] {
  const value = attr(element, name);
  return value ? value.split(',') : [];
}

function number(element: Element, name: string): number | null {
  const value = attr(element, name);
  if (value === null || value === '') return null;
  const parsed = Number(value);
  return Number.isNaN(parsed) ? null : parsed;
}

function text(element: Element, name: string): string {
  return attr(element, name) ?? '';
}

function cardRef(element: Element): CardRef {
  const targetType = attr(element, 'targetType');
  return {
    cardId: text(element, 'cardId'),
    blueprintId: text(element, 'blueprintId'),
    participantId: attr(element, 'participantId'),
    controllerId: attr(element, 'controllerId'),
    zone: attr(element, 'zone'),
    hindered: attr(element, 'hindered') === 'true',
    index: number(element, 'index'),
    targetCardId: attr(element, 'targetCardId'),
    targetType: targetType === 'attached' || targetType === 'stacked' ? targetType : null,
    testingText: attr(element, 'testingText'),
    backSideTestingText: attr(element, 'backSideTestingText'),
  };
}

function decision(element: Element): Decision | null {
  const type = attr(element, 'decisionType') as DecisionType | null;
  if (type === null || !DECISION_TYPES.includes(type)) return null;
  const params: Record<string, string[]> = {};
  for (const parameter of childElements(element, 'parameter')) {
    (params[text(parameter, 'name')] ??= []).push(text(parameter, 'value'));
  }
  return {
    id: text(element, 'id'),
    participantId: text(element, 'participantId'),
    type,
    text: text(element, 'text'),
    params,
  };
}

function zoneCount(element: Element, name: string): number | null {
  return number(element, name);
}

function gameStats(element: Element): GameStats {
  const charStats: Record<string, string[]> = {};
  for (const entry of ids(element, 'charStats')) {
    const [cardId = '', values = ''] = entry.split('=');
    charStats[cardId] = values.split('|');
  }
  const playerZones: Record<string, PlayerZones> = {};
  for (const zones of childElements(element, 'playerZones')) {
    playerZones[text(zones, 'name')] = {
      hand: zoneCount(zones, 'HAND'),
      discard: zoneCount(zones, 'DISCARD'),
      adventureDeck: zoneCount(zones, 'ADVENTURE_DECK'),
      dead: zoneCount(zones, 'DEAD'),
      deck: zoneCount(zones, 'DECK'),
      removed: zoneCount(zones, 'REMOVED'),
      handRevealed: attr(zones, 'handRevealed') === 'true',
    };
  }
  // The threats and their limits come as two lists in the same order (the legacy page pairs them by place)
  const totals = childElements(element, 'threatTotals');
  const threats: Record<string, { value: string; total: string }> = {};
  childElements(element, 'threats').forEach((threat, i) => {
    const total = totals[i];
    threats[text(threat, 'name')] = { value: text(threat, 'value'), total: total ? text(total, 'value') : '-1' };
  });
  return {
    charStats,
    fellowshipArchery: attr(element, 'fellowshipArchery'),
    shadowArchery: attr(element, 'shadowArchery'),
    moveCount: attr(element, 'moveCount'),
    moveLimit: attr(element, 'moveLimit'),
    initiative: attr(element, 'initiative'),
    ruleOf4: attr(element, 'ruleof4'),
    fellowshipStrength: attr(element, 'fellowshipStrength'),
    shadowStrength: attr(element, 'shadowStrength'),
    fellowshipDamageBonus: attr(element, 'fellowshipDamageBonus'),
    shadowDamageBonus: attr(element, 'shadowDamageBonus'),
    fpOverwhelmed: attr(element, 'fpOverwhelmed'),
    wearingRing: attr(element, 'wearingRing') === 'true',
    playerZones,
    threats,
  };
}

/** One `<ge>` element as an event; null for a type the page doesn't know (the legacy page ignored those too). */
export function parseGameEvent(element: Element): GameEvent | null {
  switch (attr(element, 'type')) {
    case 'P':
      return {
        type: 'P',
        participantId: text(element, 'participantId'),
        allParticipantIds: ids(element, 'allParticipantIds'),
        discardPublic: attr(element, 'discardPublic') === 'true',
      };
    case 'PGS':
      return {
        type: 'PGS',
        participantId: attr(element, 'participantId'),
        allParticipantIds: ids(element, 'allParticipantIds'),
        summary: text(element, 'summary'),
        notes: text(element, 'notes'),
        maps: text(element, 'maps'),
        metaSites: text(element, 'metaSites'),
      };
    case 'GPC':
      return { type: 'GPC', phase: text(element, 'phase') };
    case 'TP':
      return { type: 'TP', count: number(element, 'count') ?? 0 };
    case 'TC':
      return {
        type: 'TC',
        participantId: text(element, 'participantId'),
        inactiveCardIds: ids(element, 'otherCardIds'),
      };
    case 'PP':
      return { type: 'PP', participantId: text(element, 'participantId'), index: number(element, 'index') ?? 0 };
    case 'PCIP':
      return { type: 'PCIP', ...cardRef(element) };
    case 'MCIP':
      return { type: 'MCIP', ...cardRef(element) };
    case 'FCIP':
      return { type: 'FCIP', cardIds: ids(element, 'otherCardIds'), hindered: attr(element, 'hindered') === 'true' };
    case 'RCFP':
      return { type: 'RCFP', cardIds: ids(element, 'otherCardIds'), participantId: attr(element, 'participantId') };
    case 'AA':
      return { type: 'AA', cardId: text(element, 'cardId'), opposingCardIds: ids(element, 'otherCardIds') };
    case 'RA':
      return { type: 'RA', cardId: text(element, 'cardId'), opposingCardIds: ids(element, 'otherCardIds') };
    case 'SS':
      return { type: 'SS', cardId: attr(element, 'cardId'), opposingCardIds: ids(element, 'otherCardIds') };
    case 'ATS':
      return { type: 'ATS', cardId: text(element, 'cardId') };
    case 'RFS':
      return { type: 'RFS', cardId: text(element, 'cardId') };
    case 'ES':
      return { type: 'ES' };
    case 'AT':
      return {
        type: 'AT',
        cardId: text(element, 'cardId'),
        token: text(element, 'token'),
        count: number(element, 'count') ?? 0,
      };
    case 'RT':
      return {
        type: 'RT',
        cardId: text(element, 'cardId'),
        token: text(element, 'token'),
        count: number(element, 'count') ?? 0,
      };
    case 'M':
      return { type: 'M', message: text(element, 'message') };
    case 'W':
      return { type: 'W', message: text(element, 'message') };
    case 'GS':
      return { type: 'GS', stats: gameStats(element) };
    case 'EG':
      return { type: 'EG' };
    case 'CAC':
      return { type: 'CAC', ...cardRef(element), affectedCardIds: ids(element, 'otherCardIds') };
    case 'EP':
      return { type: 'EP', ...cardRef(element) };
    case 'CA':
      return { type: 'CA', ...cardRef(element) };
    case 'D': {
      const parsed = decision(element);
      return parsed ? { type: 'D', decision: parsed } : null;
    }
    default:
      return null;
  }
}

function clocks(root: Element): Clocks {
  const result: Record<string, number> = {};
  for (const group of childElements(root, 'clocks')) {
    for (const clock of childElements(group, 'clock')) {
      result[text(clock, 'participantId')] = Number(clock.textContent);
    }
  }
  return result;
}

/** A `<gameState>` (the whole game, on opening it) or `<update>` (since the last answer) document; null otherwise. */
export function parseGameUpdate(doc: Document): GameUpdate | null {
  const root = doc.documentElement;
  if (root.tagName !== 'gameState' && root.tagName !== 'update') return null;
  return {
    channel: attr(root, 'cn'),
    events: childElements(root, 'ge')
      .map(parseGameEvent)
      .filter((event): event is GameEvent => event !== null),
    clocks: clocks(root),
  };
}

/** A `<gameReplay>` document: every event of the recorded game, in order; null otherwise. */
export function parseReplay(doc: Document): readonly GameEvent[] | null {
  const root = doc.documentElement;
  if (root.tagName !== 'gameReplay') return null;
  return childElements(root, 'ge')
    .map(parseGameEvent)
    .filter((event): event is GameEvent => event !== null);
}
