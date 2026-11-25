/** A queue's or tournament's prizes, as data: the Prizes cell's label and the breakdown behind it. */
export interface HallPrizes {
  /** "Prize Breakdown", "No prizes", ... ("" when the server sent nothing). */
  readonly label: string;
  /** The breakdown, one plain-text line each; empty when there is none. */
  readonly lines: readonly string[];
}

/** A tournament's kind (Tournament.TournamentType, lower case as the hall compares it). */
export type HallEventType = 'constructed' | 'sealed' | 'solodraft' | 'table_solodraft' | 'table_draft' | (string & {});

/** One tournament queue of the hall poll (<queue>, HallCommunicationChannel.visitTournamentQueue), typed. */
export interface HallQueue {
  readonly id: string;
  /** Entry cost in silver. */
  readonly cost: number;
  readonly collection: string;
  /** The format's display name. */
  readonly format: string;
  /** Lower case. */
  readonly type: HallEventType;
  /** The queue's (event's) name. */
  readonly name: string;
  readonly playerCount: number;
  /** "alice, bob" as the server writes it. */
  readonly playerList: string;
  readonly prizes: HallPrizes;
  /** The pairing ("Swiss", "Swiss, minimum players: 4"). */
  readonly system: string;
  /** "When 4 players join", "at 2026-11-20 18:00", ... */
  readonly start: string;
  /** The viewer is signed up. */
  readonly signedUp: boolean;
  readonly joinable: boolean;
  /** The viewer may start it early. */
  readonly startable: boolean;
  /** Seconds left in the ready check; -1 when none is running. */
  readonly readyCheckSecsRemaining: number;
  readonly confirmedReadyCheck: boolean;
  /** World Championship. */
  readonly wc: boolean;
  /** A table draft's draft format code (the event name opens its page). */
  readonly draftCode: string | null;
  readonly recurring: boolean;
  readonly scheduled: boolean;
  /** Also shown as a row in Waiting Tables. */
  readonly displayInWaitingTables: boolean;
}

/** One running tournament of the hall poll (<tournament>, HallCommunicationChannel.visitTournament), typed. */
export interface HallTournament {
  readonly id: string;
  readonly collection: string;
  readonly format: string;
  readonly name: string;
  readonly system: string;
  /** Lower case. */
  readonly type: HallEventType;
  /** The stage as the server writes it ("Playing Games", "Deck-building", ...). */
  readonly stage: string;
  readonly round: number;
  readonly playerCount: number;
  /** "alice, bob*" ("*" = abandoned). */
  readonly playerList: string;
  readonly signedUp: boolean;
  /** The viewer has abandoned it. */
  readonly abandoned: boolean;
  /** It can still be joined late. */
  readonly joinable: boolean;
  readonly wc: boolean;
  /** "12 minutes" (DateUtils.HumanDuration), when the stage has a deadline. */
  readonly timeRemaining: string | null;
}

/** What a queue's row offers the viewer (hallUi.js drawQueue's action cell), in order. */
export type HallQueueAction =
  | { readonly kind: 'join' }
  | { readonly kind: 'leave' }
  | { readonly kind: 'start' }
  /** "READY CHECK - 12 s", or "Waiting for others - 12 s" (disabled) once confirmed. */
  | { readonly kind: 'ready-check'; readonly label: string; readonly confirmed: boolean }
  | { readonly kind: 'prizes' };

/** What a tournament's row offers (drawTournament's action cell), in order. */
export type HallTournamentAction =
  | { readonly kind: 'go-to-draft'; readonly url: string }
  | { readonly kind: 'register-deck' }
  /** "Abandon Tournament", drawn red. */
  | { readonly kind: 'abandon' }
  | { readonly kind: 'late-join' };

/** An event's name: plain, or (a table draft) a button that opens its draft format page. */
export interface HallEventName {
  readonly text: string;
  /** /gemp-lotr-server/deck/draftHtml?draftCode=..., opened in a new tab ("Show the draft format"), or null. */
  readonly draftFormatUrl: string | null;
}

/** A queue's row in the Recurring Events, Scheduled Events or World Championship Queues section. */
export interface HallQueueSectionRow {
  readonly queue: HallQueue;
  readonly format: string;
  /** The collection, or for a Sealed / Draft event its type ("Sealed", "Table Draft"). */
  readonly collection: string;
  readonly name: HallEventName;
  readonly start: string;
  readonly system: string;
  readonly cost: number;
  readonly prizes: HallPrizes;
}

/** A queue's row in Waiting Tables (displayInWaitingTables). */
export interface HallQueueWaitingRow {
  readonly queue: HallQueue;
  readonly format: string;
  /** "World Championship - " / "Swiss Tournament - " / "" then "<Type> - ", followed by `name`. */
  readonly prefix: string;
  readonly name: HallEventName;
  readonly start: string;
  readonly players: string;
  readonly actions: readonly HallQueueAction[];
  /** The viewer is signed up (the row is marked like a table the viewer sits at). */
  readonly played: boolean;
  /** World Championship: bold. */
  readonly bold: boolean;
}

/** A running tournament's row in Playing Tables. */
export interface HallTournamentRow {
  readonly tournament: HallTournament;
  readonly format: string;
  /** "Swiss Tournament - Constructed - Casual Fellowship Block", ... */
  readonly info: string;
  /** "Playing Games - Round 2", "Deck-building - 12 minutes", ... */
  readonly status: string;
  /** Up to 8 players: their list; more: a count that opens "Competing Players". */
  readonly players: { readonly list: string } | { readonly count: number; readonly list: string };
  readonly actions: readonly HallTournamentAction[];
  /** The draft page while a draft is on (the "Go to Draft" button; opened when the tournament appears), or null. */
  readonly draftUrl: string | null;
  readonly played: boolean;
  readonly bold: boolean;
}
