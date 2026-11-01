/**
 * The page frame the legacy deck builder got from jquery.layout 1.3 (deckBuilder.js: $('body').layout({
 * applyDefaultStyles: true, east__minSize: 410, east__maxSize: "50%" })): a center pane with the deck, an east pane
 * with the collection, and a 6px resizer bar between them with a toggler that closes the east pane.
 */

/** Width of the resizer bar, open or closed (spacing_open / spacing_closed). */
export const RESIZER_SIZE = 6;
/** Height of the toggler button on the resizer bar (togglerLength_open/closed). */
export const TOGGLER_LENGTH = 50;
/** Padding and border jquery.layout's default styles give each pane. */
export const PANE_PADDING = 10;
export const PANE_BORDER = 1;
/** Outer width (including padding and border) limits of the collection pane. */
export const EAST_MIN_WIDTH = 410;
export const EAST_MAX_FRACTION = 0.5;

const PANE_CHROME = 2 * (PANE_PADDING + PANE_BORDER);

export interface PageLayout {
  /** The east pane is not shown: closed by the user, or no room for it (see hasRoomForEast). */
  eastHidden: boolean;
  /** Distance of the center pane from the right edge (east pane + resizer). */
  centerRight: number;
  /** Content box of the center pane (what the legacy code read with $('#deckDiv').width()/height()). */
  centerContentWidth: number;
  centerContentHeight: number;
  /** Content box of the east pane; 0 when closed. */
  eastContentWidth: number;
  eastContentHeight: number;
  /** Distance of the resizer bar from the right edge. */
  resizerRight: number;
  resizerHeight: number;
  togglerTop: number;
}

/**
 * The largest outer width the east pane may have: jquery.layout takes the percentage of the width left after the
 * resizer bar (1280px wide -> (1280 - 6) / 2 = 637).
 */
export function maxEastWidth(viewportWidth: number): number {
  return Math.floor((viewportWidth - RESIZER_SIZE) * EAST_MAX_FRACTION);
}

/**
 * Whether the east pane fits at its minimum width (viewports narrower than about 826px don't). The legacy page then
 * hid the pane but left the resizer bar floating over the deck; here the pane counts as closed instead.
 */
export function hasRoomForEast(viewportWidth: number): boolean {
  return maxEastWidth(viewportWidth) >= EAST_MIN_WIDTH;
}

export function clampEastWidth(width: number, viewportWidth: number): number {
  return Math.min(Math.max(Math.round(width), EAST_MIN_WIDTH), Math.max(EAST_MIN_WIDTH, maxEastWidth(viewportWidth)));
}

/**
 * @param eastWidth outer width of the east pane (clamped to its limits here)
 */
export function computePageLayout(
  viewportWidth: number,
  viewportHeight: number,
  eastWidth: number,
  eastClosed: boolean,
): PageLayout {
  const hidden = eastClosed || !hasRoomForEast(viewportWidth);
  const east = hidden ? 0 : clampEastWidth(eastWidth, viewportWidth);
  const centerRight = east + RESIZER_SIZE;
  const contentHeight = Math.max(0, viewportHeight - PANE_CHROME);
  return {
    eastHidden: hidden,
    centerRight,
    centerContentWidth: Math.max(0, viewportWidth - centerRight - PANE_CHROME),
    centerContentHeight: contentHeight,
    eastContentWidth: hidden ? 0 : east - PANE_CHROME,
    eastContentHeight: hidden ? 0 : contentHeight,
    resizerRight: east,
    resizerHeight: viewportHeight,
    togglerTop: Math.round((viewportHeight - TOGGLER_LENGTH) / 2),
  };
}

/** East pane outer width for a resizer dropped with its left edge at `resizerLeft` (jquery.layout's drag stop). */
export function eastWidthForResizerAt(resizerLeft: number, viewportWidth: number): number {
  return clampEastWidth(viewportWidth - resizerLeft - RESIZER_SIZE, viewportWidth);
}
