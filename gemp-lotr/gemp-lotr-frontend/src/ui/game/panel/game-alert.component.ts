import { Component, computed, inject } from '@angular/core';
import { DecisionButton, mainButton } from '../../../core/game/decision/decision-view';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { GameEnding } from '../../../core/game/session/game-ending';
import { ThemeService } from '../../../core/platform/theme.service';
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
 * empty. Once the game has ended, the modern theme has a button here that shows its end again (the result and the
 * replay; the owner, 2026-11-20); classic keeps game.html's empty box.
 *
 * DOM contract: `gd-game-alert` with `data-frame` (action, selection), holding `[data-game="alert"]` (the text) and
 * `[data-game="alert-buttons"]` with a `button[data-decision-button]` per button (the button's text; `data-button-id`
 * the legacy id; `data-right` on a Done that stands at the right), and `button[data-game="ended"]` once it ended.
 */
@Component({
  selector: 'gd-game-alert',
  styleUrl: './game-alert.component.css',
  imports: [CardHintsDirective],
  host: { '[attr.data-frame]': 'decisions.view().frame' },
  template: `
    <div class="gd-game-alert__text" data-game="alert" gdCardHints [innerHTML]="decisions.view().alert"></div>
    <div class="gd-game-alert__buttons" data-game="alert-buttons">
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
      @if (showEnd()) {
        <button
          type="button"
          class="gd-button gd-game-alert__button"
          data-game="ended"
          data-main
          (click)="ending.show()"
        >
          <span>Result and replay</span>
        </button>
      }
    </div>
  `,
})
export class GameAlertComponent {
  protected readonly decisions = inject(GameDecisions);
  /** The button that goes on with the game (mainButton): the modern theme's gold pill. */
  protected readonly main = computed(() => mainButton(this.decisions.view().buttons));
  protected readonly ids = BUTTON_IDS;
  protected readonly ending = inject(GameEnding);
  private readonly theme = inject(ThemeService).theme;
  protected readonly showEnd = computed(() => this.theme() === 'modern' && this.ending.ended());
}
