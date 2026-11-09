/**
 * Card sizing for a box of cards that wrap into rows (the collection, a deck pile): the height of a row of cards, so
 * that every card fits in the box. Pure: the fitCards directive measures the box and applies the result.
 */

/** Width / height of an upright card (Card.CardScale). A landscape card is as wide as a row is high. */
const CARD_SCALE = 350 / 490;

/**
 * - `contain`: as large as fits (never above the maximum), so every card is visible without scrolling;
 * - `rows`: the classic layout (see rowsFitHeight), so cards can come out smaller than with `contain`;
 * - `none`: always the maximum height; the cards wrap and the box scrolls.
 */
export type CardFit = 'contain' | 'rows' | 'none';

/** A card in the box, with the other copies drawn behind it (a draw deck pile), each showing a fifth of its width. */
export interface FitCard {
  /** A landscape card. */
  horizontal: boolean;
  /** One entry per copy behind it: whether it is landscape. */
  behind?: readonly boolean[];
}

/** The share of a copy's width left showing beside the card in front of it. */
const BEHIND_SHOWN = 0.2;

export interface FitCardsInput {
  /** The box's content size, in px. */
  width: number;
  height: number;
  cards: readonly FitCard[];
  /** Space between cards, across and down. */
  gap: number;
  /** The largest row height. */
  maxHeight: number;
  fit: CardFit;
}

/** The width a card takes in a row of the given height (rounded down, as card elements are). */
export function cardWidth(horizontal: boolean, rowHeight: number): number {
  return horizontal ? Math.floor(rowHeight) : Math.floor(rowHeight * CARD_SCALE);
}

/** The width a card and the copies behind it take in a row. */
export function stackWidth(card: FitCard, rowHeight: number): number {
  const behind = (card.behind ?? []).reduce(
    (sum, landscape) => sum + Math.floor(cardWidth(landscape, rowHeight) * BEHIND_SHOWN),
    0,
  );
  return behind + cardWidth(card.horizontal, rowHeight);
}

/** How many rows the cards need at that row height in a box that wide (Infinity if a card is wider than the box). */
export function rowsNeeded(cards: readonly FitCard[], rowHeight: number, width: number, gap: number): number {
  let rows = 1;
  let x = 0;
  for (const card of cards) {
    const w = stackWidth(card, rowHeight);
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
  const rows = rowsNeeded(input.cards, rowHeight, input.width, input.gap);
  return rows * rowHeight + (rows - 1) * input.gap <= input.height;
}

export interface CardFitResult {
  /** The row height. */
  height: number;
  /**
   * The space (px, whole) above the rows the height was chosen for when they are centred in the box: the legacy
   * groups put their rows there (with `rows`, the rows tried, which the cards may not all fill). 0 when they don't fit.
   */
  offset: number;
}

/**
 * How far each card's row moves so it starts on a whole pixel, as the legacy groups rounded each row's position down
 * (`rows` fit only: a fractional row height would otherwise put some rows a pixel lower). One entry per card.
 */
export function rowSnaps(cards: readonly FitCard[], rowHeight: number, width: number, gap: number): number[] {
  const step = rowHeight + gap;
  let x = 0;
  let row = 0;
  return cards.map((card) => {
    const share =
      widthForLongSide(card.horizontal, rowHeight) +
      (card.behind ?? []).reduce((sum, landscape) => sum + widthForLongSide(landscape, rowHeight), 0) * BEHIND_SHOWN;
    if (x > 0 && x + share > width) {
      row++;
      x = 0;
    }
    x += stackWidth(card, rowHeight) + gap;
    return Math.floor(row * step) - row * step;
  });
}

/** The row height (fitCardHeight) and where the rows start. */
export function fitCards(input: FitCardsInput): CardFitResult {
  const { height, rows } = fitRows(input);
  const used = rows * height + (rows - 1) * input.gap;
  return { height, offset: Math.max(0, Math.floor((input.height - used) / 2)) };
}

/**
 * The row height for the cards, at least 1. With `contain`, the largest whole height up to `maxHeight` at which all
 * cards fit in the box (smaller rows never need more rows, so a binary search finds it). With `rows`, the legacy
 * height, which can have a fraction as it did there.
 */
export function fitCardHeight(input: FitCardsInput): number {
  return fitRows(input).height;
}

function fitRows(input: FitCardsInput): { height: number; rows: number } {
  const max = Math.max(1, Math.floor(input.maxHeight));
  const at = (height: number) => ({
    height,
    rows: input.cards.length ? rowsNeeded(input.cards, height, input.width, input.gap) : 0,
  });
  if (input.fit === 'none' || input.cards.length === 0) {
    return at(max);
  }
  if (input.fit === 'rows') {
    const fit = rowsFitHeight(input, max);
    return Number.isFinite(fit.height) && fit.height >= 1 ? fit : { height: 1, rows: 1 };
  }
  if (fits(input, max)) {
    return at(max);
  }
  let low = 1;
  let high = max;
  if (!fits(input, low)) {
    return at(low);
  }
  while (high - low > 1) {
    const middle = Math.floor((low + high) / 2);
    if (fits(input, middle)) {
      low = middle;
    } else {
      high = middle;
    }
  }
  return at(low);
}

/** Card.getWidthForMaxDimension: the width of a card whose longer side is `size`. */
function widthForLongSide(horizontal: boolean, size: number): number {
  return horizontal ? size : Math.floor(size * CARD_SCALE);
}

/**
 * The height of one row that holds every card if two rows of it wouldn't fit;
 * otherwise 2, 3... rows, each `(height - gaps) / rows` high (capped at the maximum), until the cards fit in that
 * many rows.
 *
 * When a card is wider than the box at the tried height, more rows are tried.
 */
function rowsFitHeight({ width, height, cards, gap }: FitCardsInput, max: number): { height: number; rows: number } {
  const capped = (rowHeight: number) => Math.min(max, rowHeight);
  // Each card's width per pixel of height, from the legacy page's 1000px reference
  const proportions = cards.map(
    (card) =>
      (widthForLongSide(card.horizontal, 1000) +
        (card.behind ?? []).reduce((sum, landscape) => sum + widthForLongSide(landscape, 1000), 0) * BEHIND_SHOWN) /
      1000,
  );

  const totalWidth = proportions.reduce((sum, proportion) => sum + proportion * height, 0);
  const withoutGaps = width - gap * (cards.length - 1);
  const single = totalWidth > withoutGaps ? Math.floor(height / (totalWidth / withoutGaps)) : height;
  if (single * 2 + gap > height) {
    return { height: capped(single), rows: 1 };
  }

  const canLayoutInRows = (rows: number, rowHeight: number) => {
    let x = 0;
    let row = 0;
    for (const proportion of proportions) {
      const w = proportion * rowHeight;
      x += w;
      if (x > width) {
        row++;
        if (row >= rows) {
          return false;
        }
        x = w;
      }
      x += gap;
    }
    return true;
  };
  // The legacy row layout also gave up when a card ended past the box's edge
  const layoutFits = (rowHeight: number) => {
    let x = 0;
    for (const card of cards) {
      const w = stackWidth(card, rowHeight);
      const share =
        widthForLongSide(card.horizontal, rowHeight) +
        (card.behind ?? []).reduce((sum, landscape) => sum + widthForLongSide(landscape, rowHeight), 0) * BEHIND_SHOWN;
      if (x + share > width) {
        x = 0;
      }
      x += w;
      if (x > width) {
        return false;
      }
      x += gap;
    }
    return true;
  };
  // Every extra row makes the cards smaller, so this ends; the cap only guards against bad input (NaN sizes)
  for (let rows = 2; rows < 10_000; rows++) {
    const rowHeight = capped((height - gap * (rows - 1)) / rows);
    if (rowHeight < 1) {
      break;
    }
    if (canLayoutInRows(rows, rowHeight) && layoutFits(rowHeight)) {
      return { height: rowHeight, rows };
    }
  }
  return { height: capped(single), rows: 1 };
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
