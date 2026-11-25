import {
  afterNextRender,
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  Injector,
  linkedSignal,
  untracked,
} from '@angular/core';
import {
  COUNTER_LABELS,
  COUNTER_MAX,
  counterValue,
  setCounter,
  stepCounter,
} from '../../../core/game/table-edit/edit-counters';
import { EditOp } from '../../../core/game/table-edit/edit-ops';
import { Box, placeBeside, Size } from '../../../core/game/table-edit/popup-place';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { CounterPopups } from './counter-popups';
import { PopupPlaceDirective } from './popup-place.directive';

/**
 * The box of a counter opened from its number (gd-edit-counter): − and + change it by one at once, and the number to
 * set it to is set by Set or Enter. The number typed is the person's until it is set or the box closes: the counter
 * changing meanwhile does not overwrite it. Escape (wherever the focus is), or a press outside, closes it. Drawn over
 * the page by gd-table-edit.
 *
 * DOM contract: `gd-edit-counter-box` holding, while a counter's box is open, `.gd-edit-backdrop` and
 * `.gd-edit-counter__box` (`role=dialog`, `data-counter`; placed by gdPopupPlace) with `.gd-edit-counter__label`,
 * `button[data-step=down|up]`, `input[data-field=set]` and `button[data-action=set]`.
 */
@Component({
  selector: 'gd-edit-counter-box',
  styleUrl: './counter-box.component.css',
  imports: [PopupPlaceDirective],
  // (Escape closes the box wherever the focus is: a step's button is disabled, and loses it, while its change goes)
  host: { '(document:keydown.escape)': 'escape()' },
  template: `
    @if (popups.open(); as open) {
      <div class="gd-edit-backdrop" (pointerdown)="close()" (contextmenu)="$event.preventDefault(); close()"></div>
      <div
        class="gd-edit-counter__box"
        role="dialog"
        [attr.aria-label]="label()"
        [attr.data-counter]="open.counter"
        [gdPopupPlace]="placement()"
      >
        <span class="gd-edit-counter__label">{{ label() }}</span>
        <button
          type="button"
          data-step="down"
          aria-label="One fewer"
          [disabled]="edit.busy() || value() <= 0"
          (click)="send(stepOp(-1), open.anchor)"
        >
          −
        </button>
        <input
          #setBox
          type="number"
          min="0"
          [max]="max"
          data-field="set"
          aria-label="Set to"
          [value]="text()"
          (input)="draft.set(setBox.value)"
          (keydown.enter)="send(setOp(), open.anchor)"
        />
        <button
          type="button"
          data-step="up"
          aria-label="One more"
          [disabled]="edit.busy()"
          (click)="send(stepOp(1), open.anchor)"
        >
          +
        </button>
        <button
          type="button"
          data-action="set"
          title="Set it to this number"
          [disabled]="edit.busy() || setOp() === null"
          (click)="send(setOp(), open.anchor)"
        >
          Set
        </button>
      </div>
    }
  `,
})
export class CounterBoxComponent {
  protected readonly popups = inject(CounterPopups);
  protected readonly edit = inject(TableEdit);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly injector = inject(Injector);

  protected readonly max = COUNTER_MAX;
  private readonly counter = computed(() => this.popups.open()?.counter ?? 'twilight');
  protected readonly label = computed(() => COUNTER_LABELS[this.counter()]);
  /** The counter as the table has it. */
  protected readonly value = computed(() => counterValue(this.edit.board(), this.counter()));
  /** The number being typed, until it is set (null: none; the counter's own is shown); none again at each opening. */
  protected readonly draft = linkedSignal<string | null>(() => {
    this.popups.open();
    return null;
  });
  protected readonly text = computed(() => this.draft() ?? String(this.value()));
  protected readonly setOp = computed(() => setCounter(this.counter(), this.text()));
  protected readonly placement = computed(() => {
    const anchor = this.popups.open()?.anchor;
    return (size: Size, room: Size) => (anchor ? placeBeside(anchor, size, room) : { left: 0, top: 0 });
  });

  constructor() {
    // Opened: the number to set it to takes the focus, all of it chosen to be typed over
    effect(() => {
      if (!this.popups.open()) return;
      untracked(() =>
        afterNextRender(
          () => {
            const field = this.host.querySelector<HTMLInputElement>('input[data-field="set"]');
            field?.focus();
            field?.select();
          },
          { injector: this.injector },
        ),
      );
    });
  }

  protected stepOp(by: 1 | -1): EditOp | null {
    return stepCounter(this.counter(), by, this.value());
  }

  protected escape(): void {
    if (this.popups.open()) this.close(true);
  }

  protected close(focusBack = false): void {
    this.popups.close(focusBack);
  }

  /** Makes the change; the box stays open, showing the counter as it now is. A refusal is said under the box. */
  protected async send(op: EditOp | null, at: Box): Promise<void> {
    if (!op) return;
    if (await this.edit.apply([op], { x: at.left, y: at.bottom + 40 })) this.draft.set(null);
  }
}
