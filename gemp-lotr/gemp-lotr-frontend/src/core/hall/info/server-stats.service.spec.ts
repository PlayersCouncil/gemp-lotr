import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { Notifier } from '../../notifier/notifier';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { Timers } from '../../platform/timers';
import { FakeNotifier, provideFakeNotifier } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { INFO_SERVICES } from './info-services';
import { ServerStatsService } from './server-stats.service';

const URL = '/gemp-lotr-server/stats';
const NOW = new Date(2026, 10, 20, 12).getTime();

describe('ServerStatsService', () => {
  let backend: HttpTestingController;
  let notifier: FakeNotifier;
  let service: ServerStatsService;

  async function answer(startDay: string, length: string, body = readServerResponse('server-stats.json')) {
    const request = backend.expectOne((req) => req.url === URL);
    expect(request.request.params.get('startDay')).toBe(startDay);
    expect(request.request.params.get('length')).toBe(length);
    request.flush(body);
    await Promise.resolve();
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        ...INFO_SERVICES,
        provideFakeNotifier(),
        { provide: Timers, useValue: { now: () => NOW, after: () => null, clear: () => undefined } },
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    notifier = TestBed.inject(Notifier) as FakeNotifier;
    service = TestBed.inject(ServerStatsService);
  });

  afterEach(() => backend.verify());

  it('opens on the month before today', async () => {
    expect(service.startDay()).toBe('2026-10-20');
    expect(service.period()).toBe('month');
    expect(service.tables()).toBeNull();
    const done = service.display();
    await answer('2026-10-20', 'month');
    await done;
    expect(service.shown()).toEqual({ startDay: '2026-10-20', period: 'month' });
    expect(service.stats()?.activePlayers).toBe(34);
    expect(service.tables()?.competitive.rows).toHaveLength(2);
  });

  it('says "Invalid parameter entered" to a start day the server cannot read, and nothing to other failures', async () => {
    service.startDay.set('soon');
    service.period.set('week');
    let done = service.display();
    backend.expectOne((req) => req.url === URL).flush('', { status: 400, statusText: 'Bad Request' });
    await done;
    expect(notifier.dialogs).toEqual(['alert: Invalid parameter entered']);
    done = service.display();
    backend.expectOne((req) => req.url === URL).flush('', { status: 500, statusText: 'Error' });
    await done;
    expect(notifier.dialogs).toHaveLength(1);
    expect(service.shown()).toBeNull();
  });

  it('sorts each table on its own; a refresh keeps the sorts and the dates shown, Display Stats resets them', async () => {
    let done = service.display();
    await answer('2026-10-20', 'month');
    await done;
    service.sortBy('casual', 'Format');
    expect(service.tables()?.casual.sort).toEqual({ key: 'Format', dir: 'asc' });
    service.sortBy('casual', 'Format');
    expect(service.tables()?.casual.sort).toEqual({ key: 'Format', dir: 'desc' });
    service.sortBy('competitive', 'Count');
    expect(service.tables()?.competitive.sort).toEqual({ key: 'Count', dir: 'asc' });

    service.startDay.set('2026-1-1');
    done = service.refresh();
    await answer('2026-10-20', 'month');
    await done;
    expect(service.tables()?.casual.sort).toEqual({ key: 'Format', dir: 'desc' });

    done = service.refresh();
    backend.expectOne((req) => req.url === URL).error(new ProgressEvent('error'));
    await done;
    expect(service.stats()?.gamesCount).toBe(107);
    expect(notifier.dialogs).toEqual([]);

    done = service.display();
    await answer('2026-1-1', 'month');
    await done;
    expect(service.tables()?.casual.sort).toEqual({ key: 'Count', dir: 'desc' });
  });

  it('refreshes nothing before the first stats', async () => {
    await service.refresh();
    backend.expectNone(URL);
  });

  it('keeps only the latest answer when Display Stats is pressed again', async () => {
    const first = service.display();
    const stale = backend.expectOne((req) => req.url === URL);
    service.period.set('day');
    const second = service.display();
    await answer('2026-10-20', 'day');
    await second;
    stale.flush(JSON.stringify({ ActivePlayers: 99 }));
    await first;
    expect(service.shown()).toEqual({ startDay: '2026-10-20', period: 'day' });
    expect(service.stats()?.activePlayers).toBe(34);
  });

  it('sorts nothing before the first stats', () => {
    service.sortBy('casual', 'Format');
    expect(service.sorts()).toEqual({ competitive: null, casual: null });
  });
});
