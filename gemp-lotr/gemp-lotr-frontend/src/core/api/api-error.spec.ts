import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage, GENERIC_COMMUNICATION_ERROR } from './api-error';

const failure = (status: number) => new HttpErrorResponse({ status });

describe('errorMessage', () => {
  it('uses the message for that status', () => {
    expect(errorMessage(failure(404), { 404: 'Not there.', 400: 'Bad.' })).toBe('Not there.');
  });

  it('falls back to the generic message for other statuses', () => {
    expect(errorMessage(failure(500), { 404: 'Not there.' })).toBe(GENERIC_COMMUNICATION_ERROR);
  });

  it('falls back to the generic message without any table, and for things that are not HTTP errors', () => {
    expect(errorMessage(failure(400))).toBe(GENERIC_COMMUNICATION_ERROR);
    expect(errorMessage(new Error('boom'), { 400: 'Bad.' })).toBe(GENERIC_COMMUNICATION_ERROR);
  });
});
