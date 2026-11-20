/**
 * The Timeline tab's list: a row for each step of the script (and under a block, one for each of its steps), grouped
 * by the turn and phase they come in, with what each waits for, what it does, and its marks; and what is said at the
 * end of the list. Pure: from the server's state and which step is selected.
 */
import { DraftState, DraftStep, StepStatus } from './authoring-state';
import { isAwaiting, isBlock, isHumanSeat, isOpening, kidsOf, leftOutOf, messagesOf, numberOf, stepsOf } from './steps';
import { askedTerm, askedWords, SEATS, splitTerms, TYPES, whenParts } from './terms';
import { unscripted } from './notices';

/** Who a step is about: read only (a pause, the opening), the player's, the opponent's, either's, or a block. */
export type StepWho = 'Read' | 'You' | 'Opp' | 'Any' | 'Block';

export interface StepSummary {
  who: StepWho;
  /** What it waits for, short: "action fellowship t1" (the seat goes in `who`). */
  when: string;
  /** What it does: what the opening does, its answer, or what it allows. */
  act: string;
}

/** What each group's phase is called in its heading. */
export const PHASE_NAMES: Readonly<Record<string, string>> = {
  opening: 'Before the first turn',
  fellowship: 'Fellowship',
  shadow: 'Shadow',
  maneuver: 'Maneuver',
  archery: 'Archery',
  assignment: 'Assignment',
  skirmish: 'Skirmish',
  regroup: 'Regroup',
  between_turns: 'Between turns',
  block: 'Several phases, in any order',
  skirmishes: 'Assignment and skirmishes, in any order',
  'after-block': 'After the block',
};

/** Said of a step that is a pass the game makes by itself. */
export const SILENT =
  'A pass the game makes by itself, when there is nothing to play: this step is used up by it with nobody asked. ' +
  'Give it a message and the pass is asked for, and the message said over it.';

/** A step in a line of the list. */
export function summary(step: DraftStep, human: (seat: string) => boolean): StepSummary {
  // (a When with requirements reads as its terms, marked as having more to it)
  const parts = whenParts(step.when);
  const when =
    typeof step.when === 'string'
      ? step.when
      : parts !== null
        ? `${parts.terms}, only while…`
        : step.when !== undefined
          ? '{...}'
          : '';
  let seat: string | null = null;
  const rest: string[] = [];
  const terms = splitTerms(when);
  // (a When that says what the decision is for reads as that, not as the kind of decision it comes as: "next
  // skirmish", not "cards")
  const saysAsked = terms.some((term) => askedTerm(term)?.not === false);
  for (const term of terms) {
    const phase = /^phase\((.*)\)$/i.exec(term);
    const turn = /^turn\((.*)\)$/i.exec(term);
    const asked = askedWords(term);
    if ((SEATS as readonly string[]).includes(term.toLowerCase())) seat = term.toLowerCase();
    else if (phase) rest.push(phase[1] ?? '');
    else if (turn) rest.push(`t${turn[1] ?? ''}`);
    else if (asked !== null) rest.push(asked);
    else if (!(saysAsked && (TYPES as readonly string[]).includes(term))) rest.push(term);
  }
  if (isBlock(step)) {
    const until = typeof step.until === 'string' ? step.until : '{...}';
    return {
      who: 'Block',
      when: rest.join(' '),
      act: `${step.skirmishes ? 'skirmishes' : 'any order'}, until ${until}`,
    };
  }
  const who: StepWho = step.pause || isOpening(step) ? 'Read' : seat === null ? 'Any' : human(seat) ? 'You' : 'Opp';
  let act = '';
  if (isOpening(step)) act = step.do ?? '';
  else if (step.answer) act = step.answer;
  else if (step.allow !== undefined)
    act = typeof step.allow === 'string' ? step.allow : (step.allow.answers ?? 'pass when it may');
  return { who, when: rest.join(' '), act };
}

/** The turn and phase a step comes in, each null where that can't be told. */
export interface StepWhere {
  turn: number | null;
  phase: string | null;
}

/**
 * Where a step comes: what its When names; else where the game was when the step had its first decision (`seen`, as
 * the server keeps it); else what its When says of the decision (an assignment, a skirmish's).
 */
export function placeOf(step: DraftStep, seen: StepWhere | null | undefined): StepWhere {
  if (isOpening(step)) return { turn: 0, phase: 'opening' };
  const when = typeof step.when === 'string' ? step.when : '';
  const turn = /turn\(\s*(\d+)\s*\)/i.exec(when);
  const phase = /phase\(\s*([a-z_]+)\s*\)/i.exec(when);
  let said = phase ? (phase[1] ?? '').toLowerCase() : null;
  if (isBlock(step)) said = step.skirmishes ? 'skirmishes' : (said ?? 'block');
  else if (said === null && seen) said = seen.phase;
  else if (said === null && /(^|,)\s*assign\s*(,|$)/i.test(when)) said = 'assignment';
  else if (said === null && /\b(inSkirmish|fighting)\(/i.test(when)) said = 'skirmish';
  else said ??= askedPhase(when);
  return { turn: turn ? Number(turn[1]) : (seen?.turn ?? null), phase: said };
}

/** The phase a When's "what is asked" comes in, where it always comes in the same one; else null. */
function askedPhase(when: string): string | null {
  const terms = splitTerms(when);
  const asked = terms.map(askedTerm).find((one) => one !== null && !one.not)?.asked;
  if (asked === 'archeryWound') return 'archery';
  if (asked === 'anotherMove' || asked === 'reconcile') return 'regroup';
  // (the first choice of skirmish comes in the Assignment phase, the ones after a skirmish in the Skirmish phase)
  if (asked === 'nextSkirmish') return terms.some((term) => /^after\(/i.test(term)) ? 'skirmish' : 'assignment';
  return null;
}

const onlyPass = (text: string | undefined) => {
  const terms = splitTerms(text);
  return terms.length > 0 && terms.every((term) => term.replace(/\?$/, '') === 'pass');
};

/** A step that is only a pass and says nothing (the server's ScenarioDirector.isSilentPass). */
export function silentPass(step: DraftStep): boolean {
  if (step.pause || isBlock(step) || messagesOf(step).length > 0 || step.checkpoint) return false;
  if (step.end !== undefined && (typeof step.end === 'string' || Boolean(step.end.result))) return false;
  const allow = step.allow === undefined ? null : typeof step.allow === 'string' ? { answers: step.allow } : step.allow;
  const answers = step.answer !== undefined && step.answer.trim() !== '';
  const allows = allow !== null && allow.passWhen === undefined && onlyPass(allow.answers);
  return (answers && onlyPass(step.answer) && (allow === null || allows)) || (allows && !answers);
}

/**
 * A silent pass where the game passes by itself: the opponent's (with nothing to play it is never asked), or the
 * player's in a phase they are passed through. (The player's anywhere else is asked all the same.)
 */
export function passesByItself(state: DraftState, step: DraftStep): boolean {
  if (!silentPass(step) || typeof step.when !== 'string') return false;
  const seat = (splitTerms(step.when)[0] ?? '').toLowerCase();
  if ((SEATS as readonly string[]).includes(seat) && !isHumanSeat(state, seat)) return true;
  const phase = /phase\(\s*([a-z_]+)\s*\)/i.exec(step.when);
  const passed = state.draft.options?.autoPass;
  if (!phase || passed === undefined) return false;
  return (
    passed === true ||
    String(passed).toLowerCase() === 'true' ||
    splitTerms(String(passed)).includes((phase[1] ?? '').toLowerCase())
  );
}

/** The last checkpoint step before where the game is (or, `inclusive`, at it); 1 if there is none. */
export function lastCheckpoint(state: DraftState, inclusive: boolean): number {
  const steps = stepsOf(state);
  const at = state.position.step;
  for (let i = Math.min(steps.length, inclusive ? at : at - 1) - 1; i >= 0; i--) {
    if (steps[i]?.checkpoint) return i + 1;
  }
  return 1;
}

/** A mark on a row: a short word, and what it means (the row's tooltip). */
export interface TimelineTag {
  kind: 'if' | 'unless' | 'do' | 'messages' | 'once' | 'checkpoint' | 'end' | 'awaiting' | 'left-out';
  text: string;
  title: string;
}

export interface TimelineGroup {
  kind: 'group';
  /** "Turn 2 · Fellowship". */
  label: string;
  /** The group's phase (its colour), or "none". */
  phase: string;
  /** An even turn (drawn a little apart from the odd ones). */
  even: boolean;
}

export interface TimelineRow {
  kind: 'row';
  /** The step's place in the script (from 0) and, for one of a block's steps, in the block (-1: none). */
  index: number;
  sub: number;
  /** "34" or "34.2". */
  number: string;
  uid: string | null;
  status: StepStatus;
  summary: StepSummary;
  /** The step's own name, if it has one. */
  id: string | null;
  tags: readonly TimelineTag[];
  /** Where the game is. */
  current: boolean;
  selected: boolean;
  /** A pass the game makes by itself (faded; SILENT says why). */
  silent: boolean;
  block: boolean;
  phase: string;
  even: boolean;
}

export type TimelineItem = TimelineGroup | TimelineRow;

/** Which step is selected: its number (from 1) and, in a block, its step's name. */
export interface TimelinePick {
  selected: number | null;
  sub: string | null;
}

const ONCE =
  'It applies to one decision. Without this, a step in a block applies to every decision that fits it, for as long ' +
  'as the block lasts.';
const AWAITING = 'It says nothing yet about what is done. When the game comes to it, what you do there is written in.';

function tagsOf(state: DraftState, step: DraftStep, uid: string | null, child: boolean): TimelineTag[] {
  const tags: TimelineTag[] = [];
  if (step.if) tags.push({ kind: 'if', text: `if ${step.if}`, title: '' });
  if (step.unless) tags.push({ kind: 'unless', text: `unless ${step.unless}`, title: '' });
  if (step.do) tags.push({ kind: 'do', text: 'do', title: step.do });
  const count = messagesOf(step).length;
  if (count > 0) {
    tags.push({ kind: 'messages', text: `▬${count}`, title: `${count} message${count === 1 ? '' : 's'}` });
  }
  if (step.once) tags.push({ kind: 'once', text: 'once', title: ONCE });
  if (step.checkpoint) tags.push({ kind: 'checkpoint', text: '⚑', title: 'A checkpoint' });
  if (step.end) tags.push({ kind: 'end', text: 'end', title: '' });
  if (isAwaiting(state, uid)) tags.push({ kind: 'awaiting', text: 'no answer yet', title: AWAITING });
  const mistaken = child ? [] : leftOutOf(state, uid);
  if (mistaken.length > 0) {
    tags.push({
      kind: 'left-out',
      text: 'left out',
      title: mistaken
        .map((one) => `${one.where.replace(/^step \d+( \([^)]*\))?\.?/, '')}: ${one.message}\nIt said: ${one.was}`)
        .join('\n\n'),
    });
  }
  return tags;
}

/**
 * The list: the steps grouped by the turn and phase they are in, with a heading where that changes (a step that says
 * neither is where the one before it is); and each row in its group's phase.
 */
export function timelineItems(state: DraftState, pick: TimelinePick): TimelineItem[] {
  const position = state.position;
  const human = (seat: string) => isHumanSeat(state, seat);
  const items: TimelineItem[] = [];
  let group: StepWhere = { turn: null, phase: null };
  const row = (step: DraftStep, index: number, sub: number): TimelineRow => {
    const number = index + 1;
    const child = sub > -1;
    const uid = (child ? kidsOf(state, index)[sub] : state.uids[index]) ?? null;
    const status = (child ? state.substatuses[index]?.[sub] : state.statuses[index]) ?? 'unreached';
    return {
      kind: 'row',
      index,
      sub,
      number: numberOf(index, sub),
      uid,
      status,
      summary: summary(step, human),
      id: step.id ?? null,
      tags: tagsOf(state, step, uid, child),
      current: number === position.step && !position.over && !position.frontier && (!child || position.sub === sub + 1),
      selected: number === pick.selected && (child ? pick.sub === uid : pick.sub === null),
      silent: passesByItself(state, step),
      block: isBlock(step),
      phase: group.phase ?? 'none',
      even: group.turn !== null && group.turn % 2 === 0,
    };
  };
  for (const [index, step] of stepsOf(state).entries()) {
    const where = placeOf(step, state.places[index]);
    const turn = where.turn ?? group.turn;
    const phase = where.phase ?? group.phase;
    if (turn !== group.turn || phase !== group.phase) {
      group = { turn, phase };
      const words = [
        ...(turn === null ? [] : [`Turn ${turn}`]),
        ...(phase === null ? [] : [PHASE_NAMES[phase] ?? phase]),
      ];
      if (words.length > 0) {
        items.push({
          kind: 'group',
          label: words.join(' · '),
          phase: phase ?? 'none',
          even: turn !== null && turn % 2 === 0,
        });
      }
    }
    items.push(row(step, index, -1));
    for (const [sub, child] of (step.anyOrder ?? []).entries()) items.push(row(child, index, sub));
    // (a block may run through several phases: what follows it says where it is, or is in the block's last)
    if (isBlock(step) && typeof step.until === 'string' && /phase\(\s*([a-z_]+)\s*\)/i.test(step.until)) {
      group = { ...group, phase: 'after-block' };
    }
  }
  return items;
}

/** What is said under the last row. */
export interface TimelineEnd {
  /** The game is over: how, and that a step is to be gone back to. */
  over: string | null;
  /** The game is past the script's end (the row is "current"). */
  frontier: string | null;
  /** A decision is waiting that no step covers: what is asked (a step can be added for it). */
  asked: string | null;
}

export function timelineEnd(state: DraftState): TimelineEnd {
  const position = state.position;
  const result = position.result === null ? '' : ` (${position.result.replace(/_/g, ' ')})`;
  const waiting = state.waiting;
  const asked = waiting.human ?? waiting.opponent;
  return {
    over: position.over ? `The game is over${result}. Go back to a step to carry on.` : null,
    frontier:
      !position.over && position.frontier
        ? `The script ends here. What you do from here on is ${state.recording ? '' : 'not '}written down.`
        : null,
    asked:
      unscripted(state) && asked
        ? `${waiting.human ? 'You are' : 'The opponent is'} being asked: ${asked.text} (${asked.where})`
        : null,
  };
}
