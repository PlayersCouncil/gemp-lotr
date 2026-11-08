import { Injectable } from '@angular/core';
import { CardRef } from '../../core/cards/card-ref';

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
