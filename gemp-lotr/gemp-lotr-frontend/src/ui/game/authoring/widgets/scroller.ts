/** The element a tab of the panel scrolls in (the panel's body): the nearest ancestor that scrolls. */
export function scrollerOf(element: HTMLElement): HTMLElement | null {
  for (let at = element.parentElement; at; at = at.parentElement) {
    if (/(auto|scroll)/.test(getComputedStyle(at).overflowY)) return at;
  }
  return null;
}
