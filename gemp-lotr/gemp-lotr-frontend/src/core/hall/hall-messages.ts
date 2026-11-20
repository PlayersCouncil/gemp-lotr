import { HallConnectionState } from './hall-models';

/** The connection readout's word for each state. */
export const HALL_CONNECTION_LABELS: Readonly<Record<HallConnectionState, string>> = {
  connecting: 'Connecting',
  connected: 'Connected',
  shutdown: 'Shutdown',
  reconnecting: 'Reconnecting',
  disconnected: 'Disconnected',
  loggedout: 'Not logged in',
};

/** The first line of the readout's popup. */
export const HALL_CONNECTION_STATUS: Readonly<Record<HallConnectionState, string>> = {
  connecting: 'Connecting to the Game Hall…',
  connected: 'Connected: the Game Hall is updating live.',
  shutdown:
    'Shutdown: the server will restart for an update when the games in progress end. You can finish or ' +
    "watch games, but can't start or join tables, bot games or tournament queues until then; waiting tables " +
    'and queues were closed.',
  reconnecting: 'Reconnecting: the Game Hall lost contact with the server and is retrying.',
  disconnected: 'Disconnected: the Game Hall is not updating.',
  loggedout: "Not logged in: log in to see the Game Hall's tables and chat and to play.",
};

/** What the readout's live region says when the state changes (with the reason once the hall is disconnected). */
export function spokenConnection(state: HallConnectionState, message: string | null): string {
  if (state === 'loggedout') {
    return 'Not logged in.';
  }
  if (state === 'shutdown') {
    return "Server in shutdown mode: new games can't be started until it restarts.";
  }
  const spoken = `Game Hall ${HALL_CONNECTION_LABELS[state].toLowerCase()}.`;
  return state === 'disconnected' && message ? `${spoken} ${message}` : spoken;
}

/** "Last update: ..." in the popup. */
export function lastUpdateLine(lastUpdate: string | null): string {
  return `Last update: ${lastUpdate === null ? 'none yet' : `${lastUpdate} (server time)`}`;
}

/** The popup's link text for each action. */
export const RELOAD_LINK = 'Reload the page';
export function loginLink(state: HallConnectionState): string {
  return state === 'loggedout' ? 'Log in or register' : 'Go to the main page to log in';
}

export const RESUBSCRIBED =
  'Reconnected after the server had dropped this window from the hall (it had been away too long). ' +
  'Any table or queue you were waiting in may have been closed.';
export const NOT_LOGGED_IN = 'You are not logged in (or your session has ended).';
export const OPENED_ELSEWHERE =
  'The Game Hall was opened in another window or tab, so this one stopped updating. ' +
  'Close this one, or reload it to use the hall here.';
export const stoppedWithError = (status: number) =>
  `The Game Hall stopped updating: the server answered with error ${status}.`;

/** Why a poll failed, for the retry messages. */
export const REASON_TIMEOUT = 'the server did not answer in time';
export const REASON_UNREACHABLE = 'could not reach the server';
export const REASON_DROPPED = 'the server had dropped this window from the hall';
export const REASON_UNREADABLE = 'the server sent an answer that could not be read';
export const reasonStatus = (status: number) => `the server answered with error ${status}`;

export const retrying = (reason: string, seconds: number) =>
  `Lost contact with the server (${reason}). Retrying in ${seconds} s.`;
export const stillRetrying = (reason: string, seconds: number) =>
  `Lost contact with the server (${reason}). Still retrying every ${seconds} s; if it does not come back, reload the page.`;

/** The "Not logged in" dialog (showLoginRequired). */
export const LOGIN_REQUIRED_TITLE = 'Not logged in';
export const LOGIN_REQUIRED =
  'You are not logged in. The Game Hall, Events and My Account need you to log in; ' +
  'Help and Server Info can be read without.';
