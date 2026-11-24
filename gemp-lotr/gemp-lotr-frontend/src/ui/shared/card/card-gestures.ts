import { CardView } from '../../../core/cards/card-view';

/** Where on the page a card's menu was asked for: the card, and the pointer (null: from the keyboard). */
export interface CardGesturePlace {
  element: HTMLElement;
  point: { x: number; y: number } | null;
}

/**
 * A page's say in the gesture that asks for a card's details (right-click, the context-menu key, a long press): a page
 * that offers a menu of its own for some cards at some moments takes the gesture (the game page while its table is
 * being edited by hand: the edit controls' card menu, whose first entry is the details). Optional: without it, or when
 * it does not take the gesture, the card's details are shown as always. Shift-click always shows the details.
 */
export abstract class CardGestures {
  /** True when the page has taken the gesture (and opened its menu). */
  abstract menu(card: CardView, place: CardGesturePlace): boolean;
}
