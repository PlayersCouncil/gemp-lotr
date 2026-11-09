import { BlockFilter } from '../api/models';
import { siteImage } from '../platform/app-paths';

/** The culture buttons, and which of them a format shows. */

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
  image: siteImage(`cultures/${file}.svg`),
  cultureClass: true,
});
const png = (code: string, file: string): Culture => ({
  code,
  image: siteImage(`cultures/${file}.png`),
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
