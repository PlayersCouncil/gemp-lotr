import { Component, ElementRef, inject, input, output, signal } from '@angular/core';

/** One line of the list: a name under its heading, or a note that is not a choice. */
export type NameSuggestion = { readonly name: string; readonly heading: string } | { readonly note: string };

/** Where the suggestions come from: what was typed in, the lines to offer out. */
export type NameSuggestionSource = (term: string) => Promise<readonly NameSuggestion[]>;

/** How long typing must pause before the suggestions are asked for (playerPicker.js: the autocomplete's delay). */
export const SUGGEST_DELAY_MS = 250;

/**
 * A field for one name, with suggestions (playerPicker.js PlayerPicker): typing filters the list (asked from
 * `suggest` after a short pause); the ▾ button, a click on the empty field, Down on it or Alt+Down open the whole list,
 * as a select would. The list's names are grouped under their headings; notes are not choices. Up / Down move in the
 * list, Enter picks, Escape closes. A name that is not in the list can still be typed: `valueChange` reports every
 * change, picked or typed. Generic: what it offers is the page's.
 *
 * DOM contract: `gd-name-suggest.gd-name-suggest` (`data-disabled`) holding `input.gd-name-suggest__input`
 * (`role=combobox`, `aria-expanded`, `aria-controls`), `button.gd-name-suggest__toggle` ("▾", `aria-label`) and, while
 * open, `ul.gd-name-suggest__list` (`role=listbox`) of `li.gd-name-suggest__heading` (`role=presentation`),
 * `li.gd-name-suggest__note` and `li.gd-name-suggest__option` (`role=option`, `aria-selected` on the one chosen with the
 * keys).
 */
@Component({
  selector: 'gd-name-suggest',
  styleUrl: './name-suggest.component.css',
  host: {
    class: 'gd-name-suggest',
    '[attr.data-disabled]': "disabled() ? '' : null",
    '(focusout)': 'focusLeft($event)',
  },
  template: `
    <input
      class="gd-name-suggest__input"
      type="text"
      autocomplete="off"
      role="combobox"
      aria-autocomplete="list"
      [attr.aria-expanded]="open()"
      [attr.aria-controls]="listId"
      [attr.aria-label]="label()"
      [attr.maxlength]="maxLength()"
      [placeholder]="placeholder()"
      [disabled]="disabled()"
      [value]="value()"
      (input)="typed($any($event.target).value)"
      (click)="value() === '' && !open() && show('')"
      (keydown)="key($event)"
    />
    <button
      type="button"
      class="gd-name-suggest__toggle"
      tabindex="-1"
      [attr.aria-label]="toggleLabel()"
      [title]="toggleLabel()"
      [disabled]="disabled()"
      (mousedown)="wasOpen = open()"
      (click)="wasOpen ? close() : show('')"
    >
      ▾
    </button>
    @if (open()) {
      <ul class="gd-name-suggest__list" role="listbox" [id]="listId">
        @for (item of items(); track $index) {
          @if (isHeading(item, $index)) {
            <li class="gd-name-suggest__heading" role="presentation">{{ asName(item).heading }}</li>
          }
          @if (asNote(item); as note) {
            <li class="gd-name-suggest__note" role="presentation">{{ note }}</li>
          } @else {
            <li
              class="gd-name-suggest__option"
              role="option"
              tabindex="-1"
              [attr.aria-selected]="$index === active()"
              (mousedown)="$event.preventDefault()"
              (click)="pick(asName(item).name)"
              (keydown.enter)="pick(asName(item).name)"
            >
              {{ asName(item).name }}
            </li>
          }
        }
      </ul>
    }
  `,
})
export class NameSuggestComponent {
  private static ids = 0;
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  readonly value = input('');
  readonly suggest = input.required<NameSuggestionSource>();
  readonly disabled = input(false);
  readonly placeholder = input('');
  readonly label = input('');
  readonly toggleLabel = input('');
  readonly maxLength = input<number | null>(null);
  /** The name changed, picked or typed. */
  readonly valueChange = output<string>();

  protected readonly listId = `gd-name-suggest-${++NameSuggestComponent.ids}`;
  protected readonly open = signal(false);
  protected readonly items = signal<readonly NameSuggestion[]>([]);
  protected readonly active = signal(-1);
  protected wasOpen = false;
  private timer: ReturnType<typeof setTimeout> | null = null;
  private asked = 0;

  protected asName(item: NameSuggestion): { name: string; heading: string } {
    return 'name' in item ? item : { name: '', heading: '' };
  }

  protected asNote(item: NameSuggestion): string | null {
    return 'note' in item ? item.note : null;
  }

  /** A heading goes before the first name of its group. */
  protected isHeading(item: NameSuggestion, index: number): boolean {
    if (!('name' in item)) {
      return false;
    }
    const before = this.items()[index - 1];
    return before === undefined || !('name' in before) || before.heading !== item.heading;
  }

  protected typed(text: string): void {
    this.valueChange.emit(text);
    if (this.timer !== null) {
      clearTimeout(this.timer);
    }
    this.timer = setTimeout(() => void this.show(text), SUGGEST_DELAY_MS);
  }

  protected async show(term: string): Promise<void> {
    const ask = ++this.asked;
    const items = await this.suggest()(term.trim());
    // an answer to an older search is ignored
    if (ask !== this.asked || this.disabled()) {
      return;
    }
    this.items.set(items);
    this.active.set(-1);
    this.open.set(true);
  }

  protected close(): void {
    this.open.set(false);
    this.wasOpen = false;
  }

  protected pick(name: string): void {
    this.valueChange.emit(name);
    this.close();
  }

  protected key(event: KeyboardEvent): void {
    const names = this.items()
      .map((item, index) => ('name' in item ? index : -1))
      .filter((index) => index >= 0);
    if (event.key === 'ArrowDown' && (!this.open() || event.altKey)) {
      event.preventDefault();
      void this.show(event.altKey ? this.value() : '');
      return;
    }
    if (!this.open()) {
      return;
    }
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      const at = names.indexOf(this.active());
      const next = event.key === 'ArrowDown' ? Math.min(names.length - 1, at + 1) : Math.max(0, at - 1);
      this.active.set(names[next] ?? -1);
    } else if (event.key === 'Enter' && this.items()[this.active()] !== undefined) {
      event.preventDefault();
      const item = this.items()[this.active()];
      if (item !== undefined) {
        this.pick(this.asName(item).name);
      }
    } else if (event.key === 'Escape') {
      event.preventDefault();
      event.stopPropagation();
      this.close();
    }
  }

  protected focusLeft(event: FocusEvent): void {
    if (!(event.relatedTarget instanceof Node) || !this.host.contains(event.relatedTarget)) {
      this.close();
    }
  }
}
