import { Component, computed, inject, input } from '@angular/core';
import { valueAt } from '../../../../core/game/authoring/step-model';
import { Asked, ASKED, ASKED_WORDS, askedNamed, askedOf, putAsked } from '../../../../core/game/authoring/terms';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { StepEditor } from './step-editor';

const HINT =
  'What the decision is for, whatever kind of decision it comes as: the choice of the next skirmish, an archery wound, ' +
  'reconciling, sanctuary healing, "make another move?", optional or required responses, choosing a site, or a ' +
  "phase's own play-or-pass. Not: any decision but that one.";

/**
 * The When composer's "what is being asked" (B5), for a When, an Until or a Skip when (`path`): a select of what a
 * decision can be for, and "not", which puts that term (`nextSkirmish`, `not(nextSkirmish)`) into the line in its
 * place, keeping the other terms, and sends it. Not shown for one written out in full, nor for an Until or a Skip when
 * that says nothing yet.
 *
 * DOM contract: `gd-author-asked` holding `.gd-author-asked` (`data-path`) with `gd-author-select[data-picker=asked]`
 * and `label.gd-author-check` with `input[data-picker=not]`.
 */
@Component({
  selector: 'gd-author-asked',
  styleUrl: './asked-picker.component.css',
  imports: [AuthorSelectComponent],
  template: `
    @if (picked(); as picked) {
      <span class="gd-author-asked" [attr.data-path]="path()">
        <gd-author-select
          data-picker="asked"
          label="What is being asked"
          [hint]="hint"
          [options]="options"
          [value]="picked.asked"
          (changed)="pick($event, picked.not)"
        />
        <label class="gd-author-check" title="Any decision but this one">
          <input
            type="checkbox"
            data-picker="not"
            [checked]="picked.not"
            [disabled]="picked.asked === ''"
            (change)="pick(picked.asked, $any($event.target).checked)"
          />
          not
        </label>
      </span>
    }
  `,
})
export class AskedPickerComponent {
  /** The field of the form it puts its term into: when, until or skipWhen. */
  readonly path = input('when');

  private readonly editor = inject(StepEditor);
  protected readonly hint = HINT;
  protected readonly options: readonly AuthorOption[] = [
    ['', 'anything asked'],
    ...ASKED.map((asked) => [asked, ASKED_WORDS[asked]] as const),
  ];

  private readonly value = computed(() => {
    const value = valueAt(this.editor.work(), this.path());
    return typeof value === 'string' ? value : '';
  });
  protected readonly picked = computed(() =>
    this.editor.work() && (this.path() === 'when' || this.value().trim() !== '') ? askedOf(this.value()) : null,
  );

  protected pick(value: string, not: boolean): void {
    const asked: Asked | '' = askedNamed(value) ?? '';
    this.editor.put(this.path(), putAsked(this.value(), asked, asked !== '' && not));
    this.editor.now();
  }
}
