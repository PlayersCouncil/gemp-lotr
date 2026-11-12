import { CollectionItem } from '../api/models';
import { CardView } from '../cards/card-view';
import { DeckCard, DeckSlot, DrawCopy } from '../deck/deck-model';

/** What an item of the collection is. */
export type CollectionRole = 'card' | 'pack' | 'selection' | 'placeholder';

/** What a card on the deck builder stands for: what clicking it does (DeckbuilderActions). */
export type DeckbuilderCardRef =
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

export type DeckbuilderCardView = CardView<DeckbuilderCardRef>;
