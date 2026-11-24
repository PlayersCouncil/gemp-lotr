import { Injectable, signal } from '@angular/core';
import { EditCounter } from '../../../core/game/table-edit/edit-counters';
import { Box } from '../../../core/game/table-edit/popup-place';

/** A counter's box open: which counter, by the number it was opened from. */
export interface OpenCounter {
  counter: EditCounter;
  anchor: Box;
  /** The number pressed: the focus goes back to it. */
  element: HTMLElement | null;
}

/**
 * Which counter's box is open (gd-edit-counter opens it, gd-edit-counter-box draws it over the page, in gd-table-edit,
 * so no part of the page it was opened from can be drawn over it). Page-scoped (ui).
 */
@Injectable()
export class CounterPopups {
  readonly open = signal<OpenCounter | null>(null);

  toggle(counter: EditCounter, element: HTMLElement): void {
    if (this.open()?.counter === counter) {
      this.close();
      return;
    }
    const { left, top, right, bottom } = element.getBoundingClientRect();
    this.open.set({ counter, anchor: { left, top, right, bottom }, element });
  }

  close(focusBack = false): void {
    const open = this.open();
    this.open.set(null);
    if (focusBack) open?.element?.focus();
  }
}
