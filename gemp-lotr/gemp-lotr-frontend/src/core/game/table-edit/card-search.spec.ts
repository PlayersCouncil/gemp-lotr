import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { GempFormat } from '../../api/models';
import { EMPTY_FILTER } from '../../card-filter/filter-fields';
import { FormatsService } from '../../formats/formats.service';
import { PageLifetime } from '../../platform/page-lifetime.service';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import {
  allBlocks,
  boundFields,
  boundOmits,
  boundText,
  CardSearch,
  CardSearchApi,
  readFields,
  readSearchPage,
  SEARCH_KEY,
  searchFilter,
} from './card-search';

const format = (code: string, blocks: GempFormat['blockFilters']): GempFormat => ({
  code,
  name: code,
  order: 0,
  hall: true,
  usesMaps: false,
  blockFilters: blocks,
  errata: {},
});

const FOTR = { filter: 'fotr_block', name: 'Fellowship Block', setFilters: { 'The Fellowship of the Ring': '1' } };
const PAGE = {
  total: 30,
  start: 0,
  cards: [
    { blueprintId: '1_50', name: 'Legolas, Greenleaf', type: 'COMPANION', culture: 'elven', site: false },
    { blueprintId: '1_319', name: 'Bag End', type: 'SITE', culture: null, site: true, siteNumber: 1, twilight: null },
    { name: 'no id' },
  ],
};

describe('card search', () => {
  afterEach(() => localStorage.clear());

  it('joins the blocks of every format, their sets with them', () => {
    const blocks = allBlocks({
      a: format('a', [FOTR]),
      b: format('b', [
        { ...FOTR, setFilters: { 'Mines of Moria': '2' } },
        { filter: 'hobbit', name: 'Hobbit', setFilters: {} },
      ]),
      c: format('c', null),
    });
    expect(blocks).toEqual([
      { ...FOTR, setFilters: { 'The Fellowship of the Ring': '1', 'Mines of Moria': '2' } },
      { filter: 'hobbit', name: 'Hobbit', setFilters: {} },
    ]);
  });

  it('reads the remembered filters, field by field, every block and set at first, never a product or rarity', () => {
    expect(readFields(null)).toEqual({ ...EMPTY_FILTER, block: '', set: '' });
    expect(readFields({ name: 'legolas', cultures: ['ELVEN', 3], rarity: 'R', product: 'pack', side: 7 })).toEqual({
      ...EMPTY_FILTER,
      block: '',
      set: '',
      name: 'legolas',
      cultures: ['ELVEN'],
    });
  });

  it("writes the person's own filter: the server holds it to the bound, only a fixed card type is not offered", () => {
    const fields = { ...readFields(null), name: 'son of', cardType: 'MINION', side: 'SHADOW' };
    expect(searchFilter(fields, null)).toBe('side:SHADOW cardType:MINION sort:name name:son name:of');
    // (the bound goes to the server by itself: the person's filter can only narrow it, so only the card type it fixes,
    // and a site number, are not offered, where a select offering others would only confuse)
    expect(boundFields({ filter: 'cardType:COMPANION', label: 'x' })).toEqual(['cardType']);
    expect(boundFields({ filter: 'set:1 side:FREE_PEOPLE', label: 'x' })).toEqual([]);
    expect(boundFields({ filter: 'cardType:SITE siteNumber:3', label: 'x' })).toEqual([
      'cardType',
      'siteNumberCompare',
      'siteNumberValue',
    ]);
    expect(boundFields({ filter: '', pool: 'set:1-6 cardtype:ALLY', label: 'x' })).toEqual(['cardType']);
    expect(boundFields(null)).toEqual([]);
    expect(searchFilter(fields, { filter: 'cardType:COMPANION', label: 'Companions' })).toBe(
      'side:SHADOW sort:name name:son name:of',
    );
  });

  it("leaves every field of what is always searched with to it, whatever case it writes its keys in (a slot's place)", () => {
    const bound = {
      filter: '',
      with: 'set:1-6 Culture:ROHAN cardtype:ALLY KEYWORD:VALIANT strength:5',
      label: 'a Rohan ally',
    };
    expect(boundFields(bound)).toEqual(['cardType', 'set', 'block', 'keyword', 'strengthCompare', 'strengthValue']);
    expect(boundOmits(bound)).toEqual([
      'cardType',
      'set',
      'block',
      'keyword',
      'strengthCompare',
      'strengthValue',
      'strength',
      'cultures',
    ]);
    expect(boundOmits(null)).toEqual([]);
    // The person's own values for those fields are never sent beside the bound's: they could only widen it
    const fields = {
      ...readFields(null),
      cultures: ['GONDOR'],
      keyword: 'ARCHER',
      strengthCompare: 'GREATER_THAN',
      strengthValue: '7',
      name: 'aldor',
    };
    expect(searchFilter(fields, bound)).toBe(
      'sort:name name:aldor set:1-6 Culture:ROHAN cardtype:ALLY KEYWORD:VALIANT strength:5',
    );
    for (const key of ['name', 'gametext', 'phase', 'race', 'itemclass', 'signet', 'sitenumbercompare', 'side']) {
      expect(boundFields({ filter: '', with: `${key}:X`, label: '' }).length).toBeGreaterThan(0);
    }
    expect(boundFields({ filter: '', with: 'block:fotr_block', label: 'x' })).toEqual(['block', 'set']);
  });

  it('says the bound in words, a line each for the pool, the bound and the place', () => {
    expect(boundText({ filter: 'culture:ROHAN', pool: 'set:1-6', with: 'cardtype:ALLY', label: 'x' })).toBe(
      'Pool: set:1-6\nBound: culture:ROHAN\nPlace: cardtype:ALLY',
    );
    expect(boundText({ filter: 'cardType:COMPANION', label: 'Companions' })).toBe('Bound: cardType:COMPANION');
  });

  it('reads a page of cards found, leaving out what is not a card', () => {
    expect(readSearchPage(PAGE)).toEqual({
      total: 30,
      start: 0,
      cards: [
        { blueprintId: '1_50', name: 'Legolas, Greenleaf', type: 'COMPANION', culture: 'elven', site: false },
        { blueprintId: '1_319', name: 'Bag End', type: 'SITE', culture: null, site: true },
      ],
    });
    expect(readSearchPage(null)).toEqual({ total: 0, start: 0, cards: [] });
  });

  describe('CardSearch', () => {
    let search: CardSearch;
    let backend: HttpTestingController;

    beforeEach(() => {
      localStorage.setItem(SEARCH_KEY, JSON.stringify({ name: 'legolas' }));
      TestBed.configureTestingModule({
        providers: [
          provideGempHttp(),
          provideHttpClientTesting(),
          { provide: PAGE_QUERY_STRING, useValue: '' },
          PageLifetime,
          FormatsService,
          CardSearchApi,
          CardSearch,
        ],
      });
      search = TestBed.inject(CardSearch);
      backend = TestBed.inject(HttpTestingController);
    });

    afterEach(() => backend.verify());

    const asked = () => {
      const request = backend.expectOne((r) => r.url === '/gemp-lotr-server/scenarios/search');
      return { params: request.request.params, request };
    };

    it('searches with the remembered filters within the bound, from the first page, and fills the Block list', () => {
      expect(search.isPackProduct()).toBe(false);
      expect(search.hiddenCultureGroups().size).toBe(0);
      search.open({ filter: 'cardType:COMPANION', label: 'Companions' });
      backend.expectOne('/gemp-lotr-server/deck/formats').flush({ Formats: { a: format('a', [FOTR]) } });
      expect(search.blockOptions().map((option) => option.value)).toEqual(['', 'fotr_block']);
      expect(search.setOptions().map((option) => option.value)).toEqual(['', '1']);
      expect(search.omitted()).toEqual(['product', 'rarity', 'cardType']);
      const { params, request } = asked();
      expect(params.get('filter')).toBe('sort:name name:legolas');
      expect(params.get('bound')).toBe('cardType:COMPANION');
      expect(params.has('pool')).toBe(false);
      expect(params.get('start')).toBe('0');
      expect(params.get('count')).toBe('18');
      expect(search.loading()).toBe(true);
      request.flush(PAGE);
      expect(search.loading()).toBe(false);
      expect(search.page()?.cards.length).toBe(2);
      expect(search.previousDisabled()).toBe(true);
      expect(search.nextDisabled()).toBe(false);
      expect(search.sliderDisabled()).toBe(false);
    });

    it("sends a slot's pool and bound for the server to search with by themselves, the place's kinds in the filter", () => {
      search.open({ filter: 'culture:ROHAN', pool: 'set:1-6', with: 'cardtype:ALLY', label: 'a Rohan ally' });
      backend.expectOne('/gemp-lotr-server/deck/formats').flush({ Formats: {} });
      expect(search.omitted()).toEqual(['product', 'rarity', 'cardType']);
      const { params, request } = asked();
      expect(params.get('filter')).toBe('sort:name name:legolas cardtype:ALLY');
      expect(params.get('bound')).toBe('culture:ROHAN');
      expect(params.get('pool')).toBe('set:1-6');
      request.flush(PAGE);
    });

    it('goes page by page, and changes the page size', () => {
      search.open();
      backend.expectOne('/gemp-lotr-server/deck/formats').flush({}, { status: 500, statusText: 'x' });
      expect(search.blockOptions().map((option) => option.value)).toEqual(['']);
      asked().request.flush(PAGE);
      search.previousPage();
      search.nextPage();
      let next = asked();
      expect(next.params.get('start')).toBe('18');
      next.request.flush({ ...PAGE, start: 18 });
      expect(search.nextDisabled()).toBe(true);
      search.nextPage();
      search.previousPage();
      next = asked();
      expect(next.params.get('start')).toBe('0');
      next.request.flush(PAGE);
      search.setPageSize(1000);
      next = asked();
      expect(next.params.get('count')).toBe('48');
      next.request.flush(PAGE);
      search.setPageSize(1);
      expect(asked().params.get('count')).toBe('6');
    });

    it('searches again from the first page as a filter changes, and remembers it; a later answer wins', () => {
      search.open();
      backend.expectOne('/gemp-lotr-server/deck/formats').flush({ Formats: {} });
      const stale = asked();
      search.setField('cardType', 'MINION');
      const fresh = asked();
      expect(fresh.params.get('filter')).toBe('cardType:MINION sort:name name:legolas');
      fresh.request.flush(PAGE);
      stale.request.flush({ total: 0, start: 0, cards: [] });
      expect(search.page()?.total).toBe(30);
      expect((JSON.parse(localStorage.getItem(SEARCH_KEY)!) as { cardType: unknown }).cardType).toBe('MINION');

      search.toggleCulture('ELVEN');
      expect(asked().params.get('filter')).toContain('culture:ELVEN');
      search.toggleCulture('ELVEN');
      expect(asked().params.get('filter')).not.toContain('culture:');
      search.setField('block', 'fotr_block');
      expect(search.fields().set).toBe('');
      asked().request.flush(PAGE);
      search.setField('strengthCompare', 'GREATER');
      search.setField('strengthValue', '5');
      backend.match(() => true);
      search.setField('strengthCompare', '');
      expect(search.fields().strengthValue).toBe('');
      asked().request.error(new ProgressEvent('down'));
      expect(search.page()).toEqual({ total: 0, start: 0, cards: [] });
      search.resetAdvanced();
      expect(search.fields().name).toBe('');
      asked();
      search.resetAll();
      expect(search.fields()).toEqual({ ...EMPTY_FILTER, block: '', set: '' });
      asked();
    });
  });
});
