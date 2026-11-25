import { HttpErrorResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { provideCardInteractions } from '../../../core/cards/card-interactions';
import { EventHistoryBrowser, EventHistorySource } from '../../../core/hall/events/event-history';
import {
  CompletedEventsMonth,
  EventKind,
  LeagueDetail,
  TournamentDetail,
} from '../../../core/hall/events/events-models';
import { parseEventHistory, parseLeagueDetail } from '../../../core/hall/events/events-parsers';
import { EVENTS_SERVICES } from '../../../core/hall/events/events-tab.service';
import { Timers } from '../../../core/platform/timers';
import { provideFakeNotifier, settle } from '../../../testing/fake-notifier';
import { readServerResponse } from '../../../testing/files';
import { EventHistoryComponent } from './event-history.component';
import { EVENTS_AUTO_ZOOM_COOKIE, EventsCardActions } from './events-tab.component';

const LEAGUE_MONTH = parseEventHistory(
  JSON.parse(readServerResponse('event-history-league.json')),
  '2026-10',
  'league',
);
const LEAGUE = parseLeagueDetail(readServerResponse('league-detail.xml'))!;
const failure = (status = 500) => new HttpErrorResponse({ status, statusText: 'Server Error', error: '' });

/**
 * eventHistoryUi.js EventHistoryUI and its MonthNav: the month dropdown (loading, failed with Retry, the months by
 * year), the status line with its Retry, a league month's rows (dates "start – end", "—" for what is missing) and each
 * row's details beneath it.
 */
describe('EventHistoryComponent', () => {
  let months: Subject<string[]>;
  let month: Subject<CompletedEventsMonth>;
  let leagues: Record<string, Subject<LeagueDetail | null>>;
  let asked: string[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        ...EVENTS_SERVICES,
        provideCardInteractions({ actions: EventsCardActions, autoZoomCookie: EVENTS_AUTO_ZOOM_COOKIE }),
        { provide: Timers, useValue: { now: () => Date.UTC(2026, 10, 20), after: setTimeout, clear: clearTimeout } },
      ],
    });
    asked = [];
    leagues = {};
  });

  function render(kind: EventKind = 'league') {
    const source: EventHistorySource = {
      months: () => {
        asked.push('months');
        return (months = new Subject());
      },
      month: (key) => {
        asked.push(key);
        return (month = new Subject());
      },
      league: (code) => (leagues[code] = new Subject()),
      tournament: () => new Subject<TournamentDetail | null>(),
      monthsCache: new Map(),
      monthCache: new Map(),
    };
    const browser = new EventHistoryBrowser(kind, source, TestBed.inject(Timers));
    const fixture = TestBed.createComponent(EventHistoryComponent);
    fixture.componentRef.setInput('browser', browser);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    document.body.append(el);
    const update = async () => {
      await settle();
      fixture.detectChanges();
    };
    const text = (selector: string) => el.querySelector(selector)?.textContent?.replace(/\s+/g, ' ').trim();
    const click = (selector: string) => el.querySelector<HTMLElement>(selector)!.click();
    return { browser, fixture, el, update, text, click };
  }

  afterEach(() => document.body.querySelectorAll('gd-event-history').forEach((el) => el.remove()));

  const answer = <T>(subject: Subject<T>, value: T) => {
    subject.next(value);
    subject.complete();
  };

  it('opens the months: loading, a failure with Retry, then the months by year; a pick loads that month', async () => {
    const { el, update, text, click } = await render();
    click('.gd-event-history__title');
    await update();
    expect(text('.gd-event-history__dd-status')).toBe('Loading months…');
    expect(el.querySelector<HTMLButtonElement>('[data-action=previous]')!.disabled).toBe(true);

    months.error(failure());
    await update();
    expect(el.querySelector('.gd-event-history__dd-status')?.getAttribute('data-state')).toBe('error');
    expect(text('.gd-event-history__dd-status')).toMatch(/^Could not load the list of months\. /);

    click('[data-action=retry-months]');
    await update();
    expect(text('.gd-event-history__dd-status')).toBe('Loading months…');
    answer(months, ['2026-10', '2025-12']);
    await update();
    expect(
      Array.from(el.querySelectorAll('.gd-event-history__year')).map((year) => [
        year.querySelector('.gd-event-history__year-label')?.textContent,
        ...Array.from(year.querySelectorAll('.gd-event-history__dd-month')).map((m) => m.getAttribute('data-month')),
      ]),
    ).toEqual([
      ['2026', '2026-10'],
      ['2025', '2025-12'],
    ]);

    click('[data-month="2025-12"]');
    await update();
    expect(el.querySelector('.gd-event-history__dropdown')).toBeNull();
    expect(document.activeElement).toBe(el.querySelector('.gd-event-history__title'));
    expect(text('.gd-event-history__title')).toContain('December 2025');
    expect(asked.at(-1)).toBe('2025-12');
    expect(text('.gd-event-history__status')).toBe('Loading December 2025…');
  });

  it('says when there are no months with events yet', async () => {
    const { update, text, click } = await render('tournament');
    click('.gd-event-history__title');
    answer(months, []);
    await update();
    expect(text('.gd-event-history__dd-status')).toBe('No completed tournaments have been recorded yet.');
  });

  it('closes the months once the focus leaves them, and keeps them open while it moves inside', async () => {
    const { el, update, click } = await render();
    click('.gd-event-history__title');
    answer(months, ['2026-10']);
    await update();
    const title = el.querySelector('.gd-event-history__title')!;
    const inside = el.querySelector('.gd-event-history__dd-month')!;
    title.dispatchEvent(new FocusEvent('focusout', { bubbles: true, relatedTarget: inside }));
    await update();
    expect(el.querySelector('.gd-event-history__dropdown')).not.toBeNull();
    // Escape outside the month does nothing
    el.querySelector('[data-action=next]')!.dispatchEvent(
      new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }),
    );
    await update();
    expect(el.querySelector('.gd-event-history__dropdown')).not.toBeNull();
    inside.dispatchEvent(new FocusEvent('focusout', { bubbles: true, relatedTarget: null }));
    await update();
    expect(el.querySelector('.gd-event-history__dropdown')).toBeNull();
  });

  it('retries a month that failed, then draws its leagues: dates from start to end, a dash for what is missing', async () => {
    const { el, browser, update, text, click } = await render();
    browser.start();
    answer(months, ['2026-10', '2026-09']);
    await update();
    month.error(failure());
    await update();
    expect(el.querySelector('.gd-event-history__status')?.getAttribute('data-state')).toBe('error');
    expect(text('.gd-event-history__status')).toMatch(/^Could not load October 2026\. .* Retry$/);

    click('[data-action=retry]');
    answer(month, LEAGUE_MONTH);
    await update();
    expect(text('.gd-event-history__status')).toBe('October 2026: 2 completed leagues.');
    expect(Array.from(el.querySelectorAll('thead th')).map((th) => th.textContent)).toEqual([
      '',
      'Name',
      'Dates',
      'Format',
      'Players',
    ]);
    const rows = Array.from(el.querySelectorAll('.gd-event-history__row')).map((row) =>
      Array.from(row.querySelectorAll('td'))
        .slice(1)
        .map((td) => td.textContent?.trim()),
    );
    expect(rows).toEqual([
      ['October League', '2026-10-01 – 2026-10-28', 'Fellowship Block', '14'],
      ['—', '2026-09-20 – 2026-10-04', '—', '—'],
    ]);

    // the previous month with events
    click('[data-action=previous]');
    await update();
    expect(asked.at(-1)).toBe('2026-09');
    // and back: October is kept, not asked again
    click('[data-action=next]');
    await update();
    expect(asked.at(-1)).toBe('2026-09');
    expect(text('.gd-event-history__status')).toBe('October 2026: 2 completed leagues.');
  });

  it("opens a league's details beneath its row: loading, then the league; a failure or nothing to show says so", async () => {
    const { el, browser, update, text } = await render();
    browser.start();
    answer(months, ['2026-10']);
    await update();
    answer(month, LEAGUE_MONTH);
    await update();
    const toggles = () => Array.from(el.querySelectorAll<HTMLButtonElement>('.gd-event-history__detail-cell button'));
    const details = () => Array.from(el.querySelectorAll<HTMLElement>('.gd-event-history__detail'));

    toggles()[0].click();
    await update();
    expect(details()[0].getAttribute('data-state')).toBe('loading');
    expect(text('.gd-event-history__loading')).toMatch(/\S/);
    answer(leagues['1690000001'], LEAGUE);
    await update();
    expect(details()[0].querySelector('gd-league-detail')).not.toBeNull();
    expect(details()[0].querySelector('.gd-league-detail__membership')).toBeNull();

    toggles()[1].click();
    await update();
    leagues['1690000002'].error(failure(404));
    await update();
    expect(text('.gd-event-history__error')).toMatch(/^Could not load the details \(HTTP 404\)\./);

    toggles()[1].click();
    toggles()[1].click();
    await update();
    answer(leagues['1690000002'], null);
    await update();
    expect(details()[1].querySelector('i')?.textContent).toBe('No details are available for this league.');
  });

  it('gives an admin a dash for an event with no admin links', async () => {
    const { el, browser, update } = await render('tournament');
    browser.start();
    answer(months, ['2026-10']);
    await update();
    answer(month, {
      ...parseEventHistory(JSON.parse(readServerResponse('event-history-tournament.json')), '2026-10', 'tournament'),
      events: [
        {
          id: null,
          name: 'Old one',
          startDate: '2026-10-03',
          endDate: null,
          format: 'PC-Movie',
          playerCount: 2,
          rounds: 1,
          adminLinks: [],
        },
      ],
    } as CompletedEventsMonth);
    await update();
    const row = el.querySelector('.gd-event-history__row')!;
    expect(row.querySelector('.gd-event-history__admin-none')?.textContent).toBe('—');
    expect(row.querySelector('.gd-event-history__detail-cell button')).toBeNull();
  });
});
