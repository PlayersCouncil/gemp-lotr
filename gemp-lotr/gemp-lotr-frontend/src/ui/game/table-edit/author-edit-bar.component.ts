import { Component, computed, inject } from '@angular/core';
import { AuthorBackend } from '../../../core/game/table-edit/author-backend';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { CardPicker } from './card-picker';

/** The two boards an author's change can go to, as the chip says them. */
const WHERE: readonly { where: 'setup' | 'now'; label: string; title: string }[] = [
  {
    where: 'setup',
    label: 'Start',
    title: 'Changes go to the board the scenario starts with (a fresh game, played back to here)',
  },
  { where: 'now', label: 'Now', title: 'Changes go to the board as it stands (the step in hand does them)' },
];

/**
 * The author's edit bar, in the scripted game's menu beside Start again while a draft is written at the table: while
 * the panel's Edit table is on, "Editing" and which board the table's changes go to (Start: the one the scenario
 * starts with; Now: the board as it stands), which is also the switch, the same one as the Board tab's "Changes are
 * made" (one state, two places). While a card is being picked on the table it says so instead. A refusal not said by
 * a control is said under it. Nothing while the table is played.
 *
 * DOM contract: `gd-author-edit-bar` with `data-mode` (edit, play) and `data-busy` while a change is on its way,
 * holding (while edited) `.gd-edit-bar__mode` ("Editing"; `data-picking` and the instruction while picking) and
 * `.gd-edit-bar__where[role=group]` with `button.gd-edit-bar__where-button[data-where]` = setup, now
 * (`aria-pressed`); and `.gd-edit-bar__problem` (`role=alert`) while a refusal is up.
 */
@Component({
  selector: 'gd-author-edit-bar',
  styleUrl: './edit-bar.component.css',
  host: {
    '[attr.data-mode]': 'edit.mode()',
    '[attr.data-busy]': "edit.busy() ? '' : null",
    '[attr.data-idle]': "author.editing() || picker.told() !== null ? null : ''",
  },
  template: `
    @if (picker.told(); as told) {
      <span class="gd-edit-bar__mode" data-picking title="Escape: cancel">{{ told }}</span>
    } @else if (author.editing()) {
      <span class="gd-edit-bar__mode" title="Changing the table: right-click a card, or use a + spot">Editing</span>
      <span class="gd-edit-bar__where" role="group" aria-label="Changes go to">
        @for (choice of choices; track choice.where) {
          <button
            type="button"
            class="gd-edit-bar__where-button"
            [attr.data-where]="choice.where"
            [attr.aria-pressed]="author.inSetup() === (choice.where === 'setup')"
            [title]="choice.title"
            [disabled]="edit.busy()"
            (click)="author.setWhere(choice.where)"
          >
            {{ choice.label }}
          </button>
        }
      </span>
    }
    @if (barProblem(); as problem) {
      <span class="gd-edit-bar__problem" role="alert">{{ problem }}</span>
    }
  `,
})
export class AuthorEditBarComponent {
  protected readonly edit = inject(TableEdit);
  protected readonly author = inject(AuthorBackend);
  protected readonly picker = inject(CardPicker);
  protected readonly choices = WHERE;
  /** A refusal said by nothing else. */
  protected readonly barProblem = computed(() => {
    const problem = this.edit.problem();
    return problem?.at === null ? problem.text : null;
  });
}
