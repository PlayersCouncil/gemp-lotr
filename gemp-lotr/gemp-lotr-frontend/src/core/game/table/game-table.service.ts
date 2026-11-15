import { computed, inject, Injectable, signal } from '@angular/core';
import { GameDecisions } from '../decision/game-decisions.service';
import { GameSession } from '../session/game-session.service';
import { gameTable } from './game-table';

/** The table of the page's game (gameTable), following the game and the order the player gave their cards. */
@Injectable()
export class GameTableState {
  private readonly session = inject(GameSession);
  /** Card ids in the order the player arranged them. */
  readonly order = signal<readonly string[]>([]);
  private readonly decisions = inject(GameDecisions);

  /** The game with the cards the waiting decision adds to the page (cards with an action that lie in no row). */
  readonly shown = computed(() => {
    const model = this.session.model();
    const extra = this.decisions.view().extraCards.filter((card) => !model.cards[card.id]);
    if (extra.length === 0) return model;
    return {
      ...model,
      cards: { ...model.cards, ...Object.fromEntries(extra.map((card) => [card.id, card])) },
      cardOrder: [...model.cardOrder, ...extra.map((card) => card.id)],
    };
  });

  readonly table = computed(() => gameTable(this.shown(), this.order()));
}
