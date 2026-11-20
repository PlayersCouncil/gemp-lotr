import { HallRoute } from '../hall-links';
import { CONDUCT_TAB, ERRATA_TAB, FORMATS_TAB, HOW_TO_PLAY_TAB, LEAGUES_TAB } from './help-messages';

/**
 * Help's sub-tabs as routes (help.html #help-tabs; the legacy slugs of hallLinks.js GempLinks.TABS in `legacySlug`).
 * Help is public: a visitor who is not logged in reads it.
 */
export interface HelpPage {
  readonly slug: string;
  readonly label: string;
  readonly legacySlug: string;
}

export const HELP_PAGES: readonly HelpPage[] = [
  { slug: 'how-to-play', label: HOW_TO_PLAY_TAB, legacySlug: 'howtoplay' },
  { slug: 'conduct', label: CONDUCT_TAB, legacySlug: 'coc' },
  { slug: 'formats', label: FORMATS_TAB, legacySlug: 'formats' },
  { slug: 'leagues', label: LEAGUES_TAB, legacySlug: 'leagues' },
  { slug: 'errata', label: ERRATA_TAB, legacySlug: 'pc-errata' },
];

/** The Manual's place on How Do I Play? (`#help/manual`, which scrolled to `#help-manual`). */
export const MANUAL_FRAGMENT = 'manual';

/** A format on Format Definitions (`#format-<code>`). */
export function formatRoute(code: string): HallRoute {
  return { path: `/help/formats/${code}`, queryParams: {}, fragment: null };
}

/**
 * Help › PC Errata, filtered to a format's errata, or the whole list for null (GempErrataHelp.show({format}); the
 * filter's name is PC Errata's own, `format`).
 */
export function errataRoute(format: string | null): HallRoute {
  return { path: '/help/errata', queryParams: format === null ? {} : { format }, fragment: null };
}
