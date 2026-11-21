import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiError } from '../../../api/api-error';
import { CARD_IMAGE_TABLES } from '../../../cards/card-data';
import { ImagePreloader } from '../../../platform/image-preloader.service';
import { TimerHandle, Timers } from '../../../platform/timers';
import { HallRoute } from '../../hall-links';
import { loadErrorMessage } from '../../hall-load-error';
import { ErrataApi } from './errata-api.service';
import { errataReadout, ErrataReadout } from './errata-diff';
import {
  DEFAULT_SORT,
  EMPTY_FILTERS,
  errataAddress,
  errataStatus,
  filterEntries,
  filtersOfParams,
  findCard,
  formatOptions,
  indexOfCard,
  isErrataLink,
  matchesFilters,
  setOptions,
  sortEntries,
  sortOfParams,
  viewChips,
  withChosen,
} from './errata-filter';
import { errataImages, ErrataImages, errataPreview, ErrataPreview } from './errata-images';
import { counterText, ERRATA_SUBJECT, errataSummary, unknownCard } from './errata-messages';
import {
  ErrataCatalog,
  ErrataChip,
  ErrataEntry,
  ErrataFilterKey,
  ErrataFilters,
  ErrataLoad,
  ErrataOption,
  ErrataSort,
  ErrataSortKey,
} from './errata-models';
import { FOLLOW_INTERVAL_MS, SLIDE_MS } from './errata-skim';

/** The search box is applied this long after the last key (renderFilters' timer). */
export const SEARCH_DELAY_MS = 150;

/** The card in the viewer: `direction` is its slide (1 from below, -1 from above, 0 none); `seq` tells shows apart. */
export interface ErrataShown {
  readonly entry: ErrataEntry;
  readonly direction: number;
  readonly seq: number;
}

/** The view keeps the rows in step after a selection: scrolls the current row into the middle (`scroll`) or not. */
export interface ErrataRowSync {
  readonly scroll: boolean;
  readonly seq: number;
}

/** The reel nudges at an end of the list (`direction` 1 past the last card, -1 before the first). */
export interface ErrataBump {
  readonly direction: number;
  readonly seq: number;
}

/** How a card is selected (PcErrataUI.select's options). */
export interface SelectOptions {
  /** The viewer's slide; left out: from the move (down the list slides up). 0: none, and the card is shown again. */
  readonly direction?: number;
  /** false: leave the rows where they are (a click on a row, skimming). */
  readonly scroll?: boolean;
  /** The rows are being skimmed: the viewer catches up at most every FOLLOW_INTERVAL_MS. */
  readonly follow?: boolean;
}

/** A link to the page: the card it names (or null) and its query parameters. */
export interface ErrataLink {
  readonly card: string | null;
  readonly params: Readonly<Record<string, string | undefined>>;
}

function sameRoute(a: HallRoute, b: HallRoute): boolean {
  const keys = Object.keys(a.queryParams);
  return (
    a.path === b.path &&
    keys.length === Object.keys(b.queryParams).length &&
    keys.every((key) => a.queryParams[key] === b.queryParams[key])
  );
}

/**
 * Help › PC Errata (pcErrataUi.js PcErrataUI and GempErrataHelp): the errata (fetched once, as PcErrataUI.cache kept
 * them), the filters and sort, the card selected in the table and the one in the viewer, and the address of what is on
 * show. It lives as long as the page (its route's providers), so a later visit to the sub-tab finds the filters, sort
 * and card it left (PcErrataUI.lastState). The view measures and scrolls the rows (errata-skim.ts), animates, and
 * writes `address()` into the address bar (replaceUrl) once `touched()`.
 */
@Injectable()
export class PcErrataService {
  private readonly api = inject(ErrataApi);
  private readonly tables = inject(CARD_IMAGE_TABLES);
  private readonly timers = inject(Timers);
  private readonly preloader = inject(ImagePreloader);

  private loadSeq = 0;
  private searchTimer: TimerHandle | null = null;
  private followTimer: TimerHandle | null = null;
  private lastFollowShow = 0;
  private lastSlide = 0;
  private showSeq = 0;
  private syncSeq = 0;
  private bumpSeq = 0;
  private shownIndex: number | null = null;
  private readonly preloaded = new Set<string>();
  /** A link that came before the errata (GempErrataHelp.pending); a bare visit is `null`. */
  private pending: ErrataLink | null = null;

  readonly load = signal<ErrataLoad>({ kind: 'loading' });
  readonly catalog = computed<ErrataCatalog | null>(() => {
    const load = this.load();
    return load.kind === 'ready' ? load.catalog : null;
  });
  readonly entries = computed<readonly ErrataEntry[]>(() => this.catalog()?.entries ?? []);

  readonly filters = signal<ErrataFilters>(EMPTY_FILTERS);
  /** The search box's text, as typed. */
  readonly query = signal('');
  readonly sort = signal<ErrataSort>(DEFAULT_SORT);
  /** The table's rows: the filtered cards in the sort's order. */
  readonly list = computed(() => sortEntries(filterEntries(this.entries(), this.filters()), this.sort()));

  private readonly selectedId = signal<string | null>(null);
  /** The row selected (highlighted, counted), or -1. */
  readonly index = computed(() => {
    const id = this.selectedId();
    return id === null ? -1 : this.list().findIndex((entry) => entry.id === id);
  });
  readonly selected = computed<ErrataEntry | null>(() => this.list()[this.index()] ?? null);
  /** The card in the viewer, or null for "nothing matches" (it lags the selection while the rows are skimmed). */
  readonly shown = signal<ErrataShown | null>(null);
  /** The viewer shows the original rather than the PC version. */
  readonly showOriginal = signal(false);
  /** Both versions side by side over the hall. */
  readonly enlarged = signal(false);
  /** The page was used, or opened from a link: from then on the address follows what is on show (updateHash). */
  readonly touched = signal(false);
  /** Put before the status: a link named a card without errata. */
  private readonly notice = signal('');
  readonly rowSync = signal<ErrataRowSync | null>(null);
  readonly bump = signal<ErrataBump | null>(null);
  /** The viewer takes the focus (after a link, or when the errata arrive), so the keys browse at once. */
  readonly focusRequest = signal(0);

  readonly summary = computed(() => {
    const catalog = this.catalog();
    if (!catalog) {
      return '';
    }
    const counts = catalog.counts;
    return errataSummary(catalog.entries.length, counts.errata + counts.playtest, counts.revision);
  });
  readonly chips = computed<readonly ErrataChip[]>(() => viewChips(this.entries()));
  readonly setOptions = computed<readonly ErrataOption[]>(() =>
    withChosen(setOptions(this.entries()), 'set', this.filters().set, this.entries(), this.catalog()?.formats ?? []),
  );
  readonly formatOptions = computed<readonly ErrataOption[]>(() => {
    const formats = this.catalog()?.formats ?? [];
    return withChosen(formatOptions(formats), 'format', this.filters().format, this.entries(), formats);
  });
  /** The status beside the filters (and in the empty table and viewer); `empty` when nothing is listed. */
  readonly status = computed(() => {
    const status = errataStatus(
      this.list().length,
      this.entries().length,
      this.filters(),
      this.catalog()?.formats ?? [],
    );
    return { text: this.notice() + status.text, empty: status.empty };
  });
  readonly counter = computed(() => counterText(this.index(), this.list().length));
  readonly canPrevious = computed(() => this.index() > 0);
  readonly canNext = computed(() => this.index() >= 0 && this.index() < this.list().length - 1);
  readonly readout = computed<ErrataReadout | null>(() => {
    const shown = this.shown();
    return shown ? errataReadout(shown.entry) : null;
  });
  readonly images = computed<ErrataImages | null>(() => {
    const shown = this.shown();
    return shown ? errataImages(this.tables, shown.entry) : null;
  });
  /** Where the page is: the card on show and the filters in use (PcErrata.buildHash). */
  readonly address = computed<HallRoute>(() => errataAddress(this.shown()?.entry.id ?? null, this.filters()));

  /** The sub-tab is shown: the errata are fetched unless they are in already (PcErrataUI.load). */
  async start(): Promise<void> {
    if (this.load().kind === 'ready') {
      return;
    }
    const seq = ++this.loadSeq;
    this.load.set({ kind: 'loading' });
    let catalog: ErrataCatalog;
    try {
      catalog = await firstValueFrom(this.api.errata());
    } catch (error) {
      if (seq === this.loadSeq) {
        this.load.set({ kind: 'error', message: loadErrorMessage(ApiError.from(error).status, ERRATA_SUBJECT) });
      }
      return;
    }
    if (seq !== this.loadSeq) {
      return;
    }
    this.load.set({ kind: 'ready', catalog });
    const link = this.pending;
    this.pending = null;
    if (link) {
      this.applyLink(link);
    } else {
      this.refresh(this.selectedId());
    }
    this.focusRequest.update((n) => n + 1);
  }

  /**
   * The page's route: `help/errata` (the sub-tab, which comes back to the filters, sort and card it left), or a link
   * with a card (`help/errata/<id>`) or filters (GempErrataHelp.open: the address then follows the card on show). The
   * address this page wrote itself changes nothing.
   */
  openRoute(link: ErrataLink): void {
    const route = errataAddress(link.card, filtersOfParams(link.params));
    if (this.load().kind === 'ready' && this.touched() && sameRoute(route, this.address()) && !link.params['sort']) {
      return;
    }
    if (!isErrataLink(link.card, link.params)) {
      // a new visit to the sub-tab (PcErrataUI.mount): its last state, not enlarged, the address left as it is
      this.pending = null;
      this.touched.set(false);
      this.enlarged.set(false);
      if (this.load().kind === 'ready') {
        this.refresh(this.selectedId());
        this.focusRequest.update((n) => n + 1);
      }
      return;
    }
    this.touched.set(true);
    if (this.load().kind !== 'ready') {
      this.pending = link;
      return;
    }
    this.applyLink(link);
    this.focusRequest.update((n) => n + 1);
  }

  /** A link's filters (and nothing else), then its card (applyRoute). */
  private applyLink(link: ErrataLink): void {
    const sort = sortOfParams(link.params);
    if (sort) {
      this.sort.set(sort);
    }
    let filters = filtersOfParams(link.params);
    this.filters.set(filters);
    if (link.card === null) {
      this.refresh();
      return;
    }
    // an original with two errata (PC and playtest): the one the link's filters list, else the first (the legacy page
    // always took the first, and dropped the filters of a link to the other)
    const known =
      this.entries().find(
        (entry) => (entry.id === link.card || entry.base === link.card) && matchesFilters(entry, filters),
      ) ?? findCard(this.entries(), link.card);
    if (!known) {
      this.refresh();
      this.notice.set(unknownCard(link.card));
      return;
    }
    if (!matchesFilters(known, filters)) {
      // the link's filters leave its card out: the card wins
      filters = EMPTY_FILTERS;
      this.filters.set(filters);
    }
    this.refresh(known.id);
  }

  // ---- filters and sort ----

  /** The search box changed: applied a moment after the last key. */
  setQuery(value: string): void {
    this.query.set(value);
    this.timers.clear(this.searchTimer);
    this.searchTimer = this.timers.after(SEARCH_DELAY_MS, () => {
      this.searchTimer = null;
      this.touched.set(true);
      this.filters.update((filters) => ({ ...filters, q: value }));
      this.refresh();
    });
  }

  /** The Set or Format list, or a View chip (`show`). */
  setFilter(key: Exclude<ErrataFilterKey, 'q'>, value: string): void {
    this.touched.set(true);
    this.filters.update((filters) => ({ ...filters, [key]: value }));
    this.refresh();
  }

  /** A column header: sorts by it, or turns the sort round when it already does. */
  setSort(key: ErrataSortKey): void {
    this.touched.set(true);
    this.sort.update((sort) => (sort.key === key ? { key, dir: sort.dir === 1 ? -1 : 1 } : { key, dir: 1 }));
    this.refresh();
  }

  /** Re-filters and re-sorts; keeps the current card when it is still listed (or shows keepId's), else the first. */
  private refresh(keepId: string | null = null): void {
    const current = keepId ?? this.selectedId();
    this.notice.set('');
    this.query.set(this.filters().q);
    const list = this.list();
    const index = current === null ? -1 : indexOfCard(list, current);
    this.selectedId.set(null);
    if (list.length === 0) {
      this.timers.clear(this.followTimer);
      this.followTimer = null;
      this.shown.set(null);
      this.shownIndex = null;
      this.rowSync.set({ scroll: false, seq: ++this.syncSeq });
      return;
    }
    this.select(index >= 0 ? index : 0, { direction: 0 });
  }

  // ---- selection ----

  /** Selects the row at `index` (clamped) and shows its card (PcErrataUI.select). */
  select(index: number, options: SelectOptions = {}): void {
    const list = this.list();
    const at = Math.max(0, Math.min(list.length - 1, index));
    const entry = list[at];
    if (entry === undefined) {
      return;
    }
    const previous = this.index();
    let direction = options.direction ?? (at > previous ? 1 : at < previous ? -1 : 0);
    if (previous < 0) {
      direction = 0;
    }
    this.selectedId.set(entry.id);
    if (options.follow) {
      this.scheduleShow();
    } else {
      this.timers.clear(this.followTimer);
      this.followTimer = null;
      if (this.shown()?.entry.id !== entry.id || options.direction === 0) {
        this.showOriginal.set(false);
        this.show(entry, direction, at);
      }
      this.preloadAround(at);
      this.rowSync.set({ scroll: options.scroll !== false, seq: ++this.syncSeq });
    }
  }

  /** The row clicked in the table. */
  selectRow(index: number): void {
    this.touched.set(true);
    this.select(index, { scroll: false });
  }

  /** The rows were skimmed (scrolled, or the wheel at their end) onto `index`: the viewer catches up. */
  follow(index: number): void {
    if (index === this.index()) {
      return;
    }
    this.touched.set(true);
    this.select(index, { scroll: false, follow: true });
  }

  /** One card on or back (a key, the wheel, a swipe, the arrows); at an end the reel nudges. False when it didn't move. */
  step(delta: number): boolean {
    const index = this.index();
    if (index < 0 || this.list().length === 0) {
      return false;
    }
    this.touched.set(true);
    const next = index + delta;
    if (next < 0 || next >= this.list().length) {
      this.nudge(delta);
      return false;
    }
    this.select(next, { direction: delta > 0 ? 1 : -1 });
    return true;
  }

  /** At either end of the list the reel nudges and settles back (bump). */
  nudge(delta: number): void {
    this.bump.set({ direction: delta > 0 ? 1 : -1, seq: ++this.bumpSeq });
  }

  /** Home and End. */
  first(): void {
    if (this.list().length) {
      this.touched.set(true);
      this.select(0);
    }
  }

  last(): void {
    if (this.list().length) {
      this.touched.set(true);
      this.select(this.list().length - 1);
    }
  }

  /**
   * Skimming: the row highlight has moved at once; the viewer shows the selected card at most every
   * FOLLOW_INTERVAL_MS (and once the rows stop), sliding only when the last slide is over (scheduleShow).
   */
  private scheduleShow(): void {
    if (this.followTimer !== null) {
      return;
    }
    const wait = Math.max(0, this.lastFollowShow + FOLLOW_INTERVAL_MS - this.timers.now());
    this.followTimer = this.timers.after(Math.max(wait, 30), () => {
      this.followTimer = null;
      const entry = this.selected();
      if (!entry) {
        return;
      }
      this.lastFollowShow = this.timers.now();
      const at = this.index();
      if (this.shown()?.entry.id !== entry.id) {
        let direction = this.shownIndex === null ? 0 : at > this.shownIndex ? 1 : -1;
        if (this.timers.now() - this.lastSlide < SLIDE_MS) {
          direction = 0;
        }
        this.showOriginal.set(false);
        this.show(entry, direction, at);
      }
      this.preloadAround(at);
    });
  }

  private show(entry: ErrataEntry, direction: number, index: number): void {
    this.shownIndex = index;
    if (direction) {
      this.lastSlide = this.timers.now();
    }
    this.shown.set({ entry, direction, seq: ++this.showSeq });
  }

  /** The images of the cards around the current one, so stepping shows them at once (preloadAround). */
  private preloadAround(index: number): void {
    const list = this.list();
    const urls: string[] = [];
    for (const delta of [1, -1, 2, -2]) {
      const entry = list[index + delta];
      if (!entry) {
        continue;
      }
      const images = errataImages(this.tables, entry);
      for (const url of [images.after, images.before]) {
        if (url && !this.preloaded.has(url)) {
          this.preloaded.add(url);
          urls.push(url);
        }
      }
    }
    if (urls.length) {
      void this.preloader.load(urls);
    }
  }

  // ---- the viewer ----

  /** Original | PC errata (the switch, a click on the card, O). Not while enlarged, which shows both. */
  setShowOriginal(original: boolean): void {
    if (original === this.showOriginal() || this.enlarged()) {
      return;
    }
    this.showOriginal.set(original);
  }

  toggleOriginal(): void {
    this.setShowOriginal(!this.showOriginal());
  }

  /** Enlarge / Close: the card is shown again, as the PC version, in the other layout. */
  setEnlarged(on: boolean): void {
    this.enlarged.set(on);
    const entry = this.selected();
    if (entry) {
      this.showOriginal.set(false);
      this.show(entry, 0, this.index());
    }
  }

  /** What the card preview shows for one side of the card on show, or null when it has no image. */
  preview(side: 'before' | 'after'): ErrataPreview | null {
    const shown = this.shown();
    return shown ? errataPreview(this.tables, shown.entry, side) : null;
  }
}
