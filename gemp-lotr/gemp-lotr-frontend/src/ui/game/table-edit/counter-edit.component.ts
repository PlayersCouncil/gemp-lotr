import { Component, computed, inject, input } from '@angular/core';
import { COUNTER_LABELS, EditCounter } from '../../../core/game/table-edit/edit-counters';
import { CounterPopups } from './counter-popups';

/**
 * A counter of the game (the twilight pool, the burdens, the threats) where it is shown, while the table is edited by
 * hand with the `counters` tool: its number reads as a link, and pressing it opens the counter's box by it
 * (gd-edit-counter-box: − and +, the number to set it to). The host shows this only in edit mode; in play mode it
 * shows its own number as always.
 *
 * DOM contract: `gd-edit-counter` holding `button.gd-edit-counter[data-edit-counter]` (twilight, burdens, threats;
 * `aria-expanded` while its box is open) with the number shown.
 */
@Component({
  selector: 'gd-edit-counter',
  styleUrl: './counter-edit.component.css',
  template: `
    <button
      #opener
      type="button"
      class="gd-edit-counter"
      [attr.data-edit-counter]="counter()"
      [attr.aria-expanded]="open()"
      [title]="'Change ' + label()"
      (click)="popups.toggle(counter(), opener)"
    >
      {{ shown() }}
    </button>
  `,
})
export class CounterEditComponent {
  /** Which counter. */
  readonly counter = input.required<EditCounter>();
  /** What the host shows for it (the page's own number: "3", or the threats' "1/2"). */
  readonly shown = input<string | number | null>('');

  protected readonly popups = inject(CounterPopups);
  protected readonly label = computed(() => COUNTER_LABELS[this.counter()]);
  protected readonly open = computed(() => this.popups.open()?.counter === this.counter());
}
