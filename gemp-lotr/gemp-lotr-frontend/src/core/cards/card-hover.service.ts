import { Injectable, signal } from '@angular/core';
import { CardView } from './card-view';

/** Where a hovered card is on the screen (its bounding box, in viewport px). */
export interface CardBox {
  left: number;
  top: number;
  right: number;
  bottom: number;
}

export interface HoveredCard {
  card: CardView;
  box: CardBox;
  /** Whatever reported it (the card component): its card view can be recomputed while it is hovered. */
  source: object;
}

/**
 * What the mouse is doing to the cards, for whatever shows a hovered card large (the auto-zoom): each card reports
 * the mouse coming onto it, leaving it and pressing a button on it. Only the mouse: touch and pen don't hover.
 */
@Injectable()
export class CardHover {
  /** The card under the mouse, or null. */
  readonly hovered = signal<HoveredCard | null>(null);
  /** Counts mouse presses on cards. */
  readonly presses = signal(0);

  enter(source: object, card: CardView, box: CardBox): void {
    this.hovered.set({ card, box, source });
  }

  leave(source: object): void {
    if (this.hovered()?.source === source) {
      this.hovered.set(null);
    }
  }

  press(): void {
    this.presses.update((n) => n + 1);
  }
}
