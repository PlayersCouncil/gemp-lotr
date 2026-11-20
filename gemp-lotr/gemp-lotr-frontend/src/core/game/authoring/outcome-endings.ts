/**
 * The Scenario tab's endings (outcome-section): what is said, and what it counts as, when the scenario ends other
 * than by a step's `end` — `outcome.winMessages` / `loseMessages` when `win` / `lose` comes true, and
 * `outcome.gameWon` / `gameLost` when the game ends by itself (the person having won or lost it): its result
 * (complete or fail) and its closing messages. The outcome is changed only whole (`top: {outcome}`), so each change
 * here is the outcome as the draft has it with that one part different: `win`, `lose`, `loseMessage`, `offScript`
 * and anything else in it are copied as they are. Pure.
 */
import { Draft, DraftEnd, DraftMessage, DraftMessageValue } from './authoring-state';

type Outcome = NonNullable<Draft['outcome']>;
type Result = DraftEnd['result'];

/** The endings, as the outcome names them. */
export type EndingKind = 'winMessages' | 'loseMessages' | 'gameWon' | 'gameLost';

export interface Ending {
  kind: EndingKind;
  /** What the section calls it. */
  label: string;
  hint: string;
  /** What it counts as when the file says nothing; null for one that has no result of its own. */
  stock: Result | null;
}

export const ENDINGS: readonly Ending[] = [
  {
    kind: 'winMessages',
    label: 'Win comes true',
    hint: "Said when the scenario's win condition (outcome.win) comes true",
    stock: null,
  },
  {
    kind: 'loseMessages',
    label: 'Lose comes true',
    hint: "Said when the scenario's lose condition (outcome.lose) comes true, before what they are told when they lose",
    stock: null,
  },
  {
    kind: 'gameWon',
    label: 'Game won',
    hint: 'The game ends by itself and the player has won it: the other Ring-bearer killed or corrupted, or their fellowship survives its last site',
    stock: 'complete',
  },
  {
    kind: 'gameLost',
    label: 'Game lost',
    hint: "The game ends by itself and the player has lost it: the player's Ring-bearer killed or corrupted, say",
    stock: 'fail',
  },
];

/** A message as an object (one written as its text alone is that text). */
export function messageObject(value: DraftMessageValue): DraftMessage {
  return typeof value === 'string' ? { text: value } : value;
}

/** A message as the file writes it: its text alone when that is all there is. */
export function messageValue(message: DraftMessage): DraftMessageValue {
  const parts = Object.keys(message).filter((part) => message[part as keyof DraftMessage] !== undefined);
  return parts.length === 1 && parts[0] === 'text' ? message.text : message;
}

const listOf = (value: readonly DraftMessageValue[] | DraftMessageValue | undefined): DraftMessage[] =>
  value === undefined ? [] : (Array.isArray(value) ? value : [value as DraftMessageValue]).map(messageObject);

/** The closing messages of an ending, as objects. */
export function endingMessages(outcome: Outcome | undefined, kind: EndingKind): DraftMessage[] {
  const value = outcome?.[kind];
  if (kind === 'winMessages' || kind === 'loseMessages') {
    return listOf(value as readonly DraftMessageValue[] | DraftMessageValue | undefined);
  }
  return value !== undefined && typeof value === 'object' ? listOf((value as DraftEnd).messages) : [];
}

/** What an ending counts as: the file's result, or the stock one (null for one that has none). */
export function endingResult(outcome: Outcome | undefined, kind: EndingKind): Result | null {
  const stock = ENDINGS.find((ending) => ending.kind === kind)?.stock ?? null;
  if (stock === null) return null;
  const value = outcome?.[kind] as Result | DraftEnd | undefined;
  if (value === undefined) return stock;
  return typeof value === 'string' ? value : ((value as Partial<DraftEnd>).result ?? 'complete');
}

/**
 * The outcome with one ending changed, every other part of it as the draft has it; null when nothing is left in it.
 * A game ending with the stock result and nothing to say is taken out; one with only a result is written as it
 * (`gameLost: complete`), else `{ result, messages }` (the result always written: without one the server reads
 * complete, which is not what a lost game is by default).
 */
export function outcomeWithEnding(
  draft: Draft,
  kind: EndingKind,
  change: { result?: Result; messages?: readonly DraftMessage[] },
): Outcome | null {
  const outcome: Record<string, unknown> = { ...draft.outcome };
  const messages = (change.messages ?? endingMessages(draft.outcome, kind)).map(messageValue);
  const stock = ENDINGS.find((ending) => ending.kind === kind)?.stock ?? null;
  if (stock === null) {
    if (messages.length === 0) Reflect.deleteProperty(outcome, kind);
    else outcome[kind] = messages;
  } else {
    const result = change.result ?? endingResult(draft.outcome, kind) ?? stock;
    if (messages.length > 0) outcome[kind] = { result, messages };
    else if (result !== stock) outcome[kind] = result;
    else Reflect.deleteProperty(outcome, kind);
  }
  return Object.keys(outcome).length === 0 ? null : outcome;
}
