import { afterRenderEffect, Component, computed, ElementRef, inject, ViewEncapsulation } from '@angular/core';
import { isBlock, stepsOf } from '../../../core/game/authoring/steps';
import { BlockPageComponent } from './step/block-page.component';
import { SkirmishPageComponent } from './step/skirmish-page.component';
import { StepActionsComponent } from './step/step-actions.component';
import { StepEditor } from './step/step-editor';
import { StepFieldsComponent } from './step/step-fields.component';
import { StepHeadComponent } from './step/step-head.component';

/** After the author leaves a field, the selection catches up with the game this much later (ms), as legacy did. */
export const CATCH_UP_MS = 300;

/**
 * The authoring panel's Step tab (the legacy scenarioAuthor.js renderStep / buildStepForm): the step selected, as a
 * form — its head (gd-author-step-head), what can be done with it as a whole (gd-author-step-actions), then for a
 * step of the script its fields (gd-author-step-fields), for a block its page of sections (gd-author-block-page), or
 * for a turn's skirmishes the page laid out by skirmish (gd-author-skirmish-page). The form is a working copy of the
 * step (StepEditor) that only the author's typing changes: what the server answers never takes the focus, moves the
 * caret or writes over what was typed since. Turned to another step, the tab starts at its top. A message whose card
 * had the focus is put on the table again whenever its step is.
 *
 * Its stylesheet is not scoped: it also draws what the step's parts share (`.gd-author-step__section`,
 * `__heading`, `__short`, `__block`); every rule starts at `gd-game-author-step`.
 *
 * DOM contract: `gd-game-author-step` holding, with a step selected, `gd-author-step-head`, `gd-author-step-actions`
 * and one of `gd-author-step-fields`, `gd-author-block-page`, `gd-author-skirmish-page` (`data-uid` on the host: the
 * form's step); else `.gd-author-note[data-empty]`.
 */
@Component({
  selector: 'gd-game-author-step',
  styleUrl: './step-tab.component.css',
  encapsulation: ViewEncapsulation.None,
  providers: [StepEditor],
  imports: [StepHeadComponent, StepActionsComponent, StepFieldsComponent, BlockPageComponent, SkirmishPageComponent],
  host: {
    '[attr.data-uid]': 'editor.form()?.uid ?? null',
    '(focusout)': 'left()',
  },
  template: `
    @if (page(); as page) {
      <gd-author-step-head />
      <gd-author-step-actions />
      @switch (page) {
        @case ('skirmishes') {
          <gd-author-skirmish-page />
        }
        @case ('block') {
          <gd-author-block-page />
        }
        @default {
          <gd-author-step-fields />
        }
      }
    } @else if (empty(); as empty) {
      <p class="gd-author-note" data-empty [title]="empty[1]">{{ empty[0] }}</p>
    }
  `,
})
export class StepTabComponent {
  protected readonly editor = inject(StepEditor);
  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  /** Which page the step has: its fields, a block's, or a turn's skirmishes. */
  protected readonly page = computed(() => {
    const top = this.editor.top();
    if (!top || !this.editor.work()) return null;
    return !isBlock(top) ? 'fields' : top.skirmishes ? 'skirmishes' : 'block';
  });
  protected readonly empty = computed<readonly [string, string] | null>(() => {
    const draft = this.editor.authoring.draft();
    if (!draft) return null;
    return stepsOf(draft).length === 0
      ? [
          'No steps yet: play, and what you do becomes steps.',
          'The script has no steps yet. Play: what you do becomes steps. Or write a step for the decision in front of you (Timeline).',
        ]
      : ['Select a step in the timeline.', ''];
  });
  /** The step of the script the tab was last at: turned to another, the tab starts at its top. */
  private turnedFrom: string | null = null;

  constructor() {
    this.editor.element = this.element;
    afterRenderEffect(() => {
      const form = this.editor.form();
      const place = this.editor.place();
      if (!form || !place) return;
      const top = `${place.index}:${place.sub < 0 ? form.uid : ''}`;
      if (top === this.turnedFrom) return;
      const first = this.turnedFrom === null;
      this.turnedFrom = top;
      this.editor.panelShown = null;
      if (!first && place.sub < 0) scrollerOf(this.element)?.scrollTo({ top: 0 });
    });
    // Where the focus was on the page before a fresh game, it is again
    afterRenderEffect(() => {
      const due = this.editor.focusDue();
      if (!due || !this.editor.place()) return;
      this.editor.focusDue.set(null);
      const field = [...this.element.querySelectorAll<HTMLInputElement | HTMLTextAreaElement>('[data-field]')].find(
        (one) => one.dataset['field'] === due.field,
      );
      if (!field) return;
      field.focus();
      field.setSelectionRange(due.start, due.end);
    });
  }

  /** A field was left: the selection catches up with the game, once the author is done (legacy focusout). */
  protected left(): void {
    setTimeout(() => this.editor.authoring.catchUp(), CATCH_UP_MS);
  }
}

/** The element the tab scrolls in (the panel's body). */
function scrollerOf(element: HTMLElement): HTMLElement | null {
  for (let at = element.parentElement; at; at = at.parentElement) {
    if (/(auto|scroll)/.test(getComputedStyle(at).overflowY)) return at;
  }
  return null;
}
