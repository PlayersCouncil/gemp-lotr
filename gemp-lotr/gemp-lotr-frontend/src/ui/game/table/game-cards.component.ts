import { Component, computed, inject, input } from '@angular/core';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { TableStack } from '../../../core/game/table/game-table';
import { EditPlace } from '../../../core/game/table-edit/edit-ops';
import { OpenSpotComponent } from '../table-edit/open-spot.component';
import { CardComponent } from '../../shared/card/card.component';
import { FitCard } from '../../shared/fit-cards/fit-cards';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';
import { GameCardLayersComponent } from './game-card-layers.component';
import { GameCardDirective } from './game-card.directive';
import { ReorderDirective } from './reorder.directive';
import { FilledSlot } from '../../../core/game/table-edit/puzzle-backend';
import { PuzzleSlotComponent } from '../table-edit/puzzle-slot.component';

/** The share of a card left showing under the one drawn over it: attached, a fifth; stacked, a little more. */
const SHOWN = { attached: 0.2, stacked: 0.22 } as const;

/**
 * A row of the table's cards (a legacy NormalCardGroup): each card with what is attached to it and stacked on it
 * drawn under it, to its left; the cards as high as fits, in as many rows as that takes (fitCards, `rows`).
 *
 * While the table is edited by hand, a row that can take a card ends with an open spot (`spot`: where a card chosen
 * there goes), sized as its cards.
 *
 * DOM contract: `gd-game-cards` holding `.gd-game-cards__box` (sized by fitCards), in it one `.gd-game-stack` per card,
 * holding its cards as `button.gd-card` (see GameCardDirective for their attributes), the ones under it first; then,
 * with a `spot`, `.gd-game-stack[data-open-spot]` holding `gd-open-spot`; then a `.gd-game-stack[data-puzzle-slot]`
 * holding `gd-puzzle-slot` per slot of a puzzle's in the row (`slots`).
 */
@Component({
  selector: 'gd-game-cards',
  styleUrl: './game-cards.component.css',
  imports: [
    CardComponent,
    FitCardsDirective,
    GameCardDirective,
    GameCardLayersComponent,
    ReorderDirective,
    OpenSpotComponent,
    PuzzleSlotComponent,
  ],
  template: `
    <div class="gd-game-cards__box" [gdFitCards]="fit()" [gdFitCardsOnPagePixels]="placed()" [gdReorder]="ids()">
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
      @if (spot(); as where) {
        <div class="gd-game-stack" data-open-spot>
          <gd-open-spot [where]="where" />
        </div>
      }
      @for (filled of slots(); track filled.slot.id) {
        <div class="gd-game-stack" data-puzzle-slot>
          <gd-puzzle-slot [slot]="filled" />
        </div>
      }
    </div>
  `,
})
export class GameCardsComponent {
  readonly stacks = input.required<readonly TableStack[]>();
  /** Changes when the row may have moved on the page without changing its size (see gdFitCardsOnPagePixels). */
  readonly placed = input<unknown>(0);
  /** Where a card added at the row's open spot goes; null: no open spot. */
  readonly spot = input<EditPlace | null>(null);
  /** A puzzle's slots in this row, while they are being filled (each sized as an upright card). */
  readonly slots = input<readonly FilledSlot[]>([]);

  protected readonly views = inject(GameCardViews);

  /** The row's cards (the ones others lie under), in their order: what dragging reorders. */
  protected readonly ids = computed(() => this.stacks().map((stack) => stack.card.id));

  protected readonly fit = computed<FitCard[]>(() => [
    ...this.stacks().map((stack) => ({
      horizontal: this.views.of(stack.card).horizontal,
      behind: stack.under.map((child) => this.views.of(child.card).horizontal),
      behindShown: stack.under.map((child) => SHOWN[child.kind]),
    })),
    // (the open spot is the size of an upright card)
    ...(this.spot() ? [{ horizontal: false, behind: [], behindShown: [] }] : []),
    ...this.slots().map(() => ({ horizontal: false, behind: [], behindShown: [] })),
  ]);
}
