import { provideHttpClientTesting, HttpTestingController, TestRequest } from '@angular/common/http/testing';
import { Provider } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideGempHttp } from '../core/api/gemp-interceptors';
import { EVENTS_SERVICES } from '../core/hall/events/events-tab.service';
import { Timers } from '../core/platform/timers';
import { EVENTS_ROUTES } from '../ui/hall/events/events.routes';
import { provideFakeNotifier, settle } from './fake-notifier';
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

/** The Events tab at /events/..., as the router shows it; the hall's hooks for joining are left unprovided. */
export interface EventsPage {
  readonly backend: HttpTestingController;
  readonly harness: RouterTestingHarness;
  /** The page the route shows. */
  page(): HTMLElement;
  /** Answers every request in flight to `path` (without its query) with a saved server response. */
  answer(path: string, file: string, method?: string): void;
  /** Lets the answers land and draws the page. */
  render(): Promise<void>;
  /** Goes to `url` and answers the tab's first requests (lists, calendar, completed events) with the saved ones. */
  open(url: string): Promise<HTMLElement>;
}

export async function setUpEventsPage(extra: Provider[] = []): Promise<EventsPage> {
  TestBed.configureTestingModule({
    providers: [
      provideGempHttp(),
      provideHttpClientTesting(),
      provideFakeNotifier(),
      provideRouter([{ path: 'events', children: EVENTS_ROUTES }]),
      { provide: Timers, useValue: { now: () => Date.UTC(2026, 10, 20, 12), after: setTimeout, clear: clearTimeout } },
      ...extra,
    ],
  });
  const backend = TestBed.inject(HttpTestingController);
  const harness = await RouterTestingHarness.create();
  const page = () => harness.routeNativeElement as HTMLElement;
  const answer = (path: string, file: string, method = 'GET') =>
    backend
      .match((request) => request.url === SERVER + path && request.method === method)
      .forEach((request) => request.flush(readServerResponse(file)));
  const render = async () => {
    await settle();
    harness.detectChanges();
    await harness.fixture.whenStable();
  };
  const open = async (url: string) => {
    await harness.navigateByUrl(url);
    answer('/league', 'leagues.xml');
    answer('/tournament', 'tournaments.xml');
    answer('/calendar', 'events-calendar.json');
    await render();
    answer('/eventHistory/months', 'event-history-months.json');
    await render();
    backend
      .match((request) => request.url === `${SERVER}/eventHistory`)
      .forEach((request) =>
        request.flush(
          readServerResponse(
            request.request.params.get('kind') === 'league'
              ? 'event-history-league.json'
              : 'event-history-tournament.json',
          ),
        ),
      );
    await render();
    return page();
  };
  return { backend, harness, page, answer, render, open };
}
