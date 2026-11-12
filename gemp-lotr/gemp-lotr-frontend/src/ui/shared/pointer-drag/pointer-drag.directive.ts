import { DestroyRef, Directive, ElementRef, inject, input } from '@angular/core';

/** Keeps a pointer's events on the element, also outside it (where the browser can: jsdom has no capture). */
export function capturePointer(element: Element, pointerId: number): void {
  if ('setPointerCapture' in element) {
    element.setPointerCapture(pointerId);
  }
}

/** What a drag does. */
export interface DragHandlers {
  move: (event: PointerEvent) => void;
  /** The pointer was released. */
  end: (event: PointerEvent) => void;
  /** The browser took the pointer away; `end` runs when this is left out. */
  cancel?: (event: PointerEvent) => void;
}

/**
 * Dragging with the main mouse button or a finger: `[gdPointerDrag]` gets a function called for each press, which
 * returns what this drag does, or null when the press shouldn't start one. The pointer is captured, so the drag goes
 * on outside the element, and everything is cleaned up afterwards, also if the element goes away.
 */
@Directive({
  selector: '[gdPointerDrag]',
  host: { '(pointerdown)': 'press($event)' },
})
export class PointerDragDirective {
  readonly gdPointerDrag = input.required<(event: PointerEvent) => DragHandlers | null>();

  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private stopListening: (() => void) | null = null;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.stopListening?.());
  }

  protected press(event: PointerEvent): void {
    if (event.button !== 0) {
      return;
    }
    const handlers = this.gdPointerDrag()(event);
    if (!handlers) {
      return;
    }
    event.preventDefault();
    capturePointer(this.element, event.pointerId);
    this.stopListening?.();
    const finishWith = (handler: (e: PointerEvent) => void) => (e: PointerEvent) => {
      this.stopListening?.();
      handler(e);
    };
    const listeners: [string, (e: PointerEvent) => void][] = [
      // Firefox still starts selecting text with the mouse after a cancelled pointerdown (Chromium doesn't), as the
      // legacy page's drags did not
      ['mousedown', (e) => e.preventDefault()],
      ['pointermove', (e) => handlers.move(e)],
      ['pointerup', finishWith(handlers.end)],
      ['pointercancel', finishWith(handlers.cancel ?? handlers.end)],
    ];
    for (const [type, listener] of listeners) {
      this.element.addEventListener(type, listener as EventListener);
    }
    this.stopListening = () => {
      for (const [type, listener] of listeners) {
        this.element.removeEventListener(type, listener as EventListener);
      }
      this.stopListening = null;
    };
  }
}
