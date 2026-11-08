import { CardBox } from '../../shared/card/card-hover';

/** The remastered card size (CardDisplay.TargetLong / TargetShort). */
export const TARGET_LONG = 1039;
export const TARGET_SHORT = 745;

export interface ZoomPlacement {
  /** Where the zoomed card goes, in viewport px. */
  left: number;
  top: number;
  /** Its size, in whole px. */
  width: number;
  height: number;
}

/**
 * AutoZoom.displayPreviewImage: the zoomed card as large as 90% of the window's shorter side allows (never above the
 * remastered size), right of the hovered card (overlapping it by 15px), else left of it, vertically centred on it and
 * kept within the window.
 */
export function zoomPlacement(
  horizontal: boolean,
  card: CardBox,
  windowWidth: number,
  windowHeight: number,
): ZoomPlacement {
  const maxLongSide = (windowWidth / windowHeight <= 1 ? windowWidth : windowHeight) * 0.9;
  const targetLong = Math.min(maxLongSide, TARGET_LONG);
  const targetShort = Math.min((maxLongSide * TARGET_SHORT) / TARGET_LONG, TARGET_SHORT);
  // CardDisplay.resize with the legacy (width, height) limits
  const maxWidth = horizontal ? targetLong : targetShort;
  const maxHeight = horizontal ? targetShort : targetLong;
  const long = Math.min(maxWidth, maxHeight, TARGET_LONG);
  const short = (long * TARGET_SHORT) / TARGET_LONG;
  const width = Math.floor(horizontal ? Math.min(long, maxWidth) : Math.min(short, maxWidth));
  const height = Math.floor(horizontal ? Math.min(short, maxHeight) : Math.min(long, maxHeight));

  const cardWidth = card.right - card.left;
  const cardHeight = card.bottom - card.top;
  let placedWidth = width;
  let placedHeight = height;
  const rightEdge = card.left + cardWidth - 15;
  const leftEdge = card.left;
  const pastRight = rightEdge + placedWidth > windowWidth;
  const pastLeft = leftEdge - placedWidth < 0;
  let left = rightEdge;
  if (pastRight && pastLeft) {
    // Neither side has room: the larger one (the legacy page placed it by a smaller size but drew it full size)
    const rightSpace = windowWidth - (leftEdge + cardWidth);
    const leftSpace = leftEdge;
    placedWidth = Math.max(rightSpace, leftSpace);
    left = rightSpace > leftSpace ? rightEdge : leftEdge - placedWidth;
    placedHeight = placedWidth / (width / height);
  } else if (pastRight) {
    left = leftEdge - placedWidth;
  }
  let top = card.top + cardHeight / 2 - placedHeight / 2;
  if (top + placedHeight + 15 > windowHeight) {
    top = windowHeight - placedHeight - 15;
  } else if (top < 0) {
    top = 0;
  }
  return { left, top, width, height };
}
