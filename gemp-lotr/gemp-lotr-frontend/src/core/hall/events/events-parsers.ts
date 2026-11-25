import { parseServerMarkup, RICH_MARKUP } from '../../chat/chat-markup';
import { attr, boolAttr, childElements, intAttr, parseXml, rootElement } from '../../api/xml';
import { price } from '../price';
import {
  CalendarEvent,
  CalendarMonth,
  CalendarSerie,
  CompletedEvent,
  CompletedEventsMonth,
  EventAdminLink,
  EventKind,
  EventPrizes,
  LeagueDetail,
  LeagueMatch,
  LeagueSerie,
  LeagueStanding,
  LeagueSummary,
  RtmdPath,
  TournamentDetail,
  TournamentStanding,
  TournamentSummary,
} from './events-models';

/**
 * Reading the Events calls' answers (CalendarRequestHandler, EventHistoryRequestHandler / EventHistoryService,
 * LeagueRequestHandler, TournamentRequestHandler). JSON fields of the wrong type count as absent; an XML answer with
 * another root is null, as the legacy page drew nothing for it.
 */

// ---- JSON helpers ----

type Json = Record<string, unknown>;

function object(value: unknown): Json {
  return value !== null && typeof value === 'object' && !Array.isArray(value) ? (value as Json) : {};
}

function array(value: unknown): unknown[] {
  return Array.isArray(value) ? value : [];
}

/** A text field; numbers are taken as their text (ids may be either). */
function text(value: unknown): string | null {
  if (typeof value === 'string') {
    return value;
  }
  return typeof value === 'number' && Number.isFinite(value) ? String(value) : null;
}

/** A text, or null when it is empty or absent. */
function nonEmpty(value: string | null): string | null {
  return value === null || value === '' ? null : value;
}

function number(value: unknown): number | null {
  return typeof value === 'number' && Number.isFinite(value) ? value : null;
}

// ---- the calendar ----

/** GET /calendar. Events of an unknown kind are left out. */
export function parseCalendar(json: unknown): CalendarMonth {
  const root = object(json);
  return {
    from: text(root['from']) ?? '',
    to: text(root['to']) ?? '',
    eventAdmin: root['eventAdmin'] === true,
    events: array(root['events']).flatMap((entry) => {
      const event = parseCalendarEvent(object(entry));
      return event ? [event] : [];
    }),
  };
}

function parseCalendarEvent(json: Json): CalendarEvent | null {
  const base = {
    id: text(json['id']) ?? '',
    name: text(json['name']) ?? '',
    start: text(json['start']) ?? '',
    end: text(json['end']) ?? '',
    joined: json['joined'] === true,
  };
  const cost = number(json['cost']);
  switch (json['kind']) {
    case 'league':
      return {
        ...base,
        kind: 'league',
        leagueType: text(json['leagueType']),
        inviteOnly: json['inviteOnly'] === true,
        cost: cost === null ? null : price(cost),
        description: parseServerMarkup(text(json['description']) ?? '', RICH_MARKUP),
        scheduleId: text(json['scheduleId']),
        series: array(json['series']).map((serie) => parseCalendarSerie(object(serie))),
      };
    case 'tournament':
      return {
        ...base,
        kind: 'tournament',
        tournamentType: text(json['tournamentType']),
        format: text(json['format']),
        startTime: startTimeOf(text(json['startTime'])),
        started: json['started'] === true,
        cost: cost === null ? null : price(cost),
        queue:
          json['queueOpen'] === true
            ? {
                id: base.id,
                name: base.name,
                type: text(json['queueType']) ?? '',
                start: text(json['queueStart']) ?? '',
              }
            : null,
        prizes: parseEventPrizes(json),
      };
    case 'projected':
      return {
        ...base,
        kind: 'projected',
        scheduleId: text(json['scheduleId']),
        scheduleName: text(json['scheduleName']) ?? '',
        eventName: text(json['eventName']),
        leagueType: text(json['leagueType']),
        createdOn: text(json['createdOn']) ?? '',
        error: nonEmpty(text(json['error'])),
      };
    default:
      return null;
  }
}

function parseCalendarSerie(json: Json): CalendarSerie {
  return {
    name: text(json['name']) ?? '',
    start: text(json['start']) ?? '',
    end: text(json['end']) ?? '',
    format: nonEmpty(text(json['format'])),
    limited: json['limited'] === true,
  };
}

/** "HH:mm" out of a ZonedDateTime's text ("2026-12-05T18:00Z[UTC]"): characters 11 to 16, as the legacy page took. */
function startTimeOf(value: string | null): string | null {
  return value ? value.substring(11, 16) : null;
}

/**
 * A tournament's prizes as data: the label and the breakdown's plain-text lines, as the server sends them beside its
 * old HTML snippet (`prizesLabel` and `prizesLines`, the lines a JSON array or its text). Null when there is no label.
 * The HTML snippet (`prizes`) is never read.
 *
 * TODO(hall port): the server is getting a structured form of the prize descriptions (PrizeSummary, a label and
 * plain-text lines beside the old HTML snippet). It is sent on the hall poll's queues; no Events call carries it yet.
 * When the calendar's tournaments or GET /tournament get it, this is the one place that reads it.
 */
export function parseEventPrizes(source: Json | Element): EventPrizes | null {
  const read = (name: string): unknown => (source instanceof Element ? source.getAttribute(name) : source[name]);
  const label = text(read('prizesLabel'));
  if (label === null) {
    return null;
  }
  let lines = read('prizesLines');
  if (typeof lines === 'string') {
    try {
      lines = JSON.parse(lines) as unknown;
    } catch {
      lines = [];
    }
  }
  return { label, lines: array(lines).filter((line): line is string => typeof line === 'string') };
}

// ---- event history ----

/** GET /eventHistory/months: the months with completed events, newest first. */
export function parseEventHistoryMonths(json: unknown): string[] {
  return array(object(json)['months']).flatMap((month) => {
    const value = text(month);
    return value ? [value] : [];
  });
}

/** GET /eventHistory */
export function parseEventHistory(json: unknown, month: string, kind: EventKind): CompletedEventsMonth {
  const root = object(json);
  const isAdmin = root['isAdmin'] === true;
  return {
    month: text(root['month']) ?? month,
    kind,
    isAdmin,
    events: array(root['events']).map((entry) => parseCompletedEvent(object(entry), isAdmin)),
  };
}

function parseCompletedEvent(json: Json, isAdmin: boolean): CompletedEvent {
  return {
    id: text(json['id']),
    name: text(json['name']),
    startDate: text(json['startDate']),
    endDate: text(json['endDate']),
    format: text(json['format']),
    playerCount: number(json['playerCount']),
    rounds: number(json['rounds']),
    adminLinks: isAdmin ? array(json['adminLinks']).flatMap((link) => parseAdminLink(link)) : [],
  };
}

/** renderAdminCell: a link with a safe address, else its text; an entry with neither is left out. */
function parseAdminLink(value: unknown): EventAdminLink[] {
  if (value === null || typeof value !== 'object') {
    return [];
  }
  const json = value as Json;
  const label = text(json['label']) ?? '';
  const url = safeUrl(text(json['url']));
  const shown = text(json['text']) ?? label;
  if (url === null && !shown) {
    return [];
  }
  return [{ label, text: shown || (url ?? ''), url }];
}

/** EventHistoryUI.safeUrl: same-site absolute paths and http(s) addresses only. */
export function safeUrl(url: string | null): string | null {
  if (url === null) {
    return null;
  }
  return /^\/(?!\/)/.test(url) || /^https?:\/\//i.test(url) ? url : null;
}

// ---- leagues ----

/** GET /league: the current leagues, in the server's order. */
export function parseLeagueList(xml: string): LeagueSummary[] | null {
  const root = rootElement(parseXml(xml), 'leagues');
  if (root === null) {
    return null;
  }
  return childElements(root, 'league').map((league) => ({
    code: attr(league, 'code') ?? '',
    name: attr(league, 'name') ?? '',
    start: attr(league, 'start') ?? '',
    end: attr(league, 'end') ?? '',
  }));
}

/** GET /league/{code} (loadedLeague). Null when the answer is not a <league>. */
export function parseLeagueDetail(xml: string): LeagueDetail | null {
  const league = rootElement(parseXml(xml), 'league');
  if (league === null) {
    return null;
  }
  const rtmd = attr(league, 'type') === 'RTMD';
  return {
    code: attr(league, 'code') ?? '',
    name: attr(league, 'name') ?? '',
    cost: price(intAttr(league, 'cost', 0)),
    start: attr(league, 'start') ?? '',
    end: attr(league, 'end') ?? '',
    member: boolAttr(league, 'member'),
    joinable: boolAttr(league, 'joinable'),
    draftable: boolAttr(league, 'draftable'),
    inviteOnly: boolAttr(league, 'inviteOnly'),
    description: parseServerMarkup(attr(league, 'desc') ?? '', RICH_MARKUP),
    rtmd: rtmd ? parseRtmdPath(league) : null,
    series: childElements(league, 'serie').map(parseLeagueSerie),
    standings: sortStandings(childElements(league, 'leagueStanding').map((standing) => leagueStanding(standing, rtmd))),
  };
}

function parseRtmdPath(league: Element): RtmdPath {
  const position = attr(league, 'playerPosition');
  return {
    pathLength: intAttr(league, 'pathLength', 0),
    playerPosition: position ? intAttr(league, 'playerPosition', 0) : null,
    cumulative: boolAttr(league, 'cumulative'),
    advancementMode: attr(league, 'advancementMode') ?? '',
    advanceFactor: intAttr(league, 'advanceFactor', 0),
    metaSites: childElements(league, 'metaSite').map((site) => ({
      position: intAttr(site, 'position', 0),
      blueprintId: attr(site, 'blueprintId') ?? '',
      visualBlueprintId: nonEmpty(attr(site, 'visualBlueprintId')),
      name: attr(site, 'name'),
    })),
  };
}

function parseLeagueSerie(serie: Element): LeagueSerie {
  const matches = childElements(serie, 'matches').flatMap((group) => childElements(group, 'match'));
  return {
    name: attr(serie, 'type') ?? '',
    start: attr(serie, 'start') ?? '',
    end: attr(serie, 'end') ?? '',
    maxMatches: attr(serie, 'maxMatches') ?? '',
    formatCode: attr(serie, 'formatType') ?? '',
    format: attr(serie, 'format') ?? '',
    formatAvailable: attr(serie, 'formatAvailable') !== 'false' && !!attr(serie, 'formatType'),
    collection: attr(serie, 'collection') ?? '',
    limited: boolAttr(serie, 'limited'),
    matches: matches.map((match): LeagueMatch => ({
      winner: attr(match, 'winner') ?? '',
      loser: attr(match, 'loser') ?? '',
    })),
    standings: sortStandings(childElements(serie, 'standing').map((standing) => leagueStanding(standing, false))),
  };
}

function leagueStanding(element: Element, rtmd: boolean): LeagueStanding {
  return {
    standing: intAttr(element, 'standing', 0),
    player: attr(element, 'player') ?? '',
    points: intAttr(element, 'points', 0),
    gamesPlayed: intAttr(element, 'gamesPlayed', 0),
    opponentWin: attr(element, 'opponentWin') ?? '',
    level: rtmd ? nonEmpty(attr(element, 'position')) : null,
  };
}

/** createStandingsTable sorts by standing (a stable sort, as the server's order breaks ties). */
function sortStandings<T extends { standing: number }>(standings: T[]): T[] {
  return standings.sort((a, b) => a.standing - b.standing);
}

// ---- tournaments ----

/** GET /tournament: the live tournaments. */
export function parseTournamentList(xml: string): TournamentSummary[] | null {
  const root = rootElement(parseXml(xml), 'tournaments');
  if (root === null) {
    return null;
  }
  return childElements(root, 'tournament').map((tournament) => ({
    id: attr(tournament, 'id') ?? '',
    name: attr(tournament, 'name') ?? '',
    start: attr(tournament, 'start') ?? '',
  }));
}

export const STAGE_FINISHED = 'Finished';
export const STAGE_PLAYING_GAMES = 'Playing Games';

/** GET /tournament/{id} (loadedTournament). Null when the answer is not a <tournament>. */
export function parseTournamentDetail(xml: string): TournamentDetail | null {
  const tournament = rootElement(parseXml(xml), 'tournament');
  if (tournament === null) {
    return null;
  }
  const id = attr(tournament, 'id') ?? '';
  const stage = attr(tournament, 'stage') ?? '';
  const finished = stage === STAGE_FINISHED;
  return {
    id,
    name: attr(tournament, 'name') ?? '',
    format: attr(tournament, 'format') ?? '',
    collection: attr(tournament, 'collection') ?? '',
    round: attr(tournament, 'round') ?? '',
    stage,
    finished,
    playingGames: stage === STAGE_PLAYING_GAMES,
    standings: sortStandings(
      childElements(tournament, 'tournamentStanding').map((element): TournamentStanding => {
        const player = attr(element, 'player') ?? '';
        return {
          standing: intAttr(element, 'standing', 0),
          player,
          points: intAttr(element, 'points', 0),
          gamesPlayed: intAttr(element, 'gamesPlayed', 0),
          medianScore: attr(element, 'medianScore') ?? '',
          cumulativeScore: attr(element, 'cumulativeScore') ?? '',
          opponentWin: attr(element, 'opponentWin') ?? '',
          deckUrl: finished ? tournamentDeckUrl(id, player) : null,
        };
      }),
    ),
  };
}

/** A finished tournament's deck of a player: a page the server draws (DeckRenderer), opened in a new tab. */
export function tournamentDeckUrl(tournamentId: string, player: string): string {
  return `/gemp-lotr-server/tournament/${encodeURIComponent(tournamentId)}/deck/${encodeURIComponent(player)}/html`;
}

/** A tournament's report (admins and league admins see the link): a page the server draws, in a new tab. */
export function tournamentReportUrl(tournamentId: string): string {
  return `/gemp-lotr-server/tournament/${encodeURIComponent(tournamentId)}/report/html`;
}

/** A solo-draft league's draft page ("Go to draft"), a legacy page, in a new tab. */
export function soloDraftUrl(leagueCode: string): string {
  return `/gemp-lotr/soloDraft.html?eventId=${leagueCode}`;
}
