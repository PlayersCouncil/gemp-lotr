/**
 * How Help › PC Errata is browsed with the wheel, the keys, a swipe and the table's scrolling (pcErrataUi.js
 * bindWheel, onListWheel, bindSwipe, onKey, geometry, scrollToRow, rebaseFollow, followScroll): the arithmetic, with
 * the view measuring the rows and handing in the events' numbers.
 */

/** A notch (or a trackpad's ~40px) steps one card, at most one per 140ms; a pause of 250ms starts the count again. */
export const WHEEL_STEP_PX = 40;
export const WHEEL_STEP_MS = 140;
export const WHEEL_RESET_MS = 250;
/** The viewer's slide (PcErrataUI.DURATION, ms). */
export const SLIDE_MS = 240;
/** While the rows are skimmed, the viewer shows the selected card at most this often (ms). */
export const FOLLOW_INTERVAL_MS = 150;
/** A scroll the page started still owns the rows' scroll events this long after it arrives (ms). */
export const AUTO_SCROLL_SETTLE_MS = 200;
/** A swipe: 40px up or down, or 50px sideways. */
export const SWIPE_VERTICAL_PX = 40;
export const SWIPE_SIDEWAYS_PX = 50;
/** A move this far makes a press a drag (not a tap on the card). */
export const DRAG_PX = 4;
/** The card follows the finger by this much of its move. */
export const DRAG_FOLLOW = 0.35;
/** The reel nudges at either end of the list this long (ms). */
export const BUMP_MS = 300;

/** A wheel event's deltaY in pixels (lines are 16px; a page is `pagePx`). */
export function wheelPixels(deltaY: number, deltaMode: number, pagePx: number): number {
  return deltaY * (deltaMode === 1 ? 16 : deltaMode === 2 ? pagePx : 1);
}

/** The wheel over the viewer: it only ever steps the list, one card at a time (bindWheel). */
export class WheelStepper {
  private sum = 0;
  private last = 0;
  private lastStep = 0;

  /** The step (1 next, -1 previous) this much wheel makes at `now`, or 0. */
  step(dy: number, now: number): number {
    if (now - this.last > WHEEL_RESET_MS) {
      this.sum = 0;
    }
    this.last = now;
    this.sum += dy;
    if (Math.abs(this.sum) >= WHEEL_STEP_PX && now - this.lastStep >= WHEEL_STEP_MS) {
      const step = this.sum > 0 ? 1 : -1;
      this.sum = 0;
      this.lastStep = now;
      return step;
    }
    return 0;
  }
}

/**
 * The wheel over the rows once they can't scroll any further that way (or all fit): the selection keeps going at the
 * rows' pace, up to the first or last card (onListWheel).
 */
export class EdgeWheel {
  private sum = 0;
  private last = 0;

  /** How many rows to move the selection by (signed), for `dy` px of wheel at `now` with rows `rowHeight` apart. */
  rows(dy: number, now: number, rowHeight: number): number {
    if (now - this.last > WHEEL_RESET_MS || this.sum > 0 !== dy > 0) {
      this.sum = 0;
    }
    this.last = now;
    this.sum += dy;
    // (+ 0: never -0)
    const rows = Math.trunc(this.sum / rowHeight) + 0;
    if (rows) {
      this.sum -= rows * rowHeight;
    }
    return rows;
  }
}

/** Whether the rows can't scroll further the way the wheel turns (onListWheel's atEdge). */
export function atScrollEdge(dy: number, scrollTop: number, scrollHeight: number, clientHeight: number): boolean {
  const max = scrollHeight - clientHeight;
  return max <= 1 || (dy < 0 && scrollTop <= 0) || (dy > 0 && scrollTop >= max - 1);
}

/**
 * Where each row is in the scrolled rows (geometry): the tops and heights, and the height of the column headers that
 * stick over the first `headH` px.
 */
export interface RowGeometry {
  readonly tops: readonly number[];
  readonly heights: readonly number[];
  readonly headH: number;
}

export function rowCenter(geom: RowGeometry, index: number): number {
  return (geom.tops[index] ?? 0) + (geom.heights[index] ?? 0) / 2;
}

/** The row at y in the scrolled rows (rowAt). */
export function rowAt(geom: RowGeometry, y: number): number {
  let lo = 0;
  let hi = geom.tops.length - 1;
  while (lo < hi) {
    const mid = (lo + hi + 1) >> 1;
    if ((geom.tops[mid] ?? 0) <= y) {
      lo = mid;
    } else {
      hi = mid - 1;
    }
  }
  return lo;
}

/** The rows' spacing (rowHeight): 25 until there are two rows, never under 8. */
export function rowHeight(geom: RowGeometry | null): number {
  if (!geom || geom.tops.length < 2) {
    return 25;
  }
  const first = geom.tops[0] ?? 0;
  const last = geom.tops[geom.tops.length - 1] ?? 0;
  return Math.max(8, (last - first) / (geom.tops.length - 1));
}

/** The rows' box as it scrolls. */
export interface ScrollBox {
  readonly scrollTop: number;
  readonly scrollHeight: number;
  readonly clientHeight: number;
}

/**
 * Where to scroll the rows to bring the current row to the middle of their window (as far as they scroll), or null
 * when they are within 2px of it already (scrollToRow).
 */
export function rowScrollTarget(geom: RowGeometry, index: number, box: ScrollBox): number | null {
  const room = box.clientHeight - geom.headH;
  let target = rowCenter(geom, index) - geom.headH - room / 2;
  target = Math.max(0, Math.min(box.scrollHeight - box.clientHeight, Math.round(target)));
  return Math.abs(target - box.scrollTop) < 2 ? null : target;
}

/** A scroll closer than three windows away is smooth (and lasts up to 900ms), unless motion is reduced. */
export function isSmoothScroll(target: number, box: ScrollBox, reducedMotion: boolean): boolean {
  return !reducedMotion && Math.abs(target - box.scrollTop) < 3 * box.clientHeight;
}

/** Where the selection rides in the rows: `s` the scroll position, `y` the selected row's place in the rows. */
export interface FollowPoint {
  readonly s: number;
  readonly y: number;
}

/** From now on, the selection rides at the current row's middle, kept inside the rows' window (rebaseFollow). */
export function followPoint(geom: RowGeometry, index: number, box: ScrollBox): FollowPoint | null {
  if (index < 0 || index >= geom.tops.length) {
    return null;
  }
  const s = box.scrollTop;
  let y = rowCenter(geom, index);
  const low = s + geom.headH;
  const high = s + box.clientHeight - 1;
  if (high > low) {
    y = Math.max(low, Math.min(high, y));
  }
  return { s, y };
}

/**
 * The rows scrolled (wheel, swipe, scrollbar, keys): where the selection rides now, and on which row (followScroll).
 * Scrolling up it closes in on the first row, scrolling down on the last, so that the top of the rows selects the first
 * card and the bottom the last; in between it moves about as far as the rows do. Null when the rows did not move.
 */
export function followScroll(
  geom: RowGeometry,
  follow: FollowPoint,
  box: ScrollBox,
): { readonly follow: FollowPoint; readonly index: number } | null {
  const s = box.scrollTop;
  if (Math.abs(s - follow.s) < 0.5) {
    return null;
  }
  const max = Math.max(0, box.scrollHeight - box.clientHeight);
  const first = rowCenter(geom, 0);
  const end = rowCenter(geom, geom.tops.length - 1);
  let y: number;
  if (s <= 0) {
    y = first;
  } else if (s >= max - 1) {
    y = end;
  } else if (s < follow.s) {
    y = first + (follow.y - first) * (s / follow.s);
  } else {
    y = end - (end - follow.y) * ((max - s) / (max - follow.s));
  }
  return { follow: { s, y }, index: rowAt(geom, y) };
}

/**
 * A swipe on the viewer (bindSwipe's end): up or left is the next card (1), down or right the previous (-1), else 0.
 */
export function swipeStep(dx: number, dy: number): number {
  if (Math.abs(dy) >= SWIPE_VERTICAL_PX && Math.abs(dy) >= Math.abs(dx)) {
    return dy < 0 ? 1 : -1;
  }
  if (Math.abs(dx) >= SWIPE_SIDEWAYS_PX && Math.abs(dx) > Math.abs(dy)) {
    return dx < 0 ? 1 : -1;
  }
  return 0;
}

/** A swipe up that started on a card, by touch or pen, opens the card preview instead of stepping. */
export function isPreviewSwipe(touch: boolean, onCardSide: boolean, dx: number, dy: number): boolean {
  return touch && onCardSide && dy <= -SWIPE_VERTICAL_PX && Math.abs(dy) >= Math.abs(dx);
}

/** What a key does on the page (onKey). */
export type ErrataKeyAction = 'next' | 'previous' | 'flip' | 'first' | 'last' | 'close';

/**
 * The action of a key, or null for none. On a tab (where the arrow keys switch tabs) only j, k and o work; Escape only
 * closes the enlarged viewer.
 */
export function errataKeyAction(key: string, onTab: boolean, enlarged: boolean): ErrataKeyAction | null {
  if (onTab && !['j', 'J', 'k', 'K', 'o', 'O'].includes(key)) {
    return null;
  }
  switch (key) {
    case 'Escape':
      return enlarged ? 'close' : null;
    case 'ArrowDown':
    case 'j':
    case 'J':
      return 'next';
    case 'ArrowUp':
    case 'k':
    case 'K':
      return 'previous';
    case 'o':
    case 'O':
      return 'flip';
    case 'Home':
      return 'first';
    case 'End':
      return 'last';
    default:
      return null;
  }
}
