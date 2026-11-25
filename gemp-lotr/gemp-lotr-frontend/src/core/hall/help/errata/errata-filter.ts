import { HallRoute } from '../../hall-links';
import { orIfEmpty } from '../../json-values';
import { foldText } from '../format-definitions';
import {
  ALL_FORMATS,
  ALL_SETS,
  countedLabel,
  NO_MATCHES,
  noErrataInFormat,
  setOptionLabel,
  SHOW_ALL,
  SHOW_RECENT,
  statusText,
} from './errata-messages';
import {
  ErrataChip,
  ErrataEntry,
  ErrataFilterKey,
  ErrataFilters,
  ErrataFormat,
  ErrataOption,
  ErrataSort,
  ErrataSortKey,
} from './errata-models';

/** The pure rules of Help › PC Errata's table: PcErrata's filters, sorts and deep links (pcErrataUi.js). */

/** The filters, in the order a link writes them (PcErrata.FILTER_KEYS). */
export const ERRATA_FILTER_KEYS: readonly ErrataFilterKey[] = ['q', 'set', 'format', 'show'];

export const EMPTY_FILTERS: ErrataFilters = { q: '', set: '', format: '', show: '' };

/** The View chips; '' is everything (PcErrata.SHOWS). */
export const ERRATA_SHOWS: readonly { readonly value: string; readonly label: string }[] = [
  { value: '', label: SHOW_ALL },
  { value: 'recent', label: SHOW_RECENT },
];

/** Formats left out of the Format list (a link to them still works): PcErrata.HIDDEN_FORMATS. */
export const HIDDEN_FORMATS: readonly string[] = ['rev_tow_sta', 'debug'];

export const ERRATA_SORT_KEYS: readonly ErrataSortKey[] = ['num', 'culture', 'name', 'rev'];
export const DEFAULT_SORT: ErrataSort = { key: 'num', dir: 1 };

/** The search as a card number: "1.45", "1_45", "1-45", "1 45" → "1_45" (PcErrata.searchedNumber). */
export function searchedNumber(query: string): string | null {
  const match = /^(\d+)\s*[._\- ]\s*(\d+)$/.exec(query.trim());
  return match?.[1] !== undefined && match[2] !== undefined
    ? `${parseInt(match[1], 10)}_${parseInt(match[2], 10)}`
    : null;
}

/** Whether a card passes the filters (PcErrata.matches). */
export function matchesFilters(entry: ErrataEntry, filters: ErrataFilters): boolean {
  if (filters.set && entry.set !== filters.set) {
    return false;
  }
  if (filters.format && !entry.formats.includes(filters.format)) {
    return false;
  }
  if (filters.show === 'recent' && !entry.recent) {
    return false;
  }
  const query = foldText(filters.q).trim();
  if (query) {
    // the title; a collector's number ("1U29") or a set and card number ("1.29") also find the card
    if (entry.searchName.includes(query)) {
      return true;
    }
    if (query.replace(/\s+/g, '') === entry.searchColl) {
      return true;
    }
    const number = searchedNumber(query);
    return number !== null && (entry.base === number || entry.id === number);
  }
  return true;
}

export function filterEntries(entries: readonly ErrataEntry[], filters: ErrataFilters): ErrataEntry[] {
  return entries.filter((entry) => matchesFilters(entry, filters));
}

/** By set, then card number, then id (PcErrata.byNumber). */
export function byNumber(a: ErrataEntry, b: ErrataEntry): number {
  return a.setNum - b.setNum || a.cardNum - b.cardNum || (a.id < b.id ? -1 : a.id > b.id ? 1 : 0);
}

/** Sorts after any text. */
const LAST = String.fromCharCode(0xffff);

function byText(field: 'name' | 'culture'): (a: ErrataEntry, b: ErrataEntry) => number {
  // an empty value sorts last
  return (a, b) => orIfEmpty(a[field], LAST).localeCompare(orIfEmpty(b[field], LAST));
}

const COMPARE: Record<ErrataSortKey, (a: ErrataEntry, b: ErrataEntry) => number> = {
  num: byNumber,
  name: byText('name'),
  culture: byText('culture'),
  rev: (a, b) => a.revision - b.revision,
};

/** A sorted copy; ties fall back to the set and card number order (PcErrata.sort). */
export function sortEntries(entries: readonly ErrataEntry[], sort: ErrataSort): ErrataEntry[] {
  const compare = COMPARE[sort.key];
  return [...entries].sort((a, b) => sort.dir * compare(a, b) || byNumber(a, b));
}

/** The position of a card in a list, by its id or its original's (PcErrataUI.indexOf); -1 when it isn't listed. */
export function indexOfCard(list: readonly ErrataEntry[], id: string): number {
  return list.findIndex((entry) => entry.id === id || entry.base === id);
}

/** The first card a link's id names: the errata itself, or the errata of that original. */
export function findCard(entries: readonly ErrataEntry[], id: string): ErrataEntry | null {
  return entries.find((entry) => entry.id === id || entry.base === id) ?? null;
}

/** The View chips with their counts; a chip that would show nothing is left out, except All (renderFilters). */
export function viewChips(entries: readonly ErrataEntry[]): ErrataChip[] {
  return ERRATA_SHOWS.map((show) => ({
    ...show,
    count: filterEntries(entries, { ...EMPTY_FILTERS, show: show.value }).length,
  })).filter((chip) => !chip.value || chip.count > 0);
}

/** The Set list: each set that has errata, by number, with its name and count ("All sets" first is the view's). */
export function setOptions(entries: readonly ErrataEntry[]): ErrataOption[] {
  const sets = new Map<string, { num: number; name: string | null; count: number }>();
  for (const entry of entries) {
    const set = sets.get(entry.set);
    if (set) {
      set.count++;
    } else {
      sets.set(entry.set, { num: entry.setNum, name: entry.setName, count: 1 });
    }
  }
  return [...sets.entries()]
    .sort((a, b) => a[1].num - b[1].num)
    .map(([value, set]) => ({ value, label: setOptionLabel(value, set.name, set.count) }));
}

/** The Format list: the hall formats with errata in effect, in hall order, but the hidden ones. */
export function formatOptions(formats: readonly ErrataFormat[]): ErrataOption[] {
  return formats
    .filter((format) => format.count > 0 && !HIDDEN_FORMATS.includes(format.code))
    .map((format) => ({ value: format.code, label: countedLabel(format.name, format.count) }));
}

/**
 * A list's options with what the filter holds kept as the choice when the list leaves it out (a link to a hidden
 * format, or to a set without errata): "<format's name or the value> (<count>)" (syncControls).
 */
export function withChosen(
  options: readonly ErrataOption[],
  key: 'set' | 'format',
  value: string,
  entries: readonly ErrataEntry[],
  formats: readonly ErrataFormat[],
): ErrataOption[] {
  if (!value || options.some((option) => option.value === value)) {
    return [...options];
  }
  const label = key === 'format' ? (formats.find((format) => format.code === value)?.name ?? value) : value;
  const shown = filterEntries(entries, { ...EMPTY_FILTERS, [key]: value }).length;
  return [...options, { value, label: countedLabel(label, shown), extra: true }];
}

export const ALL_OPTION_LABELS = { set: ALL_SETS, format: ALL_FORMATS } as const;

function onlyFilter(filters: ErrataFilters, key: ErrataFilterKey): boolean {
  return ERRATA_FILTER_KEYS.every((other) => other === key || !filters[other]);
}

/**
 * The status beside the filters, which is also what the empty table and viewer say (renderStatus): "40 cards",
 * "3 of 40 cards", or why nothing is listed.
 */
export function errataStatus(
  shown: number,
  total: number,
  filters: ErrataFilters,
  formats: readonly ErrataFormat[],
): { readonly text: string; readonly empty: boolean } {
  if (shown > 0) {
    return { text: statusText(shown, total), empty: false };
  }
  if (filters.format && onlyFilter(filters, 'format')) {
    const name = formats.find((format) => format.code === filters.format)?.name ?? filters.format;
    return { text: noErrataInFormat(name), empty: true };
  }
  return { text: NO_MATCHES, empty: true };
}

/** A link's filters (applyRoute): only the filter keys, everything else ''. */
export function filtersOfParams(params: Readonly<Record<string, string | undefined>>): ErrataFilters {
  const filters: Record<ErrataFilterKey, string> = { ...EMPTY_FILTERS };
  for (const key of ERRATA_FILTER_KEYS) {
    filters[key] = params[key] ?? '';
  }
  return filters;
}

/** A link's sort (applyRoute: `sort` one of the columns, `dir=desc`), or null when it names none. */
export function sortOfParams(params: Readonly<Record<string, string | undefined>>): ErrataSort | null {
  const key = params['sort'];
  if (key === undefined || !(ERRATA_SORT_KEYS as readonly string[]).includes(key)) {
    return null;
  }
  return { key: key as ErrataSortKey, dir: params['dir'] === 'desc' ? -1 : 1 };
}

/** Whether a link carries anything for the page (a card, or any parameter it reads). */
export function isErrataLink(card: string | null, params: Readonly<Record<string, string | undefined>>): boolean {
  return card !== null || [...ERRATA_FILTER_KEYS, 'sort', 'dir'].some((key) => params[key] !== undefined);
}

/**
 * The address of what is on show (PcErrata.buildHash): `/help/errata[/<card>]` with the filters in use as query
 * parameters. The sort is not written, as the legacy address did not write it.
 */
export function errataAddress(card: string | null, filters: ErrataFilters): HallRoute {
  const queryParams: Record<string, string> = {};
  for (const key of ERRATA_FILTER_KEYS) {
    if (filters[key]) {
      queryParams[key] = filters[key];
    }
  }
  return { path: card ? `/help/errata/${card}` : '/help/errata', queryParams, fragment: null };
}
