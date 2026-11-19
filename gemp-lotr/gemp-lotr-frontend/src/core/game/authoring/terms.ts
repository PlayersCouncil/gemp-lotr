/**
 * The scenario's lines of terms ("freeps, action, phase(fellowship)"), as the panel reads them. Pure.
 */
import { DraftCondition, DraftWhen } from './authoring-state';

/** The seats a When can start with. */
export const SEATS = ['freeps', 'shadow', 'human', 'opponent'] as const;
/** A turn's phases, in order. */
export const PHASES = ['fellowship', 'shadow', 'maneuver', 'archery', 'assignment', 'skirmish', 'regroup'] as const;

/** A line of terms as its terms: commas inside parentheses belong to their term; each trimmed, blanks dropped. */
export function splitTerms(text: string | null | undefined): string[] {
  const terms: string[] = [];
  let depth = 0;
  let term = '';
  for (const ch of text ?? '') {
    if (ch === '(') depth++;
    if (ch === ')') depth--;
    if (ch === ',' && depth <= 0) {
      terms.push(term.trim());
      term = '';
    } else {
      term += ch;
    }
  }
  terms.push(term.trim());
  return terms.filter((one) => one !== '');
}

/** The parts of a When written out in full that a line of terms can say too, as `name(value)`. */
const CALLED = ['phase', 'turn', 'skirmish', 'offers', 'inSkirmish', 'after', 'text'] as const;
const SIMPLE = new Set<string>(['seat', 'decision', 'fighting', 'terms', 'require', ...CALLED]);

/**
 * A When written out with requirements, as the line of terms the rest of it makes and its requirements as text (JSON,
 * tab-indented). Null for a When that is a line of terms, has no requirements, or says something terms can't.
 */
export function whenParts(when: DraftCondition | null | undefined): { terms: string; require: string } | null {
  if (when === null || when === undefined || typeof when !== 'object' || Array.isArray(when)) return null;
  const written = when as DraftWhen;
  if (written.require === undefined) return null;
  if (Object.keys(written).some((name) => !SIMPLE.has(name))) return null;
  const terms: string[] = [];
  if (written.terms) terms.push(written.terms);
  if (written.seat) terms.push(written.seat);
  if (written.decision) terms.push(written.decision);
  for (const name of CALLED) {
    const value = written[name];
    if (value !== undefined && value !== '') terms.push(`${name}(${value})`);
  }
  if (written.fighting) terms.push(`inSkirmish(${written.fighting})`);
  return { terms: terms.join(', '), require: JSON.stringify(written.require, null, '\t') };
}

/* ---------------------------------------------------------------- the Step tab's When composer */

/** The kinds of decision a When can name. */
export const TYPES = ['action', 'actionChoice', 'cards', 'arbitrary', 'choice', 'number', 'assign'] as const;

/** Whether a When (or an Until) is written out in full, as the file has it: not a line of terms to take apart. */
export function writtenInFull(text: string | null | undefined): boolean {
  return /^\s*([{[]|when\s*:)/.test(text ?? '');
}

/** The composer's pickers: the seat, the kind of decision, the phase and the turn. */
export type WhenPicker = 'seat' | 'kind' | 'phase' | 'turn';

const named =
  (names: readonly string[]) =>
  (term: string): string | null =>
    names.some((name) => name.toLowerCase() === term.toLowerCase()) ? term : null;
const called =
  (name: string) =>
  (term: string): string | null =>
    new RegExp(`^${name}\\((.*)\\)$`, 'i').exec(term)?.[1] ?? null;

/** Which term each picker reads (what the term says, or null for a term that isn't its). */
const PICKED: Readonly<Record<WhenPicker, (term: string) => string | null>> = {
  seat: named(SEATS),
  kind: named(TYPES),
  phase: called('phase'),
  turn: called('turn'),
};

/** Where a term goes in a line of terms: seat, kind, phase, turn, skirmish, the rest (the order a person says them). */
export function rank(term: string): number {
  if ((SEATS as readonly string[]).includes(term.toLowerCase())) return 0;
  if ((TYPES as readonly string[]).includes(term)) return 1;
  if (/^phase\(/i.test(term)) return 2;
  if (/^turn\(/i.test(term)) return 3;
  if (/^skirmish\(/i.test(term)) return 4;
  return 5;
}

/**
 * A When with one picker's term put in (`value` '' takes it out), its other terms kept, and the line put in order. The
 * phase and turn pickers give the value inside phase(...) and turn(...).
 */
export function putTerm(when: string | null | undefined, picker: WhenPicker, value: string): string {
  const kept = splitTerms(when).filter((term) => PICKED[picker](term) === null);
  if (value !== '') kept.push(picker === 'phase' || picker === 'turn' ? `${picker}(${value})` : value);
  return [...kept].sort((a, b) => rank(a) - rank(b)).join(', ');
}

/** What the pickers show for a When (each '' for none); null when it is written out in full (no pickers). */
export function pickersOf(when: string | null | undefined): Record<WhenPicker, string> | null {
  if (writtenInFull(when)) return null;
  const terms = splitTerms(when);
  const found = (picker: WhenPicker) => terms.map(PICKED[picker]).find((one) => one !== null) ?? '';
  const kind = found('kind');
  return {
    seat: found('seat').toLowerCase(),
    kind: TYPES.find((one) => one.toLowerCase() === kind.toLowerCase()) ?? '',
    phase: found('phase'),
    turn: found('turn'),
  };
}

/** What a step that repeats is likely to repeat until: the phase after the one its When names, or the next turn. */
export function nextUntil(when: string | null | undefined): string {
  const text = when ?? '';
  const phase = /phase\(\s*([a-z]+)\s*\)/i.exec(text);
  const turn = /turn\(\s*(\d+)\s*\)/i.exec(text);
  if (phase) {
    const at = (PHASES as readonly string[]).indexOf((phase[1] ?? '').toLowerCase());
    const after = PHASES[at + 1];
    if (at > -1 && after !== undefined) return `phase(${after})`;
  }
  return turn ? `turn(${Number(turn[1]) + 1})` : '';
}

/** The When of the first step after this one (from 0) that waits for something else: what it would repeat until. */
export function untilFromNext(steps: readonly { when?: unknown }[], index: number): string | null {
  const step = steps[index];
  if (index < 0 || step === undefined) return null;
  for (const next of steps.slice(index + 1)) {
    if (next.when !== undefined && next.when !== null && JSON.stringify(next.when) !== JSON.stringify(step.when)) {
      return typeof next.when === 'string' ? next.when : JSON.stringify(next.when, null, '\t');
    }
  }
  return null;
}
