/** Where the Gemp server answers. */
export const SERVER_URL = '/gemp-lotr-server';

/** Where the app's pages are: `APP_URL + '/deckbuilder'`. */
export const APP_URL = '/gemp-lotr/ng';

/** The login page. */
export const LOGIN_PAGE_URL = `${APP_URL}/login`;

/** The site's front door (the `start` route): the hall for a logged-in player, the login page for anyone else. */
export const START_PAGE_URL = `${APP_URL}/start`;

/** A place in the app, as the router writes it: "/events/leagues", "/deckbuilder?theme=modern". */
const APP_PATH = /^\/(?!\/)[A-Za-z0-9_\-/?=&%.+]{1,200}$/;

/**
 * Whether `path` is a place in the app the login page may come back to (its `?return=`): anything else is dropped,
 * so the login page can't be made to send a player elsewhere, nor back to itself.
 */
export function isReturnPath(path: string): boolean {
  return APP_PATH.test(path) && !path.startsWith('/login');
}

/** Where the legacy pages are: the server's HTML names its pictures relative to them (images/cultures/gondor.png). */
const LEGACY_PAGES_URL = '/gemp-lotr/';

/**
 * The server's HTML with its relative picture addresses made the legacy pages' ones, so they show from this app's
 * pages too (under /gemp-lotr/ng/, `images/…` named a missing file: the owner, 2026-11-17, a culture in an action's
 * button). Absolute addresses and ones with a scheme stay.
 */
export function legacyRelativeImages(html: string): string {
  return html.replace(/(<img\b[^>]*?\bsrc\s*=\s*['"]?)(?![a-z][a-z0-9+.-]*:|[/'"])/gi, `$1${LEGACY_PAGES_URL}`);
}

/** The game page's address for a game, for a player acting as another (an admin's ?participantId=) if given. */
export function gamePageUrl(gameId: string, participantId: string | null = null): string {
  const acting = participantId === null ? '' : `&participantId=${encodeURIComponent(participantId)}`;
  return `/gemp-lotr/ng/game?gameId=${encodeURIComponent(gameId)}${acting}`;
}

/** Where the site's own images are. */
const IMAGES_URL = '/gemp-lotr/images';

/** The address of one of the site's images, by its path under the images folder. */
export function siteImage(path: string): string {
  return `${IMAGES_URL}/${path}`;
}
