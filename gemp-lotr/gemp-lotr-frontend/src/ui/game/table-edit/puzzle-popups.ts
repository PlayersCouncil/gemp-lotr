import { Injectable, signal } from '@angular/core';
import { Box } from '../../../core/game/table-edit/popup-place';

/** The chooser open at a puzzle's slot: which slot, where it is, and the element to give the focus back to. */
export interface OpenSlotChooser {
  slot: string;
  anchor: Box;
  element: HTMLElement | null;
}

/**
 * Which of the puzzle page's popups is open: the chooser of a slot (Recent, In this scenario, Search…, Empty), or the
 * search window for a slot (one at a time). The slots themselves are their hosts' (a row of the table, the slots
 * strip). Page-scoped (ui), beside EditPopups.
 */
@Injectable()
export class PuzzlePopups {
  readonly chooser = signal<OpenSlotChooser | null>(null);
  /** The slot the search window fills, while it is open. */
  readonly search = signal<string | null>(null);

  openChooser(slot: string, element: HTMLElement): void {
    const { left, top, right, bottom } = element.getBoundingClientRect();
    this.chooser.set({ slot, anchor: { left, top, right, bottom }, element });
  }

  closeChooser(focusBack = false): void {
    const open = this.chooser();
    this.chooser.set(null);
    if (focusBack) open?.element?.focus();
  }

  openSearch(slot: string): void {
    this.chooser.set(null);
    this.search.set(slot);
  }

  closeSearch(): void {
    this.search.set(null);
  }
}
