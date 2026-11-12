import { provideDeckbuilderState } from '../deckbuilder-state';
import { provideGempHttp } from '../api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FakeNotifier, provideFakeNotifier } from '../../testing/fake-notifier';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { addDrawCopy } from './deck-model';
import { DeckDraft } from './deck-draft.service';
import { DeckStatsRefresher, STATS_REFRESH_DELAY_MS } from './deck-stats-refresher.service';

describe('DeckStatsRefresher', () => {
  let refresher: DeckStatsRefresher;
  let draft: DeckDraft;
  let backend: HttpTestingController;

  beforeEach(() => {
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
    TestBed.configureTestingModule({
      providers: [
        ...provideDeckbuilderState(),
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    refresher = TestBed.inject(DeckStatsRefresher);
    draft = TestBed.inject(DeckDraft);
  });

  afterEach(() => vi.useRealTimers());

  const requests = (): TestRequest[] => backend.match((req) => req.url.endsWith('/deck/stats'));
  const wait = () => vi.advanceTimersByTime(STATS_REFRESH_DELAY_MS);

  it('asks nothing before the page has started', () => {
    refresher.refreshSoon();
    wait();
    expect(requests().length).toBe(0);
  });

  it('asks once shortly after the start', () => {
    refresher.start();
    expect(requests().length).toBe(0);
    wait();
    expect(requests().length).toBe(1);
  });

  it('stays quiet when nothing changes', () => {
    refresher.start();
    wait();
    requests();
    vi.advanceTimersByTime(STATS_REFRESH_DELAY_MS * 20);
    expect(requests().length).toBe(0);
  });

  it('asks once for a burst of changes, with the latest deck', () => {
    refresher.start();
    wait();
    requests()[0].flush('');
    refresher.refreshSoon();
    draft.deck.update((deck) => addDrawCopy(deck, '1_12', 'FREE_PEOPLE'));
    refresher.refreshSoon();
    vi.advanceTimersByTime(STATS_REFRESH_DELAY_MS / 2);
    refresher.refreshSoon();
    expect(requests().length).toBe(0);
    vi.advanceTimersByTime(STATS_REFRESH_DELAY_MS / 2);
    const [only, ...others] = requests();
    expect(others).toEqual([]);
    expect((only.request.body as URLSearchParams).get('deckContents')).toBe('|||1_12');
  });

  it('asks again for a change after the last answer', () => {
    refresher.start();
    wait();
    requests()[0].flush('');
    refresher.refreshSoon();
    wait();
    expect(requests().length).toBe(1);
  });

  it('shows the parsed answer', () => {
    refresher.start();
    wait();
    requests()[0].flush('<b>Free People</b>: 0');
    expect(refresher.stats().lines.length).toBe(1);
  });

  it('ignores the answer to an older request when a newer one is on its way', () => {
    refresher.start();
    wait();
    const [older] = requests();
    refresher.refreshSoon();
    wait();
    const [newer] = requests();
    expect(older.cancelled).toBe(true);
    newer.flush('<b>Newer</b>: 1');
    expect(refresher.stats().lines[0].segments[0].text).toBe('Newer');
  });

  it('still refreshes after a failed request, and keeps the last stats meanwhile', () => {
    refresher.start();
    wait();
    requests()[0].flush('<b>Free People</b>: 0');
    const shown = refresher.stats();
    refresher.refreshSoon();
    wait();
    requests()[0].flush('', { status: 400, statusText: 'x' });
    expect(refresher.stats()).toBe(shown);
    expect(TestBed.inject(FakeNotifier).dialogs).toEqual(['alert: Invalid deck for getting stats.']);
    refresher.refreshSoon();
    wait();
    expect(requests().length).toBe(1);
  });

  it('stops its timer when destroyed', () => {
    refresher.start();
    TestBed.resetTestingModule();
    wait();
    expect(vi.getTimerCount()).toBe(0);
  });
});
