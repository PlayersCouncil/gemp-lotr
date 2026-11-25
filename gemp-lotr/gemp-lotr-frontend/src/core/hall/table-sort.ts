/**
 * The sorting of the hall's data tables (commonUi.js renderTable with `sortable`: Server Info's stats, My Account's
 * tables): a header sorts its column ascending, then descending; numbers sort as numbers, text ignoring case with the
 * numbers in it in numeric order, and empty values last whichever the direction.
 */

export type SortDirection = 'asc' | 'desc';

export interface TableSort<Key extends string = string> {
  readonly key: Key;
  readonly dir: SortDirection;
}

/** The sort after the header of `key` is pressed: ascending, or descending when it was ascending on that column. */
export function nextSort<Key extends string>(current: TableSort<Key> | null, key: Key): TableSort<Key> {
  return { key, dir: current?.key === key && current.dir === 'asc' ? 'desc' : 'asc' };
}

/** A header's aria-sort (renderTable marks the sorted column ▲ / ▼). */
export function ariaSort(sort: TableSort | null, key: string): 'ascending' | 'descending' | 'none' {
  if (sort?.key !== key) {
    return 'none';
  }
  return sort.dir === 'asc' ? 'ascending' : 'descending';
}

function isEmpty(value: unknown): boolean {
  return value === null || value === undefined || value === '' || (typeof value === 'number' && Number.isNaN(value));
}

/** Ascending order of two values; an empty one after any other. */
export function compareSortValues(a: unknown, b: unknown): number {
  const aEmpty = isEmpty(a);
  const bEmpty = isEmpty(b);
  if (aEmpty || bEmpty) {
    return aEmpty === bEmpty ? 0 : aEmpty ? 1 : -1;
  }
  if (typeof a === 'number' && typeof b === 'number') {
    return a - b;
  }
  return String(a).localeCompare(String(b), undefined, { numeric: true, sensitivity: 'base' });
}

/** The rows in the sort's order (a new array; equal rows keep their order); unsorted with no sort. */
export function sortRows<Row>(rows: readonly Row[], sort: TableSort | null, valueOf: (row: Row) => unknown): Row[] {
  if (sort === null) {
    return [...rows];
  }
  const sign = sort.dir === 'desc' ? -1 : 1;
  return [...rows].sort((a, b) => {
    const va = valueOf(a);
    const vb = valueOf(b);
    const result = compareSortValues(va, vb);
    // empty values stay last whatever the direction
    return isEmpty(va) !== isEmpty(vb) ? result : sign * result;
  });
}
