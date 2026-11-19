/**
 * Changes to which steps the script has, as `edit` takes them (its `steps` ops: ScenarioAuthoring.edit), and the steps
 * the panel's buttons put in. A step is named by its uid; `before: null` is the end. Pure: the Authoring service sends
 * them (changeSteps, changeBlock), and the Step tab will build its own the same way.
 */
import { DraftState, DraftStep, UncoveredDecision } from './authoring-state';
import { isOpening, stepsOf } from './steps';

export type StepOp =
  /** Changes these parts of the step, keeps the rest. */
  | { op: 'merge'; uid: string; set?: Readonly<Record<string, unknown>>; unset?: readonly string[] }
  /** Replaces the step whole (it keeps its name, and its steps' names). */
  | { op: 'set'; uid: string; step: DraftStep }
  /**
   * Before the step named `before` (in the script or a block; null: at the end), else last in the block `into`, else
   * at `index`. `awaiting`: named now, and takes its answer from what is done at it.
   */
  | { op: 'insert'; step: DraftStep; before?: string | null; into?: string; index?: number; awaiting?: true }
  | { op: 'delete'; uid: string }
  /** Out, and back in where `before`, `into` or `to` says (as insert). */
  | { op: 'move'; uid: string; before?: string | null; into?: string; to?: number };

/** A step put in before the step now at `index` (from 0; past the last: at the end). */
export function insertOp(state: DraftState, index: number, step: DraftStep, awaiting = false): StepOp {
  return {
    op: 'insert',
    before: state.uids[index] ?? null,
    step,
    ...(awaiting ? { awaiting: true as const } : {}),
  };
}

/** What a new message says until it is written. */
export const NEW_TEXT = '...';

/** The opening: read before the first turn begins, on the board as the scenario lays it out. */
export function openingStep(): DraftStep {
  return { when: 'opening', messages: [{ text: NEW_TEXT }] };
}

/** A step that is only read. */
export function pauseStep(): DraftStep {
  return { pause: true, messages: [{ text: NEW_TEXT }] };
}

/** A step that says nothing yet: what is done when the game comes to it is written in (insert it `awaiting`). */
export function blankStep(state: DraftState): DraftStep {
  return { when: `${state.draft.human ?? 'freeps'}, action` };
}

/**
 * A block (or the skirmishes of a turn: a block shown skirmish by skirmish): from this turn (when the game has one)
 * until the regroup phase, with no steps yet.
 */
export function blockStep(turn: number, skirmishes: boolean): DraftStep {
  return {
    ...(skirmishes ? { skirmishes: true } : {}),
    until: 'phase(regroup)',
    anyOrder: [],
    ...(turn > 0 ? { when: `turn(${turn})` } : {}),
  };
}

/** Whether the script can be given an opening (its first step isn't one). */
export function mayAddOpening(state: DraftState): boolean {
  return !isOpening(stepsOf(state)[0]);
}

/** Where a step put in before the selected one goes (none selected: at the end), from 0. */
export function beforeSelected(state: DraftState, selected: number | null): number {
  return selected === null ? stepsOf(state).length : selected - 1;
}

/** Where a step put in after the selected one goes (none selected: at the end), from 0. */
export function afterSelected(state: DraftState, selected: number | null): number {
  return selected ?? stepsOf(state).length;
}

/**
 * Whether a step put in at `index` would be exactly the one for the decision that is waiting with no step to cover it:
 * then the server writes it ("here"), for that decision.
 */
export function coversUncovered(state: DraftState, index: number): boolean {
  const open: UncoveredDecision | null = state.uncovered;
  return (
    open !== null &&
    open.problem === null &&
    open.reuses === null &&
    open.block === null &&
    index === open.after &&
    !state.position.over
  );
}

/** "Rewrite from here": every step from the one the game is at (from 1) to the last taken out, the last first. */
export function rewriteOps(state: DraftState, from: number): StepOp[] {
  const ops: StepOp[] = [];
  for (let i = stepsOf(state).length - 1; i >= from - 1; i--) {
    const uid = state.uids[i];
    if (uid !== undefined) ops.push({ op: 'delete', uid });
  }
  return ops;
}

/** Whether "Rewrite from here" has anything to take out. */
export function mayRewrite(state: DraftState): boolean {
  return state.position.step <= stepsOf(state).length && !state.position.over;
}

/**
 * The fresh game a change to which steps there are needs: one at or before where the game is (`firstChanged`, from
 * 1) is played to again, at the step changed; one after it the game in hand can take (null).
 */
export function stepsGoto(firstChanged: number, gameStep: number): number | null {
  return firstChanged <= gameStep ? Math.max(1, firstChanged) : null;
}

/**
 * The fresh game a change to a block's steps needs: in a block the game has gone through, one at the block; in the
 * block the game is in (or one still to come), none: the game carries on if it can.
 */
export function blockGoto(index: number, gameStep: number): number | null {
  return index + 1 < gameStep ? index + 1 : null;
}
