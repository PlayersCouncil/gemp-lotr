/** The Events tab's wording, as the legacy tab has it (events.html and the *Ui.js files named on each). */

import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { messageHeader } from '../hall-request-failed';

// ---- the tab and its sub-tabs (events.html) ----

export const EVENTS_SUB_TABS = [
  { slug: 'calendar', label: 'Calendar' },
  { slug: 'leagues', label: 'Current Leagues' },
  { slug: 'tournaments', label: 'Current Tournaments' },
] as const;

/** The Calendar's intro; "bold" is drawn in bold. */
export const CALENDAR_INTRO_BEFORE_BOLD =
  'All dates are server time (UTC). Click an event to see its details; events you have joined are in ';
export const CALENDAR_INTRO_BOLD = 'bold';
export const CALENDAR_INTRO_AFTER_BOLD = '.';
export const LISTS_INTRO = 'All times are server time (UTC).';
export const DETAILS_PLACEHOLDER = 'Click on an event above to load details.';

// ---- the calendar (calendarUi.js) ----

export const MONTH_NAMES = [
  'January',
  'February',
  'March',
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'December',
] as const;
export const WEEKDAY_NAMES = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] as const;
export const CALENDAR_PREVIOUS = '<';
export const CALENDAR_NEXT = '>';
export const CALENDAR_TODAY = 'Today';
export const CALENDAR_LEGEND = {
  league: 'League',
  tournament: 'Tournament',
  projected: 'Scheduled (not yet created)',
} as const;
export const CALENDAR_BAD_REQUEST = 'Could not load the calendar.';
export const CALENDAR_LOGGED_OUT = 'You must be logged in to see the calendar.';
export const SIGNED_UP = 'You are signed up';
export const GO_TO_LEAGUE = 'Go to League';
export const GO_TO_TOURNAMENT = 'Go to Tournament';
export const KIND_LABELS = { league: 'League', tournament: 'Tournament', projected: 'Scheduled' } as const;
export const SERVER_TIME_TITLE = 'Server time (UTC)';
export const JOIN_QUEUE = 'Join Queue';

/** The details' fact labels (renderFacts). */
export const FACT_STARTS = 'Starts';
export const FACT_FORMAT = 'Format';
export const FACT_RUNS = 'Runs';
export const FACT_COST = 'Cost';
export const FACT_ENTRY = 'Entry';
export const FACT_STATUS = 'Status';
export const INVITE_ONLY = 'Invite only';
export const SERIES_HEADERS = ['', 'Serie', 'Format', 'Dates'] as const;

export const cannotBeCreated = (error: string) => `Cannot be created: ${error}`;
export const projectedStatus = (createdOn: string, scheduleName: string) =>
  `Not created yet; will appear on ${createdOn} (schedule '${scheduleName}')`;
export const projectedTooltip = (createdOn: string, scheduleName: string) =>
  `Will be created on ${createdOn} by schedule '${scheduleName}'`;

// ---- the drawers (eventDrawer.js) ----

export const DETAILS_CLOSED = '+ Details';
/** Starts with a real minus sign, as wide as the plus. */
export const DETAILS_OPEN = '− Details';
export const DRAWER_LOADING = 'Loading details…';

/**
 * EventDrawer.errorMap: what a failed Events call says, inline. A 400 with the server's own words says them.
 */
export function eventErrorMessage(error: ApiError): string {
  const status = error.status;
  if (status === 400) {
    const detail = error.said ?? messageHeader(error.cause);
    return detail !== null ? `The request was rejected: ${detail}` : 'The request was rejected as malformed.';
  }
  if (status === INVALID_RESPONSE_STATUS) {
    return "The server's answer could not be read.";
  }
  return STATUS_MESSAGES[status] ?? `The server answered with HTTP ${status}.`;
}

/** The status the legacy messages put in "(HTTP n)": an unreadable answer came with a 200. */
export function shownStatus(error: ApiError): number {
  return error.status === INVALID_RESPONSE_STATUS ? 200 : error.status;
}

const STATUS_MESSAGES: Readonly<Record<number, string>> = {
  0: 'The server is unreachable or the connection dropped.',
  401: 'You are not logged in.',
  403: 'You do not have permission to see this.',
  404: 'It was not found on the server.',
  410: 'You were logged out after being inactive; refresh the page.',
  500: 'The server failed to produce it.',
};

// ---- leagues (leagueResultsUi.js) ----

export const NO_CURRENT_LEAGUES = 'There are no current leagues at the moment.';
export const leaguesFailed = (message: string) => `Could not load the current leagues. ${message}`;
export const leagueFailed = (status: number, message: string) =>
  `Could not load the league (HTTP ${status}). ${message}`;
export const LEAGUE_NOT_DISPLAYED = 'Could not display the league.';
export const NO_LEAGUE_DETAILS = 'No details are available for this league.';
export const LEAGUE_CODE_TITLE = 'League code - click to select';
export const COST_LABEL = 'Cost:';
export const ALREADY_MEMBER = 'You are already a member of this league. ';
export const GO_TO_DRAFT = 'Go to draft';
export const NOT_A_MEMBER = "You're not a member of this league. ";
export const JOIN_LEAGUE = 'Join league';
export const INVITATION_ONLY = 'Invitation-only. See below for how to join.';
/** The join question's dialog (displayBuyAction); the price is drawn with the gold and silver images. */
export const JOIN_DIALOG_TITLE = 'League operation';
export const JOIN_QUESTION_BEFORE_PRICE = 'Do you want to join the league by paying ';
export const JOIN_QUESTION_AFTER_PRICE = '?';
export const JOIN_YES = 'Yes';
export const JOIN_NO = 'No';
export const NOT_ENOUGH_FUNDS = "You don't have enough funds to join this league.";
export const FORMAT_DIALOG_TITLE = 'Format description';
export const OVERALL_RESULTS = 'Overall results';
export const YOUR_MATCHES = 'Your league matches';
export const serieTab = (index: number) => `Serie ${index + 1}`;
export const serieHeading = (name: string, start: string, end: string) => `${name} - ${start} to ${end}`;
export const MATCH_HEADERS = ['Winner', 'Loser'] as const;
export const FORMAT_LABEL = 'Format:';
export const COLLECTION_LABEL = 'Collection:';
export const RETIRED = ' (retired)';
export const CONSTRUCTED_PREFIX = 'Constructed ';
export const maxMatchesLine = (maxMatches: string) => `Maximum ranked matches in serie: ${maxMatches}`;
/** createStandingsTable's headers, one half (the table shows two halves side by side; Level for RTMD only). */
export const LEAGUE_STANDING_HEADERS = ['Standing', 'Level', 'Player', 'Points', 'Games played', 'Opp. Win %'] as const;
export const NO_LEVEL = '-';

/** The Race to Mount Doom block (leagueResultsUi.js loadedLeague, isRTMD). */
export const RTMD_TITLE = 'Race to Mount Doom';
export const RTMD_INTRO = [
  'Race to Mount Doom is a meta-progression constructed league.  Every player starts at the first meta-site, which ' +
    'will be played automatically to your support area whenever you play matches in this league.  Such meta-sites ' +
    'tend to have game text which is beneficial at low levels, detrimental at middle levels, and catastrophic at ' +
    'high levels.',
  'As you play matches, good performance will push you to harder and harder meta-sites.  Each time you get ' +
    'promoted, you will leave the old meta-site behind permanently and start using the next meta-site.  Some ' +
    'meta-sites impose deckbuilding restrictions, so watch out!',
  'There are dozens of different meta-site modifiers, so no two Races will ever be the same!',
] as const;
/** The intro's last paragraph; "only" is in italics. */
export const RTMD_NOTE_BEFORE_ITALIC = 'NOTE: Any meta-site that says "you" or "yours" ';
export const RTMD_NOTE_ITALIC = 'only';
export const RTMD_NOTE_AFTER_ITALIC =
  ' affects that player.  If it does not have one of those words, it affects both players.';
export const RTMD_LEVEL_BEFORE = 'Your level: ';
export const rtmdLevelAfter = (pathLength: number) => ` of ${pathLength}`;
export const rtmdAdvance = (factor: number, mode: string) =>
  `Advance every ${factor} ${mode === 'SCORE' ? 'points' : 'win(s)'}`;
export const RTMD_CUMULATIVE = 'Cumulative mode: all prior meta-sites remain active';

// ---- tournaments (tournamentResultsUi.js) ----

export const NO_RUNNING_TOURNAMENTS = 'There are no running tournaments at the moment.';
export const tournamentsFailed = (message: string) => `Could not load the current tournaments. ${message}`;
export const tournamentFailed = (status: number, message: string) =>
  `Could not load the tournament (HTTP ${status}). ${message}`;
export const TOURNAMENT_NOT_DISPLAYED = 'Could not display the tournament.';
export const NO_TOURNAMENT_DETAILS = 'No details are available for this tournament.';
export const TOURNAMENT_REPORT_TITLE = 'Tournament report';
export const ROUNDS_LABEL = 'Rounds:';
export const ROUND_LABEL = 'Round:';
export const TOURNAMENT_STANDING_HEADERS = [
  'Standing',
  'Player',
  'Points',
  'Games played',
  'Mod. Median Score',
  'Cumulative Score',
  'Opponent Win %',
] as const;

// ---- completed events (eventHistoryUi.js) ----

export const historyHeading = (kind: 'league' | 'tournament') =>
  kind === 'league' ? 'Completed leagues' : 'Completed tournaments';
export const kindPlural = (kind: 'league' | 'tournament') => (kind === 'league' ? 'leagues' : 'tournaments');
export const PREVIOUS_MONTH_WITH_EVENTS = 'Previous month with events';
export const NEXT_MONTH_WITH_EVENTS = 'Next month with events';
export const CHOOSE_A_MONTH = 'Choose a month';
export const MONTHS_LOADING = 'Loading months…';
export const monthsFailed = (message: string) => `Could not load the list of months. ${message}`;
export const RETRY = 'Retry';
export const historyLoadingAll = (kind: 'league' | 'tournament') => `Loading completed ${kindPlural(kind)}…`;
export const historyNoneYet = (kind: 'league' | 'tournament') =>
  `No completed ${kindPlural(kind)} have been recorded yet.`;
export const historyLoadingMonth = (label: string) => `Loading ${label}…`;
export const historyMonthFailed = (label: string, message: string) => `Could not load ${label}. ${message}`;
export const historyNoneIn = (kind: 'league' | 'tournament', label: string) =>
  `No completed ${kindPlural(kind)} in ${label}.`;
export const historyCount = (kind: 'league' | 'tournament', label: string, count: number) =>
  `${label}: ${count} completed ${count === 1 ? kind : kindPlural(kind)}.`;
export const historyColumns = (kind: 'league' | 'tournament', isAdmin: boolean): readonly string[] => [
  ...(kind === 'league'
    ? ['', 'Name', 'Dates', 'Format', 'Players']
    : ['', 'Name', 'Date', 'Format', 'Players', 'Rounds']),
  ...(isAdmin ? ['Admin'] : []),
];
export const historyDetailsFailed = (status: number, message: string) =>
  `Could not load the details (HTTP ${status}). ${message}`;
export const HISTORY_NOT_DISPLAYED = 'Could not display the details.';
export const noHistoryDetails = (kind: 'league' | 'tournament') => `No details are available for this ${kind}.`;
/** An empty cell (EventHistoryUI.orDash). */
export const DASH = '—';
/** Between a league's dates in the completed list. */
export const DATE_DASH = ' – ';
export const adminTextTitle = (label: string) => `${label ? `${label} - ` : ''}click to select`;
export const adminLinkTitle = (label: string, url: string) => (label ? `${label}: ${url}` : url);
