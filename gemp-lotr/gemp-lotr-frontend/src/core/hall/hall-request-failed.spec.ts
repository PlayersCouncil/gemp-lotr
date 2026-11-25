import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { ApiError, INVALID_RESPONSE_STATUS } from '../api/api-error';
import { FakeNotifier } from '../../testing/fake-notifier';
import { hallRequestFailed, messageHeader } from './hall-request-failed';

describe('hall request failures', () => {
  it("words a side request's failure as hallUi.js requestFailed did", () => {
    const notifier = new FakeNotifier();
    let logins = 0;
    const targets = { requireLogin: () => logins++, notifier };
    hallRequestFailed(new ApiError(401, 'x'), targets);
    expect(logins).toBe(1);
    for (const status of [0, 504, INVALID_RESPONSE_STATUS]) {
      hallRequestFailed(new ApiError(status, 'x'), targets);
    }
    expect(notifier.dialogs).toEqual([]);
    hallRequestFailed(new ApiError(503, 'x'), targets);
    expect(notifier.dialogs).toEqual([
      'alert: The server could not complete that request (error 503). Try again; if it keeps happening, reload the page.',
    ]);
  });

  it("reads a refused request's message header, when it says something", () => {
    const answer = (message: string | null) =>
      new HttpErrorResponse({
        status: 400,
        headers: message === null ? new HttpHeaders() : new HttpHeaders({ message }),
      });
    expect(messageHeader(answer('Invalid filter'))).toBe('Invalid filter');
    expect(messageHeader(answer(''))).toBeNull();
    expect(messageHeader(answer(null))).toBeNull();
    expect(messageHeader(null)).toBeNull();
  });
});
