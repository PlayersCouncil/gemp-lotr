import { computed, inject, Injectable } from '@angular/core';
import { GameDecisions } from '../decision/game-decisions.service';
import { GameSession } from '../session/game-session.service';
import { GameViewState } from '../session/game-view-state';
import { gameTable, moveInRow } from './game-table';

/** The table of the page's game (gameTable), following the game and the order the player gave their cards. */
@Injectable()
export class GameTableState {
  private readonly session = inject(GameSession);
  /** Card ids in the order the player arranged them (kept with the game, for a reload). */
  readonly order = inject(GameViewState).cardOrder;
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

  /** A card of a row dragged to another place in it (`row`: the row's card ids, in their order). */
  move(row: readonly string[], cardId: string, to: number): void {
    const current = this.shown().cardOrder;
    const known = this.order().filter((id) => current.includes(id));
    const full = [...known, ...current.filter((id) => !known.includes(id))];
    this.order.set(moveInRow(full, row, cardId, to));
  }
}
