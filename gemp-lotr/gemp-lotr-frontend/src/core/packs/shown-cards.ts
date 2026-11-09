import { DeckbuilderCardRef, DeckbuilderCardView } from '../actions/deckbuilder-card-ref';
import { CardViews } from '../cards/card-views.service';
import { ShownCard } from './packs.service';

/**
 * The choices of a selection pack ("Choose one"): drawn like collection cards, without counts; using one picks it
 * (`choosable`).
 */
export function selectionChoiceCards(choices: readonly string[], views: CardViews): DeckbuilderCardView[] {
  return choices.map((blueprintId) =>
    views.create<DeckbuilderCardRef>({
      ref: { kind: 'shown', blueprintId, zone: 'selection', choosable: true },
      blueprintId,
      zone: 'selection',
    }),
  );
}

/**
 * What was opened ("New items"): packs, then cards, each with its count; never borderless, and packs never marked as
 * errata (deckbuilderDeliveryService.js).
 */
export function deliveredCards(cards: readonly ShownCard[], views: CardViews): DeckbuilderCardView[] {
  return cards.map((card) =>
    views.create<DeckbuilderCardRef>({
      ref: { kind: 'shown', blueprintId: card.blueprintId, zone: 'player', choosable: false },
      blueprintId: card.blueprintId,
      zone: 'player',
      role: card.pack ? 'pack' : 'card',
      noErrata: card.pack,
      border: true,
      count: card.count,
    }),
  );
}
