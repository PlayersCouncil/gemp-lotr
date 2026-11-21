import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { Component, Provider } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideGempHttp } from '../core/api/gemp-interceptors';
import { ACCOUNT_SERVICES } from '../core/hall/account/account-tab.service';
import { HALL_SERVICES } from '../core/hall/hall-services';
import { LocalSettings } from '../core/platform/local-settings.service';
import { PageNavigation, ReservedTab } from '../core/platform/page-navigation';
import { Timers } from '../core/platform/timers';
import { provideFakeNotifier } from './fake-notifier';
import { readServerResponse } from './files';

/** A tab reserved on a click, as a spec sees it: where it was sent, and whether it was closed. */
export interface FakeTab extends ReservedTab {
  url: string | null;
  closed: boolean;
}

/** PageNavigation for the specs: tabs are given (or refused, `blockTabs`) and recorded; nothing leaves the page. */
export class FakeNavigation {
  readonly tabs: FakeTab[] = [];
  reloads = 0;
  blockTabs = false;

  reserveTab(): ReservedTab | null {
    if (this.blockTabs) {
      return null;
    }
    const tab: FakeTab = {
      url: null,
      closed: false,
      go(url: string) {
        tab.url = url;
        return !tab.closed;
      },
      close() {
        tab.closed = true;
      },
    };
    this.tabs.push(tab);
    return tab;
  }

  reload(): void {
    this.reloads++;
  }

  go(): void {
    // not used
  }

  openInNewTab(): boolean {
    return false;
  }
}

/** The My Account tab's services, under the hall's, against a fake server, with a clock the spec sets. */
export function setUpAccount(extra: Provider[] = []): {
  backend: HttpTestingController;
  clock: { now: number };
  navigation: FakeNavigation;
} {
  const clock = { now: Date.UTC(2026, 10, 20, 12) };
  const navigation = new FakeNavigation();
  TestBed.configureTestingModule({
    providers: [
      provideGempHttp(),
      provideHttpClientTesting(),
      provideFakeNotifier(),
      { provide: Timers, useValue: { now: () => clock.now, after: setTimeout, clear: clearTimeout } },
      { provide: PageNavigation, useValue: navigation },
      ...HALL_SERVICES,
      ...ACCOUNT_SERVICES,
      ...extra,
    ],
  });
  return { backend: TestBed.inject(HttpTestingController), clock, navigation };
}

const SERVER = '/gemp-lotr-server';

/** The one request in flight to `path` (without its query), by method. */
export function accountRequest(backend: HttpTestingController, path: string, method = 'GET'): TestRequest {
  return backend.expectOne((request) => request.url === SERVER + path && request.method === method);
}

/** Answers the request in flight to `path` with a saved server response. */
export function answerAccount(backend: HttpTestingController, path: string, file: string, method = 'GET'): TestRequest {
  const request = accountRequest(backend, path, method);
  request.flush(readServerResponse(file));
  return request;
}

/** Fails the request in flight to `path` with a status (and a body, e.g. the shelf's `{error}`). */
export function failAccount(
  backend: HttpTestingController,
  path: string,
  status: number,
  method = 'GET',
  body = '',
  headers: Record<string, string> = {},
): void {
  accountRequest(backend, path, method).flush(body, { status, statusText: 'Error', headers });
}

/**
 * The My Account tab's pages under a router (`/account/...` with ACCOUNT_ROUTES, and an empty `/hall`), the hall's
 * services, a fake server, localStorage in `stored`; open an address with `harness.navigateByUrl`.
 */
export async function setUpAccountPages(stored = new Map<string, string>()): Promise<{
  backend: HttpTestingController;
  navigation: FakeNavigation;
  harness: RouterTestingHarness;
  stored: Map<string, string>;
}> {
  const { ACCOUNT_ROUTES } = await import('../ui/hall/account/account.routes');
  const navigation = new FakeNavigation();
  TestBed.configureTestingModule({
    providers: [
      provideGempHttp(),
      provideHttpClientTesting(),
      provideFakeNotifier(),
      { provide: PageNavigation, useValue: navigation },
      {
        provide: LocalSettings,
        useValue: { read: (key: string) => stored.get(key) ?? null, write: (k: string, v: string) => stored.set(k, v) },
      },
      ...HALL_SERVICES,
      provideRouter([
        { path: 'hall', component: EmptyPage },
        { path: 'account', children: ACCOUNT_ROUTES },
      ]),
    ],
  });
  const harness = await RouterTestingHarness.create();
  return { backend: TestBed.inject(HttpTestingController), navigation, harness, stored };
}

@Component({ template: '' })
class EmptyPage {}

/** Lets the pages' requests go out and their answers be drawn. */
export async function drawn(harness: RouterTestingHarness): Promise<void> {
  for (let i = 0; i < 5; i++) {
    await Promise.resolve();
  }
  await harness.fixture.whenStable();
}
