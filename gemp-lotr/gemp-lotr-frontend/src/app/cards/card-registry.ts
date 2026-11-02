import { Injectable } from '@angular/core';
import { CollectionItem } from '../core/api/models';
import { DeckCard, DeckSlot, DrawCopy } from '../core/deck/deck-model';

/**
 * What a card element on the page stands for (the legacy code kept this as jQuery data "card" on the element), so
 * page-wide mouse handling can tell what was clicked.
 */
export type CardRef =
  /** A collection item; `role` is its class (cardInCollection, packInCollection, ...). */
  | { kind: 'collection'; role: string; blueprintId: string; item: CollectionItem }
  /** A card in a special slot or the sites. */
  | { kind: 'slot'; slot: DeckSlot; blueprintId: string; card: DeckCard }
  /** The shown card of a draw deck pile entry. */
  | { kind: 'draw'; blueprintId: string; card: DrawCopy }
  /** A copy drawn behind a draw deck card; clicking it does nothing, as before. */
  | { kind: 'behind'; blueprintId: string; card: DrawCopy }
  /** The Race to Mount Doom meta-site (display only). */
  | { kind: 'race'; blueprintId: string };

/** Card elements and what they stand for. Cards register themselves (gempCard's cardRef input). */
@Injectable({ providedIn: 'root' })
export class CardRegistry {
  private readonly refs = new WeakMap<Element, CardRef>();

  set(element: Element, ref: CardRef | null): void {
    if (ref) {
      this.refs.set(element, ref);
    } else {
      this.refs.delete(element);
    }
  }

  get(element: Element | null): CardRef | null {
    return (element && this.refs.get(element)) ?? null;
  }
}
