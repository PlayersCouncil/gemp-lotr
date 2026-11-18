/**
 * The game as one page holds it: what the server has told it (events of game-events.ts, applied by apply-event.ts)
 * and what the page's player has chosen but not sent (`local`, kept across reloads by GameRestore). Plain immutable
 * data: each event gives a new model, so any state can be checked without a page.
 */
import { Clocks, Decision, DecisionType, GameStats } from '../protocol/game-events';

/** The decisions asked in the alert box, which frame it. */
export const ALERT_DECISIONS: readonly string[] = ['CARD_ACTION_CHOICE', 'CARD_SELECTION', 'ASSIGN_MINIONS'];

export interface GameCard {
  id: string;
  blueprintId: string;
  zone: string;
  /** The participant the page shows it under: its controller if the server named one, else its owner. */
  owner: string | null;
  hindered: boolean;
  /**
   * Its numbers are hidden: it was turned face down, and no stats named it since it was turned face up again (the
   * legacy page hid them on the flip and showed them again only on stats naming the card).
   */
  numbersHidden: boolean;
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
  /**
   * What is chosen so far for the waiting decision, in order: card ids, or action ids for a choice of actions (the
   * decision's answer when it is sent).
   */
  selectedCardIds: readonly string[];
  /** The cards drawn as chosen (for a choice of actions: the card whose action was taken). */
  markedCardIds: readonly string[];
  /** While assigning: the minion clicked, waiting for the character it goes to (or a second click to take it back). */
  minion: string | null;
}

/**
 * The current skirmish. Its boxes show the numbers of the stats sent since it started (the legacy page made them empty
 * when the skirmish began and wrote them on each GS after that): null before any; a damage bonus or "overwhelmed" the
 * stats left out stays as it was.
 */
export interface Skirmish {
  characterId: string | null;
  minionIds: readonly string[];
  shown: SkirmishNumbers;
}

export interface SkirmishNumbers {
  fellowshipStrength: string | null;
  shadowStrength: string | null;
  fellowshipDamageBonus: string | null;
  shadowDamageBonus: string | null;
  fpOverwhelmed: string | null;
  /** The Shadow side about to be overwhelmed (new master, 2026-11-17: its total turns red too). */
  shadowOverwhelmed: string | null;
}

export const NO_SKIRMISH_NUMBERS: SkirmishNumbers = {
  fellowshipStrength: null,
  shadowStrength: null,
  fellowshipDamageBonus: null,
  shadowDamageBonus: null,
  fpOverwhelmed: null,
  shadowOverwhelmed: null,
};

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
  /** The cards not active this turn (the turn's TC event; the full state names them before putting them in play). */
  inactiveCardIds: readonly string[];
  /** Each player's site on the adventure path. */
  positions: Readonly<Record<string, number>>;
  /** Cards by id, and the order the server put them on the page in. */
  cards: Readonly<Record<string, GameCard>>;
  cardOrder: readonly string[];
  /** The current skirmish's cards, or null outside one. */
  skirmish: Skirmish | null;
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
  /** The kind of decision that was still waiting when the game ended (it could no longer be answered), or null. */
  closedByEnd: DecisionType | null;
  /**
   * Whether the alert box has asked something (ALERT_DECISIONS). In a replay nothing is answered, and the legacy page
   * left the box's frame 2px wide after that.
   */
  alertAsked: boolean;
  /** How many events have been applied. */
  eventCount: number;
}

export const NO_CHOICES: LocalChoices = { assignments: {}, selectedCardIds: [], markedCardIds: [], minion: null };

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
    inactiveCardIds: [],
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
    closedByEnd: null,
    alertAsked: false,
    eventCount: 0,
  };
}
