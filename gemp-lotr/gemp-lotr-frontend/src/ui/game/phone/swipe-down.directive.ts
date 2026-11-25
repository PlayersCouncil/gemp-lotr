import { Directive, input, output } from '@angular/core';

/** How far down (px) a finger moves for a swipe, and how much further down than sideways. */
export const SWIPE_DOWN_MIN = 70;
const MOSTLY_DOWN = 1.6;
/** A swipe is quick: a slow drag is the player reading or scrolling. */
const SWIPE_MAX_MS = 800;

/**
 * A quick swipe down with one finger (`gdSwipedDown`), while `gdSwipeDown` is on: the phone layout's card details,
 * settings and menu go back or close with it, as with their corner button (the owner, 2026-11-18). It counts only when the sheet's body (the element named
 * by `gdSwipeDownScroller`, inside the host) was scrolled to its top when the finger came down, so a swipe that scrolls
 * a long text back up stays a scroll. Touch events, not pointer events: the browser cancels a pointer that pans.
 */
@Directive({
  selector: '[gdSwipeDown]',
  host: {
    '(touchstart)': 'start($event)',
    '(touchend)': 'end($event)',
    '(touchcancel)': 'cancel()',
  },
})
export class SwipeDownDirective {
  readonly enabled = input(false, { alias: 'gdSwipeDown' });
  /** A selector for the scrolling part: the swipe counts only while it is at its top. */
  readonly scroller = input<string | null>(null, { alias: 'gdSwipeDownScroller' });
  readonly gdSwipedDown = output();

  private from: { x: number; y: number; at: number } | null = null;

  protected start(event: TouchEvent): void {
    this.from = null;
    const touch = event.touches[0];
    if (!this.enabled() || event.touches.length !== 1 || !touch) return;
    const selector = this.scroller();
    const target = event.target instanceof Element ? event.target : null;
    const scroller = selector ? target?.closest<HTMLElement>(selector) : null;
    if (scroller && scroller.scrollTop > 0) return;
    this.from = { x: touch.clientX, y: touch.clientY, at: event.timeStamp };
  }

  protected end(event: TouchEvent): void {
    const from = this.from;
    this.from = null;
    const touch = event.changedTouches[0];
    if (!from || !touch || !this.enabled()) return;
    const down = touch.clientY - from.y;
    const sideways = Math.abs(touch.clientX - from.x);
    if (down >= SWIPE_DOWN_MIN && down >= sideways * MOSTLY_DOWN && event.timeStamp - from.at <= SWIPE_MAX_MS) {
      this.gdSwipedDown.emit();
    }
  }

  protected cancel(): void {
    this.from = null;
  }
}
