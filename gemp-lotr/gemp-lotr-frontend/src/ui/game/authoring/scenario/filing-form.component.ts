import { Component, computed, inject, input, output, signal } from '@angular/core';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import {
  canonicalOf,
  filingPreview,
  FilingHow,
  folderOf,
  nameLength,
  suggestedName,
  tidyPath,
} from '../../../../core/game/authoring/filing';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorFieldComponent } from '../widgets/author-field.component';

/**
 * The form under Save as… and Rename… (the legacy filingForm): the file's new name, between the folder its kind keeps
 * it in and the ending it always has, with where that puts it (and, for a copy, the id it will be known by) said as it
 * is typed. Enter or the button files it (Authoring.saveAs) with the path tidied; nothing usable typed marks the name
 * and sends nothing. Says `done` when filed or cancelled.
 *
 * DOM contract: `.gd-author-scenario__filing` (`data-filing`: copy, rename) with `.gd-author-note[data-filing-note]`,
 * a `gd-author-field` holding `.gd-author-scenario__named` (`.gd-author-scenario__affix` either side of
 * `input[data-filing-path]`, `data-bad` when nothing usable was typed), `.gd-author-note[data-filing-preview]`, and
 * `button[data-author=file]`, `button[data-author=cancel-filing]`.
 */
@Component({
  selector: 'gd-game-author-filing',
  imports: [AuthorButtonDirective, AuthorFieldComponent],
  template: `
    <div class="gd-author-scenario__filing" [attr.data-filing]="how()">
      <p class="gd-author-note" data-filing-note [title]="explanation()">
        {{ copy() ? 'A new scenario, with an id of its own.' : 'The same scenario, its file moved.' }}
      </p>
      <gd-author-field
        [label]="copy() ? 'Save as' : 'Rename to'"
        [hint]="'Kept under ' + folder() + ' because of its kind; .hjson is put on the end.'"
      >
        <span class="gd-author-scenario__named">
          <span class="gd-author-scenario__affix">{{ folder() }}</span>
          <input
            #path
            type="text"
            class="gd-author-input"
            data-filing-path
            [maxLength]="max()"
            [value]="suggested()"
            [attr.data-bad]="bad() ? '' : null"
            [title]="nameHint()"
            (input)="typed.set(path.value)"
            (keydown.enter)="file()"
          />
          <span class="gd-author-scenario__affix">.hjson</span>
        </span>
      </gd-author-field>
      <p class="gd-author-note" data-filing-preview>{{ preview() }}</p>
      <div class="gd-author-bar">
        <button gdAuthorButton="primary" data-author="file" [disabled]="filing()" (click)="file()">
          {{ copy() ? 'Save the copy' : 'Rename' }}
        </button>
        <button gdAuthorButton data-author="cancel-filing" (click)="done.emit()">Cancel</button>
      </div>
    </div>
  `,
})
export class FilingFormComponent {
  protected readonly authoring = inject(Authoring);
  readonly state = input.required<DraftState>();
  readonly how = input.required<FilingHow>();
  readonly done = output();

  protected readonly copy = computed(() => this.how() === 'copy');
  /** What is typed, once anything is (until then the suggestion). */
  protected readonly typed = signal<string | null>(null);
  protected readonly bad = signal(false);
  protected readonly filing = signal(false);

  protected readonly folder = computed(() => folderOf(this.state()));
  protected readonly suggested = computed(() => suggestedName(this.state(), this.how()));
  protected readonly max = computed(() => nameLength(this.state()));
  private readonly name = computed(() => this.typed() ?? this.suggested());
  protected readonly preview = computed(() => filingPreview(this.state(), this.how(), this.name()));
  protected readonly explanation = computed(() =>
    this.copy()
      ? 'Saves the draft as another scenario. The copy is known by its file’s name (that becomes its id), and is what you are writing from then on. The scenario it came from stays as it was last saved.'
      : 'Gives the file another name, or moves it to another folder. It stays the same scenario: its id, and players’ progress in it, do not change.' +
        (this.state().dirty ? ' What has not been saved stays unsaved.' : ''),
  );
  protected readonly nameHint = computed(
    () =>
      'A name, with folders before it if it goes in any: basics/basics-02-the-ring. Letters, digits, spaces, dots, dashes and underscores. ' +
      `It is kept under ${this.folder()} because of its kind, and .hjson is put on the end.`,
  );

  protected async file(): Promise<void> {
    if (this.filing()) return;
    const tidy = tidyPath(this.name());
    this.bad.set(tidy === '');
    if (tidy === '') return;
    this.filing.set(true);
    const filed = await this.authoring.saveAs(this.how(), tidy, canonicalOf(this.state()));
    this.filing.set(false);
    if (filed) this.done.emit();
  }
}
