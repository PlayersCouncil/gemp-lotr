import { MarkupNode } from '../../chat/chat-markup';
import { Price } from '../price';

/**
 * The Events tab's data, read from the server's answers (see README.md). Every text the server sends is kept as text;
 * the league and event descriptions, which the server renders from markdown, are MarkupNode lists.
 */

// ---- the calendar (GET /calendar) ----

export type CalendarEventKind = 'league' | 'tournament' | 'projected';

/** One serie of a league on the calendar. */
export interface CalendarSerie {
  /** As the server names it ("Serie 2" unless an admin named it otherwise); may be empty. */
  readonly name: string;
  /** yyyy-MM-dd */
  readonly start: string;
  readonly end: string;
  /** The format's name; null when the serie's format has been retired. */
  readonly format: string | null;
  readonly limited: boolean;
}

/** What the calendar needs to offer the hall's "Join Queue" for a scheduled tournament (the hall poll's <queue>). */
export interface CalendarQueueOffer {
  /** The queue's id: the tournament's own. */
  readonly id: string;
  readonly name: string;
  /** The hall queue's type (CONSTRUCTED, SEALED, ...). */
  readonly type: string;
  /** The queue's start text. */
  readonly start: string;
}

interface CalendarEventBase {
  /** League code, tournament id, or for a projected league "<scheduleId>:<index>:<start>". */
  readonly id: string;
  readonly name: string;
  /** yyyy-MM-dd, server time (UTC); "" when the server sent none (such an event is drawn nowhere). */
  readonly start: string;
  readonly end: string;
  /** The viewer is signed up (a league's member, in a tournament's queue or among its players). */
  readonly joined: boolean;
}

export interface CalendarLeague extends CalendarEventBase {
  readonly kind: 'league';
  readonly leagueType: string | null;
  readonly inviteOnly: boolean;
  readonly cost: Price | null;
  /** Server-rendered markdown; empty when there is none. */
  readonly description: readonly MarkupNode[];
  readonly scheduleId: string | null;
  readonly series: readonly CalendarSerie[];
}

export interface CalendarTournament extends CalendarEventBase {
  readonly kind: 'tournament';
  readonly tournamentType: string | null;
  readonly format: string | null;
  /** "HH:mm" UTC, from the server's startTime; null when it sent none. */
  readonly startTime: string | null;
  /** The tournament has started, so GET /tournament/{id} knows it. */
  readonly started: boolean;
  readonly cost: Price | null;
  /** Its hall queue takes sign-ups and the viewer is not in it. */
  readonly queue: CalendarQueueOffer | null;
  readonly prizes: EventPrizes | null;
}

/** A league an active schedule will create (sent to event admins only). */
export interface CalendarProjected extends CalendarEventBase {
  readonly kind: 'projected';
  readonly scheduleId: string | null;
  readonly scheduleName: string;
  readonly eventName: string | null;
  readonly leagueType: string | null;
  /** yyyy-MM-dd */
  readonly createdOn: string;
  /** Why it cannot be created; null when it can. */
  readonly error: string | null;
}

export type CalendarEvent = CalendarLeague | CalendarTournament | CalendarProjected;

export interface CalendarMonth {
  readonly from: string;
  readonly to: string;
  readonly eventAdmin: boolean;
  readonly events: readonly CalendarEvent[];
}

/**
 * A tournament's prizes: the label the hall shows ("Prize Breakdown", "No prizes") and the breakdown's lines, plain
 * text. No Events call carries them yet (see parseEventPrizes).
 */
export interface EventPrizes {
  readonly label: string;
  readonly lines: readonly string[];
}

// ---- event history (GET /eventHistory, /eventHistory/months) ----

export type EventKind = 'league' | 'tournament';

/** A link or text an admin sees on a completed event (the tournament's report; the league's code). */
export interface EventAdminLink {
  readonly label: string;
  readonly text: string;
  /** A same-site path or an http(s) address; null when it is plain text (or the server sent something else). */
  readonly url: string | null;
}

export interface CompletedEvent {
  readonly id: string | null;
  readonly name: string | null;
  readonly startDate: string | null;
  /** Leagues only. */
  readonly endDate: string | null;
  readonly format: string | null;
  readonly playerCount: number | null;
  /** Tournaments only. */
  readonly rounds: number | null;
  /** Only when the month's answer says the viewer is an admin. */
  readonly adminLinks: readonly EventAdminLink[];
}

export interface CompletedEventsMonth {
  /** yyyy-MM */
  readonly month: string;
  readonly kind: EventKind;
  readonly isAdmin: boolean;
  readonly events: readonly CompletedEvent[];
}

// ---- leagues (GET /league, /league/{code}) ----

/** A row of Current Leagues (and what a calendar preview's row needs). */
export interface LeagueSummary {
  readonly code: string;
  readonly name: string;
  readonly start: string;
  readonly end: string;
}

export interface LeagueStanding {
  readonly standing: number;
  readonly player: string;
  readonly points: number;
  readonly gamesPlayed: number;
  /** As the server formats it ("62.50%"). */
  readonly opponentWin: string;
  /** RTMD leagues' overall standings: the player's level; null elsewhere. */
  readonly level: string | null;
}

export interface LeagueMatch {
  readonly winner: string;
  readonly loser: string;
}

export interface LeagueSerie {
  /** The serie's name as sent ("type"). */
  readonly name: string;
  readonly start: string;
  readonly end: string;
  readonly maxMatches: string;
  /** The format's code; "" when the server has none. */
  readonly formatCode: string;
  /** The format's name, or its code when retired. */
  readonly format: string;
  /** The format still exists (absent on older servers: true), so its description can be shown. */
  readonly formatAvailable: boolean;
  readonly collection: string;
  readonly limited: boolean;
  /** The viewer's matches in this serie. */
  readonly matches: readonly LeagueMatch[];
  readonly standings: readonly LeagueStanding[];
}

/** A meta-site of a Race to Mount Doom league's path. */
export interface RtmdMetaSite {
  readonly position: number;
  /** The modifier card. */
  readonly blueprintId: string;
  /** The card drawn for the position, with the modifier over its bottom; null: the modifier alone. */
  readonly visualBlueprintId: string | null;
  readonly name: string | null;
}

export interface RtmdPath {
  readonly pathLength: number;
  /** The viewer's level (members only). */
  readonly playerPosition: number | null;
  readonly cumulative: boolean;
  /** "SCORE" or "WINS" */
  readonly advancementMode: string;
  readonly advanceFactor: number;
  readonly metaSites: readonly RtmdMetaSite[];
}

export interface LeagueDetail {
  readonly code: string;
  readonly name: string;
  readonly cost: Price;
  readonly start: string;
  readonly end: string;
  readonly member: boolean;
  readonly joinable: boolean;
  readonly draftable: boolean;
  readonly inviteOnly: boolean;
  readonly description: readonly MarkupNode[];
  /** Race to Mount Doom leagues only. */
  readonly rtmd: RtmdPath | null;
  readonly series: readonly LeagueSerie[];
  /** The overall standings, by standing. */
  readonly standings: readonly LeagueStanding[];
}

// ---- tournaments (GET /tournament, /tournament/{id}) ----

export interface TournamentSummary {
  readonly id: string;
  readonly name: string;
  /** yyyy-MM-dd (UTC); "" when the server sent none. */
  readonly start: string;
}

export interface TournamentStanding {
  readonly standing: number;
  readonly player: string;
  readonly points: number;
  readonly gamesPlayed: number;
  readonly medianScore: string;
  readonly cumulativeScore: string;
  readonly opponentWin: string;
  /** The player's deck page (a finished tournament only), opened in a new tab. */
  readonly deckUrl: string | null;
}

export interface TournamentDetail {
  readonly id: string;
  readonly name: string;
  readonly format: string;
  readonly collection: string;
  readonly round: string;
  /** As the server words it ("Playing Games", "Finished", ...). */
  readonly stage: string;
  readonly finished: boolean;
  /** The "Round:" line shows while games are being played. */
  readonly playingGames: boolean;
  readonly standings: readonly TournamentStanding[];
}
