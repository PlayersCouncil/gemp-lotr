/**
 * Card positions of the legacy js/gemp-022/cards/CardGroup.js NormalCardGroup (cards in as few rows as fit, as large
 * as allowed), with the rounding of layoutCardElem. Pure: the caller renders the rectangles.
 */

/** Card.CardScale: width / height of an upright card. */
export const CARD_SCALE = 350 / 490;

/** One card to lay out, with the cards attached to it (extra copies in a deck pile). */
export interface GroupCard {
  horizontal: boolean;
  attached?: readonly { horizontal: boolean }[];
}

export interface CardRect {
  left: number;
  top: number;
  width: number;
  height: number;
  zIndex: number;
}

export interface GroupCardLayout {
  card: CardRect;
  /** In the order of GroupCard.attached. */
  attached: CardRect[];
}

export interface GroupBounds {
  left: number;
  top: number;
  width: number;
  height: number;
}

export interface NormalGroupOptions {
  /** CardGroup.padding */
  padding?: number;
  /** NormalCardGroup.maxCardHeight (497 by default; 200 for the collection and the deck piles) */
  maxCardHeight?: number | null;
}

// Card.getHeightForWidth / getWidthForMaxDimension
export function heightForWidth(horizontal: boolean, width: number): number {
  return horizontal ? Math.floor(width * CARD_SCALE) : Math.floor(width / CARD_SCALE);
}

export function widthForMaxDimension(horizontal: boolean, maxDimension: number): number {
  return horizontal ? maxDimension : Math.floor(maxDimension * CARD_SCALE);
}

/**
 * The inner area CardGroup.setBounds keeps (3px in from each side of the box it is given).
 */
export function groupArea(bounds: GroupBounds): GroupBounds {
  return {
    left: bounds.left + 3,
    top: bounds.top + 3,
    width: bounds.width - 6,
    height: bounds.height - 6,
  };
}

/** layoutCardElem rounds every value down. */
function rect(x: number, y: number, width: number, height: number, zIndex: number): CardRect {
  return {
    left: Math.floor(x),
    top: Math.floor(y),
    width: Math.floor(width),
    height: Math.floor(height),
    zIndex,
  };
}

/**
 * NormalCardGroup.layoutCards for the area returned by groupArea(): tries 1, 2, 3... rows until the cards fit.
 * Returns one entry per card, in order.
 *
 * Fixed: when a card was wider than the area at the tried row height, the legacy code ignored that the row layout had
 * failed, so cards overflowed the group or were never placed (only in tall, narrow groups). Here more rows are tried.
 */
export function layoutNormalGroup(
  cards: readonly GroupCard[],
  area: GroupBounds,
  { padding = 5, maxCardHeight = 497 }: NormalGroupOptions = {},
): GroupCardLayout[] {
  if (cards.length === 0) {
    return [];
  }
  const attachedOf = (card: GroupCard) => card.attached ?? [];
  const childWidths = (card: GroupCard, maxDimension: number) =>
    attachedOf(card).reduce((sum, child) => sum + widthForMaxDimension(child.horizontal, maxDimension), 0);
  const proportions = cards.map(
    (card) => (widthForMaxDimension(card.horizontal, 1000) + childWidths(card, 1000) * 0.2) / 1000,
  );
  const capped = (height: number) => (maxCardHeight != null ? Math.min(maxCardHeight, height) : height);

  /** Lays one card and its attachments out from layoutX; returns the new layoutX (before padding). */
  const placeCard = (card: GroupCard, layoutX: number, y: number, rowHeight: number, out: GroupCardLayout[]) => {
    let index = 10;
    const attached: CardRect[] = [];
    for (const child of attachedOf(card)) {
      const childWidth = widthForMaxDimension(child.horizontal, rowHeight);
      attached.push(
        rect(area.left + layoutX, area.top + y, childWidth, heightForWidth(child.horizontal, childWidth), index),
      );
      layoutX += Math.floor(childWidth * 0.2);
      index++;
    }
    const cardWidth = widthForMaxDimension(card.horizontal, rowHeight);
    out.push({
      card: rect(area.left + layoutX, area.top + y, cardWidth, heightForWidth(card.horizontal, cardWidth), index),
      attached,
    });
    return layoutX + Math.floor(cardWidth);
  };

  const oneRowHeight = () => {
    const totalWidth = proportions.reduce((sum, p) => sum + p * area.height, 0);
    const withoutPadding = area.width - padding * (proportions.length - 1);
    return totalWidth > withoutPadding ? Math.floor(area.height / (totalWidth / withoutPadding)) : area.height;
  };

  const layoutInRow = (height: number): GroupCardLayout[] => {
    height = capped(height);
    const out: GroupCardLayout[] = [];
    const y = Math.floor((area.height - height) / 2);
    let x = 0;
    for (const card of cards) {
      x = placeCard(card, x, y, height, out) + padding;
    }
    return out;
  };

  const canLayoutInRows = (rowCount: number) => {
    const rowHeight = capped((area.height - padding * (rowCount - 1)) / rowCount);
    let totalWidth = 0;
    let row = 0;
    for (const proportion of proportions) {
      const width = proportion * rowHeight;
      totalWidth += width;
      if (totalWidth > area.width) {
        row++;
        if (row >= rowCount) {
          return false;
        }
        totalWidth = width;
      }
      totalWidth += padding;
    }
    return true;
  };

  const layoutInRows = (rowCount: number): GroupCardLayout[] | null => {
    const rowHeight = capped((area.height - (rowCount - 1) * padding) / rowCount);
    const yBias = Math.floor((area.height - rowHeight * rowCount - padding * (rowCount - 1)) / 2);
    const out: GroupCardLayout[] = [];
    let x = 0;
    let row = 0;
    let y = yBias;
    for (const card of cards) {
      const widthWithAttachments =
        widthForMaxDimension(card.horizontal, rowHeight) + childWidths(card, rowHeight) * 0.2;
      if (x + widthWithAttachments > area.width) {
        row++;
        x = 0;
        y = yBias + row * (rowHeight + padding);
      }
      x = placeCard(card, x, y, rowHeight, out);
      if (x > area.width) {
        return null;
      }
      x += padding;
    }
    return out;
  };

  const single = oneRowHeight();
  if (single * 2 + padding > area.height) {
    return layoutInRow(single);
  }
  // Every extra row makes the cards smaller, so this ends; the cap only guards against bad input (NaN sizes)
  for (let rows = 2; rows < 10_000; rows++) {
    if (canLayoutInRows(rows)) {
      const result = layoutInRows(rows);
      if (result) {
        return result;
      }
    }
  }
  return layoutInRow(single);
}
