import { Component, computed, inject } from '@angular/core';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { addedText, opText } from '../../../../core/game/authoring/board-queue';
import { AuthorButtonDirective } from '../widgets/author-button.directive';

/** A line of what has been gathered: what it says, and what its × undoes. */
interface GatheredLine {
  kind: 'removed' | 'added' | 'op';
  index: number;
  text: string;
}

/**
 * The Board tab's bar, kept in view as the tab is scrolled: Submit changes (with how many are gathered) and Reset,
 * and under them what has been gathered, a line each (taking a card out, adding one, a change to the board) with a ×
 * that undoes it.
 *
 * DOM contract: `gd-author-board-gathered` holding `.gd-author-board-gathered__buttons` (`button[data-action]`:
 * submit, reset; `.gd-author-note` with nothing gathered), then `ul.gd-author-board-gathered__list` with an
 * `li.gd-author-board-gathered__line` per change (`data-kind`: removed, added, op) holding
 * `.gd-author-board-gathered__text` and `button[data-action=undo]`.
 */
@Component({
  selector: 'gd-author-board-gathered',
  styleUrl: './board-gathered.component.css',
  imports: [AuthorButtonDirective],
  template: `
    <div class="gd-author-board-gathered__buttons">
      <button
        gdAuthorButton="primary"
        data-action="submit"
        [title]="
          changes.inSetup()
            ? 'Makes all of them to the board the scenario starts with, and plays a fresh game by the script back to here'
            : 'Makes all of them at once to the board as it stands'
        "
        [disabled]="changes.count() === 0"
        (click)="changes.submit()"
      >
        Submit changes ({{ changes.count() }})
      </button>
      <button
        gdAuthorButton
        data-action="reset"
        title="Lets go of the changes gathered here: the board is as the scenario has it"
        [disabled]="changes.count() === 0"
        (click)="changes.reset()"
      >
        Reset
      </button>
      @if (changes.count() === 0) {
        <span class="gd-author-note" title="Changes are gathered here until they are submitted">
          Changes wait here until submitted.
        </span>
      }
    </div>
    @if (lines().length > 0) {
      <ul class="gd-author-board-gathered__list">
        @for (line of lines(); track line.kind + line.index) {
          <li class="gd-author-board-gathered__line" [attr.data-kind]="line.kind">
            <span class="gd-author-board-gathered__text" [title]="line.text">{{ line.text }}</span>
            <button gdAuthorButton data-action="undo" title="Do not make this change" (click)="undo(line)">×</button>
          </li>
        }
      </ul>
    }
  `,
})
export class BoardGatheredComponent {
  protected readonly changes = inject(BoardChanges);

  protected readonly lines = computed<GatheredLine[]>(() => {
    const gathered = this.changes.gathered();
    if (!gathered) return [];
    return [
      ...gathered.removed.map((alias, index) => ({ kind: 'removed' as const, index, text: `take out ${alias}` })),
      ...gathered.added.map((add, index) => ({ kind: 'added' as const, index, text: addedText(add) })),
      ...gathered.ops.map((op, index) => ({ kind: 'op' as const, index, text: opText(op) })),
    ];
  });

  protected undo(line: GatheredLine): void {
    if (line.kind === 'removed') this.changes.undoRemoved(line.index);
    else if (line.kind === 'added') this.changes.undoAdded(line.index);
    else this.changes.undoOp(line.index);
  }
}
