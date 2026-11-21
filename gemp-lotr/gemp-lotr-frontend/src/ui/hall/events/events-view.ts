import { RevealedRow } from '../../../core/hall/events/event-list';

/** The Events tab's small view helpers. */

const revealedSeq = new WeakMap<HTMLElement, number>();

/**
 * EventDrawer.scrollIntoView: the row opened from a link is scrolled to the top of its scroller, smoothly unless
 * the viewer prefers reduced motion. Each reveal once.
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
  const reduced = host.ownerDocument.defaultView?.matchMedia('(prefers-reduced-motion: reduce)').matches ?? true;
  try {
    row.scrollIntoView({ block: 'start', behavior: reduced ? 'auto' : 'smooth' });
  } catch {
    // a page without layout (tests) has nothing to scroll
  }
}
