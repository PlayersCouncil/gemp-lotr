import { HallRoute } from '../hall-links';
import { ABOUT_TAB, CONTRIBUTE_TAB, PATCH_NOTES_TAB, STATS_TAB } from './info-messages';

/**
 * Server Info's sub-tabs as routes (info.html #info-tabs; the legacy slugs of hallLinks.js GempLinks.TABS are the
 * same). Server Info is public: a visitor who is not logged in reads it.
 */
export interface InfoPage {
  readonly slug: string;
  readonly label: string;
}

export const INFO_PAGES: readonly InfoPage[] = [
  { slug: 'stats', label: STATS_TAB },
  { slug: 'patch-notes', label: PATCH_NOTES_TAB },
  { slug: 'contribute', label: CONTRIBUTE_TAB },
  { slug: 'about', label: ABOUT_TAB },
];

/** The patch notes from an update on (`#patch-notes/<slug>`), or the newest (null). */
export function patchNotesRoute(slug: string | null): HallRoute {
  return { path: slug ? `/info/patch-notes/${slug}` : '/info/patch-notes', queryParams: {}, fragment: null };
}
