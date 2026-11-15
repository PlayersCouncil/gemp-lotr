import { inject, Injectable } from '@angular/core';
import { CardActions } from '../../cards/card-actions';
import { GameDecisions } from '../decision/game-decisions.service';
import { GameCardRef } from './game-card-views';

/** What using a card does on the game page: it answers the waiting decision with that card, if it is offered. */
@Injectable()
export class GameCardActions extends CardActions {
  private readonly decisions = inject(GameDecisions);

  activate(ref: unknown, event?: MouseEvent): void {
    const { cardId } = ref as GameCardRef;
    this.decisions.cardClicked(cardId, { x: event?.pageX ?? 0, y: event?.pageY ?? 0 });
  }
}
