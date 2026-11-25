/**
 * What the server says about taking a game back (GameRequestHandler, "taking a game back"; LotroGameMediator
 * requestUndo), typed. The answers are JSON; each is read defensively, so an answer the page doesn't understand reads as
 * "nothing can be taken back" rather than breaking the game.
 */
import { listOf } from '../../api/json';

/** How much going back to a place undoes: only the player's own choice, other players' choices, or something seen. */
export type UndoTier = 'GREEN' | 'YELLOW' | 'RED';

export const UNDO_TIERS: readonly UndoTier[] = ['GREEN', 'YELLOW', 'RED'];

/** A place the game can be taken back to: before the choice `label` names ("Played Gimli"). */
export interface RestorePoint {
  entryCount: number;
  label: string;
  turn: number;
  phase: string;
  tier: UndoTier;
  /** What makes it yellow or red, one line each. */
  reasons: readonly string[];
  /** Why it can't be asked for now (the opponent declines every request, say), or null. */
  unavailable: string | null;
}

/** Which tiers the game allows, and whether anybody is asked before a yellow or red one (not against a bot). */
export interface UndoPolicy {
  green: boolean;
  yellow: boolean;
  red: boolean;
  asks: boolean;
  timeoutSeconds: number;
}

/** A request waiting for an answer: who asked, for which place, and how long is left. */
export interface PendingUndo {
  requestId: number;
  requester: string;
  label: string;
  turn: number;
  phase: string;
  tier: UndoTier;
  reasons: readonly string[];
  secondsLeft: number;
}

/** GET /game/{id}/restorePoints: the places, latest first, and how things stand with requests. */
export interface RestorePoints {
  points: readonly RestorePoint[];
  policy: UndoPolicy;
  /** Whether this player declines every request without being asked (for this game). */
  autoDecline: boolean;
  pending: PendingUndo | null;
}

/** How a request is answered: by the player asked (approve, decline, declineAll: this and every later one), or withdrawn by the asker (cancel). */
export type UndoAnswer = 'approve' | 'decline' | 'declineAll' | 'cancel';

/** The answer to asking for an undo or replying to a request. */
export interface UndoReply {
  status: 'done' | 'pending' | 'refused';
  message: string | null;
}

/** A notice of the game's notices poll: its number, its type and its fields (all text, as the server sends them). */
export interface GameNotice {
  number: number;
  type: string;
  fields: Readonly<Record<string, string>>;
}

/** POST /game/{id}/notices: the notices after the one asked from, and the latest number to go on from. */
export interface GameNotices {
  notices: readonly GameNotice[];
  latest: number | null;
}

export const NO_POLICY: UndoPolicy = { green: false, yellow: false, red: false, asks: false, timeoutSeconds: 0 };

type Json = Record<string, unknown>;

const object = (value: unknown): Json => (value !== null && typeof value === 'object' ? (value as Json) : {});
/** A string as it is, a number or a boolean written out; anything else (null, an object) is no text. */
const text = (value: unknown): string =>
  typeof value === 'string' ? value : typeof value === 'number' || typeof value === 'boolean' ? String(value) : '';
const number = (value: unknown): number => {
  const parsed = typeof value === 'number' ? value : parseInt(text(value), 10);
  return Number.isFinite(parsed) ? parsed : 0;
};
const tier = (value: unknown): UndoTier => UNDO_TIERS.find((known) => known === value) ?? 'RED';
const strings = (value: unknown): string[] => listOf(value).map(text);

function point(value: unknown): RestorePoint {
  const json = object(value);
  return {
    entryCount: number(json['entryCount']),
    label: text(json['label']),
    turn: number(json['turn']),
    phase: text(json['phase']),
    tier: tier(json['tier']),
    reasons: strings(json['reasons']),
    unavailable: json['unavailable'] == null ? null : text(json['unavailable']),
  };
}

function pending(value: unknown): PendingUndo | null {
  if (value === null || typeof value !== 'object') return null;
  const json = object(value);
  return {
    requestId: number(json['requestId']),
    requester: text(json['requester']),
    label: text(json['label']),
    turn: number(json['turn']),
    phase: text(json['phase']),
    tier: tier(json['tier']),
    reasons: strings(json['reasons']),
    secondsLeft: number(json['secondsLeft']),
  };
}

export function readRestorePoints(value: unknown): RestorePoints {
  const json = object(value);
  const policy = object(json['policy']);
  return {
    points: listOf(json['points']).map(point),
    policy: {
      green: policy['green'] === true,
      yellow: policy['yellow'] === true,
      red: policy['red'] === true,
      asks: policy['asks'] === true,
      timeoutSeconds: number(policy['timeoutSeconds']),
    },
    autoDecline: json['autoDecline'] === true,
    pending: pending(json['pending']),
  };
}

export function readUndoReply(value: unknown): UndoReply {
  const json = object(value);
  const status = json['status'];
  return {
    status: status === 'done' || status === 'pending' ? status : 'refused',
    message: json['message'] == null ? null : text(json['message']),
  };
}

export function readGameNotices(value: unknown): GameNotices {
  const json = object(value);
  const notices = listOf(json['notices']);
  return {
    notices: notices.map((entry) => {
      const fields = object(entry);
      return {
        number: number(fields['number']),
        type: text(fields['type']),
        fields: Object.fromEntries(Object.entries(fields).map(([name, field]) => [name, text(field)])),
      };
    }),
    latest: json['latest'] == null ? null : number(json['latest']),
  };
}

/** The request an `undoRequested` notice announces (its reasons come joined by "; "). */
export function pendingOf(notice: GameNotice): PendingUndo {
  const fields = notice.fields;
  return {
    requestId: number(fields['requestId']),
    requester: text(fields['requester']),
    label: text(fields['label']),
    turn: number(fields['turn']),
    phase: text(fields['phase']),
    tier: tier(fields['tier']),
    reasons: fields['reasons'] ? fields['reasons'].split('; ') : [],
    secondsLeft: number(fields['seconds']),
  };
}
