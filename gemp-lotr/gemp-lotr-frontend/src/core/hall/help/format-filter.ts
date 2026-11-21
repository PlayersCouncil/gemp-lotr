import { foldText, FORMAT_LIST_KEYS } from './format-definitions';
import { FormatDefinition, FormatListKey } from './format-definitions-models';
import { filterStatus } from './help-messages';

/**
 * The box above the contents list (GempFormatDefinitions.applyFilter): it shows the formats whose name (or code)
 * holds the text, and those listing a card whose name does (from two letters on), the matching cards marked and their
 * lists opened. Case and accents don't count.
 */
export interface FormatFilter {
  /** The text as searched (folded); '' shows every format. */
  readonly query: string;
  /** The formats shown. */
  readonly visible: ReadonlySet<string>;
  /** Per format code, per list, the ids of the cards that match. */
  readonly hits: ReadonlyMap<string, ReadonlyMap<FormatListKey, ReadonlySet<string>>>;
  /** "3 of 12 formats (2 by a listed card)", '' with no text. */
  readonly status: string;
}

/** Cards are looked for from this many letters on (a single letter matches names only). */
export const CARD_SEARCH_MIN_LENGTH = 2;

export function filterFormats(formats: readonly FormatDefinition[], value: string): FormatFilter {
  const query = foldText(value.trim());
  const all = new Set(formats.map((format) => format.code));
  if (query === '') {
    return { query, visible: all, hits: new Map(), status: '' };
  }
  const visible = new Set<string>();
  const hits = new Map<string, Map<FormatListKey, Set<string>>>();
  let byCard = 0;
  for (const format of formats) {
    const nameMatch = foldText(`${format.name} ${format.code}`).includes(query);
    const found = new Map<FormatListKey, Set<string>>();
    if (query.length >= CARD_SEARCH_MIN_LENGTH) {
      for (const key of FORMAT_LIST_KEYS) {
        const ids = new Set(
          format.lists[key].filter((card) => foldText(card.name).includes(query)).map((card) => card.id),
        );
        if (ids.size > 0) {
          found.set(key, ids);
        }
      }
    }
    if (found.size > 0) {
      hits.set(format.code, found);
    }
    if (nameMatch || found.size > 0) {
      visible.add(format.code);
      if (!nameMatch) {
        byCard++;
      }
    }
  }
  return { query, visible, hits, status: filterStatus(visible.size, formats.length, byCard) };
}

/** Whether the filter marks a card of a format's list. */
export function isHit(filter: FormatFilter, code: string, list: FormatListKey, cardId: string): boolean {
  return filter.hits.get(code)?.get(list)?.has(cardId) ?? false;
}
