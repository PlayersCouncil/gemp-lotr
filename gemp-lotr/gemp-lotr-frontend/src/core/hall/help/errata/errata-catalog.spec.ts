import { readServerResponse } from '../../../../testing/files';
import { errataCollectorLabel, parseErrataCatalog } from './errata-catalog';

describe('parseErrataCatalog', () => {
  const catalog = parseErrataCatalog(JSON.parse(readServerResponse('errata.json')));

  it('reads the entries, formats and counts, leaving the old maps unread', () => {
    expect(catalog.entries.map((entry) => entry.id)).toEqual([
      '51_45',
      '51_100',
      '71_100',
      '52_120',
      '55_10',
      '103_19',
      '101_5',
    ]);
    expect(catalog.formats.map((format) => [format.code, format.count])).toEqual([
      ['pc_fotr_block', 2],
      ['pc_movie', 5],
      ['rev_tow_sta', 1],
      ['pc_playtest', 1],
      ['fotr_block', 0],
    ]);
    expect(catalog.counts).toEqual({ total: 7, errata: 4, playtest: 1, revision: 2, recent: 3 });
  });

  it("adds what the filters use, and shows the original's printed name when only the accents differ (prepare)", () => {
    const elf = catalog.entries[1];
    expect(elf).toMatchObject({
      name: 'Lórien Elf',
      set: '1',
      setNum: 1,
      cardNum: 100,
      coll: '1C100',
      searchName: 'lorien elf',
      searchColl: '1c100',
      kind: 'errata',
      revision: 2,
      recent: false,
    });
    expect(elf?.before?.stats).toEqual({ Twilight: 2, Strength: 5, Vitality: 2, Unique: false });
    const wraith = catalog.entries[5];
    expect(wraith).toMatchObject({ kind: 'revision', base: '103_19', coll: 'V3 19', culture: 'Ringwraith' });
    expect(wraith?.searchName).toBe('ulaire toldea, messenger of morgul');
  });

  it('reads what the server leaves out (JsonUtils.Serialize drops nulls) as none', () => {
    const pipe = catalog.entries[6];
    expect(pipe).toMatchObject({ setName: null, coll: '101 5', before: null, formats: [] });
    const site = catalog.entries[3];
    expect(site?.culture).toBeNull();
    expect(site?.cultureCode).toBeNull();
  });

  it('takes anything else as empty', () => {
    expect(parseErrataCatalog(null)).toEqual({
      entries: [],
      formats: [],
      counts: { total: 0, errata: 0, playtest: 0, revision: 0, recent: 0 },
    });
    const odd = parseErrataCatalog({
      entries: [{ name: 'no id' }, { id: '1_2', kind: 'odd', cardNum: '7x', set: 1, revision: 'x', formats: [1, 'a'] }],
    });
    expect(odd.entries).toHaveLength(1);
    expect(odd.entries[0]).toMatchObject({
      kind: 'errata',
      cardNum: 7,
      set: '1',
      revision: 0,
      formats: ['a'],
      name: '1_2',
    });
    expect(odd.counts.total).toBe(1);
  });

  it("labels a card by its collector's info, else its set and number (collectorLabel)", () => {
    expect(errataCollectorLabel(' 1U29 ', '1', 29)).toBe('1U29');
    expect(errataCollectorLabel('V3__19', '103', 19)).toBe('V3 19');
    expect(errataCollectorLabel(null, '101', 5)).toBe('101 5');
    expect(errataCollectorLabel('  ', '101', 5)).toBe('101 5');
  });
});
