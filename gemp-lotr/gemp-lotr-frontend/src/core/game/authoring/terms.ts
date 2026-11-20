/**
 * The scenario's lines of terms ("freeps, action, phase(fellowship)"), as the panel reads them. Pure.
 */
import { DraftCondition, DraftState, DraftWhen } from './authoring-state';

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
const CALLED = ['phase', 'turn', 'as', 'skirmish', 'offers', 'inSkirmish', 'after', 'text'] as const;
const SIMPLE = new Set<string>(['seat', 'decision', 'fighting', 'terms', 'require', 'kind', ...CALLED]);

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
  // (what the decision is for: "nextSkirmish", "not(reconcile)", as its terms say it)
  if (typeof written['kind'] === 'string' && written['kind'] !== '') terms.push(written['kind']);
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

/** The composer's pickers: the seat, the kind of decision, the phase, the turn and the role (A3: `as(shadow)`). */
export type WhenPicker = 'seat' | 'kind' | 'phase' | 'turn' | 'role';

/** The roles a seat can be playing (`as(freeps)`: the Free Peoples player, whose turn it is). */
export const ROLES = ['freeps', 'shadow'] as const;

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
  role: called('as'),
};

/** Where a term goes in a line of terms: seat, kind, phase, turn, skirmish, the rest (the order a person says them). */
export function rank(term: string): number {
  if ((SEATS as readonly string[]).includes(term.toLowerCase())) return 0;
  if ((TYPES as readonly string[]).includes(term)) return 1;
  // (what is being asked comes after the kind of decision, as the server writes a new step's When: Sitting.whenFor)
  if (askedTerm(term) !== null) return 1.5;
  if (/^phase\(/i.test(term)) return 2;
  if (/^turn\(/i.test(term)) return 3;
  // (the role after the turn, as the server writes it: Sitting.whenFor)
  if (/^as\(/i.test(term)) return 3.5;
  if (/^skirmish\(/i.test(term)) return 4;
  return 5;
}

/**
 * A When with one picker's term put in (`value` '' takes it out), its other terms kept, and the line put in order. The
 * phase and turn pickers give the value inside phase(...) and turn(...).
 */
export function putTerm(when: string | null | undefined, picker: WhenPicker, value: string): string {
  const kept = splitTerms(when).filter((term) => PICKED[picker](term) === null);
  if (value !== '')
    kept.push(
      picker === 'phase' || picker === 'turn' ? `${picker}(${value})` : picker === 'role' ? `as(${value})` : value,
    );
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
    role: found('role').toLowerCase(),
  };
}

/** What a step that repeats is likely to repeat until: the phase after the one its When names, or the next turn. */
export function nextUntil(when: string | null | undefined): string {
  const text = when ?? '';
  // (the game's own opening, which has no phase or turn: its next decision, the same seat's; after a mulligan, turn 1)
  const opening = splitTerms(text)
    .map(askedTerm)
    .find((one) => one !== null && !one.not && OPENING_ASKED.includes(one.asked));
  if (opening) {
    const next = OPENING_ASKED[OPENING_ASKED.indexOf(opening.asked) + 1];
    const seat = splitTerms(text).find((term) => (SEATS as readonly string[]).includes(term.toLowerCase()));
    return next === undefined ? 'phase(fellowship)' : [seat, next].filter(Boolean).join(', ');
  }
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

/* ---------------------------------------------------------------- what is being asked (B5) */

/**
 * What a decision can be for, as a When names it (DecisionKinds on the server): the choice of the next skirmish, an
 * archery wound, ... `phaseActions` is a phase's own "play something or pass", what an action step means anyway.
 */
export const ASKED = [
  'nextSkirmish',
  'archeryWound',
  'reconcile',
  'sanctuary',
  'anotherMove',
  'optionalResponses',
  'requiredResponses',
  'chooseSite',
  'phaseActions',
  'bid',
  'goFirst',
  'startingFellowship',
  'mulligan',
] as const;
export type Asked = (typeof ASKED)[number];

/** Each, in a few words: for pickers, and a row's summary. */
export const ASKED_WORDS: Readonly<Record<Asked, string>> = {
  nextSkirmish: 'next skirmish',
  archeryWound: 'archery wound',
  reconcile: 'reconcile',
  sanctuary: 'sanctuary heal',
  anotherMove: 'another move?',
  optionalResponses: 'optional responses',
  requiredResponses: 'required responses',
  chooseSite: 'choose a site',
  phaseActions: 'play or pass',
  bid: 'the bid',
  goFirst: 'go first?',
  startingFellowship: 'starting fellowship',
  mulligan: 'mulligan?',
};

/** What is asked in the game's own opening (B10: setup.opening: game), before turn 1, in the order it comes. */
export const OPENING_ASKED: readonly Asked[] = ['bid', 'goFirst', 'startingFellowship', 'mulligan'];

/** Whether a When (or an Until) is about a decision of the game's own opening: a bid, the seat choice, ... */
export function opensTheGame(when: string | null | undefined): boolean {
  return splitTerms(when).some((term) => {
    const asked = askedTerm(term);
    return asked !== null && !asked.not && OPENING_ASKED.includes(asked.asked);
  });
}

/* ---------------------------------------------------------------- the role (A3) */

/** What a role is called: the Free Peoples player (whose turn it is), or the Shadow player. */
export const ROLE_WORDS: Readonly<Record<(typeof ROLES)[number], string>> = {
  freeps: 'Free Peoples',
  shadow: 'Shadow',
};

/** The role a When's `as(...)` names, or null for none. */
export function roleOf(when: string | null | undefined): (typeof ROLES)[number] | null {
  if (writtenInFull(when)) return null;
  const role = splitTerms(when)
    .map(PICKED.role)
    .find((one) => one !== null)
    ?.toLowerCase();
  return ROLES.find((one) => one === role) ?? null;
}

/** One of them as named (any case), as the list writes it; null if it is none. */
export function askedNamed(name: string | null | undefined): Asked | null {
  const wanted = (name ?? '').trim().toLowerCase();
  return ASKED.find((one) => one.toLowerCase() === wanted) ?? null;
}

/** A term that says what the decision is for, or is not: `nextSkirmish`, `not(reconcile)`. Null for any other term. */
export function askedTerm(term: string): { asked: Asked; not: boolean } | null {
  const negated = /^not\(\s*([A-Za-z]+)\s*\)$/i.exec(term.trim());
  const asked = askedNamed(negated ? negated[1] : term);
  return asked === null ? null : { asked, not: negated !== null };
}

/** The term for it: `nextSkirmish`, or `not(nextSkirmish)`. */
export function askedText(asked: Asked, not: boolean): string {
  return not ? `not(${asked})` : asked;
}

/** What the "what is being asked" picker shows for a When or Until (the first such term); null when written in full. */
export function askedOf(when: string | null | undefined): { asked: Asked | ''; not: boolean } | null {
  if (writtenInFull(when)) return null;
  const found = splitTerms(when)
    .map(askedTerm)
    .find((one) => one !== null);
  return found ?? { asked: '', not: false };
}

/**
 * A When with what is being asked put in (`asked` '' takes it out), every other such term taken out, the rest kept,
 * and the line put in order (that of a new step's When on the server: seat, kind, what is asked, phase, turn, ...).
 */
export function putAsked(when: string | null | undefined, asked: Asked | '', not: boolean): string {
  const kept = splitTerms(when).filter((term) => askedTerm(term) === null);
  if (asked !== '') kept.push(askedText(asked, not));
  return [...kept].sort((a, b) => rank(a) - rank(b)).join(', ');
}

/** What a When says of what is asked, as words ("next skirmish", "not reconcile"); null if it says nothing of it. */
export function askedWords(term: string): string | null {
  const found = askedTerm(term);
  return found === null ? null : `${found.not ? 'not ' : ''}${ASKED_WORDS[found.asked]}`;
}

/* ---------------------------------------------------------------- what the composer offers */

/** What the When composer needs to know of the draft: both seats take turns (A3), the game plays its own opening (B10). */
export interface WhenFacts {
  twoSided: boolean;
  opening: boolean;
}

/** The draft's facts for the composer (none without a draft). */
export function whenFacts(state: DraftState | null | undefined): WhenFacts {
  return {
    twoSided: state?.twoSided === true,
    opening: (state?.start?.opening ?? state?.draft.setup?.opening) === 'game',
  };
}
