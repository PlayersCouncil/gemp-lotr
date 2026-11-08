import { Injectable, signal } from '@angular/core';
import { CardRef } from './card-ref';

/** Whether the card information is shown, and for which card (CardInfoDialog's state). */
@Injectable({ providedIn: 'root' })
export class CardInfoService {
  readonly card = signal<CardRef | null>(null);

  isOpen(): boolean {
    return this.card() !== null;
  }

  show(ref: CardRef): void {
    this.card.set(ref);
  }

  close(): void {
    this.card.set(null);
  }
}
