/**
 * Card sizing for a box of cards that wrap into rows (the collection, a deck pile): the height of a row of cards, so
 * that every card fits in the box. Pure: the fitCards directive measures the box and applies the result.
 */

/** Width / height of an upright card (Card.CardScale). A landscape card is as wide as a row is high. */
export const CARD_SCALE = 350 / 490;

/**
 * - `contain`: as large as fits (never above the maximum), so every card is visible without scrolling;
 * - `rows`: the legacy NormalCardGroup's way: the fewest rows the cards fit in, each row as high as the box allows
 *   (so cards can come out smaller than with `contain`);
 * - `none`: always the maximum height; the cards wrap and the box scrolls.
 */
export type CardFit = 'contain' | 'rows' | 'none';

export interface FitCardsInput {
  /** The box's content size, in px. */
  width: number;
  height: number;
  /** One entry per card: whether it is a landscape card. */
  horizontal: readonly boolean[];
  /** Space between cards, across and down. */
  gap: number;
  /** The largest row height. */
  maxHeight: number;
  fit: CardFit;
}

/** The width a card takes in a row of the given height (rounded down, as card elements are). */
export function cardWidth(horizontal: boolean, rowHeight: number): number {
  return horizontal ? rowHeight : Math.floor(rowHeight * CARD_SCALE);
}

/** How many rows the cards need at that row height in a box that wide (Infinity if a card is wider than the box). */
export function rowsNeeded(horizontal: readonly boolean[], rowHeight: number, width: number, gap: number): number {
  let rows = 1;
  let x = 0;
  for (const landscape of horizontal) {
    const w = cardWidth(landscape, rowHeight);
    if (w > width) {
      return Infinity;
    }
    if (x > 0 && x + gap + w > width) {
      rows++;
      x = w;
    } else {
      x += (x > 0 ? gap : 0) + w;
    }
  }
  return rows;
}

function fits(input: FitCardsInput, rowHeight: number): boolean {
  const rows = rowsNeeded(input.horizontal, rowHeight, input.width, input.gap);
  return rows * rowHeight + (rows - 1) * input.gap <= input.height;
}

/**
 * The row height for the cards: whole pixels, at least 1. With `contain`, the largest height up to `maxHeight` at
 * which all cards fit in the box (smaller rows never need more rows, so a binary search finds it).
 */
export function fitCardHeight(input: FitCardsInput): number {
  const max = Math.max(1, Math.floor(input.maxHeight));
  if (input.fit === 'none' || input.horizontal.length === 0 || fits(input, max)) {
    return max;
  }
  if (input.fit === 'rows') {
    return rowsFitHeight(input, max);
  }
  let low = 1;
  let high = max;
  if (!fits(input, low)) {
    return low;
  }
  while (high - low > 1) {
    const middle = Math.floor((low + high) / 2);
    if (fits(input, middle)) {
      low = middle;
    } else {
      high = middle;
    }
  }
  return low;
}

/**
 * NormalCardGroup.layoutCards: tries 1, 2, 3... rows, each `(height - gaps) / rows` high (capped at the maximum),
 * until the cards fit in that many rows.
 */
function rowsFitHeight(input: FitCardsInput, max: number): number {
  for (let rows = 1; rows <= input.horizontal.length; rows++) {
    const rowHeight = Math.min(max, Math.floor((input.height - input.gap * (rows - 1)) / rows));
    if (rowHeight < 1) {
      break;
    }
    if (rowsNeeded(input.horizontal, rowHeight, input.width, input.gap) <= rows) {
      return rowHeight;
    }
  }
  return 1;
}

/** Reads a length custom property ("200px", "5") as px; `fallback` when unset or not a number. */
export function parsePx(value: string | null | undefined, fallback: number): number {
  const number = parseFloat(value ?? '');
  return Number.isFinite(number) ? number : fallback;
}

export function parseFit(value: string | null | undefined): CardFit {
  const fit = value?.trim();
  return fit === 'none' || fit === 'rows' ? fit : 'contain';
}
