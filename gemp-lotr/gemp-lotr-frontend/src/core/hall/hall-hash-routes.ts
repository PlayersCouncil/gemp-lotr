import { findHallSubTab, findHallTab } from './hall-tabs';

/** The legacy hall, kept for what the Angular hall leaves out (the Admin tab). */
export const LEGACY_HALL_URL = '/gemp-lotr/hall-legacy.html';

/**
 * Where a legacy hall link leads now: a route of the Angular hall (`route`: the router's address, "/help/formats/pc_fotr",
 * with a query and a fragment if any), or a page outside the app (`page`: the legacy hall's Admin tab).
 */
export type HallAddress = { readonly route: string } | { readonly page: string };

/** The errata filters a link may carry (PcErrata.FILTER_KEYS); `card` names the card, as `#errata-<id>` does. */
const ERRATA_KEYS = ['q', 'set', 'format', 'show'];

/**
 * The address of what a legacy hall link's fragment names (hallLinks.js GempLinks.parse and its ROUTES), for the
 * Angular hall's routes; null for a fragment the legacy hall didn't know either. Pure: the hall's answer to another
 * tab's "go" (BroadcastChannel), HallTab.openHere and the page that sends old hall links on use it.
 *
 * | Legacy fragment                                   | Route                                          |
 * | ------------------------------------------------- | ---------------------------------------------- |
 * | none, `#`                                         | `/` (the tab last opened, else the Game Hall)  |
 * | `#game-hall`, `#hall`, `#gameHall`                | `/hall`                                        |
 * | `#help`, `#help/<sub>`                            | `/help`, `/help/<sub's path>`                  |
 * | `#help/manual`                                    | `/help/how-to-play#manual`                     |
 * | `#format-<code>`                                  | `/help/formats/<code>`                         |
 * | `#pc-errata[?k=v&...]`, `#errata-<id>[?k=v&...]`  | `/help/errata[/<id>][?q=&set=&format=&show=]`  |
 * | `#events`, `#events/<sub>`                        | `/events`, `/events/<sub>`                     |
 * | `#league-<code>`, `#tournament-<id>`              | `/events/leagues/<code>`, `/events/tournaments/<id>` |
 * | `#info`, `#server-info`, `#info/<sub>`            | `/info`, `/info/<sub>`                         |
 * | `#patch-notes[/<slug>]`                           | `/info/patch-notes[/<slug>]`                   |
 * | `#account`, `#my-account`, `#account/<sub>`       | `/account`, `/account/<sub's path>`            |
 * | `#users`                                          | `/hall` (the tab is hidden)                    |
 * | `#admin`, `#admin/<sub>`                          | page `LEGACY_HALL_URL#admin[/<sub>]`           |
 *
 * A sub-tab the tab doesn't have opens the tab (as the legacy router did). The sub-tabs' paths are in hall-tabs.ts
 * (`howtoplay` → `how-to-play`, `coc` → `conduct`, `pc-errata` → `errata`, My Account's `overview` → the tab itself).
 */
export function hallAddressOfHash(hash: string): HallAddress | null {
  const fragment = hash.startsWith('#') ? hash : `#${hash}`;
  if (fragment === '#') {
    return { route: '/' };
  }
  const named = namedRoute(fragment);
  if (named !== undefined) {
    return named;
  }
  const parts = /^#([A-Za-z-]+)(?:\/([A-Za-z0-9-]+))?\/?$/.exec(fragment);
  const tab = parts?.[1] === undefined ? null : findHallTab(parts[1]);
  if (!parts || !tab) {
    return null;
  }
  const sub = parts[2] === undefined ? null : findHallSubTab(tab, parts[2]);
  if (tab.slug === 'admin') {
    return { page: `${LEGACY_HALL_URL}#admin${sub ? `/${sub.slug}` : ''}` };
  }
  if (tab.path === null || tab.hidden) {
    return { route: '/hall' };
  }
  return { route: `/${tab.path}${sub && sub.path !== '' ? `/${sub.path}` : ''}` };
}

/** The links to something inside a page (GempLinks.ROUTES); undefined when the fragment is none of them. */
function namedRoute(fragment: string): HallAddress | undefined {
  let match = /^#format-([A-Za-z0-9_-]+)$/.exec(fragment);
  if (match?.[1] !== undefined) {
    return { route: `/help/formats/${match[1]}` };
  }
  match = /^#(?:pc-errata|errata-([A-Za-z0-9_]+))(?:\?(.*))?$/.exec(fragment);
  if (match) {
    return { route: errataRoute(match[1] ?? null, match[2] ?? '') };
  }
  match = /^#patch-notes(?:\/([A-Za-z0-9_.-]+))?$/.exec(fragment);
  if (match) {
    return { route: `/info/patch-notes${match[1] === undefined ? '' : `/${match[1]}`}` };
  }
  if (fragment === '#help/manual') {
    return { route: '/help/how-to-play#manual' };
  }
  match = /^#league-([A-Za-z0-9_-]+)$/.exec(fragment);
  if (match?.[1] !== undefined) {
    return { route: `/events/leagues/${match[1]}` };
  }
  match = /^#tournament-([A-Za-z0-9_.-]+)$/.exec(fragment);
  if (match?.[1] !== undefined) {
    return { route: `/events/tournaments/${encodeURIComponent(match[1])}` };
  }
  return undefined;
}

/** PcErrata.parseHash: the card from the fragment or its `card` parameter, and the filters as query parameters. */
function errataRoute(card: string | null, query: string): string {
  const params = new Map<string, string>();
  for (const pair of query.split('&')) {
    if (pair === '') continue;
    const eq = pair.indexOf('=');
    try {
      const key = decodeURIComponent((eq < 0 ? pair : pair.slice(0, eq)).replace(/\+/g, ' '));
      const value = decodeURIComponent((eq < 0 ? '' : pair.slice(eq + 1)).replace(/\+/g, ' '));
      params.set(key, value);
    } catch {
      // a malformed pair is skipped, as the legacy page did
    }
  }
  const named = card ?? params.get('card') ?? null;
  const id = named !== null && /^[A-Za-z0-9_]+$/.test(named) ? named : null;
  const filters = new URLSearchParams();
  for (const key of ERRATA_KEYS) {
    const value = params.get(key);
    if (value) filters.set(key, value);
  }
  const search = filters.toString();
  return `/help/errata${id === null ? '' : `/${id}`}${search === '' ? '' : `?${search}`}`;
}
