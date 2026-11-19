/**
 * The Scenario tab's model of the scenario as a whole (the legacy scenarioAuthor.js renderScenario, renderRules and
 * renderOutcome): what is said of its file, what each of its controls sends as a top-level part of the draft, how the
 * usual rules are read out of `rules` and written back, how the player loses, and what Check and Try other answers
 * report. Each `...With` function answers the part's new value, or null where the part goes (the server takes null as
 * "take it out"). Pure.
 */
import {
  CheckReport,
  Draft,
  DraftOptions,
  DraftRequirement,
  DraftSeat,
  DraftState,
  OffScriptReport,
} from './authoring-state';
import { PHASES, splitTerms } from './terms';

export type Phase = (typeof PHASES)[number];

/** A part with nothing left in it is taken out. */
const orNull = <T extends object>(value: T): T | null => (Object.keys(value).length === 0 ? null : value);

/* ------------------------------------------------------------------ the file */

/** Where the file is, and whether what is in it is what is here. */
export function pathLine(state: DraftState): string {
  const saved = !state.onDisk ? 'not saved yet' : state.dirty ? 'changed since it was saved' : 'saved';
  return `${state.path ?? ''}${state.canonical ? ' (public)' : ''} — ${saved}`;
}

/** What is said of comments in the file that saving will drop (none: nothing). */
export function commentsNote(state: DraftState): { text: string; hint: string } | null {
  const lost = state.commentsLost;
  if (lost <= 0) return null;
  return {
    text: `Saving drops ${lost} line${lost === 1 ? '' : 's'} of comment.`,
    hint: 'Comments written in the file by hand are not kept by saving. Notes are part of the scenario, and are kept.',
  };
}

/** Saving moves the file to where its kind is kept (null when it doesn't). */
export function movesNote(state: DraftState): { text: string; hint: string } | null {
  if (!state.onDisk || state.fileOnSave === state.file) return null;
  return {
    text: `Saving moves it to ${state.fileOnSave}.`,
    hint: `A ${state.draft.kind ?? 'tutorial'} is kept under ${state.kindFolder}/.`,
  };
}

/** The report of a Check. */
export function checkReport(result: CheckReport): string {
  return (
    `${result.passed ? 'Scenario validated.' : 'It does not play through.'}\n\n${result.report ?? ''}` +
    (result.warnings.length > 0 ? `\n\nWarnings:\n${result.warnings.join('\n')}` : '')
  );
}

/** The report of Try other answers. */
export function offScriptReport(result: OffScriptReport): string {
  return `${result.handled ? 'Every other answer is handled.' : 'Some other answers are not handled.'}\n\n${result.report ?? ''}`;
}

/* ------------------------------------------------------------------ about it */

/** The parts "About it" edits as text. */
export type AboutPart = 'title' | 'summary' | 'id' | 'author' | 'tags' | 'version' | 'note';

/** What a text field of "About it" sends: blank is taken out; the version is a number (1 if it isn't one). */
export function aboutValue(name: AboutPart, typed: string): string | number | null {
  if (name === 'version') return Number.parseInt(typed, 10) || 1;
  return typed === '' ? null : typed;
}

/** What an "About it" field shows. */
export function aboutText(draft: Draft, name: AboutPart): string {
  const value = draft[name];
  return value === undefined ? '' : String(value);
}

/* ------------------------------------------------------------------ how it plays */

type Opponent = NonNullable<Draft['opponent']>;

/** The opponent's part with its name changed (blank: none). */
export function opponentWithName(draft: Draft, name: string): Opponent | null {
  const changed: { -readonly [K in keyof Opponent]: Opponent[K] } = { ...draft.opponent };
  if (name === '') delete changed.name;
  else changed.name = name;
  return orNull(changed);
}

/** The opponent's part with what it does unscripted changed ("fail", the default, is left out). */
export function opponentWithFallback(draft: Draft, fallback: string): Opponent | null {
  const changed: { -readonly [K in keyof Opponent]: Opponent[K] } = { ...draft.opponent };
  if (fallback === 'pass') changed.fallback = 'pass';
  else delete changed.fallback;
  return orNull(changed);
}

/** The phases the player is passed through (a file may say `true`, which the server writes back as the phases). */
export function autoPassOf(options: DraftOptions | undefined): string[] {
  return typeof options?.autoPass === 'string' ? splitTerms(options.autoPass) : [];
}

/** The options with one of the phases the player is passed through ticked or not (in the order ticked). */
export function optionsWithAutoPass(draft: Draft, phase: string, on: boolean): DraftOptions | null {
  const now = autoPassOf(draft.options).filter((name) => name !== phase);
  if (on) now.push(phase);
  return optionsWith(draft, 'autoPass', now.length === 0 ? null : now.join(', '));
}

/** The options with one changed (null: taken out, to be the default). */
export function optionsWith<K extends keyof DraftOptions>(
  draft: Draft,
  name: K,
  value: DraftOptions[K] | null,
): DraftOptions | null {
  const changed: DraftOptions = { ...draft.options };
  if (value === null || value === undefined) Reflect.deleteProperty(changed, name);
  else changed[name] = value;
  return orNull(changed);
}

/** Auto-accept is on unless the file says false: unticked, it says so; ticked, nothing. */
export function autoAcceptValue(on: boolean): false | null {
  return on ? null : false;
}

/** Whether deck reveals are dismissed when nothing says otherwise: for a tutorial they are. */
export function revealByKind(draft: Draft): boolean {
  return (draft.kind ?? 'tutorial') === 'tutorial';
}

/** Auto-dismiss deck reveal: written only where it is not what the kind gives. */
export function revealValue(draft: Draft, on: boolean): boolean | null {
  return on === revealByKind(draft) ? null : on;
}

/** The animation speeds to choose from: none (the player's own), then -4 to 4, as the slider's steps. */
export const SPEEDS: readonly (readonly [string, string])[] = [
  ['', "player's own"],
  ...[-4, -3, -2, -1, 0, 1, 2, 3, 4].map((speed): readonly [string, string] => [
    String(speed),
    speed === 0
      ? '0: usual'
      : `${speed > 0 ? '+' : ''}${speed}: ${2 ** Math.abs(speed)}× ${speed > 0 ? 'faster' : 'slower'}`,
  ]),
];

/** The animation speed chosen ("" for none) as the file has it. */
export function speedValue(chosen: string): number | null {
  return chosen === '' ? null : Number.parseInt(chosen, 10);
}

/** The bubble gap typed (blank: the stock gap). */
export function bubbleGapValue(typed: string): string | null {
  const gap = typed.trim();
  return gap === '' ? null : gap;
}

/* ------------------------------------------------------------------ rules */

export type Rules = NonNullable<Draft['rules']>;
type Effect = DraftRequirement;

/** The usual rules of one seat, as the tab offers them; anything else under the seat is kept as it is (`others`). */
export interface SeatRules {
  /** Added to the hand size of 8. */
  hand: number;
  /** Phases skipped. */
  skip: string[];
  /** The One Ring's game text switched off. */
  ring: boolean;
  /** Added to the seat's minions' roaming penalty. */
  roaming: number;
  others: Effect[];
}

export type RuleKind = 'hand' | 'skip' | 'ring' | 'roaming';

const isObject = (value: unknown): value is Readonly<Record<string, unknown>> =>
  value !== null && typeof value === 'object' && !Array.isArray(value);

/** Whether an object has none but these keys. */
const only = (value: unknown, names: readonly string[]): value is Readonly<Record<string, unknown>> =>
  isObject(value) && Object.keys(value).every((name) => names.includes(name));

/** What one of a seat's effects is, if it is one of the usual ones (legacy kindOf). */
export function kindOf(effect: unknown): RuleKind | null {
  if (!only(effect, ['type', 'modifier']) || effect['type'] !== 'modifier') return null;
  const m = effect['modifier'];
  if (!isObject(m)) return null;
  if (m['type'] === 'modifyHandSize' && only(m, ['type', 'amount'])) return 'hand';
  if (m['type'] === 'skipPhase' && only(m, ['type', 'phase'])) return 'skip';
  if (m['type'] === 'disableGameText' && only(m, ['type', 'filter']) && m['filter'] === 'name(The One Ring)') {
    return 'ring';
  }
  if (m['type'] === 'modifyRoamingPenalty' && only(m, ['type', 'filter', 'amount']) && m['filter'] === 'minion') {
    return 'roaming';
  }
  return null;
}

const amountOf = (effect: Effect): number => {
  const modifier = effect['modifier'] as Readonly<Record<string, unknown>>;
  return Number.parseInt(String(modifier['amount']), 10) || 0;
};

/** A seat's rules read out of its effects. */
export function seatRulesOf(rules: Rules | undefined, seat: DraftSeat): SeatRules {
  const known: SeatRules = { hand: 0, skip: [], ring: false, roaming: 0, others: [] };
  for (const effect of rules?.[seat] ?? []) {
    const kind = kindOf(effect);
    if (kind === 'hand') known.hand += amountOf(effect);
    else if (kind === 'skip') {
      known.skip.push(String((effect['modifier'] as Readonly<Record<string, unknown>>)['phase']).toLowerCase());
    } else if (kind === 'ring') known.ring = true;
    else if (kind === 'roaming') known.roaming += amountOf(effect);
    else known.others.push(effect);
  }
  return known;
}

/** A seat's effects written back from its rules (legacy rebuild): the usual ones first, then the rest as they were. */
export function rebuild(known: SeatRules): Effect[] {
  const out: Effect[] = [];
  const modifier = (m: Readonly<Record<string, unknown>>): Effect => ({ type: 'modifier', modifier: m });
  if (known.hand !== 0) out.push(modifier({ type: 'modifyHandSize', amount: known.hand }));
  if (known.ring) out.push(modifier({ type: 'disableGameText', filter: 'name(The One Ring)' }));
  for (const phase of PHASES) {
    if (known.skip.includes(phase)) out.push(modifier({ type: 'skipPhase', phase }));
  }
  if (known.roaming !== 0) {
    out.push(modifier({ type: 'modifyRoamingPenalty', filter: 'minion', amount: known.roaming }));
  }
  return [...out, ...known.others];
}

/** What the rules part is with nothing left in it: taken out. */
function rulesOrNull(rules: { -readonly [K in keyof Rules]: Rules[K] }): Rules | null {
  const empty = !rules.oneSided && (rules.freeps ?? []).length === 0 && (rules.shadow ?? []).length === 0;
  return empty ? null : rules;
}

/** The rules with "one-sided" ticked or not. */
export function rulesWithOneSided(draft: Draft, on: boolean): Rules | null {
  const rules: { -readonly [K in keyof Rules]: Rules[K] } = { ...draft.rules };
  if (on) rules.oneSided = true;
  else delete rules.oneSided;
  return rulesOrNull(rules);
}

/** The rules with a seat's usual ones changed (`change`: what is different). */
export function rulesWithSeat(draft: Draft, seat: DraftSeat, change: Partial<Omit<SeatRules, 'others'>>): Rules | null {
  const known = { ...seatRulesOf(draft.rules, seat), ...change };
  const effects = rebuild(known);
  const rules: { -readonly [K in keyof Rules]: Rules[K] } = { ...draft.rules };
  if (effects.length === 0) Reflect.deleteProperty(rules, seat);
  else rules[seat] = effects;
  return rulesOrNull(rules);
}

/** The skipped phases with one ticked or not. */
export function skipWith(known: SeatRules, phase: string, on: boolean): string[] {
  const skip = known.skip.filter((name) => name !== phase);
  if (on) skip.push(phase);
  return skip;
}

/** A number typed for a rule (anything else is 0). */
export function ruleNumber(typed: string): number {
  return Number.parseInt(typed, 10) || 0;
}

/* ------------------------------------------------------------------ how it is lost */

type Outcome = NonNullable<Draft['outcome']>;

/** "Also when any companion is killed". */
export const COMPANION_LOST: DraftRequirement = { type: 'hasCardInDeadPile', filter: 'companion' };

/** How the player loses: '' as the game is lost, 'companion', or 'other' (the file says something else). */
export type LoseChoice = '' | 'companion' | 'other';

export function loseChoiceOf(draft: Draft): LoseChoice {
  const lose = draft.outcome?.lose;
  if (lose === undefined) return '';
  return JSON.stringify(lose) === JSON.stringify(COMPANION_LOST) ? 'companion' : 'other';
}

/** The choices of how the player loses: "as the file says" only when it does. */
export function loseOptions(choice: LoseChoice): (readonly [LoseChoice, string])[] {
  const options: (readonly [LoseChoice, string])[] = [
    ['', 'as the game is lost'],
    ['companion', 'also when any companion is killed'],
  ];
  if (choice === 'other') options.push(['other', 'as the file says']);
  return options;
}

/**
 * The outcome with how the player loses changed; undefined for "as the file says", which changes nothing (it is only
 * there to show what the file has).
 */
export function outcomeWithLose(draft: Draft, choice: string): Outcome | null | undefined {
  if (choice !== '' && choice !== 'companion') return undefined;
  const outcome: { -readonly [K in keyof Outcome]: Outcome[K] } = { ...draft.outcome };
  if (choice === '') delete outcome.lose;
  else outcome.lose = COMPANION_LOST;
  return orNull(outcome);
}

/** The outcome with what the player is told when they lose changed (blank: nothing of the scenario's own). */
export function outcomeWithMessage(draft: Draft, typed: string): Outcome | null {
  const outcome: { -readonly [K in keyof Outcome]: Outcome[K] } = { ...draft.outcome };
  if (typed.trim() === '') delete outcome.loseMessage;
  else outcome.loseMessage = typed;
  return orNull(outcome);
}

/* ------------------------------------------------------------------ the rest of the file */

/** The parts "The rest of the file" offers, as HJSON. */
export const FILE_PARTS = [
  'cards',
  'ringBearer',
  'ring',
  'sites',
  'setup',
  'rules',
  'rng',
  'opponent',
  'options',
  'outcome',
] as const;

export type FilePart = (typeof FILE_PARTS)[number];

/** Whether the draft has the part at all. */
export function hasPart(draft: Draft, name: FilePart): boolean {
  return draft[name] !== undefined;
}
