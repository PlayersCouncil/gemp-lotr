import { readServerResponse } from '../../../testing/files';
import { parseFormatDefinitions } from './format-definitions';
import { filterFormats, isHit } from './format-filter';
import { parseJson } from '../../api/json';

const formats = parseFormatDefinitions(parseJson(readServerResponse('format-definitions.json')));

describe('filterFormats (applyFilter)', () => {
  it('shows every format with no text', () => {
    const filter = filterFormats(formats, '   ');
    expect([...filter.visible]).toEqual(['pc_fotr_block', 'rev_tow_sta', 'pc_playtest']);
    expect(filter.status).toBe('');
    expect(filter.hits.size).toBe(0);
  });

  it('finds formats by name or code, ignoring case', () => {
    const filter = filterFormats(formats, 'PC');
    expect([...filter.visible]).toEqual(['pc_fotr_block', 'pc_playtest']);
    expect(filter.status).toBe('2 of 3 formats');
    expect(filterFormats(formats, 'rev_tow').status).toBe('1 of 3 formats');
  });

  it('finds the formats listing a card, from two letters on, ignoring accents, and marks the cards', () => {
    const filter = filterFormats(formats, 'eowyn');
    expect([...filter.visible]).toEqual(['pc_fotr_block']);
    expect(filter.status).toBe('1 of 3 formats (1 by a listed card)');
    expect(isHit(filter, 'pc_fotr_block', 'banned', '101_5')).toBe(true);
    expect(isHit(filter, 'pc_fotr_block', 'banned', '1_45')).toBe(false);
    expect(isHit(filter, 'pc_fotr_block', 'restricted', '101_5')).toBe(false);
    // "Gandalf" is a title on the R-list by title, not a card: only the card on Also legal counts
    const gandalf = filterFormats(formats, 'gandalf');
    expect([...(gandalf.hits.get('rev_tow_sta')?.keys() ?? [])]).toEqual(['valid']);
    // one letter matches names only
    expect([...filterFormats(formats, 'e').visible]).toEqual(['pc_fotr_block', 'rev_tow_sta', 'pc_playtest']);
    expect(filterFormats(formats, 'e').hits.size).toBe(0);
  });

  it('counts a format found by its name as not found by a card, even when a card matches too', () => {
    const filter = filterFormats(formats, 'fellowship');
    expect([...filter.visible]).toEqual(['pc_fotr_block']);
    expect(filter.status).toBe('1 of 3 formats');
    expect(filterFormats(formats, 'nothing like it').status).toBe('0 of 3 formats');
  });
});
