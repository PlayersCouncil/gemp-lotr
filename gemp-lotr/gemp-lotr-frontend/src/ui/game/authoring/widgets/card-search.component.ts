import {
  afterNextRender,
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
import { AuthoringApi } from '../../../../core/game/authoring/authoring-api.service';
import { FoundCard } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { AuthorButtonDirective } from './author-button.directive';
import { CardHoverDirective } from './card-hover.directive';

/** How long after typing stops the search is made (ms). */
export const SEARCH_MS = 300;

/** How a card found is listed: "name · id · culture type (cost)", or for a site "name · id · site N" (or "no number"). */
export function foundText(card: FoundCard, sites: boolean, current: string | null): string {
  const parts = sites
    ? [card.name, card.id, card.site === 0 ? 'no number' : `site ${card.site ?? '?'}`]
    : [card.name, card.id, `${card.culture ?? ''} ${card.type}${card.cost !== null ? ` (${card.cost})` : ''}`.trim()];
  if (card.id === current) parts.push('this one');
  return parts.join('  ·  ');
}

/**
 * A search for a card by its name (`GET cards`, 300 ms after typing stops), shared by the Board tab's three finders
 * (the legacy siteFinder, cardFinder and add-card search): every card but the sites, or (`sites`) only the sites,
 * this site number's (`site`) first. A card found is shown large while the mouse is on it; a click picks it. `query`
 * is searched at once (`immediate`); `current` (a blueprint) is marked "this one"; Cancel (or Escape) says so.
 *
 * DOM contract: `gd-author-card-search` holding `.gd-author-search__bar` (`input.gd-author-input` with its
 * `placeholder`, `button[data-action=cancel]` when it can be cancelled), then once searched `.gd-author-search__results`
 * with a `button.gd-author-search__result` per card (`data-id`, `data-current`) or `.gd-author-note[data-none]`.
 */
@Component({
  selector: 'gd-author-card-search',
  styleUrl: './card-search.component.css',
  imports: [AuthorButtonDirective, CardHoverDirective],
  template: `
    <span class="gd-author-search__bar">
      <input
        #field
        class="gd-author-input"
        type="text"
        [attr.aria-label]="placeholder()"
        [placeholder]="placeholder()"
        [value]="query()"
        (input)="typed(field.value)"
        (keydown.escape)="cancel()"
      />
      @if (cancellable()) {
        <button gdAuthorButton data-action="cancel" (click)="cancel()">Cancel</button>
      }
    </span>
    @if (found(); as cards) {
      <div class="gd-author-search__results">
        @for (card of cards; track card.id) {
          <button
            type="button"
            class="gd-author-search__result"
            [attr.data-id]="card.id"
            [attr.data-current]="card.id === current() ? '' : null"
            [gdCardHover]="card.id"
            (click)="pick(card)"
          >
            {{ textOf(card) }}
          </button>
        } @empty {
          <p class="gd-author-note" data-none>No {{ sites() ? 'site' : 'card' }} by that name.</p>
        }
      </div>
    }
  `,
})
export class CardSearchComponent {
  /** Only sites (else everything but sites). */
  readonly sites = input(false);
  /** For sites: the place on the path whose sites come first. */
  readonly site = input<number | null>(null);
  /** The card that is there now: marked "this one". */
  readonly current = input<string | null>(null);
  /** What the field starts with. */
  readonly query = input('');
  /** It is searched for at once (else once something is typed). */
  readonly immediate = input(false);
  readonly placeholder = input('name of a card');
  readonly cancellable = input(true);
  /** It takes the focus when it comes (and selects what it starts with). */
  readonly focus = input(true);
  readonly picked = output<FoundCard>();
  readonly cancelled = output();

  private readonly authoring = inject(Authoring);
  private readonly api = inject(AuthoringApi);
  private readonly field = viewChild.required<ElementRef<HTMLInputElement>>('field');
  private readonly results = signal<readonly FoundCard[] | null>(null);
  /** The cards found, this site number's first. */
  protected readonly found = computed(() => {
    const results = this.results();
    const number = this.site();
    if (results === null || number === null) return results;
    return [...results].sort((a, b) => (a.site === number ? 0 : 1) - (b.site === number ? 0 : 1));
  });
  private timer: ReturnType<typeof setTimeout> | null = null;
  /** Searches made: only the last one's answer is shown. */
  private serial = 0;

  constructor() {
    inject(DestroyRef).onDestroy(() => {
      this.stop();
      this.serial++;
    });
    afterNextRender(() => {
      if (this.immediate()) void this.search(this.query());
      if (!this.focus()) return;
      const field = this.field().nativeElement;
      field.focus();
      field.select();
    });
  }

  protected textOf(card: FoundCard): string {
    return foundText(card, this.sites(), this.current());
  }

  protected typed(text: string): void {
    this.stop();
    this.timer = setTimeout(() => void this.search(text), SEARCH_MS);
  }

  private async search(text: string): Promise<void> {
    const serial = ++this.serial;
    const found = await this.authoring.send(this.api.cards(text.trim(), this.sites()), false);
    if (serial !== this.serial || found === null) return;
    this.results.set(found.cards);
  }

  protected pick(card: FoundCard): void {
    this.results.set(null);
    this.picked.emit(card);
  }

  /** Empty again: no text, nothing found. */
  clear(): void {
    this.stop();
    this.serial++;
    this.field().nativeElement.value = '';
    this.results.set(null);
  }

  protected cancel(): void {
    if (this.cancellable()) this.cancelled.emit();
  }

  private stop(): void {
    if (this.timer !== null) clearTimeout(this.timer);
    this.timer = null;
  }
}
