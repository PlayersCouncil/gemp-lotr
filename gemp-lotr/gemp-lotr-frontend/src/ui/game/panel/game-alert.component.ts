import { Component, computed, inject } from '@angular/core';
import { DecisionButton, mainButton } from '../../../core/game/decision/decision-view';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { GameUndo } from '../../../core/game/undo/game-undo.service';
import { UNDO_BUTTON } from '../../../core/game/undo/undo-view';
import { CardHintsDirective } from '../table/card-hints.directive';

/** The legacy ids of the alert box's buttons. */
const BUTTON_IDS: Readonly<Record<DecisionButton, string>> = {
  OK: 'OK',
  Pass: 'Pass',
  Done: 'Done',
  'Reset choice': 'ClearSelection',
  'Clear selection': 'ClearSelection',
};

/**
 * The alert box: what the game asks the player (the waiting decision's text) and the buttons to answer with, framed
 * in the colour of the kind of choice (cards to choose, actions to take). A decision that asks in a dialog leaves it
 * empty. In its lower right corner, while the player's last choice is still theirs to take back, the Undo button
 * (GameUndo), also when nothing is asked; the buttons on the left make room for it. While a revert request waits the
 * buttons can't be pressed (dimmed).
 *
 * DOM contract: `gd-game-alert` with `data-frame` (action, selection), `data-undo` while the Undo button is shown,
 * holding `[data-game="alert"]` (the text) and `[data-game="alert-buttons"]` (`inert` and `data-frozen` while a revert
 * request waits) with a `button[data-decision-button]` per button (the button's text; `data-button-id` the legacy id;
 * `data-right` on a Done that stands at the right); then `button[data-undo=last]` ("✓ Undo", its `title` what it
 * undoes or why it can't, disabled then and while a request waits).
 */
@Component({
  selector: 'gd-game-alert',
  styleUrl: './game-alert.component.css',
  imports: [CardHintsDirective],
  host: { '[attr.data-frame]': 'decisions.view().frame', '[attr.data-undo]': "undo.button() ? '' : null" },
  template: `
    <div class="gd-game-alert__text" data-game="alert" gdCardHints [innerHTML]="decisions.view().alert"></div>
    <div
      class="gd-game-alert__buttons"
      data-game="alert-buttons"
      [attr.inert]="undo.frozen() ? '' : null"
      [attr.data-frozen]="undo.frozen() ? '' : null"
    >
      @for (button of decisions.view().buttons; track button) {
        <button
          type="button"
          class="gd-button gd-game-alert__button"
          [attr.data-decision-button]="button"
          [attr.data-button-id]="ids[button]"
          [attr.data-right]="button === 'Done' && decisions.view().doneOnRight ? '' : null"
          [attr.data-main]="button === main() ? '' : null"
          (click)="decisions.press(button)"
        >
          <span>{{ button }}</span>
        </button>
      }
    </div>
    @if (undo.button(); as button) {
      <button
        type="button"
        class="gd-button gd-game-alert__button gd-game-alert__undo"
        data-undo="last"
        [title]="button.title"
        [disabled]="!button.enabled"
        (click)="undo.undoLatest()"
      >
        <span>{{ undoLabel }}</span>
      </button>
    }
  `,
})
export class GameAlertComponent {
  protected readonly decisions = inject(GameDecisions);
  protected readonly undo = inject(GameUndo);
  protected readonly undoLabel = UNDO_BUTTON;
  /** The button that goes on with the game (mainButton): the modern theme's gold pill. */
  protected readonly main = computed(() => mainButton(this.decisions.view().buttons));
  protected readonly ids = BUTTON_IDS;
}
