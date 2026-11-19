import { Directive, ElementRef, inject, input } from '@angular/core';
import { GameTableState } from '../../../core/game/table/game-table.service';

/** How far (px) the mouse must move sideways before a pressed card is being moved (the legacy page's 20px). */
export const REORDER_START = 20;

/**
 * Dragging a card of a row sideways with the mouse moves it in the row, a place per card width (the legacy page's
 * dragContinuesCardFunction). The click that ends such a drag does nothing else. On the row's box: `gdReorder` is the
 * row's card ids, in their order.
 */
@Directive({
  selector: '[gdReorder]',
  host: {
    '(pointerdown)': 'pressed($event)',
    '(pointermove)': 'moved($event)',
    '(pointerup)': 'released()',
    '(pointercancel)': 'released()',
    '(click)': 'clicked($event)',
  },
})
export class ReorderDirective {
  readonly gdReorder = input.required<readonly string[]>();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly table = inject(GameTableState);
  private press: { cardId: string; x: number; index: number; width: number; moving: boolean } | null = null;
  private moved_ = false;

  protected pressed(event: PointerEvent): void {
    this.moved_ = false;
    if (event.pointerType !== 'mouse' || event.button !== 0) return;
    const card = event.target instanceof Element ? event.target.closest<HTMLElement>('[data-card-id]') : null;
    const cardId = card?.dataset['cardId'];
    const index = cardId === undefined ? -1 : this.gdReorder().indexOf(cardId);
    if (!card || cardId === undefined || index < 0) return;
    this.press = { cardId, x: event.clientX, index, width: card.getBoundingClientRect().width, moving: false };
  }

  protected moved(event: PointerEvent): void {
    const press = this.press;
    if (!press || press.width <= 0) return;
    const dx = event.clientX - press.x;
    if (!press.moving && Math.abs(dx) < REORDER_START) return;
    press.moving = true;
    // A drag up at its end shows no card information (the legacy draggingHorizontaly)
    this.host.toggleAttribute('data-dragging', true);
    this.moved_ = true;
    const row = this.gdReorder();
    const steps = Math.floor(Math.abs(dx) / press.width);
    const to = Math.max(0, Math.min(row.length - 1, press.index + (dx < 0 ? -steps : steps)));
    if (row[to] !== press.cardId) this.table.move(row, press.cardId, to);
  }

  protected released(): void {
    this.press = null;
    this.host.removeAttribute('data-dragging');
  }

  /** The click ending a drag isn't a click on the card. */
  protected clicked(event: MouseEvent): void {
    if (this.moved_) {
      this.moved_ = false;
      event.stopPropagation();
      event.preventDefault();
    }
  }
}
