import { inject, Injectable } from '@angular/core';
import { PAGE_QUERY_STRING } from '../platform/page-location';

/**
 * Who the deck builder acts for. Authentication itself is the login cookie, sent automatically because the app is
 * served from the same origin as the server. Admins can open the page with ?participantId=<player> to act as that
 * player (LotroServerRequestHandler.getResourceOwnerSafely); everyone else simply has no participantId.
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
    return value ? value : null;
  }
}
