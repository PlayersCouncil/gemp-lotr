import { CollectionItem } from '../api/models';
import { DeckCard, DeckSlot, DrawCopy } from '../deck/deck-model';

/**
 * What a card on the page stands for: what clicking
 * it does, and which card its details show.
 */
/** What an item of the collection is. */
export type CollectionRole = 'card' | 'pack' | 'selection' | 'placeholder';

export type CardRef =
  /** A collection item. */
  | { kind: 'collection'; role: CollectionRole; blueprintId: string; item: CollectionItem }
  /** A card in a special slot or the sites. */
  | { kind: 'slot'; slot: DeckSlot; blueprintId: string; card: DeckCard }
  /** The shown card of a draw deck pile entry. */
  | { kind: 'draw'; blueprintId: string; card: DrawCopy }
  /** A copy drawn behind a draw deck card; clicking it does nothing, as before. */
  | { kind: 'behind'; blueprintId: string; card: DrawCopy }
  /** The Race to Mount Doom meta-site (display only), with its modifier drawn over it. */
  | { kind: 'race'; blueprintId: string; overlayBlueprintId: string | null }
  /** A card that is only shown (opened-pack contents); `choosable` for a selection pack's choices. */
  | { kind: 'shown'; blueprintId: string; zone: string; choosable: boolean };

/** The zone the legacy page created the card with (it decides e.g. whether a site is drawn landscape). */
export function zoneOf(ref: CardRef): string | null {
  switch (ref.kind) {
    case 'collection':
      return ref.item.kind === 'pack' ? 'pack' : ref.item.side;
    case 'slot':
    case 'race':
      return 'deck';
    case 'draw':
      return ref.card.side;
    case 'behind':
      return 'attached';
    case 'shown':
      return ref.zone;
  }
}
