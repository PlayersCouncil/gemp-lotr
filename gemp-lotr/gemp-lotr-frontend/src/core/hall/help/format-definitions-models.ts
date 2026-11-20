/**
 * Help › Format Definitions' data: GET /hall/formats/json (HallRequestHandler.getFormatDefinitions,
 * FormatDefinitions.describeAll), every hall format in the Play menu's order. JsonUtils.Serialize leaves out what is
 * null (a format without a description or set summary, a card without collector's info or a known set).
 */

/** A card on one of a format's lists (FormatDefinitions.cards): shown through the app's card code by its blueprint. */
export interface FormatCard {
  readonly id: string;
  /** With the uniqueness dots and subtitle ("·Aragorn, Heir to the White City"); the id when the server knows no card. */
  readonly name: string;
  /** The collector's info as printed ("1R40"; the V-sets' "V1_5"), or null. */
  readonly collInfo: string | null;
  readonly set: number | null;
  /** How players write the set ("1", "V1"), or null. */
  readonly setLabel: string | null;
  readonly setName: string | null;
}

/** The card lists, in the order the page shows them (GempFormatDefinitions.LISTS). */
export type FormatListKey = 'banned' | 'restricted' | 'limit2' | 'limit3' | 'valid';

export interface FormatListDefinition {
  readonly key: FormatListKey;
  readonly title: string;
  readonly note: string;
}

export const FORMAT_LISTS: readonly FormatListDefinition[] = [
  { key: 'banned', title: 'X-list', note: "can't be used" },
  { key: 'restricted', title: 'R-list', note: 'no more than 1 copy per deck' },
  { key: 'limit2', title: 'Limit 2', note: 'up to two copies per deck' },
  { key: 'limit3', title: 'Limit 3', note: 'up to three copies per deck' },
  { key: 'valid', title: 'Also legal', note: "cards from outside the format's sets" },
];

/** Which errata a format plays with (FormatDefinitions.errata). */
export interface FormatErrata {
  /** The live Player's Council errata in place of the originals. */
  readonly pc: boolean;
  readonly playtest: boolean;
  /** The PC errata sets are legal alongside the originals (Anything Goes). */
  readonly pcCardsLegal: boolean;
}

export interface FormatDefinition {
  readonly code: string;
  readonly name: string;
  /** "format-<code>" (FormatSummary.anchorId): the legacy page's section id and #format- link. */
  readonly anchor: string;
  readonly description: string | null;
  /** "Cards from sets 1-10, V1-V3", or null. */
  readonly setSummary: string | null;
  readonly sites: string | null;
  readonly cancelRingBearerSkirmish: boolean;
  readonly minimumDeckSize: number;
  readonly maximumSameName: number;
  readonly validateShadowFPCount: boolean;
  readonly winAtEndOfRegroup: boolean;
  readonly discardPileIsPublic: boolean;
  readonly winOnControlling5Sites: boolean;
  readonly ruleOfFour: boolean;
  readonly mulliganRule: boolean;
  readonly usesMaps: boolean;
  readonly playtest: boolean;
  readonly errata: FormatErrata;
  readonly lists: Readonly<Record<FormatListKey, readonly FormatCard[]>>;
  /** Titles a deck may hold one copy of, whatever the card ("R-list by title"). */
  readonly restrictedNames: readonly string[];
}

/** What the page shows while it loads, once loaded, or in place of the formats. */
export type FormatDefinitionsLoad =
  | { readonly kind: 'loading' }
  | { readonly kind: 'ready'; readonly formats: readonly FormatDefinition[] }
  | { readonly kind: 'error'; readonly message: string };

/** A line of a format's facts (renderFacts). */
export type FormatFact =
  | { readonly kind: 'text'; readonly label: string; readonly text: string }
  | {
      readonly kind: 'errata';
      readonly label: string;
      /**
       * The "PC Errata" link: to the errata of this format (`format` its code) or to the whole list (`format` null,
       * where the PC versions are only legal alongside the originals); null when there is no link.
       */
      readonly link: { readonly text: string; readonly format: string | null } | null;
      /** What follows the link. */
      readonly text: string;
    }
  | { readonly kind: 'rules'; readonly label: string; readonly rules: readonly string[] };

/** A card list as the page draws it: the list's cards, and as names grouped by set (one row per set). */
export interface FormatListView {
  readonly key: FormatListKey;
  readonly title: string;
  readonly note: string;
  readonly cards: readonly FormatCard[];
  readonly sets: readonly FormatSetRow[];
}

export interface FormatSetRow {
  /** The set as players write it, "?" when unknown. */
  readonly label: string;
  /** Its name, the tag's tooltip, if known. */
  readonly name: string | null;
  readonly cards: readonly FormatCard[];
}

/** The contents list's "X3 R2" beside a format's name, and its tooltip. */
export interface FormatNavMeta {
  readonly text: string;
  readonly title: string;
}
