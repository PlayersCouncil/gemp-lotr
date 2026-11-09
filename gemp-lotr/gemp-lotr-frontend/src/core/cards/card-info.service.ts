import { Injectable, signal } from '@angular/core';
import { CardView } from './card-view';

/** Whether the card information is shown, and for which card (CardInfoDialog's state). */
@Injectable()
export class CardInfoService {
  readonly card = signal<CardView | null>(null);
  /** Shown by dragging a card up: the click that ends that drag doesn't close it. */
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

  /** Whether this click is the one ending a drag up (it counts only once). */
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
