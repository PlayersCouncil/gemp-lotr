import { Component, computed, ElementRef, inject, Injectable, input } from '@angular/core';
import { CardActions } from '../../../core/cards/card-actions';
import { CardView } from '../../../core/cards/card-view';
import { CardViews } from '../../../core/cards/card-views.service';
import { AuthorBackend } from '../../../core/game/table-edit/author-backend';
import { FilledSlot } from '../../../core/game/table-edit/puzzle-backend';
import { comesText, slotLabel, slotPlace } from '../../../core/game/table-edit/puzzle-slots';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { CardComponent } from '../../shared/card/card.component';
import { PuzzlePopups } from './puzzle-popups';

/** What the card in a slot stands for: the slot. */
interface SlotRef {
  slot: string;
}

/** Using the card in a slot (click, Enter) opens the slot's chooser, to change or empty it. */
@Injectable()
export class PressSlot extends CardActions {
  pressed: (() => void) | null = null;

  activate(): void {
    this.pressed?.();
  }
}

/**
 * A puzzle's slot on the table: an open spot (the glowing card-shaped +) while it is empty, the card in it once one is
 * chosen (with the page's hover zoom and card information); either way its label on it, and what its card comes with
 * (tokens, wounds). Pressing it opens its chooser by it (PuzzlePopups), to fill, change or empty it. A host (a row of
 * the table, the slots strip) sizes it as its cards (`--gd-card-height`); `place` adds where its card goes under it,
 * for a slot shown away from its place.
 *
 * DOM contract: `gd-puzzle-slot` holding `.gd-puzzle-slot` (`data-slot`, `data-zone`, `data-seat`, `data-filled` when
 * it has a card) with `.gd-puzzle-slot__face` holding either `button.gd-open-spot.gd-puzzle-slot__open` (`aria-label` "Fill …", `aria-expanded`) or
 * `button.gd-card.gd-puzzle-slot__card[data-blueprint]` (`aria-expanded`), then `.gd-puzzle-slot__label` and
 * `.gd-puzzle-slot__comes` when it comes with something; then `.gd-puzzle-slot__place` with `place`.
 *
 * On the author's table (level `author`) a slot the game in hand has no card in is drawn the same way, and pressing it
 * opens the slot's form in the Board tab (AuthorBackend.showSlot).
 */
@Component({
  selector: 'gd-puzzle-slot',
  styleUrl: './puzzle-slot.component.css',
  imports: [CardComponent],
  providers: [PressSlot, { provide: CardActions, useExisting: PressSlot }],
  template: `
    @let filled = slot();
    <div
      class="gd-puzzle-slot"
      [attr.data-slot]="filled.slot.id"
      [attr.data-zone]="filled.slot.zone"
      [attr.data-seat]="filled.slot.seat"
      [attr.data-filled]="filled.card ? '' : null"
    >
      <div class="gd-puzzle-slot__face">
        @if (view(); as card) {
          <button
            class="gd-puzzle-slot__card"
            [gdCard]="card"
            [attr.data-blueprint]="card.blueprintId"
            [attr.aria-expanded]="open()"
            [title]="label() + ': ' + filled.card?.name"
          ></button>
        } @else {
          <button
            type="button"
            class="gd-open-spot gd-puzzle-slot__open"
            [attr.aria-label]="'Fill ' + label()"
            [attr.aria-expanded]="open()"
            [title]="'Choose a card: ' + label()"
            [disabled]="!edit.editing()"
            (click)="press()"
          >
            <span class="gd-open-spot__plus" aria-hidden="true">+</span>
          </button>
        }
        <span class="gd-puzzle-slot__label" [title]="filled.slot.bound">{{ label() }}</span>
        @if (comes(); as comes) {
          <span class="gd-puzzle-slot__comes" [title]="'Comes with ' + comes">{{ comes }}</span>
        }
      </div>
      @if (place()) {
        <span class="gd-puzzle-slot__place">{{ where() }}</span>
      }
    </div>
  `,
})
export class PuzzleSlotComponent {
  /** The slot, and the card in it now. */
  readonly slot = input.required<FilledSlot>();
  /** Say where its card goes, under it (a slot shown away from its place). */
  readonly place = input(false);

  protected readonly edit = inject(TableEdit);
  private readonly popups = inject(PuzzlePopups);
  private readonly cardViews = inject(CardViews);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  protected readonly label = computed(() => slotLabel(this.slot().slot));
  protected readonly where = computed(() => slotPlace(this.slot().slot));
  protected readonly comes = computed(() => comesText(this.slot().slot.comes));
  protected readonly view = computed<CardView<SlotRef> | null>(() => {
    const { slot, card } = this.slot();
    return card
      ? this.cardViews.create<SlotRef>({ ref: { slot: slot.id }, blueprintId: card.blueprintId, zone: null })
      : null;
  });
  protected readonly open = computed(() => this.popups.chooser()?.slot === this.slot().slot.id);

  private readonly author = inject(AuthorBackend, { optional: true });

  constructor() {
    inject(PressSlot).pressed = () => this.press();
  }

  protected press(): void {
    // (the author's table: an open slot is the puzzle's own, and pressing it opens its form in the Board tab)
    if (this.author && this.edit.level() === 'author') {
      if (this.edit.editing()) this.author.showSlot(this.slot().slot.id);
      return;
    }
    if (this.open()) {
      this.popups.closeChooser();
    } else if (this.edit.editing()) {
      this.popups.openChooser(this.slot().slot.id, this.host.querySelector('button') ?? this.host);
    }
  }
}
