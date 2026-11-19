import { computed, inject, Injectable, signal } from '@angular/core';
import { SettingCookies } from '../platform/setting-cookies';
import { CollectionQuery } from './collection-query.service';
import { FilterFields, NUMERIC_FILTERS, NumericFilter } from './filter-fields';

/**
 * The filters a player can keep shown above the collection in the compact layout (the rest are in the collection
 * menu), in the filter's order. The card name is always shown, so it isn't one of them.
 */
export const PINNABLE_FILTERS = [
  'product',
  'rarity',
  'block',
  'set',
  'side',
  'sort',
  'cardType',
  'gametext',
  'keyword',
  'phase',
  'race',
  'itemClass',
  'twilight',
  'siteNumber',
  'strength',
  'signet',
  'vitality',
  'resistance',
  'cultures',
] as const;
export type PinnableFilter = (typeof PINNABLE_FILTERS)[number];

/** Remembers the filters kept shown, on this device: the deck builder's own setting (names, comma separated). */
export const FILTER_PINS_COOKIE = 'gempDeckbuilderFilters';

function isPinnable(name: string): name is PinnableFilter {
  return (PINNABLE_FILTERS as readonly string[]).includes(name);
}

/** The filters kept shown, from the cookie's value (unknown names left out); none when it isn't set. */
export function parseFilterPins(value: string | null): ReadonlySet<PinnableFilter> {
  const names = (value ?? '').split(',');
  return new Set(names.filter(isPinnable));
}

function isNumeric(name: PinnableFilter): name is NumericFilter {
  return (NUMERIC_FILTERS as readonly string[]).includes(name);
}

/**
 * Whether a filter narrows the cards shown: a comparison chosen, a culture pressed, text typed, or a list on another
 * choice than its first ("Any"; for the Block and Set, the one the format selects, `defaultChoice`). The sort order
 * never does.
 */
export function filterIsSet(
  name: PinnableFilter,
  fields: FilterFields,
  defaultChoice: (name: 'block' | 'set') => string,
): boolean {
  if (name === 'sort') {
    return false;
  }
  if (name === 'cultures') {
    return fields.cultures.length > 0;
  }
  if (isNumeric(name)) {
    return fields[`${name}Compare`] !== '';
  }
  if (name === 'block' || name === 'set') {
    return fields[name] !== defaultChoice(name);
  }
  return fields[name] !== '';
}

/**
 * Which filters the compact layout keeps shown above the collection (remembered on the device), and how many of the
 * others narrow the cards, for the collection menu's button to say so.
 */
@Injectable()
export class FilterPins {
  private readonly cookies = inject(SettingCookies);
  private readonly query = inject(CollectionQuery);

  readonly pinned = signal(parseFilterPins(this.cookies.read(FILTER_PINS_COOKIE)));

  /** The filters in the menu that narrow the cards shown. */
  readonly hiddenSet = computed(() => {
    const fields = this.query.fields();
    // What the format selects (filter-block-sets): the only block or set when it has one, else "Any"
    const byDefault = (name: 'block' | 'set') => {
      const options = name === 'block' ? this.query.blockOptions() : this.query.setOptions();
      return (options.length === 2 ? options[1] : options[0])?.value ?? '';
    };
    const pinned = this.pinned();
    return PINNABLE_FILTERS.filter((name) => !pinned.has(name) && filterIsSet(name, fields, byDefault)).length;
  });

  isPinned(name: PinnableFilter): boolean {
    return this.pinned().has(name);
  }

  toggle(name: PinnableFilter): void {
    const pinned = new Set(this.pinned());
    if (!pinned.delete(name)) {
      pinned.add(name);
    }
    this.pinned.set(pinned);
    this.cookies.write(FILTER_PINS_COOKIE, PINNABLE_FILTERS.filter((filter) => pinned.has(filter)).join(','));
  }
}
