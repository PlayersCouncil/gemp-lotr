import { APP_URL } from '../platform/app-paths';

/** A place in the app, as the router writes it: "/events/leagues", "/deckbuilder?theme=modern". */
const APP_PATH = /^\/(?!\/)[A-Za-z0-9_\-/?=&%.+]{0,200}$/;

/**
 * The place in the app the visitor was sent to the login page from (its `?return=`: the login guard and the hall's
 * "log in" link pass it), or '' for none. Anything that isn't a place in the app is dropped, so the login page
 * can't be made to send a player elsewhere (as login.js hallReturnLink did for the legacy hall's #fragment).
 */
export function returnPath(queryString: string): string {
  const path = new URLSearchParams(queryString).get('return') ?? '';
  return APP_PATH.test(path) && !path.startsWith('/login') ? path : '';
}

/** The page to go to after logging in: the place the visitor came from, else the hall. */
export function loginTarget(queryString: string): string {
  return APP_URL + (returnPath(queryString) || '/');
}
