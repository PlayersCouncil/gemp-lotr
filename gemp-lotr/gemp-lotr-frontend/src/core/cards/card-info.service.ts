import { Injectable, signal } from '@angular/core';
import { CardRef } from './card-ref';

/** Whether the card info dialog is open, and its card (CardInfoDialog's state). */
@Injectable({ providedIn: 'root' })
export class CardInfoService {
  readonly card = signal<CardRef | null>(null);
  /** The slider is being used: a click then doesn't close the dialog. */
  sliding = false;

  isOpen(): boolean {
    return this.card() !== null;
  }

  show(ref: CardRef): void {
    this.card.set(ref);
  }

  close(): void {
    this.card.set(null);
  }

  /** A click anywhere on the page closes the dialog, unless the zoom slider was just used (CardInfoDialog.mouseUp). */
  pageClicked(): void {
    if (!this.sliding) {
      this.close();
    }
  }
}
