import { TestBed } from '@angular/core/testing';
import { ApiError } from '../../api/api-error';
import { readServerResponse } from '../../../testing/files';
import { parsePatchNotesMonths, parsePatchNotesPage } from './patch-notes';
import { PATCH_NOTES_CACHE_MS, PatchNotesApi } from './patch-notes-api.service';
import { PatchNotesMonth, PatchNotesPage, PatchNotesWindow } from './patch-notes-models';
import { LOADING_DELAY_MS, PatchNotesService } from './patch-notes.service';
import { parseJson } from '../../api/json';

const SECOND_PAGE = parsePatchNotesPage(parseJson(readServerResponse('patch-notes-page.json')));
const MONTHS = parsePatchNotesMonths(parseJson(readServerResponse('patch-notes-months.json')));

function newest(total = 7): PatchNotesPage {
  const notes = [
    { ...SECOND_PAGE.notes[0]!, slug: '2026-11-18', date: '2026-11-18' },
    { ...SECOND_PAGE.notes[0]!, slug: '2026-09-30', date: '2026-09-30' },
  ];
  return { ...SECOND_PAGE, start: 0, total, notes: total ? notes : [] };
}

interface Call {
  readonly span: PatchNotesWindow;
  readonly tag: string | null;
  resolve(page: PatchNotesPage): void;
  reject(status: number): void;
}

class FakeApi {
  readonly calls: Call[] = [];
  readonly monthCalls: {
    tag: string | null;
    resolve(months: PatchNotesMonth[]): void;
    reject(status: number): void;
  }[] = [];

  page(span: PatchNotesWindow, tag: string | null): Promise<PatchNotesPage> {
    return new Promise((resolve, reject) =>
      this.calls.push({ span, tag, resolve, reject: (status) => reject(new ApiError(status, 'x')) }),
    );
  }

  months(tag: string | null): Promise<PatchNotesMonth[]> {
    return new Promise((resolve, reject) =>
      this.monthCalls.push({ tag, resolve, reject: (status) => reject(new ApiError(status, 'x')) }),
    );
  }
}

async function flush(): Promise<void> {
  for (let i = 0; i < 5; i++) await Promise.resolve();
}

describe('PatchNotesService', () => {
  let api: FakeApi;
  let notes: PatchNotesService;

  function last(): Call {
    const call = api.calls[api.calls.length - 1];
    if (!call) throw new Error('no call');
    return call;
  }

  async function answer(page: PatchNotesPage): Promise<void> {
    last().resolve(page);
    await flush();
  }

  beforeEach(() => {
    vi.useFakeTimers();
    api = new FakeApi();
    TestBed.configureTestingModule({ providers: [PatchNotesService, { provide: PatchNotesApi, useValue: api }] });
    notes = TestBed.inject(PatchNotesService);
  });

  afterEach(() => vi.useRealTimers());

  it('opens on the newest updates, saying "Loading" only once the first page is slow', async () => {
    void notes.show(null, false);
    expect(last().span).toEqual({ slug: null });
    expect(notes.status()).toEqual({ kind: 'loading', shown: false });
    vi.advanceTimersByTime(LOADING_DELAY_MS);
    expect(notes.status()).toEqual({ kind: 'loading', shown: true });
    await answer(newest());
    expect(notes.status()).toEqual({ kind: 'done' });
    expect(notes.routeSlug()).toBeNull();
    expect(notes.pager()).toEqual({ newer: false, older: true, range: 'Updates 1–2 of 7' });
    expect(notes.currentMonth()).toBe('2026-11');
    expect(notes.monthTitle()).toBe('November 2026');
    expect(notes.chips().map((chip) => [chip.label, chip.count, chip.on])).toEqual([
      ['All', 7, true],
      ['Card Fixes', 3, false],
      ['PC Updates', 2, false],
      ['User Interface', 2, false],
      ['Announcement', 1, false],
    ]);
    // the months are fetched with the first page
    expect(api.monthCalls.map((call) => call.tag)).toEqual([null]);
  });

  it('shows the default chips before the first page', () => {
    expect(notes.chips().map((chip) => [chip.label, chip.count])).toEqual([
      ['All', null],
      ['Card Fixes', null],
      ['PC Updates', null],
      ['User Interface', null],
      ['Announcement', null],
    ]);
    expect(notes.pager()).toBeNull();
  });

  it("shows a link's update first, marked, and puts the window's first update in the address", async () => {
    void notes.show('2026-09-27-hall-overhaul', true);
    expect(last().span).toEqual({ slug: '2026-09-27-hall-overhaul' });
    await answer(SECOND_PAGE);
    expect(notes.highlight()).toBe('2026-09-27-hall-overhaul');
    expect(notes.scrollRequest()).toBe(1);
    expect(notes.routeSlug()).toBe('2026-09-27-hall-overhaul');
    expect(notes.pager()).toEqual({ newer: true, older: false, range: 'Updates 6–7 of 7' });
    // the address the page put there asks for nothing
    await notes.show('2026-09-27-hall-overhaul', true);
    expect(api.calls).toHaveLength(1);
  });

  it('pages newer and older, the newest page without a slug', async () => {
    void notes.show(null, false);
    await answer(newest(12));
    void notes.step(1);
    expect(last().span).toEqual({ start: 5 });
    await answer({ ...SECOND_PAGE, total: 12 });
    void notes.step(1);
    expect(last().span).toEqual({ start: 10 });
    await answer({ ...SECOND_PAGE, start: 10, total: 12 });
    await notes.step(1);
    expect(api.calls).toHaveLength(3);
    void notes.step(-1);
    expect(last().span).toEqual({ start: 5 });
    await answer({ ...SECOND_PAGE, total: 12 });
    void notes.step(-1);
    expect(last().span).toEqual({ slug: null });
  });

  it('filters by tag, from the newest; the filter stays for the next visit', async () => {
    void notes.show(null, false);
    await answer(newest());
    void notes.setTag('PC Updates');
    expect(last()).toMatchObject({ span: { slug: null }, tag: 'PC Updates' });
    expect(notes.chips().find((chip) => chip.on)?.label).toBe('PC Updates');
    await answer(newest(0));
    expect(notes.emptyText()).toBe('No updates are tagged “PC Updates” yet.');
    expect(notes.pager()).toBeNull();
    expect(notes.monthList()).toEqual({ kind: 'loading' });
    expect(api.monthCalls.map((call) => call.tag)).toEqual([null, 'PC Updates']);
  });

  it('shows a link the filter hides under All, saying so', async () => {
    void notes.setTag('Card Fixes');
    await answer(newest());
    void notes.show('announcement-12', true);
    last().reject(404);
    await flush();
    expect(notes.tag()).toBeNull();
    expect(last()).toMatchObject({ span: { slug: 'announcement-12' }, tag: null });
    await answer(SECOND_PAGE);
    expect(notes.notices()).toEqual(['Showing all updates: that one is not tagged “Card Fixes”.']);
  });

  it('says so of a link to no update, and shows the newest', async () => {
    void notes.show('2001-01-01', true);
    last().reject(404);
    await flush();
    expect(last().span).toEqual({ slug: null });
    await answer(newest());
    expect(notes.notices()).toEqual(['There is no update named “2001-01-01”. Here are the newest ones.']);
    expect(notes.highlight()).toBeNull();
    void notes.step(1);
    await answer(SECOND_PAGE);
    expect(notes.notices()).toEqual([]);
  });

  it('says what failed, and tries again on asking', async () => {
    void notes.show(null, false);
    last().reject(503);
    await flush();
    expect(notes.status()).toEqual({ kind: 'error', status: 503 });
    void notes.retry();
    last().reject(0);
    await flush();
    expect(notes.status()).toEqual({ kind: 'error', status: 0 });
  });

  it('keeps only the latest navigation', async () => {
    void notes.show(null, false);
    const first = last();
    void notes.setTag('PC Updates');
    await answer(newest(3));
    first.resolve(newest(9));
    await flush();
    expect(notes.page()?.total).toBe(3);
  });

  it('steps through the months with updates and shows a month from its newest update', async () => {
    void notes.show(null, false);
    await answer(newest());
    expect(notes.monthArrowsDisabled()).toEqual({ older: true, newer: true });
    api.monthCalls[0]?.resolve(MONTHS);
    await flush();
    expect(notes.monthArrowsDisabled()).toEqual({ older: false, newer: true });
    expect(notes.monthList()).toEqual({
      kind: 'ready',
      months: ['2026-11', '2026-09', '2023-12'],
      current: '2026-11',
      emptyText: 'There are no patch notes yet.',
    });
    void notes.jumpMonth(-1);
    await flush();
    expect(last().span).toEqual({ slug: '2026-09-30' });
    await answer(SECOND_PAGE);
    expect(notes.currentMonth()).toBe('2026-09');
    void notes.showMonth('2023-12');
    expect(last().span).toEqual({ slug: 'announcement-12' });
    await notes.showMonth('1999-01');
    expect(api.calls).toHaveLength(3);
  });

  it('fetches the months again after a while, and says when they could not be had', async () => {
    void notes.show(null, false);
    await answer(newest());
    api.monthCalls[0]?.reject(500);
    await flush();
    expect(notes.monthList()).toEqual({
      kind: 'error',
      message: 'Could not load the list of months. (the server answered 500)',
    });
    notes.retryMonths();
    api.monthCalls[1]?.resolve(MONTHS);
    await flush();
    expect(notes.monthList().kind).toBe('ready');
    void notes.step(1);
    await answer(SECOND_PAGE);
    expect(api.monthCalls).toHaveLength(2);
    vi.advanceTimersByTime(PATCH_NOTES_CACHE_MS);
    void notes.step(-1);
    await answer(newest());
    expect(api.monthCalls).toHaveLength(3);
  });

  it('coming back to the sub-tab shows what was left', async () => {
    void notes.show('2026-09-27-hall-overhaul', true);
    await answer(SECOND_PAGE);
    // the sub-tab opened again: the same window (the api's cache answers it while it is fresh)
    void notes.show(null, false);
    expect(last().span).toEqual({ slug: '2026-09-27-hall-overhaul' });
    expect(last().tag).toBeNull();
  });

  it('pages and steps through months only once a page is shown', async () => {
    await notes.step(1);
    await notes.jumpMonth(-1);
    expect(api.calls).toEqual([]);
    expect(api.monthCalls).toEqual([]);
  });

  it('fetches the months first when "<" is pressed before they came, and stays when they cannot be had', async () => {
    void notes.show(null, false);
    await answer(newest());
    api.monthCalls[0]?.reject(500);
    await flush();
    void notes.jumpMonth(-1);
    expect(api.monthCalls).toHaveLength(2);
    api.monthCalls[1]?.resolve(MONTHS);
    await flush();
    expect(last().span).toEqual({ slug: '2026-09-30' });
    await answer(SECOND_PAGE);
    api.monthCalls.length = 0;
    // the months are forgotten with the filter: another tag, then "<" with its months failing
    void notes.setTag('PC Updates');
    await answer(newest());
    api.monthCalls[0]?.reject(0);
    await flush();
    const calls = api.calls.length;
    void notes.jumpMonth(1);
    api.monthCalls[1]?.reject(0);
    await flush();
    expect(api.calls).toHaveLength(calls);
  });
});
