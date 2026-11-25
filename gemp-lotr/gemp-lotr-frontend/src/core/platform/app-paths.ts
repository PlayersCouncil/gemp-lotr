/** Where the Gemp server answers. */
export const SERVER_URL = '/gemp-lotr-server';

/** The site's start page, where players log in. */
export const LOGIN_PAGE_URL = '/gemp-lotr/';

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

/** Where the site's own images are. */
const IMAGES_URL = '/gemp-lotr/images';

/** The address of one of the site's images, by its path under the images folder. */
export function siteImage(path: string): string {
  return `${IMAGES_URL}/${path}`;
}
