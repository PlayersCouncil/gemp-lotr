import { DOCUMENT } from '@angular/common';
import { DestroyRef, inject, Injectable } from '@angular/core';
import { ThemeService } from './theme.service';

/** How soon, and how near, a tap after another is the second of a double tap. */
export const DOUBLE_TAP_MS = 500;
export const DOUBLE_TAP_PX = 30;

/** What a tap uses: the control it is on, or what it hit when that isn't one. */
const CONTROL = 'button, a[href], input, select, textarea, label, summary, [role=separator], .gd-card';

/**
 * On a touch screen, in a theme that wants it (ThemeBehaviour.guardSecondTap), the second tap of a double tap is
 * dropped when it lands on another control than the first: the first closed a menu or a dialog, or opened one, and what
 * is now under the finger isn't what the player tapped (a double tap on a menu item would also press what the menu
 * covered; one on a selection pack would choose in the dialog it opened). A double tap on one control still presses
 * it twice, as a double click does, except a control marked `data-tap-once` (a pack: a double tap opens one, where the
 * legacy page opened one, two or three depending on timing). The mouse and the keyboard are left alone.
 */
@Injectable({ providedIn: 'root' })
export class SecondTapGuard {
  private readonly behaviour = inject(ThemeService).behaviour;
  private readonly window = inject(DOCUMENT).defaultView;
  private last: { time: number; x: number; y: number; control: Element | null } | null = null;

  private readonly destroyRef = inject(DestroyRef);

  /** Starts listening, before the page's own handlers (in the capture phase of the window), until the app ends. */
  start(): void {
    const listener = (event: MouseEvent) => this.clicked(event);
    this.window?.addEventListener('click', listener, true);
    this.destroyRef.onDestroy(() => this.window?.removeEventListener('click', listener, true));
  }

  private clicked(event: MouseEvent): void {
    // Clicks of the mouse or the keyboard, or passed on by the page itself, aren't taps
    if (!this.behaviour().guardSecondTap || (event as PointerEvent).pointerType !== 'touch') {
      return;
    }
    const target = event.target instanceof Element ? event.target : null;
    // A label and the box in it are one control: a tap on the label also clicks the box
    const control = target?.closest('label') ?? target?.closest(CONTROL) ?? target;
    const last = this.last;
    this.last = { time: event.timeStamp, x: event.clientX, y: event.clientY, control };
    if (
      last &&
      event.timeStamp - last.time <= DOUBLE_TAP_MS &&
      Math.hypot(event.clientX - last.x, event.clientY - last.y) <= DOUBLE_TAP_PX &&
      (control !== last.control || !!control?.closest('[data-tap-once]'))
    ) {
      event.preventDefault();
      event.stopImmediatePropagation();
    }
  }
}
