import { computed, signal, WritableSignal } from '@angular/core';
import { Observable, Subscription } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../../api/api-error';
import { DETAILS_CLOSED, DETAILS_OPEN, DRAWER_LOADING } from './events-messages';

/**
 * The slide-open details drawer of the Events tab, without its view (eventDrawer.js EventDrawer): its content is
 * fetched on the first opening; later toggles only open and shut it. A failed fetch is fetched again at the next
 * opening, and so is a drawer marked stale (its data was refreshed while it was shut): it reopens showing its old
 * content, which the new one then replaces. An answer for content since replaced is ignored.
 */

/**
 * What the drawer's pane shows: the loading line, what was fetched, what stands in for it when it could not be
 * (the calendar's facts), or a message (`error`: the fetch or the display failed; `empty`: nothing to show).
 */
export type DrawerContent<T> =
  | { readonly kind: 'loading'; readonly text: string }
  | { readonly kind: 'data'; readonly data: T }
  | { readonly kind: 'fallback' }
  | { readonly kind: 'error'; readonly text: string }
  | { readonly kind: 'empty'; readonly text: string };

/** never opened | loading | loaded | error (fetched again at the next opening) | stale (loaded; fetched again when opened). */
export type DrawerStatus = 'idle' | 'loading' | 'loaded' | 'error' | 'stale';

export interface DrawerSource<T> {
  /** Fetches the content. null: the answer had nothing to show (`emptyText`). */
  load(): Observable<T | null>;
  /** Why the fetch failed, inline. */
  failure(error: ApiError): string;
  /** The answer could not be read ("Could not display the league."). */
  readonly unreadableText: string;
  readonly emptyText: string;
  /** Show the fallback instead of an error when the fetch fails (the calendar's preview). */
  readonly fallback?: boolean;
  /** false: do not ask the server at all; the fallback shows at once (a tournament that has not started). */
  readonly fetch?: boolean;
}

export class EventDrawer<T> {
  private readonly open_ = signal(false);
  private readonly content_ = signal<DrawerContent<T> | null>(null);
  private status_: DrawerStatus = 'idle';
  private quiet = false;
  private request: Subscription | null = null;
  /** Bumped whenever the pane is rebuilt or a fetch starts: answers of an older one are ignored. */
  private generation = 0;
  private alive_ = true;

  readonly isOpen = this.open_.asReadonly();
  /** null until first opened. */
  readonly content = this.content_.asReadonly();
  /** The + Details / − Details toggle's label. */
  readonly toggleLabel = computed(() => (this.open_() ? DETAILS_OPEN : DETAILS_CLOSED));

  constructor(private readonly source: DrawerSource<T>) {}

  get status(): DrawerStatus {
    return this.status_;
  }

  /** false once destroyed (its row or preview went away). */
  get alive(): boolean {
    return this.alive_;
  }

  toggle(): void {
    if (this.open_()) {
      this.close();
    } else {
      this.open();
    }
  }

  open(): void {
    if (this.open_()) {
      return;
    }
    if (this.content_() !== null && this.status_ !== 'error') {
      const stale = this.status_ === 'stale';
      this.open_.set(true);
      if (stale) {
        this.refresh(true);
      }
      return;
    }
    this.open_.set(true);
    this.generation++;
    this.content_.set({ kind: 'loading', text: DRAWER_LOADING });
    this.startLoad(false);
  }

  close(): void {
    this.open_.set(false);
  }

  /**
   * Fetches and shows the content again in place (after joining a league), keeping the drawer open or shut; the old
   * content stays until the new one replaces it. `quiet` (a data refresh): a failed fetch keeps the old content, and
   * the drawer is fetched again at its next refresh or opening.
   */
  refresh(quiet = false): void {
    if (this.content_() === null || !this.alive_) {
      return;
    }
    this.startLoad(quiet);
  }

  /** The data behind a shut, loaded drawer has (or may have) changed: its next opening fetches again. */
  invalidate(): void {
    if (!this.open_() && this.status_ === 'loaded') {
      this.status_ = 'stale';
    }
  }

  /** Stops the fetch in flight (the drawer's row went away). */
  destroy(): void {
    this.alive_ = false;
    this.request?.unsubscribe();
    this.request = null;
    this.generation++;
  }

  private startLoad(quiet: boolean): void {
    this.request?.unsubscribe();
    this.status_ = 'loading';
    this.quiet = quiet;
    const generation = ++this.generation;
    if (this.source.fetch === false) {
      this.settle({ kind: 'fallback' }, 'loaded');
      return;
    }
    this.request = this.source.load().subscribe({
      next: (data) => {
        if (generation === this.generation) {
          this.settle(
            data === null ? { kind: 'empty', text: this.source.emptyText } : { kind: 'data', data },
            'loaded',
          );
        }
      },
      error: (error: unknown) => {
        if (generation === this.generation) {
          this.failed(ApiError.from(error));
        }
      },
    });
  }

  private failed(error: ApiError): void {
    if (this.quiet) {
      this.quiet = false;
      this.status_ = 'stale';
      return;
    }
    if (error.status === INVALID_RESPONSE_STATUS) {
      this.settle({ kind: 'error', text: this.source.unreadableText }, 'error');
    } else if (this.source.fallback) {
      this.settle({ kind: 'fallback' }, 'loaded');
    } else {
      this.settle({ kind: 'error', text: this.source.failure(error) }, 'error');
    }
  }

  private settle(content: DrawerContent<T>, status: DrawerStatus): void {
    this.quiet = false;
    this.request = null;
    this.content_.set(content);
    this.status_ = status;
  }
}

/** A row of the Events lists: its key, header texts and drawer. */
export interface EventRow<T> {
  readonly key: string;
  readonly title: WritableSignal<string>;
  readonly date: WritableSignal<string>;
  readonly drawer: EventDrawer<T>;
}

/**
 * EventDrawer.updateRows: the rows for freshly fetched items, keeping the rows whose key is still listed (their
 * drawer and its open or shut state; their title and date updated). A kept drawer that is open is fetched again
 * quietly where it is, a shut one marked stale. New items get rows, rows no longer listed go (their fetch stopped),
 * and the rows follow the items' order; a key listed twice keeps its first place.
 */
export function updateRows<TItem, TData>(
  rows: readonly EventRow<TData>[],
  items: readonly TItem[],
  spec: {
    key(item: TItem): string;
    title(item: TItem): string;
    date(item: TItem): string;
    create(item: TItem): EventRow<TData>;
  },
): EventRow<TData>[] {
  const old = new Map(rows.map((row) => [row.key, row]));
  const next = new Map<string, EventRow<TData>>();
  const kept: EventRow<TData>[] = [];
  for (const item of items) {
    const key = spec.key(item);
    if (next.has(key)) {
      continue;
    }
    let row = old.get(key);
    if (row) {
      row.title.set(spec.title(item));
      row.date.set(spec.date(item));
      kept.push(row);
    } else {
      row = spec.create(item);
    }
    next.set(key, row);
  }
  for (const row of rows) {
    if (next.get(row.key) !== row) {
      row.drawer.destroy();
    }
  }
  for (const row of kept) {
    if (row.drawer.isOpen()) {
      row.drawer.refresh(true);
    } else {
      row.drawer.invalidate();
    }
  }
  return [...next.values()];
}

/** A new row for an item. */
export function eventRow<T>(key: string, title: string, date: string, drawer: EventDrawer<T>): EventRow<T> {
  return { key, title: signal(title), date: signal(date), drawer };
}
