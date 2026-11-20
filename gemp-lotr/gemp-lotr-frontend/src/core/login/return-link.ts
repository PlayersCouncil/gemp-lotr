/** Where the legacy login page sends the player once logged in or registered. */
export const HALL_URL = '/gemp-lotr/hall.html';

const HALL_LINK = /^#[A-Za-z0-9_\-/?=&%.+]{1,200}$/;

/**
 * The hall link the visitor came from: the hall's "Click here to register or log in." passes it as the login page's
 * #fragment (e.g. "#events/leagues"). Anything that doesn't look like one is dropped (login.js hallReturnLink).
 */
export function hallReturnLink(hash: string): string {
  return HALL_LINK.test(hash) ? hash : '';
}

/** The page to go to after logging in: the hall, at the link the visitor came from. */
export function loginTarget(hash: string): string {
  return HALL_URL + hallReturnLink(hash);
}
