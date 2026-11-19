import { Component, computed, inject, signal } from '@angular/core';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { AuthorButtonDirective } from '../widgets/author-button.directive';

type Counter = 'twilight' | 'burdens' | 'threats';

const COUNTERS: readonly { name: Counter; label: string }[] = [
  { name: 'twilight', label: 'Twilight' },
  { name: 'burdens', label: 'Burdens' },
  { name: 'threats', label: 'Threats' },
];

/**
 * The Board tab's totals: the twilight (not in the setup, which has no twilight pool), the burdens and the threats,
 * each a number and Set, which gathers it. A total a change has been gathered for is marked. What the author has
 * typed stays in its field until it is set, whatever the state that comes meanwhile says.
 *
 * DOM contract: `gd-author-board-counters` holding a `.gd-author-board-counters__row` per total (`data-counter`:
 * twilight, burdens, threats; `data-changed`) with `.gd-author-label`, `input[type=number].gd-author-input` and
 * `button[data-action=set]`.
 */
@Component({
  selector: 'gd-author-board-counters',
  styleUrl: './board-counters.component.css',
  imports: [AuthorButtonDirective],
  template: `
    @if (changes.view(); as view) {
      @for (counter of counters; track counter.name) {
        <div
          class="gd-author-board-counters__row"
          [attr.data-counter]="counter.name"
          [attr.data-changed]="view[counter.name] !== board()?.[counter.name] ? '' : null"
        >
          <span class="gd-author-label">{{ counter.label }}</span>
          <input
            class="gd-author-input"
            type="number"
            min="0"
            [attr.aria-label]="counter.label"
            [value]="typed()[counter.name] ?? view[counter.name]"
            (input)="type(counter.name, $any($event.target).value)"
          />
          <button
            gdAuthorButton
            data-action="set"
            title="Gathered with the other changes, and made when they are submitted"
            [disabled]="counter.name === 'twilight' && changes.inSetup()"
            (click)="set(counter.name, view[counter.name])"
          >
            Set
          </button>
        </div>
      }
    }
  `,
})
export class BoardCountersComponent {
  protected readonly changes = inject(BoardChanges);
  protected readonly counters = COUNTERS;
  protected readonly board = computed(() => this.changes.board());
  /** What has been typed in each field and not set yet. */
  protected readonly typed = signal<Partial<Record<Counter, string>>>({});

  protected type(name: Counter, value: string): void {
    this.typed.update((typed) => ({ ...typed, [name]: value }));
  }

  /** `shown`: what the field shows when nothing has been typed in it. */
  protected set(name: Counter, shown: number): void {
    const text = this.typed()[name] ?? String(shown);
    this.typed.update((typed) => ({ ...typed, [name]: undefined }));
    this.changes.queue(name, [parseInt(text, 10) || 0]);
  }
}
