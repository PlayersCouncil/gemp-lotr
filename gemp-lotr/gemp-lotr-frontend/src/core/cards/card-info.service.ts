import { Injectable, signal } from '@angular/core';
import { CardView } from './card-view';

/** Whether the card information is shown, and for which card (CardInfoDialog's state). */
@Injectable()
export class CardInfoService {
  readonly card = signal<CardView | null>(null);
  /**
   * Shown by a gesture (dragging a card up, or a long press on a touch screen): the click that ends it, and the context
   * menu a touch browser sends for a long press, don't close it.
   */
  private dragRelease = false;

  isOpen(): boolean {
    return this.card() !== null;
  }

  show(card: CardView): void {
    this.card.set(card);
  }

  showDragged(card: CardView): void {
    this.dragRelease = true;
    this.card.set(card);
  }

  /** Whether the gesture that showed it hasn't ended yet. */
  awaitsDragRelease(): boolean {
    return this.dragRelease;
  }

  /** Whether this click is the one ending the gesture (it counts only once). */
  takeDragRelease(): boolean {
    const release = this.dragRelease;
    this.dragRelease = false;
    return release;
  }

  /** A new press started, so the last drag is over. */
  forgetDragRelease(): void {
    this.dragRelease = false;
  }

  close(): void {
    this.card.set(null);
  }
}
