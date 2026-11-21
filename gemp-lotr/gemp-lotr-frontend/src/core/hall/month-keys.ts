/**
 * Months as the hall's month navigation names them, "yyyy-MM" keys (eventHistoryUi.js EventHistoryUI.monthName,
 * monthLabel, neighbourMonth; patchNotesUi.js PatchNotes.monthOf). Server Info › Patch Notes and the Events tab's
 * completed events both step through months this way.
 */

export const MONTH_NAMES = [
  'January',
  'February',
  'March',
  'April',
  'May',
  'June',
  'July',
  'August',
  'September',
  'October',
  'November',
  'December',
] as const;

/** 0-11 for "2026-03", or -1 for anything that is not a month key (EventHistoryUI.monthIndex). */
export function monthIndex(key: string): number {
  const parts = key.split('-');
  if (parts.length < 2) {
    return -1;
  }
  const index = parseInt(parts[1] ?? '', 10) - 1;
  return Number.isNaN(index) || index < 0 || index > 11 ? -1 : index;
}

/** "2026-03" → "March"; anything else as it is (EventHistoryUI.monthName). */
export function monthName(key: string): string {
  const index = monthIndex(key);
  return MONTH_NAMES[index] ?? key;
}

/** "2026-03" → "March 2026"; null → ""; anything else as it is (EventHistoryUI.monthLabel). */
export function monthLabel(key: string | null): string {
  if (key === null) {
    return '';
  }
  const index = monthIndex(key);
  const name = MONTH_NAMES[index];
  return name === undefined ? key : `${name} ${key.split('-')[0] ?? ''}`;
}

/**
 * The nearest month of `months` (any order) before `current` (delta < 0) or after it (delta > 0), or null
 * (EventHistoryUI.neighbourMonth).
 */
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

/** "2023-12-09" → "2023-12"; null when it is not a date (PatchNotes.monthOf). */
export function monthOfDate(iso: string | null): string | null {
  const match = /^(\d{4}-\d{2})-\d{2}$/.exec(iso ?? '');
  return match?.[1] ?? null;
}
