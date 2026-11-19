import { Component, computed, inject } from '@angular/core';
import { displayName } from '../../../core/game/scenario/scenario-state';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { pileCards, pileTitle } from '../../../core/game/table/game-table';
import { CardComponent } from '../../shared/card/card.component';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { FitCard } from '../../shared/fit-cards/fit-cards';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';
import { GameCardDirective } from '../table/game-card.directive';

/**
 * The piles the player opened from the counters (a discard pile, a dead pile, the adventure deck, the removed pile,
 * the draw deck once the game has ended, a revealed hand), each in a dialog of its own titled "<pile> - <player>",
 * that stays open beside the game until closed. The cards fill it as the legacy groups did.
 *
 * DOM contract: `gd-dialog`s with `data-dialog=pile`, each holding `.gd-game-pile` (the fitCards box) with a
 * `button.gd-card` per card (see GameCardDirective).
 */
@Component({
  selector: 'gd-game-piles',
  styleUrl: '../decision/decision-dialogs.component.css',
  imports: [DialogComponent, CardComponent, FitCardsDirective, GameCardDirective],
  host: { style: 'display: contents' },
  template: `
    @for (dialog of dialogs(); track dialog.key) {
      <gd-dialog
        name="pile"
        [heading]="dialog.title"
        [modal]="false"
        [focusSelf]="true"
        [open]="true"
        (closed)="view.closePile(dialog.pile, dialog.player)"
      >
        <div
          class="gd-game-decision__cards gd-game-pile"
          [gdFitCards]="dialog.fit"
          [gdFitCardsRoundedFrom]="roundedFrom"
        >
          @for (card of dialog.cards; track card.id) {
            <button [gdCard]="views.of(card)" [gdGameCard]="card"></button>
          }
        </div>
      </gd-dialog>
    }
  `,
})
export class PileDialogsComponent {
  private readonly session = inject(GameSession);
  protected readonly view = inject(GameViewState);
  protected readonly views = inject(GameCardViews);
  /** As the decisions' dialog of cards: the legacy group was sized from the content's height in whole pixels. */
  protected readonly roundedFrom = 8 + 13.2 - 2;

  protected readonly dialogs = computed(() => {
    const model = this.session.model();
    return this.view.pileDialogs().map(({ pile, player }) => {
      const cards = pileCards(model, pile, player);
      const own = !model.spectator && player === model.participantId;
      return {
        key: `${pile}:${player}`,
        pile,
        player,
        title: `${pileTitle(pile, own)} - ${displayName(model.scenario, player)}`,
        cards,
        fit: cards.map((card): FitCard => ({ horizontal: this.views.of(card).horizontal })),
      };
    });
  });
}
