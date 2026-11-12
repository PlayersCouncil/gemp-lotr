import { CollectionRole, DeckbuilderCardRef, DeckbuilderCardView } from '../actions/deckbuilder-card-ref';
import { CollectionItem, CollectionPage } from '../api/models';
import { isPlaceholder } from '../cards/card-blueprint';
import { CardViews, CardViewSpec } from '../cards/card-views.service';

export interface CollectionCardsOptions {
  views: CardViews;
  /** Copies of a card left once the deck's are taken out (CollectionCounts.available). */
  available: (blueprintId: string, count: number) => number;
}

/** The cards of a collection page, as the legacy page showed them: packs first, then the cards, in server order. */
export function collectionCards(page: CollectionPage | null, options: CollectionCardsOptions): DeckbuilderCardView[] {
  if (!page) {
    return [];
  }
  const items = [...page.items.filter((i) => i.kind === 'pack'), ...page.items.filter((i) => i.kind === 'card')];
  return items.map((item) => collectionCard(item, options));
}

function collectionCard(item: CollectionItem, { views, available }: CollectionCardsOptions): DeckbuilderCardView {
  const view = (
    role: CollectionRole,
    zone: string | null,
    look: Pick<CardViewSpec<unknown>, 'count' | 'noErrata' | 'noFoil' | 'border'>,
  ): DeckbuilderCardView =>
    views.create<DeckbuilderCardRef>({
      ref: { kind: 'collection', role, blueprintId: item.blueprintId, item },
      blueprintId: item.blueprintId,
      zone,
      role,
      count: item.count,
      ...look,
    });
  if (item.kind === 'pack') {
    return view(item.blueprintId.startsWith('(S)') ? 'selection' : 'pack', 'pack', {
      noErrata: true,
      noFoil: true,
      border: false,
    });
  }
  if (isPlaceholder(item.blueprintId)) {
    // A promised prize: shown, but not something to put in a deck, so it has no deck count
    return view('placeholder', item.side, { noErrata: true, noFoil: true, border: true });
  }
  return view('card', item.side, { count: available(item.blueprintId, item.count) });
}
