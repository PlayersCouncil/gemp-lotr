import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../../api/gemp-interceptors';
import { ImagePreloader } from '../../../platform/image-preloader.service';
import { PAGE_QUERY_STRING } from '../../../platform/page-location';
import { readServerResponse } from '../../../../testing/files';
import { SEARCH_DELAY_MS, PcErrataService } from './errata.service';
import { ERRATA_SERVICES } from './errata-services';
import { FOLLOW_INTERVAL_MS } from './errata-skim';

const URL = '/gemp-lotr-server/hall/errata/json';

describe('PcErrataService', () => {
  let backend: HttpTestingController;
  let preloaded: string[];

  function service(): PcErrataService {
    return TestBed.inject(PcErrataService);
  }

  async function answer(body: string = readServerResponse('errata.json')): Promise<void> {
    const request = backend.expectOne((req) => req.url === URL);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('participantId')).toBe('alice');
    request.flush(body);
    await Promise.resolve();
    await Promise.resolve();
  }

  async function ready(link = { card: null as string | null, params: {} }): Promise<PcErrataService> {
    const errata = service();
    errata.openRoute(link);
    const done = errata.start();
    await answer();
    await done;
    return errata;
  }

  const shownId = (errata: PcErrataService) => errata.shown()?.entry.id ?? null;

  beforeEach(() => {
    vi.useFakeTimers();
    preloaded = [];
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        ...ERRATA_SERVICES,
        { provide: PAGE_QUERY_STRING, useValue: '?participantId=alice' },
        {
          provide: ImagePreloader,
          useValue: {
            load: (urls: Iterable<string>) => {
              preloaded.push(...urls);
              return Promise.resolve();
            },
          },
        },
      ],
    });
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    backend.verify();
    vi.useRealTimers();
  });

  it('loads the errata once, and shows the first card (load, setData)', async () => {
    const errata = service();
    const done = errata.start();
    expect(errata.load()).toEqual({ kind: 'loading' });
    await answer();
    await done;
    expect(errata.load().kind).toBe('ready');
    expect(errata.summary()).toBe('7 cards: 5 PC errata of Decipher cards and 2 revised V-set cards.');
    expect(errata.status()).toEqual({ text: '7 cards', empty: false });
    expect(errata.index()).toBe(0);
    expect(shownId(errata)).toBe('51_45');
    expect(errata.shown()?.direction).toBe(0);
    expect(errata.counter()).toBe('1 of 7');
    expect(errata.canPrevious()).toBe(false);
    expect(errata.canNext()).toBe(true);
    expect(errata.touched()).toBe(false);
    expect(errata.focusRequest()).toBe(1);
    // the cards next to it are on their way
    expect(preloaded.length).toBeGreaterThan(0);
    await errata.start();
    backend.expectNone(URL);
  });

  it("says what went wrong in the table's place, and tries again on the next visit (neutralErrorMap)", async () => {
    const errata = service();
    const done = errata.start();
    backend.expectOne((req) => req.url === URL).flush('', { status: 503, statusText: 'Unavailable' });
    await done;
    expect(errata.load()).toEqual({
      kind: 'error',
      message: 'Could not load the errata list right now. Please try again in a few minutes.',
    });
    expect(errata.shown()).toBeNull();
    expect(errata.counter()).toBe('0 of 0');
    const again = errata.start();
    await answer();
    await again;
    expect(errata.load().kind).toBe('ready');
  });

  it('steps through the list, nudging at the ends (step, bump)', async () => {
    const errata = await ready();
    expect(errata.step(-1)).toBe(false);
    expect(errata.bump()?.direction).toBe(-1);
    expect(errata.step(1)).toBe(true);
    expect(shownId(errata)).toBe('51_100');
    expect(errata.shown()?.direction).toBe(1);
    expect(errata.touched()).toBe(true);
    expect(errata.rowSync()?.scroll).toBe(true);
    errata.last();
    expect(shownId(errata)).toBe('103_19');
    expect(errata.step(1)).toBe(false);
    expect(errata.bump()?.direction).toBe(1);
    errata.first();
    expect(errata.index()).toBe(0);
  });

  it('flips between the versions, which a new card resets; not while enlarged', async () => {
    const errata = await ready();
    errata.toggleOriginal();
    expect(errata.showOriginal()).toBe(true);
    errata.step(1);
    expect(errata.showOriginal()).toBe(false);
    errata.setEnlarged(true);
    errata.toggleOriginal();
    expect(errata.showOriginal()).toBe(false);
    expect(errata.shown()?.direction).toBe(0);
  });

  it('filters after the last key, keeping the card when it stays listed (refresh)', async () => {
    const errata = await ready();
    errata.step(1); // Lórien Elf
    errata.setQuery('lor');
    expect(errata.query()).toBe('lor');
    vi.advanceTimersByTime(SEARCH_DELAY_MS - 1);
    expect(errata.list()).toHaveLength(7);
    vi.advanceTimersByTime(1);
    expect(errata.list().map((entry) => entry.id)).toEqual(['51_100', '71_100']);
    expect(shownId(errata)).toBe('51_100');
    expect(errata.status().text).toBe('2 of 7 cards');
    expect(errata.address()).toEqual({ path: '/help/errata/51_100', queryParams: { q: 'lor' }, fragment: null });
    errata.setFilter('show', 'recent');
    expect(errata.list()).toHaveLength(0);
    expect(errata.shown()).toBeNull();
    expect(errata.status()).toEqual({ text: 'No errata match these filters.', empty: true });
    expect(errata.address().path).toBe('/help/errata');
    errata.setFilter('show', '');
    // the first card, as the card is no longer the one selected
    expect(shownId(errata)).toBe('51_100');
  });

  it('sorts by a column, the second click the other way', async () => {
    const errata = await ready();
    errata.setSort('rev');
    expect(errata.sort()).toEqual({ key: 'rev', dir: 1 });
    expect(errata.list()[0]?.id).toBe('71_100');
    expect(shownId(errata)).toBe('51_45');
    errata.setSort('rev');
    expect(errata.sort()).toEqual({ key: 'rev', dir: -1 });
    errata.setSort('name');
    expect(errata.sort()).toEqual({ key: 'name', dir: 1 });
  });

  it('opens a link that came before the errata, its card winning over its filters (applyRoute)', async () => {
    // of the original's two errata, the one the link's filters list
    const errata = await ready({ card: '1_100', params: { format: 'pc_playtest' } });
    expect(errata.touched()).toBe(true);
    expect(errata.filters().format).toBe('pc_playtest');
    expect(shownId(errata)).toBe('71_100');
    errata.openRoute({ card: '1_45', params: { format: 'pc_playtest', sort: 'name', dir: 'desc' } });
    expect(errata.filters().format).toBe('');
    expect(errata.sort()).toEqual({ key: 'name', dir: -1 });
    expect(shownId(errata)).toBe('51_45');
  });

  it('says so when a link names a card without errata', async () => {
    const errata = await ready({ card: '9_99', params: { set: '1' } });
    expect(errata.status().text).toBe('Card 9_99 has no PC errata. 3 of 7 cards');
    expect(shownId(errata)).toBe('51_45');
    errata.step(1);
    expect(errata.status().text).toBe('Card 9_99 has no PC errata. 3 of 7 cards');
    errata.setFilter('set', '');
    expect(errata.status().text).toBe('7 cards');
  });

  it('says a format has no errata, keeping it as the choice', async () => {
    const errata = await ready({ card: null, params: { format: 'fotr_block' } });
    expect(errata.status().text).toBe('No PC errata are in effect in Fellowship Block.');
    expect(errata.formatOptions().at(-1)).toEqual({ value: 'fotr_block', label: 'Fellowship Block (0)', extra: true });
  });

  it('takes the address it wrote itself as no change, and a bare visit as the sub-tab coming back', async () => {
    const errata = await ready({ card: '5_10', params: {} });
    errata.toggleOriginal();
    const seq = errata.shown()?.seq;
    errata.openRoute({ card: '55_10', params: {} });
    expect(errata.shown()?.seq).toBe(seq);
    expect(errata.showOriginal()).toBe(true);
    errata.setEnlarged(true);
    errata.openRoute({ card: null, params: {} });
    expect(errata.touched()).toBe(false);
    expect(errata.enlarged()).toBe(false);
    expect(shownId(errata)).toBe('55_10');
    expect(errata.showOriginal()).toBe(false);
  });

  it('lets the viewer catch up with skimmed rows at most every 150ms (scheduleShow)', async () => {
    const errata = await ready();
    vi.advanceTimersByTime(1000);
    errata.follow(1);
    errata.follow(2);
    expect(errata.index()).toBe(2);
    expect(shownId(errata)).toBe('51_45');
    vi.advanceTimersByTime(30);
    expect(shownId(errata)).toBe('71_100');
    expect(errata.shown()?.direction).toBe(1);
    errata.follow(3);
    vi.advanceTimersByTime(FOLLOW_INTERVAL_MS - 1);
    expect(shownId(errata)).toBe('71_100');
    vi.advanceTimersByTime(1);
    expect(shownId(errata)).toBe('52_120');
    // the last slide is not over yet: no slide
    expect(errata.shown()?.direction).toBe(0);
  });

  it('clicks a row without scrolling the rows', async () => {
    const errata = await ready();
    errata.selectRow(4);
    expect(shownId(errata)).toBe('55_10');
    expect(errata.rowSync()?.scroll).toBe(false);
    expect(errata.touched()).toBe(true);
  });

  it('gives the card preview either side of the card on show', async () => {
    const errata = await ready();
    expect(errata.preview('before')).toMatchObject({
      blueprintId: '1_45',
      title: 'Gandalf, The Grey Wizard (original)',
    });
    errata.select(5);
    expect(shownId(errata)).toBe('101_5');
    expect(errata.preview('before')).toBeNull();
    expect(errata.images()?.before).toBeNull();
    expect(errata.readout()?.texts[0]?.label).toBe('Game text');
  });
});
