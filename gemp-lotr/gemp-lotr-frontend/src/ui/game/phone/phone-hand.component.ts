import { Component, computed, inject } from '@angular/core';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { offTableChoices } from '../../../core/game/phone/hand-place';
import { PhoneCard, phoneTable } from '../../../core/game/phone/phone-table';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { GameTableState } from '../../../core/game/table/game-table.service';
import { CardHintsDirective } from '../table/card-hints.directive';
import { PhoneCardsComponent } from './phone-cards.component';

/**
 * The phone layout's hand, under the divider, scrolled sideways (G7 of trymarz/gemp-lotr-private#27). When the waiting
 * decision offers cards from off the table (a deck, a pile, the adventure deck for a site), they take the hand's
 * place under a label saying what they are; ✕ goes back to the hand, and a button brings them back. A spectator sees
 * the player's hand as its count only.
 *
 * DOM contract: `gd-phone-hand` (`data-place`: hand, choices) holding `.gd-phone-hand__head` with the label
 * (`.gd-phone-hand__label`), `button[data-phone=close-choices]` (✕) or `button[data-phone=show-choices]`, then the
 * cards (`gd-phone-cards` with `data-row` hand or choices).
 */
@Component({
  selector: 'gd-phone-hand',
  styleUrl: './phone-hand.component.css',
  imports: [PhoneCardsComponent, CardHintsDirective],
  host: { '[attr.data-place]': "showing() ? 'choices' : 'hand'" },
  template: `
    <div class="gd-phone-hand__head">
      @if (showing(); as offer) {
        <span class="gd-phone-hand__label">
          @if (offer.labelIsHtml) {
            <b gdCardHints [innerHTML]="offer.label"></b>
          } @else {
            <b>{{ offer.label }}</b>
          }
          · {{ offer.cards.length }} {{ offer.cards.length === 1 ? 'card' : 'cards' }}
        </span>
        <button
          type="button"
          class="gd-phone-hand__x"
          data-phone="close-choices"
          aria-label="Close these cards and show your hand"
          (click)="close(offer.key)"
        >
          ✕
        </button>
      } @else {
        <span class="gd-phone-hand__title">
          {{ title() }} <span class="gd-phone-hand__count">{{ hand().length }}</span>
        </span>
        @if (offer(); as closed) {
          <button type="button" class="gd-phone-hand__show" data-phone="show-choices" (click)="reopen()">
            Show the offered cards {{ closed.cards.length }}
          </button>
        }
      }
    </div>
    @if (showing(); as offer) {
      <gd-phone-cards [cards]="choiceCards()" key="choices" />
    } @else {
      <gd-phone-cards [cards]="hand()" key="hand" [empty]="emptyHand()" />
    }
  `,
})
export class PhoneHandComponent {
  private readonly session = inject(GameSession);
  private readonly decisions = inject(GameDecisions);
  private readonly viewState = inject(GameViewState);
  private readonly table = inject(GameTableState).table;

  protected readonly hand = computed<readonly PhoneCard[]>(() => phoneTable(this.table(), this.session.model()).hand);
  /** The cards the decision offers from off the table, or null. */
  protected readonly offer = computed(() => offTableChoices(this.decisions.view(), this.session.model()));
  /** The offer, unless the player closed it to see the hand. */
  readonly showing = computed(() => {
    const offer = this.offer();
    return offer && offer.key !== this.viewState.phone().closedOffer ? offer : null;
  });
  protected readonly choiceCards = computed<readonly PhoneCard[]>(() =>
    (this.showing()?.cards ?? []).map((choice) => ({ card: choice.card, under: [] })),
  );
  protected readonly title = computed(() => {
    const model = this.session.model();
    return model.spectator ? `${model.participantId ?? 'Player'}'s hand` : 'Your hand';
  });
  protected readonly emptyHand = computed(() => (this.session.model().spectator ? 'Not shown' : 'No cards'));

  protected close(key: string): void {
    this.viewState.phone.update((phone) => ({ ...phone, closedOffer: key }));
  }

  protected reopen(): void {
    this.viewState.phone.update((phone) => ({ ...phone, closedOffer: null }));
  }
}
