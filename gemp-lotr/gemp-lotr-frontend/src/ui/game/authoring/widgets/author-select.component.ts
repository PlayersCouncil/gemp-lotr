import { Component, computed, input, output } from '@angular/core';

/** An option: its value, shown as itself, or [value, what is shown]. */
export type AuthorOption = string | readonly [value: string, label: string];

/**
 * A native select of the panel's, from a list of options; says the value chosen. A value that isn't one of the
 * options shows the first.
 *
 * DOM contract: `select.gd-author-input` (`aria-label`, `title`) with an `option` per option.
 */
@Component({
  selector: 'gd-author-select',
  host: { style: 'display: contents' },
  template: `
    <select
      class="gd-author-input"
      [attr.aria-label]="label() || null"
      [attr.title]="hint() || null"
      [disabled]="disabled()"
      (change)="changed.emit($any($event.target).value)"
    >
      @for (option of choices(); track option.value) {
        <option [value]="option.value" [selected]="option.value === value()">{{ option.label }}</option>
      }
    </select>
  `,
})
export class AuthorSelectComponent {
  readonly options = input.required<readonly AuthorOption[]>();
  readonly value = input<string | null>(null);
  /** What it is for, when no label says so. */
  readonly label = input('');
  readonly disabled = input(false);
  readonly changed = output<string>();
  /** Its explanation, as its tooltip. */
  readonly hint = input('');

  protected readonly choices = computed(() =>
    this.options().map((option) =>
      typeof option === 'string' ? { value: option, label: option } : { value: option[0], label: option[1] },
    ),
  );
}
