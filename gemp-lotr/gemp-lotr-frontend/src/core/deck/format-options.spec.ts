import { GempFormat, League } from '../api/models';
import { hallFormatOptions, limitedFormatOptions, optionIndexOf, rtmdFormatOptions } from './format-options';

const format = (name: string, order: number, hall = true): GempFormat => ({
  code: name,
  name,
  order,
  hall,
  usesMaps: false,
  blockFilters: [],
  errata: {},
});

describe('format options', () => {
  it('lists the hall formats by their order', () => {
    const options = hallFormatOptions({
      b: format('B', 2),
      a: format('A', 0),
      hidden: format('Hidden', 1, false),
      first: format('First', -1),
    });
    expect(options.map((o) => o.value)).toEqual(['first', 'a', 'b']);
    expect(options[0]).toEqual({
      value: 'first',
      label: 'First',
      collectionType: 'default',
      disabled: false,
      rtmd: false,
    });
  });

  it('keeps only the later of two formats with the same order, as before', () => {
    expect(hallFormatOptions({ x: format('X', 3), y: format('Y', 3) }).map((o) => o.value)).toEqual(['y']);
  });

  it('adds the limited collections under a header, always shown', () => {
    expect(limitedFormatOptions([])).toEqual([
      { value: '---Limited---', label: '---Limited---', collectionType: null, disabled: true, rtmd: false },
    ]);
    const [, sealed] = limitedFormatOptions([{ type: 'league_1', name: 'Sealed league', format: 'fotr_block' }]);
    expect(sealed).toEqual({
      value: 'fotr_block',
      label: 'Sealed league',
      collectionType: 'league_1',
      disabled: false,
      rtmd: false,
    });
  });

  it('adds the RTMD leagues with the player position, only when there are some', () => {
    expect(rtmdFormatOptions([])).toEqual([]);
    const league: League = {
      code: '1700000002',
      name: 'Race',
      type: 'RTMD',
      member: true,
      formatCode: 'pc_fotr_block',
      playerPosition: 3,
      pathLength: 9,
      metaSites: [],
    };
    const [header, option] = rtmdFormatOptions([league]);
    expect(header.label).toBe('---Race to Mount Doom---');
    expect(option).toEqual({
      value: '1700000002',
      label: 'Race (pos 3/9)',
      collectionType: null,
      disabled: false,
      rtmd: true,
    });
  });

  it('selects the first option with a value, or the first option', () => {
    const options = [...hallFormatOptions({ a: format('A', 0), b: format('B', 1) }), ...limitedFormatOptions([])];
    expect(optionIndexOf(options, 'b')).toBe(1);
    expect(optionIndexOf(options, 'missing')).toBe(0);
  });

  it('selects the last option with a value when several have it, as a limited collection of that format does', () => {
    const option = (value: string, label: string) => ({
      value,
      label,
      collectionType: label,
      disabled: false,
      rtmd: false,
    });
    const options = [option('a', 'Format A'), option('b', 'Format B'), option('a', 'Sealed A')];
    expect(options[optionIndexOf(options, 'a')].label).toBe('Sealed A');
  });
});
