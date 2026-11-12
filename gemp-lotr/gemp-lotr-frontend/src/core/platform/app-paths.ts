/** Where the Gemp server answers. */
export const SERVER_URL = '/gemp-lotr-server';

/** The site's start page, where players log in. */
export const LOGIN_PAGE_URL = '/gemp-lotr/';

/** Where the site's own images are. */
const IMAGES_URL = '/gemp-lotr/images';

/** The address of one of the site's images, by its path under the images folder. */
export function siteImage(path: string): string {
  return `${IMAGES_URL}/${path}`;
}
