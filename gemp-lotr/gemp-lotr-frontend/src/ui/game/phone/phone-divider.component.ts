import { Component, computed, inject, input, linkedSignal } from '@angular/core';
import { GameClocks } from '../../../core/game/clock/game-clocks.service';
import { DecisionButton, mainButton } from '../../../core/game/decision/decision-view';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { GameEnding } from '../../../core/game/session/game-ending';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameUndo } from '../../../core/game/undo/game-undo.service';
import { UNDO_BUTTON } from '../../../core/game/undo/undo-view';
import { choiceLabel } from '../decision/decision-dialogs.component';
import { clockText } from '../panel/game-clock';
import { ReplayControlsComponent } from '../panel/replay-controls.component';
import { CardHintsDirective } from '../table/card-hints.directive';

/**
 * The phone layout's divider between the table and the hand (G7 of trymarz/gemp-lotr-private#27): it holds the
 * waiting decision's text and its buttons (a number or one answer of several are asked here too), and is dragged up
 * and down; dragged to the bottom it hides the hand. While nothing is asked it says whose move it is.
 *
 * The phone layout drags it (gdPointerDrag, PhoneGameComponent.handDrag). A spectator (`watching`) has no hand and
 * nothing to answer (the owner, 2026-11-20): the divider stays at the bottom, is not dragged, has no buttons, and shows
 * whose turn it is and the game's latest message.
 *
 * DOM contract: `gd-phone-divider` (`data-deciding`, `data-watching`) holding `.gd-phone-divider__text`
 * (`data-decision=text`), `.gd-phone-divider__latest` (a spectator's latest message),
 * `[data-clock]` the decision's clock, `.gd-phone-divider__hand-hint` while the hand is hidden, and
 * `.gd-phone-divider__buttons` with `button[data-decision-button]` (OK, Pass, Done, Reset choice, Clear selection),
 * `button[data-decision-choice]`, `select[data-decision=choice]`, the number's `button[data-decision=integer-down]`,
 * `output[data-decision=integer]`, `button[data-decision=integer-up]`; the buttons are `inert` (`data-frozen`) while a
 * revert request waits; once the game ended `button[data-phone=game-ended]`, which shows its end again. After them,
 * while the player's last choice is theirs to take back, `button[data-undo=last]` (as the alert box's,
 * GameAlertComponent).
 */
@Component({
  selector: 'gd-phone-divider',
  styleUrl: './phone-divider.component.css',
  imports: [CardHintsDirective, ReplayControlsComponent],
  host: {
    '[attr.data-deciding]': "deciding() ? '' : null",
    '[attr.data-watching]': "watching() ? '' : null",
    '[attr.role]': "watching() ? 'status' : 'separator'",
    '[attr.aria-orientation]': "watching() ? null : 'horizontal'",
    '[attr.aria-label]': "watching() ? null : 'Drag to resize the hand'",
  },
  template: `
    <div class="gd-phone-divider__question">
      <span class="gd-phone-divider__text" data-decision="text" gdCardHints [innerHTML]="text()"></span>
      @if (clock(); as c) {
        <span class="gd-phone-divider__clock" data-clock [attr.data-band]="clocks.view().decisionBand">{{ c }}</span>
      }
    </div>
    @if (watching()) {
      @if (latest(); as message) {
        <span class="gd-phone-divider__latest" gdCardHints [innerHTML]="message"></span>
      }
    } @else if (handHidden()) {
      <span class="gd-phone-divider__hand-hint">▲ Hand {{ handCount() }} · drag up</span>
    }
    @if (session.replaying()) {
      <gd-game-replay class="gd-phone-divider__replay" />
    }
    @if (small(); as dialog) {
      <div
        class="gd-phone-divider__buttons"
        [attr.inert]="undo.frozen() ? '' : null"
        [attr.data-frozen]="undo.frozen() ? '' : null"
      >
        @if (dialog.kind === 'integer') {
          <span class="gd-phone-spinner" role="group" aria-label="Number">
            <button
              type="button"
              class="gd-phone-button"
              data-decision="integer-down"
              aria-label="Less"
              (click)="step(-1)"
            >
              −
            </button>
            <output data-decision="integer">{{ number() }}</output>
            <button
              type="button"
              class="gd-phone-button"
              data-decision="integer-up"
              aria-label="More"
              (click)="step(1)"
            >
              +
            </button>
          </span>
          @if (dialog.answerable) {
            <button
              type="button"
              class="gd-phone-button"
              data-primary
              data-decision-button="OK"
              (click)="decisions.answerNumber(number().toString())"
            >
              OK
            </button>
          }
        } @else if (dialog.dropDown) {
          <select
            #list
            class="gd-phone-select"
            data-decision="choice"
            aria-label="Answer"
            (change)="choice.set(list.selectedIndex)"
          >
            @for (option of dialog.options; track $index) {
              <option [value]="$index" [innerHTML]="option"></option>
            }
          </select>
          @if (dialog.answerable) {
            <button
              type="button"
              class="gd-phone-button"
              data-primary
              data-decision-button="OK"
              (click)="decisions.answerChoice(choice())"
            >
              OK
            </button>
          }
        } @else {
          @for (option of dialog.options; track $index) {
            <button
              type="button"
              class="gd-phone-button"
              [attr.data-decision-choice]="$index"
              (click)="decisions.answerChoice($index)"
            >
              <span [innerHTML]="label(option)"></span>
            </button>
          }
        }
      </div>
    } @else if (ending.ended()) {
      <!-- The game's end again, with the replay (the owner, 2026-11-20) -->
      <div class="gd-phone-divider__buttons">
        <button type="button" class="gd-phone-button" data-primary data-phone="game-ended" (click)="ending.show()">
          Result and replay
        </button>
      </div>
    } @else if (buttons().length > 0) {
      <div
        class="gd-phone-divider__buttons"
        [attr.inert]="undo.frozen() ? '' : null"
        [attr.data-frozen]="undo.frozen() ? '' : null"
      >
        @for (button of buttons(); track button) {
          <button
            type="button"
            class="gd-phone-button"
            [attr.data-primary]="button === primary() ? '' : null"
            [attr.data-decision-button]="button"
            (click)="press(button)"
          >
            {{ button }}
          </button>
        }
      </div>
    }
    @if (undo.button(); as undoButton) {
      <button
        type="button"
        class="gd-phone-button gd-phone-divider__undo"
        data-undo="last"
        [title]="undoButton.title"
        [disabled]="!undoButton.enabled"
        (click)="undo.undoLatest()"
      >
        {{ undoLabel }}
      </button>
    }
  `,
})
export class PhoneDividerComponent {
  protected readonly session = inject(GameSession);
  protected readonly decisions = inject(GameDecisions);
  protected readonly clocks = inject(GameClocks);
  protected readonly ending = inject(GameEnding);
  protected readonly undo = inject(GameUndo);
  protected readonly undoLabel = UNDO_BUTTON;

  readonly handHidden = input(false);
  /** A spectator's divider: no hand, nothing to answer (see the class's comment). */
  readonly watching = input(false);

  private readonly view = this.decisions.view;
  private readonly dialog = computed(() => this.view().dialog);
  protected readonly small = computed(() => {
    if (this.watching()) return null;
    const dialog = this.dialog();
    return dialog && dialog.kind !== 'cards' ? dialog : null;
  });
  /** The alert box's buttons, or those of the cards shown in the hand's place. */
  protected readonly buttons = computed<readonly DecisionButton[]>(() => {
    if (this.watching()) return [];
    const dialog = this.dialog();
    return dialog?.kind === 'cards' ? dialog.buttons : this.view().buttons;
  });
  /** The button that goes on with the game: Done or Pass, OK. */
  protected readonly primary = computed(() => mainButton(this.buttons()));
  protected readonly deciding = computed(() => this.session.model().decision !== null && !this.session.replaying());
  protected readonly handCount = computed(() => {
    const model = this.session.model();
    const zones = model.participantId ? model.stats?.playerZones[model.participantId] : undefined;
    return zones?.hand ?? '';
  });

  protected readonly text = computed(() => {
    const view = this.view();
    const dialog = view.dialog;
    if (view.alert) return view.alert;
    if (dialog?.kind === 'cards') return dialog.title;
    if (dialog) return dialog.text;
    const model = this.session.model();
    if (model.ended) return 'The game has ended.';
    if (!model.started) return this.session.replaying() ? 'A replay: press play to watch it.' : '';
    const current = model.currentPlayer;
    if (model.spectator || this.session.replaying()) return current ? `${current}'s turn` : '';
    return model.decision
      ? ''
      : `Waiting for ${model.players.find((p) => p !== model.participantId) ?? 'the opponent'}…`;
  });

  /** The game's latest message (HTML, as the chat shows it), for a spectator. */
  protected readonly latest = computed(() => {
    const messages = this.session.model().messages;
    for (let i = messages.length - 1; i >= 0; i--) {
      const message = messages[i];
      if (message?.kind === 'message') return message.text;
    }
    return null;
  });

  protected readonly clock = computed(() => {
    // Nothing is timed once the game has ended
    if (this.ending.ended()) return null;
    const decision = this.clocks.view().decision;
    if (!decision) return null;
    const elapsed = clockText(decision.elapsed);
    return decision.limit > 0 ? `${elapsed} / ${clockText(decision.limit)}` : elapsed;
  });

  protected readonly number = linkedSignal(() => {
    const dialog = this.small();
    if (dialog?.kind !== 'integer') return 0;
    return dialog.value < dialog.min || dialog.value > dialog.max ? dialog.min : dialog.value;
  });
  protected readonly choice = linkedSignal(() => (this.small() ? 0 : 0));

  protected label(option: string): string {
    return choiceLabel(option);
  }

  protected step(by: number): void {
    const dialog = this.small();
    if (dialog?.kind !== 'integer') return;
    const next = this.number() + by;
    if (next >= dialog.min && next <= dialog.max) this.number.set(next);
  }

  protected press(button: DecisionButton): void {
    void this.decisions.press(button);
  }
}
