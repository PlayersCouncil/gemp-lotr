/** A table's status (TableStatus); the hall draws WAITING, PLAYING and FINISHED tables in their own sections. */
export type HallTableStatus = 'WAITING' | 'PLAYING' | 'FINISHED' | (string & {});

/** One game table of the hall poll (<table>, HallCommunicationChannel.visitTable), typed. */
export interface HallTable {
  readonly id: string;
  /** Set once the game exists (PLAYING and FINISHED tables). */
  readonly gameId: string | null;
  readonly status: HallTableStatus;
  /** The server's word for the status ("Waiting", "Playing", ...), shown as it is. */
  readonly statusDescription: string;
  /** The format's display name. */
  readonly format: string;
  /** The tournament or league the game belongs to, or "Casual". */
  readonly tournament: string;
  /** The creator's description (sanitized by the server); on an invite-only table the invited player's name. */
  readonly description: string;
  readonly players: readonly string[];
  /** The viewer sits at this table. */
  readonly playing: boolean;
  readonly watchable: boolean;
  readonly isPrivate: boolean;
  readonly isInviteOnly: boolean;
  /** Per viewer: the viewer is the one invited (invite-only tables). */
  readonly invitedYou: boolean;
  /** A finished game's winner. */
  readonly winner: string | null;
  /** When the table was opened, epoch ms on the server's clock; null when not sent. */
  readonly createdAt: number | null;
}

/** What the viewer can do at a table: its row's one button, or nothing. */
export type HallTableAction = 'leave' | 'join' | 'play' | 'watch';

/** A table as the hall lists it: the table, its action, its note and its place in its section. */
export interface HallTableRow {
  readonly table: HallTable;
  readonly action: HallTableAction | null;
  /** The italic note after the tournament name ("Private match for user 'bob'."), or null. */
  readonly note: string | null;
  /** The game page for "Play Match" / "Spectate", or null. */
  readonly gameUrl: string | null;
}

/** The three table sections of the Game Hall tab, each in the order the legacy hall drew its rows. */
export interface HallTableSections {
  readonly waiting: readonly HallTableRow[];
  readonly playing: readonly HallTableRow[];
  readonly finished: readonly HallTableRow[];
}

/** A Casual table timer offered by the server (POST /deck/formats `HallTimers`, GameTimer.HALL_TIMERS). */
export interface HallTimer {
  readonly code: string;
  readonly name: string;
  readonly minutesPerPlayer: number;
  readonly minutesPerDecision: number;
  /** The server's sentence about the limits (GameTimer.describeLimits). */
  readonly description: string | null;
}

/** A format offered for a hall table, in the order of the dropdown. */
export interface HallFormatOption {
  readonly code: string;
  readonly name: string;
}

/** Where a deck comes from: the player's own decks or the Deck Library (the server's `deckSource`). */
export type HallDeckSource = 'player' | 'library';

/** A deck in the Play and Join deck pickers. */
export interface HallDeckOption {
  readonly name: string;
  /** The display name of the deck's format (decks carry the name, not the code). */
  readonly formatName: string;
  readonly source: HallDeckSource;
}

/** The deck chosen in a picker. */
export interface HallDeckChoice {
  readonly name: string;
  readonly source: HallDeckSource;
}

/** The Casual table form (CreateUnrankedTable), as submitted. */
export interface CasualTableForm {
  readonly format: string;
  readonly deck: HallDeckChoice | null;
  /** A timer code; "default" when none is chosen. */
  readonly timer: string;
  readonly description: string;
  readonly isPrivate: boolean;
  readonly isInviteOnly: boolean;
  /** Invite-only: the player to invite (becomes the table's description). */
  readonly invitee: string;
  /** "Keep this window open": a success is reported in the form instead of closing it. */
  readonly keepOpen: boolean;
}

/** The Bot table form (CreateBotTable). */
export interface BotTableForm {
  readonly format: string;
  readonly deck: HallDeckChoice | null;
  /** The bot's deck name; "" = a random FotR starter. */
  readonly botDeck: string;
}

/** What a hall table call answered on HTTP 200 (TableFlow.readResponse) or how it failed. */
export interface TableCallResult {
  readonly ok: boolean;
  readonly message: string;
}

/** The result box of a flow ("Result: Ready."). */
export type TableResultKind = 'ready' | 'pending' | 'success' | 'error';

export interface TableResult {
  readonly kind: TableResultKind;
  readonly message: string;
}

/** Which flow a result belongs to. */
export type TableFlow = 'casual' | 'bot' | 'join' | 'leave';

/** A started game the browser would not open in a new tab: the "Your game is ready" prompt's link. */
export interface ReadyGame {
  readonly gameId: string;
  readonly url: string;
}
