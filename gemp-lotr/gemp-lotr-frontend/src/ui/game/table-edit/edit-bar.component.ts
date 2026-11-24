import { Component, computed, inject } from '@angular/core';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { CardPicker } from './card-picker';

/**
 * The edit bar: whether the table is being edited or played, said plainly, with Start (edit mode to play), Stop (play
 * to edit mode, while the game is at rest) and Reset (the table as it opened, asked about first). While a card is being
 * picked on the table it says so instead. A refusal not said by a control is said under it. It lives in the scripted
 * game's menu, in place of Start again (ScenarioPanelsComponent).
 *
 * DOM contract: `gd-edit-bar` with `data-mode` (edit, play) and `data-busy` while a change is on its way, holding
 * `.gd-edit-bar__mode` (the mode's word; `data-picking` and the instruction while picking), `button[data-edit-bar]`
 * = start (in edit mode), stop (in play mode; disabled unless the game can stop now), reset; and `.gd-edit-bar__problem`
 * (`role=alert`) with the server's words while a refusal of Stop, Start or Reset is up (TableEdit takes it away
 * after a while, or at the next change).
 */
@Component({
  selector: 'gd-edit-bar',
  styleUrl: './edit-bar.component.css',
  host: {
    '[attr.data-mode]': 'edit.mode()',
    '[attr.data-busy]': "edit.busy() ? '' : null",
  },
  template: `
    @if (picker.told(); as told) {
      <span class="gd-edit-bar__mode" data-picking title="Escape: cancel">{{ told }}</span>
    } @else {
      <span class="gd-edit-bar__mode" [title]="modeTitle()">{{ edit.mode() === 'edit' ? 'Editing' : 'Playing' }}</span>
      @if (edit.mode() === 'edit') {
        <button
          type="button"
          class="gd-edit-bar__button"
          data-edit-bar="start"
          data-main
          title="Play on from this table"
          [disabled]="!edit.canStart() || edit.busy()"
          (click)="edit.start()"
        >
          Start
        </button>
      } @else {
        <button
          type="button"
          class="gd-edit-bar__button"
          data-edit-bar="stop"
          [title]="stopTitle()"
          [disabled]="!edit.canStop() || edit.busy()"
          (click)="edit.stop()"
        >
          Stop
        </button>
      }
      <button
        type="button"
        class="gd-edit-bar__button"
        data-edit-bar="reset"
        title="The table as it opened"
        [disabled]="edit.busy()"
        (click)="edit.reset()"
      >
        Reset
      </button>
    }
    @if (barProblem(); as problem) {
      <span class="gd-edit-bar__problem" role="alert">{{ problem }}</span>
    }
  `,
})
export class EditBarComponent {
  protected readonly edit = inject(TableEdit);
  protected readonly picker = inject(CardPicker);
  /** A refusal said by nothing else (Stop, Start, Reset). */
  protected readonly barProblem = computed(() => {
    const problem = this.edit.problem();
    return problem?.at === null ? problem.text : null;
  });
  protected readonly stopTitle = computed(() =>
    this.edit.canStop() ? 'Stop play and change the table' : "Stop works when the game waits at a phase's own prompt",
  );
  protected readonly modeTitle = computed(() =>
    this.edit.mode() === 'edit' ? 'Change the table: right-click a card, or use a + spot' : 'The game is being played',
  );
}
