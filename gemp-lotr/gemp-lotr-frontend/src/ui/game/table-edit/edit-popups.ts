import { inject, Injectable, signal } from '@angular/core';
import { CardView } from '../../../core/cards/card-view';
import { EditPlace } from '../../../core/game/table-edit/edit-ops';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { CompactLayout } from '../../../core/platform/compact-layout.service';
import { GameCardRef } from '../../../core/game/table/game-card-views';
import { Box } from '../../../core/game/table-edit/popup-place';
import { CardGestures, CardGesturePlace } from '../../shared/card/card-gestures';

const boxOf = (element: Element): Box => {
  const { left, top, right, bottom } = element.getBoundingClientRect();
  return { left, top, right, bottom };
};

/** The chooser open at an open spot: the place it adds to, and where the spot is. */
export interface OpenChooser {
  place: EditPlace;
  anchor: Box;
  /** The spot, to give the focus back to. */
  element: HTMLElement | null;
}

/** The card menu open on a card: which card, where, and where it was asked for (null: from the keyboard). */
export interface OpenMenu {
  cardId: string;
  view: CardView;
  anchor: Box;
  point: { x: number; y: number } | null;
  element: HTMLElement | null;
}

/**
 * Which of the edit controls' popups is open, and where: the chooser of an open spot, a card's menu, the search
 * window (one place at a time). A host (a row of the table, later a pile window) needs nothing but `gd-open-spot`
 * with its place and the page's cards: the spot opens the chooser here, a card's right-click the menu. Page-scoped (ui).
 */
@Injectable()
export class EditPopups {
  readonly chooser = signal<OpenChooser | null>(null);
  readonly menu = signal<OpenMenu | null>(null);
  /** The place the search window adds to, while it is open. */
  readonly search = signal<EditPlace | null>(null);

  openChooser(place: EditPlace, element: HTMLElement): void {
    this.menu.set(null);
    this.chooser.set({ place, anchor: boxOf(element), element });
  }

  closeChooser(focusBack = false): void {
    const open = this.chooser();
    this.chooser.set(null);
    if (focusBack) open?.element?.focus();
  }

  openMenu(cardId: string, view: CardView, place: CardGesturePlace): void {
    this.chooser.set(null);
    this.menu.set({ cardId, view, anchor: boxOf(place.element), point: place.point, element: place.element });
  }

  closeMenu(focusBack = false): void {
    const open = this.menu();
    this.menu.set(null);
    if (focusBack) open?.element?.focus();
  }

  openSearch(place: EditPlace): void {
    this.chooser.set(null);
    this.search.set(place);
  }

  closeSearch(): void {
    this.search.set(null);
  }
}

/**
 * The game page's say in a card's right-click, context-menu key and long press (CardGestures): while the table is
 * being edited by hand, a card of the game opens the edit controls' card menu (whose first entry is the card's
 * information); otherwise, and for any other card (a search's results), and in the phone layout, the information as
 * always. Page-scoped (ui).
 */
@Injectable()
export class TableEditGestures extends CardGestures {
  private readonly edit = inject(TableEdit);
  private readonly popups = inject(EditPopups);
  /** The phone layout has no edit controls (scenarios are not played there): a card's details as always. */
  private readonly phone = inject(CompactLayout).gameActive;

  menu(card: CardView, place: CardGesturePlace): boolean {
    const cardId = (card.ref as Partial<GameCardRef> | null)?.cardId;
    if (!this.edit.editing() || this.phone() || typeof cardId !== 'string') return false;
    // (a puzzle's cards are not changed by hand: only its slots are filled, so a card's details as always)
    if (this.edit.level() === 'puzzle') return false;
    this.popups.openMenu(cardId, card, place);
    return true;
  }
}
