/**
 * How the game opens (B10, `setup.opening`): laid out as the setup says (`board`, the default, not written), or the
 * game's own opening played (`game`: the bid, the choice of going first, the starting fellowships and the opening
 * draw are decisions the script answers). With `game` the setup can say nothing but the order of each seat's deck;
 * switching to it takes out everything else, and the Board tab says what goes first. Pure.
 */
import { Draft, DraftState } from './authoring-state';
import { isOpening, stepsOf } from './steps';
import { opensTheGame } from './terms';

export type Opening = 'board' | 'game';
type Setup = NonNullable<Draft['setup']>;

/** What the Board tab's select offers, with a word each. */
export const OPENING_WORDS: Readonly<Record<Opening, string>> = {
  board: 'with the board laid out',
  game: 'with the game’s own opening',
};

/** How the draft opens. */
export function openingOf(state: DraftState): Opening {
  return (state.start?.opening ?? state.draft.setup?.opening) === 'game' ? 'game' : 'board';
}

/** The parts of a setup a game that plays its own opening refuses, as the author knows them. */
const PARTS: readonly (readonly [part: keyof Setup, words: string])[] = [
  ['startAt', 'where the story starts'],
  ['twilight', 'the twilight pool'],
  ['attach', 'cards attached'],
  ['stack', 'cards stacked'],
  ['wounds', 'wounds'],
  ['tokens', 'culture tokens'],
  ['burdens', 'burdens'],
  ['threats', 'threats'],
  ['skirmishes', 'skirmishes assigned'],
  ['controlled', 'sites controlled'],
  ['path', 'whose sites are laid'],
];

const SEAT_WORDS = { freeps: 'Free Peoples', shadow: 'Shadow' } as const;

/**
 * The setup for a game that plays its own opening: `opening: game` and each seat's deck order, nothing else; and
 * what that leaves out, in words (none: nothing is lost).
 */
export function setupForGame(setup: Setup | undefined): { setup: Setup; lost: string[] } {
  const lost: string[] = [];
  const next: Setup & Record<string, unknown> = { opening: 'game' };
  for (const seat of ['freeps', 'shadow'] as const) {
    const zones = setup?.[seat] ?? {};
    for (const [zone, cards] of Object.entries(zones)) {
      if (zone !== 'deck') lost.push(`${SEAT_WORDS[seat]} ${zone}: ${cards}`);
    }
    if (zones['deck'] !== undefined) next[seat] = { deck: zones['deck'] };
  }
  for (const [part, words] of PARTS) {
    const value = setup?.[part];
    if (value !== undefined && !(typeof value === 'object' && Object.keys(value).length === 0)) {
      lost.push(words);
    }
  }
  return { setup: next, lost };
}

/** The setup for a game laid out as it says: as it is, without `opening` (its deck order kept). */
export function setupForBoard(setup: Setup | undefined): Setup | null {
  const next: Setup = { ...setup };
  delete next.opening;
  return Object.keys(next).length === 0 ? null : next;
}

/** The steps (from 1) that answer the game's own opening: what a laid-out board never comes to. */
export function openingSteps(state: DraftState): number[] {
  return stepsOf(state).flatMap((step, at) =>
    typeof step.when === 'string' && opensTheGame(step.when) ? [at + 1] : [],
  );
}

/** The first step, if it is the one read before the first turn (which a game playing its own opening has none of). */
export function openingStepUid(state: DraftState): string | null {
  const first = stepsOf(state)[0];
  return first !== undefined && isOpening(first) ? (state.uids[0] ?? null) : null;
}

/** "steps 1, 2 and 4": numbers said as a person would. */
export function stepWords(numbers: readonly number[]): string {
  if (numbers.length === 1) return `step ${numbers[0]}`;
  return `steps ${numbers.slice(0, -1).join(', ')} and ${numbers[numbers.length - 1]}`;
}
