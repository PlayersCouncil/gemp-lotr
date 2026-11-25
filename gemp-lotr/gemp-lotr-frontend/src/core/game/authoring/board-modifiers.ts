/**
 * The Board tab's model of the modifiers put on a card by hand (em-notes.md 1): what a card can be modified in (the
 * state's Modify… vocabulary, the words whose card types include the card's), the board change that puts one on
 * (`modify(aragorn, strength, +2, turn)`) or takes them off (`unmodify(aragorn, strength)`), gathered as the tab's
 * other changes are and sent to the board as it stands or to the one the scenario starts with (setup.modifiers),
 * and what is on a card at either. Pure.
 */
import { BoardArg } from './board-queue';
import { CardModifier, ModifyVocabulary, ModifyWord, StartModifier } from './puzzle-state';

/** The words a card of this type can be modified in, stats first (the vocabulary's order). */
export function wordsFor(vocabulary: ModifyVocabulary | undefined, type: string): ModifyWord[] {
  const words = vocabulary?.words ?? [];
  return [...words.filter((word) => word.kind === 'stat'), ...words.filter((word) => word.kind !== 'stat')].filter(
    (word) => word.types.includes(type),
  );
}

/** How long a modifier lasts, as its select offers it ('' until it is taken off: nothing is written). */
export const UNTIL_CHOICES: readonly (readonly [string, string, string])[] = [
  ['', 'until removed', 'Until it is taken off'],
  ['turn', 'this turn', 'To the end of the turn (at the start: the turn the game starts in)'],
  ['phase', 'this phase', 'To the end of the phase (each skirmish is its own; at the start: the phase it starts in)'],
];

/** The amount a word starts with in the form: +1 for a stat, 1 for a numbered keyword, nothing for the others. */
export function defaultAmount(word: ModifyWord | undefined): string {
  if (word === undefined || word.number === 'none') return '';
  return word.number === 'signed' ? '+1' : '1';
}

/**
 * The board change putting a modifier on a card, as its arguments (`modify` gathers them), or what is wrong with the
 * amount: a stat's change has its sign and is not 0 ("+2", "-1"); a numbered keyword's is 1 or more ("+1"); a keyword
 * with no number has none. How long comes last, and not at all until it is taken off.
 */
export function modifyArgs(
  alias: string,
  word: ModifyWord,
  typed: string,
  until: string,
): { args: BoardArg[] } | { problem: string } {
  const args: BoardArg[] = [alias, word.what];
  if (word.number !== 'none') {
    const amount = Number.parseInt(typed.trim(), 10);
    if (Number.isNaN(amount) || (word.number === 'signed' ? amount === 0 : amount < 1)) {
      return {
        problem:
          word.number === 'signed'
            ? `${word.label} needs the amount it is changed by, other than 0: +2, -1`
            : `${word.label}'s number is 1 or more`,
      };
    }
    args.push(amount > 0 ? `+${amount}` : String(amount));
  }
  if (until !== '') args.push(until);
  return { args };
}

/** The board change taking a card's modifiers off: those of one kind (`what`), or all (null). */
export function unmodifyArgs(alias: string, what: string | null): BoardArg[] {
  return what === null ? [alias] : [alias, what];
}

/** A modifier on a card as the tab lists it: what it is, its words, how long, and what taking it off takes. */
export interface ShownModifier {
  what: string;
  text: string;
  until: string;
  /** Gathered, not yet sent: its place among the gathered changes (its × lets go of it); null for one made. */
  op: number | null;
  /** A change gathered takes it off (`unmodify` of it, or of all the card's). */
  removing: boolean;
}

const signed = (amount: number | null) => (amount === null ? '' : ` ${amount > 0 ? '+' : ''}${amount}`);

/** A modifier's words, from the vocabulary's label ("Strength +2"). */
export function modifierText(vocabulary: ModifyVocabulary | undefined, what: string, amount: number | null): string {
  const label = vocabulary?.words.find((word) => word.what === what)?.label ?? what;
  return `${label}${signed(amount)}`;
}

/**
 * What is on a card: at the board as it stands, its modifiers in force; at the board the scenario starts with, the
 * setup's for it. Then the ones gathered for it and not yet sent (`modify` changes about it).
 */
export function shownModifiers(
  vocabulary: ModifyVocabulary | undefined,
  inForce: readonly CardModifier[] | undefined,
  setup: readonly StartModifier[] | undefined,
  inSetup: boolean,
  gathered: readonly { name: string; args: readonly BoardArg[] }[],
  alias: string,
): ShownModifier[] {
  const made: ShownModifier[] = inSetup
    ? (setup ?? []).map((one) => ({
        what: one.what,
        text: modifierText(vocabulary, one.what, one.amount),
        until: one.until,
        op: null,
        removing: false,
      }))
    : (inForce ?? []).map((one) => ({ what: one.what, text: one.text, until: one.until, op: null, removing: false }));
  for (const op of gathered) {
    if (op.name !== 'unmodify' || op.args[0] !== alias) continue;
    for (const one of made) if (op.args[1] === undefined || op.args[1] === one.what) one.removing = true;
  }
  gathered.forEach((op, index) => {
    if (op.name !== 'modify' || op.args[0] !== alias) return;
    const what = String(op.args[1]);
    const rest = op.args.slice(2).map(String);
    const amount = rest[0] !== undefined && /^[+-]?\d+$/.test(rest[0]) ? Number.parseInt(rest[0], 10) : null;
    const until = rest.find((part) => part === 'phase' || part === 'turn') ?? 'removed';
    made.push({ what, text: modifierText(vocabulary, what, amount), until, op: index, removing: false });
  });
  return made;
}

/** The mark beside a modifier that runs out: "(turn)", "(phase)"; none for one until taken off. */
export function untilMark(until: string): string {
  return until === 'turn' || until === 'phase' ? `(${until})` : '';
}
