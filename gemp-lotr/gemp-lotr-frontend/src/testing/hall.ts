import { HttpParams } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { Provider, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../core/api/gemp-interceptors';
import { HallComms, NEXT_POLL_DELAY_MS } from '../core/hall/hall-comms.service';
import { HALL_SERVICES } from '../core/hall/hall-services';
import { LegacyCookies } from '../core/platform/legacy-cookies';
import { PAGE_QUERY_STRING } from '../core/platform/page-location';
import { PageNavigation, ReservedTab } from '../core/platform/page-navigation';
import { SoundPlayer } from '../core/platform/sound-player';
import { PlayerInfo } from '../core/session/player-info.service';
import { provideFakeNotifier } from './fake-notifier';
import { readServerResponse } from './files';

export const SERVER = '/gemp-lotr-server';

/** The legacy cookies, in memory. */
export class FakeCookies {
  readonly values = new Map<string, string>();
  read(name: string): string | null {
    return this.values.get(name) ?? null;
  }
  write(name: string, value: string): void {
    this.values.set(name, value);
  }
}

/** New tabs and reserved tabs, recorded; `blockPopups` makes the browser refuse them. */
export class FakeNavigation {
  readonly opened: string[] = [];
  readonly reserved: { url: string | null; closed: boolean }[] = [];
  blockPopups = false;
  go(): void {
    // not followed in tests
  }
  openInNewTab(url: string): boolean {
    this.opened.push(url);
    return !this.blockPopups;
  }
  reserveTab(): ReservedTab | null {
    if (this.blockPopups) {
      return null;
    }
    const tab: { url: string | null; closed: boolean } = { url: null, closed: false };
    this.reserved.push(tab);
    return {
      go: (url) => {
        tab.url = url;
        return !tab.closed;
      },
      close: () => {
        tab.closed = true;
      },
    };
  }
}

/** The viewer, without asking GET /player. */
export class FakePlayerInfo {
  readonly player = signal<{ name: string; type: string } | null>({ name: 'alice', type: 'u' });
  whoAmI() {
    return Promise.resolve(this.player());
  }
}

/** The hall test bed: the hall's services over the testing backend, as player "alice" (?participantId=alice). */
export interface HallTestBed {
  readonly backend: HttpTestingController;
  readonly comms: HallComms;
  readonly cookies: FakeCookies;
  readonly navigation: FakeNavigation;
  readonly sounds: string[];
  /** The one request to `path` (`method`, POST by default). */
  request(path: string, method?: string): TestRequest;
  /** A form request's fields. */
  body(req: TestRequest): Record<string, string | null>;
  /** Signs up with `file` (a GET /hall answer) and leaves the first update poll open. */
  connect(file?: string): void;
  /** Answers the open update poll with `file`. */
  poll(file: string): void;
  /** Stops the poll and checks no request was left (the long-poll's last one is). */
  finish(): void;
}

export function setUpHall(extra: Provider[] = []): HallTestBed {
  const cookies = new FakeCookies();
  const navigation = new FakeNavigation();
  const sounds: string[] = [];
  TestBed.configureTestingModule({
    providers: [
      provideGempHttp(),
      provideHttpClientTesting(),
      ...HALL_SERVICES,
      ...provideFakeNotifier(),
      { provide: PAGE_QUERY_STRING, useValue: '?participantId=alice' },
      { provide: LegacyCookies, useValue: cookies },
      { provide: PageNavigation, useValue: navigation },
      { provide: SoundPlayer, useValue: { play: (url: string) => sounds.push(url) } },
      { provide: PlayerInfo, useClass: FakePlayerInfo },
      ...extra,
    ],
  });
  const backend = TestBed.inject(HttpTestingController);
  const comms = TestBed.inject(HallComms);
  const request = (path: string, method = 'POST') =>
    backend.expectOne((r) => r.url === SERVER + path && r.method === method);
  return {
    backend,
    comms,
    cookies,
    navigation,
    sounds,
    request,
    body: (req) => {
      const params = req.request.body as HttpParams;
      return Object.fromEntries(params.keys().map((key) => [key, params.get(key)]));
    },
    connect: (file = 'hall-queues.xml') => {
      comms.start();
      request('/hall', 'GET').flush(readServerResponse(file));
      vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
    },
    poll: (file) => {
      request('/hall/update').flush(file.startsWith('<') ? file : readServerResponse(file));
      vi.advanceTimersByTime(NEXT_POLL_DELAY_MS);
    },
    finish: () => {
      comms.stop();
      backend.match((r) => r.url === `${SERVER}/hall/update`);
      backend.verify();
    },
  };
}
