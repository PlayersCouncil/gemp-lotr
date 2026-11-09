import { BlockFilter } from '../api/models';
import { SelectOption } from './filter-options';

/** The Block and Set lists, built from the format. */

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
