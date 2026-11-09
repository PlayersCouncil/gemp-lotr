import { Injectable, signal } from '@angular/core';
import { CardRef } from './card-ref';

/** Whether the card information is shown, and for which card (CardInfoDialog's state). */
@Injectable({ providedIn: 'root' })
export class CardInfoService {
  readonly card = signal<CardRef | null>(null);
  /** Shown by dragging a card up: the click that ends that drag doesn't close it. */
  private dragRelease = false;

  isOpen(): boolean {
    return this.card() !== null;
  }

  show(ref: CardRef): void {
    this.card.set(ref);
  }

  showDragged(ref: CardRef): void {
    this.dragRelease = true;
    this.card.set(ref);
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
