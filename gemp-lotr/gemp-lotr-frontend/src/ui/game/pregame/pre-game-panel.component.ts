import { Component, computed, inject, input } from '@angular/core';
import { CardView } from '../../../core/cards/card-view';
import { CardViews } from '../../../core/cards/card-views.service';
import { PreGameInfo } from '../../../core/game/state/game-model';
import { PreGameCard, preGameView } from '../../../core/game/table/pre-game';
import { CardComponent } from '../../shared/card/card.component';

/**
 * What the page shows until the game starts: the table's summary (format, timer) with two reminders, then "you" and
 * "them": each name, the cards shown beforehand (a map, meta-sites) and, for the player, the deck's notes.
 *
 * DOM contract: `gd-game-pregame` holding `.gd-game-pregame__summary` and `.gd-game-pregame__sides` with two
 * `.gd-game-pregame__side` (`data-side` left, right), each a `.gd-game-pregame__name` and a
 * `.gd-game-pregame__content`, with `.gd-game-pregame__versus` between them.
 */
@Component({
  selector: 'gd-game-pregame',
  styleUrl: './pre-game-panel.component.css',
  imports: [CardComponent],
  template: `
    <div class="gd-game-pregame__summary" [innerHTML]="view().summary"></div>
    <div class="gd-game-pregame__sides">
      <div class="gd-game-pregame__side" data-side="left">
        <div class="gd-game-pregame__name">{{ view().left.name }}</div>
        <div class="gd-game-pregame__content">
          @if (leftCards().length > 0) {
            <div class="gd-game-pregame__cards">
              @for (card of leftCards(); track $index) {
                <button [gdCard]="card"></button>
              }
            </div>
          }
          @if (view().left.notes !== '') {
            <div [innerHTML]="view().left.notes"></div>
          }
        </div>
      </div>
      <div class="gd-game-pregame__versus">VS</div>
      <div class="gd-game-pregame__side" data-side="right">
        <div class="gd-game-pregame__name">{{ view().right.name }}</div>
        <div class="gd-game-pregame__content">
          @if (rightCards().length > 0) {
            <div class="gd-game-pregame__cards">
              @for (card of rightCards(); track $index) {
                <button [gdCard]="card"></button>
              }
            </div>
          }
        </div>
      </div>
    </div>
  `,
})
export class PreGamePanelComponent {
  readonly info = input.required<PreGameInfo>();

  private readonly views = inject(CardViews);

  protected readonly view = computed(() => preGameView(this.info()));
  protected readonly leftCards = computed(() => this.view().left.cards.map((card) => this.cardView(card)));
  protected readonly rightCards = computed(() => this.view().right.cards.map((card) => this.cardView(card)));

  private cardView(card: PreGameCard): CardView {
    return this.views.create({
      ref: null,
      blueprintId: card.blueprintId,
      zone: 'SPECIAL',
      border: false,
      overlayBlueprintId: card.overlayBlueprintId,
    });
  }
}
