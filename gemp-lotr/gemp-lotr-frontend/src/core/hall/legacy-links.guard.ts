import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { PageNavigation } from '../platform/page-navigation';
import { PlayerInfo } from '../session/player-info.service';
import { hallAddressOfHash } from './hall-hash-routes';

/**
 * The old addresses. `hall.html` and the site's start page (`/gemp-lotr/`, `index.html`) are now two small pages that
 * send the browser on to the app, to the routes `go` and `start`, keeping the link's `#fragment`: a fragment never
 * reaches the server, so only a page can pass it on, and which route a legacy link names is decided here, in one
 * place (hallAddressOfHash), not in those pages.
 */

/** Where a legacy `#fragment` leads: a route of the app, or another page (Admin, on the legacy hall). */
function targetOf(fragment: string | null): { route: string } | { page: string } {
  const address = fragment === null || fragment === '' ? null : hallAddressOfHash(`#${fragment}`);
  // A link the hall doesn't know opens the hall, as the legacy hall did
  return address ?? { route: '/' };
}

/**
 * `go#<legacy fragment>`: what `hall.html#...` leads to. Like the legacy hall, it asks for no login itself: the tab
 * it opens does if it needs one.
 */
export const legacyHallLink: CanActivateFn = (_route, state): UrlTree | boolean => {
  const router = inject(Router);
  const target = targetOf(router.parseUrl(state.url).fragment);
  if ('page' in target) {
    inject(PageNavigation).go(target.page);
    return false;
  }
  return router.parseUrl(target.route);
};

/**
 * `start#<legacy fragment>`: what the site's start page leads to. A logged-in player goes to the hall (to the place
 * the fragment names, as the legacy login page did after logging in); anyone else to the login page, which comes back
 * to that place. If the server doesn't say who is there, the hall opens and reports the trouble itself.
 */
export const startPage: CanActivateFn = async (_route, state): Promise<UrlTree | boolean> => {
  const router = inject(Router);
  const players = inject(PlayerInfo);
  const navigation = inject(PageNavigation);
  const target = targetOf(router.parseUrl(state.url).fragment);
  let loggedIn = true;
  try {
    loggedIn = (await players.whoAmI()) !== null;
  } catch {
    // not the login's doing: the hall says what is wrong
  }
  if (!loggedIn) {
    const back = 'route' in target && target.route !== '/' ? target.route : null;
    return router.createUrlTree(['/login'], { queryParams: back === null ? {} : { return: back } });
  }
  if ('page' in target) {
    navigation.go(target.page);
    return false;
  }
  return router.parseUrl(target.route);
};
