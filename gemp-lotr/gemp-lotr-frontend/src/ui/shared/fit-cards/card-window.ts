import { FitCard, rowsNeeded, stackWidth } from './fit-cards';

/**
 * The size of a window of cards sized to its cards rather than to most of the screen (legacy gameUi.js
 * `openCompactCardDialog` and `capCards`, as of origin/feature/tutorials; Ketura, 2026-11-19: card windows sized to
 * their cards, in both themes). Pure: the gdCardWindow directive measures the window and applies it.
 */

/** The rule's numbers. */
export const CARD_WINDOW = {
  /** The tallest card tried, and the least: from the tallest down in these steps. */
  maxCardHeight: 500,
  minCardHeight: 80,
  step: 20,
  /** The share of the room the window may take, across and down. */
  share: 0.9,
  /** The least width of the cards' box. */
  minWidth: 340,
  /** The height of an empty window's cards' box (at most). */
  emptyHeight: 150,
} as const;

export interface CardWindowInput {
  cards: readonly FitCard[];
  /** Space between cards, across and down. */
  gap: number;
  /** Room left under each row for a line of text (px; 0 for none). */
  label: number;
  /** The room the window is placed in: the browser's window, less a panel docked at its side. */
  room: { width: number; height: number };
  /** What the window adds around its cards' box (title bar, buttons, padding, border). */
  chrome: { width: number; height: number };
}

export interface CardWindowSize {
  /** The cards' box (px). */
  width: number;
  height: number;
  /** The height the cards are laid out at; null where none down to the least fits (the box's own rule then). */
  cardHeight: number | null;
}

/**
 * The largest card height not above {@link CARD_WINDOW}.maxCardHeight (trying downwards in steps, to the least) at
 * which every card fits, in as few rows as possible, in a window no larger than {@link CARD_WINDOW}.share of the
 * room; and the cards' box for it: as wide as its widest row (the cards shared out evenly between the rows), at least
 * {@link CARD_WINDOW}.minWidth, and as high as its rows with their labels. Where nothing fits, the largest box allowed.
 */
export function cardWindowSize({ cards, gap, label, room, chrome }: CardWindowInput): CardWindowSize {
  const maxWidth = Math.floor(room.width * CARD_WINDOW.share) - chrome.width;
  const maxHeight = Math.floor(room.height * CARD_WINDOW.share) - chrome.height;
  if (cards.length === 0) {
    return { width: CARD_WINDOW.minWidth, height: Math.min(maxHeight, CARD_WINDOW.emptyHeight), cardHeight: null };
  }
  for (
    let cardHeight: number = CARD_WINDOW.maxCardHeight;
    cardHeight >= CARD_WINDOW.minCardHeight;
    cardHeight -= CARD_WINDOW.step
  ) {
    const size = layout(cards, cardHeight, gap, label, maxWidth, maxHeight);
    if (size) return size;
  }
  return { width: Math.max(0, maxWidth), height: Math.max(0, maxHeight), cardHeight: null };
}

function layout(
  cards: readonly FitCard[],
  cardHeight: number,
  gap: number,
  label: number,
  maxWidth: number,
  maxHeight: number,
): CardWindowSize | null {
  const widths = cards.map((card) => stackWidth(card, cardHeight));
  for (let rows = 1; rows <= widths.length; rows++) {
    const height = rows * (cardHeight + label) + (rows - 1) * gap;
    if (height > maxHeight) return null;
    const perRow = Math.ceil(widths.length / rows);
    let widest = 0;
    for (let first = 0; first < widths.length; first += perRow) {
      const row = widths.slice(first, first + perRow);
      widest = Math.max(widest, row.reduce((sum, width) => sum + width, 0) + gap * (row.length - 1));
    }
    const width = Math.max(CARD_WINDOW.minWidth, widest);
    // (the cards wrap by their own widths: the rows shared out evenly must also be how they fall)
    if (width <= maxWidth && rowsNeeded(cards, cardHeight, width, gap) <= rows) {
      return { width, height, cardHeight };
    }
  }
  return null;
}
