import { HttpParams } from '@angular/common/http';
import { HttpTestingController, TestRequest } from '@angular/common/http/testing';
import { parseJson } from '../core/api/json';
import { sentForm } from './sent-form';

/**
 * The authoring panel's calls as a spec meets them at the test backend: one place for where they go and how they
 * are picked out, for the specs of the panel's services and of its tabs.
 */

/** Where the authoring calls go (AuthoringApi): `AUTHOR + 'edit'`, `AUTHOR + 'do'`, `AUTHOR + 'state'`... */
export const AUTHOR = '/gemp-lotr-server/scenarios/author/';

/** The calls of `what` on their way. They are taken from the backend, as HttpTestingController.match takes them. */
export function authorCalls(backend: HttpTestingController, what: string, method = 'POST'): TestRequest[] {
  return backend.match((r) => r.url === AUTHOR + what && r.method === method);
}

/** The one call of `what` on its way. None, or several, fails the test. */
export function authorCall(backend: HttpTestingController, what: string, method = 'POST'): TestRequest {
  const requests = authorCalls(backend, what, method);
  expect(requests, what).toHaveLength(1);
  return requests[0]!;
}

/** The form the one call of `what` on its way sent. No such call, several, or a body that is no form fails the test. */
export function authorForm(backend: HttpTestingController, what: string): HttpParams {
  const { request } = authorCall(backend, what);
  expect(request.body as unknown).toBeInstanceOf(HttpParams);
  return sentForm(request);
}

/** The patch an `edit` sent, still to be given its shape. */
export function sentPatch(request: TestRequest): unknown {
  return parseJson(sentForm(request.request).get('patch') ?? '');
}

/** Answers a call with the body as the server sends it (JSON text), or refuses it (400) with the server's words. */
export function answerCall(request: TestRequest, body: unknown, error: string | null = null): void {
  if (error === null) request.flush(JSON.stringify(body));
  else request.flush(JSON.stringify({ error }), { status: 400, statusText: 'No' });
}
