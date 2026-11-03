import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting, TestRequest } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../../testing/files';
import { BlockFilter } from '../api/models';
import { PAGE_QUERY_STRING } from '../session/session.service';
import { CollectionService, DEFAULT_FILTER_OVERRIDE, INITIAL_PAGE_SIZE } from './collection.service';

const COLLECTION_XML = readServerResponse('collection-default.xml');

const BLOCKS: BlockFilter[] = [
  {
    filter: 'fotr_block',
    name: 'Fellowship Block',
    setFilters: { 'The Fellowship of the Ring': '1', 'Mines of Moria': '2' },
  },
  { filter: 'ttt_block', name: 'Towers Block', setFilters: { 'The Two Towers': '4' } },
];

describe('CollectionService', () => {
  let service: CollectionService;
  let backend: HttpTestingController;
  let alerts: string[];

  beforeEach(() => {
    alerts = [];
    vi.spyOn(window, 'alert').mockImplementation((message) => alerts.push(String(message)));
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), { provide: PAGE_QUERY_STRING, useValue: '' }],
    });
    service = TestBed.inject(CollectionService);
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
    service.deckCounts.set(new Map([['1_2', 3]]));
    expect(service.availableCount('1_2', 4)).toBe(1);
    expect(service.availableCount('1_3', 4)).toBe(4);
  });

  it('alerts when the player has no such collection', () => {
    answer(start());
    service.selectCollectionType('someLeague');
    pending()[0].flush('', { status: 404, statusText: 'Not Found' });
    expect(alerts).toEqual(["You don't have collection of that type."]);
  });
});
