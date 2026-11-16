import { Component, computed, ElementRef, inject, input, linkedSignal, output } from '@angular/core';
import { GameClocks } from '../../../core/game/clock/game-clocks.service';
import { DecisionButton } from '../../../core/game/decision/decision-view';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { GameSession } from '../../../core/game/session/game-session.service';
import { capturePointer } from '../../shared/pointer-drag/pointer-drag.directive';
import { choiceLabel } from '../decision/decision-dialogs.component';
import { clockText } from '../panel/game-clock';
import { ReplayControlsComponent } from '../panel/replay-controls.component';
import { CardHintsDirective } from '../table/card-hints.directive';

/**
 * The phone layout's divider between the table and the hand (G7 of trymarz/gemp-lotr-private#27): it holds the
 * waiting decision's text and its buttons (a number or one answer of several are asked here too), and is dragged up
 * and down; dragged to the bottom it hides the hand. While nothing is asked it says whose move it is.
 *
 * Emits `dragged` with the hand's height the pointer asks for (px from the page's bottom) while it moves, and
 * `dropped` when it is let go.
 *
 * DOM contract: `gd-phone-divider` (`data-deciding`) holding `.gd-phone-divider__text` (`data-decision=text`),
 * `[data-clock]` the decision's clock, `.gd-phone-divider__hand-hint` while the hand is hidden, and
 * `.gd-phone-divider__buttons` with `button[data-decision-button]` (OK, Pass, Done, Reset choice, Clear selection),
 * `button[data-decision-choice]`, `select[data-decision=choice]`, the number's `button[data-decision=integer-down]`,
 * `output[data-decision=integer]`, `button[data-decision=integer-up]`.
 */
@Component({
  selector: 'gd-phone-divider',
  styleUrl: './phone-divider.component.css',
  imports: [CardHintsDirective, ReplayControlsComponent],
  host: {
    '[attr.data-deciding]': "deciding() ? '' : null",
    role: 'separator',
    'aria-orientation': 'horizontal',
    'aria-label': 'Drag to resize the hand',
    '(pointerdown)': 'down($event)',
  },
  template: `
    <div class="gd-phone-divider__question">
      <span class="gd-phone-divider__text" data-decision="text" gdCardHints [innerHTML]="text()"></span>
      @if (clock(); as c) {
        <span class="gd-phone-divider__clock" data-clock [attr.data-band]="clocks.view().decisionBand">{{ c }}</span>
      }
    </div>
    @if (handHidden()) {
      <span class="gd-phone-divider__hand-hint">▲ Hand {{ handCount() }} · drag up</span>
    }
    @if (session.replaying()) {
      <gd-game-replay class="gd-phone-divider__replay" />
    }
    @if (small(); as dialog) {
      <div class="gd-phone-divider__buttons">
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
    } @else if (buttons().length > 0) {
      <div class="gd-phone-divider__buttons">
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
  `,
})
export class PhoneDividerComponent {
  protected readonly session = inject(GameSession);
  protected readonly decisions = inject(GameDecisions);
  protected readonly clocks = inject(GameClocks);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  readonly handHidden = input(false);
  readonly dragged = output<number>();
  readonly dropped = output<number>();

  private readonly view = this.decisions.view;
  private readonly dialog = computed(() => this.view().dialog);
  protected readonly small = computed(() => {
    const dialog = this.dialog();
    return dialog && dialog.kind !== 'cards' ? dialog : null;
  });
  /** The alert box's buttons, or those of the cards shown in the hand's place. */
  protected readonly buttons = computed<readonly DecisionButton[]>(() => {
    const dialog = this.dialog();
    return dialog?.kind === 'cards' ? dialog.buttons : this.view().buttons;
  });
  /** The button that goes on with the game: Done or Pass, OK. */
  protected readonly primary = computed(() => {
    const buttons = this.buttons();
    return buttons.find((b) => b === 'Done') ?? buttons.find((b) => b === 'Pass' || b === 'OK') ?? null;
  });
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
    if (!model.started) return '';
    const current = model.currentPlayer;
    if (model.spectator || this.session.replaying()) return current ? `${current}'s turn` : '';
    return model.decision
      ? ''
      : `Waiting for ${model.players.find((p) => p !== model.participantId) ?? 'the opponent'}…`;
  });

  protected readonly clock = computed(() => {
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

  /** Dragging starts anywhere on the divider but its buttons and fields. */
  protected down(event: PointerEvent): void {
    const target = event.target as Element | null;
    if (event.button !== 0 || target?.closest('button, select, input, a, output, gd-game-replay')) return;
    event.preventDefault();
    const page = this.host.closest('gd-game-phone')?.getBoundingClientRect();
    if (!page) return;
    capturePointer(this.host, event.pointerId);
    const offset = event.clientY - this.host.getBoundingClientRect().bottom;
    const height = (y: number) => page.bottom - y + offset;
    const move = (e: PointerEvent) => this.dragged.emit(height(e.clientY));
    const up = (e: PointerEvent) => {
      this.host.removeEventListener('pointermove', move);
      this.host.removeEventListener('pointerup', up);
      this.host.removeEventListener('pointercancel', up);
      this.dropped.emit(height(e.clientY));
    };
    this.host.addEventListener('pointermove', move);
    this.host.addEventListener('pointerup', up);
    this.host.addEventListener('pointercancel', up);
  }
}
