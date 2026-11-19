import { DestroyRef, Directive, ElementRef, inject, output } from '@angular/core';

/** How long scrolling must have stopped before it counts as settled (ms). */
export const SCROLL_SETTLE_MS = 150;

/**
 * Tells when scrolling has stopped, on the element or anything in it that scrolls (scroll events don't bubble: they
 * are caught on their way down): `(gdScrollSettled)` once nothing has scrolled for a moment, so where things are
 * scrolled is kept once a swipe ends rather than on every frame of it.
 */
@Directive({ selector: '[gdScrollSettled]' })
export class ScrollSettledDirective {
  readonly gdScrollSettled = output();

  constructor() {
    const host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
    let timer: ReturnType<typeof setTimeout> | null = null;
    const scrolled = () => {
      if (timer !== null) clearTimeout(timer);
      timer = setTimeout(() => {
        timer = null;
        this.gdScrollSettled.emit();
      }, SCROLL_SETTLE_MS);
    };
    host.addEventListener('scroll', scrolled, { capture: true, passive: true });
    inject(DestroyRef).onDestroy(() => {
      host.removeEventListener('scroll', scrolled, true);
      if (timer !== null) clearTimeout(timer);
    });
  }
}
