/**
 * The game as one page holds it: what the server has told it (events of game-events.ts, applied by apply-event.ts)
 * and what the page's player has chosen but not sent (`local`, kept across reloads by GameRestore). Plain immutable
 * data: each event gives a new model, so any state can be checked without a page.
 */
import { Clocks, Decision, GameStats } from '../protocol/game-events';

export interface GameCard {
  id: string;
  blueprintId: string;
  zone: string;
  /** The participant the page shows it under: its controller if the server named one, else its owner. */
  owner: string | null;
  hindered: boolean;
  /** For a site on the adventure path, its number. */
  index: number | null;
  /** The card it is attached to or stacked on. */
  attachedTo: string | null;
  stackedOn: string | null;
  /** When it was put where it is attached or stacked (the number of events before): cards under one card lie in this order. */
  attachedAt: number;
  tokens: Readonly<Record<string, number>>;
  /** Not active this turn (the turn's TC event). */
  inactive: boolean;
  /** For a minion: the character it is assigned to. */
  assignedTo: string | null;
  inSkirmish: boolean;
  testingText: string | null;
  backSideTestingText: string | null;
}

/** A decision waiting for the page's player, and when it came (decision ids are reused). */
export interface PendingDecision {
  decision: Decision;
  /** The number of events the page had applied when it came. */
  arrivedAt: number;
}

export interface GameMessage {
  kind: 'message' | 'warning';
  text: string;
}

export interface PreGameInfo {
  /** The participant the page is, and the players. */
  participantId: string | null;
  players: readonly string[];
  summary: string;
  notes: string;
  maps: string;
  metaSites: string;
}

/** What the page's player has chosen but not sent yet (D10: kept across reloads). */
export interface LocalChoices {
  /** Minion id → character id, for the waiting assignment decision (sent only with Done). */
  assignments: Readonly<Record<string, string>>;
  /** Cards chosen for the waiting decision without auto-accept. */
  selectedCardIds: readonly string[];
}

export interface GameModel {
  /** The participant the page plays for (or the one whose side a spectator sees). */
  participantId: string | null;
  spectator: boolean;
  players: readonly string[];
  discardPublic: boolean;
  /** False until the participants are known (the page's board appears then). */
  started: boolean;
  preGame: PreGameInfo | null;
  phase: string | null;
  twilight: number;
  currentPlayer: string | null;
  /** Each player's site on the adventure path. */
  positions: Readonly<Record<string, number>>;
  /** Cards by id, and the order the server put them on the page in. */
  cards: Readonly<Record<string, GameCard>>;
  cardOrder: readonly string[];
  /** The current skirmish's cards, or null outside one. */
  skirmish: { characterId: string | null; minionIds: readonly string[] } | null;
  stats: GameStats | null;
  /** The last Rule of 4 count the server sent while the rule applied (the page keeps showing it), or null. */
  lastRuleOf4: string | null;
  /** Players whose hand the page may see. */
  revealedHands: readonly string[];
  messages: readonly GameMessage[];
  /** The decision waiting for the page's player that it hasn't answered, or null. */
  decision: PendingDecision | null;
  local: LocalChoices;
  clocks: Clocks;
  ended: boolean;
  /** How many events have been applied. */
  eventCount: number;
}

export const NO_CHOICES: LocalChoices = { assignments: {}, selectedCardIds: [] };

export function emptyGame(): GameModel {
  return {
    participantId: null,
    spectator: false,
    players: [],
    discardPublic: false,
    started: false,
    preGame: null,
    phase: null,
    twilight: 0,
    currentPlayer: null,
    positions: {},
    cards: {},
    cardOrder: [],
    skirmish: null,
    stats: null,
    lastRuleOf4: null,
    revealedHands: [],
    messages: [],
    decision: null,
    local: NO_CHOICES,
    clocks: {},
    ended: false,
    eventCount: 0,
  };
}
