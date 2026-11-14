import { Component, computed, inject, input } from '@angular/core';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { TableStack } from '../../../core/game/table/game-table';
import { CardComponent } from '../../shared/card/card.component';
import { FitCard } from '../../shared/fit-cards/fit-cards';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';
import { GameCardLayersComponent } from './game-card-layers.component';
import { GameCardDirective } from './game-card.directive';

/** The share of a card left showing under the one drawn over it: attached, a fifth; stacked, a little more. */
const SHOWN = { attached: 0.2, stacked: 0.22 } as const;

/**
 * A row of the table's cards (a legacy NormalCardGroup): each card with what is attached to it and stacked on it
 * drawn under it, to its left; the cards as high as fits, in as many rows as that takes (fitCards, `rows`).
 *
 * DOM contract: `gd-game-cards` holding `.gd-game-cards__box` (sized by fitCards), in it one `.gd-game-stack` per card,
 * holding its cards as `button.gd-card` (see GameCardDirective for their attributes), the ones under it first.
 */
@Component({
  selector: 'gd-game-cards',
  styleUrl: './game-cards.component.css',
  imports: [CardComponent, FitCardsDirective, GameCardDirective, GameCardLayersComponent],
  template: `
    <div class="gd-game-cards__box" [gdFitCards]="fit()" [gdFitCardsOnPagePixels]="placed()">
      @for (stack of stacks(); track stack.card.id) {
        <div class="gd-game-stack">
          @for (child of stack.under; track child.card.id) {
            <button [gdCard]="views.of(child.card)" [gdGameCard]="child.card" [under]="child.kind">
              <gd-game-card-layers [card]="child.card" />
            </button>
          }
          <button [gdCard]="views.of(stack.card)" [gdGameCard]="stack.card">
            <gd-game-card-layers [card]="stack.card" />
          </button>
        </div>
      }
    </div>
  `,
})
export class GameCardsComponent {
  readonly stacks = input.required<readonly TableStack[]>();
  /** Changes when the row may have moved on the page without changing its size (see gdFitCardsOnPagePixels). */
  readonly placed = input<unknown>(0);

  protected readonly views = inject(GameCardViews);

  protected readonly fit = computed<FitCard[]>(() =>
    this.stacks().map((stack) => ({
      horizontal: this.views.of(stack.card).horizontal,
      behind: stack.under.map((child) => this.views.of(child.card).horizontal),
      behindShown: stack.under.map((child) => SHOWN[child.kind]),
    })),
  );
}
