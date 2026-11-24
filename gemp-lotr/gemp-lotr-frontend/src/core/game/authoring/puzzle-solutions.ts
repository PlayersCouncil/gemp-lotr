/**
 * The Scenario tab's model of a puzzle's solutions (ep-notes.md P4): what each is (its picks by name, how many
 * answers), what the runner said of each (the last Check's or save's `solutions`: it solves it, or why not; and
 * the game in hand's verdict for the solution it is played with), the picks a Record sends, and the whole
 * `solutions` part a change of a name, a note, a pick or the answers sends (`top: {solutions}`, every solution as
 * the file has it). Pure.
 */
import { Draft, DraftSolution } from './authoring-state';
import { PuzzleSolution, PuzzleState, PuzzleVerdict } from './puzzle-state';

/** A solution's name: the server's rule. */
export const SOLUTION_NAME = /^[A-Za-z][A-Za-z0-9_-]*$/;

/** What is wrong with a name for a solution (null: nothing). */
export function solutionNameProblem(name: string, taken: readonly string[], own: string | null = null): string | null {
  if (!SOLUTION_NAME.test(name)) return 'Letters, digits, dashes and underscores, starting with a letter';
  if (name !== own && taken.includes(name)) return `There is a solution called ${name} already.`;
  return null;
}

/** A name for a new solution: solution1, solution2... not taken. */
export function freshSolutionName(taken: readonly string[]): string {
  let n = taken.length + 1;
  while (taken.includes(`solution${n}`)) n++;
  return `solution${n}`;
}

/** What a solution that solves the puzzle is said to do. */
export const SOLVED = 'solved';

/**
 * What the runner said of each solution (a Check's answer, a save's: `solutions`, ef-notes.md 3), by name: SOLVED for
 * one that solves it (the server sends null), else the runner's words. Read defensively.
 */
export function solutionVerdicts(value: unknown): Map<string, string> {
  const verdicts = new Map<string, string>();
  if (value === null || typeof value !== 'object' || Array.isArray(value)) return verdicts;
  for (const [name, said] of Object.entries(value as Readonly<Record<string, unknown>>)) {
    if (said === null) verdicts.set(name, SOLVED);
    else if (typeof said === 'string') verdicts.set(name, said);
  }
  return verdicts;
}

/** A verdict's tone: solved, or not. */
export const solvedBy = (verdict: string): boolean => verdict.startsWith('solved');

/** The game in hand's verdict, said for the solution it is played with (null for the others, or before judging). */
export function verdictLine(puzzle: PuzzleState, name: string): string | null {
  const verdict: PuzzleVerdict | null = puzzle.verdict;
  if (verdict === null || puzzle.playingWith !== name) return null;
  const score = verdict.score === null ? '' : `, scoring ${verdict.score}`;
  return `${verdict.won ? 'won' : 'failed'} at ${verdict.moment}${score}`;
}

/** A solution's picks by name, slot by slot ("condition: Parapet"). */
export function picksLine(solution: PuzzleSolution): string {
  return Object.entries(solution.picks)
    .map(([slot, blueprint]) => `${slot}: ${solution.pickNames[slot] ?? blueprint}`)
    .join(', ');
}

/** "4 answers". */
export function answersCount(solution: { answers: readonly string[] }): string {
  const count = solution.answers.length;
  return `${count} answer${count === 1 ? '' : 's'}`;
}

/**
 * The picks Record sends: the game in hand's when every slot is filled as it has it (null: the server takes the game
 * in hand's), else every slot's, what has been chosen first (a slot with none is left out: the server says so).
 */
export function recordPicks(
  slots: readonly string[],
  inHand: Readonly<Record<string, string>>,
  chosen: Readonly<Record<string, string>>,
): Record<string, string> | null {
  const picks: Record<string, string> = {};
  for (const slot of slots) {
    const pick = chosen[slot] ?? inHand[slot];
    if (pick !== undefined && pick !== '') picks[slot] = pick;
  }
  const same = slots.every((slot) => picks[slot] !== undefined && picks[slot] === inHand[slot]);
  return same ? null : picks;
}

/** A change to one solution, as the file has it: its name, its note (blank: none), a pick, its answers. */
export interface SolutionChange {
  name?: string;
  note?: string;
  pick?: { slot: string; blueprint: string };
  answers?: readonly string[];
}

/** Answers typed one or more to a line: the lines with something on them. */
export function answerLines(typed: string): string[] {
  return typed
    .split('\n')
    .map((line) => line.trim())
    .filter((line) => line !== '');
}

/** The solutions part with one solution changed; every other as the draft has it. */
export function solutionsWith(draft: Draft, name: string, change: SolutionChange): DraftSolution[] {
  return (draft.solutions ?? []).map((solution) => {
    if (solution.name !== name) return solution;
    const next: Record<string, unknown> = { ...solution };
    if (change.name !== undefined) next['name'] = change.name;
    if (change.note !== undefined) {
      if (change.note.trim() === '') Reflect.deleteProperty(next, 'note');
      else next['note'] = change.note.trim();
    }
    if (change.pick !== undefined) next['picks'] = { ...solution.picks, [change.pick.slot]: change.pick.blueprint };
    if (change.answers !== undefined) next['answers'] = [...change.answers];
    return next as unknown as DraftSolution;
  });
}
