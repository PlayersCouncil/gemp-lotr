import { RevealedRow } from '../../../core/hall/events/event-list';

/** The Events tab's small view helpers. */

const revealedSeq = new WeakMap<HTMLElement, number>();

/** How long a revealed row is kept at the top while the page below it is still being filled. */
const REVEAL_SETTLE_MS = 2000;

/**
 * EventDrawer.scrollIntoView: the row opened from a link is scrolled to the top of its scroller, smoothly unless
 * the viewer prefers reduced motion. Each reveal once. The legacy page scrolled once its whole tab was there; here
 * the page may still be too short to bring the row up (the drawer and the completed events still loading), so while
 * the page grows in the next moments the row is brought up again, until it is at the top.
 */
export function revealRow(host: HTMLElement, revealed: RevealedRow | null): void {
  if (revealed === null || revealedSeq.get(host) === revealed.seq) {
    return;
  }
  const row = Array.from(host.querySelectorAll<HTMLElement>('gd-event-row')).find(
    (element) => element.dataset['key'] === revealed.key,
  );
  if (!row) {
    return;
  }
  revealedSeq.set(host, revealed.seq);
  const view = host.ownerDocument.defaultView;
  const reduced = view?.matchMedia('(prefers-reduced-motion: reduce)').matches ?? true;
  const scroll = () => {
    try {
      row.scrollIntoView({ block: 'start', behavior: reduced ? 'auto' : 'smooth' });
    } catch {
      // a page without layout (tests) has nothing to scroll
    }
  };
  scroll();
  if (!view || typeof view.ResizeObserver !== 'function') {
    return;
  }
  const growing = new view.ResizeObserver(() => {
    const scroller = scrollerOf(row);
    if (scroller && Math.abs(row.getBoundingClientRect().top - scroller.getBoundingClientRect().top) > 1) {
      scroll();
    }
  });
  growing.observe(host);
  view.setTimeout(() => growing.disconnect(), REVEAL_SETTLE_MS);
}

/** The nearest ancestor that scrolls vertically. */
function scrollerOf(element: HTMLElement): HTMLElement | null {
  const view = element.ownerDocument.defaultView;
  for (let node = element.parentElement; node && view; node = node.parentElement) {
    if (/auto|scroll/.test(view.getComputedStyle(node).overflowY)) {
      return node;
    }
  }
  return null;
}
