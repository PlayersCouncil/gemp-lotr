/**
 * The PC account's wording. Ketura's own labels (the buttons and links) are used exactly; the rest is short and new
 * (no legacy page had it). Players see "PC Account", never the service's name.
 */

// Ketura's labels
export const PC_SIGN_IN = 'Sign in with PC Account';
export const ENTER_GEMP = 'Enter Gemp';
export const CREATE_ALIAS = 'Create New Gemp Alias';
export const LINK_ACCOUNT = 'Link Existing Gemp Account';
export const LOG_OUT = 'Log Out';
export const CONNECT_PC_ACCOUNT = 'Connect PC Account';
export const MANAGE_PC_ACCOUNT = 'Manage PC Account';

/** Above the alias list. */
export function signedInAs(name: string): string {
  return `Signed in as ${name}`;
}

export const ALIASES_HEADING = 'Your Gemp aliases';
export const NO_ALIASES = 'No Gemp alias yet: create one, or link an account you had.';
export const ALIAS_CURRENT = 'current';
export const ALIAS_PLAYING = 'playing';
/** Why every Enter Gemp but the played alias's is disabled, and Create too (the alias is this browser's). */
export function blockedReason(busyAlias: string): string {
  return `${busyAlias} is in a game, queue, table or tournament: finish or leave it to switch.`;
}

export const PAGE_PREVIOUS = 'Previous';
export const PAGE_NEXT = 'Next';
export function pageLine(page: number, pages: number): string {
  return `Page ${page} of ${pages}`;
}

// The create and link forms
export const ALIAS_NAME_LABEL = 'Name';
export const LINK_LOGIN_LABEL = 'Username';
export const LINK_PASSWORD_LABEL = 'Password';
export const CREATE_SUBMIT = 'Create';
export const LINK_SUBMIT = 'Link';
export const CANCEL = 'Cancel';
export const CONNECT_PASSWORD_LABEL = 'Password';
export const CONNECT_SUBMIT = 'Connect';

// The errors of PC-ACCOUNT.md's tables, by code
export const NO_PC_ACCOUNT = 'Your PC Account sign-in has ended. Sign in again.';
export const NOT_LINKED = 'That alias is no longer linked to your PC Account.';
export const ALIAS_BANNED = 'That alias is banned.';
export const ALIAS_BUSY = 'Your alias here is in a game, queue, table or tournament: finish or leave it to switch.';
export const UNAVAILABLE = 'The PC Account service is not answering. Try again shortly.';
export const INVALID_ALIAS_NAME = 'A name is 2 to 30 letters, digits, _ or -.';
export const NAME_TAKEN = 'That name is taken.';
export const SLOW_DOWN = 'Too many tries. Wait a moment, then try again.';
export const BAD_CREDENTIALS = 'Wrong username or password, or that account cannot be linked with a password.';
export const LINKED_ELSEWHERE = 'That account belongs to another PC Account.';
/** Connect's own: its bad-credentials is this account's password alone. */
export const WRONG_PASSWORD = 'Wrong password.';
export const NOT_ALLOWED = 'Only an account logged in with its password can be connected.';
/** No answer, or one the page doesn't know. */
export const ACTION_FAILED = 'Something went wrong. Try again.';

// Lines for where a PC sign-in came back to
export const PC_CANCELLED = 'PC Account sign-in was cancelled.';
export const PC_FAILED = 'PC Account sign-in failed. Try again.';
export const PC_CONNECTED = 'This account is now connected to your PC Account.';
export const PC_LINKED_ELSEWHERE = 'This account already belongs to another PC Account.';

// The login page in `optional` mode, where the Register form was
export const NEW_PLAYERS = 'New players: sign in with a PC Account, then create your Gemp alias.';

// My Account's PC Account block
export const PC_BLOCK_HEADING = 'PC Account';
export const PC_ABOUT =
  "A PC Account is one sign-in for the Player's Council's sites. Connected, it keeps all your Gemp aliases together.";
export const PC_IS_CONNECTED =
  'This account is connected to a PC Account. Sign in with the PC Account to see all your aliases.';

/** The server's error codes, by action (PC-ACCOUNT.md). */
export type PcAction = 'enter' | 'create' | 'link' | 'connect';

const BY_CODE: Readonly<Record<string, string>> = {
  'no-pc-account': NO_PC_ACCOUNT,
  'not-linked': NOT_LINKED,
  banned: ALIAS_BANNED,
  busy: ALIAS_BUSY,
  unavailable: UNAVAILABLE,
  'invalid-name': INVALID_ALIAS_NAME,
  'name-taken': NAME_TAKEN,
  'slow-down': SLOW_DOWN,
  'bad-credentials': BAD_CREDENTIALS,
  'linked-elsewhere': LINKED_ELSEWHERE,
  'not-allowed': NOT_ALLOWED,
};

/** An action's own wording for a code, where the shared one doesn't fit. */
const BY_ACTION_CODE: Readonly<Partial<Record<PcAction, Readonly<Record<string, string>>>>> = {
  connect: { 'bad-credentials': WRONG_PASSWORD },
};

/**
 * The code an answer without one most likely means, by action and status (a proxy's page instead of the server's
 * JSON). Statuses with two codes (409 on create) take the likelier one.
 */
const BY_STATUS: Readonly<Record<PcAction, Readonly<Record<number, string>>>> = {
  enter: { 401: 'no-pc-account', 403: 'not-linked', 409: 'busy', 503: 'unavailable' },
  create: { 400: 'invalid-name', 401: 'no-pc-account', 409: 'name-taken', 429: 'slow-down', 503: 'unavailable' },
  link: { 401: 'bad-credentials', 409: 'linked-elsewhere', 429: 'slow-down', 503: 'unavailable' },
  connect: { 401: 'bad-credentials', 403: 'not-allowed', 429: 'slow-down', 503: 'unavailable' },
};

/** The code of a refusal: the server's own, else the likeliest for its status, else null. */
export function refusalCode(action: PcAction, status: number, said: string | null): string | null {
  if (said !== null && said in BY_CODE) return said;
  return BY_STATUS[action][status] ?? null;
}

/** What to show for a refused action (`action`'s own wording first). */
export function refusalMessage(code: string | null, action: PcAction | null = null): string {
  if (code === null) return ACTION_FAILED;
  return (action !== null ? BY_ACTION_CODE[action]?.[code] : undefined) ?? BY_CODE[code] ?? ACTION_FAILED;
}

const NOTICES: Readonly<Record<'login' | 'account', Readonly<Record<string, string>>>> = {
  login: { cancelled: PC_CANCELLED, failed: PC_FAILED },
  account: {
    connected: PC_CONNECTED,
    'linked-elsewhere': PC_LINKED_ELSEWHERE,
    cancelled: PC_CANCELLED,
    failed: PC_FAILED,
  },
};

/**
 * The line for `?pc=` where a PC sign-in came back to: the login page (cancelled, failed) or My Account, after Connect
 * PC Account (connected, linked-elsewhere, cancelled, failed).
 */
export function pcNotice(queryString: string, page: 'login' | 'account'): string | null {
  const pc = new URLSearchParams(queryString).get('pc') ?? '';
  return Object.hasOwn(NOTICES[page], pc) ? (NOTICES[page][pc] ?? null) : null;
}
