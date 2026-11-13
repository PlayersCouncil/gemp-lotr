import { provideDeckbuilderState } from '../deckbuilder-state';
import { TestBed } from '@angular/core/testing';
import { CollectionTypeInfo, GempFormat, League } from '../api/models';
import { addToSlot, EMPTY_DECK } from './deck-model';
import { DeckFormatSelection } from './deck-format-selection.service';

const format = (code: string, name: string, order: number): GempFormat => ({
  code,
  name,
  order,
  hall: true,
  usesMaps: false,
  blockFilters: [{ filter: `${code}_block`, name: 'Block', setFilters: {} }],
  errata: {},
});
const FORMATS = {
  rev_tow_sta: format('rev_tow_sta', 'Anything Goes', 0),
  fotr_block: format('fotr_block', 'Fellowship Block', 1),
  pc_movie: format('pc_movie', 'Movie Block (PC)', 2),
};
const LIMITED: CollectionTypeInfo[] = [{ type: 'sealed1', name: 'Sealed', format: 'fotr_block' }];
const league = (code: string, formatCode: string, metaSites: League['metaSites']): League =>
  ({ code, formatCode, name: `League ${code}`, metaSites }) as League;
const SITE = { blueprintId: '91_1', visualBlueprintId: '90_3', siteOverride: true } as League['metaSites'][number];

describe('DeckFormatSelection', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: provideDeckbuilderState() }));

  let formats: DeckFormatSelection;

  beforeEach(() => (formats = TestBed.inject(DeckFormatSelection)));

  const selectValue = (value: string) =>
    formats.selectedFormatIndex.set(formats.formatOptions().findIndex((o) => o.value === value));

  it('offers Anything Goes until the formats arrive', () => {
    expect(formats.selectedFormat().value).toBe('rev_tow_sta');
    expect(formats.formatOptions().length).toBe(1);
  });

  it('lists the hall formats and keeps the current selection', () => {
    formats.showHallFormats(FORMATS);
    expect(formats.formatOptions().map((o) => o.value)).toEqual(['rev_tow_sta', 'fotr_block', 'pc_movie']);
    selectValue('pc_movie');
    formats.showHallFormats(FORMATS);
    expect(formats.selectedFormat().value).toBe('pc_movie');
  });

  it('appends the limited collections and then the leagues, keeping the selection', () => {
    formats.showHallFormats(FORMATS);
    selectValue('fotr_block');
    formats.addLimitedCollections(LIMITED);
    formats.addRtmdLeagues([league('L1', 'pc_movie', [SITE])]);
    expect(formats.selectedFormat().value).toBe('fotr_block');
    expect(formats.formatOptions().some((o) => o.value === 'L1' && o.rtmd)).toBe(true);
  });

  it("uses an RTMD league's own format as the effective one", () => {
    formats.showHallFormats(FORMATS);
    formats.addRtmdLeagues([league('L1', 'pc_movie', [SITE])]);
    selectValue('L1');
    expect(formats.effectiveFormat()).toBe('pc_movie');
    expect(formats.usesRace()).toBe(true);
  });

  it('uses a Map only in the movie formats', () => {
    formats.showHallFormats(FORMATS);
    expect(formats.usesMap()).toBe(false);
    selectValue('pc_movie');
    expect(formats.usesMap()).toBe(true);
  });

  it('builds the deck contents with the Map only when the format uses one', () => {
    formats.showHallFormats(FORMATS);
    const deck = addToSlot(EMPTY_DECK, 'map', '101_1');
    expect(formats.contentsOf(deck)).toBe('|||');
    selectValue('pc_movie');
    expect(formats.contentsOf(deck)).toBe('||||101_1');
  });

  describe('the Race card', () => {
    beforeEach(() => formats.showHallFormats(FORMATS));

    it('is absent outside a league', () => {
      expect(formats.raceCard()).toBeNull();
      expect(formats.metaSites()).toBeNull();
      expect(formats.usesRace()).toBe(false);
    });

    it('shows the position card with the modifier over it', () => {
      formats.addRtmdLeagues([league('L1', 'pc_movie', [SITE])]);
      selectValue('L1');
      expect(formats.raceCard()).toEqual({ blueprintId: '90_3', overlayBlueprintId: '91_1' });
    });

    it('shows the last meta-site, alone when it has no separate position card', () => {
      const plain = {
        blueprintId: '90_7',
        visualBlueprintId: null,
        siteOverride: false,
      } as League['metaSites'][number];
      formats.addRtmdLeagues([league('L1', 'pc_movie', [SITE, plain])]);
      selectValue('L1');
      expect(formats.raceCard()).toEqual({ blueprintId: '90_7', overlayBlueprintId: null });
    });

    it('still uses the Race slot for a league without meta-sites yet', () => {
      formats.addRtmdLeagues([league('L1', 'pc_movie', [])]);
      selectValue('L1');
      expect(formats.usesRace()).toBe(true);
      expect(formats.metaSites()).toBeNull();
      expect(formats.raceCard()).toBeNull();
    });
  });

  describe('the option for a saved deck', () => {
    beforeEach(() => {
      formats.showHallFormats(FORMATS);
      formats.addRtmdLeagues([league('L1', 'pc_movie', [])]);
    });

    const value = (target: { formatCode: string; leagueCode?: string | null }) =>
      formats.formatOptions()[formats.indexForDeck(target)].value;

    it('is the league while the league is running', () => {
      expect(value({ formatCode: 'pc_movie', leagueCode: 'L1' })).toBe('L1');
    });

    it('is the format once the league is over', () => {
      expect(value({ formatCode: 'fotr_block', leagueCode: 'ended' })).toBe('fotr_block');
    });

    it('is the first option for an unknown format', () => {
      expect(value({ formatCode: 'nope' })).toBe('rev_tow_sta');
    });
  });

  it("gives the collection the format's blocks, or none for an unknown format", () => {
    formats.showHallFormats(FORMATS);
    expect(formats.blockFilters('fotr_block')?.[0].filter).toBe('fotr_block_block');
    expect(formats.blockFilters('nope')).toBeNull();
  });
});
