import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../testing/files';
import { FakeNotifier, provideFakeNotifier, settle } from '../../testing/fake-notifier';
import { GempFormat } from '../api/models';
import { CollectionService } from '../collection/collection.service';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { DeckService, STATS_INTERVAL_MS } from './deck.service';

const format = (code: string, name: string, order: number): GempFormat => ({
  code,
  name,
  order,
  hall: true,
  usesMaps: code === 'pc_movie',
  blockFilters: [
    {
      filter: 'fotr_block',
      name: 'Fellowship Block',
      setFilters: { 'The Fellowship of the Ring': '1', 'Mines of Moria': '2' },
    },
  ],
  errata: {},
});

const FORMATS = {
  Formats: {
    rev_tow_sta: format('rev_tow_sta', 'Anything Goes', 0),
    fotr_block: format('fotr_block', 'Fellowship Block', 1),
    pc_movie: format('pc_movie', 'Movie Block (PC)', 2),
  },
};

describe('DeckService', () => {
  let service: DeckService;
  let collection: CollectionService;
  let backend: HttpTestingController;
  let dialogs: string[];
  let answers: (string | boolean | null)[];

  beforeEach(async () => {
    vi.useFakeTimers({ toFake: ['setInterval', 'clearInterval'] });
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    ({ dialogs, answers } = TestBed.inject(FakeNotifier));
    backend = TestBed.inject(HttpTestingController);
    service = TestBed.inject(DeckService);
    collection = TestBed.inject(CollectionService);
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  const take = (path: string): TestRequest[] =>
    backend.match((req) => req.url.endsWith(path) || req.url.includes(`${path}?`));
  const body = (req: TestRequest) => {
    const params = req.request.body as { keys(): string[]; get(k: string): string | null };
    return Object.fromEntries(
      params
        .keys()
        .filter((k) => k !== 'participantId')
        .map((k) => [k, params.get(k)]),
    );
  };
  const collectionRequests = () => backend.match((req) => req.url.includes('/collection/'));

  /** Starts the page: formats, the limited collections and the RTMD leagues answered. */
  function start(): void {
    service.init();
    take('/deck/formats')[0].flush(JSON.stringify(FORMATS));
    take('/collection')[0].flush(readServerResponse('collection-types.xml'));
    take('/league')[0].flush(readServerResponse('leagues.xml'));
    collectionRequests();
  }

  /** Lets the stats timer run and returns the stats request it sent. */
  function statsRequest(): TestRequest {
    vi.advanceTimersByTime(STATS_INTERVAL_MS);
    const [req] = take('/deck/stats');
    return req;
  }

  it('fills the format list: hall formats, limited collections, then RTMD leagues', () => {
    start();
    expect(service.formatOptions().map((o) => o.label)).toEqual([
      'Anything Goes',
      'Fellowship Block',
      'Movie Block (PC)',
      '---Limited---',
      'Sealed League - FotR',
      'Tournament draft',
      '---Race to Mount Doom---',
      'Race to Mount Doom (pos 3/9)',
    ]);
    expect(service.selectedFormat().value).toBe('rev_tow_sta');
    expect(collection.limitedCollections().length).toBe(2);
    // Anything Goes as the page's default can't be converted until cards are added
    expect(service.convertErrataEnabled()).toBe(false);
  });

  it('refreshes the stats after changes', () => {
    start();
    const req = statsRequest();
    expect(body(req)).toEqual({ targetFormat: 'rev_tow_sta', collectionName: 'default', deckContents: '|||' });
    req.flush('<b>Free People</b>: 0');
    expect(service.stats()).toEqual({
      lines: [{ segments: [{ text: 'Free People', strong: true, tone: null }, { text: ': 0', strong: false, tone: null }] }],
    });
    vi.advanceTimersByTime(STATS_INTERVAL_MS * 3);
    expect(take('/deck/stats').length).toBe(0);

    service.addFromCollection('1_12', 'FREE_PEOPLE');
    expect(body(statsRequest())['deckContents']).toBe('|||1_12');
  });

  it('marks the deck modified and counts its cards in the collection', () => {
    start();
    service.addFromCollection('1_12', 'FREE_PEOPLE');
    service.addFromCollection('1_12', 'FREE_PEOPLE');
    expect(service.label()).toEqual({ text: 'New deck', modified: true });
    expect(service.contentsDirty()).toBe(true);
    expect(service.convertErrataEnabled()).toBe(true);
    TestBed.tick();
    expect(collection.availableCount('1_12', 4)).toBe(2);
  });

  it('fills an empty special slot from the filtered collection, then goes back to the normal filter', () => {
    start();
    service.slotClicked('ringBearer');
    expect(collectionRequests().map((r) => r.request.params.get('filter'))).toEqual([
      'format:rev_tow_sta sort:name canStartWithRing:true product:card',
    ]);
    service.addFromCollection('1_290', 'FREE_PEOPLE');
    expect(service.deck().ringBearer.map((c) => c.blueprintId)).toEqual(['1_290']);
    expect(collectionRequests()[0].request.params.get('filter')).toBe(
      'format:rev_tow_sta sort:name cardType:-THE_ONE_RING',
    );

    // A filled slot doesn't switch the filter again
    service.slotClicked('ringBearer');
    expect(collectionRequests().length).toBe(0);
  });

  it('shows the sites by site number when the sites are clicked', () => {
    start();
    service.slotClicked('sites');
    expect(collection.fields()).toMatchObject({ cardType: 'SITE', sort: 'siteNumber,name' });
    collectionRequests();
  });

  it('shows the Map slot for movie formats and sends the Map with the deck', () => {
    start();
    service.selectFormat(2);
    expect(service.usesMap()).toBe(true);
    expect(service.convertErrataEnabled()).toBe(true);
    service.slotClicked('map');
    service.addFromCollection('101_1', null);
    expect(service.deckContents()).toBe('||||101_1');
    service.selectFormat(1);
    expect(service.usesMap()).toBe(false);
    expect(service.deckContents()).toBe('|||');
    collectionRequests();
  });

  it('shows the Race meta-site for an RTMD league and validates against the league', () => {
    start();
    service.selectFormat(7);
    expect(service.effectiveFormat()).toBe('pc_movie');
    expect(service.usesRace()).toBe(true);
    expect(service.raceCard()).toEqual({ blueprintId: '90_7', overlayBlueprintId: null });
    const req = statsRequest();
    expect(body(req)).toEqual({ targetFormat: 'pc_movie', deckContents: '||||', leagueCode: '1700000002' });
    // The league's meta-site allows other sites, so site searches ask for them
    service.slotClicked('sites');
    expect(collectionRequests().at(-1)!.request.params.get('filter')).toContain('siteOverride:true');
  });

  it('checks a limited deck against its collection', () => {
    start();
    service.selectFormat(4);
    expect(body(statsRequest())).toMatchObject({ targetFormat: 'fotr_block', collectionName: '1699999999:sealed' });
    collectionRequests();
  });

  it('asks for a name when saving a new deck, and saves without "null" notes', async () => {
    start();
    service.addFromCollection('1_12', 'FREE_PEOPLE');
    answers.push('ab', 'My deck');
    expect(await service.saveCurrentDeck()).toBe(false);
    expect(await service.saveCurrentDeck()).toBe(true);
    const [save] = take('/deck');
    expect(body(save)).toEqual({
      deckName: 'My deck',
      targetFormat: 'rev_tow_sta',
      notes: '',
      deckContents: '|||1_12',
    });
    save.flush('<ok/>');
    await settle();
    expect(dialogs).toEqual([
      'prompt: Enter the name of the deck',
      'alert: Deck name has to have at least 3 characters and at most 100 characters.',
      'prompt: Enter the name of the deck',
      'alert: Deck was saved.  Refresh the Game Hall to see it!',
    ]);
    expect(service.label()).toEqual({ text: 'My deck', modified: false });
    expect(service.contentsDirty()).toBe(false);
  });

  it('saves changes under the new name when renaming a modified deck', async () => {
    start();
    service.setupDeck({ ...deckOf('1_12'), notes: 'n' }, 'Old');
    service.addFromCollection('1_13', 'FREE_PEOPLE');
    answers.push('New', true);
    await service.renameCurrentDeck();
    const [rename] = take('/deck/rename');
    expect(body(rename)).toEqual({ oldDeckName: 'Old', deckName: 'New' });
    rename.flush(readServerResponse('deck.xml'));
    await settle();
    const [save] = take('/deck');
    expect(body(save)).toMatchObject({ deckName: 'New', notes: 'n' });
    save.flush('<ok/>');
    expect(service.label()).toEqual({ text: 'New', modified: false });
    collectionRequests();
  });

  it('copies a deck to a new, unnamed one', () => {
    start();
    service.setupDeck(deckOf('1_12'), 'Mine');
    service.copyDeck();
    expect(service.deckName()).toBeNull();
    expect(service.label()).toEqual({ text: 'New deck', modified: true });
    collectionRequests();
  });

  it('on New, offers to save; Cancel discards the deck and its notes', async () => {
    start();
    service.setupDeck({ ...deckOf('1_12'), notes: 'old notes' }, 'Mine');
    service.addFromCollection('1_13', 'FREE_PEOPLE');
    answers.push(false);
    await service.newDeck();
    expect(service.deck().draw).toEqual([]);
    expect(service.deckName()).toBeNull();
    expect(service.notes()).toBe('');
    expect(service.label()).toEqual({ text: 'New deck', modified: false });
    collectionRequests();
  });

  it('asks before leaving a modified deck', async () => {
    start();
    expect(await service.savePrompt()).toBe(true);
    service.addFromCollection('1_12', 'FREE_PEOPLE');
    answers.push(false);
    expect(await service.savePrompt()).toBe(false);
    expect(dialogs.at(-1)).toBe(
      'confirm: Your deck has been modified.  Would you like to save your deck before proceeding?',
    );
  });

  it('converts to PC errata, keeping the notes', () => {
    start();
    service.setupDeck({ ...deckOf('1_12'), notes: 'keep me' }, 'Mine');
    service.convertErrata();
    take('/deck/convert')[0].flush(readServerResponse('convert.xml'));
    expect(service.notes()).toBe('keep me');
    expect(service.deck().draw.length).toBeGreaterThan(1);
    expect(service.label().modified).toBe(true);
    expect(service.convertErrataEnabled()).toBe(false);
    collectionRequests();
  });

  it('loads a deck into its RTMD league when the league is running', () => {
    start();
    service.setupDeck(
      { ...deckOf('1_12'), targetFormat: { formatCode: 'pc_movie', formatName: 'x', leagueCode: '1700000002' } },
      'Race deck',
    );
    expect(service.selectedFormat().value).toBe('1700000002');
    expect(service.label()).toEqual({ text: 'Race deck', modified: false });
    collectionRequests();
  });
});

function deckOf(...draw: string[]) {
  return {
    targetFormat: null,
    notes: '',
    ringBearer: null,
    ring: null,
    map: null,
    sites: [],
    drawDeck: draw.map((blueprintId) => ({ blueprintId, side: 'FREE_PEOPLE' })),
  };
}
