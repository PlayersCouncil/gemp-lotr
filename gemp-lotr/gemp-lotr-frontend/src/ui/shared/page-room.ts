/**
 * Room a page gives up at the window's right, for something docked there (the game page's scenario authoring panel):
 * the page sets this custom property on the document element (`--gd-page-right: 430px`), and what is laid out against
 * the window rather than its own box (dialogs centred, a card shown large, the hover zoom) keeps to the rest. Absent
 * (the usual case), the whole window is there, exactly as before.
 */
export const PAGE_RIGHT_PROPERTY = '--gd-page-right';

/** How much of the window's right the page has given up (px), as the document element's style says; 0 for none. */
export function pageRight(document: Document): number {
  const root = document.documentElement;
  const value = document.defaultView?.getComputedStyle(root).getPropertyValue(PAGE_RIGHT_PROPERTY) ?? '';
  const right = Number.parseFloat(value);
  return Number.isFinite(right) && right > 0 ? right : 0;
}
