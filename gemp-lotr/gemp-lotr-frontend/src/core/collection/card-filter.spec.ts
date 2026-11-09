import { BlockFilter } from '../api/models';
import { blockOptions, setOptions } from './filter-block-sets';
import { hiddenCultureGroups, visibleCultures } from './filter-cultures';
import { EMPTY_FILTER, FilterFields, resetAdvanced, resetAll } from './filter-fields';
import { buildFilterString, FilterContext } from './filter-string';

const context = (overrides: Partial<FilterContext> = {}): FilterContext => ({
  format: 'rev_tow_sta',
  override: 'cardType:-THE_ONE_RING',
  siteOverride: false,
  cardsInDeck: () => [],
  ...overrides,
});

const fields = (overrides: Partial<FilterFields> = {}): FilterFields => ({
  ...EMPTY_FILTER,
  block: '',
  set: '',
  ...overrides,
});

const block = (filter: string, name: string, setFilters: Record<string, string>): BlockFilter => ({
  filter,
  name,
  setFilters,
});

describe('buildFilterString', () => {
  // Expected strings were captured from the legacy deck builder's requests for the same selections
  it('builds the default filter', () => {
    expect(buildFilterString(fields(), context())).toBe('format:rev_tow_sta sort:name cardType:-THE_ONE_RING');
  });

  it('lists the cultures in button order', () => {
    expect(buildFilterString(fields({ cultures: ['SHIRE', 'ELVEN'] }), context())).toBe(
      'format:rev_tow_sta sort:name culture:ELVEN,SHIRE cardType:-THE_ONE_RING',
    );
  });

  it('puts the fields in the legacy order', () => {
    expect(
      buildFilterString(
        fields({
          side: 'FREE_PEOPLES',
          sort: 'twilight,name',
          name: 'gandalf the',
          strengthCompare: 'LESS_THAN_OR_EQUAL_TO',
          strengthValue: '5',
        }),
        context(),
      ),
    ).toBe(
      'side:FREE_PEOPLES format:rev_tow_sta sort:twilight,name name:gandalf name:the ' +
        'strengthCompare:LESS_THAN_OR_EQUAL_TO strength:5 cardType:-THE_ONE_RING',
    );
    expect(buildFilterString(fields({ block: 'king_block', set: '8', cardType: 'SITE' }), context())).toBe(
      'format:rev_tow_sta block:king_block set:8 cardType:SITE sort:name cardType:-THE_ONE_RING',
    );
    expect(
      buildFilterString(fields({ product: 'pack', twilightCompare: 'GREATER_THAN', twilightValue: '3' }), context()),
    ).toBe('format:rev_tow_sta sort:name product:pack twilightCompare:GREATER_THAN twilight:3 cardType:-THE_ONE_RING');
  });

  it('needs both the comparison and the number', () => {
    expect(buildFilterString(fields({ vitalityCompare: 'EQUALS' }), context())).toBe(
      'format:rev_tow_sta sort:name cardType:-THE_ONE_RING',
    );
  });

  it('turns every space of the game text into "_"', () => {
    expect(buildFilterString(fields({ gametext: 'draw a card' }), context())).toContain('gametext:draw_a_card');
  });

  it('leaves out a field the predefined filter sets, instead of gluing its value on', () => {
    expect(
      buildFilterString(fields({ product: 'foil' }), context({ override: 'canStartWithRing:true product:card' })),
    ).toBe('format:rev_tow_sta sort:name canStartWithRing:true product:card');
  });

  it('asks for override sites only in site searches', () => {
    expect(buildFilterString(fields({ cardType: 'SITE' }), context({ siteOverride: true }))).toContain(
      'siteOverride:true',
    );
    expect(buildFilterString(fields(), context({ siteOverride: true }))).not.toContain('siteOverride');
  });

  it('lists the deck cards for "Special in Deck"', () => {
    expect(
      buildFilterString(fields({ product: 'specialForDeck' }), context({ cardsInDeck: () => ['1_2', '1_3'] })),
    ).toBe('format:rev_tow_sta sort:name product:specialForDeck blueprints:1_2,1_3 cardType:-THE_ONE_RING');
  });

  it('has no format before the formats are loaded', () => {
    expect(buildFilterString(EMPTY_FILTER, context({ format: null, override: '' }))).toBe(
      'block:0-33,50-200,404 set:0-34,50-200,404 sort:name',
    );
  });
});

describe('block and set lists', () => {
  const fotr = block('fotr_block', 'Fellowship Block', {
    'The Fellowship of the Ring': '1',
    'Mines of Moria': '2',
  });
  const pc = block('pc', 'PC', {
    'Latest PC Errata': 'pc_errata',
    'V1 - Shadow of the Past': '101',
    Promotional: '0',
  });

  it('offers "Any" plus the blocks, and picks a format\'s only block', () => {
    expect(blockOptions([fotr, pc]).options.map((o) => o.value)).toEqual(['', 'fotr_block', 'pc']);
    expect(blockOptions([fotr, pc]).selected).toBe('');
    expect(blockOptions([fotr]).selected).toBe('fotr_block');
  });

  it('sorts the sets by number with the PC errata last', () => {
    expect(setOptions([fotr, pc], '').options.map((o) => o.value)).toEqual(['', '0', '1', '2', '101', 'pc_errata']);
    expect(setOptions([fotr, pc], 'fotr_block').options.map((o) => o.label)).toEqual([
      'Any',
      'The Fellowship of the Ring',
      'Mines of Moria',
    ]);
  });

  it("picks a block's only set", () => {
    const single = block('x', 'X', { Only: '7' });
    expect(setOptions([single], 'x').selected).toBe('7');
    expect(setOptions([fotr], 'fotr_block').selected).toBe('');
  });
});

describe('culture buttons', () => {
  const blocks = (...filters: string[]) => filters.map((f) => block(f, f, {}));

  it('shows every culture of an Anything Goes-like format', () => {
    expect(hiddenCultureGroups(blocks('fotr_block', 'ttt_block', 'war_block', 'hobbit')).size).toBe(0);
  });

  it('hides the later cultures in Fellowship block', () => {
    expect([...hiddenCultureGroups(blocks('fotr_block'))].sort()).toEqual([
      'culture_freeps_movie1',
      'culture_freeps_movie2',
      'culture_hobbit',
      'culture_shadow_movie1',
      'culture_shadow_movie2',
      'culture_shadows',
    ]);
  });

  it('shows Gollum for the Hobbit game', () => {
    expect(hiddenCultureGroups(blocks('hobbit')).has('culture_freeps_movie2')).toBe(false);
  });

  it('releases the cultures of hidden groups', () => {
    const hidden = hiddenCultureGroups(blocks('fotr_block'));
    expect(visibleCultures(['ELVEN', 'ROHAN', 'MEN', 'SMAUG'], hidden)).toEqual(['ELVEN']);
  });
});

describe('reset buttons', () => {
  const used = fields({
    product: 'card',
    block: 'fotr_block',
    sort: 'culture,name',
    name: 'x',
    keyword: 'TALE',
    signet: 'FRODO',
    twilightCompare: 'EQUALS',
    twilightValue: '2',
    cultures: ['ELVEN'],
  });

  it('"Reset advanced filters only" keeps the top fields and the cultures', () => {
    const reset = resetAdvanced(used);
    expect(reset).toMatchObject({
      product: 'card',
      block: 'fotr_block',
      sort: 'culture,name',
      cultures: ['ELVEN'],
    });
    expect(reset).toMatchObject({
      name: '',
      keyword: '',
      signet: '',
      twilightCompare: '',
      twilightValue: '',
    });
  });

  it('"Reset all filters" clears everything, sorting by name again', () => {
    expect(resetAll(used)).toEqual(fields());
  });
});
