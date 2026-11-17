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
import { UnderOffer } from '../../../core/game/phone/hand-place';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { PhoneCard, PhoneFold, phoneFolding } from '../../../core/game/phone/phone-table';
import { GameViewState } from '../../../core/game/session/game-view-state';
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
 * shrunk or hidden, with a button where they start that opens them and folds them again (`foldToggled`).
 *
 * DOM contract: `gd-phone-cards` (`data-row` = key) holding `.gd-phone-cards__scroll` with a `.gd-phone-slot` per card
 * holding its `button.gd-card` (see GameCardDirective) and `.gd-phone-slot__under` with a `button.gd-card` per card
 * under it (`data-under`; small); `.gd-phone-cards__split` where the Shadow cards start; `.gd-phone-cards__empty` for
 * none; `button.gd-phone-cards__more` the badge; `button.gd-phone-cards__fold` (`data-fold` shrink or hide,
 * `aria-expanded`) the fold's button, `data-shrunk` on a shrunk card's slot; for `offers`, a
 * `.gd-phone-cards__host` (`data-host` = the card's id) with a small copy of the card they lie under, then a
 * `.gd-phone-slot[data-from-under]` per offered card.
 */
@Component({
  selector: 'gd-phone-cards',
  styleUrl: './phone-cards.component.css',
  imports: [CardComponent, GameCardDirective, GameCardLayersComponent, ScrollSettledDirective],
  host: { '[attr.data-row]': 'key()' },
  template: `
    <div #scroller class="gd-phone-cards__scroll" (scroll)="measure()" (gdScrollSettled)="keepScroll()">
      @for (slot of cards(); track slot.card.id; let i = $index) {
        @if (i === shadowFrom()) {
          <span class="gd-phone-cards__split" aria-hidden="true"></span>
        }
        @let fold = folding();
        @if (fold && !fold.offered && slot.card.id === firstFolding()) {
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
        @if (!(fold?.folded && fold?.how === 'hide' && fold?.ids?.has(slot.card.id))) {
          <div class="gd-phone-slot" [attr.data-shrunk]="fold?.folded && fold?.ids?.has(slot.card.id) ? '' : null">
            <button [gdCard]="views.of(slot.card)" [gdGameCard]="slot.card">
              <gd-game-card-layers [card]="slot.card" />
            </button>
            @if (slot.under.length > 0) {
              <div class="gd-phone-slot__under">
                @for (child of slot.under; track child.id) {
                  <button
                    [gdCard]="views.of(child)"
                    [gdGameCard]="child"
                    [under]="child.stackedOn !== null ? 'stacked' : 'attached'"
                  ></button>
                }
              </div>
            }
          </div>
        }
      } @empty {
        <span class="gd-phone-cards__empty">{{ empty() }}</span>
      }
      @for (offer of offers(); track offer.host.id + offer.how) {
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
      }
    </div>
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
  /** Cards under a card on the table that the decision offers, shown full size after the row's own (offeredUnder). */
  readonly offers = input<readonly UnderOffer[]>([]);
  /** How the row's cards not active this turn fold away, if they do; `opened` when the player opened them. */
  readonly fold = input<PhoneFold | null>(null);
  readonly opened = input(false);
  /** The fold's button pressed: open the folded cards, or fold them again. */
  readonly foldToggled = output();

  protected readonly views = inject(GameCardViews);
  private readonly decisions = inject(GameDecisions);
  private readonly viewState = inject(GameViewState);
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
  protected readonly foldText = computed(() => {
    const fold = this.folding();
    if (!fold) return '';
    if (fold.how === 'shrink') return fold.folded ? `⤢ ${fold.ids.size}` : '⤡';
    return fold.folded ? `Show ${fold.ids.size} ▸` : '◂ Hide';
  });
  protected readonly foldLabel = computed(() => {
    const fold = this.folding();
    if (!fold) return '';
    const what = `${fold.ids.size} ${fold.ids.size === 1 ? 'card' : 'cards'} not active this turn`;
    if (fold.how === 'shrink') return fold.folded ? `Enlarge ${what}` : `Shrink ${what}`;
    return fold.folded ? `Show ${what}` : `Hide ${what}`;
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
