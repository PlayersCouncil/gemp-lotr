import { APP_URL, isReturnPath } from '../platform/app-paths';

/**
 * The place in the app the visitor was sent to the login page from (its `?return=`: the login guard and the hall's
 * "log in" link pass it), or '' for none. Anything that isn't a place in the app is dropped, so the login page
 * can't be made to send a player elsewhere (as login.js hallReturnLink did for the legacy hall's #fragment).
 */
export function returnPath(queryString: string): string {
  const path = new URLSearchParams(queryString).get('return') ?? '';
  return isReturnPath(path) ? path : '';
}

/** The page to go to after logging in: the place the visitor came from, else the hall. */
export function loginTarget(queryString: string): string {
  return APP_URL + (returnPath(queryString) || '/');
}
