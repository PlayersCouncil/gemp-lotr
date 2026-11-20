/**
 * What a step must refuse (its `refuses`, B3) as the Step tab shows it: how each refusal stands at the decision in
 * hand, in a word, and a click on the table the step did not take, as a refusal to add. Pure.
 */
import { AuthoringDecision, DecisionRefusal } from './authoring-state';
import { refusalLines } from './step-model';
import { splitTerms } from './terms';

/** Each way a refusal can stand at the decision in hand: a short word, and what it means. */
export const REFUSAL_NOW: Readonly<Record<DecisionRefusal['now'], readonly [word: string, title: string]>> = {
  refused: ['refused', 'Offered here, and Allow refuses it, as it should.'],
  allowed: ['allowed!', 'Offered here, and Allow lets it through: Check would fail on this.'],
  notOffered: ['not offered', 'The decision here does not offer it, so it proves nothing here.'],
  elsewhere: ['other decision', "It is about another of the step's decisions."],
};

/** A refusal's word and title, for its badge. */
export function refusalBadge(refusal: DecisionRefusal): { word: string; title: string } {
  const [word, title] = REFUSAL_NOW[refusal.now];
  const at = refusal.at > 0 ? ` (decision ${refusal.at} of the step)` : '';
  return { word, title: `${refusal.refusal}${at}: ${title}` };
}

/** What a click on the table that the step did not take is, as a refusal: the guide's hint (null: not a click). */
export interface RefusedClick {
  cardId: string | null;
  button: string | null;
  option: number | null;
}

const realCard = (cardId: string) => (cardId.startsWith('extra') ? cardId.substring(5) : cardId);

/**
 * The answer a click the step refused would have been, in the scenario's words: an action (play(x), use(x)), a card
 * chosen (choose(x)), Pass, one of a choice's options. Null when it can't be said (a card without a name, a button
 * that is not Pass).
 */
export function refusalTerm(asked: AuthoringDecision | null, click: RefusedClick | null): string | null {
  if (!asked || !click) return null;
  if (click.button !== null) return click.button === 'Pass' && asked.pass === true ? 'pass' : null;
  if (click.option !== null) return asked.options[click.option]?.term ?? null;
  if (click.cardId === null) return null;
  const id = realCard(click.cardId);
  const offer = asked.options.find((option) => option.id === id);
  if (offer?.term) return offer.term;
  const alias = offer?.alias ?? null;
  if (alias === null) return null;
  return asked.type === 'action' || asked.type === 'actionChoice' ? null : `choose(${alias})`;
}

/** Whether a term is one of a step's refusals already (on any line, numbered or not). */
export function refuses(text: string | undefined, term: string): boolean {
  return refusalLines(text).some((line) => splitTerms(line.replace(/^\d+\s*:\s*/, '')).includes(term));
}

/** A step's refusals with one more (on a line of its own); as they are if it is already one of them. */
export function withRefusal(text: string | undefined, term: string): string {
  const lines = refusalLines(text);
  return (refuses(text, term) ? lines : [...lines, term]).join('\n');
}

/** A line's decision number ("2: x" is 2), 0 for a line about every decision. */
const lineAt = (line: string): number => Number(/^(\d+)\s*:/.exec(line)?.[1] ?? 0);

/**
 * Whether a term is refused at the step's decision `at` already (from 1; 0: any): on a line about every decision, or
 * on that decision's own line.
 */
export function refusesAt(text: string | undefined, term: string, at: number): boolean {
  if (at < 1) return refuses(text, term);
  return refusalLines(text).some(
    (line) => (lineAt(line) === 0 || lineAt(line) === at) && splitTerms(line.replace(/^\d+\s*:\s*/, '')).includes(term),
  );
}

/**
 * A click the step did not take, as the refusal to add: for a step that repeats, about the decision in hand
 * (`"2: choose(boromir)"`, the runner's way of counting them); else about every decision. As they are if it is one
 * already.
 */
export function withRefusalAt(text: string | undefined, term: string, at: number): string {
  if (at < 1) return withRefusal(text, term);
  const lines = refusalLines(text);
  return (refusesAt(text, term, at) ? lines : [...lines, `${at}: ${term}`]).join('\n');
}
