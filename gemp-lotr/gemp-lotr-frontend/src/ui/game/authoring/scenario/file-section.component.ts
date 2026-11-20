import { Component, computed, inject, input, signal } from '@angular/core';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { FilingHow } from '../../../../core/game/authoring/filing';
import { commentsNote, movesNote, pathLine } from '../../../../core/game/authoring/scenario-settings';
import { Notifier } from '../../../../core/notifier/notifier';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { askAuthor } from '../widgets/author-confirm';
import { FilingFormComponent } from './filing-form.component';
import { SubmitDialogComponent } from './submit-dialog.component';

/**
 * The Scenario tab's "The file": where it is and whether it is saved, what saving would drop or move, and what can be
 * done with it: Save, Save as… and Rename… (the filing form, one at a time), Check, Try other answers, See the file,
 * Submit for review… (a scenario in the author's own folder only; asked first in the submit dialog), and Close the
 * draft (asked first when it has changes). What those say, and the last save's whole report (Authoring.report), is
 * shown under the buttons until put away.
 *
 * DOM contract: `section.gd-author-scenario__section[data-section=file]` with `.gd-author-scenario__path`,
 * `.gd-author-note[data-note]` (comments, moves), `.gd-author-bar` with `button[data-author]`: save, save-as, rename
 * (`data-on` while its form is open), check, other-answers, see-file, submit, close; `gd-game-author-filing`;
 * `pre.gd-author-scenario__report` and `button[data-author=put-away]`; `gd-game-author-submit`.
 */
@Component({
  selector: 'gd-game-author-scenario-file',
  imports: [AuthorButtonDirective, FilingFormComponent, SubmitDialogComponent],
  template: `
    <section class="gd-author-scenario__section" data-section="file">
      <h3 class="gd-author-scenario__heading">The file</h3>
      <p class="gd-author-note gd-author-scenario__path">{{ path() }}</p>
      @if (comments(); as note) {
        <p class="gd-author-note gd-author-scenario__pending" data-note="comments" [title]="note.hint">
          {{ note.text }}
        </p>
      }
      @if (moves(); as note) {
        <p class="gd-author-note gd-author-scenario__pending" data-note="moves" [title]="note.hint">
          {{ note.text }}
        </p>
      }
      <div class="gd-author-bar">
        <button
          gdAuthorButton="primary"
          data-author="save"
          title="Write the draft to its file"
          (click)="authoring.save()"
        >
          Save
        </button>
        <button
          gdAuthorButton
          data-author="save-as"
          title="Save the draft as another scenario, with a file and an id of its own"
          [attr.data-on]="filing() === 'copy' ? '' : null"
          [attr.aria-expanded]="filing() === 'copy'"
          (click)="toggle('copy')"
        >
          Save as…
        </button>
        <button
          gdAuthorButton
          data-author="rename"
          title="Give the file another name, or move it to another folder. It stays the same scenario."
          [attr.data-on]="filing() === 'rename' ? '' : null"
          [attr.aria-expanded]="filing() === 'rename'"
          (click)="toggle('rename')"
        >
          Rename…
        </button>
        <button
          gdAuthorButton
          data-author="check"
          title="Play the draft through with nobody at the table, the way the server does before it offers a scenario"
          (click)="authoring.check()"
        >
          Check
        </button>
        <button
          gdAuthorButton
          data-author="other-answers"
          title="Play the draft again wherever the player could answer otherwise (anything a step allows, or anything at all where it allows anything), each time up to the script's next step, and say where the script does not handle it. Takes a while."
          (click)="authoring.offScript()"
        >
          Try other answers
        </button>
        <button
          gdAuthorButton
          data-author="see-file"
          title="The draft as the file it would be saved as"
          (click)="authoring.seeFile()"
        >
          See the file
        </button>
        @if (state().own) {
          <button
            gdAuthorButton
            data-author="submit"
            [title]="
              'Submit it for review, to become a public scenario' +
              (state().dirty || !state().onDisk ? '. It is saved first.' : '.')
            "
            (click)="asking.set(true)"
          >
            Submit for review…
          </button>
        }
        <button
          gdAuthorButton="danger"
          data-author="close"
          title="Give the draft up. Anything not saved is lost."
          (click)="close()"
        >
          Close the draft
        </button>
      </div>
      @if (filing(); as how) {
        <gd-game-author-filing [state]="state()" [how]="how" (done)="filing.set(null)" />
      }
      @if (authoring.report(); as report) {
        <pre class="gd-author-scenario__report">{{ report }}</pre>
        <div class="gd-author-bar">
          <button gdAuthorButton data-author="put-away" (click)="authoring.report.set(null)">Put that away</button>
        </div>
      }
      <gd-game-author-submit [title]="name()" [open]="asking()" (answered)="submit($event)" />
    </section>
  `,
})
export class FileSectionComponent {
  protected readonly authoring = inject(Authoring);
  private readonly notifier = inject(Notifier);
  readonly state = input.required<DraftState>();
  /** The filing form open: Save as (copy) or Rename. */
  protected readonly filing = signal<FilingHow | null>(null);
  /** The submit dialog is up. */
  protected readonly asking = signal(false);

  protected readonly path = computed(() => pathLine(this.state()));
  protected readonly comments = computed(() => commentsNote(this.state()));
  protected readonly moves = computed(() => movesNote(this.state()));
  protected readonly name = computed(() => {
    const draft = this.state().draft;
    return draft.title?.trim() ? draft.title : draft.id;
  });

  protected toggle(how: FilingHow): void {
    this.filing.update((open) => (open === how ? null : how));
  }

  protected async submit(yes: boolean): Promise<void> {
    this.asking.set(false);
    if (yes) await this.authoring.submit();
  }

  protected async close(): Promise<void> {
    const dirty = this.authoring.draft()?.dirty === true;
    if (
      dirty &&
      !(await askAuthor(this.notifier, 'The draft has changes that have not been saved. Close it anyway?', 'Close it'))
    ) {
      return;
    }
    await this.authoring.close();
  }
}
