import { HttpParams, HttpRequest } from '@angular/common/http';
import { HttpTestingController } from '@angular/common/http/testing';

/**
 * The form a request sent: the Gemp client sends HttpParams (form-codec). A test request's body is typed any, so it is
 * read through here, as what it is.
 */
export function sentForm(request: HttpRequest<unknown>): HttpParams {
  const body = request.body as unknown;
  return body instanceof HttpParams ? body : new HttpParams();
}

/** The decision value the page sent to the game's address (its first answer), or null while it sent none. */
export function sentDecision(backend: HttpTestingController, url: string): string | null {
  const sent = backend.match((r) => r.url === url && r.method === 'POST' && sentForm(r).has('decisionId'));
  return sent[0] ? sentForm(sent[0].request).get('decisionValue') : null;
}
