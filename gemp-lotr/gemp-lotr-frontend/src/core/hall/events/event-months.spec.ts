import {
  groupMonthsByYear,
  monthKey,
  monthLabel,
  monthName,
  neighbourMonth,
  newestMonth,
  orDash,
  parseMonthKey,
} from './event-months';

describe('event months', () => {
  it('reads and writes yyyy-MM', () => {
    expect(monthKey({ year: 2026, month: 3 })).toBe('2026-03');
    expect(parseMonthKey('2026-11')).toEqual({ year: 2026, month: 11 });
    expect(parseMonthKey('2026-13')).toBeNull();
    expect(parseMonthKey('soon')).toBeNull();
  });

  it('names months and echoes anything else', () => {
    expect(monthName('2026-03')).toBe('March');
    expect(monthLabel('2026-03')).toBe('March 2026');
    expect(monthLabel('later')).toBe('later');
    expect(monthName('2026-00')).toBe('2026-00');
  });

  it('finds the neighbouring and the newest months', () => {
    const months = ['2026-10', '2026-08', '2025-12'];
    expect(neighbourMonth(months, '2026-09', -1)).toBe('2026-08');
    expect(neighbourMonth(months, '2026-09', 1)).toBe('2026-10');
    expect(neighbourMonth(months, '2026-10', 1)).toBeNull();
    expect(newestMonth(months, '2026-09')).toBe('2026-08');
    expect(newestMonth(months, '2025-01')).toBeNull();
  });

  it('groups the dropdown by year', () => {
    expect(groupMonthsByYear(['2026-10', '2026-08', '2025-12'])).toEqual([
      {
        year: '2026',
        months: [
          { key: '2026-10', name: 'October', title: 'October 2026' },
          { key: '2026-08', name: 'August', title: 'August 2026' },
        ],
      },
      { year: '2025', months: [{ key: '2025-12', name: 'December', title: 'December 2025' }] },
    ]);
  });

  it('shows a dash for nothing', () => {
    expect(orDash(null)).toBe('—');
    expect(orDash('')).toBe('—');
    expect(orDash(0)).toBe('0');
  });
});
