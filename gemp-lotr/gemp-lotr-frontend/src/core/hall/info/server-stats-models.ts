import { TableSort } from '../table-sort';

/**
 * Server Info › Server Stats: GET /stats?startDay=&length= (ServerStatsRequestHandler, JSONDefs.PlayHistoryStats),
 * public. Only totals per format and period.
 */
export interface FormatGameCount {
  readonly format: string;
  readonly count: number;
  readonly casual: boolean;
}

export interface ServerStats {
  /** "yyyy-MM-dd", server time (UTC); "" when absent. */
  readonly startDate: string;
  readonly endDate: string;
  readonly activePlayers: number | null;
  readonly gamesCount: number | null;
  /** Games with a bot on either side (also counted in gamesCount). */
  readonly botGamesCount: number | null;
  readonly formats: readonly FormatGameCount[];
}

/** The period a start day spans (the server's `length`). */
export type StatsPeriod = 'month' | 'week' | 'day';
export const STATS_PERIODS: readonly StatsPeriod[] = ['month', 'week', 'day'];

/** What the stats on screen were asked for. */
export interface StatsQuery {
  readonly startDay: string;
  readonly period: StatsPeriod;
}

/** The columns of a stats table, in order (StatsUI.SORT_KEYS). */
export type StatsColumnKey = 'Format' | 'Count' | 'share' | 'ofTotal';
export const STATS_COLUMN_KEYS: readonly StatsColumnKey[] = ['Format', 'Count', 'share', 'ofTotal'];

export interface StatsColumn {
  readonly key: StatsColumnKey;
  readonly title: string;
}

export interface StatsRow {
  readonly format: string;
  readonly count: number;
  /** "37.50%" of the group's games; '' when the group has none. */
  readonly share: string;
  /** Of all the (non-bot) games. */
  readonly ofTotal: string;
}

/** One of the two tables (competitive, casual), its rows in the sort shown. */
export interface StatsTable {
  readonly group: 'competitive' | 'casual';
  readonly columns: readonly StatsColumn[];
  readonly rows: readonly StatsRow[];
  readonly sort: TableSort<StatsColumnKey>;
  readonly emptyText: string;
}
