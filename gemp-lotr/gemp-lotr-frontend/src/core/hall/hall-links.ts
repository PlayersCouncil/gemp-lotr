/**
 * The legacy hall's links (`hall.html#...`, hallLinks.js GempLinks.TABS and ROUTES, shareLinks.js CARD_HASH) as the
 * Angular hall's routes. Server text still writes the legacy links (a patch note's `[the formats](#format-pc_movie)`,
 * an announcement's "Read the full patch notes here"), and so do the share pages (SharePages targets).
 *
 * The routes are the ones proposed for the port (HALL-PORT.md, the workers' brief); a route's own part may still rename
 * its sub-routes. Admin is not ported: its links name no route (the legacy hall's Admin tab stays).
 */

/** A place in the Angular hall: the path under the app (`/help/formats/pc_movie`), its query and fragment. */
export interface HallRoute {
  readonly path: string;
  readonly queryParams: Readonly<Record<string, string>>;
  readonly fragment: string | null;
}

/** Each legacy tab slug (and its aliases) and its sub-tabs' slugs, with the route of each. */
const TABS: readonly {
  readonly slugs: readonly string[];
  readonly path: string | null;
  readonly subs: Readonly<Record<string, string>>;
}[] = [
  { slugs: ['game-hall', 'hall', 'gameHall'], path: '/hall', subs: {} },
  {
    slugs: ['help'],
    path: '/help',
    subs: {
      howtoplay: '/help/how-to-play',
      coc: '/help/conduct',
      formats: '/help/formats',
      leagues: '/help/leagues',
      'pc-errata': '/help/errata',
    },
  },
  {
    slugs: ['events'],
    path: '/events',
    subs: { calendar: '/events/calendar', leagues: '/events/leagues', tournaments: '/events/tournaments' },
  },
  {
    slugs: ['info', 'server-info'],
    path: '/info',
    subs: {
      stats: '/info/stats',
      'patch-notes': '/info/patch-notes',
      contribute: '/info/contribute',
      about: '/info/about',
    },
  },
  {
    slugs: ['account', 'my-account'],
    path: '/account',
    subs: {
      overview: '/account',
      history: '/account/history',
      stats: '/account/stats',
      scenarios: '/account/scenarios',
    },
  },
  // not ported (Users is hidden in the legacy hall too, Admin stays on it)
  { slugs: ['users'], path: null, subs: {} },
  { slugs: ['admin'], path: null, subs: {} },
];

function route(path: string, queryParams: Record<string, string> = {}, fragment: string | null = null): HallRoute {
  return { path, queryParams, fragment };
}

/** "a=1&b=x+y" as query parameters (PcErrata.parseHash: `+` is a space; a malformed pair is skipped). */
function queryOf(query: string | undefined): Record<string, string> {
  const params: Record<string, string> = {};
  for (const pair of (query ?? '').split('&')) {
    if (pair === '') {
      continue;
    }
    const eq = pair.indexOf('=');
    try {
      const key = decodeURIComponent((eq < 0 ? pair : pair.substring(0, eq)).replace(/\+/g, ' '));
      params[key] = decodeURIComponent((eq < 0 ? '' : pair.substring(eq + 1)).replace(/\+/g, ' '));
    } catch {
      // a malformed pair is left out
    }
  }
  return params;
}

/**
 * The route a legacy hall link names (`#format-pc_movie`, with or without its `#`), or null for one the Angular hall
 * has no route for (Admin, Users, an unknown link).
 */
export function hallRouteOf(legacyHash: string): HallRoute | null {
  const hash = legacyHash.startsWith('#') ? legacyHash : `#${legacyHash}`;
  let match = /^#format-([A-Za-z0-9_-]+)$/.exec(hash);
  if (match) {
    return route(`/help/formats/${match[1]}`);
  }
  match = /^#(?:pc-errata|errata-([A-Za-z0-9_]+))(?:\?(.*))?$/.exec(hash);
  if (match) {
    return route(match[1] ? `/help/errata/${match[1]}` : '/help/errata', queryOf(match[2]));
  }
  match = /^#patch-notes(?:\/([A-Za-z0-9_.-]+))?$/.exec(hash);
  if (match) {
    // GempPatchNotes.show: a note's name in lower case
    return route(match[1] ? `/info/patch-notes/${match[1].toLowerCase()}` : '/info/patch-notes');
  }
  if (hash === '#help/manual') {
    return route('/help/how-to-play', {}, 'manual');
  }
  match = /^#league-([A-Za-z0-9_-]+)$/.exec(hash);
  if (match) {
    return route(`/events/leagues/${match[1]}`);
  }
  match = /^#tournament-([A-Za-z0-9_.-]+)$/.exec(hash);
  if (match) {
    return route(`/events/tournaments/${match[1]}`);
  }
  match = /^#card-(\d{1,3}_\d{1,4})$/.exec(hash);
  if (match) {
    // the card display over Help
    return route('/help', { card: match[1] ?? '' });
  }
  match = /^#([A-Za-z-]+)(?:\/([A-Za-z0-9-]+))?\/?$/.exec(hash);
  const slug = match?.[1] ?? '';
  const sub = match?.[2];
  const tab = TABS.find((entry) => entry.slugs.includes(slug));
  if (tab?.path == null) {
    return null;
  }
  // GempLinks.parse: an unknown sub-tab opens the tab
  return route((sub ? tab.subs[sub] : undefined) ?? tab.path);
}
