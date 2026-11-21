import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { Notifier } from '../../notifier/notifier';
import { Timers } from '../../platform/timers';
import { nextSort, TableSort } from '../table-sort';
import { INVALID_PARAMETER } from './info-messages';
import { defaultStartDay, statsTables } from './server-stats';
import { ServerStats, StatsColumnKey, StatsPeriod, StatsQuery } from './server-stats-models';
import { ServerStatsApi } from './server-stats-api.service';

/**
 * Server Info › Server Stats (statsUi.js StatsUI, info.html): the start day and period fields, the stats last shown and
 * each table's sort. The page shows the stats of a month ago as it opens (`display()`); coming back to it after a while
 * fetches the stats shown again for the same dates, keeping the sorts and whatever is typed in the fields
 * (`refresh()`, the hall's return-refresh).
 */
@Injectable()
export class ServerStatsService {
  private readonly api = inject(ServerStatsApi);
  private readonly notifier = inject(Notifier);
  private readonly timers = inject(Timers);
  private seq = 0;

  readonly startDay = signal(defaultStartDay(new Date(this.timers.now())));
  readonly period = signal<StatsPeriod>('month');
  /** The stats on screen and what they were asked for; null until the first answer. */
  readonly stats = signal<ServerStats | null>(null);
  readonly shown = signal<StatsQuery | null>(null);
  readonly sorts = signal<{ competitive: TableSort<StatsColumnKey> | null; casual: TableSort<StatsColumnKey> | null }>({
    competitive: null,
    casual: null,
  });
  readonly tables = computed(() => {
    const stats = this.stats();
    return stats === null ? null : statsTables(stats, this.sorts());
  });

  /**
   * "Display Stats": the stats for the fields' dates. A start day the server can't read says "Invalid parameter
   * entered"; any other failure leaves what is shown, silently (the legacy page's own communication had no failure
   * handler). The sorts go back to the most games first, as a new table did.
   */
  async display(): Promise<void> {
    const query: StatsQuery = { startDay: this.startDay(), period: this.period() };
    const seq = ++this.seq;
    let stats: ServerStats;
    try {
      stats = await firstValueFrom(this.api.stats(query.startDay, query.period));
    } catch (error) {
      if (seq === this.seq && ApiError.from(error).is(400)) {
        await this.notifier.alert(INVALID_PARAMETER);
      }
      return;
    }
    if (seq !== this.seq) {
      return;
    }
    this.sorts.set({ competitive: null, casual: null });
    this.shown.set(query);
    this.stats.set(stats);
  }

  /** The stats shown, fetched again for the same dates; the sorts stay; a failure keeps what is shown. */
  async refresh(): Promise<void> {
    const query = this.shown();
    if (query === null) {
      return;
    }
    const seq = ++this.seq;
    try {
      const stats = await firstValueFrom(this.api.stats(query.startDay, query.period));
      if (seq === this.seq) {
        this.stats.set(stats);
      }
    } catch {
      // what is shown stays
    }
  }

  /** A header of a table was pressed. */
  sortBy(group: 'competitive' | 'casual', key: StatsColumnKey): void {
    const tables = this.tables();
    if (tables === null) {
      return;
    }
    const current = tables[group].sort;
    this.sorts.update((sorts) => ({ ...sorts, [group]: nextSort(current, key) }));
  }
}
