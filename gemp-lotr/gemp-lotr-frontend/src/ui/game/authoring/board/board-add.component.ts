import { Component, computed, inject, signal, viewChild } from '@angular/core';
import { DraftSeat, FoundCard } from '../../../../core/game/authoring/authoring-state';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { addPlacesFor, suggestAlias } from '../../../../core/game/authoring/board-queue';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { CardSearchComponent } from '../widgets/card-search.component';

/** The card picked to be added, and how: the name suggested, the seat and the place chosen. */
interface Offer {
  card: FoundCard;
  alias: string;
  seat: DraftSeat;
  zone: string;
  host: string;
}

/**
 * The Board tab's "Add a card": a card found by name is offered under a name of its own (its name's first word that
 * says something, numbered until free), in the seat its side says, in a place (the hand for the Free Peoples, the
 * bottom of the deck for the Shadow; attached to or stacked on a card on the table when there is one); Add gathers it.
 * The name suggested is set once, when the card is picked: it stays as the author types it.
 *
 * DOM contract: `gd-author-board-add` holding `.gd-author-board__heading`, `gd-author-card-search`, then once a card is
 * picked `.gd-author-board-add__chosen` (`data-id`) with `.gd-author-board-add__card`, `input.gd-author-input[data-field=alias]`,
 * `gd-author-select`s (`data-field`: seat, zone, host; host only for a card put on another) and `button[data-action=add]`.
 */
@Component({
  selector: 'gd-author-board-add',
  styleUrl: './board-add.component.css',
  imports: [AuthorButtonDirective, AuthorSelectComponent, CardSearchComponent],
  template: `
    <div class="gd-author-board__heading">Add a card</div>
    <gd-author-card-search [cancellable]="false" [focus]="false" (picked)="offer($event)" />
    @if (offered(); as offer) {
      <div class="gd-author-board-add__chosen" [attr.data-id]="offer.card.id">
        <div class="gd-author-board-add__card">{{ offer.card.name }} ({{ offer.card.id }})</div>
        <div class="gd-author-board-add__row">
          <span class="gd-author-label">called</span>
          <input
            #alias
            class="gd-author-input"
            type="text"
            data-field="alias"
            aria-label="Its name in the scenario"
            [value]="offer.alias"
            (input)="choose({ alias: $any($event.target).value })"
          />
          <gd-author-select
            data-field="seat"
            label="Seat"
            [options]="seatOptions"
            [value]="offer.seat"
            (changed)="choose({ seat: $any($event) })"
          />
          <gd-author-select
            data-field="zone"
            label="Place"
            [options]="places()"
            [value]="offer.zone"
            (changed)="choose({ zone: $event })"
          />
          @if (offer.zone === 'attach' || offer.zone === 'stack') {
            <gd-author-select
              data-field="host"
              label="On"
              hint="The card it is put on"
              [options]="hosts()"
              [value]="offer.host"
              (changed)="choose({ host: $event })"
            />
          }
          <button
            gdAuthorButton="primary"
            data-action="add"
            title="Gathered with the other changes, and made when they are submitted"
            (click)="add(offer)"
          >
            Add
          </button>
        </div>
      </div>
    }
  `,
})
export class BoardAddComponent {
  protected readonly changes = inject(BoardChanges);
  private readonly search = viewChild.required(CardSearchComponent);
  protected readonly offered = signal<Offer | null>(null);
  protected readonly seatOptions: readonly AuthorOption[] = ['freeps', 'shadow'];
  protected readonly hosts = computed<AuthorOption[]>(() => this.changes.bearers().map((bearer) => bearer.alias));
  /** Where it can go in the setup: no minions on the table for the Free Peoples seat; with the game's own opening, a deck. */
  protected readonly places = computed<AuthorOption[]>(() => {
    const seat = this.offered()?.seat ?? 'freeps';
    return addPlacesFor(seat, this.changes.opening() === 'game', this.changes.bearers().length > 0);
  });

  protected offer(card: FoundCard): void {
    const shadow = card.side === 'shadow';
    this.offered.set({
      card,
      alias: suggestAlias(card.name, this.changes.aliases()),
      seat: shadow ? 'shadow' : 'freeps',
      zone: shadow || this.changes.opening() === 'game' ? '' : 'hand',
      host: this.changes.bearers()[0]?.alias ?? '',
    });
  }

  protected choose(patch: Partial<Offer>): void {
    this.offered.update((offer) => {
      if (!offer) return offer;
      const next = { ...offer, ...patch };
      // (a place the seat chosen cannot have is its deck)
      const open = addPlacesFor(next.seat, this.changes.opening() === 'game', this.changes.bearers().length > 0);
      return open.some((place) => (typeof place === 'string' ? place : place[0]) === next.zone)
        ? next
        : { ...next, zone: '' };
    });
  }

  protected async add(offer: Offer): Promise<void> {
    const host = offer.zone === 'attach' || offer.zone === 'stack' ? offer.host : null;
    if (!(await this.changes.add(offer.card, offer.alias.trim(), offer.seat, offer.zone, host))) return;
    // (as the legacy tab, drawn afresh once a card was gathered)
    this.offered.set(null);
    this.search().clear();
  }
}
