import { signal } from '@angular/core';
import { firstValueFrom, Observable } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { EventRow, updateRows } from './event-drawer';
import { eventErrorMessage } from './events-messages';

/** A list's state: loading (nothing shown yet), loaded (its rows, or its empty line), or the error in its place. */
export type EventListState =
  { readonly kind: 'loading' } | { readonly kind: 'loaded' } | { readonly kind: 'error'; readonly text: string };

/** A row was opened from a link: the view scrolls it into view (EventDrawer.scrollIntoView). */
export interface RevealedRow {
  readonly key: string;
  /** Changes with every reveal, so the same row can be revealed twice. */
  readonly seq: number;
}

export interface EventListSpec<TItem, TData> {
  /** Fetches the items; null when the answer had none to show. */
  fetch(): Observable<TItem[] | null>;
  /** The list's error line ("Could not load the current leagues. ..."). */
  failed(message: string): string;
  key(item: TItem): string;
  title(item: TItem): string;
  date(item: TItem): string;
  create(item: TItem): EventRow<TData>;
}

/**
 * The Current Leagues and Current Tournaments lists (LeagueResultsUI / TournamentResultsUI in list mode: loadList,
 * refreshList, showLeague / showTournament): rows of header and drawer, loaded once and refreshed in place.
 */
export class EventList<TItem, TData> {
  private readonly state_ = signal<EventListState>({ kind: 'loading' });
  private readonly rows_ = signal<EventRow<TData>[]>([]);
  private readonly revealed_ = signal<RevealedRow | null>(null);
  private loaded = false;
  private pending: Promise<void> | null = null;
  private seq = 0;

  readonly state = this.state_.asReadonly();
  readonly rows = this.rows_.asReadonly();
  readonly revealed = this.revealed_.asReadonly();

  constructor(private readonly spec: EventListSpec<TItem, TData>) {}

  /**
   * loadList: fetches the list and shows it afresh. Concurrent callers share one request; it resolves once the list
   * (or its error) is shown.
   */
  load(): Promise<void> {
    this.pending ??= firstValueFrom(this.spec.fetch())
      .then(
        (items) => {
          for (const row of this.rows_()) {
            row.drawer.destroy();
          }
          const rows = new Map<string, EventRow<TData>>();
          for (const item of items ?? []) {
            const key = this.spec.key(item);
            if (!rows.has(key)) {
              rows.set(key, this.spec.create(item));
            }
          }
          this.rows_.set([...rows.values()]);
          this.state_.set({ kind: 'loaded' });
          this.loaded = true;
        },
        (error: unknown) => {
          this.state_.set({ kind: 'error', text: this.spec.failed(eventErrorMessage(ApiError.from(error))) });
        },
      )
      .finally(() => (this.pending = null));
    return this.pending;
  }

  /**
   * refreshList: fetches the list again and updates it in place (rows keep their drawers; open ones are fetched
   * again where they are, shut ones when next opened). Not loaded yet: loads it. A failed refresh keeps what is
   * shown; one asked while a request is in flight does nothing.
   */
  refresh(): void {
    if (!this.loaded) {
      void this.load();
      return;
    }
    if (this.pending !== null) {
      return;
    }
    this.pending = firstValueFrom(this.spec.fetch())
      .then(
        (items) => {
          if (items !== null) {
            this.rows_.set(updateRows(this.rows_(), items, this.spec));
          }
        },
        () => undefined,
      )
      .finally(() => (this.pending = null));
  }

  /** Opens that row, loading the list first if need be, and reveals it. A key not listed leaves the list showing. */
  async show(key: string): Promise<void> {
    if (this.loaded && this.pending === null && this.reveal(key)) {
      return;
    }
    await this.load();
    this.reveal(key);
  }

  private reveal(key: string): boolean {
    const row = this.rows_().find((candidate) => candidate.key === key);
    if (!row) {
      return false;
    }
    row.drawer.open();
    this.revealed_.set({ key, seq: ++this.seq });
    return true;
  }
}
