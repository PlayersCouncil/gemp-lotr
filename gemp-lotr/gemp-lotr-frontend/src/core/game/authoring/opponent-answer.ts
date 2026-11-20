/**
 * The Opponent tab's answers for the opponent, in the script's words (the legacy scenarioAuthor.js renderOpponent):
 * the cards chosen, who each minion fights, a number; and what the tab says of a decision. Pure.
 */
import { AuthoringDecision, DecisionOffer, Draft } from './authoring-state';

/** The decisions answered by a button per option (the option's term). */
export function answersByButton(decision: AuthoringDecision): boolean {
  return decision.type === 'action' || decision.type === 'actionChoice' || decision.type === 'choice';
}

/** The decisions answered by ticking cards. */
export function answersByCards(decision: AuthoringDecision): boolean {
  return decision.type === 'cards' || decision.type === 'arbitrary';
}

/** Whether a Pass button is offered beside the decision's own answers (an assignment passes by assigning nobody). */
export function offersPass(decision: AuthoringDecision): boolean {
  return decision.pass === true && decision.type !== 'assign';
}

/** What a decision is told apart by: a change of any of it is another decision, whose answers start afresh. */
export function decisionKey(decision: AuthoringDecision | null): string {
  if (!decision) return '';
  const offers = (list: readonly DecisionOffer[] | undefined) =>
    (list ?? []).map((offer) => `${offer.id ?? ''}/${offer.term ?? ''}/${offer.alias ?? ''}`).join(',');
  return [
    decision.seat,
    decision.type,
    decision.text,
    decision.where,
    decision.min ?? '',
    decision.max ?? '',
    decision.step ?? '',
    decision.sub,
    offers(decision.options),
    offers(decision.characters),
  ].join('|');
}

/** What an offered card is called in the tab's lists: its label, and its name in the scenario if it has one. */
export function offerLabel(offer: DecisionOffer): string {
  return offer.alias ? `${offer.label} (${offer.alias})` : offer.label;
}

/** What one of a decision's cards is told apart by, in the tab's ticks and selects: its alias, else its id. */
export function offerKey(offer: DecisionOffer, index: number): string {
  return offer.alias ?? (offer.id !== null ? `#${offer.id}` : `@${index}`);
}

/** "Choose 1:", "Choose 1 to 3:". */
export function chooseLine(decision: AuthoringDecision): string {
  const min = decision.min ?? 0;
  const max = decision.max ?? min;
  return `Choose ${min}${max !== min ? ` to ${max}` : ''}:`;
}

/**
 * The answer that chooses these cards: none is `pass`; cards that all have names in the scenario are
 * `choose(a, b)`; otherwise the game's own ids, `raw(12,13)`.
 */
export function cardsTerm(chosen: readonly DecisionOffer[]): string {
  if (chosen.length === 0) return 'pass';
  if (chosen.every((offer) => offer.alias !== null)) return `choose(${chosen.map((offer) => offer.alias).join(', ')})`;
  return `raw(${chosen.map((offer) => offer.id ?? '').join(',')})`;
}

/**
 * The answer to an assignment: `fights` says, by the minion's key (offerKey), the key of the character it fights
 * ('' or absent: nobody). The characters come in the order their first minion does: `assign(frodo: orc + troll,
 * sam: goblin)`; nobody assigned is `pass`. A card the scenario has no name for can't be written so: then the whole
 * answer is in the game's own ids, `raw(character minion minion, character minion)`.
 */
export function assignTerm(
  minions: readonly DecisionOffer[],
  characters: readonly DecisionOffer[],
  fights: Readonly<Record<string, string>>,
): string {
  const characterBy = new Map(characters.map((character, index) => [offerKey(character, index), character]));
  const groups = new Map<DecisionOffer, DecisionOffer[]>();
  minions.forEach((minion, index) => {
    const character = characterBy.get(fights[offerKey(minion, index)] ?? '');
    if (!character) return;
    const group = groups.get(character) ?? [];
    group.push(minion);
    groups.set(character, group);
  });
  if (groups.size === 0) return 'pass';
  const pairs = [...groups];
  const named = pairs.every(([character, group]) => character.alias !== null && group.every((one) => one.alias));
  if (named) {
    return `assign(${pairs.map(([character, group]) => `${character.alias}: ${group.map((one) => one.alias).join(' + ')}`).join(', ')})`;
  }
  return `raw(${pairs.map(([character, group]) => [character, ...group].map((one) => one.id ?? '').join(' ')).join(', ')})`;
}

/** "The rest to the Ring-bearer": whoever is the Free Peoples player's Ring-bearer when the answer is given. */
export const RING_BEARER = 'ringBearer';

/**
 * Whether an assignment can be answered with "the rest to ...": every minion and character has a name in the scenario
 * (`rest` is the scenario's word, and stands beside names only).
 */
export function restOffered(minions: readonly DecisionOffer[], characters: readonly DecisionOffer[]): boolean {
  return [...minions, ...characters].every((offer) => offer.alias !== null);
}

/** The characters "the rest" can go to: the Ring-bearer, then each character by its name in the scenario. */
export function restChoices(characters: readonly DecisionOffer[]): (readonly [string, string])[] {
  return [
    ['', 'nobody'],
    [RING_BEARER, 'the Ring-bearer'],
    ...characters
      .filter((character) => character.alias !== null)
      .map((character) => [character.alias ?? '', offerLabel(character)] as const),
  ];
}

/**
 * The answer to an assignment where the minions nobody is chosen for go to `rest` (a character's name, or
 * RING_BEARER), written as the scenario says it: `assign(aragorn: orc1, frodo: rest)`, `assign(ringBearer: rest)`.
 * The minions chosen are named; whatever is left when the answer is given goes to the rest's character. With no rest
 * (''), or cards without names, it is assignTerm's answer.
 */
export function assignRestTerm(
  minions: readonly DecisionOffer[],
  characters: readonly DecisionOffer[],
  fights: Readonly<Record<string, string>>,
  rest: string,
): string {
  if (rest === '' || !restOffered(minions, characters)) return assignTerm(minions, characters, fights);
  const groups = new Map<string, string[]>();
  minions.forEach((minion, index) => {
    const character = characters.find((one, at) => offerKey(one, at) === (fights[offerKey(minion, index)] ?? ''));
    if (!character) return;
    groups.set(character.alias ?? '', [...(groups.get(character.alias ?? '') ?? []), minion.alias ?? '']);
  });
  groups.set(rest, [...(groups.get(rest) ?? []), 'rest']);
  return `assign(${[...groups].map(([character, group]) => `${character}: ${group.join(' + ')}`).join(', ')})`;
}

/** The answer that gives a number: `number(3)`; null for what is no whole number. */
export function numberTerm(text: string): string | null {
  const number = text.trim();
  return /^-?\d+$/.test(number) ? `number(${number})` : null;
}

/** "3", or "3.2" for the second of a block's steps. */
function stepName(decision: AuthoringDecision): string {
  return `${decision.step}${decision.sub > 0 ? `.${decision.sub}` : ''}`;
}

/** A line of the tab: what it says, and the rest of it as its tooltip. */
export interface TabLine {
  text: string;
  title: string;
}

/** What answers the opponent's decision when it is not held for the author. */
export function answeredBy(decision: AuthoringDecision): TabLine {
  return decision.step !== null
    ? { text: `Step ${stepName(decision)} answers it.`, title: 'Once what it says has been read' }
    : { text: 'It answers by itself in a moment.', title: 'By its standing orders, or its fallback' };
}

/** What the tab says of the player's own decision: which step applies to it. */
export function humanLine(decision: AuthoringDecision, recording: boolean): TabLine {
  if (decision.step !== null) return { text: `Step ${stepName(decision)} applies to it.`, title: '' };
  return recording
    ? { text: 'No step applies to it. Your answer becomes one.', title: 'Write down what I do is ticked' }
    : { text: 'No step applies to it.', title: '' };
}

/** What the opponent does where nothing answers for it: its fallback ("fail" unless the file says "pass"). */
export function fallbackOf(draft: Draft): string {
  return draft.opponent?.fallback ?? 'fail';
}
