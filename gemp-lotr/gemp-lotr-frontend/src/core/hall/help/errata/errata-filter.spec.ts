import { readServerResponse } from '../../../../testing/files';
import { parseErrataCatalog } from './errata-catalog';
import {
  EMPTY_FILTERS,
  errataAddress,
  errataStatus,
  filterEntries,
  filtersOfParams,
  findCard,
  formatOptions,
  indexOfCard,
  isErrataLink,
  matchesFilters,
  searchedNumber,
  setOptions,
  sortEntries,
  sortOfParams,
  viewChips,
  withChosen,
} from './errata-filter';
import { ErrataFilters } from './errata-models';

describe('errata filters and sorts', () => {
  const { entries, formats } = parseErrataCatalog(JSON.parse(readServerResponse('errata.json')));
  const ids = (filters: Partial<ErrataFilters>) =>
    filterEntries(entries, { ...EMPTY_FILTERS, ...filters }).map((e) => e.id);

  it('reads a card number typed in any of the ways players write it (searchedNumber)', () => {
    expect(['1.45', '1_45', '1-45', '1 45', ' 01 . 045 '].map(searchedNumber)).toEqual(Array(5).fill('1_45'));
    expect(searchedNumber('1R45')).toBeNull();
    expect(searchedNumber('gandalf')).toBeNull();
  });

  it('finds a card by its title (accents and case folded), collector’s number or set and number (matches)', () => {
    expect(ids({ q: 'LORIEN' })).toEqual(['51_100', '71_100']);
    expect(ids({ q: 'ulairë' })).toEqual(['103_19']);
    expect(ids({ q: '1r45' })).toEqual(['51_45']);
    expect(ids({ q: 'v3 19' })).toEqual(['103_19']);
    expect(ids({ q: '1.100' })).toEqual(['51_100', '71_100']);
    expect(ids({ q: '51_45' })).toEqual(['51_45']);
    expect(ids({ q: '  ' })).toHaveLength(7);
  });

  it('filters by set, format and the Latest view, together', () => {
    expect(ids({ set: '1' })).toEqual(['51_45', '51_100', '71_100']);
    expect(ids({ format: 'pc_playtest' })).toEqual(['71_100']);
    expect(ids({ show: 'recent' })).toEqual(['51_45', '55_10', '103_19']);
    expect(ids({ set: '1', show: 'recent', q: 'gandalf' })).toEqual(['51_45']);
    expect(matchesFilters(entries[0]!, { ...EMPTY_FILTERS, set: '2' })).toBe(false);
  });

  it('sorts by any column either way, ties by set and number (sort)', () => {
    expect(sortEntries(entries, { key: 'num', dir: -1 }).map((e) => e.id)).toEqual([
      '103_19',
      '101_5',
      '55_10',
      '52_120',
      '71_100',
      '51_100',
      '51_45',
    ]);
    // a card without a culture sorts last
    expect(sortEntries(entries, { key: 'culture', dir: 1 }).map((e) => e.culture ?? '-')).toEqual([
      'Elven',
      'Elven',
      'Gandalf',
      'Gondor',
      'Ringwraith',
      'Shire',
      '-',
    ]);
    expect(sortEntries(entries, { key: 'rev', dir: -1 }).map((e) => e.id)).toEqual([
      '51_100',
      '51_45',
      '52_120',
      '55_10',
      '101_5',
      '103_19',
      '71_100',
    ]);
    expect(sortEntries(entries, { key: 'name', dir: 1 }).map((e) => e.name)[0]).toBe("Bilbo's Pipe");
  });

  it('finds a card by its id or its original’s', () => {
    expect(indexOfCard(entries, '1_100')).toBe(1);
    expect(indexOfCard(entries, '71_100')).toBe(2);
    expect(indexOfCard(entries, '9_9')).toBe(-1);
    expect(findCard(entries, '5_10')?.id).toBe('55_10');
    expect(findCard(entries, '9_9')).toBeNull();
  });

  it('counts the View chips, leaving out one that would show nothing', () => {
    expect(viewChips(entries)).toEqual([
      { value: '', label: 'All', count: 7 },
      { value: 'recent', label: 'Latest', count: 3 },
    ]);
    expect(viewChips(entries.filter((e) => !e.recent)).map((chip) => chip.label)).toEqual(['All']);
  });

  it('lists the sets by number and the formats with errata in hall order, without the hidden ones', () => {
    expect(setOptions(entries)).toEqual([
      { value: '1', label: '1 – The Fellowship of the Ring (3)' },
      { value: '2', label: '2 – Mines of Moria (1)' },
      { value: '5', label: "5 – Battle of Helm's Deep (1)" },
      { value: '101', label: '101 (1)' },
      { value: '103', label: '103 – Shadow of the Past (1)' },
    ]);
    expect(formatOptions(formats)).toEqual([
      { value: 'pc_fotr_block', label: 'PC-FotR Block (2)' },
      { value: 'pc_movie', label: 'PC-Movie Block (5)' },
      { value: 'pc_playtest', label: 'PC Playtest (1)' },
    ]);
  });

  it("keeps a link's value the list leaves out as the choice (syncControls)", () => {
    const options = formatOptions(formats);
    expect(withChosen(options, 'format', 'pc_movie', entries, formats)).toEqual(options);
    expect(withChosen(options, 'format', '', entries, formats)).toEqual(options);
    expect(withChosen(options, 'format', 'rev_tow_sta', entries, formats).at(-1)).toEqual({
      value: 'rev_tow_sta',
      label: 'Towers Standard (1)',
      extra: true,
    });
    expect(withChosen(options, 'format', 'nope', entries, formats).at(-1)?.label).toBe('nope (0)');
    expect(withChosen(setOptions(entries), 'set', '40', entries, formats).at(-1)?.label).toBe('40 (0)');
  });

  it('says how many cards show, or why none do (renderStatus)', () => {
    expect(errataStatus(7, 7, EMPTY_FILTERS, formats)).toEqual({ text: '7 cards', empty: false });
    expect(errataStatus(3, 7, EMPTY_FILTERS, formats).text).toBe('3 of 7 cards');
    expect(errataStatus(0, 7, { ...EMPTY_FILTERS, format: 'fotr_block' }, formats)).toEqual({
      text: 'No PC errata are in effect in Fellowship Block.',
      empty: true,
    });
    expect(errataStatus(0, 7, { ...EMPTY_FILTERS, format: 'x' }, formats).text).toBe(
      'No PC errata are in effect in x.',
    );
    expect(errataStatus(0, 7, { ...EMPTY_FILTERS, format: 'x', q: 'a' }, formats).text).toBe(
      'No errata match these filters.',
    );
  });

  it('reads a link and writes the address of what is on show (applyRoute, buildHash)', () => {
    expect(filtersOfParams({ q: 'a b', format: 'pc_movie', other: 'x' })).toEqual({
      q: 'a b',
      set: '',
      format: 'pc_movie',
      show: '',
    });
    expect(sortOfParams({ sort: 'rev', dir: 'desc' })).toEqual({ key: 'rev', dir: -1 });
    expect(sortOfParams({ sort: 'name' })).toEqual({ key: 'name', dir: 1 });
    expect(sortOfParams({ sort: 'bogus' })).toBeNull();
    expect(isErrataLink(null, {})).toBe(false);
    expect(isErrataLink(null, { other: 'x' })).toBe(false);
    expect(isErrataLink(null, { show: '' })).toBe(true);
    expect(isErrataLink('1_45', {})).toBe(true);
    expect(errataAddress('51_45', { ...EMPTY_FILTERS, show: 'recent', q: 'g' })).toEqual({
      path: '/help/errata/51_45',
      queryParams: { q: 'g', show: 'recent' },
      fragment: null,
    });
    expect(errataAddress(null, EMPTY_FILTERS)).toEqual({ path: '/help/errata', queryParams: {}, fragment: null });
  });
});
