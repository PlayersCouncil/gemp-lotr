import { DOCUMENT } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { CardImageService } from '../../../core/cards/card-image.service';
import { CardView } from '../../../core/cards/card-view';
import { foilOverlayImage, readFoilPresentation } from '../../../core/cards/foil-presentation';
import { pileStacks, raceCardView, slotCards } from '../../../core/deck/deck-cards';
import { DeckSlot } from '../../../core/deck/deck-model';
import { DeckDraft } from '../../../core/deck/deck-draft.service';
import { DeckEditing } from '../../../core/deck/deck-editing.service';
import { DeckFormatSelection } from '../../../core/deck/deck-format-selection.service';
import { CardComponent, cardElementOf } from '../../shared/card/card.component';
import { FitCard } from '../../shared/fit-cards/fit-cards';
import { FitCardsDirective } from '../../shared/fit-cards/fit-cards.directive';
import { DeckStatsComponent } from './deck-stats.component';
import { DeckToolbarComponent } from './deck-toolbar.component';

/** A slot of the deck side, as the page shows it. */
interface SlotView {
  slot: DeckSlot | 'race';
  /** `data-slot`. */
  name: string;
  title: string;
  hint: string;
  /** Clicking the slot selects cards for it (not the Race slot, which is display only). */
  selectable: boolean;
  /** Absent from the format (the Map outside the movie formats, the Race outside RTMD). */
  hidden: boolean;
  cards: CardView[];
  fit: FitCard[];
}

/** Keeps a card's element while the deck changes: its page-unique deck card id. */
function cardKey(card: CardView): number | string {
  const ref = card.ref;
  return ref.kind === 'slot' || ref.kind === 'draw' || ref.kind === 'behind' ? ref.card.id : ref.blueprintId;
}

function slotView(view: Omit<SlotView, 'fit'>): SlotView {
  return { ...view, fit: view.cards.map((card) => ({ horizontal: card.horizontal })) };
}

/**
 * The deck side of the page (the legacy #deckDiv): the toolbar, the Ring-bearer, Ring, Map and Race slots, the sites,
 * the Free Peoples and Shadow piles of the draw deck, and the deck stats. Clicking a slot (outside its cards) selects
 * cards for it, as the legacy page did: the collection shows what fits it and the next card clicked goes there.
 * Clicking a site also shows the sites, as on the legacy page.
 *
 * DOM contract (`.gd-deck`):
 * - `gd-deck-toolbar.gd-toolbar`;
 * - `section.gd-slot` per slot (`data-slot` = ring-bearer, ring, map, race, sites; `hidden` when the format has none;
 *   `data-count` = cards in it) holding `.gd-slot__label` (a `button` for the slots that select; the race one is a
 *   `div`) with `.gd-slot__title` and `.gd-slot__hint`, the Map's `a.gd-slot__link`, then `.gd-slot__cards` (the
 *   fitCards box of the Ring-bearer, Ring, Map and Race; for the sites, `--gd-slot-count` = number of sites) with a
 *   `button.gd-card` per card;
 * - `section.gd-pile` per draw deck side (`data-side` = free-peoples, shadow; `data-count` = cards in it, copies
 *   included) holding `.gd-pile__cards` (the fitCards box) with a `.gd-stack` per card: its other copies first
 *   (`button.gd-card[data-role=copy]`, oldest first), then the card;
 * - `gd-deck-stats.gd-stats`.
 */
@Component({
  selector: 'gd-deck',
  imports: [CardComponent, FitCardsDirective, DeckToolbarComponent, DeckStatsComponent],
  host: { class: 'gd-deck' },
  template: `
    <gd-deck-toolbar />
    @for (slot of slots(); track slot.slot) {
      <section
        class="gd-slot"
        [attr.data-slot]="slot.name"
        [attr.data-count]="slot.cards.length"
        [attr.aria-label]="slot.title"
        [hidden]="slot.hidden"
      >
        @if (slot.selectable) {
          <button type="button" class="gd-slot__label" (click)="select(slot.slot)">
            <span class="gd-slot__title">{{ slot.title }}</span>
            <span class="gd-slot__hint">{{ slot.hint }}</span>
          </button>
        } @else {
          <div class="gd-slot__label">
            <span class="gd-slot__title">{{ slot.title }}</span>
            <span class="gd-slot__hint">{{ slot.hint }}</span>
          </div>
        }
        @if (slot.slot === 'map') {
          <a class="gd-slot__link" href="https://wiki.lotrtcgpc.net/wiki/Map" target="_blank">See here for details.</a>
        }
        @if (slot.slot === 'sites') {
          <!-- eslint-disable-next-line @angular-eslint/template/click-events-have-key-events, @angular-eslint/template/interactive-supports-focus -- only hears clicks bubbling up from its card buttons -->
          <div class="gd-slot__cards" [style.--gd-slot-count]="slot.cards.length" (click)="siteClicked($event)">
            @for (card of slot.cards; track cardKey(card)) {
              <button [gdCard]="card"></button>
            }
          </div>
        } @else {
          <div class="gd-slot__cards" [gdFitCards]="slot.fit">
            @for (card of slot.cards; track cardKey(card)) {
              <button [gdCard]="card"></button>
            }
          </div>
        }
      </section>
    }
    @for (pile of piles(); track pile.side) {
      <section
        class="gd-pile"
        [attr.data-side]="pile.name"
        [attr.data-count]="pile.count"
        [attr.aria-label]="pile.title"
      >
        <div class="gd-pile__cards" [gdFitCards]="pile.fit">
          @for (stack of pile.stacks; track cardKey(stack.card)) {
            <div class="gd-stack">
              @for (copy of stack.behind; track cardKey(copy)) {
                <button [gdCard]="copy"></button>
              }
              <button [gdCard]="stack.card"></button>
            </div>
          }
        </div>
      </section>
    }
    <gd-deck-stats />
  `,
})
export class DeckAreaComponent {
  private readonly draft = inject(DeckDraft);
  private readonly formats = inject(DeckFormatSelection);
  private readonly editing = inject(DeckEditing);
  private readonly options = {
    images: inject(CardImageService),
    foilImage: foilOverlayImage(readFoilPresentation(inject(DOCUMENT).cookie)),
  };

  protected readonly slots = computed<SlotView[]>(() => {
    const deck = this.draft.deck();
    const race = raceCardView(this.formats.raceCard(), this.options);
    const slot = (slot: DeckSlot, name: string, title: string, hint: string, hidden = false): SlotView =>
      slotView({ slot, name, title, hint, selectable: true, hidden, cards: slotCards(deck, slot, this.options) });
    return [
      slot('ringBearer', 'ring-bearer', 'Ring-bearer', '(Click here to select your Ring-bearer.)'),
      slot('ring', 'ring', 'The One Ring', '(Click here to select your Ring.)'),
      slot('map', 'map', 'Map', '(Click here to select your Map.)', !this.formats.usesMap()),
      slotView({
        slot: 'race',
        name: 'race',
        title: 'Race',
        hint: '(Your current Race to Mount Doom meta-site.)',
        selectable: false,
        hidden: !this.formats.usesRace(),
        cards: race ? [race] : [],
      }),
      slot(
        'sites',
        'sites',
        'Sites',
        '(Click here to select Sites. When done, you will need to manually reset the Card Type filter.)',
      ),
    ];
  });

  protected readonly piles = computed(() => {
    const deck = this.draft.deck();
    return (
      [
        ['FREE_PEOPLE', 'free-peoples', 'Free Peoples'],
        ['SHADOW', 'shadow', 'Shadow'],
      ] as const
    ).map(([side, name, title]) => {
      const stacks = pileStacks(deck, side, this.options);
      return {
        side,
        name,
        title,
        stacks,
        count: stacks.reduce((sum, stack) => sum + 1 + stack.behind.length, 0),
        fit: stacks.map((stack) => ({
          horizontal: stack.card.horizontal,
          behind: stack.behind.map((copy) => copy.horizontal),
        })),
      };
    });
  });

  protected readonly cardKey = cardKey;

  protected select(slot: DeckSlot | 'race'): void {
    if (slot !== 'race') {
      this.editing.slotClicked(slot);
    }
  }

  /** Clicking a site removes it and, as on the legacy page (whose sites box saw the click too), shows the sites. */
  protected siteClicked(event: MouseEvent): void {
    if (cardElementOf(event.target)) {
      this.editing.slotClicked('sites');
    }
  }
}
