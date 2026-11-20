/** A user's role in the list (the <user role> attribute). */
export type ChatRole = 'admin' | 'leagueAdmin';

/** A user in the room's list (ChatRequestHandler.serializeChatRoomData). */
export interface ChatUser {
  readonly name: string;
  readonly role: ChatRole | null;
  /** The viewer's own entry (listed first, even while incognito). */
  readonly self: boolean;
  /** Only on the viewer's own entry: hidden from everyone but admins. */
  readonly incognito: boolean;
}

/** A <message> as the server sent it. */
export interface ChatServerMessage {
  /** "System", "TournamentSystem", "TournamentSystemTo:<name>;<name>", or a player's name. */
  readonly from: string;
  /** Epoch ms. */
  readonly date: number;
  /**
   * The message as server markup: a player's text rendered from markdown with HTML escaped (MarkdownParser, links
   * written out), or a system text with simple tags such as <br />. See README.md for how it is shown.
   */
  readonly html: string;
}

/** One answer of GET or POST /chat/{room}: the new messages and the whole user list. */
export interface ChatSnapshot {
  readonly room: string;
  readonly messages: readonly ChatServerMessage[];
  readonly users: readonly ChatUser[];
}

/** How a line of the chat shows (the legacy message classes). */
export type ChatMessageKind = 'chat' | 'system' | 'warning';

/** A line mentioning the viewer: "@name" (ping) or the bare name, "@everyone" or "@anyone" (mention). */
export type ChatHighlight = 'ping' | 'mention';

/** A line of the chat as the view shows it. */
export interface ChatLine {
  /** Unique within the page, for tracking. */
  readonly id: number;
  readonly kind: ChatMessageKind;
  /** Shown before the message ("TournamentSystem" for both tournament senders); null for warnings. */
  readonly from: string | null;
  /** Epoch ms; null for warnings (made by the page, not sent). */
  readonly date: number | null;
  /** Server markup for messages; plain text for warnings. */
  readonly html: string;
  readonly highlight: ChatHighlight | null;
  /** A warning that offers the login link ("Click here to register or log in."). */
  readonly loginLink: boolean;
}

/** A tournament message, also shown in a dialog to the players it is meant for (hallUi.js tournamentCallback). */
export interface TournamentAnnouncement {
  /** Server markup (shown through serverMarkup rules: only <b>, <strong>, <i>, <em>, <u>, <br>). */
  readonly message: string;
  /** null: to everyone in a tournament queue ("TournamentSystem"); else the names it is addressed to. */
  readonly recipients: readonly string[] | null;
}

/** POST /player/ignores/add and /remove (IgnoreListService). */
export interface IgnoreResult {
  readonly message: string | null;
  readonly ignored: readonly string[] | null;
}
