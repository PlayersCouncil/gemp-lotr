import { Injectable } from '@angular/core';
import { CardActions } from '../../cards/card-actions';

/**
 * What using a card does on the game page: it answers the waiting decision with that card (G3 of
 * trymarz/gemp-lotr-private#27). Until then a card only shows its information.
 */
@Injectable()
export class GameCardActions extends CardActions {
  activate(): void {
    // Decisions come with G3
  }
}
