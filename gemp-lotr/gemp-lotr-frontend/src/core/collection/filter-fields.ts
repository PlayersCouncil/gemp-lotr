import { INITIAL_BLOCK_OPTION, INITIAL_SET_OPTION, SORT_OPTIONS } from './filter-options';

/** The values of the collection filter's fields, and what the two reset buttons do to them. */

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
