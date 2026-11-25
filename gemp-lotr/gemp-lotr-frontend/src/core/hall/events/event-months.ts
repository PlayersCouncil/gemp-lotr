import { DASH, MONTH_NAMES } from './events-messages';
import { YearMonth } from './calendar-grid';

/** The completed-events browser's months, "yyyy-MM" (eventHistoryUi.js EventHistoryUI's helpers and MonthNav). */

export function monthKey(month: YearMonth): string {
  return `${month.year}-${month.month < 10 ? '0' : ''}${month.month}`;
}

/** "2026-03" → { year: 2026, month: 3 }; null for anything else (setMonth ignores it). */
export function parseMonthKey(key: string): YearMonth | null {
  const [year = NaN, month = NaN] = key.split('-').map((part) => parseInt(part, 10));
  if (Number.isNaN(year) || Number.isNaN(month) || month < 1 || month > 12) {
    return null;
  }
  return { year, month };
}

function monthIndex(key: string): number {
  const parts = key.split('-');
  if (parts.length < 2) {
    return -1;
  }
  const index = parseInt(parts[1] ?? '', 10) - 1;
  return Number.isNaN(index) || index < 0 || index > 11 ? -1 : index;
}

/** "2026-03" → "March"; anything unexpected is echoed back. */
export function monthName(key: string): string {
  const index = monthIndex(key);
  return MONTH_NAMES[index] ?? key;
}

/** "2026-03" → "March 2026"; anything unexpected is echoed back. */
export function monthLabel(key: string): string {
  const index = monthIndex(key);
  const name = MONTH_NAMES[index];
  return name === undefined ? key : `${name} ${key.split('-')[0] ?? ''}`;
}

/** The nearest month in `months` strictly before (delta < 0) or after `current`, or null. */
export function neighbourMonth(months: readonly string[], current: string, delta: number): string | null {
  let best: string | null = null;
  for (const key of months) {
    if (delta < 0 ? key < current : key > current) {
      if (best === null || (delta < 0 ? key > best : key < best)) {
        best = key;
      }
    }
  }
  return best;
}

/** The newest month in `months` not after `upTo`, or null. */
export function newestMonth(months: readonly string[], upTo: string): string | null {
  let best: string | null = null;
  for (const key of months) {
    if (key <= upTo && (best === null || key > best)) {
      best = key;
    }
  }
  return best;
}

/** The dropdown's months grouped by year, in the order given (MonthNav.renderList). */
export interface MonthGroup {
  readonly year: string;
  readonly months: readonly { readonly key: string; readonly name: string; readonly title: string }[];
}

export function groupMonthsByYear(months: readonly string[]): MonthGroup[] {
  const groups: { year: string; months: { key: string; name: string; title: string }[] }[] = [];
  for (const key of months) {
    const year = key.split('-')[0] ?? '';
    let group = groups[groups.length - 1];
    if (group?.year !== year) {
      group = { year, months: [] };
      groups.push(group);
    }
    group.months.push({ key, name: monthName(key), title: monthLabel(key) });
  }
  return groups;
}

/** EventHistoryUI.orDash */
export function orDash(value: string | number | null): string {
  return value === null || value === '' ? DASH : String(value);
}
