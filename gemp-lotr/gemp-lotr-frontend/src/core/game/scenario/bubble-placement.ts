/**
 * Where a scenario's message bubble goes on the screen, and how it is joined to what it is about. Pure geometry: the
 * view measures the things (the bubble, what it points at, what it must not cover, the cards about) and draws the
 * result.
 */
import { MessageSide } from './scenario-events';

export interface Rect {
  left: number;
  top: number;
  width: number;
  height: number;
}

/** The distance a bubble keeps from what it points at when nobody says: hundredths of the window's shorter side. */
export const STOCK_GAP = '1.8';
/** The bubble stays this far inside the edges of the view (px). */
const MARGIN = 8;
/** A bubble slid along a side still overlaps the thing's end by this much, for the pointer to point from (px). */
const LAP = 30;
/** The arrow keeps this far from the bubble's corners, and is this long (px). */
const ARROW_INSET = 14;

export function overlap(a: Rect, b: Rect): number {
  const x = Math.min(a.left + a.width, b.left + b.width) - Math.max(a.left, b.left);
  const y = Math.min(a.top + a.height, b.top + b.height) - Math.max(a.top, b.top);
  return x > 0 && y > 0 ? x * y : 0;
}

/** The rectangle around several; of one, that one itself; of none, null. */
export function around(rects: readonly Rect[]): Rect | null {
  const [first] = rects;
  if (!first) return null;
  if (rects.length === 1) return first;
  const left = Math.min(...rects.map((rect) => rect.left));
  const top = Math.min(...rects.map((rect) => rect.top));
  const right = Math.max(...rects.map((rect) => rect.left + rect.width));
  const bottom = Math.max(...rects.map((rect) => rect.top + rect.height));
  return { left, top, width: right - left, height: bottom - top };
}

/**
 * A gap as written, in pixels: a plain number is hundredths of the view's shorter side, which keeps its proportion
 * to the cards whatever the screen; "20px" is pixels. The first of `written` that says anything counts (the
 * message's, then the scenario's), else the stock distance.
 */
export function gapPixels(written: readonly (string | null)[], viewWidth: number, viewHeight: number): number {
  const said = written.find((gap) => gap !== null && gap !== '' && !Number.isNaN(Number.parseFloat(gap))) ?? STOCK_GAP;
  const amount = Number.parseFloat(said);
  return said.toLowerCase().includes('px') ? amount : (amount * Math.min(viewWidth, viewHeight)) / 100;
}

export type PlacedSide = 'above' | 'below' | 'left' | 'right' | 'none';

export interface Placement {
  left: number;
  top: number;
  /** The side of what it points at that the bubble is on; `none` when it points at nothing. */
  side: PlacedSide;
}

export interface PlacementInput {
  /** The bubble's size. */
  width: number;
  height: number;
  viewWidth: number;
  viewHeight: number;
  /** What the bubble points at (each on the screen); none: it goes near the middle. */
  anchors: readonly Rect[];
  gap: number;
  /** The side the message names, or auto. */
  side: MessageSide;
  /** What the bubble must not cover (what it points at, what is outlined or to be clicked, the answering buttons). */
  keepClear: readonly Rect[];
  /** Cards it would be a pity to cover, though not a mistake. */
  cards: readonly Rect[];
}

function price(spot: Rect, keepClear: readonly Rect[], cards: readonly Rect[]): number {
  let cost = 0;
  for (const rect of keepClear) cost += overlap(spot, rect) / 50;
  for (const rect of cards) cost += overlap(spot, rect) / 400;
  return cost;
}

/** Where the bubble goes. */
export function placeBubble(input: PlacementInput): Placement {
  const { width, height, viewWidth, viewHeight, gap, keepClear, cards } = input;
  const anchor = around(input.anchors);

  if (!anchor) {
    // About nothing in particular: as near the middle of the screen as leaves clear what must be
    const wantLeft = (viewWidth - width) / 2;
    const wantTop = Math.max(MARGIN, viewHeight * 0.38 - height / 2);
    const STEPS = 6;
    let free: (Placement & { cost: number }) | null = null;
    for (let column = 0; column <= STEPS; column++) {
      for (let row = 0; row <= STEPS; row++) {
        const left = MARGIN + ((viewWidth - width - MARGIN * 2) * column) / STEPS;
        const top = MARGIN + ((viewHeight - height - MARGIN * 2) * row) / STEPS;
        const cost =
          (Math.abs(left - wantLeft) + Math.abs(top - wantTop)) / 20 +
          price({ left, top, width, height }, keepClear, cards);
        if (free === null || cost < free.cost) free = { side: 'none', left, top, cost };
      }
    }
    // Where it would go by itself, if that is as good as anywhere
    const middle = price({ left: wantLeft, top: wantTop, width, height }, keepClear, cards);
    return free === null || middle <= free.cost
      ? { side: 'none', left: wantLeft, top: wantTop }
      : { side: free.side, left: free.left, top: free.top };
  }

  // A side the scenario names is the side it goes on, level with the middle of the thing, whatever cards that covers:
  // the author has seen what it covers. Only what it must not cover still slides it along that side. Left to itself
  // it goes on whichever side costs least, and, where they cost much the same, above first: a bubble over what it
  // points at reads before the thing, and keeps clear of the hand along the bottom.
  const named = input.side !== 'auto';
  const order: readonly Exclude<PlacedSide, 'none'>[] =
    input.side !== 'auto' ? [input.side] : ['above', 'right', 'left', 'below'];
  // Beside all of what it points at taken together; or, pointing at several things, beside any one of them
  const beside = input.anchors.length > 1 ? [anchor, ...input.anchors] : [anchor];
  let best: (Placement & { cost: number }) | null = null;
  beside.forEach((box, which) => {
    const centreX = box.left + box.width / 2;
    const centreY = box.top + box.height / 2;
    const right = box.left + box.width;
    const bottom = box.top + box.height;
    // On each side in turn: level with its middle if there is room, otherwise slid along towards one end or the
    // other, as far as still leaves the pointer something to point from
    const slideDown = [centreY - height / 2, box.top, bottom - height, bottom - LAP, box.top + LAP - height];
    const slideAcross = [centreX - width / 2, box.left, right - width, right - LAP, box.left + LAP - width];
    order.forEach((side, turn) => {
      for (let slide = 0; slide < 5; slide++) {
        const down = slideDown[slide] ?? 0;
        const across = slideAcross[slide] ?? 0;
        const candidate =
          side === 'right'
            ? { left: right + gap, top: down }
            : side === 'left'
              ? { left: box.left - gap - width, top: down }
              : side === 'below'
                ? { left: across, top: bottom + gap }
                : { left: across, top: box.top - gap - height };
        const left = Math.max(MARGIN, Math.min(candidate.left, viewWidth - width - MARGIN));
        const top = Math.max(MARGIN, Math.min(candidate.top, viewHeight - height - MARGIN));
        // How far it had to be pushed back onto the screen, and how much it covers of what it must not; earlier
        // sides win ties, and a little more, so the bubble doesn't hop about; level with the middle beats slid
        // along; and beside the lot beats beside one of them
        const cost =
          Math.abs(left - candidate.left) +
          Math.abs(top - candidate.top) +
          price({ left, top, width, height }, keepClear, named ? [] : cards) +
          turn * 4 +
          (slide === 0 ? 0 : 3 + slide) +
          (which === 0 ? 0 : 2);
        if (best === null || cost < best.cost) best = { side, left, top, cost };
      }
    });
  });
  const placed = best as (Placement & { cost: number }) | null;
  return placed
    ? { side: placed.side, left: placed.left, top: placed.top }
    : { side: 'none', left: MARGIN, top: MARGIN };
}

/** How the bubble is joined to what it is about. */
export type Pointer =
  /** Nothing to join (it points at nothing, or lies on what it is about). */
  | { kind: 'none' }
  /** A small arrow on the bubble's edge: which edge, and how far along it (px from the bubble's left or top). */
  | { kind: 'arrow'; edge: 'left' | 'right' | 'top' | 'bottom'; along: number }
  /** A thin line to each thing, from the bubble's edge to the thing's. */
  | { kind: 'lines'; lines: readonly { x1: number; y1: number; x2: number; y2: number }[] };

/** Where the line from the middle of one rectangle to the middle of another leaves the first. */
export function edgePoint(rect: Rect, towards: Rect): { x: number; y: number } {
  const cx = rect.left + rect.width / 2;
  const cy = rect.top + rect.height / 2;
  const dx = towards.left + towards.width / 2 - cx;
  const dy = towards.top + towards.height / 2 - cy;
  if (dx === 0 && dy === 0) return { x: cx, y: cy };
  const scale = Math.min(
    dx === 0 ? Infinity : rect.width / 2 / Math.abs(dx),
    dy === 0 ? Infinity : rect.height / 2 / Math.abs(dy),
  );
  return { x: cx + dx * scale, y: cy + dy * scale };
}

/**
 * Joins the bubble to what it is about. Sitting right beside the one thing, a small arrow on the bubble's edge does
 * that. Anywhere else (moved by hand, set further off, beside a large card and pointing into it) a thin line runs
 * from one to the other; and about several things, a line to each.
 * @param besideOn The side of the one target the bubble was placed on; null if it wasn't placed beside it.
 * @param reach How close counts as beside (px): as close as a bubble is put when nobody says how close.
 */
export function pointerFor(
  bubble: Rect,
  targets: readonly Rect[],
  besideOn: Exclude<PlacedSide, 'none'> | null,
  reach: number,
): Pointer {
  // (nothing points at a thing the bubble is lying on)
  const clear = targets.filter((target) => overlap(bubble, target) === 0);
  const [target] = clear;
  if (!target) return { kind: 'none' };
  const side = clear.length > 1 ? null : besideOn;
  if (side !== null) {
    const sideways = side === 'right' || side === 'left';
    const distance =
      side === 'right'
        ? Math.abs(bubble.left - (target.left + target.width))
        : side === 'left'
          ? Math.abs(target.left - (bubble.left + bubble.width))
          : side === 'below'
            ? Math.abs(bubble.top - (target.top + target.height))
            : Math.abs(target.top - (bubble.top + bubble.height));
    const level = sideways
      ? target.top < bubble.top + bubble.height && target.top + target.height > bubble.top
      : target.left < bubble.left + bubble.width && target.left + target.width > bubble.left;
    if (distance <= reach && level) {
      // The arrow sits on the edge nearest the thing and slides along it to line up with the thing's middle
      const along = sideways
        ? Math.max(ARROW_INSET, Math.min(target.top + target.height / 2 - bubble.top, bubble.height - ARROW_INSET))
        : Math.max(ARROW_INSET, Math.min(target.left + target.width / 2 - bubble.left, bubble.width - ARROW_INSET));
      const edge = side === 'right' ? 'left' : side === 'left' ? 'right' : side === 'below' ? 'top' : 'bottom';
      return { kind: 'arrow', edge, along };
    }
  }
  return {
    kind: 'lines',
    lines: clear.map((each) => {
      const from = edgePoint(bubble, each);
      const to = edgePoint(each, bubble);
      return { x1: from.x, y1: from.y, x2: to.x, y2: to.y };
    }),
  };
}

/** How close to its one target a bubble counts as beside it (px). */
export function arrowReach(viewWidth: number, viewHeight: number): number {
  return Math.max(22, (Number.parseFloat(STOCK_GAP) * Math.min(viewWidth, viewHeight)) / 100 + 6);
}
