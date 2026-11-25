import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { eventErrorMessage, historyColumns, historyCount, shownStatus } from './events-messages';

const failure = (status: number, error: unknown = null, headers = new HttpHeaders()) =>
  ApiError.from(new HttpErrorResponse({ status, error, headers }));

describe('events messages', () => {
  it('says what a failed call means (EventDrawer.errorMap)', () => {
    expect(eventErrorMessage(failure(0))).toBe('The server is unreachable or the connection dropped.');
    expect(eventErrorMessage(failure(404))).toBe('It was not found on the server.');
    expect(eventErrorMessage(failure(502))).toBe('The server answered with HTTP 502.');
    expect(eventErrorMessage(failure(400, '{"error":"Bad month"}'))).toBe('The request was rejected: Bad month');
    expect(eventErrorMessage(failure(400, '', new HttpHeaders({ message: 'No' })))).toBe(
      'The request was rejected: No',
    );
    expect(eventErrorMessage(failure(400))).toBe('The request was rejected as malformed.');
    expect(eventErrorMessage(new ApiError(INVALID_RESPONSE_STATUS, 'x'))).toBe(
      "The server's answer could not be read.",
    );
    expect(shownStatus(new ApiError(INVALID_RESPONSE_STATUS, 'x'))).toBe(200);
  });

  it('words the completed-events panels', () => {
    expect(historyCount('league', 'March 2026', 1)).toBe('March 2026: 1 completed league.');
    expect(historyCount('tournament', 'March 2026', 2)).toBe('March 2026: 2 completed tournaments.');
    expect(historyColumns('tournament', true)).toEqual(['', 'Name', 'Date', 'Format', 'Players', 'Rounds', 'Admin']);
    expect(historyColumns('league', false)).toEqual(['', 'Name', 'Dates', 'Format', 'Players']);
  });
});
