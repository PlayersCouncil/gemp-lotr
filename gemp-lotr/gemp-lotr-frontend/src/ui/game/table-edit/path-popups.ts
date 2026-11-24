import { Injectable, signal } from '@angular/core';
import { Box } from '../../../core/game/table-edit/popup-place';

/** The site chooser open: the number it lays a site at, where it was opened from, and whether a site is there. */
export interface OpenSiteChooser {
  number: number;
  anchor: Box;
  /** What opened it (the path's open spot, a site): the focus goes back to it. */
  element: HTMLElement | null;
  /** A laid site is being replaced (its menu's Replace…). */
  replacing: boolean;
}

/**
 * Which of the path's edit popups is open: the site chooser (the path's open spot, or a site's Replace…) and the
 * search for a site at a number. Kept apart from EditPopups, whose places are zones, so the two never mix up what is
 * added where. Page-scoped (ui).
 */
@Injectable()
export class PathPopups {
  readonly chooser = signal<OpenSiteChooser | null>(null);
  /** The site number the search lays a site at, and where it was asked for (a refusal is said there). */
  readonly search = signal<{ number: number; anchor: Box } | null>(null);

  openChooser(number: number, element: HTMLElement, replacing = false): void {
    const { left, top, right, bottom } = element.getBoundingClientRect();
    this.openChooserAt(number, { left, top, right, bottom }, element, replacing);
  }

  openChooserAt(number: number, anchor: Box, element: HTMLElement | null, replacing: boolean): void {
    this.search.set(null);
    this.chooser.set({ number, anchor, element, replacing });
  }

  closeChooser(focusBack = false): void {
    const open = this.chooser();
    this.chooser.set(null);
    if (focusBack) open?.element?.focus();
  }

  /** The chooser's Search…: the search window for a site at its number. */
  openSearch(): void {
    const open = this.chooser();
    if (!open) return;
    this.chooser.set(null);
    this.search.set({ number: open.number, anchor: open.anchor });
  }

  closeSearch(): void {
    this.search.set(null);
  }
}
