import { readServerResponse } from '../../../testing/files';
import { defaultStartDay, parseServerStats, percentage, statsTables } from './server-stats';
import { parseJson } from '../../api/json';

const stats = parseServerStats(parseJson(readServerResponse('server-stats.json')));

describe('server stats (statsUi.js)', () => {
  it('reads the answer, anything missing as empty', () => {
    expect(stats).toMatchObject({
      startDate: '2026-10-20',
      endDate: '2026-11-20',
      activePlayers: 34,
      gamesCount: 107,
      botGamesCount: 7,
    });
    expect(stats.formats[2]).toEqual({ format: 'Fellowship Block (PC)', count: 45, casual: true });
    expect(parseServerStats(null)).toEqual({
      startDate: '',
      endDate: '',
      activePlayers: null,
      gamesCount: null,
      botGamesCount: null,
      formats: [],
    });
  });

  it('opens on the day a month ago, the last of the month when it has no such day, without zero padding', () => {
    expect(defaultStartDay(new Date(2026, 10, 20, 12))).toBe('2026-10-20');
    expect(defaultStartDay(new Date(2026, 2, 31, 12))).toBe('2026-2-28');
    expect(defaultStartDay(new Date(2026, 0, 5, 12))).toBe('2025-12-5');
  });

  it('writes shares as percentages, and nothing of a group with no games', () => {
    expect(percentage(3, 8, 'en-US')).toBe('37.50%');
    expect(percentage(0, 0, 'en-US')).toBe('');
  });

  it('splits competitive and casual games, the most first, with each share of its group and of all', () => {
    const { competitive, casual } = statsTables(stats, {}, 'en-US');
    expect(competitive.columns.map((column) => column.title)).toEqual([
      'Format name',
      '# of games',
      '% of competitive',
      '% of total',
    ]);
    expect(competitive.rows).toEqual([
      { format: 'Fellowship Block (PC)', count: 30, share: '75.00%', ofTotal: '30.00%' },
      { format: 'Movie Block (PC)', count: 10, share: '25.00%', ofTotal: '10.00%' },
    ]);
    expect(casual.columns[2]?.title).toBe('% of casual');
    expect(casual.rows.map((row) => [row.format, row.share])).toEqual([
      ['Fellowship Block (PC)', '75.00%'],
      ['Expanded', '25.00%'],
    ]);
    expect(competitive.sort).toEqual({ key: 'Count', dir: 'desc' });
    expect(competitive.emptyText).toBe('No competitive games in this period.');
    expect(casual.emptyText).toBe('No casual games in this period.');
  });

  it('sorts a table as asked, the percentages by the number of games', () => {
    const byName = statsTables(stats, { casual: { key: 'Format', dir: 'asc' } }, 'en-US');
    expect(byName.casual.rows.map((row) => row.format)).toEqual(['Expanded', 'Fellowship Block (PC)']);
    const byShare = statsTables(stats, { competitive: { key: 'share', dir: 'asc' } }, 'en-US');
    expect(byShare.competitive.rows.map((row) => row.count)).toEqual([10, 30]);
    const none = statsTables(parseServerStats({}), {}, 'en-US');
    expect(none.competitive.rows).toEqual([]);
  });
});
