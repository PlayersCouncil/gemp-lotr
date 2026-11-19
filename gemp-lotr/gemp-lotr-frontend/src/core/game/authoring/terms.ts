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
