/**
 * A puzzle slot's search, in the deck builder's filter language (SortAndFilterCards), made so that it never finds a
 * card the server's check (SlotBounds) refuses for what the filters say. The server checks the pool and the slot's
 * filter each by itself, and a card fits if both find it; one text joining the two would find more where they say the
 * same key, because the deck builder's search joins a repeated key's values with OR (`overlap` in the scenario info).
 * So the two are intersected key by key here, and the kinds of card the slot's place can hold at all (and a site
 * slot's number) are added to them. Pure.
 *
 * How each key repeated in both is joined, from how SortAndFilterCards reads it:
 * - `name`, `gametext`: every word has to be in the card: both kept (exact).
 * - `cardtype`, `culture`, `race`, `signet`: a card has one of each, so the values both allow (exact).
 * - `side`: the first one only is read: kept when both say the same, otherwise nothing fits (exact).
 * - `set`, `block`, `keyword`, `itemclass`: a card may have several; the values in both (never wider; a card found
 *   by two different values, one in each, is left out).
 * - anything else (the numbers and their comparisons, `phase`, `sort`...): the slot's own filter's is kept, and the
 *   search is said not to be exact (the server's check still decides).
 */

type Parsed = Map<string, string[]>;

/** The keys the deck builder's search reads in lower case, whatever case they were written in. */
function parse(text: string | null): Parsed {
  const parsed: Parsed = new Map();
  for (const part of (text ?? '').trim().split(/\s+/)) {
    const at = part.indexOf(':');
    if (at <= 0) continue;
    const key = part.slice(0, at).toLowerCase();
    const values = part
      .slice(at + 1)
      .split(',')
      .filter((value) => value !== '');
    if (values.length === 0) continue;
    parsed.set(key, [...(parsed.get(key) ?? []), ...values]);
  }
  return parsed;
}

/** A search that finds nothing (no card is of set 9999), for bounds that cannot both hold. */
export const FINDS_NOTHING = 'set:9999';

/** The kinds of card a slot's place can hold at all (SlotBounds.kindProblem); null: any but those below. */
const ZONE_TYPES: Readonly<Record<string, readonly string[]>> = {
  characters: ['COMPANION', 'ALLY'],
  minions: ['MINION'],
  support: ['CONDITION', 'POSSESSION', 'ARTIFACT', 'ALLY', 'FOLLOWER'],
  attached: ['CONDITION', 'POSSESSION', 'ARTIFACT', 'FOLLOWER'],
  site: ['SITE'],
};
/** What no slot but a site's takes. */
const NEVER = ['THE_ONE_RING', 'SITE', 'MAP', 'METASITE', 'ADVENTURE'];

/** Whether a card of the type (in capitals) can be in a slot of the zone at all. */
export function slotTakes(zone: string, type: string): boolean {
  const upper = type.toUpperCase();
  const only = ZONE_TYPES[zone];
  if (only) return only.includes(upper);
  return !NEVER.includes(upper);
}

const SINGLE = ['cardtype', 'culture', 'race', 'signet'];
const ANY_OF = ['set', 'block', 'keyword', 'itemclass'];
const ALL_WORDS = ['name', 'gametext'];

const split = (values: readonly string[]) => ({
  plus: values.filter((value) => !value.startsWith('-')),
  minus: values.filter((value) => value.startsWith('-')).map((value) => value.slice(1)),
});

/** The numbers a set value names ("1-6", "4"), or null for a value that is not a number or a range (a format). */
function setNumbers(value: string): number[] | null {
  const range = /^(\d+)-(\d+)$/.exec(value);
  if (range) {
    const [from, to] = [Number(range[1]), Number(range[2])];
    return from <= to && to - from < 1000 ? Array.from({ length: to - from + 1 }, (_, at) => from + at) : null;
  }
  return /^\d+$/.test(value) ? [Number(value)] : null;
}

/** Numbers as the shortest list of ranges: 1,2,3,5 → "1-3,5". */
function ranges(numbers: readonly number[]): string[] {
  const sorted = [...new Set(numbers)].sort((a, b) => a - b);
  const out: string[] = [];
  for (let at = 0; at < sorted.length;) {
    let end = at;
    while (end + 1 < sorted.length && sorted[end + 1] === (sorted[end] ?? 0) + 1) end++;
    out.push(end === at ? String(sorted[at]) : `${sorted[at]}-${sorted[end]}`);
    at = end + 1;
  }
  return out;
}

/** The values two lists of sets (or blocks) both name. */
function bothSets(one: readonly string[], other: readonly string[]): string[] {
  const numbers = (values: readonly string[]) => values.flatMap((value) => setNumbers(value) ?? []);
  const names = (values: readonly string[]) => values.filter((value) => setNumbers(value) === null);
  const otherNumbers = new Set(numbers(other));
  const otherNames = names(other);
  return [
    ...ranges(numbers(one).filter((number) => otherNumbers.has(number))),
    ...names(one).filter((name) => otherNames.includes(name)),
  ];
}

/**
 * The values of a key both lists allow, as the key's values (`-X` for one left out); null when none can be, where
 * that means nothing fits. `single`: a card has one value of the key (a positive list is "one of these").
 */
function bothEnums(one: readonly string[], other: readonly string[], single: boolean): string[] | null {
  const a = split(one);
  const b = split(other);
  const minus = [...new Set([...a.minus, ...b.minus])];
  let plus: string[];
  if (a.plus.length > 0 && b.plus.length > 0) {
    plus = a.plus.filter((value) => b.plus.includes(value));
    if (plus.length === 0) return null;
  } else {
    plus = [...a.plus, ...b.plus];
  }
  plus = plus.filter((value) => !minus.includes(value));
  if (plus.length === 0 && (a.plus.length > 0 || b.plus.length > 0)) return null;
  // (a list of values left out reads as nothing for a key a card may have several of: kept only beside some in)
  if (!single && plus.length === 0) return [];
  return [...plus, ...minus.map((value) => `-${value}`)];
}

/** A slot's search: the deck builder's filter text, and whether it finds exactly what the server's filters take. */
export interface SlotFilter {
  filter: string;
  exact: boolean;
}

/**
 * The search for a slot's card: the pool and the slot's filter intersected, held to the kinds of card its place can
 * hold, and for a site slot to its number on the path (when known). `zone` as the scenario writes it.
 */
export function slotFilter(
  pool: string | null,
  filter: string | null,
  zone: string,
  siteNumber: number | null = null,
): SlotFilter {
  const a = parse(pool);
  const b = parse(filter);
  let exact = true;
  const joined: Parsed = new Map();
  for (const key of new Set([...a.keys(), ...b.keys()])) {
    const one = a.get(key);
    const other = b.get(key);
    if (!one || !other) {
      joined.set(key, one ?? other ?? []);
      continue;
    }
    if (ALL_WORDS.includes(key)) {
      joined.set(key, [...one, ...other]);
    } else if (key === 'side') {
      if (one[0] !== other[0]) return { filter: FINDS_NOTHING, exact: true };
      joined.set(key, one.slice(0, 1));
    } else if (key === 'set' || key === 'block') {
      const both = bothSets(one, other);
      if (both.length === 0) return { filter: FINDS_NOTHING, exact: true };
      joined.set(key, both);
    } else if (SINGLE.includes(key) || ANY_OF.includes(key)) {
      const both = bothEnums(one, other, SINGLE.includes(key));
      if (both === null) return { filter: FINDS_NOTHING, exact: true };
      if (both.length > 0) joined.set(key, both);
      else joined.delete(key);
    } else {
      // (the numbers read only their first value, and a phase means one thing for events and another for the rest)
      joined.set(key, other);
      if (key !== 'sort') exact = false;
    }
  }
  // The kinds of card the place can hold at all
  const kinds = ZONE_TYPES[zone] ?? null;
  const types = bothEnums(joined.get('cardtype') ?? [], kinds ?? NEVER.map((type) => `-${type}`), true);
  if (types === null) return { filter: FINDS_NOTHING, exact: true };
  joined.set('cardtype', types);
  // A site slot takes a site of the number the path has there
  if (zone === 'site' && siteNumber !== null) {
    const said = joined.get('sitenumber');
    if (said && (said[0] !== String(siteNumber) || joined.has('sitenumbercompare'))) exact = false;
    joined.set('sitenumber', [String(siteNumber)]);
    joined.delete('sitenumbercompare');
  }
  const text = [...joined.entries()].map(([key, values]) => `${key}:${values.join(',')}`).join(' ');
  return { filter: text, exact };
}
