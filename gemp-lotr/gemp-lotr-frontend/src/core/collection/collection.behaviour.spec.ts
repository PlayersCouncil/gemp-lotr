import { provideDeckbuilderState } from '../deckbuilder-state';
import { provideGempHttp } from '../api/gemp-interceptors';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../testing/files';
import { FakeNotifier, provideFakeNotifier } from '../../testing/fake-notifier';
import { BlockFilter } from '../api/models';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { combined, Public } from '../../testing/combined';
import { DeckDraft } from '../deck/deck-draft.service';
import { addDrawCopy } from '../deck/deck-model';
import { CollectionBrowser, INITIAL_PAGE_SIZE } from './collection-browser.service';
import { CollectionCounts } from './collection-counts.service';
import { CollectionQuery, DEFAULT_FILTER_OVERRIDE } from './collection-query.service';

type Collection = Public<CollectionQuery> & Public<CollectionBrowser> & Public<CollectionCounts>;

const COLLECTION_XML = readServerResponse('collection-default.xml');

const BLOCKS: BlockFilter[] = [
  {
    filter: 'fotr_block',
    name: 'Fellowship Block',
    setFilters: { 'The Fellowship of the Ring': '1', 'Mines of Moria': '2' },
  },
  { filter: 'ttt_block', name: 'Towers Block', setFilters: { 'The Two Towers': '4' } },
];

describe('the collection', () => {
  let service: Collection;
  let backend: HttpTestingController;
  let notifier: FakeNotifier;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ...provideDeckbuilderState(),
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
      ],
    });
    notifier = TestBed.inject(FakeNotifier);
    service = combined<Collection>(
      TestBed.inject(CollectionQuery),
      TestBed.inject(CollectionBrowser),
      TestBed.inject(CollectionCounts),
    );
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    backend.verify();
    vi.restoreAllMocks();
  });

  const pending = (): TestRequest[] => backend.match((req) => req.url.includes('/collection/'));
  const params = (req: TestRequest) => ({
    type: req.request.url.replace(/.*\/collection\//, ''),
    filter: req.request.params.get('filter'),
    start: req.request.params.get('start'),
    count: req.request.params.get('count'),
  });
  const answer = (req: TestRequest, total = 30) =>
    req.flush(COLLECTION_XML.replace(/<collection count="\d+"/, `<collection count="${total}"`));

  /** Puts cards in the deck, for what the collection takes from it. */
  function deckHolds(...blueprints: string[]): void {
    for (const blueprintId of blueprints) {
      TestBed.inject(DeckDraft).deck.update((deck) => addDrawCopy(deck, blueprintId, 'FREE_PEOPLE'));
    }
  }

  /** The page startup: format first, then the default filter (the format's request is superseded). */
  function start(): TestRequest {
    service.updateFormat('rev_tow_sta', BLOCKS);
    service.setFilterOverride(DEFAULT_FILTER_OVERRIDE);
    const requests = pending();
    expect(requests.length).toBe(2);
    expect(requests[0].cancelled).toBe(true);
    return requests[1];
  }

  it('loads the first page with the default filter', () => {
    const req = start();
    expect(params(req)).toEqual({
      type: 'default',
      filter: 'format:rev_tow_sta sort:name cardType:-THE_ONE_RING',
      start: '0',
      count: `${INITIAL_PAGE_SIZE}`,
    });
    answer(req);
    expect(service.page()?.items.length).toBeGreaterThan(0);
    expect(service.previousDisabled()).toBe(true);
    expect(service.nextDisabled()).toBe(false);
    expect(service.sliderDisabled()).toBe(false);
  });

  it('pages forward and back, disabling the arrows while loading', () => {
    answer(start());
    service.nextPage();
    expect(service.previousDisabled()).toBe(true);
    expect(service.nextDisabled()).toBe(true);
    let [req] = pending();
    expect(params(req).start).toBe('12');
    answer(req, 30);
    expect(service.previousDisabled()).toBe(false);
    expect(service.nextDisabled()).toBe(false);

    service.nextPage();
    [req] = pending();
    answer(req, 30);
    expect(service.nextDisabled()).toBe(true);

    service.previousPage();
    [req] = pending();
    expect(params(req).start).toBe('12');
    answer(req, 30);
  });

  it('ignores the second click of a double click on the arrows while the page loads', () => {
    answer(start());
    service.nextPage();
    service.nextPage();
    const requests = pending();
    expect(requests.length).toBe(1);
    expect(params(requests[0]).start).toBe('12');
    answer(requests[0], 30);
  });

  it('starts from the first page with the new page size', () => {
    answer(start());
    service.nextPage();
    answer(pending()[0]);
    service.setPageSize(25);
    const [req] = pending();
    expect(params(req)).toMatchObject({ start: '0', count: '25' });
    answer(req);
  });

  it('shows only the newest answer', () => {
    answer(start());
    service.setPageSize(20);
    service.setPageSize(21);
    const [older, newer] = pending();
    expect(older.cancelled).toBe(true);
    answer(newer, 99);
    expect(service.page()?.total).toBe(99);
  });

  it('reloads only when the filter string changes', () => {
    answer(start());
    service.setField('name', 'gandalf');
    const [req] = pending();
    expect(params(req).filter).toBe('format:rev_tow_sta sort:name name:gandalf cardType:-THE_ONE_RING');
    answer(req);
    service.setField('name', 'gandalf');
    expect(pending().length).toBe(0);
  });

  it('clears a number when its comparison goes back to "Any"', () => {
    answer(start());
    service.setField('twilightCompare', 'EQUALS');
    service.setField('twilightValue', '3');
    answer(pending()[0]);
    service.setField('twilightCompare', '');
    expect(service.fields().twilightValue).toBe('');
    answer(pending()[0]);
  });

  it('rebuilds the sets when the block changes', () => {
    answer(start());
    expect(service.setOptions().map((o) => o.value)).toEqual(['', '1', '2', '4']);
    service.setField('block', 'ttt_block');
    expect(service.setOptions().map((o) => o.value)).toEqual(['', '4']);
    expect(service.fields().set).toBe('4');
    const [req] = pending();
    expect(params(req).filter).toBe('format:rev_tow_sta block:ttt_block set:4 sort:name cardType:-THE_ONE_RING');
    answer(req);
  });

  it("selects a format's only block and hides the cultures it lacks", () => {
    answer(start());
    service.toggleCulture('ROHAN');
    answer(pending()[0]);
    service.updateFormat('fotr_block', [BLOCKS[0]]);
    expect(service.fields().block).toBe('fotr_block');
    expect(service.hiddenCultureGroups().has('culture_freeps_movie1')).toBe(true);
    expect(service.fields().cultures).toEqual([]);
    const [req] = pending();
    expect(params(req).filter).toBe('format:fotr_block block:fotr_block sort:name cardType:-THE_ONE_RING');
    answer(req);
  });

  it('keeps the page when switching collections', () => {
    answer(start());
    service.nextPage();
    answer(pending()[0]);
    service.selectCollectionType('permanent');
    const [req] = pending();
    expect(params(req)).toMatchObject({ type: 'permanent', start: '12' });
    answer(req);
  });

  it('subtracts the deck copies from the counts', () => {
    deckHolds('1_2', '1_2', '1_2');
    expect(service.available('1_2', 4)).toBe(1);
    expect(service.available('1_3', 4)).toBe(4);
  });

  it('alerts when the player has no such collection', () => {
    answer(start());
    service.selectCollectionType('someLeague');
    pending()[0].flush('', { status: 404, statusText: 'Not Found' });
    expect(notifier.dialogs).toEqual(["alert: You don't have collection of that type."]);
  });

  describe('filter fields', () => {
    /** The filter string of the newest request, after the startup's has been answered. */
    function afterStart(): void {
      answer(start());
    }
    const lastFilter = () => params(pending().at(-1)!).filter;

    it('asks for pressed cultures in display order, and goes back to the first page', () => {
      afterStart();
      service.nextPage();
      answer(pending()[0]);
      service.toggleCulture('SHIRE');
      service.toggleCulture('DWARVEN');
      const requests = pending();
      expect(params(requests.at(-1)!).filter).toContain('culture:DWARVEN,SHIRE');
      expect(params(requests.at(-1)!).start).toBe('0');
    });

    it('releases a culture pressed a second time', () => {
      afterStart();
      service.toggleCulture('SHIRE');
      service.toggleCulture('SHIRE');
      expect(service.fields().cultures).toEqual([]);
      expect(lastFilter()).not.toContain('culture:');
    });

    it('"Reset advanced" clears the advanced fields only', () => {
      afterStart();
      service.setField('rarity', 'R');
      service.setField('name', 'Gandalf');
      service.setField('twilightCompare', 'EQUALS');
      service.setField('twilightValue', '3');
      service.toggleCulture('ELVEN');
      pending();
      service.resetAdvanced();
      expect(service.fields()).toMatchObject({ rarity: 'R', name: '', twilightCompare: '', twilightValue: '' });
      expect(service.fields().cultures).toEqual(['ELVEN']);
      const filter = lastFilter();
      expect(filter).toContain('rarity:R');
      expect(filter).not.toContain('name:');
    });

    it('"Reset all" clears everything and keeps the predefined filter', () => {
      afterStart();
      service.setField('rarity', 'R');
      service.toggleCulture('ELVEN');
      pending();
      service.resetAll();
      expect(service.fields()).toMatchObject({ rarity: '', sort: 'name', block: '', set: '' });
      expect(service.fields().cultures).toEqual([]);
      expect(lastFilter()).toBe('format:rev_tow_sta sort:name cardType:-THE_ONE_RING');
    });

    it('a predefined filter resets the fields and goes back to the first page', () => {
      afterStart();
      service.setField('rarity', 'R');
      pending();
      service.setFilterOverride('cardType:MAP product:card');
      expect(service.fields().rarity).toBe('');
      expect(lastFilter()).toContain('cardType:MAP product:card');
      expect(service.start()).toBe(0);
    });

    it('sends a comparison only once it has a number', () => {
      afterStart();
      service.setField('strengthCompare', 'GREATER_THAN');
      expect(pending().length).toBe(0);
      service.setField('strengthValue', '5');
      expect(lastFilter()).toContain('strengthCompare:GREATER_THAN strength:5');
    });

    it('asks for other sites only for a site search with a meta-site that allows them', () => {
      afterStart();
      service.setSiteOverride(true);
      service.setField('rarity', 'R');
      expect(lastFilter()).not.toContain('siteOverride');
      service.setField('cardType', 'SITE');
      expect(lastFilter()).toContain('siteOverride:true');
    });

    it('selects the first block when the current one is not in the new list', () => {
      afterStart();
      service.setField('block', 'ttt_block');
      pending();
      service.updateFormat('fotr_block', [BLOCKS[0]]);
      expect(service.fields().block).toBe('fotr_block');
      expect(service.setOptions().map((o) => o.label)).toEqual(['Any', 'The Fellowship of the Ring', 'Mines of Moria']);
      pending();
    });

    it('releases cultures the new format hides', () => {
      afterStart();
      service.updateFormat('x', [{ filter: 'ttt_block', name: 'Towers', setFilters: {} }]);
      pending();
      service.toggleCulture('ROHAN');
      pending();
      service.updateFormat('fotr_block', [BLOCKS[0]]);
      expect(service.fields().cultures).toEqual([]);
      pending();
    });

    it('lists the deck\'s cards for "Special in Deck"', () => {
      afterStart();
      deckHolds('1_12', '1_13');
      service.setField('product', 'specialForDeck');
      expect(lastFilter()).toContain('blueprints:1_12,1_13');
    });

    it('knows when the Product filter is on packs', () => {
      afterStart();
      expect(service.isPackProduct()).toBe(false);
      service.setField('product', 'pack');
      expect(service.isPackProduct()).toBe(true);
      pending();
    });
  });

  describe('paging and loading', () => {
    it('keeps the arrows and the slider disabled until the first page arrives', () => {
      expect(service.previousDisabled()).toBe(true);
      expect(service.nextDisabled()).toBe(true);
      expect(service.sliderDisabled()).toBe(true);
      answer(start());
      expect(service.previousDisabled()).toBe(true);
      expect(service.nextDisabled()).toBe(false);
      expect(service.sliderDisabled()).toBe(false);
    });

    it('disables the next arrow on the last page', () => {
      answer(start(), INITIAL_PAGE_SIZE);
      expect(service.nextDisabled()).toBe(true);
    });

    it('reloads the same page on load()', () => {
      answer(start());
      service.nextPage();
      answer(pending()[0]);
      service.load();
      expect(params(pending()[0]).start).toBe(String(INITIAL_PAGE_SIZE));
      service.load();
      pending();
    });

    it('asks the chosen collection', () => {
      answer(start());
      service.selectCollectionType('permanent');
      expect(service.collectionType()).toBe('permanent');
      expect(params(pending()[0]).type).toBe('permanent');
    });

    it('reports any other failure and keeps the page', () => {
      answer(start());
      const shown = service.page();
      service.load();
      pending()[0].flush('', { status: 500, statusText: 'x' });
      expect(notifier.dialogs).toEqual(['alert: There was a problem during communication with server']);
      expect(service.page()).toBe(shown);
    });

    it('goes on working after a failed request', () => {
      answer(start());
      service.load();
      pending()[0].flush('', { status: 500, statusText: 'x' });
      service.load();
      expect(pending().length).toBe(1);
    });
  });
});
