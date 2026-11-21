import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { Notifier } from '../../../core/notifier/notifier';
import { FakeNotifier, provideFakeNotifier } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { Timers } from '../../../core/platform/timers';
import { INFO_ROUTES } from './info.routes';

describe('ServerStatsComponent', () => {
  let backend: HttpTestingController;

  async function open() {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        provideRouter([{ path: 'info', children: INFO_ROUTES }]),
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/info/stats');
    backend = TestBed.inject(HttpTestingController);
    return { harness, el: harness.routeNativeElement as HTMLElement };
  }

  async function answer(
    harness: RouterTestingHarness,
    body: string | number = readServerResponse('server-stats.json'),
  ) {
    const request = backend.expectOne((req) => req.url.endsWith('/stats'));
    if (typeof body === 'number') request.flush('', { status: body, statusText: 'x' });
    else request.flush(body);
    await harness.fixture.whenStable();
    harness.detectChanges();
    return request;
  }

  it('shows the stats of a month ago as it opens: the figures and the two tables, the most games first', async () => {
    const { harness, el } = await open();
    const request = await answer(harness);
    expect(request.request.params.get('length')).toBe('month');
    expect(el.querySelector('.gd-stats__period-title')?.textContent?.replace(/\s+/g, ' ').trim()).toBe(
      'Stats for 2026-10-20 - 2026-11-20',
    );
    expect(el.querySelector('[data-figure="bot-games"]')?.textContent?.replace(/\s+/g, ' ').trim()).toBe(
      'Bot games count: 7 (Not counted in the below stats)',
    );
    const tables = el.querySelectorAll('table.gd-stats__table');
    expect(Array.from(tables[0]!.querySelectorAll('th')).map((th) => th.textContent?.trim())).toEqual([
      'Format name',
      '# of games ▼',
      '% of competitive',
      '% of total',
    ]);
    expect(Array.from(tables[1]!.querySelectorAll('tbody tr')).map((tr) => tr.firstElementChild?.textContent)).toEqual([
      'Fellowship Block (PC)',
      'Expanded',
    ]);
  });

  it('sorts a table by a press on its header', async () => {
    const { harness, el } = await open();
    await answer(harness);
    const casual = el.querySelectorAll('table.gd-stats__table')[1]!;
    casual.querySelector<HTMLButtonElement>('th button')!.click();
    harness.detectChanges();
    expect(casual.querySelector('th')?.getAttribute('aria-sort')).toBe('ascending');
    expect(Array.from(casual.querySelectorAll('tbody tr')).map((tr) => tr.firstElementChild?.textContent)).toEqual([
      'Expanded',
      'Fellowship Block (PC)',
    ]);
  });

  it('asks for the dates typed, and says so when the server cannot read them', async () => {
    const { harness, el } = await open();
    await answer(harness);
    const start = el.querySelector<HTMLInputElement>('.gd-stats__start')!;
    start.value = 'soon';
    start.dispatchEvent(new Event('input'));
    const period = el.querySelector<HTMLSelectElement>('.gd-stats__period')!;
    period.value = 'week';
    period.dispatchEvent(new Event('change'));
    el.querySelector<HTMLInputElement>('.gd-stats__display')!.click();
    const request = await answer(harness, 400);
    expect(request.request.params.get('startDay')).toBe('soon');
    expect(request.request.params.get('length')).toBe('week');
    await harness.fixture.whenStable();
    expect((TestBed.inject(Notifier) as FakeNotifier).dialogs).toEqual(['alert: Invalid parameter entered']);
  });

  it('says so of a period without games', async () => {
    const { harness, el } = await open();
    await answer(harness, JSON.stringify({ Stats: [], StartDate: '2026-01-01', EndDate: '2026-01-02' }));
    expect(Array.from(el.querySelectorAll('.gd-stats__empty')).map((td) => td.textContent)).toEqual([
      'No competitive games in this period.',
      'No casual games in this period.',
    ]);
  });

  describe('coming back after a while (GempHallTabs.onReturn)', () => {
    let now: number;

    async function openAt() {
      now = new Date(2026, 10, 20, 12).getTime();
      TestBed.configureTestingModule({
        providers: [
          provideGempHttp(),
          provideHttpClientTesting(),
          provideFakeNotifier(),
          provideRouter([{ path: 'info', children: INFO_ROUTES }]),
          {
            provide: Timers,
            useValue: {
              now: () => now,
              after: (ms: number, callback: () => void) => setTimeout(callback, ms),
              clear: (handle: ReturnType<typeof setTimeout> | null) => clearTimeout(handle ?? undefined),
            },
          },
        ],
      });
      const harness = await RouterTestingHarness.create();
      await harness.navigateByUrl('/info/stats');
      backend = TestBed.inject(HttpTestingController);
      await answer(harness);
      return harness;
    }

    it('fetches the stats shown again, for the same dates, when the viewer comes back to the sub-tab after 10 s', async () => {
      const harness = await openAt();
      (harness.routeNativeElement as HTMLElement).querySelector<HTMLInputElement>('.gd-stats__start')!.value =
        '2026-1-1';
      await harness.navigateByUrl('/info/about');
      now += 9000;
      await harness.navigateByUrl('/info/stats');
      backend.expectNone((req) => req.url.endsWith('/stats'));
      await harness.navigateByUrl('/info/about');
      now += 10000;
      await harness.navigateByUrl('/info/stats');
      const request = await answer(harness);
      expect(request.request.params.get('startDay')).toBe('2026-10-20');
    });

    it('fetches them again when the window comes back after 10 s', async () => {
      const harness = await openAt();
      window.dispatchEvent(new Event('blur'));
      harness.detectChanges();
      now += 3000;
      window.dispatchEvent(new Event('focus'));
      harness.detectChanges();
      backend.expectNone((req) => req.url.endsWith('/stats'));
      window.dispatchEvent(new Event('blur'));
      harness.detectChanges();
      now += 10000;
      window.dispatchEvent(new Event('focus'));
      harness.detectChanges();
      await answer(harness);
    });
  });
});
