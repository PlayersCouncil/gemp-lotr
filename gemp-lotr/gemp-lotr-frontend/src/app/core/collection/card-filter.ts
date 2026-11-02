import { BlockFilter } from '../api/models';

/**
 * The collection filter of the legacy js/gemp-022/cards/CardFilter.js: the field values, their option lists, the
 * Block/Set lists built from the format, the culture buttons shown for the format, and the filter string sent to
 * GET /collection/{type}.
 */

export interface SelectOption {
  value: string;
  label: string;
}

/** The numeric comparisons; each has a compare select and a number input that shows once a comparison is chosen. */
export const NUMERIC_FILTERS = ['twilight', 'strength', 'vitality', 'siteNumber', 'resistance'] as const;
export type NumericFilter = (typeof NUMERIC_FILTERS)[number];

export interface FilterFields {
  product: string;
  rarity: string;
  block: string;
  set: string;
  side: string;
  sort: string;
  cardType: string;
  name: string;
  gametext: string;
  keyword: string;
  phase: string;
  race: string;
  itemClass: string;
  signet: string;
  twilightCompare: string;
  twilightValue: string;
  strengthCompare: string;
  strengthValue: string;
  vitalityCompare: string;
  vitalityValue: string;
  siteNumberCompare: string;
  siteNumberValue: string;
  resistanceCompare: string;
  resistanceValue: string;
  /** Culture codes whose button is pressed. */
  cultures: readonly string[];
}

/** The "Any" option the Block and Set lists start with, before a format has filled them. */
export const INITIAL_BLOCK_OPTION: SelectOption = { value: '0-33,50-200,404', label: 'Any' };
export const INITIAL_SET_OPTION: SelectOption = { value: '0-34,50-200,404', label: 'Any' };

export const EMPTY_FILTER: FilterFields = {
  product: '',
  rarity: '',
  block: INITIAL_BLOCK_OPTION.value,
  set: INITIAL_SET_OPTION.value,
  side: '',
  sort: 'name',
  cardType: '',
  name: '',
  gametext: '',
  keyword: '',
  phase: '',
  race: '',
  itemClass: '',
  signet: '',
  twilightCompare: '',
  twilightValue: '',
  strengthCompare: '',
  strengthValue: '',
  vitalityCompare: '',
  vitalityValue: '',
  siteNumberCompare: '',
  siteNumberValue: '',
  resistanceCompare: '',
  resistanceValue: '',
  cultures: [],
};

const opts = (...pairs: [string, string][]): SelectOption[] => pairs.map(([value, label]) => ({ value, label }));

export const PRODUCT_OPTIONS = opts(
  ['', 'Any'],
  ['pack', 'Packs'],
  ['card', 'Cards'],
  ['foil', 'Foils'],
  ['nonFoil', 'Non-foils'],
  ['tengwar', 'Tengwar'],
  ['special', 'Special'],
  ['specialForDeck', 'Special in Deck'],
);

export const RARITY_OPTIONS = opts(
  ['', 'Any'],
  ['R', 'Rare'],
  ['U', 'Uncommon'],
  ['C', 'Common'],
  ['A', 'Alternate Image'],
  ['P', 'Promo'],
  ['X', 'Rare+'],
  ['S', 'Fixed'],
  ['C,U,P,S', "Poorman's"],
);

export const SIDE_OPTIONS = opts(
  ['', 'Any'],
  ['SHADOW', 'Shadow'],
  ['FREE_PEOPLES', 'Free Peoples'],
  ['NONE', 'Neither'],
);

export const SORT_OPTIONS = opts(
  ['name', 'Name'],
  ['collinfo,set', "Collector's Info"],
  ['twilight,name', 'Twilight'],
  ['siteNumber,name', 'Site Number'],
  ['strength,name', 'Strength'],
  ['vitality,name', 'Vitality'],
  ['cardType,name', 'Card Type'],
  ['culture,name', 'Culture'],
);

export const CARD_TYPE_OPTIONS = opts(
  ['', 'Any'],
  ['COMPANION,ALLY,MINION', 'Characters'],
  ['POSSESSION,ARTIFACT', 'Items'],
  ['SITE', 'Sites'],
  ['ALLY', 'Allies'],
  ['ARTIFACT', 'Artifacts'],
  ['COMPANION', 'Companions'],
  ['CONDITION', 'Conditions'],
  ['EVENT', 'Events'],
  ['FOLLOWER', 'Followers'],
  ['MINION', 'Minions'],
  ['POSSESSION', 'Possessions'],
);

export const KEYWORD_OPTIONS = opts(
  ['', 'Any'],
  ['ARCHER', 'Archer'],
  ['BATTLEGROUND', 'Battleground'],
  ['BESIEGER', 'Besieger'],
  ['CORSAIR', 'Corsair'],
  ['DWELLING', 'Dwelling'],
  ['EASTERLING', 'Easterling'],
  ['ENDURING', 'Enduring'],
  ['ENGINE', 'Engine'],
  ['FIERCE', 'Fierce'],
  ['FOREST', 'Forest'],
  ['FORTIFICATION', 'Fortification'],
  ['HUNTER', 'Hunter'],
  ['KNIGHT', 'Knight'],
  ['LURKER', 'Lurker'],
  ['MACHINE', 'Machine'],
  ['MARSH', 'Marsh'],
  ['MOUNTAIN', 'Mountain'],
  ['MUSTER', 'Muster'],
  ['PIPEWEED', 'Pipeweed'],
  ['PLAINS', 'Plains'],
  ['RANGER', 'Ranger'],
  ['RING_BOUND', 'Ring-bound'],
  ['RIVER', 'River'],
  ['SEARCH', 'Search'],
  ['SOUTHRON', 'Southron'],
  ['SPELL', 'Spell'],
  ['STEALTH', 'Stealth'],
  ['TALE', 'Tale'],
  ['TENTACLE', 'Tentacle'],
  ['TRACKER', 'Tracker'],
  ['TWILIGHT', 'Twilight'],
  ['UNDERGROUND', 'Underground'],
  ['UNHASTY', 'Unhasty'],
  ['VALIANT', 'Valiant'],
  ['VILLAGER', 'Villager'],
  ['WARG_RIDER', 'Warg-rider'],
  ['WEATHER', 'Weather'],
  ['WISE', 'Wise'],
);

export const RACE_OPTIONS = opts(
  ['', 'Any'],
  ['BALROG', 'Balrog'],
  ['CREATURE', 'Creature'],
  ['CROW', 'Crow'],
  ['DRAGON', 'Dragon'],
  ['DWARF', 'Dwarf'],
  ['EAGLE', 'Eagle'],
  ['ELF', 'Elf'],
  ['ENT', 'Ent'],
  ['GIANT', 'Giant'],
  ['HALF_TROLL', 'Half-troll'],
  ['HOBBIT', 'Hobbit'],
  ['MAIA', 'Maia'],
  ['MAN', 'Man'],
  ['NAZGUL', 'Nazgul'],
  ['ORC', 'Orc'],
  ['SPIDER', 'Spider'],
  ['TREE', 'Tree'],
  ['TROLL', 'Troll'],
  ['URUK_HAI', 'Uruk-hai'],
  ['WARG', 'Warg'],
  ['WIZARD', 'Wizard'],
  ['WRAITH', 'Wraith'],
);

export const ITEM_CLASS_OPTIONS = opts(
  ['', 'Any'],
  ['CLASSLESS', 'Classless'],
  ['SUPPORT_AREA', 'Support Area'],
  ['ARMOR', 'Armor'],
  ['BOX', 'Box'],
  ['BRACERS', 'Bracers'],
  ['BROOCH', 'Brooch'],
  ['CLOAK', 'Cloak'],
  ['GAUNTLETS', 'Gauntlets'],
  ['HAND_WEAPON', 'Hand Weapon'],
  ['HELM', 'Helm'],
  ['MOUNT', 'Mount'],
  ['PALANTIR', 'Palantir'],
  ['PHIAL', 'Phial'],
  ['PIPE', 'Pipe'],
  ['PONY', 'Pony'],
  ['RANGED_WEAPON', 'Ranged Weapon'],
  ['RING', 'Ring'],
  ['SHIELD', 'Shield'],
  ['STAFF', 'Staff'],
);

export const PHASE_OPTIONS = opts(
  ['', 'Any'],
  ['FELLOWSHIP', 'Fellowship'],
  ['SHADOW', 'Shadow'],
  ['MANEUVER', 'Maneuver'],
  ['ARCHERY', 'Archery'],
  ['ASSIGNMENT', 'Assignment'],
  ['SKIRMISH', 'Skirmish'],
  ['REGROUP', 'Regroup'],
  ['RESPONSE', 'Response'],
);

export const SIGNET_OPTIONS = opts(
  ['', 'Any'],
  ['ARAGORN', 'Aragorn'],
  ['FRODO', 'Frodo'],
  ['GANDALF', 'Gandalf'],
  ['THEODEN', 'Theoden'],
);

export const COMPARE_OPTIONS = opts(
  ['', 'Any'],
  ['EQUALS', '='],
  ['GREATER_THAN', '>'],
  ['GREATER_THAN_OR_EQUAL_TO', '>='],
  ['LESS_THAN', '<'],
  ['LESS_THAN_OR_EQUAL_TO', '<='],
);

export interface Culture {
  code: string;
  image: string;
  /** The legacy markup gives the Hobbit cultures' labels no "culture" class. */
  cultureClass: boolean;
}

export type CultureGroupId =
  | 'culture_freeps_base1'
  | 'culture_freeps_movie1'
  | 'culture_freeps_base2'
  | 'culture_freeps_movie2'
  | 'culture_shadow_movie1'
  | 'culture_shadow_base1'
  | 'culture_shadow_movie2'
  | 'culture_shadow_base2'
  | 'culture_shadows'
  | 'culture_hobbit';

export interface CultureGroup {
  id: CultureGroupId;
  cultures: Culture[];
}

const svg = (code: string, file: string): Culture => ({
  code,
  image: `/gemp-lotr/images/cultures/${file}.svg`,
  cultureClass: true,
});
const png = (code: string, file: string): Culture => ({
  code,
  image: `/gemp-lotr/images/cultures/${file}.png`,
  cultureClass: false,
});

/** The culture buttons in display order (also the order of the culture codes in the filter). */
export const CULTURE_GROUPS: readonly CultureGroup[] = [
  {
    id: 'culture_freeps_base1',
    cultures: [svg('DWARVEN', 'dwarven'), svg('ELVEN', 'elven'), svg('GANDALF', 'gandalf'), svg('GONDOR', 'gondor')],
  },
  { id: 'culture_freeps_movie1', cultures: [svg('ROHAN', 'rohan')] },
  { id: 'culture_freeps_base2', cultures: [svg('SHIRE', 'shire')] },
  { id: 'culture_freeps_movie2', cultures: [svg('GOLLUM', 'gollum')] },
  { id: 'culture_shadow_movie1', cultures: [svg('DUNLAND', 'dunland')] },
  { id: 'culture_shadow_base1', cultures: [svg('ISENGARD', 'isengard'), svg('MORIA', 'moria')] },
  { id: 'culture_shadow_movie2', cultures: [svg('RAIDER', 'raider')] },
  { id: 'culture_shadow_base2', cultures: [svg('WRAITH', 'ringwraith'), svg('SAURON', 'sauron')] },
  {
    id: 'culture_shadows',
    cultures: [svg('MEN', 'men'), svg('ORC', 'orc'), svg('URUK_HAI', 'uruk')],
  },
  {
    id: 'culture_hobbit',
    cultures: [
      png('ESGAROTH', 'esgaroth'),
      png('GUNDABAD', 'gundabad'),
      png('MIRKWOOD', 'mirkwood'),
      png('SMAUG', 'smaug'),
      png('SPIDER', 'spider'),
      png('TROLL', 'troll'),
    ],
  },
];

export const ALL_CULTURES: readonly string[] = CULTURE_GROUPS.flatMap((group) => group.cultures.map((c) => c.code));

const MOVIE_BLOCKS = ['ttt_block', 'king_block', 'pc_ttt_block', 'pc_king_block'];
const EXPANDED_BLOCKS = ['war_block', 'hunter_block'];
const HOBBIT_BLOCKS = ['hobbit'];

/**
 * The culture groups the format hides (CardFilter.setFilterChanged): Rohan and Gollum appear from the Towers block on,
 * Dunland and Raider in movie blocks, Men/Orc/Uruk-hai in expanded blocks, the Hobbit cultures only for the Hobbit game.
 */
export function hiddenCultureGroups(blocks: readonly BlockFilter[] | null): Set<CultureGroupId> {
  const has = (names: string[]) => (blocks ?? []).some((block) => names.includes(block.filter));
  const hidden = new Set<CultureGroupId>();
  if (!has([...MOVIE_BLOCKS, ...EXPANDED_BLOCKS])) hidden.add('culture_freeps_movie1');
  if (!has([...MOVIE_BLOCKS, ...EXPANDED_BLOCKS, ...HOBBIT_BLOCKS])) hidden.add('culture_freeps_movie2');
  if (!has(MOVIE_BLOCKS)) {
    hidden.add('culture_shadow_movie1');
    hidden.add('culture_shadow_movie2');
  }
  if (!has(EXPANDED_BLOCKS)) hidden.add('culture_shadows');
  if (!has(HOBBIT_BLOCKS)) hidden.add('culture_hobbit');
  return hidden;
}

/**
 * Cultures still pressed once the hidden groups are released. The legacy code meant to release them too, but only
 * managed it for the Hobbit cultures (the other label IDs it used do not exist), so a hidden culture kept filtering
 * the collection without being visible. Here every hidden culture is released.
 */
export function visibleCultures(cultures: readonly string[], hidden: ReadonlySet<CultureGroupId>): string[] {
  const hiddenCodes = new Set(
    CULTURE_GROUPS.filter((group) => hidden.has(group.id)).flatMap((group) => group.cultures.map((c) => c.code)),
  );
  return cultures.filter((code) => !hiddenCodes.has(code));
}

/** Block list for a format (CardFilter.updateFormat), and the block selected with it: the only one, or "Any". */
export function blockOptions(blocks: readonly BlockFilter[]): {
  options: SelectOption[];
  selected: string;
} {
  const options = [{ value: '', label: 'Any' }, ...blocks.map((block) => ({ value: block.filter, label: block.name }))];
  return { options, selected: blocks.length === 1 ? options[1].value : '' };
}

/**
 * Set list for the chosen block ("" = every block of the format), sorted by set number with the PC errata last, and
 * the set selected with it: the only one, or "Any" (CardFilter's blockSelect change handler).
 */
export function setOptions(
  blocks: readonly BlockFilter[],
  block: string,
): { options: SelectOption[]; selected: string } {
  const source = block === '' ? blocks : blocks.filter((b) => b.filter === block).slice(0, 1);
  const options: SelectOption[] = [{ value: '', label: 'Any' }];
  for (const b of source) {
    for (const [label, value] of Object.entries(b.setFilters)) {
      options.push({ value, label });
    }
  }
  const order = (value: string) => (value === 'pc_errata' ? 1000 : value === '' ? 0 : parseInt(value, 10));
  // The legacy comparator, kept as-is (including how it treats non-numeric values)
  options.sort((a, b) => {
    const av = order(a.value);
    const bv = order(b.value);
    return av == bv ? 0 : av < bv ? -1 : 1;
  });
  return { options, selected: options.length === 2 ? options[1].value : options[0].value };
}

export interface FilterContext {
  /** Format code of the deck (format:...); null before the formats are loaded. */
  format: string | null;
  /** The deck builder's predefined filter (e.g. "cardType:-THE_ONE_RING"), appended last. */
  override: string;
  /** RTMD meta-site allowing other sites: sent with site searches. */
  siteOverride: boolean;
  /** Bare blueprint IDs of the cards in the deck, for "Special in Deck". */
  cardsInDeck: () => readonly string[];
}

/**
 * The filter string (CardFilter.calculateDeckFilter), in the same order. A field the predefined filter already sets
 * is left out. (The legacy code then appended the raw value without a separator, e.g. "sort:namefoil"; that is
 * fixed.) Multi-word game text now has every space turned into "_" (the legacy code replaced only the first one).
 */
export function buildFilterString(fields: FilterFields, context: FilterContext): string {
  const override = context.override;
  const unlessOverridden = (key: string, value: string, render = (v: string) => ` ${key}:${v}`) =>
    value !== '' && !override.includes(`${key}:`) ? render(value) : '';

  const culture =
    fields.cultures.length > 0 && !override.includes('culture:')
      ? ` culture:${ALL_CULTURES.filter((code) => fields.cultures.includes(code)).join(',')}`
      : '';
  const product = unlessOverridden('product', fields.product);
  const rarity = unlessOverridden('rarity', fields.rarity);
  const side = unlessOverridden('side', fields.side);
  const sort = unlessOverridden('sort', fields.sort);
  const format = context.format ? ` format:${context.format}` : '';
  const block = fields.block ? ` block:${fields.block}` : '';
  const set = fields.set ? ` set:${fields.set}` : '';
  const cardType = fields.cardType !== '' ? ` cardType:${fields.cardType}` : '';
  const name =
    fields.name.trim() !== '' && !override.includes('name:')
      ? fields.name
          .split(' ')
          .map((word) => ` name:${word}`)
          .join('')
      : '';
  const gametext = unlessOverridden('gametext', fields.gametext, (v) => ` gametext:${v.replaceAll(' ', '_')}`);
  const keyword = unlessOverridden('keyword', fields.keyword);
  const phase = unlessOverridden('phase', fields.phase);
  const race = unlessOverridden('race', fields.race);
  const itemClass = unlessOverridden('itemClass', fields.itemClass);
  const numeric = (stat: NumericFilter) => {
    const compare = fields[`${stat}Compare`];
    const value = fields[`${stat}Value`];
    return compare !== '' && value !== '' && !override.includes(`${stat}:`)
      ? ` ${stat}Compare:${compare} ${stat}:${value}`
      : '';
  };
  const signet = fields.signet !== '' ? ` signet:${fields.signet}` : '';
  const siteOverride = context.siteOverride && cardType.includes('SITE') ? ' siteOverride:true' : '';
  const blueprints = product === ' product:specialForDeck' ? ` blueprints:${context.cardsInDeck().join(',')}` : '';

  return (
    side +
    format +
    block +
    set +
    cardType +
    rarity +
    sort +
    product +
    culture +
    name +
    gametext +
    keyword +
    phase +
    race +
    itemClass +
    numeric('twilight') +
    numeric('strength') +
    numeric('vitality') +
    numeric('resistance') +
    numeric('siteNumber') +
    signet +
    siteOverride +
    blueprints +
    ' ' +
    override
  ).trim();
}

/** "Reset advanced filters only": the fields below the "Advanced filters" line. */
export function resetAdvanced(fields: FilterFields): FilterFields {
  const cleared: Partial<FilterFields> = {
    name: '',
    gametext: '',
    keyword: '',
    phase: '',
    race: '',
    itemClass: '',
    signet: '',
  };
  for (const stat of NUMERIC_FILTERS) {
    cleared[`${stat}Compare`] = '';
    cleared[`${stat}Value`] = '';
  }
  return { ...fields, ...cleared };
}

/** "Reset all filters": every field back to its first option, and no culture pressed. Block and Set are rebuilt after. */
export function resetAll(fields: FilterFields): FilterFields {
  return {
    ...resetAdvanced(fields),
    product: '',
    rarity: '',
    block: '',
    set: '',
    side: '',
    sort: SORT_OPTIONS[0].value,
    cardType: '',
    cultures: [],
  };
}
