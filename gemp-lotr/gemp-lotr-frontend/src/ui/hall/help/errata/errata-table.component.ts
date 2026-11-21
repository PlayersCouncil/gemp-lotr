import { DOCUMENT } from '@angular/common';
import { afterNextRender, afterRenderEffect, Component, DestroyRef, ElementRef, inject, signal } from '@angular/core';
import {
  CARD_COLUMN,
  CARD_COLUMN_TITLE,
  CULTURE_COLUMN,
  ERRATA_LOADING,
  NEW_BADGE,
  NEW_BADGE_TITLE,
  PLAYTEST_BADGE,
  REVISIONS_COLUMN,
  REVISIONS_COLUMN_TITLE,
  rowTitle,
  SORT_ASCENDING_MARK,
  SORT_DESCENDING_MARK,
  SORT_NAMES,
  sortTitle,
  TABLE_LABEL,
  TITLE_COLUMN,
} from '../../../../core/hall/help/errata/errata-messages';
import { ErrataEntry, ErrataSortKey } from '../../../../core/hall/help/errata/errata-models';
import {
  AUTO_SCROLL_SETTLE_MS,
  atScrollEdge,
  EdgeWheel,
  FollowPoint,
  followPoint,
  followScroll,
  isSmoothScroll,
  RowGeometry,
  rowHeight,
  rowScrollTarget,
  wheelPixels,
} from '../../../../core/hall/help/errata/errata-skim';
import { PcErrataService } from '../../../../core/hall/help/errata/errata.service';
import { siteImage } from '../../../../core/platform/app-paths';
import { prefersReducedMotion } from './errata-motion';

interface Column {
  readonly key: ErrataSortKey;
  readonly label: string;
  readonly title: string;
  readonly column: 'num' | 'culture' | 'name' | 'rev';
}

const COLUMNS: readonly Column[] = [
  { key: 'num', label: CARD_COLUMN, title: CARD_COLUMN_TITLE, column: 'num' },
  { key: 'culture', label: CULTURE_COLUMN, title: sortTitle(SORT_NAMES.culture), column: 'culture' },
  { key: 'name', label: TITLE_COLUMN, title: sortTitle(SORT_NAMES.name), column: 'name' },
  { key: 'rev', label: REVISIONS_COLUMN, title: REVISIONS_COLUMN_TITLE, column: 'rev' },
];

/**
 * The table of errata'd cards (pcErrataUi.js renderTable, row, highlight, setSort, and the rows' scrolling: geometry,
 * scrollToRow, rebaseFollow, bindListScroll, followScroll, onListWheel). Every row of the filtered list is drawn; the
 * column headers stick and only the rows scroll. The selected row is the card in the viewer: stepping brings it to the
 * middle; scrolling the rows skims, the selection riding along with them.
 *
 * DOM contract: `gd-errata-table.gd-errata-table` (the scrolling box) holding `p.gd-errata-table__loading`, or
 * `p.gd-errata-table__error`, or `table.gd-errata-table__table` (`aria-label` "PC errata") with a
 * `th[data-column]` (`aria-sort` on the sorted one) > `button.gd-errata-table__sort` (`title`) per column, then a
 * `tr.gd-errata-table__row` per card (`data-index`, `data-id`, `aria-current` on the selected one) with
 * `td[data-column]`: num (`title`; `.gd-errata-table__coll`, `.gd-errata-table__badge[data-badge=new]`), culture
 * (`title`; `img.gd-errata-table__culture-icon`, or `.gd-errata-table__culture-name` when the icon fails), name
 * (`.gd-errata-table__name`, `.gd-errata-table__badge` "playtest"), rev; or a `td.gd-errata-table__empty` with the
 * status.
 */
@Component({
  selector: 'gd-errata-table',
  styleUrl: './errata-table.component.css',
  host: {
    class: 'gd-errata-table',
    '(scroll)': 'scrolled()',
    '(pointerdown)': 'handOn()',
    '(touchstart)': 'handOn()',
    '(keydown)': 'handOn()',
    '(wheel)': 'wheel($event)',
  },
  template: `
    @switch (errata.load().kind) {
      @case ('loading') {
        <p class="gd-errata-table__loading">{{ text.ERRATA_LOADING }}</p>
      }
      @case ('error') {
        <p class="gd-errata-table__error">{{ errorMessage() }}</p>
      }
      @default {
        <table class="gd-errata-table__table" [attr.aria-label]="text.TABLE_LABEL">
          <thead>
            <tr>
              @for (column of columns; track column.key) {
                <th scope="col" [attr.data-column]="column.column" [attr.aria-sort]="ariaSort(column.key)">
                  <button
                    type="button"
                    class="gd-errata-table__sort"
                    [title]="column.title"
                    (click)="errata.setSort(column.key)"
                  >
                    {{ column.label }}<span class="gd-errata-table__sort-mark">{{ sortMark(column.key) }}</span>
                  </button>
                </th>
              }
            </tr>
          </thead>
          <tbody>
            @for (entry of errata.list(); track entry.id; let i = $index) {
              <tr
                class="gd-errata-table__row"
                [attr.data-index]="i"
                [attr.data-id]="entry.id"
                [attr.aria-current]="i === errata.index() ? 'true' : null"
                (click)="errata.selectRow(i)"
              >
                <td data-column="num" [title]="rowTitle(entry)">
                  <div class="gd-errata-table__num">
                    <span class="gd-errata-table__coll">{{ entry.coll }}</span>
                    @if (entry.recent) {
                      <span class="gd-errata-table__badge" data-badge="new" [title]="text.NEW_BADGE_TITLE">{{
                        text.NEW_BADGE
                      }}</span>
                    }
                  </div>
                </td>
                <td data-column="culture" [attr.title]="entry.culture">
                  @if (entry.cultureCode; as code) {
                    @if (failedIcons().has(code)) {
                      <span class="gd-errata-table__culture-name">{{ entry.culture ?? '' }}</span>
                    } @else {
                      <img
                        class="gd-errata-table__culture-icon"
                        [src]="cultureIcon(code)"
                        [alt]="entry.culture ?? code"
                        (error)="iconFailed(code)"
                      />
                    }
                  }
                </td>
                <td data-column="name">
                  <span class="gd-errata-table__name">{{ entry.name }}</span>
                  @if (entry.kind === 'playtest') {
                    <span class="gd-errata-table__badge">{{ text.PLAYTEST_BADGE }}</span>
                  }
                </td>
                <td data-column="rev">{{ entry.revision || '' }}</td>
              </tr>
            } @empty {
              <tr>
                <td class="gd-errata-table__empty" colspan="4">{{ errata.status().text }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    }
  `,
})
export class ErrataTableComponent {
  protected readonly errata = inject(PcErrataService);
  private readonly document = inject(DOCUMENT);
  private readonly box = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  protected readonly text = {
    ERRATA_LOADING,
    NEW_BADGE,
    NEW_BADGE_TITLE,
    PLAYTEST_BADGE,
    TABLE_LABEL,
  };
  protected readonly columns = COLUMNS;
  protected readonly failedIcons = signal<ReadonlySet<string>>(new Set());

  private geom: RowGeometry | null = null;
  private follow: FollowPoint | null = null;
  /** A scroll this page started (scrollToRow): its target, and until when it owns the rows' scroll events. */
  private autoScroll: { target: number; until: number } | null = null;
  private readonly edgeWheel = new EdgeWheel();

  constructor() {
    const destroyRef = inject(DestroyRef);
    // after each selection that didn't come from skimming: the current row to the middle (unless a click chose it),
    // and from there the selection rides with the rows
    afterRenderEffect(() => {
      const sync = this.errata.rowSync();
      this.errata.list();
      this.geom = null;
      if (sync?.scroll) {
        this.scrollToRow();
      }
      this.rebaseFollow();
    });
    afterNextRender(() => {
      const Observer = this.document.defaultView?.ResizeObserver;
      if (!Observer) return;
      const observer = new Observer(() => {
        this.geom = null;
        this.rebaseFollow();
      });
      observer.observe(this.box);
      destroyRef.onDestroy(() => observer.disconnect());
    });
  }

  protected errorMessage(): string {
    const load = this.errata.load();
    return load.kind === 'error' ? load.message : '';
  }

  protected rowTitle(entry: ErrataEntry): string {
    return rowTitle(entry.set, entry.setName, entry.cardNum);
  }

  protected cultureIcon(code: string): string {
    return siteImage(`cultures/${code}.png`);
  }

  protected iconFailed(code: string): void {
    this.failedIcons.update((failed) => new Set(failed).add(code));
  }

  protected sortMark(key: ErrataSortKey): string {
    const sort = this.errata.sort();
    return sort.key === key ? (sort.dir > 0 ? SORT_ASCENDING_MARK : SORT_DESCENDING_MARK) : '';
  }

  protected ariaSort(key: ErrataSortKey): string | null {
    const sort = this.errata.sort();
    return sort.key === key ? (sort.dir > 0 ? 'ascending' : 'descending') : null;
  }

  private now(): number {
    return this.document.defaultView?.performance.now() ?? Date.now();
  }

  private reducedMotion(): boolean {
    return prefersReducedMotion(this.document);
  }

  /** Each row's top and height in the scrolled rows; null while they are not laid out (geometry). */
  private geometry(): RowGeometry | null {
    if (this.geom) {
      return this.geom;
    }
    const rows = Array.from(this.box.querySelectorAll<HTMLElement>('tr.gd-errata-table__row'));
    if (rows.length === 0) {
      return null;
    }
    const base = this.box.getBoundingClientRect().top + this.box.clientTop - this.box.scrollTop;
    const tops: number[] = [];
    const heights: number[] = [];
    let laidOut = false;
    for (const row of rows) {
      const rect = row.getBoundingClientRect();
      tops.push(rect.top - base);
      heights.push(rect.height);
      laidOut ||= rect.height > 0;
    }
    if (!laidOut) {
      return null;
    }
    const head = this.box.querySelector('thead');
    this.geom = { tops, heights, headH: head ? head.getBoundingClientRect().height : 0 };
    return this.geom;
  }

  /** Brings the current row to the middle of the rows' window (scrollToRow). */
  private scrollToRow(): void {
    const geom = this.geometry();
    const index = this.errata.index();
    if (!geom || index < 0 || this.box.offsetParent === null) {
      return;
    }
    const target = rowScrollTarget(geom, index, this.box);
    if (target === null) {
      return;
    }
    const smooth = isSmoothScroll(target, this.box, this.reducedMotion());
    this.autoScroll = { target, until: this.now() + (smooth ? 900 : 200) };
    if (smooth && typeof this.box.scrollTo === 'function') {
      this.box.scrollTo({ top: target, behavior: 'smooth' });
    } else {
      this.box.scrollTop = target;
    }
  }

  private rebaseFollow(): void {
    const geom = this.errata.index() >= 0 ? this.geometry() : null;
    this.follow = geom ? followPoint(geom, this.errata.index(), this.box) : null;
  }

  /** A hand on the rows takes over from a scroll this page started. */
  protected handOn(): void {
    this.autoScroll = null;
  }

  /** The rows scrolled: the selection moves along with them (followScroll). */
  protected scrolled(): void {
    if (this.errata.load().kind !== 'ready' || this.errata.index() < 0) {
      return;
    }
    const auto = this.autoScroll;
    if (auto) {
      // ours: it ends a moment after arriving (a smooth scroll can report a pixel short first), or when its time is up
      const now = this.now();
      if (now > auto.until) {
        this.autoScroll = null;
      } else if (Math.abs(this.box.scrollTop - auto.target) <= 1) {
        auto.until = Math.min(auto.until, now + AUTO_SCROLL_SETTLE_MS);
      }
      this.rebaseFollow();
      return;
    }
    const geom = this.geometry();
    if (!geom) {
      return;
    }
    if (!this.follow) {
      this.rebaseFollow();
      return;
    }
    const next = followScroll(geom, this.follow, this.box);
    if (!next) {
      return;
    }
    this.follow = next.follow;
    this.errata.follow(next.index);
  }

  /** The wheel at the rows' end keeps the selection going at their pace (onListWheel). */
  protected wheel(event: WheelEvent): void {
    this.autoScroll = null;
    if (event.ctrlKey || this.errata.load().kind !== 'ready' || this.errata.index() < 0) {
      return;
    }
    const dy = wheelPixels(event.deltaY, event.deltaMode, this.box.clientHeight);
    if (!dy || !atScrollEdge(dy, this.box.scrollTop, this.box.scrollHeight, this.box.clientHeight)) {
      return;
    }
    event.preventDefault();
    const rows = this.edgeWheel.rows(dy, this.now(), rowHeight(this.geometry()));
    if (!rows) {
      return;
    }
    const last = this.errata.list().length - 1;
    const next = Math.max(0, Math.min(last, this.errata.index() + rows));
    if (next === this.errata.index()) {
      this.errata.nudge(rows);
      return;
    }
    this.errata.follow(next);
    this.rebaseFollow();
  }
}
