import { HttpErrorResponse } from '@angular/common/http';
import { ApiError, errorMessage, GENERIC_COMMUNICATION_ERROR } from './api-error';

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

describe('ApiError', () => {
  const answered = (error: unknown) => new HttpErrorResponse({ status: 400, error });

  it("keeps what the server said in a JSON body's error, read from the text or already parsed", () => {
    expect(ApiError.from(answered('{"error":"Not that."}')).said).toBe('Not that.');
    expect(ApiError.from(answered({ error: 'Nor that.' })).said).toBe('Nor that.');
  });

  it('says nothing for a body without words in it', () => {
    for (const body of ['', 'oops', '{"error":""}', '{"error":3}', '[1]', null, 'null']) {
      expect(ApiError.from(answered(body)).said).toBeNull();
    }
    expect(ApiError.from(new Error('boom')).said).toBeNull();
    expect(new ApiError(0, 'x').said).toBeNull();
  });
});
