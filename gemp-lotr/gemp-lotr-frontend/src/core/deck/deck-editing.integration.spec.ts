import { provideDeckbuilderState } from '../deckbuilder-state';
import { provideGempHttp } from '../api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { combined, Public } from '../../testing/combined';
import { readServerResponse } from '../../testing/files';
import { FakeNotifier, provideFakeNotifier, settle } from '../../testing/fake-notifier';
import { GempFormat } from '../api/models';
import { CollectionBrowser } from '../collection/collection-browser.service';
import { CollectionCounts } from '../collection/collection-counts.service';
import { CollectionQuery } from '../collection/collection-query.service';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { DeckChanges } from './deck-changes.service';
import { DeckDraft } from './deck-draft.service';
import { DeckEditing } from './deck-editing.service';
import { DeckFormatSelection } from './deck-format-selection.service';
import { DeckOpener } from './deck-opener.service';
import { DeckSaver } from './deck-saver.service';
import { DeckStartup } from './deck-startup.service';
import { DeckStatsRefresher, STATS_REFRESH_DELAY_MS as STATS_INTERVAL_MS } from './deck-stats-refresher.service';

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

type Parts = Public<DeckDraft> &
  Public<DeckFormatSelection> &
  Public<DeckChanges> &
  Public<DeckStatsRefresher> &
  Public<DeckEditing> &
  Public<DeckOpener> &
  Public<DeckSaver> &
  Public<DeckStartup> & { deckContents(): string };

/** The deck classes together, so these tests describe what the player gets, whichever class does it. */
function allParts(): Parts {
  const draft = TestBed.inject(DeckDraft);
  const formats = TestBed.inject(DeckFormatSelection);
  return combined<Parts>(
    draft,
    formats,
    // Before the classes with a private field of the same name as its public `stats`
    TestBed.inject(DeckStatsRefresher),
    TestBed.inject(DeckChanges),
    TestBed.inject(DeckEditing),
    TestBed.inject(DeckOpener),
    TestBed.inject(DeckSaver),
    TestBed.inject(DeckStartup),
    { deckContents: () => formats.contentsOf(draft.deck()) },
  );
}

describe('editing a deck', () => {
  let service: Parts;
  let collection: {
    limitedCollections: CollectionBrowser['limitedCollections'];
    fields: CollectionQuery['fields'];
    available: CollectionCounts['available'];
  };
  let backend: HttpTestingController;
  let dialogs: string[];
  let answers: (string | boolean | null)[];

  beforeEach(async () => {
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
    ({ dialogs, answers } = TestBed.inject(FakeNotifier));
    backend = TestBed.inject(HttpTestingController);
    service = allParts();
    collection = combined(
      TestBed.inject(CollectionBrowser),
      TestBed.inject(CollectionQuery),
      TestBed.inject(CollectionCounts),
    );
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
      lines: [
        {
          segments: [
            { text: 'Free People', strong: true, tone: null },
            { text: ': 0', strong: false, tone: null },
          ],
        },
      ],
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
    expect(collection.available('1_12', 4)).toBe(2);
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

  describe('naming a deck', () => {
    const LENGTH_ALERT = 'alert: Deck name has to have at least 3 characters and at most 100 characters.';

    it('accepts names of 3 and of 100 characters, and rejects 2 and 101', async () => {
      start();
      for (const [name, ok] of [
        ['abc', true],
        ['x'.repeat(100), true],
        ['ab', false],
        ['x'.repeat(101), false],
      ] as const) {
        service.deckName.set(null);
        dialogs.length = 0;
        answers.push(name);
        expect(await service.saveCurrentDeck()).toBe(ok);
        expect(dialogs.includes(LENGTH_ALERT)).toBe(!ok);
        take('/deck').forEach((req) => req.flush('<ok/>'));
        await settle();
      }
    });

    it('does nothing when the name prompt is cancelled', async () => {
      start();
      answers.push(null);
      expect(await service.saveCurrentDeck()).toBe(false);
      expect(take('/deck').length).toBe(0);
      expect(service.deckName()).toBeNull();
    });

    it('saves a named deck without asking', async () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Mine');
      expect(await service.saveCurrentDeck()).toBe(true);
      expect(dialogs).toEqual([]);
      expect(body(take('/deck')[0])).toMatchObject({ deckName: 'Mine' });
      collectionRequests();
    });
  });

  describe('saving', () => {
    async function saveFails(status: number) {
      start();
      service.setupDeck(deckOf('1_12'), 'Mine');
      service.addFromCollection('1_13', 'FREE_PEOPLE');
      await service.saveCurrentDeck();
      take('/deck')[0].flush('', { status, statusText: 'x' });
      await settle();
      collectionRequests();
    }

    it('reports an invalid format and stays modified', async () => {
      await saveFails(400);
      expect(dialogs).toEqual(['alert: Invalid deck format.']);
      expect(service.contentsDirty()).toBe(true);
    });

    it('reports any other failure', async () => {
      await saveFails(500);
      expect(dialogs).toEqual(['alert: There was a problem during communication with server']);
    });

    it("stores an RTMD deck under its league's code", async () => {
      start();
      service.selectFormat(7);
      answers.push('Race');
      await service.saveCurrentDeck();
      expect(body(take('/deck')[0])).toMatchObject({ targetFormat: '1700000002' });
      collectionRequests();
    });
  });

  describe('renaming', () => {
    it('renames an unnamed deck by saving it', async () => {
      start();
      answers.push('First name');
      await service.renameCurrentDeck();
      expect(dialogs).toEqual(['prompt: Enter the name of the deck']);
      expect(body(take('/deck')[0])).toMatchObject({ deckName: 'First name' });
    });

    it('sends nothing when cancelled or when the new name is invalid', async () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Old');
      answers.push(null, 'ab');
      await service.renameCurrentDeck();
      await service.renameCurrentDeck();
      expect(take('/deck/rename').length).toBe(0);
      expect(dialogs.at(-1)).toBe('alert: Deck name has to have at least 3 characters and at most 100 characters.');
      expect(service.deckName()).toBe('Old');
      collectionRequests();
    });

    it("doesn't offer to save an unmodified deck", async () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Old');
      answers.push('New');
      await service.renameCurrentDeck();
      take('/deck/rename')[0].flush(readServerResponse('deck.xml'));
      await settle();
      expect(dialogs).toEqual(['prompt: Enter new name for the deck']);
      expect(take('/deck').length).toBe(0);
      expect(service.label()).toEqual({ text: 'New', modified: false });
      collectionRequests();
    });

    it('stays modified under the new name when the save is declined', async () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Old');
      service.addFromCollection('1_13', 'FREE_PEOPLE');
      answers.push('New', false);
      await service.renameCurrentDeck();
      take('/deck/rename')[0].flush(readServerResponse('deck.xml'));
      await settle();
      expect(take('/deck').length).toBe(0);
      expect(service.label()).toEqual({ text: 'New', modified: true });
      collectionRequests();
    });

    it.each([
      [404, "alert: Couldn't find the deck to rename on the server."],
      [500, 'alert: There was a problem during communication with server'],
    ])('reports a %i', async (status, message) => {
      start();
      service.setupDeck(deckOf('1_12'), 'Old');
      answers.push('New');
      await service.renameCurrentDeck();
      take('/deck/rename')[0].flush('', { status, statusText: 'x' });
      await settle();
      expect(dialogs.at(-1)).toBe(message);
      expect(service.deckName()).toBe('Old');
      collectionRequests();
    });

    it('follows a rename from the deck list only for the open deck', () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Open');
      service.deckRenamed('Other', 'Elsewhere');
      expect(service.deckName()).toBe('Open');
      service.deckRenamed('Open', 'Moved');
      expect(service.deckName()).toBe('Moved');
      expect(service.label()).toEqual({ text: 'Moved', modified: false });
      collectionRequests();
    });
  });

  describe('deleting from the deck list', () => {
    it('replaces the open deck with a new one', () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Open');
      service.deckDeleted('Open');
      expect(service.deckName()).toBeNull();
      expect(service.deck().draw).toEqual([]);
      expect(service.label()).toEqual({ text: 'New deck', modified: false });
      collectionRequests();
    });

    it('leaves the open deck alone when another one is deleted', () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Open');
      service.deckDeleted('Other');
      expect(service.deckName()).toBe('Open');
      expect(service.deck().draw.length).toBe(1);
      collectionRequests();
    });
  });

  describe('new deck and the save prompts', () => {
    it("doesn't ask when there is nothing to lose", async () => {
      start();
      await service.newDeck();
      expect(dialogs).toEqual([]);
    });

    it('on New, OK saves first and then starts a new deck', async () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Mine');
      service.addFromCollection('1_13', 'FREE_PEOPLE');
      answers.push(true);
      await service.newDeck();
      expect(body(take('/deck')[0])).toMatchObject({ deckName: 'Mine', deckContents: '|||1_12,1_13' });
      expect(service.deckName()).toBeNull();
      expect(service.deck().draw).toEqual([]);
      collectionRequests();
    });

    it('on New, keeps the deck when the name prompt of the save is cancelled', async () => {
      start();
      service.addFromCollection('1_13', 'FREE_PEOPLE');
      answers.push(true, null);
      await service.newDeck();
      expect(service.deck().draw.length).toBe(1);
      expect(service.contentsDirty()).toBe(true);
    });

    it('leaving a modified deck: OK saves it', async () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Mine');
      service.addFromCollection('1_13', 'FREE_PEOPLE');
      answers.push(true);
      expect(await service.savePrompt()).toBe(true);
      expect(take('/deck').length).toBe(1);
      collectionRequests();
    });

    it("leaving a modified deck: OK, then a cancelled name, doesn't count as saved", async () => {
      start();
      service.addFromCollection('1_13', 'FREE_PEOPLE');
      answers.push(true, null);
      expect(await service.savePrompt()).toBe(false);
    });

    it('copying keeps the cards and notes and counts as unsaved', () => {
      start();
      service.setupDeck({ ...deckOf('1_12'), notes: 'n' }, 'Mine');
      service.copyDeck();
      expect(service.deck().draw.length).toBe(1);
      expect(service.notes()).toBe('n');
      expect(service.contentsDirty()).toBe(true);
      collectionRequests();
    });
  });

  describe('the Convert button', () => {
    it('is off for formats that are neither PC nor Anything Goes', () => {
      start();
      service.selectFormat(1);
      expect(service.convertErrataEnabled()).toBe(false);
      service.addFromCollection('1_12', 'FREE_PEOPLE');
      expect(service.convertErrataEnabled()).toBe(false);
      collectionRequests();
    });

    it('is on for Anything Goes once the player selects it', () => {
      start();
      service.selectFormat(1);
      service.selectFormat(0);
      expect(service.convertErrataEnabled()).toBe(true);
      collectionRequests();
    });

    it('stays usable after a 400, with a message', () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Mine');
      service.convertErrata();
      take('/deck/convert')[0].flush('', { status: 400, statusText: 'x' });
      expect(dialogs).toEqual(['alert: Error processing errata conversion.']);
      expect(service.convertErrataEnabled()).toBe(true);
      collectionRequests();
    });

    it('reports other failures without changing the button', () => {
      start();
      service.selectFormat(1);
      service.convertErrata();
      take('/deck/convert')[0].flush('', { status: 500, statusText: 'x' });
      expect(dialogs).toEqual(['alert: There was a problem during communication with server']);
      expect(service.convertErrataEnabled()).toBe(false);
      collectionRequests();
    });

    it('sends the deck for its effective format', () => {
      start();
      service.selectFormat(7);
      service.convertErrata();
      expect(body(take('/deck/convert')[0])).toMatchObject({ targetFormat: 'pc_movie' });
      collectionRequests();
    });
  });

  describe('opening decks', () => {
    it('shows a deck from the server, unmodified, with its name and notes', () => {
      start();
      service.loadDeck('Saved');
      take('/deck')[0].flush(readServerResponse('deck.xml'));
      expect(service.deckName()).toBe('Saved');
      expect(service.label()).toEqual({ text: 'Saved', modified: false });
      expect(service.notes()).toBe('Notes with + plus & ampersand');
      expect(service.contentsDirty()).toBe(false);
      expect(service.selectedFormat().value).toBe('fotr_block');
      expect(service.deck().ringBearer.map((c) => c.blueprintId)).toEqual(['1_2']);
      expect(service.deck().sites.length).toBe(9);
      collectionRequests();
    });

    it('reloads the collection for the opened deck', () => {
      start();
      service.setupDeck(deckOf('1_12'), 'Saved');
      expect(collectionRequests().length).toBeGreaterThan(0);
    });

    it('opens a library deck under its name, as unsaved', () => {
      start();
      service.loadLibraryDeck('From library');
      take('/deck/library')[0].flush(readServerResponse('deck.xml'));
      expect(service.label()).toEqual({ text: 'From library', modified: true });
      expect(service.contentsDirty()).toBe(true);
      collectionRequests();
    });

    it('keeps the current deck when the server has no such deck', () => {
      start();
      service.addFromCollection('1_12', 'FREE_PEOPLE');
      service.loadDeck('Gone');
      take('/deck')[0].flush(readServerResponse('deck-missing.xml'));
      expect(service.deck().draw.length).toBe(1);
    });

    it.each(['/deck', '/deck/library'])('reports a failure of %s', (path) => {
      start();
      if (path === '/deck') service.loadDeck('x');
      else service.loadLibraryDeck('x');
      take(path)[0].flush('', { status: 500, statusText: 'x' });
      expect(dialogs).toEqual(['alert: There was a problem during communication with server']);
    });

    it('puts cards in their slots and ignores draw cards of neither side', () => {
      start();
      service.setupDeck(
        {
          targetFormat: null,
          notes: '',
          ringBearer: '1_290',
          ring: '1_2',
          map: '101_1',
          sites: ['1_319', '1_327'],
          drawDeck: [
            { blueprintId: '1_12', side: 'FREE_PEOPLE' },
            { blueprintId: '1_13', side: 'SHADOW' },
            { blueprintId: '1_14', side: 'UNKNOWN' as never },
          ],
        },
        'Full',
      );
      const deck = service.deck();
      expect(deck.ringBearer.map((c) => c.blueprintId)).toEqual(['1_290']);
      expect(deck.ring.map((c) => c.blueprintId)).toEqual(['1_2']);
      expect(deck.map.map((c) => c.blueprintId)).toEqual(['101_1']);
      expect(deck.sites.length).toBe(2);
      expect(deck.draw.map((c) => c.blueprintId)).toEqual(['1_12', '1_13']);
      collectionRequests();
    });

    it('uses the format code when the deck belongs to a league that is not running', () => {
      start();
      service.setupDeck(
        { ...deckOf('1_12'), targetFormat: { formatCode: 'fotr_block', formatName: 'x', leagueCode: 'ended' } },
        'Old league',
      );
      expect(service.selectedFormat().value).toBe('fotr_block');
      collectionRequests();
    });

    it("opens a deck under the player's limited collection of its format, and checks it against that", () => {
      start();
      service.setupDeck(
        { ...deckOf('1_12'), targetFormat: { formatCode: 'fotr_block', formatName: 'x', leagueCode: null } },
        'Limited',
      );
      expect(service.selectedFormat().label).toBe('Sealed League - FotR');
      expect(service.effectiveFormat()).toBe('fotr_block');
      expect(body(statsRequest())).toMatchObject({ targetFormat: 'fotr_block', collectionName: '1699999999:sealed' });
      collectionRequests();
    });

    it('keeps the selected format for a deck without one', () => {
      start();
      service.selectFormat(1);
      service.setupDeck(deckOf('1_12'), 'No format');
      expect(service.selectedFormat().value).toBe('fotr_block');
      collectionRequests();
    });
  });

  describe('changing the format', () => {
    it('marks a saved deck modified, but not a new one', () => {
      start();
      service.selectFormat(1);
      expect(service.contentsDirty()).toBe(false);
      service.setupDeck(deckOf('1_12'), 'Mine');
      service.selectFormat(2);
      expect(service.contentsDirty()).toBe(true);
      expect(service.label()).toEqual({ text: 'Mine', modified: true });
      collectionRequests();
    });

    it('filters the collection for the new format', () => {
      start();
      service.selectFormat(1);
      expect(collectionRequests().at(-1)!.request.params.get('filter')).toContain('format:fotr_block');
    });

    it('removes the Race slot when leaving an RTMD league', () => {
      start();
      service.selectFormat(7);
      service.selectFormat(0);
      expect(service.usesRace()).toBe(false);
      expect(service.raceCard()).toBeNull();
      expect(service.metaSites()).toBeNull();
      collectionRequests();
    });

    it('counts the Race card as on the deck side', () => {
      start();
      service.selectFormat(7);
      TestBed.tick();
      expect(collection.available('90_7', 1)).toBe(0);
      collectionRequests();
    });
  });

  describe('starting the page', () => {
    it.each([
      [400, 'alert: Could not retrieve formats.'],
      [500, 'alert: There was a problem during communication with server'],
    ])('reports a %i for the formats', (status, message) => {
      service.init();
      take('/deck/formats')[0].flush('', { status, statusText: 'x' });
      expect(dialogs).toEqual([message]);
    });

    it('reports a failure of the limited collections, and asks for no leagues', () => {
      service.init();
      take('/deck/formats')[0].flush(JSON.stringify(FORMATS));
      take('/collection')[0].flush('', { status: 500, statusText: 'x' });
      expect(dialogs).toEqual(['alert: There was a problem during communication with server']);
      expect(take('/league').length).toBe(0);
    });

    it('reports a failure of the leagues', () => {
      service.init();
      take('/deck/formats')[0].flush(JSON.stringify(FORMATS));
      take('/collection')[0].flush(readServerResponse('collection-types.xml'));
      take('/league')[0].flush('', { status: 500, statusText: 'x' });
      expect(dialogs).toEqual(['alert: There was a problem during communication with server']);
      expect(service.formatOptions().at(-1)?.label).toBe('Tournament draft');
    });

    it('loads the formats before the limited collections', () => {
      service.init();
      expect(take('/collection').length).toBe(0);
    });
  });

  describe('importing a decklist', () => {
    const IMPORTED = `<?xml version="1.0"?><collection count="4">
      <card blueprintId="1_290" count="1" group="ringBearer" side="FREE_PEOPLE"/>
      <card blueprintId="1_2" count="1" group="ring"/>
      <card blueprintId="1_12" count="3" group="fp" side="FREE_PEOPLE"/>
      <pack blueprintId="FotR - Booster" count="1"/>
    </collection>`;
    const importRequest = () => backend.match((req) => req.url.includes('/collection/import/'));

    it('replaces the deck with what the server recognised', () => {
      start();
      service.setupDeck(deckOf('1_99'), 'Before');
      collectionRequests();
      service.importDecklist('1x Gandalf');
      expect(service.deckName()).toBeNull();
      importRequest()[0].flush(IMPORTED);
      const deck = service.deck();
      expect(deck.ringBearer.map((c) => c.blueprintId)).toEqual(['1_290']);
      expect(deck.ring.map((c) => c.blueprintId)).toEqual(['1_2']);
      expect(deck.draw.map((c) => c.blueprintId)).toEqual(['1_12', '1_12', '1_12']);
      expect(service.label()).toEqual({ text: 'Imported Deck (unsaved)', modified: false });
      expect(service.contentsDirty()).toBe(true);
    });

    it.each([
      [414, 'alert: Deck too large to import.'],
      [500, 'alert: There was a problem during communication with server'],
    ])('reports a %i', (status, message) => {
      start();
      service.importDecklist('1x Gandalf');
      importRequest()[0].flush('', { status, statusText: 'x' });
      expect(dialogs).toEqual([message]);
    });
  });

  describe('editing', () => {
    it('counts changed notes as a modification, and unchanged ones not', () => {
      start();
      service.setNotes('');
      expect(service.contentsDirty()).toBe(false);
      service.setNotes('new');
      expect(service.notes()).toBe('new');
      expect(service.contentsDirty()).toBe(true);
    });

    it('removes a card from a slot', () => {
      start();
      service.slotClicked('ring');
      service.addFromCollection('1_2', null);
      const [ring] = service.deck().ring;
      service.deckModified(false);
      service.removeSlotCard('ring', ring.id);
      expect(service.deck().ring).toEqual([]);
      expect(service.contentsDirty()).toBe(true);
      collectionRequests();
    });

    it('removes a copy from the draw deck', () => {
      start();
      service.addFromCollection('1_12', 'FREE_PEOPLE');
      service.addFromCollection('1_12', 'FREE_PEOPLE');
      service.removeDrawCard(service.deck().draw[0].id);
      expect(service.deck().draw.length).toBe(1);
    });

    it('goes back to adding by side after a special slot was filled', () => {
      start();
      service.slotClicked('ring');
      service.addFromCollection('1_2', null);
      service.addFromCollection('1_12', 'FREE_PEOPLE');
      expect(service.deck().ring.length).toBe(1);
      expect(service.deck().draw.length).toBe(1);
      collectionRequests();
    });

    it('shows the sites when a filled sites column is clicked too', () => {
      start();
      service.slotClicked('sites');
      service.addFromCollection('1_319', null);
      collectionRequests();
      service.slotClicked('sites');
      expect(collection.fields()).toMatchObject({ cardType: 'SITE' });
      collectionRequests();
    });

    it('tells the collection which cards are in the deck, for "Special in Deck"', () => {
      start();
      service.addFromCollection('1_12', 'FREE_PEOPLE');
      service.addFromCollection('1_13', 'FREE_PEOPLE');
      TestBed.inject(CollectionQuery).setField('product', 'specialForDeck');
      expect(collectionRequests().at(-1)!.request.params.get('filter')).toContain('blueprints:1_12,1_13');
    });
  });

  describe('deck stats', () => {
    it('sends one request for several changes in a row', () => {
      start();
      statsRequest().flush('');
      service.addFromCollection('1_12', 'FREE_PEOPLE');
      service.addFromCollection('1_13', 'FREE_PEOPLE');
      service.addFromCollection('1_14', 'FREE_PEOPLE');
      vi.advanceTimersByTime(STATS_INTERVAL_MS);
      const requests = take('/deck/stats');
      expect(requests.length).toBe(1);
      expect(body(requests[0])['deckContents']).toBe('|||1_12,1_13,1_14');
    });

    it('refreshes after the format changes and after the deck is emptied', async () => {
      start();
      statsRequest().flush('');
      service.selectFormat(1);
      expect(body(statsRequest())['targetFormat']).toBe('fotr_block');
      await service.newDeck();
      expect(statsRequest()).toBeDefined();
      collectionRequests();
    });

    it.each([
      [400, 'alert: Invalid deck for getting stats.'],
      [500, 'alert: There was a problem during communication with server'],
    ])('reports a %i and keeps the last stats', (status, message) => {
      start();
      statsRequest().flush('<b>Free People</b>: 0');
      const before = service.stats();
      service.addFromCollection('1_12', 'FREE_PEOPLE');
      statsRequest().flush('', { status, statusText: 'x' });
      expect(dialogs).toEqual([message]);
      expect(service.stats()).toBe(before);
    });
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
