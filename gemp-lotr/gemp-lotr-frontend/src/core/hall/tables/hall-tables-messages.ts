/** The Play / Join flows' wording (CreateUnrankedTable, CreateBotTable, JoinTable, TableFlow). */

export const RESULT_READY = 'Ready.';
export const RESULT_WORKING = 'Working…';
export const RESULT_OK = 'OK';

export const MUST_SELECT_FORMAT = 'You must select a format.';
export const MUST_SELECT_DECK = 'You must select a deck: one of yours, or one from the Deck Library.';
export const MUST_SELECT_OWN_DECK =
  'You must select a deck for you to use: one of yours, or one from the Deck Library.';
export const MUST_NAME_INVITEE =
  'If set to invite-only, you must name a player to invite (all others will be disallowed from joining your table).';
export const CANNOT_INVITE_SELF = 'You cannot invite yourself; name another player.';
export const TABLE_CREATED_KEPT_OPEN = 'Table created. It waits in Waiting Tables until an opponent joins.';

/** What a refusal without a message says. */
export const SERVER_REFUSED = 'The server refused the request.';

/** The (i) text of the Game Timer when the server sent no description of the timer. */
export const TIMER_HELP =
  'Each player has a total time bank (the first number), and will time out with a loss if they run out of their ' +
  'time bank or take longer than the second number between actions.';

/** The decks dropdown's empty first option. */
export const CHOOSE_DECK = 'Choose one of your decks';
export const NO_DECKS_YET = 'You have no decks yet';
/** The bot's deck dropdown's first option (botDeck ""). */
export const RANDOM_STARTER = 'Random FotR Starter';

/** Formats and decks that could not be loaded (FormatManager.reportError). */
export const formatsFailed = (message: string) => `Could not load the list of formats: ${message}`;

/** "Your game is ready" (hallUi.js openOrOffer), when the browser blocked the new tab. */
export const GAME_READY_TITLE = 'Your game is ready';
export const GAME_READY_TEXT = 'Your game has started, but your browser blocked the new window.';
export const GAME_READY_OPEN = 'Open game';

/** TableFlow.errorMap: a readable sentence per status; 400 carries the server's own message when there is one. */
const STATUS_MESSAGES: Readonly<Record<number, string>> = {
  0: 'Could not reach the server. Check your internet connection and try again.',
  401: 'You are not logged in. Log in again from the main page.',
  403: 'You do not have permission to do that.',
  404: 'The server could not find that; it may have been removed. Refresh the hall and try again.',
  409: 'That conflicts with the current state on the server (for example, not enough currency).',
  410: 'You were logged out after being inactive. Refresh the page to reconnect.',
  500: 'The server ran into an error. Please try again, and report a bug if it keeps happening.',
};

export const MALFORMED_REQUEST = 'The server rejected the request as malformed.';

export function statusMessage(status: number): string {
  if (status === 400) {
    return MALFORMED_REQUEST;
  }
  return STATUS_MESSAGES[status] ?? `The server answered with an error (HTTP ${status}).`;
}
