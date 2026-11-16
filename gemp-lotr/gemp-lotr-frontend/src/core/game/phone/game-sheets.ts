import { computed, inject, Injectable } from '@angular/core';
import { GameLogEntry } from '../session/game-log';
import { GameViewState } from '../session/game-view-state';
import { Sheet, SheetScroll } from './sheet';

/**
 * The sheets of the phone layout, one over another (G7 of trymarz/gemp-lotr-private#27): a sheet opened from another
 * (a card's details from a pile, the piles from the menu, the details of an attached card) keeps the one below, and
 * closing it shows that one again as it was, scrolled to the same place (the owner, 2026-11-16). Answering or acting
 * closes them all. Kept with the game (GameViewState), so a reload shows them again (D10). Page-scoped.
 */
@Injectable()
export class GameSheets {
  private readonly stack = inject(GameViewState).sheets;

  readonly entries = this.stack.asReadonly();
  /** The sheet shown, or null. */
  readonly top = computed(() => this.entries().at(-1) ?? null);
  /** Closing the top one goes back to another (the close button says "Back"). */
  readonly canGoBack = computed(() => this.entries().length > 1);

  /** A sheet over the one shown (if any). */
  open(sheet: Sheet): void {
    this.stack.update((stack) => [...stack, { sheet, scroll: { top: 0, rows: [] } }]);
  }

  /** The sheet shown drawn as another (a settings tab), not a new level. */
  replace(sheet: Sheet): void {
    this.stack.update((stack) => [...stack.slice(0, -1), { sheet, scroll: { top: 0, rows: [] } }]);
  }

  /** One level back, or closed when it was the only one. */
  back(): void {
    this.stack.update((stack) => stack.slice(0, -1));
  }

  closeAll(): void {
    if (this.stack().length > 0) this.stack.set([]);
  }

  /** Where the sheet shown is scrolled now (kept for when it is shown again). */
  keepScroll(scroll: SheetScroll): void {
    this.stack.update((stack) => {
      const top = stack.at(-1);
      if (!top) return stack;
      if (top.scroll.top === scroll.top && sameRows(top.scroll.rows, scroll.rows)) return stack;
      return [...stack.slice(0, -1), { ...top, scroll }];
    });
  }
}

function sameRows(a: readonly number[], b: readonly number[]): boolean {
  return a.length === b.length && a.every((n, i) => n === b[i]);
}

/** The lines the chat button counts as unread when not seen: what players said (not the room's own notices). */
export function chatLines(entries: readonly GameLogEntry[]): number {
  return entries.filter((entry) => entry.kind === 'chat' && !entry.message.system).length;
}
