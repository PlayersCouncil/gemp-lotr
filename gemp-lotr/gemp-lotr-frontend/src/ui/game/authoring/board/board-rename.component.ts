import { afterNextRender, Component, ElementRef, inject, input, output, signal, viewChild } from '@angular/core';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { ALIAS_MAX, renameProblem } from '../../../../core/game/authoring/board-queue';
import { AuthorButtonDirective } from '../widgets/author-button.directive';

/**
 * A card's (or a site's) name in the scenario, changed where it stands in the Board tab: Enter (or Rename) changes
 * it, Escape (or Cancel) leaves it. A name the rule refuses, or that is taken, is marked with why. The server puts
 * the new name wherever the scenario said the old one, in a fresh game. Says `closed` when it is done with either way.
 *
 * DOM contract: `gd-author-board-rename` holding `input.gd-author-input` (`maxlength` 40; `data-bad` with its reason
 * as its `title`), `button[data-action=rename]` and `button[data-action=cancel]`.
 */
@Component({
  selector: 'gd-author-board-rename',
  styleUrl: './board-rename.component.css',
  imports: [AuthorButtonDirective],
  template: `
    <input
      #field
      class="gd-author-input"
      type="text"
      aria-label="New name"
      [attr.maxlength]="max"
      [value]="alias()"
      [attr.data-bad]="bad() === null ? null : ''"
      [title]="bad() ?? ''"
      (keydown.enter)="rename()"
      (keydown.escape)="leave()"
    />
    <button
      gdAuthorButton="primary"
      data-action="rename"
      title="A fresh game is played by the script back to here"
      (click)="rename()"
    >
      Rename
    </button>
    <button gdAuthorButton data-action="cancel" (click)="leave()">Cancel</button>
  `,
})
export class BoardRenameComponent {
  /** The name it has now. */
  readonly alias = input.required<string>();
  readonly closed = output();

  private readonly changes = inject(BoardChanges);
  private readonly field = viewChild.required<ElementRef<HTMLInputElement>>('field');
  protected readonly max = ALIAS_MAX;
  /** Why the name typed can't be had. */
  protected readonly bad = signal<string | null>(null);
  /** Done with (renamed, or left): nothing more is done. */
  private over = false;

  constructor() {
    afterNextRender(() => {
      const field = this.field().nativeElement;
      field.focus();
      field.select();
    });
  }

  protected leave(): void {
    if (this.over) return;
    this.over = true;
    this.closed.emit();
  }

  protected async rename(): Promise<void> {
    if (this.over) return;
    const to = this.field().nativeElement.value.trim();
    if (to === this.alias()) {
      this.leave();
      return;
    }
    const problem = renameProblem(to, this.changes.aliases());
    this.bad.set(problem);
    if (problem !== null) return;
    this.over = true;
    await this.changes.rename(this.alias(), to);
    this.closed.emit();
  }
}
