/**
 * A block that is a turn's skirmishes, laid out the way they are fought (the Step tab's skirmishes page; the legacy
 * scenarioAuthor.js skirmishPage, skirmishPart, fitsHere): what comes before them (the assignment); the first choice
 * of which skirmish is fought; steps for every skirmish; and for each character's skirmish (companions first, then
 * the minions'), its steps in the order they come and what is said after it. Underneath it is a block like any
 * other: a step of a skirmish says inSkirmish(that card), a step after it after(that card). Pure.
 */
import { BoardCard, DraftState, DraftStep, DraftWhen } from './authoring-state';
import { NEW_TEXT } from './step-ops';
import { splitTerms } from './terms';

/** Where a step of a skirmishes block belongs, and whose skirmish it is (for a fight or what comes after one). */
export interface SkirmishPart {
  part: 'before' | 'first' | 'every' | 'fight' | 'after';
  who: string | null;
}

/** Where a step of a skirmishes block belongs, read off its When. */
export function skirmishPart(step: DraftStep): SkirmishPart {
  const when = step.when;
  let fighting: string | null = null;
  let after: string | null = null;
  let next = false;
  let skirmish = false;
  if (typeof when === 'string') {
    for (const term of splitTerms(when)) {
      if (/^phase\(\s*skirmish\s*\)$/i.test(term)) skirmish = true;
      const match = /^(fighting|inSkirmish|after|text)\((.*)\)$/i.exec(term);
      if (!match) continue;
      const what = (match[1] ?? '').toLowerCase();
      const arg = (match[2] ?? '').trim();
      if (what === 'fighting' || what === 'inskirmish') fighting = arg;
      else if (what === 'after') after = arg;
      else if (/next skirmish/i.test(arg)) next = true;
    }
  } else if (when !== undefined && !Array.isArray(when)) {
    const written = when as DraftWhen;
    fighting = written.inSkirmish ?? written.fighting ?? null;
    after = written.after ?? null;
    next = /next skirmish/i.test(written.text ?? '');
    skirmish = /^skirmish$/i.test(written.phase ?? '');
  }
  if (fighting !== null) return { part: 'fight', who: fighting };
  if (after !== null) return { part: 'after', who: after };
  return { part: next ? 'first' : skirmish ? 'every' : 'before', who: null };
}

/** Whether the waiting decision no step covers is one a step about to be added to the block would be for. */
export function fitsHere(state: DraftState, step: DraftStep): boolean {
  const open = state.uncovered;
  if (!open?.when) return false;
  const want = skirmishPart(step);
  const have = skirmishPart({ when: open.when });
  return want.part === have.part && want.who === have.who;
}

/** One group of the page: its steps (their places in the block, from 0) and the step its "+ Step" adds, if any. */
export interface SkirmishGroup {
  /** What it is: before, first, every, fight, after. */
  kind: SkirmishPart['part'];
  title: string;
  /** Said under the title (short; `hint` says more). */
  note: string | null;
  hint: string;
  who: string | null;
  subs: number[];
  add: { at: number; step: DraftStep; title: string } | null;
}

/** A card that can be given a skirmish of its own on the page ("+ a skirmish for…"). */
export interface FightOffer {
  alias: string;
  label: string;
}

const opposite = (seat: string) => (seat === 'freeps' ? 'shadow' : 'freeps');

/** The page's groups, for the skirmishes block at `index` (from 0). `newFights`: aliases given one before any step. */
export function skirmishGroups(
  state: DraftState,
  top: DraftStep,
  newFights: readonly string[],
): { groups: SkirmishGroup[]; offers: FightOffer[] } {
  const human = state.draft.human ?? 'freeps';
  const steps = top.anyOrder ?? [];
  const cards: readonly BoardCard[] = state.board?.cards ?? [];
  const cardOf = (alias: string) => cards.find((card) => card.alias === alias);
  // (a skirmish is the Free Peoples character's: a step for one a minion is in is for whichever skirmish that is)
  const minion = (alias: string) => {
    const card = cardOf(alias);
    return card !== undefined && card.owner !== human;
  };
  const shortName = (alias: string) => cardOf(alias)?.name.split(',')[0] ?? alias;
  const parts: Record<string, number[]> = { before: [], first: [], every: [] };
  const fights: string[] = [];
  for (const [sub, step] of steps.entries()) {
    const place = skirmishPart(step);
    if (place.who !== null) {
      if (!fights.includes(place.who)) fights.push(place.who);
      (parts[`${place.part}:${place.who}`] ??= []).push(sub);
    } else {
      (parts[place.part] ??= []).push(sub);
    }
  }
  const aliases = new Set(cards.map((card) => card.alias));
  for (const who of newFights) if (!fights.includes(who) && aliases.has(who)) fights.push(who);
  const count = steps.length;
  const before = parts['before'] ?? [];
  const first = parts['first'] ?? [];
  const every = parts['every'] ?? [];
  const firstChoice = first[0] ?? count;
  const after = (subs: readonly number[]) => {
    const last = subs[subs.length - 1];
    return last === undefined ? count : last + 1;
  };
  const groups: SkirmishGroup[] = [
    {
      kind: 'before',
      title: 'Before the skirmishes, or at any moment',
      note: 'The assignment, and the rest.',
      hint: 'The assignment, and anything else not said to be in one skirmish or after it.',
      who: null,
      subs: before,
      add: {
        at: after(before),
        step: { once: true, when: `${human}, assign` },
        title: 'A step for a decision before the skirmishes',
      },
    },
    {
      kind: 'first',
      title: 'Choosing the first skirmish',
      note: null,
      hint: 'Said when the player chooses which skirmish is fought first.',
      who: null,
      subs: first,
      add:
        first.length > 0
          ? null
          : {
              at: count,
              step: { once: true, when: `${human}, cards, text(next skirmish)` },
              title: 'A step for the first choice of skirmish',
            },
    },
  ];
  if (every.length > 0) {
    groups.push({
      kind: 'every',
      title: 'In every skirmish',
      note: null,
      hint: 'Steps for any skirmish, whoever is fighting it: after the steps of the skirmish being fought, which come first if they fit.',
      who: null,
      subs: every,
      add: null,
    });
  }
  // (the steps for one skirmish come before the ones for every skirmish, which would otherwise take its decisions)
  const firstEvery = every[0] ?? null;
  const ordered = [...fights.filter((who) => !minion(who)), ...fights.filter(minion)];
  for (const who of ordered) {
    const name = shortName(who);
    const isMinion = minion(who);
    const fight = parts[`fight:${who}`] ?? [];
    let end = fight.length > 0 ? after(fight) : (firstEvery ?? count);
    if (fight.length > 0 && firstEvery !== null && end > firstEvery) end = firstEvery;
    groups.push({
      kind: 'fight',
      title: isMinion ? `Any skirmish ${name} (${who}) is in` : `${name}'s skirmish`,
      note: null,
      hint: isMinion ? 'Steps for the skirmish this minion is in, whichever character it fights.' : '',
      who,
      subs: fight,
      add: {
        at: end,
        step: { once: true, when: `${isMinion ? opposite(human) : human}, action, inSkirmish(${who})` },
        title: 'A step for the next decision in this skirmish',
      },
    });
    const afterSubs = parts[`after:${who}`] ?? [];
    // (before the first choice's step, which would otherwise take the choices after a skirmish too)
    groups.push({
      kind: 'after',
      title: `After ${name}'s skirmish`,
      note: null,
      hint:
        "Said at the first decision after it, whatever that is: the choice of the next skirmish, the next skirmish's own, " +
        "a fierce assignment, or the first of the Regroup phase (the block's end does not cut it off).",
      who,
      subs: afterSubs,
      add:
        afterSubs.length > 0
          ? null
          : {
              at: Math.min(firstChoice, count),
              step: { once: true, pause: true, when: `${human}, after(${who})`, messages: [{ text: NEW_TEXT }] },
              title: 'A pause read at the first decision after this skirmish',
            },
    });
  }
  // Another skirmish to write steps for: a character's, or a minion's
  const offers = cards
    .filter(
      (card) =>
        !fights.includes(card.alias) &&
        (card.owner === human ? card.type === 'COMPANION' || card.type === 'ALLY' : card.type === 'MINION'),
    )
    .sort((x, y) => (x.owner === human ? 0 : 1) - (y.owner === human ? 0 : 1))
    .map((card) => ({
      alias: card.alias,
      label: `${card.alias} (${card.name}${card.owner === human ? '' : ', minion'})`,
    }));
  return { groups, offers };
}
