import { computed, Signal, signal, WritableSignal } from '@angular/core';
import { firstValueFrom, Observable } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { Timers } from '../../platform/timers';
import { shiftMonth, utcMonthOf, YearMonth } from './calendar-grid';
import { EventDrawer } from './event-drawer';
import {
  groupMonthsByYear,
  monthKey,
  monthLabel,
  MonthGroup,
  neighbourMonth,
  newestMonth,
  parseMonthKey,
} from './event-months';
import { CompletedEvent, CompletedEventsMonth, EventKind, LeagueDetail, TournamentDetail } from './events-models';
import {
  eventErrorMessage,
  HISTORY_NOT_DISPLAYED,
  historyCount,
  historyDetailsFailed,
  historyLoadingAll,
  historyLoadingMonth,
  historyMonthFailed,
  historyNoneIn,
  historyNoneYet,
  noHistoryDetails,
  shownStatus,
} from './events-messages';

/** The calls the browser makes, and the caches its panels share (EventHistoryUI.cache, monthsCache). */
export interface EventHistorySource {
  months(kind: EventKind): Observable<string[]>;
  month(month: string, kind: EventKind): Observable<CompletedEventsMonth>;
  league(code: string): Observable<LeagueDetail | null>;
  tournament(id: string): Observable<TournamentDetail | null>;
  /** kind -> its months, fetched at most once a page. */
  readonly monthsCache: Map<EventKind, string[]>;
  /** "kind|yyyy-MM" -> the month's answer. */
  readonly monthCache: Map<string, CompletedEventsMonth>;
}

/** The status line under the header: what the panel is doing or found; `retry` with an error. */
export interface HistoryStatus {
  readonly state: 'loading' | 'empty' | 'loaded' | 'error';
  readonly text: string;
  readonly retry: boolean;
}

/** The month dropdown's content (MonthNav.renderList). */
export type MonthDropdown =
  | { readonly state: 'loading' }
  | { readonly state: 'error'; readonly text: string }
  | {
      readonly state: 'ready';
      readonly groups: readonly MonthGroup[];
      readonly current: string;
      readonly emptyText: string;
    };

/** A completed event's row; `drawer` is its details (none for an event without an id). */
export interface CompletedEventRow {
  readonly event: CompletedEvent;
  readonly drawer: EventDrawer<LeagueDetail> | EventDrawer<TournamentDetail> | null;
}

type MonthsState = 'idle' | 'loading' | 'ready' | 'error';

/**
 * The completed events of one kind, a month at a time (eventHistoryUi.js EventHistoryUI with its MonthNav). The
 * panel is built at once but asks nothing until start(), which the tab calls once the live list above it is shown:
 * it shows the newest month with completed events (up to the current UTC month), or says there are none yet. A
 * navigation before start() starts the panel on that month instead. The arrows jump to the nearest month with
 * events, from the list of months (fetched once, on the first arrow or the dropdown's first opening), and are
 * disabled where there is none; without that list they step a calendar month (never past the current one).
 */
export class EventHistoryBrowser {
  private readonly month_: WritableSignal<YearMonth>;
  private readonly monthsState_ = signal<MonthsState>('idle');
  private readonly monthsError_ = signal<string | null>(null);
  private readonly dropdownOpen_ = signal(false);
  private readonly status_: WritableSignal<HistoryStatus>;
  private readonly shown_ = signal<CompletedEventsMonth | null>(null);
  private readonly rows_ = signal<CompletedEventRow[]>([]);
  private live = false;
  private started = false;
  private touched = false;
  private requested: string | null = null;
  private monthsWaiting: ((list: string[] | null) => void)[] = [];

  /** The month in the title. */
  readonly month: Signal<YearMonth>;
  readonly title = computed(() => monthLabel(monthKey(this.month_())));
  readonly status: Signal<HistoryStatus>;
  /** The month whose events are shown (with isAdmin: the Admin column); null while none is. */
  readonly shown = this.shown_.asReadonly();
  readonly rows = this.rows_.asReadonly();
  readonly dropdownOpen = this.dropdownOpen_.asReadonly();
  readonly previousDisabled = computed(() => this.disabled(-1));
  readonly nextDisabled = computed(() => this.disabled(1));
  readonly dropdown = computed<MonthDropdown>(() => {
    const state = this.monthsState_();
    if (state === 'loading') {
      return { state: 'loading' };
    }
    if (state === 'error') {
      return { state: 'error', text: this.monthsError_() ?? '' };
    }
    return {
      state: 'ready',
      groups: groupMonthsByYear(this.source.monthsCache.get(this.kind) ?? []),
      current: monthKey(this.month_()),
      emptyText: historyNoneYet(this.kind),
    };
  });

  constructor(
    readonly kind: EventKind,
    private readonly source: EventHistorySource,
    private readonly timers: Timers,
  ) {
    this.month_ = signal(utcMonthOf(timers.now()));
    this.status_ = signal({ state: 'loading', text: historyLoadingAll(kind), retry: false });
    this.month = this.month_.asReadonly();
    this.status = this.status_.asReadonly();
  }

  /** Loads the panel once: the newest month with completed events, or the month the viewer navigated to. */
  start(): void {
    if (this.started) {
      return;
    }
    this.started = true;
    this.live = true;
    if (this.touched) {
      this.load();
      return;
    }
    this.loadMonths((list) => {
      if (this.touched) {
        return;
      }
      if (list === null) {
        this.load();
        return;
      }
      const newest = newestMonth(list, this.currentKey());
      if (newest === null) {
        this.setStatus('empty', historyNoneYet(this.kind));
        return;
      }
      this.setMonth(newest, true);
    });
  }

  /** The arrows: the nearest month with events before (delta < 0) or after it. */
  jump(delta: number): void {
    const months = this.source.monthsCache.get(this.kind);
    if (months !== undefined) {
      const target = neighbourMonth(months, monthKey(this.month_()), delta);
      if (target !== null) {
        this.setMonth(target);
      }
      return;
    }
    this.loadMonths((list) => (list === null ? this.step(delta) : this.jump(delta)));
  }

  /** A month picked in the dropdown, or set by the panel itself (`auto`); anything not "yyyy-MM" is ignored. */
  setMonth(key: string, auto = false): void {
    const month = parseMonthKey(key);
    if (month === null) {
      return;
    }
    this.month_.set(month);
    this.navigated(auto);
  }

  toggleDropdown(): void {
    if (this.dropdownOpen_()) {
      this.closeDropdown();
    } else {
      this.openDropdown();
    }
  }

  openDropdown(): void {
    this.dropdownOpen_.set(true);
    if (this.source.monthsCache.has(this.kind)) {
      this.monthsState_.set('ready');
    } else {
      this.loadMonths();
    }
  }

  closeDropdown(): void {
    this.dropdownOpen_.set(false);
  }

  /** A month picked in the dropdown: it closes, and the month loads. */
  pick(key: string): void {
    this.closeDropdown();
    this.setMonth(key);
  }

  /** The dropdown's Retry. */
  retryMonths(): void {
    this.monthsState_.set('idle');
    this.loadMonths();
  }

  /** The status line's Retry, for the month that failed. */
  retry(): void {
    if (this.status_().retry && this.requested === monthKey(this.month_())) {
      this.load();
    }
  }

  // ---- internals ----

  private currentKey(): string {
    return monthKey(utcMonthOf(this.timers.now()));
  }

  private atCurrentMonth(): boolean {
    return monthKey(this.month_()) >= this.currentKey();
  }

  private disabled(delta: number): boolean {
    if (this.monthsState_() === 'loading') {
      return true;
    }
    const months = this.source.monthsCache.get(this.kind);
    if (months !== undefined) {
      return neighbourMonth(months, monthKey(this.month_()), delta) === null;
    }
    return delta > 0 && this.atCurrentMonth();
  }

  /** One calendar month, when the months list could not be fetched. */
  private step(delta: number): void {
    if (delta > 0 && this.atCurrentMonth()) {
      return;
    }
    this.month_.set(shiftMonth(this.month_(), delta));
    this.navigated(false);
  }

  private navigated(auto: boolean): void {
    if (!auto) {
      this.touched = true;
    }
    if (this.live) {
      this.load();
    } else {
      this.start();
    }
  }

  /** The months list, once; concurrent callers share the request. `then` gets the list, or null on failure. */
  private loadMonths(then?: (list: string[] | null) => void): void {
    const cached = this.source.monthsCache.get(this.kind);
    if (cached !== undefined) {
      this.monthsState_.set('ready');
      then?.(cached);
      return;
    }
    if (this.monthsState_() === 'loading') {
      if (then) {
        this.monthsWaiting.push(then);
      }
      return;
    }
    this.monthsState_.set('loading');
    this.monthsError_.set(null);
    this.monthsWaiting = then ? [then] : [];
    const finish = (list: string[] | null) => {
      const waiting = this.monthsWaiting;
      this.monthsWaiting = [];
      for (const callback of waiting) {
        callback(list);
      }
    };
    firstValueFrom(this.source.months(this.kind)).then(
      (list) => {
        this.source.monthsCache.set(this.kind, list);
        this.monthsState_.set('ready');
        finish(list);
      },
      (error: unknown) => {
        this.monthsState_.set('error');
        this.monthsError_.set(eventErrorMessage(ApiError.from(error)));
        finish(null);
      },
    );
  }

  /** One month: from the cache, or fetched. An answer for a month the viewer has left is kept but not shown. */
  private load(): void {
    const month = monthKey(this.month_());
    const cacheKey = `${this.kind}|${month}`;
    this.requested = month;
    const cached = this.source.monthCache.get(cacheKey);
    if (cached !== undefined) {
      this.show(cached, month);
      return;
    }
    this.clearRows();
    this.setStatus('loading', historyLoadingMonth(monthLabel(month)));
    firstValueFrom(this.source.month(month, this.kind)).then(
      (answer) => {
        this.source.monthCache.set(cacheKey, answer);
        if (this.requested === month) {
          this.show(answer, month);
        }
      },
      (error: unknown) => {
        if (this.requested !== month) {
          return;
        }
        this.clearRows();
        this.setStatus('error', historyMonthFailed(monthLabel(month), eventErrorMessage(ApiError.from(error))), true);
      },
    );
  }

  /** renderMonth */
  private show(answer: CompletedEventsMonth, month: string): void {
    this.clearRows();
    const label = monthLabel(month);
    if (answer.events.length === 0) {
      this.setStatus('empty', historyNoneIn(this.kind, label));
      return;
    }
    this.setStatus('loaded', historyCount(this.kind, label, answer.events.length));
    this.shown_.set(answer);
    this.rows_.set(answer.events.map((event) => ({ event, drawer: event.id === null ? null : this.drawer(event.id) })));
  }

  /** renderDetailsButton / loadDetails: the league's or tournament's detail as the live lists show it. */
  private drawer(id: string): EventDrawer<LeagueDetail> | EventDrawer<TournamentDetail> {
    const common = {
      failure: (error: ApiError) => historyDetailsFailed(shownStatus(error), eventErrorMessage(error)),
      unreadableText: HISTORY_NOT_DISPLAYED,
      emptyText: noHistoryDetails(this.kind),
    };
    return this.kind === 'league'
      ? new EventDrawer<LeagueDetail>({ ...common, load: () => this.source.league(id) })
      : new EventDrawer<TournamentDetail>({ ...common, load: () => this.source.tournament(id) });
  }

  private clearRows(): void {
    for (const row of this.rows_()) {
      row.drawer?.destroy();
    }
    this.rows_.set([]);
    this.shown_.set(null);
  }

  private setStatus(state: HistoryStatus['state'], text: string, retry = false): void {
    this.status_.set({ state, text, retry });
  }
}
