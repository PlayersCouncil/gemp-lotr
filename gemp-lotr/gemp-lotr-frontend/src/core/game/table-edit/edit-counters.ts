/**
 * The game's counters changed by hand (the `counters` tool): the twilight pool, the burdens and the threats, each set
 * to a number (`set`, 0 to 99) or changed by one (`amount`). Pure.
 */
import { EditBoard } from './edit-board';
import { EditOp } from './edit-ops';

export type EditCounter = 'twilight' | 'burdens' | 'threats';

export const EDIT_COUNTERS: readonly EditCounter[] = ['twilight', 'burdens', 'threats'];

/** What each counter is called: a word. */
export const COUNTER_LABELS: Readonly<Record<EditCounter, string>> = {
  twilight: 'Twilight',
  burdens: 'Burdens',
  threats: 'Threats',
};

/** The largest number a counter is set to (the server's limit). */
export const COUNTER_MAX = 99;

/** A counter as the board has it (0 before the board is read). */
export function counterValue(board: EditBoard | null, counter: EditCounter): number {
  return board?.[counter] ?? 0;
}

/** Sets the counter; null for text that is not a whole number from 0 to 99. */
export function setCounter(counter: EditCounter, text: string): EditOp | null {
  const trimmed = text.trim();
  if (!/^\d+$/.test(trimmed)) return null;
  const value = Number(trimmed);
  return value <= COUNTER_MAX ? { op: counter, set: value } : null;
}

/** Changes the counter by one up or down (a counter at 0 is not lowered). */
export function stepCounter(counter: EditCounter, by: 1 | -1, now: number): EditOp | null {
  return by < 0 && now <= 0 ? null : { op: counter, amount: by };
}
