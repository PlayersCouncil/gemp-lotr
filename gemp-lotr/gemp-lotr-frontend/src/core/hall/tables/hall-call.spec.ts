import { HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { ApiError } from '../../api/api-error';
import { jsonCallResult, tableCallFailure } from './hall-call';

const failure = (status: number, error: unknown = null, headers = new HttpHeaders()) =>
  tableCallFailure(ApiError.from(new HttpErrorResponse({ status, error, headers })));

describe('the hall calls outcome', () => {
  it('reads a JSON answer as TableFlow.readResponse did', () => {
    expect(jsonCallResult({ error: 'No' })).toEqual({ ok: false, message: 'No' });
    expect(jsonCallResult({ response: 'Fine' })).toEqual({ ok: true, message: 'Fine' });
    expect(jsonCallResult('OK')).toEqual({ ok: true, message: 'OK' });
    expect(jsonCallResult(null)).toEqual({ ok: true, message: 'OK' });
  });

  it("words HTTP failures, with a 400's own message", () => {
    expect(failure(400, { error: 'Bad deck' })).toEqual({ ok: false, message: 'Bad deck', status: 400 });
    expect(failure(400, null, new HttpHeaders({ message: 'From header' })).message).toBe('From header');
    expect(failure(400).message).toBe('The server rejected the request as malformed.');
    expect(failure(418).message).toBe('The server answered with an error (HTTP 418).');
  });

  it("reads a 400's message from a JSON body sent as text, and falls back when the text is not JSON", () => {
    expect(failure(400, '{"error":"Bad deck"}').message).toBe('Bad deck');
    expect(failure(400, 'not json', new HttpHeaders({ message: 'From header' })).message).toBe('From header');
    expect(failure(400, 'not json').message).toBe('The server rejected the request as malformed.');
  });

  it('words a failure with no answer behind it (no cause) by its status alone', () => {
    expect(tableCallFailure(new ApiError(400, 'x'))).toEqual({
      ok: false,
      message: 'The server rejected the request as malformed.',
      status: 400,
    });
    expect(tableCallFailure(new ApiError(-1, 'x')).status).toBe(0);
  });
});
