import {
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  input,
  model,
  output,
  signal,
  untracked,
} from '@angular/core';
import { PICKER_TOGGLE } from '../../../core/hall/play/play-messages';
import { PickerItem } from '../../../core/hall/play/player-picker';
import { TimerHandle, Timers } from '../../../core/platform/timers';

/** playerPicker.js: the list is asked for this long (ms) after the last key. */
export const PICKER_DELAY_MS = 250;

let pickers = 0;

/** One line of the menu as drawn: a heading where the items' heading changes, then the item or note. */
type MenuLine =
  | { readonly kind: 'heading'; readonly text: string }
  | { readonly kind: 'note'; readonly text: string }
  | { readonly kind: 'option'; readonly name: string; readonly index: number };

/**
 * A field to choose ONE player in (playerPicker.js PlayerPicker), the one picker of the hall: My Account's "Ignore a
 * player" and the Casual table's invitee. A text field with a ▾ button after it that opens the whole list, as a select
 * would; typing asks for the list again after a pause (250 ms, the autocomplete's delay). The list's lines come from
 * `suggest` (the hall's `PlayerSearch.suggest`: the players in the hall, then other players matching from 2 letters),
 * under their headings; notes are not choices. A name in no list can still be typed: the field's text is the value.
 *
 * Keys and clicks as the legacy jQuery UI autocomplete had them: a click on the empty field, Down on the empty field
 * or Alt+Down open the whole list; while it is open Down and Up move along it (past either end back to the field,
 * nothing chosen), Enter takes the line chosen, Escape closes it (and goes no further, so a dialog around it stays
 * open). ▾ toggles it. Leaving the field closes it, as does being disabled. Knows nothing of what the name is for.
 *
 * Inputs: `suggest` (required: the lines for what is typed), `value` (two-way, `valueChange` on every change, picked
 * or typed), `placeholder`, `label` (the field's accessible name when no `label` element names it), `fieldId` (the
 * field's id, for a `label for`), `toggleLabel` (▾'s name, "Show the players in the hall"), `maxLength`, `disabled`.
 * Output: `picked` (a name taken from the list).
 *
 * DOM contract: `gd-player-picker.gd-player-picker` (`data-disabled`) holding `input.gd-player-picker__input`
 * (`role=combobox`, `aria-expanded`, `aria-controls`, `aria-activedescendant`), `button.gd-player-picker__toggle` (▾,
 * `aria-label`) and, while open, `ul.gd-player-picker__menu` (`role=listbox`) of `li.gd-player-picker__heading` and
 * `li.gd-player-picker__heading.gd-player-picker__note` (`role=presentation`) and `li.gd-player-picker__option`
 * (`role=option`, `aria-selected` on the line chosen, `data-name`).
 */
@Component({
  selector: 'gd-player-picker',
  styleUrl: './player-picker.component.css',
  host: {
    class: 'gd-player-picker',
    '[attr.data-disabled]': "disabled() ? '' : null",
    '(focusout)': 'focusLeft($event)',
  },
  template: `
    <input
      class="gd-player-picker__input"
      type="text"
      role="combobox"
      autocomplete="off"
      aria-autocomplete="list"
      [attr.id]="fieldId()"
      [attr.aria-label]="label()"
      [attr.maxlength]="maxLength()"
      [attr.placeholder]="placeholder()"
      [attr.aria-expanded]="open()"
      [attr.aria-controls]="menuId"
      [attr.aria-activedescendant]="active() >= 0 ? menuId + '-' + active() : null"
      [disabled]="disabled()"
      [value]="value()"
      (input)="typed($event)"
      (click)="clicked()"
      (keydown)="key($event)"
    />
    <button
      type="button"
      class="gd-player-picker__toggle"
      tabindex="-1"
      [attr.aria-label]="toggleLabel()"
      [attr.title]="toggleLabel()"
      [disabled]="disabled()"
      (mousedown)="wasOpen = open()"
      (click)="toggle()"
    >
      ▾
    </button>
    @if (open()) {
      <ul class="gd-player-picker__menu" role="listbox" [attr.id]="menuId" (mousedown)="$event.preventDefault()">
        @for (line of lines(); track $index) {
          @switch (line.kind) {
            @case ('heading') {
              <li class="gd-player-picker__heading" role="presentation">{{ line.text }}</li>
            }
            @case ('note') {
              <li class="gd-player-picker__heading gd-player-picker__note" role="presentation">{{ line.text }}</li>
            }
            @case ('option') {
              <li
                class="gd-player-picker__option"
                role="option"
                [attr.id]="menuId + '-' + line.index"
                [attr.data-name]="line.name"
                [attr.aria-selected]="line.index === active()"
                tabindex="-1"
                (mouseenter)="active.set(line.index)"
                (click)="take(line.name)"
                (keydown.enter)="take(line.name)"
              >
                {{ line.name }}
              </li>
            }
          }
        }
      </ul>
    }
  `,
})
export class PlayerPickerComponent {
  private readonly timers = inject(Timers);

  readonly suggest = input.required<(term: string) => Promise<readonly PickerItem[]>>();
  readonly value = model('');
  readonly placeholder = input<string | null>(null);
  readonly maxLength = input<number | null>(null);
  readonly label = input<string | null>(null);
  readonly fieldId = input<string | null>(null);
  readonly toggleLabel = input(PICKER_TOGGLE);
  readonly disabled = input(false);
  readonly picked = output<string>();

  /** The list was open when ▾ was pressed (pressing it takes the focus from the field, which closes the list). */
  protected wasOpen = false;
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  protected readonly menuId = `gd-player-picker-${++pickers}`;
  protected readonly open = signal(false);
  protected readonly active = signal(-1);
  private readonly items = signal<readonly PickerItem[]>([]);
  private timer: TimerHandle | null = null;
  private asked = 0;

  protected readonly lines = computed<MenuLine[]>(() => {
    const lines: MenuLine[] = [];
    let heading: string | null = null;
    let index = 0;
    for (const item of this.items()) {
      if (item.kind === 'note') {
        lines.push({ kind: 'note', text: item.text });
        continue;
      }
      if (item.heading !== heading) {
        heading = item.heading;
        lines.push({ kind: 'heading', text: heading });
      }
      lines.push({ kind: 'option', name: item.name, index: index++ });
    }
    return lines;
  });
  private readonly names = computed(() => this.items().flatMap((item) => (item.kind === 'player' ? [item.name] : [])));

  constructor() {
    inject(DestroyRef).onDestroy(() => this.timers.clear(this.timer));
    // PlayerPicker.enable(false) closes the list
    effect(() => {
      if (this.disabled()) {
        untracked(() => this.close());
      }
    });
  }

  protected typed(event: Event): void {
    this.value.set((event.target as HTMLInputElement).value);
    this.timers.clear(this.timer);
    this.timer = this.timers.after(PICKER_DELAY_MS, () => void this.show(this.value()));
  }

  /** An empty field opens the list like a dropdown. */
  protected clicked(): void {
    if (this.value().trim() === '' && !this.open()) {
      void this.show('');
    }
  }

  /** ▾: the whole list, or closed if it was open; the focus goes to the field. */
  protected toggle(): void {
    if (this.wasOpen) {
      this.wasOpen = false;
      this.close();
      return;
    }
    this.host.querySelector('input')?.focus();
    void this.show('');
  }

  protected key(event: KeyboardEvent): void {
    if (!this.open()) {
      // Down on an empty field, or Alt+Down, opens the whole list, as on a select
      if (event.key === 'ArrowDown' && (event.altKey || this.value().trim() === '')) {
        event.preventDefault();
        void this.show('');
      }
      return;
    }
    const count = this.names().length;
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      // the menu's next / previous: past either end back to the field, from the field to the first / last line
      const at = this.active();
      const next = event.key === 'ArrowDown' ? (at < 0 ? 0 : at + 1) : at < 0 ? count - 1 : at - 1;
      this.active.set(next >= count ? -1 : next);
    } else if (event.key === 'Enter' && this.active() >= 0) {
      event.preventDefault();
      this.take(this.names()[this.active()] ?? this.value());
    } else if (event.key === 'Escape') {
      event.preventDefault();
      event.stopPropagation();
      this.close();
    }
  }

  /** The focus left the picker (not to its own button or list): the list closes. */
  protected focusLeft(event: FocusEvent): void {
    if (!(event.relatedTarget instanceof Node) || !this.host.contains(event.relatedTarget)) {
      this.close();
    }
  }

  protected take(name: string): void {
    this.value.set(name);
    this.close();
    this.picked.emit(name);
  }

  close(): void {
    this.timers.clear(this.timer);
    this.asked++;
    this.open.set(false);
    this.active.set(-1);
  }

  /** Asks for the lines of `term` and opens the list with them (an answer for an older term is dropped). */
  private async show(term: string): Promise<void> {
    const asked = ++this.asked;
    const items = await this.suggest()(term.trim());
    if (asked !== this.asked || this.disabled()) {
      return;
    }
    this.items.set(items);
    this.active.set(-1);
    this.open.set(true);
  }
}
