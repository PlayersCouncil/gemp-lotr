import { provideHttpClientTesting, HttpTestingController, TestRequest } from '@angular/common/http/testing';
import { Provider } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../core/api/gemp-interceptors';
import { EVENTS_SERVICES } from '../core/hall/events/events-tab.service';
import { Timers } from '../core/platform/timers';
import { provideFakeNotifier } from './fake-notifier';
import { readServerResponse } from './files';

/** The Events tab's services against a fake server, with a clock the spec sets (`clock.now`). */
export function setUpEvents(extra: Provider[] = []): { backend: HttpTestingController; clock: { now: number } } {
  const clock = { now: Date.UTC(2026, 10, 20, 12) };
  TestBed.configureTestingModule({
    providers: [
      provideGempHttp(),
      provideHttpClientTesting(),
      provideFakeNotifier(),
      { provide: Timers, useValue: { now: () => clock.now, after: setTimeout, clear: clearTimeout } },
      ...EVENTS_SERVICES,
      ...extra,
    ],
  });
  return { backend: TestBed.inject(HttpTestingController), clock };
}

const SERVER = '/gemp-lotr-server';

/** The one request in flight to `path` (without its query), by method. */
export function eventsRequest(backend: HttpTestingController, path: string, method = 'GET'): TestRequest {
  return backend.expectOne((request) => request.url === SERVER + path && request.method === method);
}

/** Answers the request in flight to `path` with a saved server response. */
export function answerWith(backend: HttpTestingController, path: string, file: string): TestRequest {
  const request = eventsRequest(backend, path);
  request.flush(readServerResponse(file));
  return request;
}

/** Answers the request in flight to `path` with an error status. */
export function failWith(backend: HttpTestingController, path: string, status: number, method = 'GET'): void {
  eventsRequest(backend, path, method).flush('', { status, statusText: 'Error' });
}
