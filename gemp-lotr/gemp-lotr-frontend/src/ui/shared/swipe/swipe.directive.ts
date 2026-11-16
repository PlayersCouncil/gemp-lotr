import { Directive, input, output } from '@angular/core';

/** How far (px) a finger must move sideways for a swipe. */
export const SWIPE_MIN_PX = 50;
/** How much more a swipe must move sideways than up or down (a steeper one is a scroll). */
export const SWIPE_SLOPE = 2;
/** How long (ms) a swipe may take: a slower move is a drag or a look, not a swipe. */
export const SWIPE_MAX_MS = 800;

/** Where a swipe can't start: fields and sliders move with the finger themselves, and dialogs are another view. */
const NOT_FROM = 'input, select, textarea, dialog, [role=separator]';

/**
 * A quick sideways swipe of a finger (or a pen) on the element: `swipeLeft` when it moves to the left, `swipeRight`
 * to the right. The mouse is left alone. Nothing is cancelled, so scrolling, taps and long presses work as before;
 * the element's styles should let the browser keep sideways moves to the page (`touch-action: pan-y`), or it takes
 * them for itself and the swipe never ends here. Off while `gdSwipeEnabled` is false.
 */
@Directive({
  selector: '[gdSwipe]',
  host: {
    '(pointerdown)': 'down($event)',
    '(pointerup)': 'up($event)',
    '(pointercancel)': 'start = null',
  },
})
export class SwipeDirective {
  readonly gdSwipeEnabled = input(true);
  readonly swipeLeft = output();
  readonly swipeRight = output();

  protected start: { id: number; x: number; y: number; time: number } | null = null;

  protected down(event: PointerEvent): void {
    const target = event.target instanceof Element ? event.target : null;
    const fromControl = !!target?.closest(NOT_FROM);
    this.start =
      this.gdSwipeEnabled() && event.pointerType !== 'mouse' && !fromControl
        ? { id: event.pointerId, x: event.clientX, y: event.clientY, time: event.timeStamp }
        : null;
  }

  protected up(event: PointerEvent): void {
    const start = this.start;
    this.start = null;
    if (start === null) {
      return;
    }
    if (start.id !== event.pointerId || event.timeStamp - start.time > SWIPE_MAX_MS) {
      return;
    }
    const dx = event.clientX - start.x;
    const dy = event.clientY - start.y;
    if (Math.abs(dx) < SWIPE_MIN_PX || Math.abs(dx) < SWIPE_SLOPE * Math.abs(dy)) {
      return;
    }
    if (dx < 0) {
      this.swipeLeft.emit();
    } else {
      this.swipeRight.emit();
    }
  }
}
