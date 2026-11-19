import { Injectable, Provider } from '@angular/core';
import { CardActions } from '../core/cards/card-actions';
import { provideCardInteractions } from '../core/cards/card-interactions';

@Injectable()
class IgnoredCardActions extends CardActions {
  activate(): void {
    // Using a card does nothing here
  }
}

/** The card services alone, as a page other than the deck builder would have them. */
export function provideTestCardInteractions(): Provider[] {
  return provideCardInteractions({ actions: IgnoredCardActions, autoZoomCookie: 'autoZoomInTest' });
}
