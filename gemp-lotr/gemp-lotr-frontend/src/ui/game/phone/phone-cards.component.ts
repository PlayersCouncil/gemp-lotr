import { afterNextRender, Component, computed, DestroyRef, ElementRef, inject, input, signal } from '@angular/core';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { PhoneCard } from '../../../core/game/phone/phone-table';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { CardComponent } from '../../shared/card/card.component';
import { GameCardLayersComponent } from '../table/game-card-layers.component';
import { GameCardDirective } from '../table/game-card.directive';

/**
 * A row of cards in the phone layout: one size (the table's, or what the place gives with `--gd-phone-card`), scrolled
 * sideways; the cards lying under one drawn as small copies under it. A tap does what the card does in the waiting
 * decision; a long press shows its details (CardComponent). A badge at the row's end counts the cards to choose that
 * are scrolled out of view, and scrolls to the first. With a `key`, where the row is scrolled is kept with the game
 * (D10) and taken up again after a reload.
 *
 * DOM contract: `gd-phone-cards` (`data-row` = key) holding `.gd-phone-cards__scroll` with a `.gd-phone-slot` per card
 * holding its `button.gd-card` (see GameCardDirective) and `.gd-phone-slot__under` with a `button.gd-card` per card
 * under it (`data-under`; small); `.gd-phone-cards__split` where the Shadow cards start; `.gd-phone-cards__empty` for
 * none; `button.gd-phone-cards__more` the badge.
 */
@Component({
  selector: 'gd-phone-cards',
  styleUrl: './phone-cards.component.css',
  imports: [CardComponent, GameCardDirective, GameCardLayersComponent],
  host: { '[attr.data-row]': 'key()' },
  template: `
    <div #scroller class="gd-phone-cards__scroll" (scroll)="scrolled(scroller)">
      @for (slot of cards(); track slot.card.id; let i = $index) {
        @if (i === shadowFrom()) {
          <span class="gd-phone-cards__split" aria-hidden="true"></span>
        }
        <div class="gd-phone-slot">
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
      } @empty {
        <span class="gd-phone-cards__empty">{{ empty() }}</span>
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

  protected readonly views = inject(GameCardViews);
  private readonly decisions = inject(GameDecisions);
  private readonly viewState = inject(GameViewState);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly scrollLeft = signal(0);
  private readonly width = signal(0);
  /** How wide the cards are together: they grow with the hand's height or the card size. */
  private readonly contentWidth = signal(0);
  private saveTimer: ReturnType<typeof setTimeout> | null = null;

  /** The cards to choose that lie right of what the row shows. */
  protected readonly hiddenMarks = computed(() => {
    const marks = this.decisions.view().marks;
    this.cards();
    this.contentWidth();
    const right = this.scrollLeft() + this.width();
    if (this.width() === 0) return 0;
    return [...this.host.querySelectorAll<HTMLElement>('.gd-phone-slot > .gd-card')].filter(
      (card) => marks[card.dataset['cardId'] ?? ''] === 'selectable' && card.offsetLeft + 12 > right,
    ).length;
  });

  constructor() {
    const destroy = inject(DestroyRef);
    afterNextRender({
      write: () => {
        const scroller = this.host.querySelector<HTMLElement>('.gd-phone-cards__scroll');
        if (!scroller) return;
        const key = this.key();
        const kept = key === null ? undefined : this.viewState.phone().rows[key];
        if (kept) scroller.scrollLeft = kept;
        this.measure(scroller);
        if (typeof ResizeObserver === 'function') {
          // The row's own size, and the host's (its height follows the hand's, which sizes the cards)
          const observer = new ResizeObserver(() => this.measure(scroller));
          observer.observe(scroller);
          observer.observe(this.host);
          destroy.onDestroy(() => observer.disconnect());
        }
      },
    });
    destroy.onDestroy(() => {
      if (this.saveTimer !== null) clearTimeout(this.saveTimer);
    });
  }

  protected scrolled(scroller: HTMLElement): void {
    this.measure(scroller);
    const key = this.key();
    if (key === null) return;
    if (this.saveTimer !== null) clearTimeout(this.saveTimer);
    // Kept once the row stops, not on every frame of a swipe
    this.saveTimer = setTimeout(() => {
      this.saveTimer = null;
      const left = Math.round(scroller.scrollLeft);
      this.viewState.phone.update((phone) =>
        phone.rows[key] === left ? phone : { ...phone, rows: { ...phone.rows, [key]: left } },
      );
    }, 150);
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

  private measure(scroller: HTMLElement): void {
    this.scrollLeft.set(scroller.scrollLeft);
    this.width.set(scroller.clientWidth);
    this.contentWidth.set(scroller.scrollWidth);
  }
}
