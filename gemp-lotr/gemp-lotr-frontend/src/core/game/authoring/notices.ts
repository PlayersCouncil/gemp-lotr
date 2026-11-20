/**
 * What the authoring panel says above its tabs: where the game is in the script, what went wrong, the decision no
 * step covers, the parts of the file left out to open it, how a save went. Pure: the words, from the state.
 */
import { ApiError } from '../../api/api-error';
import { AuthoringDecision, DraftState, LeftOutPart, SaveReport, UncoveredDecision } from './authoring-state';
import { askedNamed } from './terms';
import { isBlock, stepsOf } from './steps';

/** The line under the panel's title: where the game is in the script, and in the game. */
export function whereLine(state: DraftState): string {
  const position = state.position;
  const count = stepsOf(state).length;
  const inBlock = isBlock(stepsOf(state)[position.step - 1]);
  const where = position.over
    ? 'The game is over'
    : position.frontier
      ? `Past the end of the script (${count} steps)`
      : `Step ${position.step}${position.sub > 0 ? `.${position.sub}` : ''} of ${count}` +
        (position.inHand
          ? ''
          : position.underWay
            ? inBlock
              ? ' (a block, between its decisions)'
              : ' (which repeats, and is under way)'
            : ' (waiting to come to it)');
  if (position.over) return where;
  // (the game's own opening, B10: before turn 1, in its own steps)
  if (position.turn === 0 && state.start?.opening === 'game') return `${where} · the game’s opening`;
  return `${where} · turn ${position.turn}, ${position.phase ?? ''}${turnWords(state)}`;
}

/** In a game of two sides (A3), whose turn it is and the person's role where it is not their seat's: " · their turn, you as Shadow". */
export function turnWords(state: DraftState): string {
  const { turnOf, roles } = state.position;
  if (state.twoSided !== true || turnOf === undefined) return '';
  // (the person's role only where their seat's name does not say it)
  const role = roles?.human !== undefined && roles.human !== (state.draft.human ?? 'freeps') ? roles.human : null;
  const as = role === null ? '' : `, you as ${role === 'shadow' ? 'Shadow' : 'Free Peoples'}`;
  return ` · ${turnOf === 'human' ? 'your' : 'their'} turn${as}`;
}

/** A waiting decision's role, in a game of two sides (A3): " · as Shadow"; nothing in a one-sided game. */
export function roleWords(state: DraftState, decision: AuthoringDecision | null): string {
  if (state.twoSided !== true || !decision?.role) return '';
  return ` · as ${decision.role === 'shadow' ? 'Shadow' : 'Free Peoples'}`;
}

/** What a failed call is said to be: the server's own words, or what its status means. */
export function problemOf(error: unknown): string {
  const failed = ApiError.from(error);
  if (failed.said !== null) return failed.said;
  if (failed.status === 403) return 'Writing scenarios is not open to this account.';
  if (failed.status === 0) return 'The server could not be reached.';
  return `The server answered ${failed.status}.`;
}

/**
 * Whether a decision is waiting that no step has in hand (the player's, or the opponent's held for the author), so
 * that a step can be added for it.
 */
export function unscripted(state: DraftState): boolean {
  const { position, waiting } = state;
  return !position.over && !position.inHand && (waiting.human !== null || waiting.opponent?.held === true);
}

/** The decision that is waiting with no step to cover it, where that is something to do something about. */
export function uncoveredOf(state: DraftState): UncoveredDecision | null {
  const open = state.uncovered;
  if (!open || state.mode === 'test' || state.position.over || state.position.frontier) return null;
  if ((open.seat === 'human' ? state.waiting.human : state.waiting.opponent) === null) return null;
  // A scenario that leaves its player free between steps has such decisions as a matter of course: there it is
  // only said where the script, playing itself, could get no further
  if (state.draft.coverage === 'guided' && open.seat === 'human' && state.stopped !== 'uncovered') return null;
  return open;
}

/** A line of the notices, with the longer explanation for its tooltip. */
export interface Said {
  text: string;
  title: string;
}

export interface UncoveredNotice {
  head: string;
  /** What is asked, and what it offers. */
  asked: string;
  /** Why no step can be added for it; null when one can. */
  problem: string | null;
  /** Where the step "Add a step for this decision" writes goes; null when none can be added. */
  how: string | null;
  /** The other ways to cover it. */
  or: Said;
}

/** What a decision is for, where a When can name it, after where it is: ", for nextSkirmish". */
function askedNote(asked: AuthoringDecision | null): string {
  const kind = askedNamed(asked?.kind ?? null);
  return kind === null ? '' : `; it is ${kind}`;
}

/** What is said of the decision no step covers: what is asked, what adding a step for it does, and the other ways. */
export function uncoveredNotice(state: DraftState, open: UncoveredDecision): UncoveredNotice {
  const asked = (open.seat === 'human' ? state.waiting.human : state.waiting.opponent) ?? null;
  const offers = (asked?.options ?? []).map((option) => option.term ?? option.alias ?? option.label);
  const pass = asked?.pass === true;
  const offered =
    offers.length === 0
      ? pass
        ? '. It offers nothing but Pass.'
        : '.'
      : `. It offers: ${offers.slice(0, 4).join(', ')}${offers.length > 4 ? `, and ${offers.length - 4} more` : ''}${pass ? ', or Pass.' : '.'}`;
  const who = open.seat === 'human' ? 'You are' : 'The opponent is';
  const how =
    open.block !== null
      ? `It goes into the block the game is in, as step ${open.block}.${open.sub}.`
      : open.reuses !== null
        ? `Step ${open.reuses}, which says nothing yet, becomes the step for it.`
        : open.repeating !== null
          ? `It goes in after step ${open.repeating}. That step repeats until "${open.until ?? ''}"; that becomes "${open.becomes ?? ''}", so that it ends where the new step begins.`
          : open.after === 0
            ? 'It goes in at the start of the script.'
            : `It goes in after step ${open.after}.`;
  const or: Said =
    open.seat === 'opponent'
      ? {
          text: 'Or answer it under Opponent.',
          title: 'With "write it into the script" ticked, that writes the same step.',
        }
      : state.recording
        ? {
            text: 'Or just answer it in the game.',
            title:
              'That writes the same step, with what you did as what it allows. Add the step first if it is to say ' +
              'something before it is answered.',
          }
        : { text: '"Write down what I do" is off: answering it writes nothing.', title: '' };
  return {
    head:
      state.stopped === 'uncovered'
        ? 'The script stopped here: no step covers this decision.'
        : 'No step covers this decision.',
    asked: `${who} being asked "${asked?.text ?? ''}" (${(asked?.where ?? '').replace(/_/g, ' ')}${askedNote(asked)})${offered}`,
    problem: open.problem === null ? null : `A step cannot be added for it as things stand: ${open.problem}`,
    how: open.problem === null ? how : null,
    or,
  };
}

export interface LeftOutLine {
  /** Where it was, said by the step's number now (and the file's, if that is another). */
  where: string;
  message: string;
  was: string;
  /** The step (from 1) it is in now, for "Show"; null if it is in none. */
  step: number | null;
}

export interface LeftOutNotice {
  head: string;
  lines: readonly LeftOutLine[];
}

/** What is said of the parts of the file left out to open it (none: null). */
export function leftOutNotice(state: DraftState): LeftOutNotice | null {
  const leftOut = state.leftOut;
  if (leftOut.length === 0) return null;
  const one = leftOut.length === 1;
  const head =
    `${one ? 'One part of the file was' : `${leftOut.length} parts of the file were`} mistaken, and ` +
    `${one ? 'has' : 'have'} been left out so that it could be opened.` +
    (state.dirty ? ` Saving writes the file without ${one ? 'it' : 'them'}.` : '');
  return { head, lines: leftOut.map((part) => leftOutLine(state, part)) };
}

const STEP_WHERE = /^step \d+( \([^)]*\))?/;

function leftOutLine(state: DraftState, part: LeftOutPart): LeftOutLine {
  const at = part.uid === null ? -1 : state.uids.indexOf(part.uid);
  let where = part.where;
  // (a step is said by where it is now; the file's number for it only if that is another)
  if (at > -1) {
    where =
      `step ${at + 1}${where.replace(STEP_WHERE, '')}` +
      (part.step !== at + 1 ? ` (step ${part.step} of the file)` : '');
  } else if (part.step > 0) {
    where += ` of the file${/^step \d+( \([^)]*\))?$/.test(part.where) ? ', left out whole' : ''}`;
  }
  return { where, message: part.message, was: part.was, step: at > -1 ? at + 1 : null };
}

/** The latest of what has gone wrong with the script since it was last read (and how many more), or null. */
export function problemsNotice(problems: readonly string[], read: number): string | null {
  const unread = problems.slice(read);
  const last = unread.at(-1);
  if (last === undefined) return null;
  return last + (unread.length > 1 ? ` (and ${unread.length - 1} more before it)` : '');
}

/** Whether the notice that the script stopped at a step offers to take that step's answer from what is done next. */
export function mayRetake(state: DraftState): boolean {
  const at = state.position;
  return state.stopped === 'step' && at.inHand && !at.over && !stepsOf(state)[at.step - 1]?.pause;
}

/** How a save went, at the top of the panel. */
export interface SavedNotice {
  /** It plays through, and nothing was left unsent. */
  good: boolean;
  /** There is more to read about it, under Scenario. */
  why: boolean;
  /** Kept when the draft changes again (a rename's). */
  stays: boolean;
  text: string;
}

/**
 * What is said of a save: the notice, and the whole report (for the Scenario tab). `unsent`: what is said of a step the
 * Step tab still has, which the server would not take.
 */
export function savedNotice(saved: SaveReport, unsent: string | null): { notice: SavedNotice; report: string } {
  const broken = saved.errors.length > 0;
  const warnings = saved.warnings.length;
  const moved = saved.movedFrom
    ? {
        report: ` (moved from ${saved.movedFrom}: that is where its kind is kept)`,
        text: `, moved from ${saved.movedFrom}`,
      }
    : { report: '', text: '' };
  let report =
    `Saved to ${saved.path}${moved.report}.\n` +
    (broken
      ? `It cannot be played:\n${saved.errors.join('\n')}`
      : saved.verified
        ? 'Scenario validated.'
        : `It does not play through yet:\n${saved.report ?? ''}`) +
    (warnings > 0 ? `\n\nWarnings:\n${saved.warnings.join('\n')}` : '');
  const notice: SavedNotice = {
    good: !broken && saved.verified,
    why: broken || !saved.verified || warnings > 0,
    stays: false,
    text:
      `Saved to ${saved.path}${moved.text}. ` +
      (broken
        ? 'It cannot be played as it stands.'
        : saved.verified
          ? 'Scenario validated.'
          : 'It does not play through yet.') +
      (warnings > 0 ? ` ${warnings} warning${warnings === 1 ? '' : 's'}.` : ''),
  };
  if (unsent !== null) {
    notice.good = false;
    notice.text += ` ${unsent}`;
    report += `\n\n${unsent}`;
  }
  return { notice, report };
}

/**
 * Where a waiting decision is, as the panel says it: the server's "turn 3, fellowship", or the game's own opening
 * (B10: turn 0, in no phase of its own); and its role in a game of two sides (" · as Shadow").
 */
export function decisionWhere(state: DraftState, decision: AuthoringDecision): string {
  const opening = state.position.turn === 0 && state.start?.opening === 'game';
  return (opening ? 'the game’s opening' : decision.where) + roleWords(state, decision);
}
