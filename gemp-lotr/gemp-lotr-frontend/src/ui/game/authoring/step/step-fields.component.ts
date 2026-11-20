import { Component, computed, inject } from '@angular/core';
import { isOpeningWork, written } from '../../../../core/game/authoring/step-model';
import { isAwaiting } from '../../../../core/game/authoring/steps';
import { passesByItself, SILENT } from '../../../../core/game/authoring/timeline';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { HjsonAreaDirective } from '../widgets/hjson-area.directive';
import { AllowEditorComponent } from './allow-editor.component';
import { MessageListComponent } from './message-list.component';
import { StepEditor } from './step-editor';
import { StepFieldDirective } from './step-field.directive';
import { WhenEditorComponent } from './when-editor.component';
import { AskedPickerComponent } from './asked-picker.component';

const OPENING_DO_HINT =
  "Changes to the board, made after the opening's messages and before the first turn begins, so that the first decision is worked out with them: " +
  'draw(n) or draw(seat, n) from the top of the deck, move(card, seat.zone), attach(card, bearer), stack(card, host), wound(card, n), burdens(n). ' +
  '(Twilight set here is gone again when the turn starts.)';

/**
 * The form of one step (legacy buildFields): a step of the script, what a block says of itself, or one of a block's
 * steps. At its top, "Answer it again" for the step in hand, what is said of a step with no answer yet or one the game
 * passes by itself; then what it waits for (gd-author-when); what is done (the opening's Do; a block's end; a step's
 * gd-author-allow; nothing for a pause); its messages; whether it ends the scenario (with the closing messages); its
 * note.
 *
 * DOM contract: `gd-author-step-fields` holding `button[data-act=retake]`, `.gd-author-note[data-note]` (awaiting,
 * silent), `gd-author-when`, `.gd-author-step__section[data-section]` (opening-do, ends, messages, rest),
 * `gd-author-asked` (a block's Until and Skip when), `gd-author-allow`, `gd-author-messages`, `gd-author-select[data-pick=end]` and the closing `gd-author-messages`.
 */
@Component({
  selector: 'gd-author-step-fields',
  styleUrl: './step-fields.component.css',
  imports: [
    AuthorButtonDirective,
    AuthorFieldComponent,
    AuthorSelectComponent,
    HjsonAreaDirective,
    StepFieldDirective,
    AllowEditorComponent,
    MessageListComponent,
    WhenEditorComponent,
    AskedPickerComponent,
  ],
  template: `
    @if (view(); as view) {
      @if (view.retake) {
        <div class="gd-author-bar">
          <button
            gdAuthorButton
            data-act="retake"
            title="This step forgets what it allows and how it is answered, and takes both from what you do next. Its messages stay."
            (click)="editor.authoring.retake()"
          >
            Answer it again
          </button>
        </div>
      }
      @if (view.awaiting) {
        <div
          class="gd-author-note"
          data-note="awaiting"
          [title]="
            view.inHandNow
              ? 'This step says nothing yet about what is done. Do it in the game now, and what you do is written in as what it allows. Messages put on it here first are shown before it is answered.'
              : 'This step says nothing yet about what is done. When the game comes to it, what you do there is written in as what it allows.'
          "
        >
          {{
            view.inHandNow ? 'No answer yet: do it in the game now.' : 'No answer yet: what you do there is written in.'
          }}
        </div>
      }
      @if (view.silent) {
        <div class="gd-author-note" data-note="silent" [title]="silent">A pass the game makes by itself.</div>
      }
      <gd-author-when />
      @if (view.opening) {
        <div class="gd-author-step__section" data-section="opening-do">
          <div class="gd-author-step__heading">What is done once it has been read</div>
          <gd-author-field label="Do" [hint]="openingDoHint">
            <input class="gd-author-input" placeholder="draw(5)" gdStepField="do" />
          </gd-author-field>
        </div>
      } @else if (view.kind === 'block') {
        <div class="gd-author-step__section" data-section="ends">
          <div class="gd-author-step__heading">Where it ends</div>
          <gd-author-field
            label="Until"
            hint="The block is over at the first decision that fits this, which goes to the step after the block: phase(regroup), say, or turn(3). A block has to have one. Written like When: a line of terms, or in full as the file has it, in HJSON."
          >
            <textarea gdHjsonArea gdStepField="until" rows="1" [text]="view.until"></textarea>
          </gd-author-field>
          <div class="gd-author-step__block"><gd-author-asked path="until" /></div>
          <gd-author-field
            label="Skip when"
            hint="Given up on, before any of its steps has applied, if a decision that fits this arrives"
          >
            <textarea gdHjsonArea gdStepField="skipWhen" rows="1" [text]="view.skipWhen"></textarea>
          </gd-author-field>
          <div class="gd-author-step__block"><gd-author-asked path="skipWhen" /></div>
          <label
            class="gd-author-check gd-author-step__block"
            title="Its steps shown by the skirmish they are in, and after: for a turn's assignment and skirmishes"
          >
            <input type="checkbox" data-flag="skirmishes" [checked]="view.skirmishes" (change)="skirmishes($event)" />
            the skirmishes of a turn
          </label>
        </div>
      } @else if (!view.pause) {
        <gd-author-allow />
      }
      @if (view.kind !== 'block') {
        <div class="gd-author-step__section" data-section="messages">
          <div class="gd-author-step__heading">Messages</div>
          <gd-author-messages path="messages" />
        </div>
      }
      <div class="gd-author-step__section" data-section="rest">
        @if (view.kind !== 'block') {
          <gd-author-field label="Ends the scenario" hint="Once this step has been answered (or, for a pause, read)">
            <gd-author-select
              data-pick="end"
              label="Ends the scenario"
              [options]="ends"
              [value]="view.end"
              (changed)="end($event)"
            />
          </gd-author-field>
          @if (view.end !== '') {
            <gd-author-messages path="end.messages" [closing]="true" />
          }
        }
        <gd-author-field label="Note" hint="For whoever works on the scenario next. Never shown to a player.">
          <textarea class="gd-author-input" gdStepField="note" rows="2"></textarea>
        </gd-author-field>
      </div>
    }
  `,
})
export class StepFieldsComponent {
  protected readonly editor = inject(StepEditor);
  protected readonly silent = SILENT;
  protected readonly openingDoHint = OPENING_DO_HINT;
  protected readonly ends: readonly AuthorOption[] = [
    ['', 'no'],
    ['complete', 'yes: complete'],
    ['fail', 'yes: failed'],
  ];

  protected readonly view = computed(() => {
    const work = this.editor.work();
    const place = this.editor.place();
    const form = this.editor.form();
    const draft = this.editor.authoring.draft();
    if (!work || !place || !form || !draft) return null;
    const kind = place.kind;
    const position = draft.position;
    const opening = isOpeningWork(work);
    const pause = work.pause === true;
    const inHandNow =
      place.index + 1 === position.step && position.inHand && position.sub === (kind === 'child' ? place.sub + 1 : 0);
    return {
      kind,
      opening,
      pause,
      inHandNow,
      retake: inHandNow && kind !== 'block' && !pause && !opening,
      awaiting: isAwaiting(draft, form.uid),
      silent: kind !== 'block' && passesByItself(draft, written(work)),
      until: work.until ?? '',
      skipWhen: work.skipWhen ?? '',
      skirmishes: work.skirmishes === true,
      end: work.end.result,
    };
  });

  /** A block's steps shown skirmish by skirmish, or not: the page is laid out afresh once the change is back. */
  protected skirmishes(event: Event): void {
    this.editor.put('skirmishes', (event.target as HTMLInputElement).checked);
    this.editor.now();
  }

  protected end(result: string): void {
    this.editor.put('end.result', result);
    this.editor.now();
  }
}
