import { DOCUMENT } from '@angular/common';
import { DestroyRef, Directive, inject, output } from '@angular/core';

const PRESS_EVENTS = ['pointerdown', 'mousedown', 'click', 'contextmenu'] as const;

/**
 * Reports every press (pointerdown, when it starts, also with a finger), mouse press, click and right-click anywhere on
 * the page, for as long as the element exists. It hears
 * them before whatever was pressed does, so a handler can see the state before that acts, or stop the event reaching it.
 */
@Directive({ selector: '[gdDocumentPress]' })
export class DocumentPressDirective {
  readonly gdDocumentPress = output<MouseEvent>();

  constructor() {
    const document = inject(DOCUMENT);
    const listener = (event: Event) => this.gdDocumentPress.emit(event as MouseEvent);
    for (const type of PRESS_EVENTS) {
      document.addEventListener(type, listener, true);
    }
    inject(DestroyRef).onDestroy(() => {
      for (const type of PRESS_EVENTS) {
        document.removeEventListener(type, listener, true);
      }
    });
  }
}
