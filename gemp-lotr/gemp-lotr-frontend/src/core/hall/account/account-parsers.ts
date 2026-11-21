import { attr, childElements, firstChild, rootElement } from '../../api/xml';
import {
  AuthoringDraft,
  GameHistoryEntry,
  GameHistoryFilterOptions,
  GameHistoryPage,
  MyScenariosAnswer,
  OwnScenario,
  PlayerStatEntry,
  PlayerStats,
  PublicScenario,
  ScenarioCopy,
  ShelfCampaign,
  ShelfOrigin,
  ShelfScenario,
} from './account-models';
import { firstText } from './first-text';

/**
 * Reading the My Account calls' answers (GameHistoryRequestHandler, PlayerStatsRequestHandler, ScenarioRequestHandler
 * with ScenarioShelf). JSON fields of the wrong type count as absent; an XML answer with another root is null, as the
 * legacy page drew nothing for it.
 */

// ---- JSON helpers ----

type Json = Record<string, unknown>;

function object(value: unknown): Json {
  return value !== null && typeof value === 'object' && !Array.isArray(value) ? (value as Json) : {};
}

function objectOrNull(value: unknown): Json | null {
  return value !== null && typeof value === 'object' && !Array.isArray(value) ? (value as Json) : null;
}

function array(value: unknown): unknown[] {
  return Array.isArray(value) ? value : [];
}

function text(value: unknown): string | null {
  return typeof value === 'string' ? value : null;
}

function texts(value: unknown): string[] {
  return array(value).filter((item): item is string => typeof item === 'string');
}

function number(value: unknown): number | null {
  return typeof value === 'number' && Number.isFinite(value) ? value : null;
}

// ---- Game History ----

function msAttr(element: Element, name: string): number | null {
  const value = attr(element, name);
  if (value === null) {
    return null;
  }
  const parsed = parseInt(value, 10);
  return Number.isNaN(parsed) ? null : parsed;
}

/** GameHistoryUI.entryToRow: the server's result, opponent and times, with the legacy fall-backs. */
function historyEntry(entry: Element, playerId: string): GameHistoryEntry {
  const winner = attr(entry, 'winner');
  const sent = attr(entry, 'result');
  const result: 'W' | 'L' = sent ? (sent === 'W' ? 'W' : 'L') : winner === playerId ? 'W' : 'L';
  const opponent = firstText(attr(entry, 'opponent'), result === 'W' ? attr(entry, 'loser') : winner) || null;
  const startMs = msAttr(entry, 'startMs');
  const endMs = msAttr(entry, 'endMs');
  return {
    result,
    winReason: attr(entry, 'winReason'),
    loseReason: attr(entry, 'loseReason'),
    opponent,
    deck: attr(entry, 'deckName'),
    format: attr(entry, 'formatName'),
    event: attr(entry, 'tournament'),
    startMs,
    endMs,
    endTime: attr(entry, 'endTime'),
    durationMs: startMs !== null && endMs !== null ? endMs - startMs : null,
    recordingId: attr(entry, 'gameRecordingId'),
  };
}

/** GET /gameHistory: `<gameHistory count total start pageSize playerId>` of `<historyEntry>`s; null for another root. */
export function parseGameHistory(doc: Document): GameHistoryPage | null {
  const root = rootElement(doc, 'gameHistory');
  if (!root) {
    return null;
  }
  const playerId = attr(root, 'playerId') ?? '';
  const matching = parseInt(attr(root, 'count') ?? '', 10) || 0;
  const total = attr(root, 'total') !== null ? parseInt(attr(root, 'total') ?? '', 10) || 0 : matching;
  const start = attr(root, 'start') !== null ? parseInt(attr(root, 'start') ?? '', 10) || 0 : null;
  return {
    playerId,
    matching,
    total,
    start,
    entries: childElements(root, 'historyEntry').map((entry) => historyEntry(entry, playerId)),
  };
}

/** GET /gameHistory/filters: {formats, events}. */
export function parseGameHistoryFilters(json: unknown): GameHistoryFilterOptions {
  const answer = object(json);
  return { formats: texts(answer['formats']), events: texts(answer['events']) };
}

// ---- Your Stats ----

function statEntries(group: Element | null): PlayerStatEntry[] {
  if (!group) {
    return [];
  }
  return childElements(group, 'entry').map((entry) => ({
    format: attr(entry, 'format') ?? '',
    deckName: attr(entry, 'deckName') ?? '',
    wins: attr(entry, 'wins') ?? '',
    losses: attr(entry, 'losses') ?? '',
    percentage: attr(entry, 'perc') ?? '',
  }));
}

/** GET /playerStats: `<playerStats><casual><entry .../>*</casual><competitive>...</competitive></playerStats>`. */
export function parsePlayerStats(doc: Document): PlayerStats | null {
  const root = rootElement(doc, 'playerStats');
  if (!root) {
    return null;
  }
  return { casual: statEntries(firstChild(root, 'casual')), competitive: statEntries(firstChild(root, 'competitive')) };
}

// ---- My Scenarios ----

function campaign(value: unknown): ShelfCampaign | null {
  const json = objectOrNull(value);
  if (!json) {
    return null;
  }
  return { id: text(json['id']), title: text(json['title']), position: number(json['position']) ?? 0 };
}

function origin(value: unknown): ShelfOrigin | null {
  const json = objectOrNull(value);
  const id = json ? text(json['id']) : null;
  if (!json || id === null) {
    return null;
  }
  return { id, title: text(json['title']), exists: json['exists'] === true };
}

function shelfScenario(json: Json): ShelfScenario {
  return {
    path: text(json['path']) ?? '',
    id: text(json['id']),
    fullId: text(json['fullId']),
    title: text(json['title']),
    kind: text(json['kind']),
    summary: text(json['summary']),
    campaign: campaign(json['campaign']),
    playable: json['playable'] === true,
    verified: json['verified'] === true,
    openable: json['openable'] === true,
    errors: texts(json['errors']),
  };
}

function ownScenario(value: unknown): OwnScenario {
  const json = object(value);
  const published = json['published'];
  return {
    ...shelfScenario(json),
    status: text(json['status']),
    submittedAt: number(json['submittedAt']),
    feedback: text(json['feedback']),
    reviewedAt: number(json['reviewedAt']),
    reviewedBy: text(json['reviewedBy']),
    locked: json['locked'] === true,
    origin: origin(json['origin']),
    published: published === 'current' || published === 'changed' ? published : null,
  };
}

function publicScenario(value: unknown): PublicScenario {
  const json = object(value);
  return { ...shelfScenario(json), author: text(json['author']), disabled: json['disabled'] === true };
}

/** The `authoring` of an answer (ScenarioAuthoring.current), or null when none is open. */
export function parseAuthoringDraft(value: unknown): AuthoringDraft | null {
  const json = objectOrNull(value);
  const gameId = json?.['gameId'];
  if (!json || (typeof gameId !== 'string' && typeof gameId !== 'number')) {
    return null;
  }
  return {
    gameId: String(gameId),
    id: text(json['id']),
    title: text(json['title']),
    over: json['over'] === true,
    test: json['test'] === true,
    dirty: json['dirty'] === true,
  };
}

/** GET /scenarios/mine: {scenarios, limit, ownFolder, authoring}; `ownFolder` counts as true unless it is false. */
export function parseMyScenarios(json: unknown): MyScenariosAnswer {
  const answer = object(json);
  return {
    scenarios: array(answer['scenarios']).map(ownScenario),
    limit: number(answer['limit']) ?? 0,
    ownFolder: answer['ownFolder'] !== false,
    authoring: parseAuthoringDraft(answer['authoring']),
  };
}

/** GET /scenarios/public: {scenarios}. */
export function parsePublicScenarios(json: unknown): PublicScenario[] {
  return array(object(json)['scenarios']).map(publicScenario);
}

/** POST mine/copy, mine/copyPublic: {ok, id, path}. */
export function parseScenarioCopy(json: unknown): ScenarioCopy {
  const answer = object(json);
  return { id: text(answer['id']), path: text(answer['path']) };
}
