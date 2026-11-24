import { Component, input, output } from '@angular/core';

/** A field of a PC account form. */
export interface PcFormField {
  readonly name: string;
  readonly label: string;
  readonly type: 'text' | 'password';
  readonly autocomplete: string;
  readonly maxLength?: number;
}

/**
 * The small form the PC account opens in place (the alias list's Create and Link, My Account's Connect): its fields,
 * the refusal of its last submit, and its submit and Cancel buttons, disabled while a call is under way. It holds no
 * state: the values come in, each change and the submit and Cancel go out. Enter in a field submits.
 *
 * DOM contract: `gd-pc-form.gd-pc-form` (`data-place` = login | account) holding `form.gd-pc-form__form`
 * (`data-form`, `aria-label`) with a `label.gd-pc-form__field` per field (`data-field` = its name;
 * `span.gd-pc-form__label`, `input.gd-pc-form__input` with `name`, `type`, `autocomplete`, `maxlength`),
 * `p.gd-pc-form__note` (with a note), `p.gd-pc-form__error` (`role=alert`, with a refusal) and `div.gd-pc-form__actions` with
 * `button.gd-button[data-action=submit]` (type submit) and `button.gd-button[data-action=cancel]`.
 */
@Component({
  selector: 'gd-pc-form',
  styleUrl: './pc-form.component.css',
  host: { class: 'gd-pc-form', '[attr.data-place]': 'place()' },
  template: `
    <form
      class="gd-pc-form__form"
      [attr.data-form]="form()"
      [attr.aria-label]="label()"
      (submit)="$event.preventDefault(); submitted.emit()"
    >
      @for (field of fields(); track field.name) {
        <label class="gd-pc-form__field" [attr.data-field]="field.name">
          <span class="gd-pc-form__label">{{ field.label }}</span>
          <input
            class="gd-pc-form__input"
            [type]="field.type"
            [name]="field.name"
            [attr.autocomplete]="field.autocomplete"
            [attr.autocapitalize]="field.type === 'text' ? 'none' : null"
            [attr.spellcheck]="field.type === 'text' ? 'false' : null"
            [attr.maxlength]="field.maxLength ?? null"
            [value]="values()[field.name] ?? ''"
            (input)="changed(field.name, $event)"
          />
        </label>
      }
      @if (note(); as note) {
        <p class="gd-pc-form__note">{{ note }}</p>
      }
      @if (error(); as error) {
        <p class="gd-pc-form__error" role="alert">{{ error }}</p>
      }
      <div class="gd-pc-form__actions">
        <button type="submit" class="gd-button" data-action="submit" [disabled]="busy()">{{ submitLabel() }}</button>
        <button type="button" class="gd-button" data-action="cancel" [disabled]="busy()" (click)="cancelled.emit()">
          {{ cancelLabel() }}
        </button>
      </div>
    </form>
  `,
})
export class PcFormComponent {
  /** Where the form is: the login page's look, or My Account's. */
  readonly place = input<'login' | 'account'>('account');
  /** Its name (`data-form`) and what it is called (`aria-label`). */
  readonly form = input.required<string>();
  readonly label = input.required<string>();
  readonly fields = input.required<readonly PcFormField[]>();
  readonly values = input.required<Readonly<Record<string, string>>>();
  /** A line before the buttons on what submitting means, if any. */
  readonly note = input<string | null>(null);
  readonly error = input<string | null>(null);
  readonly submitLabel = input.required<string>();
  readonly cancelLabel = input.required<string>();
  readonly busy = input(false);

  readonly valueChange = output<{ name: string; value: string }>();
  readonly submitted = output();
  readonly cancelled = output();

  protected changed(name: string, event: Event): void {
    this.valueChange.emit({ name, value: (event.target as HTMLInputElement).value });
  }
}
