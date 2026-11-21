import { Route, Routes, UrlMatchResult, UrlSegment } from '@angular/router';
import { ERRATA_SERVICES } from '../../../../core/hall/help/errata/errata-services';
import { ShareLinks } from '../../../../core/hall/share-links';
import { ERRATA_CARD_PARAM } from './errata-route-param';

/**
 * `errata` and `errata/<cardId>` as one route, so moving from one card's address to another's (as the address follows
 * the viewer) keeps the page as it is instead of drawing it again.
 */
export function errataMatcher(segments: UrlSegment[]): UrlMatchResult | null {
  const [first, card] = segments;
  if (first?.path !== 'errata' || segments.length > 2) {
    return null;
  }
  return card ? { consumed: segments, posParams: { [ERRATA_CARD_PARAM]: card } } : { consumed: segments };
}

/**
 * Help › PC Errata's routes, for Help's routes file (help/errata[/<cardId>], the filters as query parameters: `q`,
 * `set`, `format`, `show`, and `sort`/`dir` from a link). Its state lives as long as the page (route providers), so
 * a later visit finds it as it was left. ShareLinks is Help's (HELP_SERVICES) too: listed here so the page works
 * wherever it is mounted.
 */
export const ERRATA_ROUTE: Route = {
  matcher: errataMatcher,
  providers: [...ERRATA_SERVICES, ShareLinks],
  loadComponent: () => import('./errata-page.component').then((m) => m.ErrataPageComponent),
};

export const ERRATA_ROUTES: Routes = [ERRATA_ROUTE];
