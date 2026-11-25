import { DOCUMENT } from '@angular/common';
import { inject, InjectionToken } from '@angular/core';

/** The page's query string (e.g. "?gameId=7"); a token so tests can supply their own. */
export const PAGE_QUERY_STRING = new InjectionToken<string>('PAGE_QUERY_STRING', {
  providedIn: 'root',
  factory: () => inject(DOCUMENT).defaultView?.location.search ?? '',
});

/** The page's path (e.g. "/gemp-lotr/ng/game"); a token so tests can supply their own. */
export const PAGE_PATH = new InjectionToken<string>('PAGE_PATH', {
  providedIn: 'root',
  factory: () => inject(DOCUMENT).defaultView?.location.pathname ?? '',
});

/** The page's origin (e.g. "https://play.lotrtcgpc.net"), for links to copy; a token so tests can supply their own. */
export const PAGE_ORIGIN = new InjectionToken<string>('PAGE_ORIGIN', {
  providedIn: 'root',
  factory: () => {
    const location = inject(DOCUMENT).defaultView?.location;
    return location ? location.origin || `${location.protocol}//${location.host}` : '';
  },
});
