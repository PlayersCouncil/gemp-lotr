import { Rect } from './rect';

/** Positions inside the deck (center) pane, ported from the legacy GempLotrDeckBuildingUI.layoutUI. */
export interface DeckLayout {
  manageDecks: Rect;
  ringBearer: Rect;
  ring: Rect;
  /** Null when hidden (only the movie formats use a Map). */
  map: Rect | null;
  /** Null when hidden (only Race to Mount Doom leagues have a Race meta-site). */
  race: Rect | null;
  sites: Rect;
  /** The region holding both draw deck halves. */
  drawDeck: Rect;
  /** Free Peoples half, relative to drawDeck. */
  freePeoples: Rect;
  /** Shadow half, relative to drawDeck. */
  shadow: Rect;
  stats: Rect;
}

const MANAGE_HEIGHT = 23;
const PADDING = 5;
const BOTTOM_BAR = 50;
const STATS_HEIGHT = 70;
const MAX_SITES_WIDTH = 250;

/**
 * @param paneWidth content width of the deck pane
 * @param paneHeight content height of the deck pane
 */
export function computeDeckLayout(paneWidth: number, paneHeight: number, showMap: boolean, showRace: boolean): DeckLayout {
  const deckHeight = paneHeight - (MANAGE_HEIGHT + PADDING);
  const rowHeight = Math.floor((deckHeight - 6 * PADDING) / 5);
  const sitesWidth = Math.min(Math.floor((1.5 * deckHeight) / 5), MAX_SITES_WIDTH);
  const halfWidth = Math.floor((sitesWidth - PADDING) / 2);
  const rightHalfLeft = Math.floor((sitesWidth + 3 * PADDING) / 2);

  const firstRowTop = MANAGE_HEIGHT + 2 * PADDING;
  const specialRowTop = firstRowTop + rowHeight;
  const showSpecialRow = showMap || showRace;

  const drawLeft = PADDING * 2 + sitesWidth;
  const drawWidth = paneWidth - (sitesWidth + PADDING) - PADDING;
  const drawHeight = deckHeight - 2 * PADDING - BOTTOM_BAR;

  return {
    manageDecks: { left: PADDING, top: PADDING, width: paneWidth, height: MANAGE_HEIGHT },
    ringBearer: { left: PADDING, top: firstRowTop, width: halfWidth, height: rowHeight },
    ring: { left: rightHalfLeft, top: firstRowTop, width: halfWidth, height: rowHeight },
    map: showMap ? { left: PADDING, top: specialRowTop, width: halfWidth, height: rowHeight } : null,
    race: showRace ? { left: rightHalfLeft, top: specialRowTop, width: halfWidth, height: rowHeight } : null,
    sites: showSpecialRow
      ? {
          left: PADDING,
          top: MANAGE_HEIGHT + 3 * PADDING + 2 * rowHeight,
          width: sitesWidth,
          height: deckHeight - 2 * rowHeight - 2 * PADDING,
        }
      : {
          left: PADDING,
          top: MANAGE_HEIGHT + 3 * PADDING + rowHeight,
          width: sitesWidth,
          height: deckHeight - rowHeight - 2 * PADDING,
        },
    drawDeck: { left: drawLeft, top: firstRowTop, width: drawWidth, height: drawHeight },
    freePeoples: { left: 0, top: 0, width: drawWidth, height: drawHeight / 2 },
    shadow: { left: 0, top: drawHeight / 2, width: drawWidth, height: drawHeight / 2 },
    stats: { left: drawLeft, top: MANAGE_HEIGHT + PADDING + deckHeight - BOTTOM_BAR, width: drawWidth, height: STATS_HEIGHT },
  };
}
