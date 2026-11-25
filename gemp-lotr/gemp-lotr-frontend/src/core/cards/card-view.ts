/**
 * What a card on a page shows, whatever theme draws it. `Ref` is what the card stands for on that page, handed to the
 * page's CardActions when the card is used; the shared card code never looks inside it.
 */
export interface CardView<Ref = unknown> {
  /** As given, including markers such as "*" (foil). */
  blueprintId: string;
  /** The zone it is shown in (it decides e.g. whether a site is drawn landscape). */
  zone: string | null;
  ref: Ref;
  /** The card's role on the page, e.g. a card or a pack in the collection; the themes may tell them apart. */
  role: CardRole;
  imageUrl: string;
  /** Landscape (a site), drawn wider than tall. */
  horizontal: boolean;
  /** Drawn with the errata marker. */
  errata: boolean;
  /** Foil layer image, or null for none. */
  foilImage: string | null;
  /** Packs and other products have no card border. */
  border: boolean;
  /** The number shown on the card (copies left in the collection), or null for none. */
  count: number | null;
  /** An RTMD meta-site's modifier, drawn over the lower part of its position card. */
  overlayImage: string | null;
  /** The theme puts this card on whole pixels on screen, so what is placed beside it rounds its edges too. */
  wholePixels?: boolean;
}

/**
 * - `card`: a card (in the collection, the deck or a slot);
 * - `pack`, `selection`: a pack or a selection pack in the collection;
 * - `placeholder`: a promised prize in the collection;
 * - `copy`: another copy of a draw deck card, drawn behind it.
 */
export type CardRole = 'card' | 'pack' | 'selection' | 'placeholder' | 'copy';
