import { DOCUMENT } from '@angular/common';
import { inject, InjectionToken } from '@angular/core';

/** The page's query string (e.g. "?participantId=bob"); a token so tests can supply their own. */
export const PAGE_QUERY_STRING = new InjectionToken<string>('PAGE_QUERY_STRING', {
  providedIn: 'root',
  factory: () => inject(DOCUMENT).defaultView?.location.search ?? '',
});
