import { afterRenderEffect, Component, ElementRef, input, output, viewChild } from '@angular/core';
import {
  SUBMIT_ASK,
  SUBMIT_BEFORE_HEADING,
  SUBMIT_CONSEQUENCES,
  SUBMIT_DOES_HEADING,
  SUBMIT_GUIDELINES,
  SUBMIT_TITLE,
  SUBMIT_YES,
} from '../../../core/game/authoring/submit-wording';
import { CANCEL } from '../../../core/hall/account/account-messages';
import { ShelfQuestion } from '../../../core/hall/account/my-scenarios.service';

/**
 * My Scenarios' questions, in the shelf's own box (scenarioSubmit.js ScenarioConfirm.ask and ScenarioSubmit.ask: not
 * a jQuery UI dialog): a modal `<dialog>` over a shade, the title, the paragraphs (or, for submitting, what to check
 * first and what submitting does, the authoring panel's words), and the buttons, yes first; Cancel has the focus.
 * Escape and a press on the shade answer no. Says the answer (`answered`), from the click.
 *
 * Inputs: `question` (null: closed). Output: `answered` (true for yes).
 *
 * DOM contract: `dialog.gd-shelf-question` (`data-kind` = confirm | submit, `aria-labelledby`) holding `h2`, the
 * paragraphs (`p`) or the two lists (`h3` and `ul.gd-shelf-question__guidelines` / `__consequences`, and
 * `p.gd-shelf-question__ask`), and `div.gd-shelf-question__buttons` with `button[data-action=yes]` (`data-danger`
 * when it cannot be taken back) and `button[data-action=no]`.
 */
@Component({
  selector: 'gd-shelf-question',
  styleUrl: './shelf-question.component.css',
  host: { style: 'display: contents' },
  template: `
    <dialog
      #box
      class="gd-shelf-question"
      aria-labelledby="gd-shelf-question-title"
      [attr.data-kind]="question()?.kind ?? null"
      (cancel)="$event.preventDefault(); answered.emit(false)"
      (pointerdown)="shade($event)"
    >
      @if (question(); as question) {
        @if (question.kind === 'submit') {
          <h2 id="gd-shelf-question-title">{{ submitTitle(question.title) }}</h2>
          <h3>{{ beforeHeading }}</h3>
          <ul class="gd-shelf-question__guidelines">
            @for (line of guidelines; track $index) {
              <li>{{ line }}</li>
            }
          </ul>
          <h3>{{ doesHeading }}</h3>
          <ul class="gd-shelf-question__consequences">
            @for (line of consequences; track $index) {
              <li>{{ line }}</li>
            }
          </ul>
          <p class="gd-shelf-question__ask">{{ ask }}</p>
          <div class="gd-shelf-question__buttons">
            <button type="button" data-action="yes" (click)="answered.emit(true)">{{ submitYes }}</button>
            <button type="button" data-action="no" autofocus (click)="answered.emit(false)">{{ cancel }}</button>
          </div>
        } @else {
          <h2 id="gd-shelf-question-title">{{ question.title }}</h2>
          @for (line of question.lines; track $index) {
            <p>{{ line }}</p>
          }
          <div class="gd-shelf-question__buttons">
            <button
              type="button"
              data-action="yes"
              [attr.data-danger]="question.danger ? '' : null"
              (click)="answered.emit(true)"
            >
              {{ question.yes }}
            </button>
            <button type="button" data-action="no" autofocus (click)="answered.emit(false)">{{ question.no }}</button>
          </div>
        }
      }
    </dialog>
  `,
})
export class ShelfQuestionComponent {
  readonly question = input<ShelfQuestion | null>(null);
  readonly answered = output<boolean>();

  private readonly box = viewChild.required<ElementRef<HTMLDialogElement>>('box');
  protected readonly submitTitle = SUBMIT_TITLE;
  protected readonly beforeHeading = SUBMIT_BEFORE_HEADING;
  protected readonly doesHeading = SUBMIT_DOES_HEADING;
  protected readonly guidelines = SUBMIT_GUIDELINES;
  protected readonly consequences = SUBMIT_CONSEQUENCES;
  protected readonly ask = SUBMIT_ASK;
  protected readonly submitYes = SUBMIT_YES;
  protected readonly cancel = CANCEL;

  constructor() {
    afterRenderEffect(() => {
      const box = this.box().nativeElement;
      const open = this.question() !== null;
      if (open && !box.open) {
        // jsdom has no showModal
        if (typeof box.showModal === 'function') {
          box.showModal();
        } else {
          box.setAttribute('open', '');
        }
        box.querySelector<HTMLElement>('[autofocus]')?.focus();
      } else if (!open && box.open) {
        // jsdom has no close
        if (typeof box.close === 'function') {
          box.close();
        } else {
          box.removeAttribute('open');
        }
      }
    });
  }

  /** A press on the shade (the dialog's own box, outside its content) answers no. */
  protected shade(event: PointerEvent): void {
    if (event.target === this.box().nativeElement) {
      const rect = this.box().nativeElement.getBoundingClientRect();
      const inside =
        event.clientX >= rect.left &&
        event.clientX <= rect.right &&
        event.clientY >= rect.top &&
        event.clientY <= rect.bottom;
      if (!inside) {
        this.answered.emit(false);
      }
    }
  }
}
