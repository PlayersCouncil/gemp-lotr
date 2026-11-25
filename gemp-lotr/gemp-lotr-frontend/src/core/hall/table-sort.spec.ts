import { ariaSort, compareSortValues, nextSort, sortRows } from './table-sort';

describe('table sort (renderTable)', () => {
  it('sorts a column ascending first, then descending', () => {
    expect(nextSort(null, 'Count')).toEqual({ key: 'Count', dir: 'asc' });
    expect(nextSort({ key: 'Count', dir: 'asc' }, 'Count')).toEqual({ key: 'Count', dir: 'desc' });
    expect(nextSort({ key: 'Count', dir: 'desc' }, 'Count')).toEqual({ key: 'Count', dir: 'asc' });
    expect(nextSort({ key: 'Count', dir: 'asc' }, 'Format')).toEqual({ key: 'Format', dir: 'asc' });
  });

  it('marks the sorted header', () => {
    expect(ariaSort({ key: 'Count', dir: 'asc' }, 'Count')).toBe('ascending');
    expect(ariaSort({ key: 'Count', dir: 'desc' }, 'Count')).toBe('descending');
    expect(ariaSort({ key: 'Count', dir: 'desc' }, 'Format')).toBe('none');
    expect(ariaSort(null, 'Format')).toBe('none');
  });

  it('compares numbers as numbers and text without case, numbers in it in order', () => {
    expect(compareSortValues(2, 10)).toBeLessThan(0);
    expect(compareSortValues('set 2', 'Set 10')).toBeLessThan(0);
    expect(compareSortValues('abc', 'ABC')).toBe(0);
    expect(compareSortValues(null, 1)).toBe(1);
    expect(compareSortValues('', '')).toBe(0);
  });

  it('keeps empty values last in either direction, and equal rows in their order', () => {
    const rows = [
      { name: 'b', n: 2 },
      { name: 'x', n: null },
      { name: 'a', n: 2 },
      { name: 'c', n: 10 },
    ];
    const n = (row: { n: number | null }) => row.n;
    expect(sortRows(rows, { key: 'n', dir: 'asc' }, n).map((row) => row.name)).toEqual(['b', 'a', 'c', 'x']);
    expect(sortRows(rows, { key: 'n', dir: 'desc' }, n).map((row) => row.name)).toEqual(['c', 'b', 'a', 'x']);
    expect(sortRows(rows, null, n)).toEqual(rows);
    expect(sortRows(rows, null, n)).not.toBe(rows);
  });
});
