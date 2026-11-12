/** Typed views of the Gemp server responses used by the deck builder. The server API itself is unchanged. */

export type CardSide = 'FREE_PEOPLE' | 'SHADOW' | (string & {});

/** Which deck slot a collection card belongs in (CollectionRequestHandler.appendCardGroup). */
export type CardGroup = 'ring' | 'site' | 'map' | 'ringBearer' | 'fp' | 'shadow';

/** An entry of GET /deck/list or /deck/libraryList. */
export interface DeckSummary {
  name: string;
  /** Display name of the deck's format; RTMD league decks are prefixed with "Race: ". */
  formatName: string;
}

export interface DeckTargetFormat {
  formatName: string;
  formatCode: string;
  /** Set when the deck was saved for a Race to Mount Doom league that is still running. */
  leagueCode: string | null;
}

export interface DeckDrawCard {
  blueprintId: string;
  side: CardSide;
}

/** A deck as returned by GET /deck, /deck/library, POST /deck/rename and /deck/convert. */
export interface Deck {
  targetFormat: DeckTargetFormat | null;
  notes: string;
  ringBearer: string | null;
  ring: string | null;
  map: string | null;
  /** Sorted by site number by the server. */
  sites: string[];
  /** One entry per copy, sorted by card type, culture and name by the server. */
  drawDeck: DeckDrawCard[];
}

export interface CollectionCard {
  kind: 'card';
  blueprintId: string;
  count: number;
  side: CardSide | null;
  group: CardGroup | null;
}

export interface CollectionPack {
  kind: 'pack';
  blueprintId: string;
  count: number;
  /** For selection packs only: the blueprint IDs the player can choose from. */
  selection: string[] | null;
}

export type CollectionItem = CollectionCard | CollectionPack;

/** One page of GET /collection/{type}. */
export interface CollectionPage {
  /** Total number of items matching the filter (not just this page). */
  total: number;
  items: CollectionItem[];
}

/** A limited collection the player can build from (GET /collection): league or tournament product. */
export interface CollectionTypeInfo {
  type: string;
  name: string;
  /** Format code the collection is meant for. */
  format: string;
}

export interface MetaSite {
  position: number;
  blueprintId: string;
  visualBlueprintId: string | null;
  name: string | null;
  siteOverride: boolean;
}

/** An entry of GET /league; only the fields the deck builder needs. */
export interface League {
  code: string;
  name: string;
  type: string;
  member: boolean;
  formatCode: string | null;
  /** RTMD only. */
  playerPosition: number | null;
  /** RTMD only. */
  pathLength: number | null;
  /** RTMD only: the meta-sites at the player's current position. */
  metaSites: MetaSite[];
}

/** Opened-pack contents for one collection (GET /delivery/packContents). */
export interface PackDelivery {
  collectionName: string;
  packs: { blueprintId: string; count: number }[];
  cards: { blueprintId: string; count: number }[];
}

export interface BlockFilter {
  name: string;
  filter: string;
  setFilters: Record<string, string>;
}

/** A format from POST /deck/formats (JSONDefs.Format); only the fields the deck builder uses are typed. */
export interface GempFormat {
  code: string;
  name: string;
  order: number;
  hall: boolean;
  usesMaps: boolean;
  blockFilters: BlockFilter[] | null;
  errata: Record<string, string>;
}

export interface FormatsReadout {
  Formats: Record<string, GempFormat>;
}
