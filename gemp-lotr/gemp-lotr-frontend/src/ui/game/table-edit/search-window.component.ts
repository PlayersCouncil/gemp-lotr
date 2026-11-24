import { Component, computed, effect, inject, Injectable, input, output, untracked } from '@angular/core';
import { CardActions } from '../../../core/cards/card-actions';
import { CardView } from '../../../core/cards/card-view';
import { CardViews } from '../../../core/cards/card-views.service';
import { CardFilterModel, CardFilterPager } from '../../../core/card-filter/card-filter-model';
import { CardSearch, FoundCard, SearchBound } from '../../../core/game/table-edit/card-search';
import { CardFilterComponent } from '../../shared/card-filter/card-filter.component';
import { CardComponent } from '../../shared/card/card.component';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { FitCard } from '../../shared/fit-cards/fit-cards';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';

/** What a card found stands for in the search window: the card found. */
interface FoundRef {
  found: FoundCard;
}

/** Using a card found (click, Enter) chooses it: the search window's CardActions. */
@Injectable()
export class ChooseFound extends CardActions {
  chosen: ((card: FoundCard) => void) | null = null;

  activate(ref: unknown): void {
    const found = (ref as Partial<FoundRef> | null)?.found;
    if (found) this.chosen?.(found);
  }
}

/**
 * The search window of the edit controls: the deck builder's own filter panel (the shared `gd-collection-filter`,
 * over CardSearch, without products, rarities and its reset buttons), the cards found as cards (the page's hover zoom
 * and card information work on them; paged by the panel's pager), "Clear filters", and the bound the search is held to
 * (`bound`: a place's card types, a puzzle slot's bound), shown by its label and never changed. It remembers the last
 * filters (CardSearch). Using a card found (click, Enter) says `chosen`; what that does is the host's (the chooser's
 * place: `add`; a puzzle slot later). `said` is a refusal to show (in the server's words). Non-modal, so the hover
 * zoom shows over it.
 *
 * DOM contract: `gd-dialog` with `data-dialog=edit-search`, holding `.gd-edit-search` (`data-loading`) with
 * `.gd-edit-search__filters` (`gd-collection-filter`, see its contract) and `.gd-edit-search__results`:
 * `.gd-edit-search__bar` (`.gd-edit-search__bound` with the bound's label and its filter as `title`,
 * `.gd-edit-search__count`, `button[data-action=clear]` "Clear filters"), `.gd-edit-search__problem` (`role=alert`)
 * while a refusal is up, and `.gd-edit-search__cards` (fitCards) with a `button.gd-card[data-blueprint]` per card found.
 * With `use`, the bar ends with `button[data-action=use]` (its label; its title the filter text it hands on).
 */
@Component({
  selector: 'gd-edit-search',
  styleUrl: './search-window.component.css',
  imports: [DialogComponent, CardFilterComponent, CardComponent, FitCardsDirective],
  providers: [
    ChooseFound,
    { provide: CardActions, useExisting: ChooseFound },
    { provide: CardFilterModel, useExisting: CardSearch },
    { provide: CardFilterPager, useExisting: CardSearch },
  ],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog name="edit-search" [heading]="heading()" [modal]="false" [open]="open()" (closed)="closed.emit()">
      @if (open()) {
        <div class="gd-edit-search" [attr.data-loading]="search.loading() ? '' : null">
          <div class="gd-edit-search__filters">
            <gd-collection-filter [omit]="search.omitted()" [resets]="false" />
          </div>
          <div class="gd-edit-search__results">
            <div class="gd-edit-search__bar">
              @if (search.bound(); as bound) {
                <span class="gd-edit-search__bound" [title]="bound.filter">{{ bound.label }}</span>
              }
              <span class="gd-edit-search__count">{{ countText() }}</span>
              <button
                type="button"
                class="gd-edit-search__clear"
                data-action="clear"
                title="Every filter back to Any"
                (click)="search.resetAll()"
              >
                Clear filters
              </button>
              @if (use(); as label) {
                <button
                  type="button"
                  class="gd-edit-search__clear"
                  data-action="use"
                  [title]="usedFilter() || 'Any card'"
                  (click)="used.emit(usedFilter())"
                >
                  {{ label }}
                </button>
              }
            </div>
            @if (said(); as problem) {
              <p class="gd-edit-search__problem" role="alert">{{ problem }}</p>
            }
            <div class="gd-edit-search__cards" [gdFitCards]="fit()">
              @for (view of views(); track view.blueprintId) {
                <button [gdCard]="view" [attr.data-blueprint]="view.blueprintId" [title]="titleOf(view)"></button>
              }
            </div>
          </div>
        </div>
      }
    </gd-dialog>
  `,
})
export class SearchWindowComponent {
  readonly open = input(false);
  /** The bound every search here is held to (shown, never changed); null: none. */
  readonly bound = input<SearchBound | null>(null);
  /** What the window is called ("Search", "Add to Characters"). */
  readonly heading = input('Search');
  /** A refusal to show in the window (the server's words), or null. */
  readonly said = input<string | null>(null);
  readonly chosen = output<FoundCard>();
  readonly closed = output();
  /** A button handing the filters on (its label, "Use as bound"); null: none. */
  readonly use = input<string | null>(null);
  /** The filters as the deck builder's filter text (without the sort), when `use` is pressed. */
  readonly used = output<string>();

  protected readonly search = inject(CardSearch);
  private readonly cardViews = inject(CardViews);

  protected readonly views = computed<CardView<FoundRef>[]>(() =>
    (this.search.page()?.cards ?? []).map((found) =>
      this.cardViews.create<FoundRef>({ ref: { found }, blueprintId: found.blueprintId, zone: null }),
    ),
  );
  protected readonly fit = computed<FitCard[]>(() => this.views().map((view) => ({ horizontal: view.horizontal })));
  protected readonly countText = computed(() => {
    const page = this.search.page();
    if (!page) return '';
    if (page.total === 0) return 'No cards';
    const last = Math.min(page.total, page.start + page.cards.length);
    return `${page.start + 1}–${last} of ${page.total}`;
  });

  constructor() {
    inject(ChooseFound).chosen = (found) => this.chosen.emit(found);
    // Opened (or held to another bound): searched afresh, with the remembered filters
    effect(() => {
      const open = this.open();
      const bound = this.bound();
      untracked(() => {
        if (open) this.search.open(bound);
      });
    });
  }

  /** The filters set, as the deck builder's filter text a bound takes (the sort is no part of a bound). */
  protected readonly usedFilter = computed(() =>
    this.search
      .filter()
      .split(/\s+/)
      .filter((part) => part !== '' && !/^sort:/i.test(part))
      .join(' '),
  );

  protected titleOf(view: CardView<FoundRef>): string {
    return view.ref.found.name;
  }
}
