import { DOCUMENT } from '@angular/common';
import { Component, DestroyRef, inject } from '@angular/core';
import * as words from '../../../core/hall/info/info-messages';
import { STATS_CAPTIONS } from '../../../core/hall/info/server-stats';
import { STATS_PERIODS, StatsColumnKey, StatsPeriod, StatsTable } from '../../../core/hall/info/server-stats-models';
import { ServerStatsService } from '../../../core/hall/info/server-stats.service';
import { ariaSort } from '../../../core/hall/table-sort';
import { REFRESH_AFTER_MS, ReturnRefresh } from '../../../core/hall/events/return-refresh';
import { Timers } from '../../../core/platform/timers';

/** When the viewer last left the page (the sub-tab), per service: coming back counts from there. */
const leftAt = new WeakMap<ServerStatsService, number>();
const PART = 'stats';

/**
 * Server Info › Server Stats (info.html #server-stats, statsUi.js): the start day and period, "Display Stats", the
 * figures of the period and the competitive and casual games by format, each table sorted by a press on its header
 * (▲ / ▼). The stats of a month ago show as the page opens; coming back to the browser window after a while fetches
 * them again.
 *
 * DOM contract: `gd-server-stats.gd-stats` holding `p.gd-stats__note`, `div.gd-stats__params`
 * (`input.gd-stats__start`, `select.gd-stats__period`, `input[type=button].gd-stats__display`), `div.gd-stats__period`
 * (`span.gd-stats__start-date`, `span.gd-stats__end-date`), `div.gd-stats__figure`s (`data-figure` = active-players,
 * games, bot-games), and per group (`data-group` = competitive, casual) `h2.gd-stats__caption` and
 * `table.gd-stats__table` (`th` with `aria-sort` holding `button.gd-stats__sort` and `span.gd-stats__sort-mark`;
 * `td.gd-stats__empty` when it has no rows).
 */
@Component({
  selector: 'gd-server-stats',
  styleUrl: './server-stats.component.css',
  host: { class: 'gd-stats' },
  templateUrl: './server-stats.component.html',
})
export class ServerStatsComponent {
  protected readonly stats = inject(ServerStatsService);
  protected readonly words = words;
  protected readonly periods = STATS_PERIODS;
  protected readonly captions = STATS_CAPTIONS;

  constructor() {
    // the hall's return-refresh (GempHallTabs.onReturn): back to the sub-tab, the browser tab or the window after
    // 10 s or more, the stats shown are fetched again
    const timers = inject(Timers);
    const returns = new ReturnRefresh(timers);
    const left = leftAt.get(this.stats);
    returns.register(PART, () => void this.stats.refresh());
    if (this.stats.shown() === null) {
      void this.stats.display();
    } else if (left !== undefined && timers.now() - left >= REFRESH_AFTER_MS) {
      void this.stats.refresh();
    }
    returns.setShown(PART, true);
    const document = inject(DOCUMENT);
    const view = document.defaultView;
    const pageShown = () => returns.setPageShown(document.visibilityState !== 'hidden' && document.hasFocus());
    const events: [EventTarget | null, string][] = [
      [document, 'visibilitychange'],
      [view, 'focus'],
      [view, 'blur'],
    ];
    for (const [target, type] of events) target?.addEventListener(type, pageShown);
    inject(DestroyRef).onDestroy(() => {
      for (const [target, type] of events) target?.removeEventListener(type, pageShown);
      leftAt.set(this.stats, timers.now());
    });
  }

  protected tables(): StatsTable[] {
    const tables = this.stats.tables();
    return tables ? [tables.competitive, tables.casual] : [];
  }

  protected sortOf(table: StatsTable, key: StatsColumnKey) {
    return ariaSort(table.sort, key);
  }

  protected mark(table: StatsTable, key: StatsColumnKey): string {
    const sort = this.sortOf(table, key);
    return sort === 'ascending' ? ' ▲' : sort === 'descending' ? ' ▼' : '';
  }

  protected cell(row: StatsTable['rows'][number], key: StatsColumnKey): string | number {
    return key === 'Format' ? row.format : key === 'Count' ? row.count : key === 'share' ? row.share : row.ofTotal;
  }

  protected setStart(event: Event): void {
    this.stats.startDay.set((event.target as HTMLInputElement).value);
  }

  protected setPeriod(event: Event): void {
    this.stats.period.set((event.target as HTMLSelectElement).value as StatsPeriod);
  }
}
