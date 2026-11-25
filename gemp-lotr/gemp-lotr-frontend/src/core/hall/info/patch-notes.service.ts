import { computed, inject, Injectable, signal } from '@angular/core';
import { ApiError } from '../../api/api-error';
import { TimerHandle, Timers } from '../../platform/timers';
import { monthLabel, monthOfDate, neighbourMonth } from '../month-keys';
import {
  ALL_FILTER,
  missingUpdate,
  monthsFailed,
  NO_OLDER_UPDATES,
  NO_PATCH_NOTES,
  noTaggedUpdates,
  serverAnswered,
  showingAllUpdates,
  updatesRange,
} from './info-messages';
import { PATCH_NOTE_TAGS, PATCH_NOTES_PAGE_SIZE } from './patch-notes';
import { PATCH_NOTES_CACHE_MS, PatchNotesApi } from './patch-notes-api.service';
import {
  PatchNotesMonth,
  PatchNotesMonthList,
  PatchNotesPage,
  PatchNotesPager,
  PatchNotesStatus,
  PatchNotesWindow,
} from './patch-notes-models';

/** "Loading patch notes…" shows only once the first page is slow in coming (PatchNotesUI.LOADING_DELAY_MS). */
export const LOADING_DELAY_MS = 300;

/** A chip of the filter: All (tag null) or a tag, with its count once the server sent it. */
export interface PatchNotesChip {
  readonly tag: string | null;
  readonly label: string;
  readonly count: number | null;
  readonly on: boolean;
}

interface MonthsEntry {
  readonly at: number;
  readonly months: readonly PatchNotesMonth[];
}

type MonthsState = { readonly kind: 'idle' | 'loading' } | { readonly kind: 'error'; readonly message: string };

/**
 * Server Info › Patch Notes (patchNotesUI.js PatchNotesUI, GempPatchNotes): a window of PATCH_NOTES_PAGE_SIZE updates,
 * newest first, that can start at any update, so every screen has a link (`info/patch-notes[/<slug>]`, `routeSlug`);
 * the tag filter (server side, so the paging and the months follow it; kept while the hall is open, not past a
 * reload); the month navigation; Newer / Older at the bottom. It lives with the hall's route, so coming back to the
 * sub-tab shows what was left (the legacy lastState and lastTag).
 *
 * The view scrolls the page's top into view when `scrollRequest` changes, marks the update `highlight` names, and keeps
 * the address on `routeSlug` (replacing it, as the legacy page did with replaceState).
 */
@Injectable()
export class PatchNotesService {
  private readonly api = inject(PatchNotesApi);
  private readonly timers = inject(Timers);

  private seq = 0;
  private monthsSeq = 0;
  private loadingTimer: TimerHandle | null = null;
  /** Said above the list with the next page shown (an old link, a link the filter hid). */
  private pendingNotices: string[] = [];

  /** The filter: null for All. */
  readonly tag = signal<string | null>(null);
  /** The page shown; null until the first arrives. */
  readonly page = signal<PatchNotesPage | null>(null);
  readonly status = signal<PatchNotesStatus>({ kind: 'done' });
  /** Notices above the list: "There is no update named …", "Showing all updates: …". */
  readonly notices = signal<readonly string[]>([]);
  /** The update a link pointed to, marked. */
  readonly highlight = signal<string | null>(null);
  /** Changes each time the view should bring the page's top (filter, months, then the update linked to) into view. */
  readonly scrollRequest = signal(0);
  /** The first update shown when it is not the newest: the address's slug (null: `info/patch-notes`). */
  readonly routeSlug = signal<string | null>(null);

  private readonly monthsCache = signal<ReadonlyMap<string, MonthsEntry>>(new Map());
  private readonly monthsState = signal<{ readonly tag: string; readonly state: MonthsState }>({
    tag: '',
    state: { kind: 'idle' },
  });

  /** The chips: All and each tag, with the counts of the last page (renderFilters). */
  readonly chips = computed<PatchNotesChip[]>(() => {
    const page = this.page();
    const tag = this.tag();
    const tags =
      page && page.filters.length > 0
        ? page.filters.map((filter) => ({ tag: filter.tag, count: filter.count }))
        : PATCH_NOTE_TAGS.map((name) => ({ tag: name, count: null as number | null }));
    return [
      { tag: null, label: ALL_FILTER, count: page ? page.all : null, on: tag === null },
      ...tags.map((entry) => ({ tag: entry.tag, label: entry.tag, count: entry.count, on: entry.tag === tag })),
    ];
  });

  /** The month of the first update shown (the navigation's title is its name). */
  readonly currentMonth = computed(() => {
    const first = this.page()?.notes[0];
    return first ? monthOfDate(first.date) : null;
  });
  readonly monthTitle = computed(() => monthLabel(this.currentMonth()));

  private readonly months = computed(() => this.monthsCache().get(this.tag() ?? '')?.months ?? null);

  /** "<" (an older month with updates) and ">" (a newer one) disabled (syncMonthNav). */
  readonly monthArrowsDisabled = computed(() => {
    const current = this.currentMonth();
    const months = this.months();
    if (months === null || current === null) {
      const off = this.monthsLoading() || current === null;
      return { older: off, newer: off };
    }
    const keys = months.map((month) => month.month);
    return {
      older: neighbourMonth(keys, current, -1) === null,
      newer: neighbourMonth(keys, current, 1) === null,
    };
  });

  /** The month dropdown (renderMonthList). */
  readonly monthList = computed<PatchNotesMonthList>(() => {
    const months = this.months();
    const tag = this.tag();
    if (months === null) {
      const { tag: stateTag, state } = this.monthsState();
      return state.kind === 'error' && stateTag === (tag ?? '')
        ? { kind: 'error', message: state.message }
        : { kind: 'loading' };
    }
    return {
      kind: 'ready',
      months: months.map((month) => month.month),
      current: this.currentMonth(),
      emptyText: tag ? noTaggedUpdates(tag) : NO_PATCH_NOTES,
    };
  });

  /** Newer / Older and "Updates 6–10 of 23" (renderPagers); null with no updates at all. */
  readonly pager = computed<PatchNotesPager | null>(() => {
    const page = this.page();
    if (!page?.total) {
      return null;
    }
    const last = page.start + page.notes.length;
    return {
      newer: page.start > 0,
      older: last < page.total,
      range: updatesRange(page.start + 1, last, page.total, page.notes.length),
    };
  });

  /** What the list says when it has no updates (render). */
  readonly emptyText = computed(() => {
    const page = this.page();
    if (!page || page.notes.length > 0) {
      return null;
    }
    const tag = this.tag();
    return page.total ? NO_OLDER_UPDATES : tag ? noTaggedUpdates(tag) : NO_PATCH_NOTES;
  });

  /**
   * The sub-tab is shown for `slug` (null: the newest). From a link (`link`: an address, a note's link, the router
   * after a reload) the updates from it are shown, it marked; opening the sub-tab itself shows what was left, or the
   * newest. The address the page itself put there (`routeSlug`) asks for nothing.
   */
  show(slug: string | null, link: boolean): Promise<void> {
    if (this.page() !== null && slug === this.routeSlug() && this.status().kind !== 'error') {
      return Promise.resolve();
    }
    if (link) {
      return this.go({ slug }, true, slug);
    }
    return this.go({ slug: this.page() === null ? null : this.routeSlug() }, false);
  }

  /** A chip: the newest updates with its tag (the active one too: back to the top). */
  setTag(tag: string | null): Promise<void> {
    this.tag.set(tag);
    return this.go({ slug: null }, false);
  }

  /** "Older updates ›" (+1) or "‹ Newer updates" (-1) from the window shown. */
  step(direction: 1 | -1): Promise<void> {
    const page = this.page();
    if (!page) {
      return Promise.resolve();
    }
    const start = page.start + direction * PATCH_NOTES_PAGE_SIZE;
    if (direction < 0 && start <= 0) {
      return this.go({ slug: null }, true);
    }
    if (Math.max(0, start) >= page.total) {
      return Promise.resolve();
    }
    return this.go({ start: Math.max(0, start) }, true);
  }

  /** "Try again" after a failure. */
  retry(): Promise<void> {
    return this.go({ slug: this.routeSlug() }, false);
  }

  /** "<" (delta -1): the next older month with updates; ">" (+1): the next newer one. */
  async jumpMonth(delta: 1 | -1): Promise<void> {
    const current = this.currentMonth();
    if (current === null) {
      return;
    }
    let months = this.months();
    if (months === null) {
      months = await this.loadMonths();
      if (months === null) {
        return;
      }
    }
    const target = neighbourMonth(
      months.map((month) => month.month),
      current,
      delta,
    );
    if (target !== null) {
      await this.showMonth(target);
    }
  }

  /** A month picked from the dropdown: the updates from its first (newest) one on. */
  showMonth(key: string): Promise<void> {
    const month = this.months()?.find((entry) => entry.month === key);
    return month ? this.go({ slug: month.first }, true) : Promise.resolve();
  }

  /** The dropdown opened: its months are fetched if not yet at hand. */
  openMonths(): void {
    if (this.months() === null && !this.monthsLoading()) {
      void this.loadMonths();
    }
  }

  /** The dropdown's Retry. */
  retryMonths(): void {
    void this.loadMonths();
  }

  private monthsLoading(): boolean {
    const { tag, state } = this.monthsState();
    return state.kind === 'loading' && tag === (this.tag() ?? '');
  }

  /** Fetches the filter's months (loadMonths); null when that failed or the filter changed meanwhile. */
  private async loadMonths(): Promise<readonly PatchNotesMonth[] | null> {
    const tag = this.tag() ?? '';
    const seq = ++this.monthsSeq;
    this.monthsState.set({ tag, state: { kind: 'loading' } });
    try {
      const months = await this.api.months(tag || null);
      this.monthsCache.update((cache) => new Map(cache).set(tag, { at: this.timers.now(), months }));
      if (seq === this.monthsSeq) {
        this.monthsState.set({ tag, state: { kind: 'idle' } });
      }
      return seq === this.monthsSeq ? months : null;
    } catch (error) {
      if (seq === this.monthsSeq) {
        this.monthsState.set({
          tag,
          state: { kind: 'error', message: monthsFailed(serverAnswered(statusOf(error))) },
        });
      }
      return null;
    }
  }

  /** Shows a window (go); `highlight` marks the update a link named. A later navigation wins. */
  private async go(span: PatchNotesWindow, scroll: boolean, highlight: string | null = null): Promise<void> {
    const seq = ++this.seq;
    this.timers.clear(this.loadingTimer);
    this.status.set({ kind: 'loading', shown: false });
    if (this.page() === null) {
      this.loadingTimer = this.timers.after(LOADING_DELAY_MS, () => {
        this.loadingTimer = null;
        if (seq === this.seq && this.status().kind === 'loading') {
          this.status.set({ kind: 'loading', shown: true });
        }
      });
    }
    let page: PatchNotesPage;
    try {
      page = await this.api.page(span, this.tag());
    } catch (error) {
      if (seq === this.seq) {
        await this.failed(statusOf(error), span, scroll, highlight);
      }
      return;
    }
    if (seq !== this.seq) {
      return;
    }
    this.render(page, scroll, highlight);
  }

  private async failed(status: number, span: PatchNotesWindow, scroll: boolean, highlight: string | null) {
    const slug = 'slug' in span ? span.slug : null;
    if (status === 404 && slug) {
      const tag = this.tag();
      if (tag) {
        // a link to an update the filter hides: show it under All
        this.pendingNotices.push(showingAllUpdates(tag));
        this.tag.set(null);
        await this.go(span, scroll, highlight);
        return;
      }
      // an old or mistyped link: say so, and show the newest updates instead
      this.pendingNotices.push(missingUpdate(slug));
      await this.go({ slug: null }, false);
      return;
    }
    this.timers.clear(this.loadingTimer);
    this.status.set({ kind: 'error', status });
  }

  private render(page: PatchNotesPage, scroll: boolean, highlight: string | null): void {
    this.timers.clear(this.loadingTimer);
    this.loadingTimer = null;
    this.page.set(page);
    this.status.set({ kind: 'done' });
    this.routeSlug.set(page.start > 0 ? (page.notes[0]?.slug ?? null) : null);
    this.notices.set(this.pendingNotices);
    this.pendingNotices = [];
    this.highlight.set(highlight !== null && page.notes.some((note) => note.slug === highlight) ? highlight : null);
    if (scroll) {
      this.scrollRequest.update((n) => n + 1);
    }
    const entry = this.monthsCache().get(this.tag() ?? '');
    const fresh = entry !== undefined && this.timers.now() - entry.at < PATCH_NOTES_CACHE_MS;
    if (!fresh && !this.monthsLoading()) {
      void this.loadMonths();
    }
  }
}

/** The HTTP status of a failure; 0 for no answer (or one that could not be read). */
function statusOf(error: unknown): number {
  const status = ApiError.from(error).status;
  return status > 0 ? status : 0;
}
