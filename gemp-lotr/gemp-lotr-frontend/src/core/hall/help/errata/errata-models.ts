/**
 * Help › PC Errata's data: GET /hall/errata/json (HallRequestHandler.getErrataInfo, ErrataCatalog.build), serialized by
 * JsonUtils.Serialize, which leaves nulls out. The page reads `entries`, `formats` and `counts`; the answer's older
 * `all` and `recent` maps (LotroCardBlueprintLibrary.getErrata, the pc_errata format's recent errata) go unread, as
 * they did on the legacy page.
 */

/** How a card came to be listed (ErrataCatalog.KIND_*). */
export type ErrataKind = 'errata' | 'playtest' | 'revision';

/** One version of the card (ErrataCatalog.describe): the original (`before`) or the PC's (`after`). */
export interface ErrataSide {
  /** The full name ("Gandalf, The Grey Wizard"), or null when the server sent none. */
  readonly name: string | null;
  /** The card type in words ("Companion", "The One Ring"), or null. */
  readonly type: string | null;
  /** Twilight, Strength, Vitality, Resistance, Site, Unique, as the server lists them (in its order). */
  readonly stats: Readonly<Record<string, ErrataStat>>;
  /** The game text as plain text (the server's search text: no markup but what slipped through). */
  readonly gametext: string;
}

export type ErrataStat = number | boolean | string | null;

/** One errata'd card (a row of the table, a card in the viewer), with what the filters and sorts use. */
export interface ErrataEntry {
  /** The blueprint shown: the errata's ("51_45"), or for a revision the card itself ("103_19"). */
  readonly id: string;
  /** The original's blueprint ("1_45"); the same as `id` for a revision. */
  readonly base: string;
  readonly kind: ErrataKind;
  /** The printed name: the original's when the two only differ by accents (PcErrata.prepare). */
  readonly name: string;
  /** The base set, as sent ("1", "103"). */
  readonly set: string;
  /** The set as a number for sorting (0 if it is none). */
  readonly setNum: number;
  readonly setName: string | null;
  readonly cardNum: number;
  /** The collector's number as printed ("1R45"; "V3 19"; else "<set> <number>"): PcErrata.collectorLabel. */
  readonly coll: string;
  /** The culture's name ("Ringwraith"), or null. */
  readonly culture: string | null;
  /** The culture's icon name (`images/cultures/<code>.png`), or null. */
  readonly cultureCode: string | null;
  readonly side: string | null;
  readonly type: string | null;
  /** How many times the PC has revised the card (0 when the server sent none). */
  readonly revision: number;
  /** In the latest batch of errata (the pc_errata format's list). */
  readonly recent: boolean;
  /** The hall formats it is in effect in, by code. */
  readonly formats: readonly string[];
  readonly after: ErrataSide | null;
  /** Null when there is nothing to compare with (a revision whose earlier version is gone). */
  readonly before: ErrataSide | null;
  /** The name and the collector's number as searched (folded; the number without spaces). */
  readonly searchName: string;
  readonly searchColl: string;
}

/** A hall format, in hall order, with how many entries are in effect in it. */
export interface ErrataFormat {
  readonly code: string;
  readonly name: string;
  readonly count: number;
}

/** How many entries there are of each kind. */
export interface ErrataCounts {
  readonly total: number;
  readonly errata: number;
  readonly playtest: number;
  readonly revision: number;
  readonly recent: number;
}

export interface ErrataCatalog {
  readonly entries: readonly ErrataEntry[];
  readonly formats: readonly ErrataFormat[];
  readonly counts: ErrataCounts;
}

/** The filters (PcErrata.FILTER_KEYS): each '' when unused. */
export interface ErrataFilters {
  /** The search box: a title, a collector's number ("1R45") or a set and card number ("1.45"). */
  readonly q: string;
  /** A base set, as sent ("1"). */
  readonly set: string;
  /** A format's code. */
  readonly format: string;
  /** The View chips: '' (All) or 'recent' (Latest). */
  readonly show: string;
}

export type ErrataFilterKey = keyof ErrataFilters;

/** The table's columns, which sort it (PcErrata.SORTS). */
export type ErrataSortKey = 'num' | 'culture' | 'name' | 'rev';

/** 1 ascending, -1 descending. */
export type SortDirection = 1 | -1;

export interface ErrataSort {
  readonly key: ErrataSortKey;
  readonly dir: SortDirection;
}

export type ErrataLoad =
  | { readonly kind: 'loading' }
  | { readonly kind: 'error'; readonly message: string }
  | { readonly kind: 'ready'; readonly catalog: ErrataCatalog };

/** An option of the Set or Format list. `extra`: a link's value the list leaves out, kept as the choice. */
export interface ErrataOption {
  readonly value: string;
  readonly label: string;
  readonly extra?: boolean;
}

/** A View chip: its value, label and how many cards it shows. */
export interface ErrataChip {
  readonly value: string;
  readonly label: string;
  readonly count: number;
}
