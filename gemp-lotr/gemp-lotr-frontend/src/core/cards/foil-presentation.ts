import { readCookie } from '../platform/cookies';

/**
 * How foil cards are drawn (Card.getFoilPresentation), from the "foilPresentation" cookie the game settings write:
 * "static" (a holo layer, the default), "animated" (foil.gif) or "none". Cookies from before 2025 stored true/false.
 * The legacy page also wrote the default back into the cookie; this only reads it, so it never creates a cookie with
 * this page's path.
 */
export type FoilPresentation = 'static' | 'animated' | 'none';

export function readFoilPresentation(cookies: string): FoilPresentation {
  const raw = readCookie(cookies, 'foilPresentation');
  if (raw === null) {
    return 'static';
  }
  if (raw === 'static' || raw === 'animated' || raw === 'none') {
    return raw;
  }
  return raw === 'true' ? 'animated' : 'static';
}

/** The image drawn over a foil card, or null when foils are shown plain. */
export function foilOverlayImage(presentation: FoilPresentation): string | null {
  if (presentation === 'none') {
    return null;
  }
  return `/gemp-lotr/images/${presentation === 'animated' ? 'foil.gif' : 'holo.jpg'}`;
}
