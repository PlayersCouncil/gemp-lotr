import { computed, Directive, ElementRef, inject, input } from '@angular/core';
import { valueAt } from '../../../../core/game/authoring/step-model';
import { StepEditor } from './step-editor';

/**
 * A text field of the Step tab's form, bound to a part of the working copy by its path (`gdStepField="when"`,
 * `"messages.0.text"`): what is typed goes into the working copy at once; it is sent when the field is left (or Enter
 * is pressed in a line), and, with `sends="typing"` (a message's fields), also a moment after typing pauses. The
 * field shows the working copy's value, which nothing but the author's typing changes while they are in it.
 *
 * DOM contract: `data-field` the path.
 */
@Directive({
  selector: 'input[gdStepField], textarea[gdStepField]',
  host: {
    '[value]': 'shown()',
    '[attr.data-field]': 'path()',
    '(input)': 'typed()',
    '(change)': 'editor.now()',
    '(keydown.enter)': 'enter()',
  },
})
export class StepFieldDirective {
  readonly path = input.required<string>({ alias: 'gdStepField' });
  /** When it is sent: when it is left (`change`), or also as it is typed in (`typing`). */
  readonly sends = input<'change' | 'typing'>('change');

  protected readonly editor = inject(StepEditor);
  private readonly field = inject<ElementRef<HTMLInputElement | HTMLTextAreaElement>>(ElementRef).nativeElement;

  protected readonly shown = computed(() => {
    const value = valueAt(this.editor.work(), this.path());
    return value === undefined || value === null ? '' : typeof value === 'string' ? value : JSON.stringify(value);
  });

  protected typed(): void {
    this.editor.put(this.path(), this.field.value);
    if (this.sends() === 'typing') this.editor.later();
  }

  protected enter(): void {
    if (this.field instanceof HTMLInputElement) this.editor.now();
  }
}
