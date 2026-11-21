import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { AccountApi } from './account-api.service';
import {
  CASUAL_EVENT,
  FROM_AFTER_TO,
  HISTORY_BAD_FILTERS,
  HISTORY_FAILED,
  HISTORY_LOADING,
  HISTORY_NOT_LOGGED_IN,
  HISTORY_UNREACHABLE,
  historyShowing,
  NO_GAMES_ROW,
  NO_GAMES_YET,
  NO_MATCHING_GAMES,
  pageOf,
} from './account-messages';
import {
  GameHistoryFilterOptions,
  GameHistoryFilters,
  GameHistoryPage,
  NO_GAME_HISTORY_FILTERS,
} from './account-models';
import { GameHistoryRow, gameHistoryRow } from './game-history-rows';

/** GameHistoryUI.pageSize. */
export const GAME_HISTORY_PAGE_SIZE = 20;

/** The pager above and below the table (GameHistoryUI.updatePager). */
export interface GameHistoryPager {
  /** Shown only when there is more than one page. */
  readonly shown: boolean;
  /** "Page n of m". */
  readonly text: string;
  readonly firstDisabled: boolean;
  readonly previousDisabled: boolean;
  readonly nextDisabled: boolean;
  readonly lastDisabled: boolean;
}

/** An event suggestion: the value the field takes, and a label where it has one ("Casual games"). */
export interface EventSuggestion {
  readonly value: string;
  readonly label: string | null;
}

function hasFilters(filters: GameHistoryFilters): boolean {
  return Object.values(filters).some((value) => value !== '');
}

/** The filters as the form has them, trimmed (GameHistoryUI.readFilters). */
export function readFilters(form: GameHistoryFilters): GameHistoryFilters {
  return {
    format: form.format,
    opponent: form.opponent.trim(),
    event: form.event.trim(),
    from: form.from,
    to: form.to,
  };
}

/**
 * My Account > My Game History (gameHistoryUi.js GameHistoryUI): the player's games, newest first, a page of 20 at a
 * time, with the server's filters (format, opponent, event, the days the game ended). What is typed in the form but
 * not applied is the view's; `filters` are those applied. Only the latest request's answer is shown.
 */
@Injectable()
export class GameHistory {
  private readonly api = inject(AccountApi);

  private readonly filters_ = signal<GameHistoryFilters>(NO_GAME_HISTORY_FILTERS);
  private readonly start_ = signal(0);
  private readonly matching_ = signal(0);
  private readonly page_ = signal<GameHistoryPage | null>(null);
  private readonly status_ = signal('');
  private readonly loading_ = signal(false);
  private readonly options_ = signal<GameHistoryFilterOptions>({ formats: [], events: [] });
  private requestNumber = 0;
  private started = false;

  /** The filters applied. */
  readonly filters = this.filters_.asReadonly();
  /** The status line under the upper pager (a live region in the view). */
  readonly status = this.status_.asReadonly();
  /** The format list's choices after "Any format". */
  readonly formats = computed(() => this.options_().formats);
  /** The event field's suggestions: "Casual games", then the server's. */
  readonly events = computed<EventSuggestion[]>(() => [
    { value: CASUAL_EVENT.value, label: CASUAL_EVENT.label },
    ...this.options_().events.map((value) => ({ value, label: null })),
  ]);
  /** The table's rows; null until the first page has come (no table yet). */
  readonly rows = computed<GameHistoryRow[] | null>(() => {
    const page = this.page_();
    return page ? page.entries.map((entry) => gameHistoryRow(entry, page.playerId)) : null;
  });
  /** The table's one row when it has none. */
  readonly emptyRow = computed(() => (hasFilters(this.filters_()) ? NO_MATCHING_GAMES : NO_GAMES_ROW));
  readonly pageCount = computed(() => Math.max(1, Math.ceil(this.matching_() / GAME_HISTORY_PAGE_SIZE)));
  /** Both pagers; every button is disabled while a page loads. */
  readonly pager = computed<GameHistoryPager>(() => {
    const pages = this.pageCount();
    const page = Math.floor(this.start_() / GAME_HISTORY_PAGE_SIZE) + 1;
    const loading = this.loading_();
    const atStart = loading || this.start_() <= 0;
    const atEnd = loading || page >= pages;
    return {
      shown: this.matching_() > GAME_HISTORY_PAGE_SIZE,
      text: pageOf(page, pages),
      firstDisabled: atStart,
      previousDisabled: atStart,
      nextDisabled: atEnd,
      lastDisabled: atEnd,
    };
  });

  /** The sub-tab's first showing: the filters' suggestions and the first page (GameHistoryUI.init). */
  start(): void {
    if (this.started) {
      return;
    }
    this.started = true;
    this.loadFilterOptions();
    void this.load(false);
  }

  /** Apply (GameHistoryUI.applyFilters): back to the first page; refused when From is after To. */
  apply(form: GameHistoryFilters): void {
    const filters = readFilters(form);
    if (filters.from !== '' && filters.to !== '' && filters.from > filters.to) {
      this.status_.set(FROM_AFTER_TO);
      return;
    }
    this.filters_.set(filters);
    this.start_.set(0);
    void this.load(false);
  }

  /** Clear (GameHistoryUI.clearFilters): no filters, the first page. The view empties its fields. */
  clear(): void {
    this.filters_.set(NO_GAME_HISTORY_FILTERS);
    this.start_.set(0);
    void this.load(false);
  }

  first(): void {
    this.goToStart(0);
  }

  previous(): void {
    this.goToStart(this.start_() - GAME_HISTORY_PAGE_SIZE);
  }

  next(): void {
    this.goToStart(this.start_() + GAME_HISTORY_PAGE_SIZE);
  }

  last(): void {
    this.goToStart((this.pageCount() - 1) * GAME_HISTORY_PAGE_SIZE);
  }

  /**
   * A data refresh (the viewer came back after a while: GameHistoryUI.refresh): the suggestions and the same page with
   * the same filters, without "Loading…"; a failure keeps what is shown.
   */
  refresh(): void {
    if (!this.started) {
      return;
    }
    this.loadFilterOptions();
    void this.load(true);
  }

  private goToStart(start: number): void {
    const maxStart = (this.pageCount() - 1) * GAME_HISTORY_PAGE_SIZE;
    const clamped = Math.max(0, Math.min(maxStart, start));
    if (clamped === this.start_()) {
      return;
    }
    this.start_.set(clamped);
    void this.load(false);
  }

  /** loadFilterOptions: a failure leaves the filters as free text, quietly. */
  private loadFilterOptions(): void {
    this.api.gameHistoryFilters().subscribe({ next: (options) => this.options_.set(options), error: () => undefined });
  }

  /** loadHistory; `quiet`: a data refresh. */
  private async load(quiet: boolean): Promise<void> {
    const requestNumber = ++this.requestNumber;
    if (!quiet) {
      this.status_.set(HISTORY_LOADING);
      this.loading_.set(true);
    }
    let page: GameHistoryPage | null;
    try {
      page = await firstValueFrom(this.api.gameHistory(this.start_(), GAME_HISTORY_PAGE_SIZE, this.filters_()));
    } catch (error) {
      if (requestNumber === this.requestNumber && !quiet) {
        this.failed(historyError(ApiError.from(error)));
      }
      return;
    }
    if (requestNumber !== this.requestNumber) {
      return; // an older request answering after a newer one
    }
    if (page === null) {
      if (!quiet) {
        this.failed(HISTORY_FAILED);
      }
      return;
    }
    this.loaded(page);
  }

  private failed(message: string): void {
    this.status_.set(message);
    this.loading_.set(false);
  }

  /** loadedGameHistory */
  private loaded(page: GameHistoryPage): void {
    this.matching_.set(page.matching);
    if (page.start !== null) {
      this.start_.set(page.start);
    }
    this.page_.set(page);
    const filtered = hasFilters(this.filters_());
    if (page.matching === 0) {
      this.status_.set(filtered ? NO_MATCHING_GAMES : NO_GAMES_YET);
    } else {
      const first = this.start_() + 1;
      const last = this.start_() + page.entries.length;
      this.status_.set(historyShowing(first, last, page.matching, filtered, page.total));
    }
    this.loading_.set(false);
  }
}

/** The legacy errorMap of loadHistory; a status it did not name left the page "Loading…" for good. */
function historyError(error: ApiError): string {
  switch (error.status) {
    case 0:
      return HISTORY_UNREACHABLE;
    case 400:
      return headerMessage(error.cause) ?? HISTORY_BAD_FILTERS;
    case 401:
      return HISTORY_NOT_LOGGED_IN;
    default:
      return HISTORY_FAILED;
  }
}

/** The server's words for a 400 (GempukkuHttpRequestHandler sends them in the `message` header). */
function headerMessage(cause: unknown): string | null {
  if (cause === null || typeof cause !== 'object') {
    return null;
  }
  const headers = (cause as { headers?: { get(name: string): string | null } }).headers;
  const message = headers?.get('message') ?? null;
  return message !== null && message !== '' ? message : null;
}
