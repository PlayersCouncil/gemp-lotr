import { listOf } from '../../api/json';
import { sortRows, TableSort } from '../table-sort';
import {
  CASUAL_CAPTION,
  COMPETITIVE_CAPTION,
  COUNT_COLUMN,
  FORMAT_COLUMN,
  NO_CASUAL_GAMES,
  NO_COMPETITIVE_GAMES,
  OF_TOTAL_COLUMN,
  shareColumn,
} from './info-messages';
import { FormatGameCount, ServerStats, StatsColumn, StatsColumnKey, StatsRow, StatsTable } from './server-stats-models';

/** Ported from js/gemp-022/statsUi.js (StatsUI). */

type Json = Record<string, unknown>;

function numberOrNull(value: unknown): number | null {
  return typeof value === 'number' && Number.isFinite(value) ? value : null;
}

/** The answer of GET /stats (anything missing shows as empty, as loadedStats did). */
export function parseServerStats(json: unknown): ServerStats {
  const data: Json = json !== null && typeof json === 'object' ? (json as Json) : {};
  const formats: FormatGameCount[] = listOf(data['Stats']).map((item: unknown) => {
    const row: Json = item !== null && typeof item === 'object' ? (item as Json) : {};
    return {
      format: typeof row['Format'] === 'string' ? row['Format'] : '',
      count: numberOrNull(row['Count']) ?? 0,
      casual: row['Casual'] === true,
    };
  });
  return {
    startDate: typeof data['StartDate'] === 'string' ? data['StartDate'] : '',
    endDate: typeof data['EndDate'] === 'string' ? data['EndDate'] : '',
    activePlayers: numberOrNull(data['ActivePlayers']),
    gamesCount: numberOrNull(data['GamesCount']),
    botGamesCount: numberOrNull(data['BotGamesCount']),
    formats,
  };
}

/**
 * The start day the form opens with: a month before today, in the viewer's own calendar, the day kept when that month
 * has it and else its last day; written without zero padding ("2026-2-28"), which the server reads (StatsUI.init).
 */
export function defaultStartDay(now: Date): string {
  const date = new Date(now.getTime());
  const day = date.getDate();
  date.setMonth(date.getMonth() - 1);
  if (date.getDate() !== day) {
    date.setDate(0);
  }
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
}

/** "37.50%" of `total`, as the viewer's locale writes percentages; '' for a total of none (getPercentage). */
export function percentage(part: number, total: number, locale?: string): string {
  return total > 0 ? (part / total).toLocaleString(locale, { style: 'percent', minimumFractionDigits: 2 }) : '';
}

/** The sort each table opens with: the most games first. */
export const DEFAULT_STATS_SORT: TableSort<StatsColumnKey> = { key: 'Count', dir: 'desc' };

/** The captions of the two tables. */
export const STATS_CAPTIONS = { competitive: COMPETITIVE_CAPTION, casual: CASUAL_CAPTION } as const;

function sortValue(key: StatsColumnKey, row: StatsRow): unknown {
  // the percentages sort by the number of games
  return key === 'Format' ? row.format : row.count;
}

/**
 * The two tables (loadedStats): competitive and casual games by format, each with its share of its group and of all
 * the games counted (bot games are not), in the sort given.
 */
export function statsTables(
  stats: ServerStats,
  sorts: { competitive?: TableSort<StatsColumnKey> | null; casual?: TableSort<StatsColumnKey> | null } = {},
  locale?: string,
): { competitive: StatsTable; casual: StatsTable } {
  let casuals = 0;
  let comps = 0;
  for (const item of stats.formats) {
    if (item.casual) casuals += item.count;
    else comps += item.count;
  }
  const total = casuals + comps;
  const table = (group: 'competitive' | 'casual', groupTotal: number, emptyText: string): StatsTable => {
    const columns: StatsColumn[] = [
      { key: 'Format', title: FORMAT_COLUMN },
      { key: 'Count', title: COUNT_COLUMN },
      { key: 'share', title: shareColumn(group) },
      { key: 'ofTotal', title: OF_TOTAL_COLUMN },
    ];
    const rows: StatsRow[] = stats.formats
      .filter((item) => item.casual === (group === 'casual'))
      .map((item) => ({
        format: item.format,
        count: item.count,
        share: percentage(item.count, groupTotal, locale),
        ofTotal: percentage(item.count, total, locale),
      }));
    const sort = sorts[group] ?? DEFAULT_STATS_SORT;
    return { group, columns, rows: sortRows(rows, sort, (row) => sortValue(sort.key, row)), sort, emptyText };
  };
  return {
    competitive: table('competitive', comps, NO_COMPETITIVE_GAMES),
    casual: table('casual', casuals, NO_CASUAL_GAMES),
  };
}
