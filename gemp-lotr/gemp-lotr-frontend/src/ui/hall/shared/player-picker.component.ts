import { Component, computed, DestroyRef, inject, input, model, output, signal } from '@angular/core';
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
 * A field to choose ONE player in (playerPicker.js PlayerPicker): a text field with a ▾ button after it that opens
 * the whole list, as a select would; typing asks for the list again after a pause. The list's lines come from
 * `suggest` (e.g. the hall's `PlayerSearch.suggest`: the players in the hall, then other players matching from 2
 * letters), under their headings; notes are not choices. A name that is in no list can still be typed: the field's
 * text is the value. Down (or Alt+Down) opens the list and moves along it, Up moves back, Enter takes the line
 * chosen, Escape closes it; clicking an empty field opens it. Knows nothing of what the name is for.
 *
 * Inputs: `suggest` (required: the lines for what is typed), `value` (two-way: the field's text), `placeholder`,
 * `maxlength`, `fieldId` (the field's id, for a label), `disabled`. Output: `picked` (a name taken from the list).
 *
 * DOM contract: `gd-player-picker.gd-player-picker` holding `input.gd-player-picker__input` (`role=combobox`,
 * `aria-expanded`, `aria-controls`, `aria-activedescendant`), `button.gd-player-picker__toggle` (▾, `aria-label`
 * "Show the players in the hall") and, while open, `ul.gd-player-picker__menu` (`role=listbox`) of
 * `li.gd-player-picker__heading` and `li.gd-player-picker__note` (`role=presentation`) and
 * `li.gd-player-picker__option` (`role=option`, `aria-selected` on the line chosen, `data-name`).
 */
@Component({
  selector: 'gd-player-picker',
  styleUrl: './player-picker.component.css',
  host: { class: 'gd-player-picker' },
  template: `
    <input
      class="gd-player-picker__input"
      type="text"
      role="combobox"
      autocomplete="off"
      aria-autocomplete="list"
      [attr.id]="fieldId()"
      [attr.maxlength]="maxlength()"
      [attr.placeholder]="placeholder()"
      [attr.aria-expanded]="open()"
      [attr.aria-controls]="menuId"
      [attr.aria-activedescendant]="active() >= 0 ? menuId + '-' + active() : null"
      [disabled]="disabled()"
      [value]="value()"
      (input)="typed($event)"
      (click)="clicked()"
      (keydown)="key($event)"
      (blur)="close()"
    />
    <button
      type="button"
      class="gd-player-picker__toggle"
      tabindex="-1"
      [attr.aria-label]="toggleLabel"
      [attr.title]="toggleLabel"
      [disabled]="disabled()"
      (mousedown)="$event.preventDefault()"
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
  readonly maxlength = input<number | null>(null);
  readonly fieldId = input<string | null>(null);
  readonly disabled = input(false);
  readonly picked = output<string>();

  protected readonly toggleLabel = PICKER_TOGGLE;
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
  }

  protected typed(event: Event): void {
    this.value.set((event.target as HTMLInputElement).value);
    this.timers.clear(this.timer);
    this.timer = this.timers.after(PICKER_DELAY_MS, () => void this.show(this.value()));
  }

  /** An empty field opens the list like a dropdown. */
  protected clicked(): void {
    if (this.value() === '' && !this.open()) {
      void this.show('');
    }
  }

  /** ▾: the whole list, or closed if it is open. */
  protected toggle(): void {
    if (this.open()) {
      this.close();
    } else {
      void this.show('');
    }
  }

  protected key(event: KeyboardEvent): void {
    const count = this.names().length;
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      if (!this.open()) {
        void this.show(this.value());
      } else if (count > 0) {
        this.active.set((this.active() + 1) % count);
      }
    } else if (event.key === 'ArrowUp' && this.open()) {
      event.preventDefault();
      if (count > 0) {
        this.active.set(this.active() <= 0 ? count - 1 : this.active() - 1);
      }
    } else if (event.key === 'Enter' && this.open() && this.active() >= 0) {
      event.preventDefault();
      this.take(this.names()[this.active()] ?? this.value());
    } else if (event.key === 'Escape' && this.open()) {
      event.preventDefault();
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
    const items = await this.suggest()(term);
    if (asked !== this.asked) {
      return;
    }
    this.items.set(items);
    this.active.set(-1);
    this.open.set(true);
  }
}
