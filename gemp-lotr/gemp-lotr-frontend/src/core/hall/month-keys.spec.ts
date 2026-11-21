import { monthIndex, monthLabel, monthName, monthOfDate, neighbourMonth } from './month-keys';

describe('month keys', () => {
  it('names a month, echoing anything that is not one', () => {
    expect(monthIndex('2026-03')).toBe(2);
    expect(monthIndex('2026-13')).toBe(-1);
    expect(monthIndex('2026')).toBe(-1);
    expect(monthName('2026-12')).toBe('December');
    expect(monthName('soon')).toBe('soon');
    expect(monthLabel('2026-03')).toBe('March 2026');
    expect(monthLabel('2026-00')).toBe('2026-00');
    expect(monthLabel(null)).toBe('');
  });

  it('finds the nearest month with entries on either side', () => {
    const months = ['2026-09', '2023-12', '2026-11', '2025-01'];
    expect(neighbourMonth(months, '2026-09', -1)).toBe('2025-01');
    expect(neighbourMonth(months, '2026-09', 1)).toBe('2026-11');
    expect(neighbourMonth(months, '2026-10', 1)).toBe('2026-11');
    expect(neighbourMonth(months, '2023-12', -1)).toBeNull();
    expect(neighbourMonth(months, '2026-11', 1)).toBeNull();
    expect(neighbourMonth([], '2026-11', 1)).toBeNull();
  });

  it("gives a date's month", () => {
    expect(monthOfDate('2023-12-09')).toBe('2023-12');
    expect(monthOfDate('2023-12')).toBeNull();
    expect(monthOfDate(null)).toBeNull();
  });
});
