import { Component, computed, input, output } from '@angular/core';

/** An option: its value, shown as itself, or [value, what is shown], or [value, what is shown, its tooltip]. */
export type AuthorOption =
  string | readonly [value: string, label: string] | readonly [value: string, label: string, title: string];

/**
 * A native select of the panel's, from a list of options; says the value chosen. A value that isn't one of the
 * options shows the first.
 *
 * DOM contract: `select.gd-author-input` (`aria-label`, `title`) with an `option` per option (`title` where it has one).
 */
@Component({
  selector: 'gd-author-select',
  host: { style: 'display: contents' },
  template: `
    <select
      #pick
      class="gd-author-input"
      [attr.aria-label]="label() || null"
      [attr.title]="hint() || null"
      [disabled]="disabled()"
      (change)="changed.emit(pick.value)"
    >
      @for (option of choices(); track option.value) {
        <option
          [value]="option.value"
          [selected]="option.value === value()"
          [attr.title]="option.title || null"
          [textContent]="option.label"
        ></option>
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
      typeof option === 'string'
        ? { value: option, label: option, title: '' }
        : { value: option[0], label: option[1], title: option[2] ?? '' },
    ),
  );
}
