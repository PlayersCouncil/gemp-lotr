/**
 * What the game server sends a game page (GET and POST /game/{id}, GET /replay/{id}), typed. The server writes each
 * event as a `<ge type="…">` element (EventSerializer); game-xml.ts reads them into these.
 */

/** A card as an event names it: put into play, moved, flashed, shown, given tokens. */
export interface CardRef {
  cardId: string;
  blueprintId: string;
  /** The participant the card belongs to (its owner; the controller when the server sends one, see ownerOf). */
  participantId: string | null;
  controllerId: string | null;
  zone: string | null;
  hindered: boolean;
  /** For sites on the adventure path: the site's number. */
  index: number | null;
  /** The card it is attached to or stacked on, and how. */
  targetCardId: string | null;
  targetType: 'attached' | 'stacked' | null;
  testingText: string | null;
  backSideTestingText: string | null;
}

export type DecisionType =
  | 'INTEGER'
  | 'MULTIPLE_CHOICE'
  | 'ARBITRARY_CARDS'
  | 'CARD_ACTION_CHOICE'
  | 'ACTION_CHOICE'
  | 'CARD_SELECTION'
  | 'ASSIGN_MINIONS';

/** A decision waiting for a participant. Its parameters by name, each a list in the server's order. */
export interface Decision {
  /** The server's id for it; reused (often "1"), so a decision is told apart by when it came. */
  id: string;
  participantId: string;
  type: DecisionType;
  text: string;
  params: Readonly<Record<string, readonly string[]>>;
}

/** A player's counters as game stats give them. */
export interface PlayerZones {
  hand: number | null;
  discard: number | null;
  adventureDeck: number | null;
  dead: number | null;
  deck: number | null;
  removed: number | null;
  handRevealed: boolean;
}

export interface GameStats {
  /** Per card id: strength, vitality and the rest of "S|V|…" as sent. */
  charStats: Readonly<Record<string, readonly string[]>>;
  fellowshipArchery: string | null;
  shadowArchery: string | null;
  moveCount: string | null;
  moveLimit: string | null;
  initiative: string | null;
  ruleOf4: string | null;
  fellowshipStrength: string | null;
  shadowStrength: string | null;
  fellowshipDamageBonus: string | null;
  shadowDamageBonus: string | null;
  fpOverwhelmed: string | null;
  /** Whether the Ring-bearer wears The One Ring. */
  wearingRing: boolean;
  playerZones: Readonly<Record<string, PlayerZones>>;
  /** Per player: threats, and the limit (-1 when there is none). */
  threats: Readonly<Record<string, { value: string; total: string }>>;
}

/** Every event, by the server's type code. */
export type GameEvent =
  | { type: 'P'; participantId: string; allParticipantIds: readonly string[]; discardPublic: boolean }
  | {
      type: 'PGS';
      participantId: string | null;
      allParticipantIds: readonly string[];
      summary: string;
      notes: string;
      maps: string;
      metaSites: string;
    }
  | { type: 'GPC'; phase: string }
  | { type: 'TP'; count: number }
  | { type: 'TC'; participantId: string; inactiveCardIds: readonly string[] }
  | { type: 'PP'; participantId: string; index: number }
  | ({ type: 'PCIP' } & CardRef)
  | ({ type: 'MCIP' } & CardRef)
  | { type: 'FCIP'; cardIds: readonly string[]; hindered: boolean }
  | { type: 'RCFP'; cardIds: readonly string[]; participantId: string | null }
  | { type: 'AA'; cardId: string; opposingCardIds: readonly string[] }
  | { type: 'RA'; cardId: string; opposingCardIds: readonly string[] }
  | { type: 'SS'; cardId: string | null; opposingCardIds: readonly string[] }
  | { type: 'ATS'; cardId: string }
  | { type: 'RFS'; cardId: string }
  | { type: 'ES' }
  | { type: 'AT'; cardId: string; token: string; count: number }
  | { type: 'RT'; cardId: string; token: string; count: number }
  | { type: 'M'; message: string }
  | { type: 'W'; message: string }
  | { type: 'GS'; stats: GameStats }
  | { type: 'EG' }
  | ({ type: 'CAC'; affectedCardIds: readonly string[] } & CardRef)
  | ({ type: 'EP' } & CardRef)
  | ({ type: 'CA' } & CardRef)
  | { type: 'D'; decision: Decision };

/** Clock values: per participant the seconds left in their bank; also the decision's clock and limit. */
export type Clocks = Readonly<Record<string, number>>;

/** One answer of /game: the channel number to send back, the events in order, the clocks. */
export interface GameUpdate {
  channel: string | null;
  events: readonly GameEvent[];
  clocks: Clocks;
}

/** The participant a card's events put it under on the page: its controller if any, else the participant. */
export function ownerOf(card: Pick<CardRef, 'participantId' | 'controllerId'>): string | null {
  return card.controllerId ?? card.participantId;
}
