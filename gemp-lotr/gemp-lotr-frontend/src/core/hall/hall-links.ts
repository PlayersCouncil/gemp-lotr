import { hallAddressOfHash } from './hall-hash-routes';

/**
 * A legacy hall link (`hall.html#...`) as a place the router can go to. Server text still writes the legacy links (a
 * patch note's `[the formats](#format-pc_movie)`, an announcement's "Read the full patch notes here"), and so do the
 * share pages (SharePages targets). Which route each link names is decided in one place, hall-hash-routes.ts
 * (`hallAddressOfHash`); this gives that route in the parts the router takes.
 */

/** A place in the Angular hall: the path under the app (`/help/formats/pc_movie`), its query and fragment. */
export interface HallRoute {
  readonly path: string;
  readonly queryParams: Readonly<Record<string, string>>;
  readonly fragment: string | null;
}

/** A route as written (`/help/errata/1_5?q=x#top`) in its parts. */
export function routeParts(route: string): HallRoute {
  const hashAt = route.indexOf('#');
  const fragment = hashAt < 0 ? null : route.slice(hashAt + 1);
  const rest = hashAt < 0 ? route : route.slice(0, hashAt);
  const queryAt = rest.indexOf('?');
  const queryParams: Record<string, string> = {};
  if (queryAt >= 0) {
    for (const [key, value] of new URLSearchParams(rest.slice(queryAt + 1))) {
      queryParams[key] = value;
    }
  }
  return { path: queryAt < 0 ? rest : rest.slice(0, queryAt), queryParams, fragment };
}

/**
 * The route a legacy hall link names (`#format-pc_movie`, with or without its `#`), or null for one the Angular hall
 * has no route for (Admin, which stays on the legacy hall, or an unknown link).
 */
export function hallRouteOf(legacyHash: string): HallRoute | null {
  const hash = legacyHash.startsWith('#') ? legacyHash : `#${legacyHash}`;
  if (hash === '#') {
    return null;
  }
  const address = hallAddressOfHash(hash);
  return address !== null && 'route' in address ? routeParts(address.route) : null;
}
