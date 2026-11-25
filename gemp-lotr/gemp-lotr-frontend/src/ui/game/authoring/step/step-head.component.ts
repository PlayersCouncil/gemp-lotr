import { Component, computed, inject } from '@angular/core';
import { AuthoringDecision } from '../../../../core/game/authoring/authoring-state';
import { unscripted } from '../../../../core/game/authoring/notices';
import { whereOf } from '../../../../core/game/authoring/step-model';
import { stepsOf } from '../../../../core/game/authoring/steps';
import { ASKED_WORDS, askedNamed } from '../../../../core/game/authoring/terms';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { StepEditor } from './step-editor';

/**
 * The head of the Step tab, which stays in sight however far down the tab is scrolled: ◀ and ▶ to the steps either
 * side, which step this is of how many, ▶▶ to the step the game is at (and from there, or past it, to the last), +
 * for the decision waiting that no step covers (as the Timeline's "Add a step for this decision"), what has become
 * of the step in the game, and what the decision waiting is for (the choice of the next skirmish...).
 *
 * DOM contract: `gd-author-step-head` holding `.gd-author-step-head` with `button[data-go]` (previous, next, current),
 * `.gd-author-step-head__name`, `button[data-author=add-here]`, `.gd-author-step-head__where` (`data-status`) and,
 * on the step the game is at, `.gd-author-step-head__asked` (`data-kind`): what the decision waiting is for.
 */
@Component({
  selector: 'gd-author-step-head',
  styleUrl: './step-head.component.css',
  imports: [AuthorButtonDirective],
  template: `
    @if (head(); as head) {
      <div class="gd-author-step-head">
        <button
          gdAuthorButton
          data-go="previous"
          title="The step before"
          [disabled]="head.number <= 1"
          (click)="editor.select(head.number - 1)"
        >
          ◀
        </button>
        <span class="gd-author-step-head__name">Step {{ head.number }} of {{ head.count }}</span>
        <button
          gdAuthorButton
          data-go="next"
          title="The step after"
          [disabled]="head.number >= head.count"
          (click)="editor.select(head.number + 1)"
        >
          ▶
        </button>
        <button
          gdAuthorButton
          data-go="current"
          title="To the step the game is at; from there or past it, to the last step"
          (click)="editor.select(head.current)"
        >
          ▶▶
        </button>
        <button
          gdAuthorButton
          data-author="add-here"
          title="Add a step for the decision that is waiting, which no step covers (as on the Timeline). It goes where that decision comes in the script, whatever step is shown here."
          [disabled]="!head.addHere"
          (click)="editor.authoring.stepHere()"
        >
          +
        </button>
        <span class="gd-author-step-head__where" [attr.data-status]="head.where.status">{{ head.where.words }}</span>
        @if (head.asked; as asked) {
          <span class="gd-author-step-head__asked" [attr.data-kind]="asked.kind" [title]="asked.title">{{
            asked.words
          }}</span>
        }
      </div>
    }
  `,
})
export class StepHeadComponent {
  protected readonly editor = inject(StepEditor);

  /** What the decision waiting is for, where it is one a When can name (B5): it says why a step does or does not take it. */
  private asked(decision: AuthoringDecision | null) {
    const kind = askedNamed(decision?.kind ?? null);
    if (!decision || kind === null) return null;
    return {
      kind,
      words: ASKED_WORDS[kind],
      title: `The decision waiting ("${decision.text}") is for ${kind}: a When with ${kind} takes it; not(${kind}) or another such term does not.`,
    };
  }

  protected readonly head = computed(() => {
    const draft = this.editor.authoring.draft();
    const place = this.editor.place();
    if (!draft || !place) return null;
    const number = place.index + 1;
    const count = stepsOf(draft).length;
    const at = draft.position.step;
    return {
      number,
      count,
      current: number >= at || at > count ? count : at,
      addHere: unscripted(draft),
      where: whereOf(draft, place.index, -1),
      asked: number === at ? this.asked(draft.waiting.human ?? draft.waiting.opponent) : null,
    };
  });
}
