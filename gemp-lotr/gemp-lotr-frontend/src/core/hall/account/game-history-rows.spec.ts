import { readServerXml } from '../../../testing/files';
import { parseGameHistory } from './account-parsers';
import { formatDuration, formatLocalTime, gameHistoryRow, serverDate } from './game-history-rows';

describe('game history rows', () => {
  it('formats lengths as the legacy formatDuration', () => {
    expect(formatDuration(null)).toBe('');
    expect(formatDuration(-1)).toBe('');
    expect(formatDuration(45000)).toBe('45 s');
    expect(formatDuration(12 * 60000)).toBe('12 min');
    expect(formatDuration(65 * 60000)).toBe('1 h 05 min');
    expect(formatLocalTime(null)).toBe('');
    expect(formatLocalTime(new Date(2026, 0, 2, 3, 4).getTime())).toBe('2026-01-02 03:04');
  });

  it('shows each entry as its row', () => {
    const page = parseGameHistory(readServerXml('game-history.xml'))!;
    const [won, lost, old] = page.entries.map((entry) => gameHistoryRow(entry, page.playerId));
    expect(won).toMatchObject({
      date: '2025-11-20',
      length: '55 min',
      result: 'W',
      resultTitle: 'Won',
      reason: 'Concession',
      reasonTitle: 'Winner: Opponent conceded',
      event: 'Weekly League',
      deck: 'Rohan <Riders>',
      replayUrl: '/gemp-lotr/ng/game?replayId=alice%24rec1',
    });
    expect(won?.dateTitle).toMatch(
      /^Ended 2025-11-20 12:05:00 server time \(UTC\); \d{4}-\d\d-\d\d \d\d:\d\d your time$/,
    );
    expect(lost).toMatchObject({ result: 'L', resultTitle: 'Lost', event: 'Casual', deck: '—', replayUrl: null });
    expect(old).toMatchObject({ date: '2025-11-18', dateTitle: null, length: '', reason: '—', reasonTitle: null });
    expect(serverDate({ ...page.entries[2]!, endTime: null })).toBe('');
  });
});
