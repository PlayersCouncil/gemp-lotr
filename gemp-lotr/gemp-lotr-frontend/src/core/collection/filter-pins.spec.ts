import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideFakeNotifier } from '../../testing/fake-notifier';
import { provideGempHttp } from '../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { provideDeckbuilderState } from '../deckbuilder-state';
import { CollectionQuery } from './collection-query.service';
import { EMPTY_FILTER } from './filter-fields';
import { FILTER_PINS_COOKIE, FilterPins, filterIsSet, parseFilterPins } from './filter-pins';

describe('filter pins', () => {
  describe('parseFilterPins', () => {
    it('reads the names kept shown, leaving out unknown ones', () => {
      expect([...parseFilterPins('side,cultures,name,bogus')]).toEqual(['side', 'cultures']);
      expect(parseFilterPins('').size).toBe(0);
      expect(parseFilterPins(null).size).toBe(0);
    });
  });

  describe('filterIsSet', () => {
    const first = (name: 'block' | 'set') => (name === 'block' ? 'all-blocks' : 'all-sets');
    const fields = { ...EMPTY_FILTER, block: 'all-blocks', set: 'all-sets' };

    it('is not set on the first choices, nor ever for the sort order', () => {
      for (const name of ['product', 'block', 'set', 'side', 'cardType', 'gametext', 'twilight', 'cultures'] as const) {
        expect(filterIsSet(name, fields, first)).toBe(false);
      }
      expect(filterIsSet('sort', { ...fields, sort: 'twilight' }, first)).toBe(false);
    });

    it('is set by another choice, text, a comparison or a pressed culture', () => {
      expect(filterIsSet('side', { ...fields, side: 'SHADOW' }, first)).toBe(true);
      expect(filterIsSet('block', { ...fields, block: '1' }, first)).toBe(true);
      expect(filterIsSet('gametext', { ...fields, gametext: 'ring' }, first)).toBe(true);
      expect(filterIsSet('strength', { ...fields, strengthCompare: 'MIN' }, first)).toBe(true);
      expect(filterIsSet('strength', { ...fields, strengthValue: '5' }, first)).toBe(false);
      expect(filterIsSet('cultures', { ...fields, cultures: ['ELVEN'] }, first)).toBe(true);
    });
  });

  describe('FilterPins', () => {
    let written: string[];

    function pins(cookie = '') {
      TestBed.configureTestingModule({
        providers: [
          ...provideDeckbuilderState(),
          provideGempHttp(),
          provideHttpClientTesting(),
          provideFakeNotifier(),
          { provide: PAGE_QUERY_STRING, useValue: '' },
        ],
      });
      written = [];
      vi.spyOn(document, 'cookie', 'get').mockReturnValue(cookie);
      vi.spyOn(document, 'cookie', 'set').mockImplementation((value: string) => void written.push(value));
      return TestBed.inject(FilterPins);
    }

    afterEach(() => vi.restoreAllMocks());

    it('starts with none kept shown, or the remembered ones', () => {
      expect(pins().pinned().size).toBe(0);
      TestBed.resetTestingModule();
      expect([...pins(`${FILTER_PINS_COOKIE}=cultures,side`).pinned()]).toEqual(['cultures', 'side']);
    });

    it('keeps a filter shown or not, and remembers it in the filter order', () => {
      const filterPins = pins();
      filterPins.toggle('cultures');
      filterPins.toggle('side');
      expect(filterPins.isPinned('side')).toBe(true);
      expect(written.at(-1)).toMatch(new RegExp(`^${FILTER_PINS_COOKIE}=side%2Ccultures; expires=.*; path=/gemp-lotr`));
      filterPins.toggle('side');
      expect(filterPins.isPinned('side')).toBe(false);
      expect(written.at(-1)).toMatch(new RegExp(`^${FILTER_PINS_COOKIE}=cultures;`));
    });

    it('counts the filters in the menu that narrow the cards', () => {
      const filterPins = pins();
      const query = TestBed.inject(CollectionQuery);
      expect(filterPins.hiddenSet()).toBe(0);
      query.setField('side', 'SHADOW');
      query.setField('name', 'Gandalf');
      query.setField('sort', 'twilight');
      query.toggleCulture('ELVEN');
      expect(filterPins.hiddenSet()).toBe(2);
      filterPins.toggle('side');
      expect(filterPins.hiddenSet()).toBe(1);
      query.resetAll();
      expect(filterPins.hiddenSet()).toBe(0);
    });

    it("doesn't count the block and set a format selects by itself", () => {
      const filterPins = pins();
      const query = TestBed.inject(CollectionQuery);
      query.updateFormat('fotr_block', [
        { filter: 'fotr_block', name: 'Fellowship Block', setFilters: { 'The Fellowship of the Ring': '1' } },
      ]);
      expect(query.fields().block).toBe('fotr_block');
      expect(filterPins.hiddenSet()).toBe(0);
      query.setField('block', '');
      expect(filterPins.hiddenSet()).toBe(1);
    });
  });
});
