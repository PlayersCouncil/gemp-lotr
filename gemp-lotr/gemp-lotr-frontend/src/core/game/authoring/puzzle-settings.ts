/**
 * The Scenario tab's model of what puzzles and sandboxes brought to a scenario's file (ep-notes.md, es-notes.md 4):
 * the kinds and a sandbox's tools (`options.tools`), take-backs (`options.undo`), when it is judged
 * (`outcome.judge`), its score (`outcome.score`), what the person may not do (`rules.forbid`) and the puzzle's pool.
 * Each `...With` answers the part's new value as the whole part is sent (`top: {options}`, `{outcome}`, `{rules}`),
 * every other key of the part as the draft has it; null where nothing is left of the part. Pure.
 */
import { Draft, DraftOptions, DraftState, ScenarioKind } from './authoring-state';
import { JudgeMomentKind, PuzzleScore, SandboxTools } from './puzzle-state';
import { optionsWith } from './scenario-settings';

type Outcome = NonNullable<Draft['outcome']>;
type Rules = NonNullable<Draft['rules']>;
type Judge = NonNullable<Outcome['judge']>;

const KINDS: readonly ScenarioKind[] = ['tutorial', 'puzzle', 'sandbox'];

/** The kinds a scenario can be, as the server names them (a server before it says: the three). */
export function kindChoices(state: DraftState): readonly ScenarioKind[] {
  return state.kinds ?? KINDS;
}

/* ------------------------------------------------------------------ a sandbox's tools */

/** The tools to tick: each name, whether it is in force, and whether it is not built (shown, not offered). */
export interface ToolTick {
  name: string;
  on: boolean;
  notBuilt: boolean;
}

export function toolTicks(tools: SandboxTools): ToolTick[] {
  return tools.names.map((name) => ({
    name,
    on: tools.allowed.includes(name),
    notBuilt: tools.notBuilt.includes(name),
  }));
}

/**
 * `options.tools` with one tool ticked or not: every tool ticked is the field left out of the file (null), none is
 * `false`, else the names in the server's order ("add, move").
 */
export function toolsValue(tools: SandboxTools, name: string, on: boolean): string | false | null {
  const ticked = tools.names.filter((each) => (each === name ? on : tools.allowed.includes(each)));
  if (ticked.length === tools.names.length) return null;
  if (ticked.length === 0) return false;
  return ticked.join(', ');
}

/** The options with a tool ticked or not (null: no options left). */
export function optionsWithTool(draft: Draft, tools: SandboxTools, name: string, on: boolean): DraftOptions | null {
  return optionsWith(draft, 'tools', toolsValue(tools, name, on));
}

/* ------------------------------------------------------------------ take-backs */

/** What take-backs are when the file says nothing: off in a puzzle, on in anything else. */
export function undoByKind(kind: ScenarioKind | undefined): 'on' | 'off' {
  return kind === 'puzzle' ? 'off' : 'on';
}

/** The take-back choices: as the kind has it (saying which that is), on, off. */
export function undoOptions(kind: ScenarioKind | undefined): (readonly [string, string, string])[] {
  return [
    [
      '',
      `default (${undoByKind(kind)})`,
      `As the kind has it: off in a puzzle, on otherwise. Here ${undoByKind(kind)}.`,
    ],
    ['on', 'on', 'The player may take back what they have done.'],
    ['off', 'off', 'The player may not take anything back.'],
  ];
}

/** The options with take-backs chosen ('' as the kind has it: the key left out). */
export function optionsWithUndo(draft: Draft, choice: string): DraftOptions | null {
  return optionsWith(draft, 'undo', choice === 'on' || choice === 'off' ? choice : null);
}

/* ------------------------------------------------------------------ the judge */

/** The judge as its row shows it: '' for none. */
export interface JudgeForm {
  moment: JudgeMomentKind | '';
  phase: string;
  turn: number | null;
}

/** Whether a moment takes a phase. */
export const momentTakesPhase = (moment: string): boolean => moment === 'startOf' || moment === 'endOf';

/** What each moment is called in its select, and what it is. */
export const MOMENT_LABELS: Readonly<Record<string, readonly [string, string] | undefined>> = {
  startOf: ['start of', 'As the phase begins, before its "at the start of" triggers are answered'],
  endOf: ['end of', 'As the phase ends, before its "at the end of" triggers are answered'],
  endOfTurn: ['end of turn', 'As the turn ends'],
  skirmishResolution: [
    'skirmish resolution',
    'As the first skirmish is resolved: strengths compared, nobody yet wounded or overwhelmed for it',
  ],
};

/** The moment as the file writes it ("endOf(archery), turn(2)"); null for none. */
export function judgeAt(form: JudgeForm): string | null {
  if (form.moment === '') return null;
  const at = momentTakesPhase(form.moment) ? `${form.moment}(${form.phase})` : form.moment;
  return form.turn === null ? at : `${at}, turn(${form.turn})`;
}

/** The outcome as the draft has it, to change. */
const outcomeOf = (draft: Draft): Record<string, unknown> => ({ ...draft.outcome });
const outcomeOrNull = (outcome: Record<string, unknown>): Outcome | null =>
  Object.keys(outcome).length === 0 ? null : outcome;

/** The draft's judge's requirements as the file has them (JSON), or undefined for none. */
function judgeWinOf(draft: Draft): Exclude<Judge, string>['win'] | undefined {
  const judge = draft.outcome?.judge;
  return judge !== undefined && typeof judge === 'object' ? judge.win : undefined;
}

/**
 * The outcome with the judge changed: its moment (`at`, null taking the judge out) and/or its requirements (`win`:
 * HJSON text, blank for none, sent as the server takes a step's requirements; undefined: as the draft has them). A
 * judge with no requirements is the moment alone (`judge: endOfTurn`).
 */
export function outcomeWithJudge(draft: Draft, at: string | null, win?: string): Outcome | null {
  const outcome = outcomeOf(draft);
  if (at === null) {
    Reflect.deleteProperty(outcome, 'judge');
    return outcomeOrNull(outcome);
  }
  const requirements = win === undefined ? judgeWinOf(draft) : win.trim() === '' ? undefined : win;
  outcome['judge'] = requirements === undefined ? at : { at, win: requirements };
  return outcomeOrNull(outcome);
}

/* ------------------------------------------------------------------ the score */

/** The score as its row shows it: what is read ('' for none, 'custom' for a card definition's value), of what. */
export interface ScoreForm {
  what: string;
  /** The card a per-card value reads, or the seat a hand is counted for; '' for none. */
  of: string;
  better: 'higher' | 'lower';
  label: string;
  /** A custom value's HJSON text. */
  text: string;
}

/** The score row of the state's score. */
export function scoreForm(score: PuzzleScore | null): ScoreForm {
  if (score === null) return { what: '', of: '', better: 'higher', label: '', text: '' };
  const label = score.label ?? '';
  if (score.value === null) return { what: 'custom', of: '', better: score.better, label, text: score.valueText ?? '' };
  const [what, ...of] = score.value.replace(/^\{|\}$/g, '').split(':');
  return { what: what ?? '', of: of.join(':'), better: score.better, label, text: '' };
}

/** Whether a value reads a card (strength:x): the state's perCard. */
export const readsCard = (what: string, perCard: readonly string[]): boolean => perCard.includes(what);

/**
 * The score as the outcome writes it, or null for none: the value as message text quotes the game, without its
 * braces ("strength:smeagol"), `better` only when lower (higher is the default), the label when there is one. A
 * custom value is kept as the draft has it (its text goes as text: see outcomeTextWithScore).
 */
export function scoreValue(draft: Draft, form: ScoreForm): Outcome['score'] | null {
  if (form.what === '') return null;
  const kept = draft.outcome?.score?.value;
  const value =
    form.what === 'custom' ? (kept ?? '') : form.of.trim() === '' ? form.what : `${form.what}:${form.of.trim()}`;
  const label = form.label.trim();
  return {
    value,
    ...(form.better === 'lower' ? { better: 'lower' as const } : {}),
    ...(label !== '' ? { label } : {}),
  };
}

/** The outcome with the score changed (none: taken out). */
export function outcomeWithScore(draft: Draft, form: ScoreForm): Outcome | null {
  const outcome = outcomeOf(draft);
  const score = scoreValue(draft, form);
  if (score === null) Reflect.deleteProperty(outcome, 'score');
  else outcome['score'] = score;
  return outcomeOrNull(outcome);
}

/**
 * The whole outcome as HJSON text (`topText: {outcome}`), for a score whose value is a card definition's object typed
 * as HJSON (the page reads no HJSON): every other part of the outcome as JSON, a member to a line (HJSON reads JSON),
 * and the score with the value as typed, on lines of its own.
 */
export function outcomeTextWithScore(draft: Draft, form: ScoreForm): string {
  const outcome = outcomeOf(draft);
  Reflect.deleteProperty(outcome, 'score');
  const label = form.label.trim();
  const lines = Object.entries(outcome).map(([name, value]) => `${JSON.stringify(name)}: ${JSON.stringify(value)}`);
  lines.push(
    '"score": {',
    `"value": ${form.text.trim()}`,
    ...(form.better === 'lower' ? ['"better": "lower"'] : []),
    ...(label !== '' ? [`"label": ${JSON.stringify(label)}`] : []),
    '}',
  );
  return `{\n${lines.join('\n')}\n}`;
}

/* ------------------------------------------------------------------ what the person may not do */

/** What each forbidden thing's tick says, and its tooltip. */
export const FORBID_LABELS: Readonly<Record<string, readonly [string, string]>> = {
  shuffle: ['no shuffling', 'The player may not have their draw deck shuffled, by anything: doing it fails the puzzle'],
  reveal: ['no revealing', 'The player may not reveal a card by a card of theirs: doing it fails the puzzle'],
};

/** The rules with one thing forbidden or not ("shuffle, reveal", in the server's order; none: the key goes). */
export function rulesWithForbid(
  draft: Draft,
  choices: readonly string[],
  forbidden: readonly string[],
  what: string,
  on: boolean,
): Rules | null {
  const now = choices.filter((each) => (each === what ? on : forbidden.includes(each)));
  const rules: Record<string, unknown> = { ...draft.rules };
  if (now.length === 0) Reflect.deleteProperty(rules, 'forbid');
  else rules['forbid'] = now.join(', ');
  return Object.keys(rules).length === 0 ? null : rules;
}

/* ------------------------------------------------------------------ the pool */

/** A filter typed (the pool, a slot's bound): blank is none. */
export function filterValue(typed: string): string | null {
  const filter = typed.trim();
  return filter === '' ? null : filter;
}

/** Whether the draft is a puzzle, or has a puzzle's parts (the pool, slots, solutions): the puzzle's forms show. */
export function puzzleLike(draft: Draft): boolean {
  return (
    draft.kind === 'puzzle' ||
    draft.pool !== undefined ||
    draft.slots !== undefined ||
    (draft.solutions?.length ?? 0) > 0
  );
}

/** What the server says of a filter beside its field: how many cards it finds, or why it was refused. */
export type FilterVerdict =
  { kind: 'count'; total: number } | { kind: 'refused'; why: string } | { kind: 'none' } | { kind: 'busy' };

/** A verdict's words, and its tone. */
export function verdictText(verdict: FilterVerdict): { text: string; tone: 'good' | 'error' | 'quiet' } | null {
  switch (verdict.kind) {
    case 'count':
      return {
        text: verdict.total === 0 ? 'no card' : `${verdict.total} card${verdict.total === 1 ? '' : 's'}`,
        tone: verdict.total === 0 ? 'error' : 'good',
      };
    case 'refused':
      return { text: 'refused', tone: 'error' };
    case 'busy':
      return { text: 'looking…', tone: 'quiet' };
    default:
      return null;
  }
}

/**
 * The options with who plays a sandbox's other seat chosen (options.otherSeat, ef-notes.md A): the script is the key
 * left out; the bot, or the person.
 */
export function optionsWithOtherSeat(draft: Draft, choice: string): DraftOptions | null {
  return optionsWith(draft, 'otherSeat', choice === 'bot' || choice === 'person' ? choice : null);
}

/** Who can play a sandbox's other seat, as its select offers them: [value, word, tooltip]. */
export function otherSeatOptions(choices: readonly string[]): (readonly [string, string, string])[] {
  const words: Readonly<Record<string, readonly [string, string]>> = {
    script: ['script', 'The file’s opponent: its steps, orders and fallback'],
    bot: ['bot', 'Gemp’s bot, where steps and orders leave it (it plays weakly)'],
    person: ['you', 'Nobody: its decisions wait for the person, who plays it from the other side (flip table)'],
  };
  return choices.map((choice) => [choice, ...(words[choice] ?? [choice, choice])] as const);
}
