import { Component, computed, inject, input } from '@angular/core';
import { GameClocks } from '../../../../core/game/clock/game-clocks.service';
import { PhoneCard } from '../../../../core/game/phone/phone-table';
import { Sheet } from '../../../../core/game/phone/sheet';
import { GameSession } from '../../../../core/game/session/game-session.service';
import { handRevealed, PileName, pileCards } from '../../../../core/game/table/game-table';
import { clockText } from '../../panel/game-clock';
import { PhoneCardsComponent } from '../phone-cards.component';

interface PileView {
  pile: PileName;
  title: string;
  /** The cards, or null when only their number may be seen. */
  cards: readonly PhoneCard[] | null;
  count: string;
}

const PILE_TITLES: Readonly<Record<PileName, string>> = {
  adventureDeck: 'Adventure deck',
  deadPile: 'Dead pile',
  discard: 'Discard pile',
  removedPile: 'Removed from game',
  deck: 'Draw deck',
  revealedHand: 'Revealed hand',
};

/**
 * A player's piles in the phone layout's sheet: their time bank, site, hand, deck and threats, and each pile's cards
 * where the player may see them (only the number where not).
 *
 * DOM contract: `dl.gd-phone-facts`, and a `section[data-pile]` per pile with its `gd-phone-cards`.
 */
@Component({
  selector: 'gd-phone-piles-sheet',
  styleUrl: './phone-piles-sheet.component.css',
  imports: [PhoneCardsComponent],
  template: `
    @if (view(); as p) {
      <dl class="gd-phone-facts">
        @for (fact of p.facts; track fact.name) {
          <dt>{{ fact.name }}</dt>
          <dd [attr.data-clock]="fact.clock ? '' : null">{{ fact.value }}</dd>
        }
      </dl>
      @for (pile of p.piles; track pile.pile) {
        <section [attr.data-pile]="pile.pile">
          <h3>{{ pile.title }} ({{ pile.count }})</h3>
          @if (pile.cards === null) {
            <div class="gd-phone-note">Only its owner sees these cards.</div>
          } @else {
            <gd-phone-cards class="gd-phone-sheet__cards" [cards]="pile.cards" empty="Empty" />
          }
        </section>
      }
    }
  `,
})
export class PhonePilesSheetComponent {
  readonly sheet = input.required<Extract<Sheet, { kind: 'piles' }>>();

  private readonly session = inject(GameSession);
  private readonly clocks = inject(GameClocks);

  protected readonly view = computed(() => {
    const model = this.session.model();
    const player = this.sheet().player;
    const own = !model.spectator && player === model.participantId;
    const zones = model.stats?.playerZones[player];
    const threat = model.stats?.threats[player];
    const seconds = this.clocks.view().banks[player];
    const count = (n: number | null | undefined) => (n === null || n === undefined ? '–' : String(n));
    const cardsOf = (pile: PileName) => pileCards(model, pile, player).map((card) => ({ card, under: [] }));
    const piles: PileView[] = [
      {
        pile: 'adventureDeck',
        title: PILE_TITLES.adventureDeck,
        cards: own ? cardsOf('adventureDeck') : null,
        count: count(zones?.adventureDeck),
      },
      { pile: 'deadPile', title: PILE_TITLES.deadPile, cards: cardsOf('deadPile'), count: count(zones?.dead) },
      {
        pile: 'discard',
        title: PILE_TITLES.discard,
        cards: model.discardPublic || own ? cardsOf('discard') : null,
        count: count(zones?.discard),
      },
      {
        pile: 'removedPile',
        title: PILE_TITLES.removedPile,
        cards: cardsOf('removedPile'),
        count: count(zones?.removed),
      },
    ];
    if (handRevealed(model, player)) {
      const hand = cardsOf('revealedHand');
      piles.push({
        pile: 'revealedHand',
        title: own ? 'Your revealed hand' : PILE_TITLES.revealedHand,
        cards: hand,
        count: String(hand.length),
      });
    }
    if (own && model.ended) {
      piles.push({ pile: 'deck', title: PILE_TITLES.deck, cards: cardsOf('deck'), count: count(zones?.deck) });
    }
    return {
      facts: [
        { name: 'Time bank', value: seconds === undefined ? '–' : clockText(seconds), clock: true },
        { name: 'Site', value: count(model.positions[player]), clock: false },
        { name: 'Hand', value: count(zones?.hand), clock: false },
        { name: 'Draw deck', value: count(zones?.deck), clock: false },
        {
          name: 'Threats',
          value: threat ? (threat.total === '-1' ? threat.value : `${threat.value} / ${threat.total}`) : '–',
          clock: false,
        },
      ],
      piles,
    };
  });
}
