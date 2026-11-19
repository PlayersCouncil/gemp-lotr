import { inject, Injectable } from '@angular/core';
import { CardActions } from '../../cards/card-actions';
import { ThemeService } from '../../platform/theme.service';
import { GameDecisions } from '../decision/game-decisions.service';
import { GameCardRef } from './game-card-views';

/** What using a card does on the game page: it answers the waiting decision with that card, if it is offered. */
@Injectable()
export class GameCardActions extends CardActions {
  private readonly decisions = inject(GameDecisions);
  private readonly theme = inject(ThemeService);

  activate(ref: unknown, event?: MouseEvent): void {
    const { cardId } = ref as GameCardRef;
    // A tap's menu at the corner where the theme says so (ThemeBehaviour.tapMenuInCorner)
    const corner =
      this.theme.behaviour().tapMenuInCorner && (event as PointerEvent | undefined)?.pointerType === 'touch';
    this.decisions.cardClicked(cardId, corner ? { x: 0, y: 0 } : { x: event?.pageX ?? 0, y: event?.pageY ?? 0 });
  }
}
