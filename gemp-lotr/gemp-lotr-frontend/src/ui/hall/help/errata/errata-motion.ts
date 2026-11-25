/** Whether the player asked for less motion (PcErrata.reducedMotion): no slides, nudges, turns or smooth scrolls. */
export function prefersReducedMotion(document: Document): boolean {
  const view = document.defaultView;
  return typeof view?.matchMedia === 'function' && view.matchMedia('(prefers-reduced-motion: reduce)').matches;
}
