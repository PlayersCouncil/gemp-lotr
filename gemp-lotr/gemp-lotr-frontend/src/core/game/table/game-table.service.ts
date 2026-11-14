import { computed, inject, Injectable, signal } from '@angular/core';
import { GameSession } from '../session/game-session.service';
import { gameTable } from './game-table';

/** The table of the page's game (gameTable), following the game and the order the player gave their cards. */
@Injectable()
export class GameTableState {
  private readonly session = inject(GameSession);
  /** Card ids in the order the player arranged them. */
  readonly order = signal<readonly string[]>([]);
  readonly table = computed(() => gameTable(this.session.model(), this.order()));
}
