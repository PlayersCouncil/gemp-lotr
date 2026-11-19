import { Component, input, output } from '@angular/core';
import { DialogComponent } from '../../../shared/dialog/dialog.component';

/** What to see to before submitting a scenario for review (the legacy scenarioSubmit.js GUIDELINES). */
export const SUBMIT_GUIDELINES: readonly string[] = [
  'Play it through yourself first, as a player would. The server plays it through too: one that does not play through cannot be submitted.',
  'Give it a clear title and a summary that says what the player will do or learn, and put your name in Author.',
  'Keep the messages friendly, on topic and accurate: what they say about cards and rules has to be true of the game as Gemp plays it.',
  'A tutorial teaches one thing at a time; a puzzle has a solution a player can find from what they are shown.',
];

/** What submitting does (the legacy WHAT_HAPPENS). */
export const SUBMIT_CONSEQUENCES: readonly string[] = [
  'It is locked: you cannot change, rename or delete it until an admin has approved or rejected it. A draft of it open in the authoring tool is closed. To keep working on it in the meantime, copy it in My Scenarios.',
  'An admin plays it, and either approves it or sends it back with the changes they would like, which you will see in My Scenarios.',
  'Approved, it becomes a public scenario, offered to every player. If it is a version of a public scenario (copied from one), it replaces that one.',
  'A public scenario is part of the game: the admins may change it, disable it or take it down, and it may be kept with the game’s own scenarios. It stays credited to whoever its Author says.',
];

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
