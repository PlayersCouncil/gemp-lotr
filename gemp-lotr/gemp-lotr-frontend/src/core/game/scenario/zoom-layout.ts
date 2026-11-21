/**
 * A card (or a picture) a scenario's message shows large: how big it is drawn, where the bubble's space beside it
 * is, and where the parts the message is about lie. Pure geometry; the view draws it.
 */
import { Rect } from './bubble-placement';
import { ScenarioZoom } from './scenario-events';

/** The space between the large card and the bubble beside it (px). */
const BESIDE_GAP = 28;

export interface ZoomLayout {
  /** The frame the card is seen through, on the screen. */
  frame: Rect;
  /** The card itself, in the frame (larger than it when magnified, and slid so the parts in focus are in sight). */
  card: Rect;
  /** Everything of the frame but the parts in focus, as an SVG path (drawn dark, even-odd); null with no focus. */
  dim: string | null;
  /** The outline of each part in focus that shows through the frame, in the frame. */
  marks: readonly Rect[];
  /** The same parts on the screen: what the bubble points at. */
  focus: readonly Rect[];
}

/**
 * @param shape The picture's width over its height.
 * @param bubbleWidth The bubble that goes beside it.
 */
export function layoutZoom(
  zoom: ScenarioZoom,
  shape: number,
  bubbleWidth: number,
  viewWidth: number,
  viewHeight: number,
): ZoomLayout {
  // As large as the window allows, with room left beside it for the bubble
  let frameHeight = Math.max(120, viewHeight - 56);
  let frameWidth = frameHeight * shape;
  const room = Math.max(160, viewWidth - bubbleWidth - BESIDE_GAP - 40);
  if (frameWidth > room) {
    frameWidth = room;
    frameHeight = frameWidth / shape;
  }
  const frame: Rect = {
    left: Math.max(12, (viewWidth - (frameWidth + BESIDE_GAP + bubbleWidth)) / 2),
    top: (viewHeight - frameHeight) / 2,
    width: frameWidth,
    height: frameHeight,
  };

  // Magnified, the card is larger than its frame, and is slid so that what is in focus sits in the middle
  const scale = Math.max(1, zoom.magnify);
  const cardWidth = frameWidth * scale;
  const cardHeight = frameHeight * scale;
  const rects = zoom.focus.map((part) => part.rect);
  const centreX =
    rects.length === 0 ? 50 : (Math.min(...rects.map((r) => r[0])) + Math.max(...rects.map((r) => r[0] + r[2]))) / 2;
  const centreY =
    rects.length === 0 ? 50 : (Math.min(...rects.map((r) => r[1])) + Math.max(...rects.map((r) => r[1] + r[3]))) / 2;
  const cardLeft = Math.min(0, Math.max(frameWidth - cardWidth, frameWidth / 2 - (cardWidth * centreX) / 100));
  const cardTop = Math.min(0, Math.max(frameHeight - cardHeight, frameHeight / 2 - (cardHeight * centreY) / 100));

  // Everything on the card but what is in focus is darkened; each part in focus gets an outline
  let dim = `M0 0H${frameWidth}V${frameHeight}H0Z`;
  const marks: Rect[] = [];
  const focus: Rect[] = [];
  for (const [partLeft, partTop, partWidth, partHeight] of rects) {
    // Only as much of it as shows through the frame
    const left = Math.max(0, cardLeft + (cardWidth * partLeft) / 100);
    const top = Math.max(0, cardTop + (cardHeight * partTop) / 100);
    const right = Math.min(frameWidth, cardLeft + (cardWidth * (partLeft + partWidth)) / 100);
    const bottom = Math.min(frameHeight, cardTop + (cardHeight * (partTop + partHeight)) / 100);
    if (right - left < 2 || bottom - top < 2) continue;
    dim += `M${left} ${top}H${right}V${bottom}H${left}Z`;
    marks.push({ left: left - 3, top: top - 3, width: right - left + 6, height: bottom - top + 6 });
    focus.push({ left: frame.left + left, top: frame.top + top, width: right - left, height: bottom - top });
  }
  return {
    frame,
    card: { left: cardLeft, top: cardTop, width: cardWidth, height: cardHeight },
    dim: rects.length > 0 ? dim : null,
    marks,
    focus,
  };
}

/** A picture's shape until it has loaded: a card's, upright or (a site) on its side. */
export function cardShape(horizontal: boolean): number {
  return horizontal ? 497 / 355 : 357 / 497;
}
