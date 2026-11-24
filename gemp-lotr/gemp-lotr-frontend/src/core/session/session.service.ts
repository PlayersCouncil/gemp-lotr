import { inject, Injectable } from '@angular/core';
import { PAGE_QUERY_STRING } from '../platform/page-location';

/**
 * The page's ?participantId=, if any. Authentication is the login cookie, sent automatically because the app is
 * served from the same origin as the server. The parameter used to let an admin act as that player; since November
 * 2026 the server ignores it (LotroServerRequestHandler.getResourceOwnerSafely: the player is the session's, always).
 * What still reads it (the participant interceptor, game page addresses) is left for removal once the login page's
 * rewrite has landed, because the login specs still expect it.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  /**
   * Decoded value of the participantId query parameter, or null. (The legacy client passed the still-encoded value
   * on, so names with special characters were encoded twice; this one is decoded once.)
   */
  readonly participantId: string | null = SessionService.readParticipantId(inject(PAGE_QUERY_STRING));

  private static readParticipantId(queryString: string): string | null {
    const value = new URLSearchParams(queryString).get('participantId');
    return value === '' ? null : value;
  }
}
