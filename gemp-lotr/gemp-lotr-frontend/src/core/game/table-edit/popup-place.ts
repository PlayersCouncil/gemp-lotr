/**
 * Where the edit controls' popups go in the window (the chooser by an open spot, a card's menu, a submenu): beside what
 * they are for, flipped where there is no room, and kept inside the window. Pure: the view measures and hands in.
 */

/** A box of the window (px), as getBoundingClientRect gives it. */
export interface Box {
  left: number;
  top: number;
  right: number;
  bottom: number;
}

export interface Size {
  width: number;
  height: number;
}

export interface Place {
  left: number;
  top: number;
}

/** How far a popup keeps from the window's edges (px). */
export const EDGE = 8;

const within = (start: number, length: number, room: number) => Math.max(EDGE, Math.min(start, room - length - EDGE));

/**
 * Under the box if it fits there, else over it if it fits there, else on whichever side has more room; its left edge
 * with the box's.
 */
export function placeBeside(anchor: Box, size: Size, room: Size, gap = 4): Place {
  const below = room.height - anchor.bottom - gap - EDGE;
  const above = anchor.top - gap - EDGE;
  const top =
    size.height <= below || (size.height > above && below >= above)
      ? anchor.bottom + gap
      : anchor.top - gap - size.height;
  return { left: within(anchor.left, size.width, room.width), top: within(top, size.height, room.height) };
}

/** At a point: to its right and down from it, each flipped where it would leave the window. */
export function placeAtPoint(point: { x: number; y: number }, size: Size, room: Size): Place {
  const left = point.x + size.width + EDGE <= room.width ? point.x : point.x - size.width;
  const top = point.y + size.height + EDGE <= room.height ? point.y : point.y - size.height;
  return { left: within(left, size.width, room.width), top: within(top, size.height, room.height) };
}

/** Beside an item of a menu: to its right, else to its left; its top with the item's. */
export function placeSubmenu(item: Box, size: Size, room: Size): Place {
  const left = item.right + size.width + EDGE <= room.width ? item.right : item.left - size.width;
  return { left: within(left, size.width, room.width), top: within(item.top, size.height, room.height) };
}
