import { Observable, of, Subject, throwError } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { Timers } from '../../platform/timers';
import { readServerResponse } from '../../../testing/files';
import { EventHistoryBrowser, EventHistorySource } from './event-history';
import { CompletedEventsMonth, EventKind } from './events-models';
import { parseEventHistory } from './events-parsers';
import { parseJson } from '../../api/json';

const TOURNAMENTS = parseEventHistory(
  parseJson(readServerResponse('event-history-tournament.json')),
  '2026-10',
  'tournament',
);

describe('EventHistoryBrowser', () => {
  let months: Subject<string[]>[];
  let monthRequests: { month: string; answer: Subject<CompletedEventsMonth> }[];
  let source: EventHistorySource;
  const timers = { now: () => Date.UTC(2026, 10, 20) } as Timers;

  beforeEach(() => {
    months = [];
    monthRequests = [];
    source = {
      months: () => {
        const answer = new Subject<string[]>();
        months.push(answer);
        return answer;
      },
      month: (month: string) => {
        const answer = new Subject<CompletedEventsMonth>();
        monthRequests.push({ month, answer });
        return answer;
      },
      league: () => of(null),
      tournament: (): Observable<null> => throwError(() => new ApiError(500, 'x')),
      monthsCache: new Map<EventKind, string[]>(),
      monthCache: new Map<string, CompletedEventsMonth>(),
    };
  });

  const answerMonths = (list: string[]) => {
    months[months.length - 1]!.next(list);
    months[months.length - 1]!.complete();
  };
  const answerMonth = (answer: CompletedEventsMonth, index = monthRequests.length - 1) => {
    monthRequests[index]!.answer.next(answer);
    monthRequests[index]!.answer.complete();
  };
  const tick = () => new Promise((resolve) => setTimeout(resolve, 0));

  it('asks nothing until started, then shows the newest month with completed events', async () => {
    const browser = new EventHistoryBrowser('tournament', source, timers);
    expect(browser.title()).toBe('November 2026');
    expect(browser.status()).toEqual({ state: 'loading', text: 'Loading completed tournaments…', retry: false });
    expect(months).toHaveLength(0);
    browser.start();
    answerMonths(['2026-10', '2026-08']);
    await tick();
    expect(browser.title()).toBe('October 2026');
    expect(browser.status().text).toBe('Loading October 2026…');
    answerMonth(TOURNAMENTS);
    await tick();
    expect(browser.status()).toEqual({ state: 'loaded', text: 'October 2026: 2 completed tournaments.', retry: false });
    expect(browser.rows()).toHaveLength(2);
    expect(browser.shown()?.isAdmin).toBe(true);
    expect(browser.previousDisabled()).toBe(false);
    expect(browser.nextDisabled()).toBe(true);
  });

  it('says when there are none yet', async () => {
    const browser = new EventHistoryBrowser('league', source, timers);
    browser.start();
    answerMonths([]);
    await tick();
    expect(browser.status()).toEqual({
      state: 'empty',
      text: 'No completed leagues have been recorded yet.',
      retry: false,
    });
  });

  it('jumps between months with events, from the cache once fetched', async () => {
    const browser = new EventHistoryBrowser('tournament', source, timers);
    browser.start();
    answerMonths(['2026-10', '2026-08']);
    await tick();
    answerMonth(TOURNAMENTS);
    await tick();
    browser.jump(-1);
    expect(browser.title()).toBe('August 2026');
    answerMonth({ ...TOURNAMENTS, month: '2026-08', events: [] });
    await tick();
    expect(browser.status().text).toBe('No completed tournaments in August 2026.');
    browser.jump(1);
    expect(browser.status().text).toBe('October 2026: 2 completed tournaments.');
    expect(monthRequests).toHaveLength(2);
  });

  it('steps a calendar month when the list of months cannot be had, never past this month', async () => {
    const browser = new EventHistoryBrowser('tournament', source, timers);
    browser.jump(-1);
    expect(browser.previousDisabled()).toBe(true);
    months[0]!.error(new ApiError(0, 'x'));
    await tick();
    expect(browser.title()).toBe('October 2026');
    expect(browser.dropdown()).toEqual({
      state: 'error',
      text: 'The server is unreachable or the connection dropped.',
    });
    expect(browser.nextDisabled()).toBe(false);
  });

  it('starts on the month the viewer chose before it started, and drops an answer for a month left', async () => {
    const browser = new EventHistoryBrowser('tournament', source, timers);
    browser.setMonth('2026-08');
    expect(monthRequests.map((request) => request.month)).toEqual(['2026-08']);
    browser.setMonth('2026-09');
    answerMonth({ ...TOURNAMENTS, month: '2026-08' }, 0);
    await tick();
    expect(browser.status().text).toBe('Loading September 2026…');
    monthRequests[1]!.answer.error(new ApiError(400, 'x'));
    await tick();
    expect(browser.status()).toEqual({
      state: 'error',
      text: 'Could not load September 2026. The request was rejected as malformed.',
      retry: true,
    });
    browser.retry();
    expect(monthRequests).toHaveLength(3);
  });

  it('fills the dropdown when it opens, grouped by year, and closes it on a pick', async () => {
    const browser = new EventHistoryBrowser('league', source, timers);
    browser.openDropdown();
    expect(browser.dropdown()).toEqual({ state: 'loading' });
    answerMonths(['2026-10', '2025-12']);
    await tick();
    expect(browser.dropdown()).toMatchObject({
      state: 'ready',
      current: '2026-11',
      groups: [{ year: '2026' }, { year: '2025' }],
    });
    browser.pick('2025-12');
    expect(browser.dropdownOpen()).toBe(false);
    expect(browser.title()).toBe('December 2025');
  });

  it("opens a completed event's details and says why they failed", async () => {
    const browser = new EventHistoryBrowser('tournament', source, timers);
    source.monthsCache.set('tournament', ['2026-10']);
    source.monthCache.set('tournament|2026-10', TOURNAMENTS);
    browser.start();
    await tick();
    const drawer = browser.rows()[0]!.drawer!;
    drawer.open();
    expect(drawer.content()).toEqual({
      kind: 'error',
      text: 'Could not load the details (HTTP 500). The server failed to produce it.',
    });
  });
  it('starts once; without the list of months it shows this month', async () => {
    const browser = new EventHistoryBrowser('tournament', source, timers);
    browser.start();
    browser.start();
    expect(months).toHaveLength(1);
    months[0]!.error(new ApiError(500, 'x'));
    await tick();
    expect(monthRequests.map((request) => request.month)).toEqual(['2026-11']);
    expect(browser.status().text).toBe('Loading November 2026…');
  });

  it('keeps the month the viewer moved to while the list of months was still coming', async () => {
    const browser = new EventHistoryBrowser('tournament', source, timers);
    browser.start();
    browser.setMonth('2026-07');
    answerMonths(['2026-10', '2026-07']);
    await tick();
    expect(browser.title()).toBe('July 2026');
    expect(monthRequests.map((request) => request.month)).toEqual(['2026-07']);
  });

  it('ignores a month that is not "yyyy-MM", and the title toggles the dropdown shut', () => {
    const browser = new EventHistoryBrowser('league', source, timers);
    browser.setMonth('July');
    expect(browser.title()).toBe('November 2026');
    expect(monthRequests).toEqual([]);
    browser.toggleDropdown();
    browser.toggleDropdown();
    expect(browser.dropdownOpen()).toBe(false);
  });

  it('asks for the months once when an arrow is pressed while they are coming, then jumps', async () => {
    const browser = new EventHistoryBrowser('tournament', source, timers);
    browser.openDropdown();
    browser.jump(-1);
    expect(months).toHaveLength(1);
    answerMonths(['2026-10', '2026-07']);
    await tick();
    expect(browser.title()).toBe('October 2026');
  });

  it('drops a failure for a month the viewer has left', async () => {
    const browser = new EventHistoryBrowser('tournament', source, timers);
    browser.setMonth('2026-08');
    browser.setMonth('2026-09');
    monthRequests[0]!.answer.error(new ApiError(500, 'x'));
    await tick();
    expect(browser.status()).toEqual({ state: 'loading', text: 'Loading September 2026…', retry: false });
  });
});
