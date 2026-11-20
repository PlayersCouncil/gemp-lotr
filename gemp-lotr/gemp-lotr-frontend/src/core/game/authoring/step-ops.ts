/**
 * Changes to which steps the script has, as `edit` takes them (its `steps` ops: ScenarioAuthoring.edit), and the steps
 * the panel's buttons put in. A step is named by its uid; `before: null` is the end. Pure: the Authoring service sends
 * them (changeSteps, changeBlock), and the Step tab will build its own the same way.
 */
import { DraftState, DraftStep, UncoveredDecision } from './authoring-state';
import { Diff, without, working, Written, written } from './step-model';
import { isBlock, isOpening, kidsOf, stepsOf } from './steps';
import { nextUntil, splitTerms, untilFromNext, writtenInFull } from './terms';

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
  // (a game that plays its own opening has none: its first decision is the bid)
  const realOpening = (state.start?.opening ?? state.draft.setup?.opening) === 'game';
  return !realOpening && !isOpening(stepsOf(state)[0]);
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

/* ---------------------------------------------------------------- the Step tab's */

const filled = (value: unknown) =>
  value !== undefined && value !== null && (typeof value !== 'string' || value.trim() !== '');
const allowOf = (step: DraftStep) =>
  step.allow === undefined ? '' : typeof step.allow === 'string' ? step.allow : (step.allow.answers ?? '');

/** Whether a step can be joined into one before it that waits for the same (see joinOps). */
export function joinable(step: DraftStep | undefined): boolean {
  return (
    step !== undefined &&
    !isBlock(step) &&
    !step.pause &&
    !step.checkpoint &&
    step.end === undefined &&
    (step.messages === undefined || (Array.isArray(step.messages) && step.messages.length === 0)) &&
    !filled(step.until) &&
    !step.if &&
    !step.unless &&
    !step.do &&
    !step.skipWhen
  );
}

/** How many steps straight after this one (from 0) wait for exactly what it waits for, and could be joined into it. */
export function alikeAfter(state: DraftState, index: number): number {
  const steps = stepsOf(state);
  const top = steps[index];
  if (top === undefined || isBlock(top) || top.pause || typeof top.when !== 'string' || isOpening(top)) return 0;
  let count = 0;
  for (let i = index + 1; i < steps.length; i++) {
    const next = steps[i];
    if (typeof next?.when !== 'string' || next.when !== top.when || !joinable(next)) break;
    count++;
  }
  return count;
}

/**
 * Joins the `count` steps after this one (from 0) into it, as one step that repeats: what each allowed it allows (a
 * term once, without its `?`), their answers are its answers in order (only if every one of them had one: the
 * step's answer, or the one thing it allowed), and it repeats until the step after them.
 */
export function joinOps(state: DraftState, index: number, count: number): StepOp[] {
  const steps = stepsOf(state).slice(index, index + count + 1);
  const top = steps[0];
  const uid = state.uids[index];
  if (top === undefined || uid === undefined) return [];
  const allowed: string[] = [];
  const answers: string[] = [];
  let answered = 0;
  for (const step of steps) {
    for (const term of splitTerms(allowOf(step))) {
      const plain = term.replace(/\?$/, '');
      if (!allowed.includes(plain)) allowed.push(plain);
    }
    // (what the script plays: the step's answers, or what it allows if that is one thing)
    let mine = splitTerms(step.answer);
    if (mine.length === 0 && splitTerms(allowOf(step)).length === 1) mine = splitTerms(allowOf(step));
    if (mine.length > 0) answered++;
    answers.push(...mine);
  }
  const set: Record<string, unknown> = {
    until: untilFromNext(stepsOf(state), index + count) ?? nextUntil(typeof top.when === 'string' ? top.when : ''),
  };
  if (top.allow !== undefined && typeof top.allow === 'object')
    set['allow'] = { ...top.allow, answers: allowed.join(', ') };
  else if (allowed.length > 0) set['allow'] = allowed.join(', ');
  // (answers for some of the decisions and not others would not line up: then the script plays what is allowed)
  const all = answered === steps.length && answers.length > 0;
  if (all) set['answer'] = answers.join(', ');
  const ops: StepOp[] = [{ op: 'merge', uid, set, unset: all ? [] : ['answer'] }];
  for (let k = 1; k <= count; k++) {
    const gone = state.uids[index + k];
    if (gone !== undefined) ops.push({ op: 'delete', uid: gone });
  }
  return ops;
}

/**
 * Whether what a step waits for, or ends at, is in the assignment or the skirmishes: phase(assignment),
 * phase(skirmish), an assign decision, fighting(...), inSkirmish(...), after(...), text(... skirmish ...), skirmish(n).
 */
export function skirmishy(step: DraftStep | undefined): boolean {
  if (step === undefined) return false;
  for (const value of [step.when, step.until]) {
    if (value === undefined) continue;
    const text = typeof value === 'string' ? value : JSON.stringify(value);
    if (
      /phase\(\s*(assignment|skirmish)\s*\)|fighting\(|inSkirmish\(|\bafter\(|text\([^)]*skirmish|skirmish\(\s*\d+\s*\)/i.test(
        text,
      )
    )
      return true;
    if (typeof value === 'string' && splitTerms(value).some((term) => term.toLowerCase() === 'assign')) return true;
    // (the choice of the next skirmish, said as what it is for)
    if (/\bnextSkirmish\b/i.test(text) && !/not\(\s*nextSkirmish\s*\)/i.test(text)) return true;
    if (typeof value === 'object' && !Array.isArray(value)) {
      const kind =
        (value as Readonly<Record<string, unknown>>)['type'] ?? (value as Readonly<Record<string, unknown>>)['kind'];
      if (typeof kind === 'string' && /^assign$/i.test(kind)) return true;
    }
  }
  return false;
}

/** Whether the step at this place (from 0), or either of its neighbours, is in the assignment or the skirmishes. */
export function nearSkirmishes(state: DraftState, index: number): boolean {
  const steps = stepsOf(state);
  return skirmishy(steps[index - 1]) || skirmishy(steps[index]) || skirmishy(steps[index + 1]);
}

/**
 * Turns a step of the script (from 0) into a block with the step as its first step. The block waits as the step did
 * as far as the turn (and, for a block that is not a turn's skirmishes, the phase) goes, and the step keeps the rest
 * of what it waits for; it ends where the step said it repeats until, or where the step after begins (or the phase
 * after, or the regroup); what is said of a step as a whole of the script (skip when, checkpoint, if, unless) is said
 * of the block; the step now applies once in the block (unless it is a pause, or repeated).
 */
export function convertOp(state: DraftState, index: number, skirmishes: boolean): StepOp | null {
  const original = stepsOf(state)[index];
  const uid = state.uids[index];
  if (original === undefined || uid === undefined || isBlock(original)) return null;
  let step: Record<string, unknown> = { ...original };
  let turn: string | null = null;
  let phase: string | null = null;
  const kept: string[] = [];
  const text = typeof original.when === 'string' ? original.when : null;
  for (const term of splitTerms(text)) {
    if (turn === null && /^turn\(/i.test(term)) turn = term;
    else if (!skirmishes && phase === null && /^phase\(/i.test(term)) phase = term;
    else kept.push(term);
  }
  const block: Record<string, unknown> = {};
  let when = [phase, turn].filter((term) => term !== null);
  if (skirmishes && turn === null && state.position.turn > 0) when = [`turn(${state.position.turn})`];
  if (when.length > 0) block['when'] = when.join(', ');
  const repeating = filled(original.until);
  block['until'] = repeating
    ? original.until
    : skirmishes
      ? 'phase(regroup)'
      : nextOf(untilFromNext(stepsOf(state), index)) || nextUntil(text) || 'phase(regroup)';
  for (const name of ['skipWhen', 'checkpoint', 'if', 'unless']) {
    const value = step[name];
    if (value !== undefined && value !== null && value !== '' && value !== false) block[name] = value;
  }
  if (skirmishes) block['skirmishes'] = true;
  step = without(step, 'skipWhen', 'checkpoint', 'if', 'unless', 'until');
  if (text !== null) step = kept.length > 0 ? { ...step, when: kept.join(', ') } : without(step, 'when');
  if (!original.pause && !repeating) step['once'] = true;
  block['anyOrder'] = [step];
  return { op: 'set', uid, step: block };
}

const nextOf = (until: string | null) => until ?? '';

/** A step of the script (from 0) one earlier. */
export function earlierOp(state: DraftState, index: number): StepOp {
  return { op: 'move', uid: state.uids[index] ?? '', before: state.uids[index - 1] ?? null };
}

/** A step of the script (from 0) one later. */
export function laterOp(state: DraftState, index: number): StepOp {
  return { op: 'move', uid: state.uids[index] ?? '', before: state.uids[index + 2] ?? null };
}

/**
 * A step of the script (from 0) made one of a block's next to it (at `toIndex`): the last of the block before it, or
 * the first of the block after it. With where the block is once it has moved, and where in it the step is.
 */
export function intoBlockOp(
  state: DraftState,
  index: number,
  toIndex: number,
): { op: StepOp; blockAt: number; subAt: number; firstChanged: number } {
  const first = toIndex > index;
  const kids = kidsOf(state, toIndex);
  const uid = state.uids[index] ?? '';
  const op: StepOp =
    first && kids.length > 0
      ? { op: 'move', uid, before: kids[0] ?? null }
      : { op: 'move', uid, into: state.uids[toIndex] ?? '' };
  // (the block is where it was, or one earlier now that this step is not in front of it)
  const blockAt = first ? toIndex - 1 : toIndex;
  return { op, blockAt, subAt: first ? 0 : kids.length, firstChanged: Math.min(index + 1, blockAt + 1) };
}

/** A new step of a block's: it says nothing yet (insert it `awaiting`), and applies once. */
export function childStep(state: DraftState): DraftStep {
  return { once: true, when: `${state.draft.human ?? 'freeps'}, action` };
}

/** A step of a block's that is only read: once, over the first of the player's decisions that fits it. */
export function childPause(state: DraftState): DraftStep {
  return { pause: true, when: `${state.draft.human ?? 'freeps'}, action`, messages: [{ text: NEW_TEXT }] };
}

/** A step put into the block at `index` (from 0), before its step at `at` (from 0; past the last: last). */
export function childInsertOp(state: DraftState, index: number, at: number, step: DraftStep, awaiting = false): StepOp {
  const kids = kidsOf(state, index);
  const before = kids[at];
  return {
    op: 'insert',
    step,
    ...(before !== undefined ? { before } : { into: state.uids[index] ?? '' }),
    ...(awaiting ? { awaiting: true as const } : {}),
  };
}

/** A block's step moved in the block (at `index`) to before its step at `before` (from 0; past the last: last). */
export function childMoveOp(state: DraftState, index: number, uid: string, before: number): StepOp {
  const kids = kidsOf(state, index);
  const named = kids[before];
  return named !== undefined ? { op: 'move', uid, before: named } : { op: 'move', uid, into: state.uids[index] ?? '' };
}

/** A block's step (the block at `index`) taken out of it: the step of the script after the block. */
export function outOfBlockOp(state: DraftState, index: number, uid: string): StepOp {
  return { op: 'move', uid, before: state.uids[index + 1] ?? null };
}

/** What of a form is sent with a change made from it: its unsent parts but the ones named. */
function restOf(dirty: Diff, but: string): { set: Record<string, unknown>; unset: string[] } {
  const set: Record<string, unknown> = {};
  for (const [name, value] of Object.entries(dirty.set)) if (name !== but) set[name] = value;
  return { set, unset: dirty.unset.filter((name) => name !== but) };
}

/**
 * A message of a step of the script (from 0) moved to the step before or after it: going earlier it is the last that
 * step says, going later the first. `now`: the form's step as its file would have it; `dirty`: what of it is
 * unsent, which goes with it. With the place of the message in the step it went to.
 */
export function moveMessageOps(
  state: DraftState,
  uid: string,
  now: Written,
  dirty: Diff,
  position: number,
  fromIndex: number,
  toIndex: number,
): { ops: StepOp[]; at: number } | null {
  const target = stepsOf(state)[toIndex];
  const targetUid = state.uids[toIndex];
  const messages = [...((now['messages'] as readonly unknown[] | undefined) ?? [])];
  const moved = messages.splice(position, 1)[0];
  if (target === undefined || targetUid === undefined || moved === undefined) return null;
  const theirs = written(working(target, state.texts[targetUid]));
  const there = [...((theirs['messages'] as readonly unknown[] | undefined) ?? [])];
  if (toIndex < fromIndex) there.push(moved);
  else there.unshift(moved);
  const rest = restOf(dirty, 'messages');
  const from: StepOp = {
    op: 'merge',
    uid,
    set: { ...rest.set, ...(messages.length > 0 ? { messages } : {}) },
    unset: [...rest.unset, ...(messages.length > 0 ? [] : ['messages'])],
  };
  return {
    ops: [from, { op: 'merge', uid: targetUid, set: { messages: there }, unset: [] }],
    at: toIndex < fromIndex ? there.length - 1 : 0,
  };
}

/**
 * The messages of a step up to and including one (`position`) made a pause of their own, put in before the step: it
 * waits for what the step does (but what it offers or says), and is part of the game when the step is (if, unless).
 * A message that waited on the answering cannot do that in a step that is only read.
 */
export function splitToPauseOps(uid: string, now: Written, dirty: Diff, position: number): StepOp[] {
  const messages = [...((now['messages'] as readonly Readonly<Record<string, unknown>>[] | undefined) ?? [])];
  const taken = messages.splice(0, position + 1).map((message) => {
    return message['advance'] === 'decision' ? without(message, 'on', 'advance') : without(message, 'on');
  });
  const pause: Record<string, unknown> = { pause: true, messages: taken };
  if (now['if']) pause['if'] = now['if'];
  if (now['unless']) pause['unless'] = now['unless'];
  const when = now['when'];
  if (typeof when === 'string' && !writtenInFull(when)) {
    pause['when'] = splitTerms(when)
      .filter((term) => !/^(offers|text)\(/i.test(term))
      .join(', ');
  }
  const rest = restOf(dirty, 'messages');
  const merge: StepOp = {
    op: 'merge',
    uid,
    set: { ...rest.set, ...(messages.length > 0 ? { messages } : {}) },
    unset: [...rest.unset, ...(messages.length > 0 ? [] : ['messages'])],
  };
  return [merge, { op: 'insert', before: uid, step: pause }];
}
