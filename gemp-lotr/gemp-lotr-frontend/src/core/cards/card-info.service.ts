import { Injectable, signal } from '@angular/core';
import { CardView } from './card-view';

/** Whether the card information is shown, and for which card (CardInfoDialog's state). */
@Injectable()
export class CardInfoService {
  readonly card = signal<CardView | null>(null);
  /**
   * What the page says about the card beside it (HTML; a game's modifiers on the card), or null for nothing. The view
   * sanitizes it.
   */
  readonly details = signal<string | null>(null);
  /** The title the page gives the card ("Gandalf, The Grey Wizard (original)"), or null for "Card Information". */
  readonly title = signal<string | null>(null);
  /**
   * Shown by a gesture (dragging a card up, or a long press on a touch screen): the click that ends it, and the context
   * menu a touch browser sends for a long press, don't close it.
   */
  private dragRelease = false;

  isOpen(): boolean {
    return this.card() !== null;
  }

  /** `title`: what the dialog is headed (the legacy hall's card preview named the card); default "Card Information". */
  show(card: CardView, title: string | null = null): void {
    this.details.set(null);
    this.title.set(title);
    this.card.set(card);
  }

  showDragged(card: CardView): void {
    this.dragRelease = true;
    this.details.set(null);
    this.title.set(null);
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
    this.details.set(null);
    this.title.set(null);
  }
}
