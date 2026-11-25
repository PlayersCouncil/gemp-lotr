import { listOf } from '../api/json';
import { SERVER_URL } from '../platform/app-paths';

/**
 * The PC account in Gemp (gemp-lotr/PC-ACCOUNT.md, "The Server's Addresses"): who this browser is, as GET
 * /argonauth/session tells it, and the addresses the browser is sent to. Players see "PC Account", never the service's
 * name; in code, settings and addresses `argonauth` is fine.
 */

/** `sso.mode`: `test` shows no PC account anywhere; `optional` offers it beside the password login. */
export type PcMode = 'test' | 'optional';

/** The signed-in PC account. `fresh` is false once its sign-in has lapsed: creating and linking then sign in again. */
export interface PcAccount {
  readonly name: string;
  readonly manageUrl: string;
  readonly fresh: boolean;
}

/**
 * A Gemp account of the PC account. `busy`: at a table, in a queue, in a running tournament or in a game, in whatever
 * browser; shown, and gating nothing by itself (see `blockedBy`).
 */
export interface Alias {
  readonly id: number;
  readonly name: string;
  readonly current: boolean;
  readonly busy: boolean;
}

/** GET /argonauth/session, and what the actions answer on success. */
export interface PcSession {
  readonly mode: PcMode;
  /** The alias this session plays as, or null. */
  readonly player: string | null;
  /** Null for a password login and for nobody. */
  readonly pcAccount: PcAccount | null;
  /** Whether `player` is linked to a PC account (Gemp's copy); null with no player. */
  readonly linked: boolean | null;
  readonly aliases: readonly Alias[];
  /**
   * `player`, when a PC account is signed in and the alias this browser plays is busy (at a table, in a queue, in a
   * running tournament or in a game), else null. While set, this browser can enter only that alias and create none;
   * linking and logging out still can. The switching gate is each browser's own: another alias busy elsewhere gates
   * nothing.
   */
  readonly blockedBy: string | null;
}

/** What the browser's session is when the server can't say (an older server, or none): `test` mode, nobody. */
export const NO_PC_SESSION: PcSession = {
  mode: 'test',
  player: null,
  pcAccount: null,
  linked: null,
  aliases: [],
  blockedBy: null,
};

/** Who is signed in, in the terms the pages use. */
export type PcSessionKind = 'test' | 'nobody' | 'password' | 'pc';

export function sessionKind(session: PcSession): PcSessionKind {
  if (session.mode === 'test') return 'test';
  if (session.pcAccount) return 'pc';
  return session.player === null ? 'nobody' : 'password';
}

const text = (value: unknown): string | null => (typeof value === 'string' && value !== '' ? value : null);

function parseAccount(json: unknown): PcAccount | null {
  if (typeof json !== 'object' || json === null) return null;
  const { name, manageUrl, fresh } = json as Record<string, unknown>;
  return { name: text(name) ?? '', manageUrl: text(manageUrl) ?? '', fresh: fresh !== false };
}

function parseAlias(json: unknown): Alias | null {
  if (typeof json !== 'object' || json === null) return null;
  const { id, name, current, busy } = json as Record<string, unknown>;
  const aliasName = text(name);
  if (typeof id !== 'number' || aliasName === null) return null;
  return { id, name: aliasName, current: current === true, busy: busy === true };
}

/** Reads the session's JSON; anything it doesn't say is as for nobody, and a mode other than `optional` is `test`. */
export function parsePcSession(json: unknown): PcSession {
  if (typeof json !== 'object' || json === null) return NO_PC_SESSION;
  const { mode, player, pcAccount, linked, aliases, blockedBy } = json as Record<string, unknown>;
  return {
    mode: mode === 'optional' ? 'optional' : 'test',
    player: text(player),
    pcAccount: parseAccount(pcAccount),
    linked: typeof linked === 'boolean' ? linked : null,
    aliases: listOf(aliases)
      .map(parseAlias)
      .filter((alias) => alias !== null),
    blockedBy: text(blockedBy),
  };
}

/** Whether `alias` can be entered: always, unless this browser's alias is busy (`blockedBy`) and it is not that one. */
export function canEnter(session: PcSession, alias: Alias): boolean {
  return session.blockedBy === null || session.blockedBy === alias.name;
}

/** Gemp's rule for a new account's name (RegisterRequestHandler): 2 to 30 of A-Z a-z 0-9 _ -. */
export const ALIAS_NAME = /^[A-Za-z0-9_-]{2,30}$/;
export const ALIAS_NAME_MAX = 30;

export function isAliasName(name: string): boolean {
  return ALIAS_NAME.test(name);
}

/** Where the PC Account's pages are on the Gemp server. */
export const ARGONAUTH_URL = `${SERVER_URL}/argonauth`;

/**
 * What a PC sign-in is for, besides signing in: open the link or create form. (Connecting a password login is POST
 * /argonauth/connect, with the account's password.)
 */
export type PcSignInPurpose = 'link' | 'create';

/**
 * GET /argonauth/login: starts a PC sign-in, for `then`, coming back (after entering) to `returnTo` if given. With
 * `register` (Register PC Account) the server sends the browser to the account service's sign-up page where it can;
 * the person comes back signed in either way.
 */
export function pcSignInAddress(then: PcSignInPurpose | null = null, returnTo = '', register = false): string {
  const params = new URLSearchParams();
  if (then !== null) params.set('then', then);
  if (register) params.set('register', '1');
  if (returnTo !== '') params.set('return', returnTo);
  const query = params.toString();
  return `${ARGONAUTH_URL}/login${query === '' ? '' : `?${query}`}`;
}

/** The form a page opens on load: `?open=link` or `?open=create` (the sign-in for them came back). */
export function openedForm(queryString: string): 'link' | 'create' | null {
  const open = new URLSearchParams(queryString).get('open');
  return open === 'link' || open === 'create' ? open : null;
}
