import { Component, computed, inject, input } from '@angular/core';
import { Draft, DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { AboutPart, aboutText, aboutValue } from '../../../../core/game/authoring/scenario-settings';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorSelectComponent } from '../widgets/author-select.component';
import { AuthorSendDirective } from '../widgets/author-send.directive';
import { sendTop } from './top';
import { kindChoices } from '../../../../core/game/authoring/puzzle-settings';
import { SandboxToolsComponent } from './sandbox-tools.component';

/** The text fields of "About it", in order: the part, its label, its explanation, and whether it takes lines. */
const FIELDS: readonly (readonly [AboutPart, string, string, boolean])[] = [
  ['title', 'Title', '', false],
  ['summary', 'Summary', 'What the Game Hall says about it', true],
  [
    'id',
    'Id',
    'What the scenario is known as. Changing it makes it a different scenario as far as players’ progress goes.',
    false,
  ],
  ['author', 'Author', '', false],
  ['tags', 'Tags', 'Separated by commas', false],
  ['version', 'Version', 'Raise it when the scenario changes in a way players should hear about', false],
];

/**
 * The Scenario tab's "About it": title, summary, id, author, tags, version, kind (a sandbox's tools under it) and
 * note, each sent as it is left
 * (a blank one taken out; the version a number, 1 if what is typed is none). Fields are bound to the draft, so a
 * fresh state does not disturb one being typed in.
 *
 * DOM contract: `section.gd-author-scenario__section[data-section=about]` with a `gd-author-field` per part holding
 * `input` or `textarea` `.gd-author-input[data-part]` (title, summary, id, author, tags, version, note), and the kind's
 * `gd-author-select[data-part=kind]` (the kinds the server names), for a sandbox followed by its tools
 * (`gd-game-author-sandbox-tools`).
 */
@Component({
  selector: 'gd-game-author-scenario-about',
  imports: [AuthorFieldComponent, AuthorSelectComponent, AuthorSendDirective, SandboxToolsComponent],
  template: `
    <section class="gd-author-scenario__section" data-section="about">
      <h3 class="gd-author-scenario__heading">About it</h3>
      @for (field of fields; track field[0]) {
        <gd-author-field [label]="field[1]" [hint]="field[2]">
          @if (field[3]) {
            <textarea
              #area
              class="gd-author-input"
              rows="3"
              [attr.data-part]="field[0]"
              [value]="text(state().draft, field[0])"
              gdAuthorSend
              (sent)="send(field[0], $event, area)"
            ></textarea>
          } @else {
            <input
              #line
              type="text"
              class="gd-author-input"
              [attr.data-part]="field[0]"
              [value]="text(state().draft, field[0])"
              gdAuthorSend
              (sent)="send(field[0], $event, line)"
            />
          }
        </gd-author-field>
      }
      <gd-author-field label="Kind">
        <gd-author-select
          data-part="kind"
          label="Kind"
          [options]="kinds()"
          [value]="state().draft.kind ?? 'tutorial'"
          (changed)="top('kind', $event)"
        />
      </gd-author-field>
      @if (state().draft.kind === 'sandbox') {
        <gd-game-author-sandbox-tools [state]="state()" />
      }
      <gd-author-field label="Note" hint="For whoever works on the scenario next. Never shown to a player.">
        <textarea
          #note
          class="gd-author-input"
          rows="3"
          data-part="note"
          [value]="text(state().draft, 'note')"
          gdAuthorSend
          (sent)="send('note', $event, note)"
        ></textarea>
      </gd-author-field>
    </section>
  `,
})
export class AboutSectionComponent {
  protected readonly authoring = inject(Authoring);
  readonly state = input.required<DraftState>();
  protected readonly fields = FIELDS;
  protected readonly kinds = computed(() => kindChoices(this.state()));

  protected text(draft: Draft, name: AboutPart): string {
    return aboutText(draft, name);
  }

  protected top(name: string, value: unknown): void {
    void sendTop(this.authoring, name, value);
  }

  /** Sends a field; the version shows the number it is taken as. */
  protected send(name: AboutPart, typed: string, field: HTMLInputElement | HTMLTextAreaElement): void {
    const value = aboutValue(name, typed);
    if (name === 'version') field.value = String(value);
    this.top(name, value);
  }
}
