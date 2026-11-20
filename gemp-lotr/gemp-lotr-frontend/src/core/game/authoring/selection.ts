/**
 * Which step the panel has selected (the Step tab's): the author's to say, until the game comes to another step; then
 * the step the game is at is selected, so that the Step tab is about what is on the table unless it has been turned
 * to something else since the table last changed. Pure.
 */
import { DraftState } from './authoring-state';
import { kidsOf, locate, stepsOf } from './steps';

export interface Selection {
  /** The step selected (from 1), and its name; in a block, the name of the block's step selected. */
  selected: number | null;
  selectedUid: string | null;
  sub: string | null;
  /** Which of a block's steps (from 0) to select once the change that asked for it is back. */
  subAt: number | null;
  /** "Follow the game". */
  follow: boolean;
  /** Where the game was last seen: the step's name, and "/" and the block's step's name. */
  atUid: string | null;
  /** The step selected was asked for by a change, and stays across it. */
  hold: boolean;
  /** The step the game is at is to be selected, whatever. */
  jump: boolean;
}

/** The step the game is at: the one in hand, or one that repeats and is between its decisions, or the last played. */
export function currentOf(state: DraftState): { current: number | null; sub: string | null; here: string | null } {
  const steps = stepsOf(state);
  const at = state.position.step;
  if (steps.length === 0) return { current: null, sub: null, here: null };
  const current = Math.max(1, Math.min(state.position.inHand || state.position.underWay ? at : at - 1, steps.length));
  const uid = state.uids[current - 1] ?? null;
  const sub =
    current === at && state.position.inHand && state.position.sub > 0
      ? (kidsOf(state, current - 1)[state.position.sub - 1] ?? null)
      : null;
  return { current, sub, here: uid === null ? null : uid + (sub === null ? '' : `/${sub}`) };
}

/**
 * The selection for a new state. `engaged`: the author is in the middle of something in the Step tab (typing,
 * picking) that turning to another step would lose; the game's step then waits (`due`) until they are done.
 */
export function settle(state: DraftState, was: Selection, engaged: boolean): { selection: Selection; due: boolean } {
  const steps = stepsOf(state);
  const next: Selection = { ...was };
  // The step selected is known by its name: where it comes changes when the game writes a step in before it. In a
  // block, its step selected is known by its name too
  const found = locate(state, next.sub);
  if (found) {
    next.selected = found.index + 1;
    next.selectedUid = state.uids[found.index] ?? null;
    // (taken out of its block, it is a step of the script)
    if (found.sub < 0) next.sub = null;
  } else {
    next.sub = null;
    const at = next.selectedUid === null ? -1 : state.uids.indexOf(next.selectedUid);
    if (at > -1) next.selected = at + 1;
  }
  const { current, sub, here } = currentOf(state);
  const lost = next.selected === null || next.selected > steps.length;
  let due = false;
  if (next.hold && !lost) {
    // (asked for by the change that moved the game; and in a block, by where in it the step asked for comes)
    next.hold = false;
    if (next.subAt !== null && next.selected !== null) {
      next.sub = kidsOf(state, next.selected - 1)[next.subAt] ?? null;
    }
  } else if (lost || next.jump || (next.follow && here !== next.atUid)) {
    if (!lost && !next.jump && engaged) {
      due = true;
    } else {
      next.selected = current;
      next.sub = sub;
    }
  }
  next.subAt = null;
  if (!due) {
    next.atUid = here;
    next.jump = false;
  }
  next.selectedUid = next.selected === null ? null : (state.uids[next.selected - 1] ?? null);
  return { selection: next, due };
}
