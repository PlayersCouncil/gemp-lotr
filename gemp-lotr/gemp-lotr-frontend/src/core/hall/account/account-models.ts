/**
 * The My Account tab's data, read from the server's answers (see README.md). Every text the server sends is kept as
 * text, never markup.
 */

// ---- Game History (GET /gameHistory, GET /gameHistory/filters) ----

/** One game of the player's history (GameHistoryUI.entryToRow). */
export interface GameHistoryEntry {
  /** From the player's side: won or lost. */
  readonly result: 'W' | 'L';
  readonly winReason: string | null;
  readonly loseReason: string | null;
  readonly opponent: string | null;
  /** The player's own deck. */
  readonly deck: string | null;
  readonly format: string | null;
  /** The league or tournament; null for a casual game. */
  readonly event: string | null;
  readonly startMs: number | null;
  readonly endMs: number | null;
  /** The end as the server wrote it, "yyyy-MM-dd HH:mm:ss" in server time (UTC). */
  readonly endTime: string | null;
  readonly durationMs: number | null;
  /** The player's own recording of the game; null when none was kept. */
  readonly recordingId: string | null;
}

/** One page of the history. */
export interface GameHistoryPage {
  /** The player whose history it is (the replay's "player$recording"). */
  readonly playerId: string;
  /** Games matching the filters. */
  readonly matching: number;
  /** All the player's games. */
  readonly total: number;
  /** The first game's index, as the server clamped it; null when it did not say. */
  readonly start: number | null;
  readonly entries: readonly GameHistoryEntry[];
}

/** The filters as applied (the form's fields, trimmed); "" is no filter. Dates are yyyy-MM-dd. */
export interface GameHistoryFilters {
  readonly format: string;
  readonly opponent: string;
  readonly event: string;
  readonly from: string;
  readonly to: string;
}

export const NO_GAME_HISTORY_FILTERS: GameHistoryFilters = { format: '', opponent: '', event: '', from: '', to: '' };

/** The filters' suggestions: formats most played first, events most recent first (at most 100). */
export interface GameHistoryFilterOptions {
  readonly formats: readonly string[];
  readonly events: readonly string[];
}

// ---- Your Stats (GET /playerStats) ----

export interface PlayerStatEntry {
  readonly format: string;
  readonly deckName: string;
  readonly wins: string;
  readonly losses: string;
  /** As the server wrote it ("62.5%"). */
  readonly percentage: string;
}

export interface PlayerStats {
  readonly casual: readonly PlayerStatEntry[];
  readonly competitive: readonly PlayerStatEntry[];
}

// ---- My Scenarios (GET /scenarios/mine, GET /scenarios/public) ----

export interface ShelfCampaign {
  readonly id: string | null;
  readonly title: string | null;
  /** 1-based; 0 when not known. */
  readonly position: number;
}

/** The public scenario a player's scenario is a version of. */
export interface ShelfOrigin {
  readonly id: string;
  readonly title: string | null;
  /** It is still public. */
  readonly exists: boolean;
}

/** What every shelf entry has (ScenarioShelf.describe). */
export interface ShelfScenario {
  /** The file, as the server shows it. */
  readonly path: string;
  /** The file's id (what a new copy is opened by). */
  readonly id: string | null;
  /** The scenario's id (to play it or open it); null when the file can't be read. */
  readonly fullId: string | null;
  readonly title: string | null;
  /** tutorial | puzzle | sandbox, or null (unreadable). */
  readonly kind: string | null;
  readonly summary: string | null;
  readonly campaign: ShelfCampaign | null;
  readonly playable: boolean;
  readonly verified: boolean;
  readonly openable: boolean;
  readonly errors: readonly string[];
}

/** submitted | rejected | approved, or null for one never submitted. */
export type ShelfStatus = string | null;

/** One of the player's own scenarios (ScenarioShelf.mine). */
export interface OwnScenario extends ShelfScenario {
  readonly status: ShelfStatus;
  readonly submittedAt: number | null;
  readonly feedback: string | null;
  readonly reviewedAt: number | null;
  readonly reviewedBy: string | null;
  /** In review: it cannot be changed or deleted. */
  readonly locked: boolean;
  readonly origin: ShelfOrigin | null;
  /** An approved one still linked to its public scenario: unchanged since, or changed. */
  readonly published: 'current' | 'changed' | null;
}

/** A public scenario the player may copy (ScenarioShelf.publicOnes). */
export interface PublicScenario extends ShelfScenario {
  readonly author: string | null;
  readonly disabled: boolean;
}

/** The draft the player has open in the authoring tool (ScenarioAuthoring.current). */
export interface AuthoringDraft {
  readonly gameId: string;
  readonly id: string | null;
  readonly title: string | null;
  /** Its game is over. */
  readonly over: boolean;
  /** Being played as a player would. */
  readonly test: boolean;
  /** It has changes that are not saved. */
  readonly dirty: boolean;
}

/** GET /scenarios/mine. */
export interface MyScenariosAnswer {
  readonly scenarios: readonly OwnScenario[];
  readonly limit: number;
  /** The server has a folder for players' own scenarios (scenarios.user.path). */
  readonly ownFolder: boolean;
  readonly authoring: AuthoringDraft | null;
}

/** What POST mine/copy and mine/copyPublic answer: the copy's id and file. */
export interface ScenarioCopy {
  readonly id: string | null;
  readonly path: string | null;
}
