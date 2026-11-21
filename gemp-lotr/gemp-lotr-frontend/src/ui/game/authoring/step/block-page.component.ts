import { Component, computed, inject } from '@angular/core';
import { childPause, childStep } from '../../../../core/game/authoring/step-ops';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { uncoveredIn } from '../../../../core/game/authoring/skirmish-page';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { BlockPanelComponent } from './block-panel.component';
import { StepEditor } from './step-editor';

/**
 * A block's page (legacy buildStepForm for a block): what a block is, the block's own section, then a section for
 * each of its steps, and + Step (for the decision waiting, when the game is in this block and none of its steps takes
 * it) / + Pause after them.
 *
 * DOM contract: `gd-author-block-page` holding `.gd-author-note`, `gd-author-block-panel`s (the block's own first),
 * `.gd-author-step__heading[data-kids]` and `.gd-author-bar` with `button[data-add]` step, pause.
 */
@Component({
  selector: 'gd-author-block-page',
  styleUrl: './block-page.component.css',
  imports: [AuthorButtonDirective, BlockPanelComponent],
  template: `
    @if (view(); as view) {
      <div
        class="gd-author-note"
        title="A block: steps for a stretch of the game whose decisions come in no order the script can know. Each decision goes to the first of its steps that fits it, wherever in the list that step comes, until a decision arrives that fits the block's Until."
      >
        A block: its steps take its decisions in any order.
      </div>
      <gd-author-block-panel [sub]="-1" />
      <div class="gd-author-step__heading" [attr.data-kids]="view.count">
        {{
          view.count === 0
            ? 'It has no steps yet'
            : 'Its ' + view.count + ' step' + (view.count === 1 ? '' : 's') + ', in any order'
        }}
      </div>
      @for (kid of view.kids; track kid) {
        <gd-author-block-panel [sub]="$index" />
      }
      <div class="gd-author-bar">
        <button
          gdAuthorButton="primary"
          data-add="step"
          title="Another step in this block, after the others. It says nothing yet: what you do at its decision is written in. (With the game in this block, and a decision waiting that none of its steps takes, it is the step for that decision.)"
          (click)="addStep(view)"
        >
          + Step
        </button>
        <button
          gdAuthorButton
          data-add="pause"
          title="A step of this block that is only read: once, over the first of the player's decisions that fits it"
          (click)="addPause(view)"
        >
          + Pause
        </button>
      </div>
    }
  `,
})
export class BlockPageComponent {
  protected readonly editor = inject(StepEditor);

  protected readonly view = computed(() => {
    const draft = this.editor.authoring.draft();
    const place = this.editor.place();
    if (!draft || !place) return null;
    const kids = draft.kids[place.index] ?? [];
    return { draft, index: place.index, count: kids.length, kids };
  });

  /** + Step: for the decision waiting, where it is this block's; else a step with no answer yet, after the others. */
  protected addStep(view: BlockAt): void {
    if (uncoveredIn(view.draft, view.index)) void this.editor.authoring.stepHere();
    else void this.editor.insertInBlock(view.index, view.count, childStep(view.draft), true);
  }

  protected addPause(view: BlockAt): void {
    void this.editor.insertInBlock(view.index, view.count, childPause(view.draft));
  }
}

/** A block's page: the draft, the block (from 0), how many steps it has. */
export interface BlockAt {
  draft: DraftState;
  index: number;
  count: number;
}
