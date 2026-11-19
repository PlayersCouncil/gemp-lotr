/**
 * The Step tab's form, as a model: a step as the form works on it (every part there, a message as an object, a When
 * with requirements taken apart, `allow` and `end` always objects), the step its file would have, and which of its
 * parts differ from what was last sent. Pure (the legacy scenarioAuthor.js working / written / dirty / whereOf).
 */
import { DraftMessageValue, DraftState, DraftStep, StepStatus, StepTexts } from './authoring-state';
import { isAwaiting, kidsOf, messagesOf, own, stepsOf } from './steps';
import { whenParts } from './terms';

/** A message as the form edits it: lists of things on the table as one line, the gap as text. */
export interface WorkMessage {
  text: string;
  note?: string;
  header?: string;
  anchor?: string;
  highlight?: string;
  highlightStyle?: string;
  avoid?: string;
  side?: string;
  gap?: string;
  /** Only ever false (said every time the step applies); absent: once. */
  once?: boolean;
  advance?: string;
  on?: string;
  zoom?: string;
  focus?: string;
  magnify?: string | number;
  choices?: Readonly<Record<string, string>>;
  /** What else a file's message can say, kept as it is. */
  readonly [other: string]: unknown;
}

export interface WorkAllow {
  answers?: string;
  /** As the file has it (HJSON text) when written out in full. */
  passWhen?: string;
  otherwise?: string;
  hints: Readonly<Record<string, string>>;
}

export interface WorkEnd {
  /** '' (it doesn't end the scenario), complete or fail. */
  result: string;
  messages: readonly WorkMessage[];
}

/** A step as the form edits it. */
export interface WorkStep {
  id?: string;
  note?: string;
  checkpoint?: boolean;
  pause?: boolean;
  once?: boolean;
  if?: string;
  unless?: string;
  /** A line of terms, or (written out in full) HJSON text. */
  when?: string;
  /** A When's requirements, as HJSON text, beside its terms. */
  whenRequire?: string;
  until?: string;
  skipWhen?: string;
  do?: string;
  answer?: string;
  skirmishes?: boolean;
  messages: readonly WorkMessage[];
  allow: WorkAllow;
  end: WorkEnd;
  readonly [other: string]: unknown;
}

/** A step as its file has it, as a record of its parts (what is compared and sent). */
export type Written = Readonly<Record<string, unknown>>;

/** The parts of a step that differ from what was last sent: `set` to their new values, `unset` gone. */
export interface Diff {
  set: Record<string, unknown>;
  unset: string[];
  any: boolean;
}

/** The parts a file can have written out in full ({ ... }), edited as the file has them. */
export const TYPED = ['when', 'until', 'skipWhen'] as const;

/** A copy of a record without the parts named. */
export function without(record: Readonly<Record<string, unknown>>, ...names: string[]): Record<string, unknown> {
  return Object.fromEntries(Object.entries(record).filter(([name]) => !names.includes(name)));
}

/** A value as text, keys in order: two values that say the same compare equal. */
export function canon(value: unknown): string {
  return JSON.stringify(value, (_key, part: unknown) =>
    part !== null && typeof part === 'object' && !Array.isArray(part)
      ? Object.fromEntries(Object.entries(part as Record<string, unknown>).sort(([a], [b]) => (a < b ? -1 : 1)))
      : part,
  );
}

const asMessage = (message: DraftMessageValue): WorkMessage => {
  if (typeof message === 'string') return { text: message };
  const work: Record<string, unknown> = { ...message };
  // (lists of things on the table are edited as the line of text a file usually has them as)
  for (const name of ['anchor', 'highlight', 'avoid']) {
    const value = work[name];
    if (Array.isArray(value)) work[name] = value.join(', ');
  }
  if (typeof work['gap'] === 'number') work['gap'] = String(work['gap']);
  return work as WorkMessage;
};

/** A part written out in full, as the file has it: the server's text of it, else as JSON. */
const filed = (value: unknown, text: string | undefined): string | undefined =>
  value === undefined || value === null || typeof value === 'string'
    ? (value ?? undefined)
    : (text ?? JSON.stringify(value, null, '\t'));

/** A step as the form works on it. `texts`: the parts written out in full, as the server gives them (state.texts). */
export function working(step: DraftStep, texts: StepTexts = {}): WorkStep {
  const mine = own(step);
  let work: Record<string, unknown> = { ...mine };
  for (const name of TYPED) work[name] = filed(mine[name], texts[name]);
  // A When with requirements is edited as its terms and, beside them, its requirements
  const split = whenParts(mine.when);
  if (split !== null) {
    work['when'] = split.terms;
    work['whenRequire'] = texts.whenRequire ?? split.require;
  }
  work = Object.fromEntries(Object.entries(work).filter(([, value]) => value !== undefined));
  work['messages'] = messagesOf(mine).map(asMessage);
  const allow = mine.allow === undefined ? {} : typeof mine.allow === 'string' ? { answers: mine.allow } : mine.allow;
  const passWhen = filed(allow.passWhen, texts.passWhen);
  work['allow'] = {
    ...allow,
    ...(passWhen === undefined ? {} : { passWhen }),
    hints: { ...(allow.hints ?? {}) },
  };
  const end = mine.end === undefined ? null : typeof mine.end === 'string' ? { result: mine.end } : mine.end;
  work['end'] = {
    result: end?.result ?? '',
    messages: end?.messages === undefined ? [] : messagesOf({ messages: end.messages }).map(asMessage),
  };
  return work as WorkStep;
}

const blank = (value: unknown) => value === undefined || value === null || value === '' || value === false;
const blankText = (value: unknown) => blank(value) || (typeof value === 'string' && value.trim() === '');

/** A message as the file has it: blank parts dropped (but `once: false`), and choices without a name. */
function tidy(message: WorkMessage): Written {
  const out: Record<string, unknown> = {};
  for (const [name, value] of Object.entries(message)) {
    if (blank(value) && !(name === 'once' && value === false)) continue;
    if (name === 'choices') {
      const choices = Object.entries(value as Record<string, string>).filter(([choice]) => choice !== '');
      if (choices.length > 0) out['choices'] = Object.fromEntries(choices);
    } else {
      out[name] = value;
    }
  }
  out['text'] ??= '';
  return out;
}

const PLAIN = [
  'id',
  'note',
  'checkpoint',
  'pause',
  'once',
  'if',
  'unless',
  'when',
  'until',
  'skipWhen',
  'do',
  'skirmishes',
] as const;

/** The working copy as the step its file would have. */
export function written(work: WorkStep): Written {
  const step: Record<string, unknown> = {};
  for (const name of PLAIN) if (!blank(work[name])) step[name] = work[name];
  // (its requirements make the When an object, its terms one part of it)
  if (!blankText(work.whenRequire)) {
    step['when'] =
      `{\n\tterms: ${JSON.stringify((work.when ?? '').trim())}\n\trequire: ${(work.whenRequire ?? '').trim()}\n}`;
  }
  if (work.messages.length > 0) step['messages'] = work.messages.map(tidy);
  const allow = work.allow;
  const hints = Object.entries(allow.hints).filter(([alias, text]) => alias !== '' && !blank(text));
  if (!blank(allow.passWhen) || !blank(allow.otherwise) || hints.length > 0) {
    const out: Record<string, unknown> = {};
    if (!blank(allow.answers)) out['answers'] = allow.answers;
    if (!blank(allow.passWhen)) out['passWhen'] = allow.passWhen;
    if (!blank(allow.otherwise)) out['otherwise'] = allow.otherwise;
    if (hints.length > 0) out['hints'] = Object.fromEntries(hints);
    step['allow'] = out;
  } else if (!blank(allow.answers)) {
    step['allow'] = allow.answers;
  }
  if (!blank(work.answer)) step['answer'] = work.answer;
  if (!blank(work.end.result)) {
    step['end'] =
      work.end.messages.length > 0
        ? { result: work.end.result, messages: work.end.messages.map(tidy) }
        : work.end.result;
  }
  return step;
}

/** The parts of `now` that are not as `origin` has them. */
export function diff(now: Written, origin: Written): Diff {
  const result: Diff = { set: {}, unset: [], any: false };
  for (const [name, value] of Object.entries(now)) {
    if (canon(value) !== canon(origin[name])) {
      result.set[name] = value;
      result.any = true;
    }
  }
  for (const name of Object.keys(origin)) {
    if (!(name in now)) {
      result.unset.push(name);
      result.any = true;
    }
  }
  return result;
}

/** The fields of the working copy that make up a part of the file's step. */
export function fieldsOf(part: string): readonly string[] {
  return part === 'when' ? ['when', 'whenRequire'] : [part];
}

/** The part of the file's step a field of the working copy is in ("messages.2.text" is in messages). */
export function partOf(path: string): string {
  const head = path.split('.')[0] ?? path;
  return head === 'whenRequire' ? 'when' : head;
}

/** The value at a path of the working copy ("messages.0.text", "allow.hints.aragorn"); undefined if there is none. */
export function valueAt(work: unknown, path: string): unknown {
  let at: unknown = work;
  for (const key of path.split('.')) {
    if (at === null || typeof at !== 'object') return undefined;
    at = (at as Record<string, unknown>)[key];
  }
  return at;
}

/** A copy of the working copy with the value at a path changed (undefined: taken out), the rest shared. */
export function withValue<T>(work: T, path: string, value: unknown): T {
  const [key = '', ...rest] = path.split('.');
  const into = (at: unknown): unknown => {
    if (Array.isArray(at)) {
      const copy: unknown[] = [...(at as unknown[])];
      copy[Number(key)] = rest.length === 0 ? value : withValue(copy[Number(key)], rest.join('.'), value);
      return copy;
    }
    const copy: Record<string, unknown> = { ...((at ?? {}) as Record<string, unknown>) };
    const next = rest.length === 0 ? value : withValue(copy[key], rest.join('.'), value);
    return next === undefined ? without(copy, key) : { ...copy, [key]: next };
  };
  return into(work) as T;
}

/**
 * Takes the server's step into the form where the author has not changed it since it was last sent: each part of the
 * file's step that is still as `origin` has it in the form, and is not so on the server, is taken from `theirs` (the
 * server's step, as the form works on it). What the author has typed since stays as typed. `skip`: parts left as
 * they are whatever the server has (the ones just sent: what the server makes of them is asFiled's).
 */
export function rebase(
  work: WorkStep,
  origin: Written,
  theirs: WorkStep,
  skip: readonly string[] = [],
): { work: WorkStep; origin: Written; changed: boolean } {
  const mine = written(work);
  const filedTheirs = written(theirs);
  let next = work;
  let nextOrigin: Record<string, unknown> = { ...origin };
  let changed = false;
  for (const part of new Set([...Object.keys(filedTheirs), ...Object.keys(origin), ...Object.keys(mine)])) {
    if (skip.includes(part)) continue;
    const untouched = canon(mine[part]) === canon(origin[part]);
    if (!untouched || canon(filedTheirs[part]) === canon(origin[part])) continue;
    for (const field of fieldsOf(part)) next = withValue(next, field, theirs[field]);
    nextOrigin = part in filedTheirs ? { ...nextOrigin, [part]: filedTheirs[part] } : without(nextOrigin, part);
    changed = true;
  }
  return { work: next, origin: nextOrigin, changed };
}

/** What kind of step the form is for: one of the script's, what a block says of itself, or one of a block's steps. */
export type FormKind = 'step' | 'block' | 'child';

/** What has become of a step in the game in hand, in words, and its status. `sub`: a block's step (from 0), or -1. */
export function whereOf(state: DraftState, index: number, sub: number): { words: string; status: StepStatus } {
  const position = state.position;
  const child = sub > -1;
  const uid = child ? (kidsOf(state, index)[sub] ?? null) : (state.uids[index] ?? null);
  const status: StepStatus = (child ? state.substatuses[index]?.[sub] : state.statuses[index]) ?? 'unreached';
  const here = index + 1 === position.step && !position.over;
  // (a block is where the game is for as long as it lasts; one of its steps, while its decision is in hand)
  const current =
    here &&
    (child
      ? position.inHand && position.sub === sub + 1
      : position.inHand || (position.underWay && stepsOf(state)[index]?.anyOrder !== undefined));
  let words = current
    ? 'Current Step'
    : status === 'matched'
      ? 'Completed Step'
      : status === 'bypassed'
        ? 'not on this way through'
        : status === 'skipped'
          ? 'skipped'
          : 'Future Step';
  if (isAwaiting(state, uid)) words += ', no answer yet';
  return { words, status };
}

/** The aliases a play(card) in what a step allows or answers names, each once. */
export function playsOf(work: WorkStep): string[] {
  const plays: string[] = [];
  for (const match of `${work.allow.answers ?? ''}, ${work.answer ?? ''}`.matchAll(/play\(\s*([A-Za-z0-9_]+)\s*\)/g)) {
    const alias = match[1] ?? '';
    if (!plays.includes(alias)) plays.push(alias);
  }
  return plays;
}

const canSpot = (alias: string, indent: string) =>
  `${indent}{\n${indent}\ttype: canSpot\n${indent}\tfilter: alias(${alias})\n${indent}}`;

/** "Pass only once they are in play": Pass when, as the file has it, for these cards on the table. */
export function passWhenInPlay(plays: readonly string[]): string {
  return `[\n${plays.map((alias) => canSpot(alias, '\t')).join('\n')}\n]`;
}

/** "Until they are in play": an Until, as the file has it, for the player's action once these are on the table. */
export function untilInPlay(human: string, plays: readonly string[]): string {
  return `{\n\tseat: ${human}\n\tdecision: action\n\trequire: [\n${plays
    .map((alias) => canSpot(alias, '\t\t'))
    .join('\n')}\n\t]\n}`;
}

/** "Pass only once X and Y are in play": the words for some cards. */
export function inPlayWords(plays: readonly string[]): string {
  return plays.length === 0 ? 'they are' : `${plays.join(' and ')} ${plays.length === 1 ? 'is' : 'are'}`;
}
