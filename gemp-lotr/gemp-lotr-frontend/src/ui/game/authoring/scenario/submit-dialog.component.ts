import { Component, input, output } from '@angular/core';
import { SUBMIT_CONSEQUENCES, SUBMIT_GUIDELINES } from '../../../../core/game/authoring/submit-wording';
import { DialogComponent } from '../../../shared/dialog/dialog.component';

/**
 * Asked before a scenario of the author's own is submitted for review (the legacy ScenarioSubmit.ask): what to see
 * to first, what submitting does, and whether to go ahead. Cancel has the focus; Escape, the close button and Cancel
 * all answer no. Says the answer (`answered`); the owner closes it.
 *
 * DOM contract: `dialog.gd-dialog[data-dialog=author-submit]` with `.gd-author-submit__guidelines` and
 * `.gd-author-submit__consequences` (an `li` each), `p.gd-author-submit__ask`, and `.gd-dialog__actions` with
 * `button.gd-button[data-action]`: submit, cancel.
 */
@Component({
  selector: 'gd-game-author-submit',
  imports: [DialogComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog
      name="author-submit"
      [heading]="'Submit “' + (title() || 'this scenario') + '” for review?'"
      [open]="open()"
      (closed)="answered.emit(false)"
    >
      <h3 class="gd-author-submit__head">Before you submit</h3>
      <ul class="gd-author-submit__guidelines">
        @for (line of guidelines; track $index) {
          <li>{{ line }}</li>
        }
      </ul>
      <h3 class="gd-author-submit__head">What submitting does</h3>
      <ul class="gd-author-submit__consequences">
        @for (line of consequences; track $index) {
          <li>{{ line }}</li>
        }
      </ul>
      <p class="gd-author-submit__ask">Are you sure you want to submit it?</p>
      <div class="gd-dialog__actions" gdDialogActions>
        <button type="button" class="gd-button" data-action="submit" (click)="answered.emit(true)">
          Submit for review
        </button>
        <button type="button" class="gd-button" data-action="cancel" autofocus (click)="answered.emit(false)">
          Cancel
        </button>
      </div>
    </gd-dialog>
  `,
})
export class SubmitDialogComponent {
  /** What the scenario is called. */
  readonly title = input('');
  readonly open = input(false);
  readonly answered = output<boolean>();
  protected readonly guidelines = SUBMIT_GUIDELINES;
  protected readonly consequences = SUBMIT_CONSEQUENCES;
}
