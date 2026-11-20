import {
  afterNextRender,
  afterRenderEffect,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { UnderOffer } from '../../../core/game/phone/hand-place';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { PhoneCard, PhoneFold, phoneFolding } from '../../../core/game/phone/phone-table';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { GameSettings } from '../../../core/game/settings/game-settings';
import { GameCard } from '../../../core/game/state/game-model';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { CardComponent } from '../../shared/card/card.component';
import { GameCardLayersComponent } from '../table/game-card-layers.component';
import { GameCardDirective } from '../table/game-card.directive';
import { ScrollSettledDirective } from './scroll-settled.directive';

/**
 * A row of cards in the phone layout: one size (the table's, or what the place gives with `--gd-phone-card`), scrolled
 * sideways; the cards lying under one drawn as small copies under it. A tap does what the card does in the waiting
 * decision; a long press shows its details (CardComponent). A badge at the row's end counts the cards to choose that
 * are scrolled out of view, and scrolls to the first. With a `key`, where the row is scrolled is kept with the game
 * (D10) and taken up again after a reload. With a `fold`, its cards not active this turn fold away (phoneFolding):
 * shrunk or hidden. Shrunk ones have a button where they start that enlarges them and shrinks them again
 * (`foldToggled`); hidden ones are opened from the row's title line (PhoneTableComponent).
 *
 * DOM contract: `gd-phone-cards` (`data-row` = key) holding `.gd-phone-cards__scroll` with a `.gd-phone-slot` per card
 * holding a `button.gd-card` per card under it (`data-under`: attached or stacked; peeking out on the card's left), then
 * its own `button.gd-card` (see GameCardDirective), then `button.gd-phone-slot__toggle` (▾/▴, `aria-expanded`,
 * `data-mode` the player's setting: button or space) and, opened (`data-under-open` on the slot), the cards under it
 * in `.gd-phone-slot__opened` (`button.gd-card[data-opened]`, as big as the card) instead of peeking;
 * `.gd-phone-cards__split` where the Shadow cards start; `.gd-phone-cards__empty` for
 * none; `button.gd-phone-cards__more` the badge; `button.gd-phone-cards__fold` (`data-fold` shrink or hide,
 * `aria-expanded`) the fold's button, `data-shrunk` on a shrunk card's slot; for `offers`, a
 * `.gd-phone-cards__host` (`data-host` = the card's id) with a small copy of the card they lie under, then a
 * `.gd-phone-slot[data-from-under]` per offered card, all before the row's own cards, and a split
 * (`[data-offers-end]`) after them.
 */
@Component({
  selector: 'gd-phone-cards',
  styleUrl: './phone-cards.component.css',
  imports: [CardComponent, GameCardDirective, GameCardLayersComponent, ScrollSettledDirective, NgTemplateOutlet],
  host: { '[attr.data-row]': 'key()' },
  template: `
    <div #scroller class="gd-phone-cards__scroll" (scroll)="measure()" (gdScrollSettled)="keepScroll()">
      <!-- First, before the row's own cards (the owner, 2026-11-17: an action such as putting on the Ring is seen first) -->
      @for (offer of offers(); track offer.host.id + offer.how; let last = $last) {
        <span class="gd-phone-cards__host" [attr.data-host]="offer.host.id">
          <span class="gd-phone-cards__host-label">{{ offer.how === 'stacked' ? 'Stacked on' : 'Attached to' }}</span>
          <button [gdCard]="views.of(offer.host)" [gdGameCard]="offer.host" data-host-copy=""></button>
        </span>
        @for (slot of offer.cards; track slot.card.id) {
          <div class="gd-phone-slot" data-from-under="">
            <button [gdCard]="views.of(slot.card)" [gdGameCard]="slot.card">
              <gd-game-card-layers [card]="slot.card" />
            </button>
          </div>
        }
        @if (last) {
          <span class="gd-phone-cards__split" data-offers-end="" aria-hidden="true"></span>
        }
      }
      <!--
        A fight's minions (layered): the first on top, the others under it peeking out on its left, as attached cards do;
        its ▾ opens them in a row under it (the owner, 2026-11-18)
      -->
      @if (stack(); as fight) {
        @let open = isOpen(fight.top, 'layered');
        <div
          class="gd-phone-layered"
          [attr.data-layered-open]="open ? '' : null"
          [style.--gd-slot-columns]="layeredColumns(fight.others.length + 1, open)"
        >
          @if (!open) {
            @for (other of fight.others; track other.card.id) {
              <button [gdCard]="views.of(other.card)" [gdGameCard]="other.card" data-layered=""></button>
            }
          }
          <ng-container *ngTemplateOutlet="slotView; context: { $implicit: fight.top }" />
          @if (underButton() !== 'none') {
            <button
              type="button"
              class="gd-phone-layered__toggle"
              [attr.data-mode]="underButton()"
              [attr.aria-expanded]="open"
              [attr.aria-label]="
                (open ? 'Hide the ' : 'Show the ') + fight.others.length + ' other minions in this fight'
              "
              (click)="toggleUnder(fight.top.card.id, 'layered')"
            >
              {{ open ? '▴' : '▾ ' + fight.others.length }}
            </button>
          }
          @if (open) {
            <div class="gd-phone-layered__rest">
              @for (other of fight.others; track other.card.id) {
                <ng-container *ngTemplateOutlet="slotView; context: { $implicit: other }" />
              }
            </div>
          }
        </div>
      } @else {
        @for (slot of cards(); track slot.card.id; let i = $index) {
          @if (i === shadowFrom()) {
            <span class="gd-phone-cards__split" aria-hidden="true"></span>
          }
          @let fold = folding();
          <!-- The player's own shrink here; the opponent's hidden cards have their button on the row's title line -->
          @if (fold && fold.how === 'shrink' && !fold.offered && slot.card.id === firstFolding()) {
            <button
              type="button"
              class="gd-phone-cards__fold"
              [attr.data-fold]="fold.how"
              [attr.aria-expanded]="!fold.folded"
              [attr.aria-label]="foldLabel()"
              (click)="foldToggled.emit()"
            >
              {{ foldText() }}
            </button>
          }
          @if (!(fold?.folded && fold?.how === 'hide' && fold?.ids.has(slot.card.id))) {
            <ng-container *ngTemplateOutlet="slotView; context: { $implicit: slot }" />
          }
        } @empty {
          <span class="gd-phone-cards__empty">{{ empty() }}</span>
        }
      }
    </div>
    <!-- A card's place: the cards under it peeking out on its left, its ▾, and the cards under it once opened -->
    <ng-template #slotView let-slot>
      @let fold = folding();
      @let open = isOpen(slot);
      <div
        class="gd-phone-slot"
        [attr.data-shrunk]="fold?.folded && fold?.ids.has(slot.card.id) ? '' : null"
        [attr.data-under-open]="open ? '' : null"
        [style.--gd-slot-columns]="columns(slot, open)"
      >
        <!-- The cards under it peek out on its left, as on the table (the owner, 2026-11-18); opened, they move below -->
        @if (!open) {
          @for (child of slot.under; track child.id) {
            <button [gdCard]="views.of(child)" [gdGameCard]="child" [under]="kindOf(child)"></button>
          }
        }
        <button [gdCard]="views.of(slot.card)" [gdGameCard]="slot.card">
          <gd-game-card-layers [card]="slot.card" />
        </button>
        @if (slot.under.length > 0 && underButton() !== 'none') {
          <button
            type="button"
            class="gd-phone-slot__toggle"
            [attr.data-mode]="underButton()"
            [attr.aria-expanded]="open"
            [attr.aria-label]="(open ? 'Hide the ' : 'Show the ') + underLabel(slot)"
            (click)="toggleUnder(slot.card.id)"
          >
            {{ open ? '▴' : '▾' }}
          </button>
        }
        @if (open) {
          <div class="gd-phone-slot__opened">
            @for (child of slot.under; track child.id) {
              <button [gdCard]="views.of(child)" [gdGameCard]="child" data-opened="">
                <gd-game-card-layers [card]="child" />
              </button>
            }
          </div>
        }
      </div>
    </ng-template>
    @if (hiddenMarks() > 0) {
      <button type="button" class="gd-phone-cards__more" (click)="showMarked(scroller)">
        {{ hiddenMarks() }} more ›
      </button>
    }
  `,
})
export class PhoneCardsComponent {
  readonly cards = input.required<readonly PhoneCard[]>();
  /** The row's name, to keep where it is scrolled (D10); none for a row that isn't kept. */
  readonly key = input<string | null>(null);
  readonly shadowFrom = input<number | null>(null);
  readonly empty = input('None');
  /** Cards under a card on the table that the decision offers, shown before the row's own, smaller (offeredUnder). */
  readonly offers = input<readonly UnderOffer[]>([]);
  /** How the row's cards not active this turn fold away, if they do; `opened` when the player opened them. */
  readonly fold = input<PhoneFold | null>(null);
  readonly opened = input(false);
  /** A fight's side: several cards layered, the first on top (only the minions' side, the owner 2026-11-18). */
  readonly layered = input(false);
  /** The fold's button pressed: open the folded cards, or fold them again. */
  readonly foldToggled = output();

  protected readonly views = inject(GameCardViews);
  private readonly decisions = inject(GameDecisions);
  private readonly viewState = inject(GameViewState);
  /** The player's choice of the button under a card others lie under (GameSettings.underButton). */
  protected readonly underButton = inject(GameSettings).underButton;
  /** The cards whose cards under them are opened below them (kept with the game, D10). */
  private readonly openUnder = computed(() => new Set(this.viewState.phone().openUnder));
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly scrollLeft = signal(0);
  private readonly width = signal(0);
  /** Where each of the row's cards starts in it (px), as last measured: the badge counts from these. */
  private readonly lefts = signal<readonly { id: string; left: number }[]>([]);

  /** The cards to choose that lie right of what the row shows. */
  protected readonly hiddenMarks = computed(() => {
    if (this.width() === 0) return 0;
    const marks = this.decisions.view().marks;
    const right = this.scrollLeft() + this.width();
    return this.lefts().filter(({ id, left }) => marks[id] === 'selectable' && left + 12 > right).length;
  });

  protected readonly folding = computed(() =>
    phoneFolding(this.cards(), this.fold(), this.opened(), this.decisions.view().marks),
  );
  /** The first card that folds: the button stands before it. */
  protected readonly firstFolding = computed(() => {
    const ids = this.folding()?.ids;
    return ids ? (this.cards().find(({ card }) => ids.has(card.id))?.card.id ?? null) : null;
  });
  /** The shrink button's text and label (the only fold with its button in the row). */
  protected readonly foldText = computed(() => {
    const fold = this.folding();
    if (!fold) return '';
    return fold.folded ? `⤢ ${fold.ids.size}` : '⤡';
  });
  protected readonly foldLabel = computed(() => {
    const fold = this.folding();
    if (!fold) return '';
    const what = `${fold.ids.size} ${fold.ids.size === 1 ? 'card' : 'cards'} not active this turn`;
    return fold.folded ? `Enlarge ${what}` : `Shrink ${what}`;
  });

  private readonly scroller = viewChild.required<ElementRef<HTMLElement>>('scroller');

  constructor() {
    const destroy = inject(DestroyRef);
    afterNextRender({
      write: () => {
        const scroller = this.scroller().nativeElement;
        const key = this.key();
        const kept = key === null ? undefined : this.viewState.phone().rows[key];
        if (kept) scroller.scrollLeft = kept;
        if (typeof ResizeObserver === 'function') {
          // The row's own size, and the host's (its height follows the hand's, which sizes the cards)
          const observer = new ResizeObserver(() => this.measure());
          observer.observe(scroller);
          observer.observe(this.host);
          destroy.onDestroy(() => observer.disconnect());
        }
      },
    });
    // Measured again whenever the row's cards change, once they are drawn
    afterRenderEffect({
      read: () => {
        this.cards();
        this.measure();
      },
    });
  }

  protected kindOf(card: GameCard): 'stacked' | 'attached' {
    return card.stackedOn !== null ? 'stacked' : 'attached';
  }

  /** Layered, several cards: the first on top, the others under it. */
  protected readonly stack = computed(() => {
    const [top, ...others] = this.cards();
    return this.layered() && top && others.length > 0 ? { top, others } : null;
  });

  /**
   * The cards under this one are shown below it (its own ▾ pressed; each card on its own); `layered`: a fight's other
   * minions, under its first.
   */
  protected isOpen(slot: PhoneCard, what: 'under' | 'layered' = 'under'): boolean {
    if (this.underButton() === 'none') return false;
    if (what === 'layered') return this.openUnder().has(`layered:${slot.card.id}`);
    return slot.under.length > 0 && this.openUnder().has(slot.card.id);
  }

  /** A fight's layered minions: a share of each of the others peeking out, then the top one's place. */
  protected layeredColumns(count: number, open: boolean): string {
    return open ? 'auto' : `${Array.from({ length: count - 1 }, () => 'var(--gd-phone-peek)').join(' ')} auto`;
  }

  /** The slot's columns: a share of each card under it peeking out (as on the table), then the card. */
  protected columns(slot: PhoneCard, open: boolean): string {
    if (open || slot.under.length === 0) return 'auto';
    const peeks = slot.under.map((child) =>
      this.kindOf(child) === 'stacked' ? 'var(--gd-phone-peek-stacked)' : 'var(--gd-phone-peek)',
    );
    return `${peeks.join(' ')} auto`;
  }

  protected underLabel(slot: PhoneCard): string {
    const count = slot.under.length;
    return `${count} ${count === 1 ? 'card' : 'cards'} under this card`;
  }

  /** A card's ▾ or ▴ pressed: the cards under it opened below it, or back under it. */
  protected toggleUnder(card: string, what: 'under' | 'layered' = 'under'): void {
    const id = what === 'layered' ? `layered:${card}` : card;
    this.viewState.phone.update((phone) => ({
      ...phone,
      openUnder: phone.openUnder.includes(id)
        ? phone.openUnder.filter((open) => open !== id)
        : [...phone.openUnder, id],
    }));
  }

  /** Where the row is scrolled, kept with the game once it stops (D10). */
  protected keepScroll(): void {
    const key = this.key();
    if (key === null) return;
    const left = Math.round(this.scroller().nativeElement.scrollLeft);
    this.viewState.phone.update((phone) =>
      phone.rows[key] === left ? phone : { ...phone, rows: { ...phone.rows, [key]: left } },
    );
  }

  /** The badge pressed: the first card to choose out of view comes into it. */
  protected showMarked(scroller: HTMLElement): void {
    const marks = this.decisions.view().marks;
    const right = scroller.scrollLeft + scroller.clientWidth;
    const first = [...scroller.querySelectorAll<HTMLElement>('.gd-phone-slot > .gd-card')].find(
      (card) => marks[card.dataset['cardId'] ?? ''] === 'selectable' && card.offsetLeft + 12 > right,
    );
    if (first) scroller.scrollTo({ left: Math.max(0, first.offsetLeft - 12), behavior: 'smooth' });
  }

  /** Reads the row from the page: where it is scrolled, how wide it shows, where each card starts. */
  protected measure(): void {
    const scroller = this.scroller().nativeElement;
    this.scrollLeft.set(scroller.scrollLeft);
    this.width.set(scroller.clientWidth);
    const lefts = [...scroller.querySelectorAll<HTMLElement>('.gd-phone-slot > .gd-card')].map((card) => ({
      id: card.dataset['cardId'] ?? '',
      left: card.offsetLeft,
    }));
    const key = (list: readonly { id: string; left: number }[]) => list.map((c) => `${c.id}@${c.left}`).join(',');
    if (key(this.lefts()) !== key(lefts)) this.lefts.set(lefts);
  }
}
