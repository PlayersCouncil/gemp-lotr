/** What a hall row change does: rows are drawn (add), redrawn (update) or taken away (remove). */
export type HallChangeAction = 'add' | 'update' | 'remove';

/**
 * One <queue>, <tournament> or <table> of a hall answer: its action, id, and every other attribute as sent (the
 * typed rows are part 4.2's, see README.md). A "remove" carries no other attributes.
 */
export interface HallItemChange {
  readonly action: HallChangeAction;
  readonly id: string;
  readonly attributes: Readonly<Record<string, string>>;
}

/**
 * One answer of the hall long-poll: GET /hall (`full`: a snapshot of everything, drawn on a cleared hall) or
 * POST /hall/update (only what changed since the previous answer on that channel).
 */
export interface HallUpdate {
  readonly full: boolean;
  /** The channel the next POST /hall/update polls. */
  readonly channelNumber: number | null;
  /** The player's silver ("pocket", shown on My Account). */
  readonly currency: number | null;
  /** The server's clock, "yyyy-MM-dd HH:mm:ss" (GMT), shown at the right of the main bar. */
  readonly serverTime: string | null;
  /** The same clock in epoch ms (for the waiting tables' timers). */
  readonly serverTimeMs: number | null;
  /** The message of the day; only sent when it changed (and on a snapshot, when there is one). Server markup. */
  readonly motd: string | null;
  /** The server is in shutdown mode ahead of a restart. */
  readonly shutdown: boolean;
  readonly queues: readonly HallItemChange[];
  readonly tournaments: readonly HallItemChange[];
  readonly tables: readonly HallItemChange[];
  /** Games that started for this player (<newGame id>), in order. A new channel repeats those still running. */
  readonly newGames: readonly string[];
}

/** The connection readout's states (hallUi.js HallConnectionIndicator). */
export type HallConnectionState =
  'connecting' | 'connected' | 'shutdown' | 'reconnecting' | 'disconnected' | 'loggedout';

/** What the readout's popup offers besides its message. */
export type HallConnectionAction = 'reload' | 'login';

export interface HallConnectionDetail {
  readonly message: string | null;
  readonly action: HallConnectionAction | null;
}

export interface HallConnection {
  readonly state: HallConnectionState;
  readonly detail: HallConnectionDetail | null;
  /** The server clock at the last successful update ("yyyy-MM-dd HH:mm:ss", GMT), or null before the first. */
  readonly lastUpdate: string | null;
}
