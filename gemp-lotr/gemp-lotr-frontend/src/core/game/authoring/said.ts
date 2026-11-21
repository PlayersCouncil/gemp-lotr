/**
 * Which of a step's messages were left out at the decision on the table because their condition (`require`, B6) did
 * not hold. The server leaves such a message out of the step it puts up and marks each one it does say with its place
 * among the step's messages in the file (`index`, which the page reads into ScenarioMessage.index). Where only how
 * many were said is known, that tells only some cases apart. Pure.
 */

/** What the table shows of the step: the places of the messages said, if known, and how many were said. */
export interface ShownMessages {
  indexes: readonly number[] | null;
  count: number;
}

/** The messages left out (their places, from 0), and whether that is known or only may be so. */
export interface LeftOut {
  places: ReadonlySet<number>;
  sure: boolean;
}

const NONE: LeftOut = { places: new Set(), sure: true };

/**
 * The step's messages left out at the decision on the table. `conditional`: for each of the step's messages, whether
 * it has a condition. With the places said, exactly those not said; with only a count, the messages with a condition
 * when as many are missing as have one (all of them), and those that may be when fewer are (not sure); nothing when
 * nothing is missing, or more are than have a condition (said before, a step put up again: not their conditions).
 */
export function leftOutOf(conditional: readonly boolean[], shown: ShownMessages | null): LeftOut {
  if (shown === null) return NONE;
  if (shown.indexes !== null) {
    const said = new Set(shown.indexes);
    // (only a message with a condition is ever left out: one without that isn't said is newer than the step on the
    // table, which its author has just changed and the server has yet to put up again)
    return { places: new Set(conditional.flatMap((has, i) => (has && !said.has(i) ? [i] : []))), sure: true };
  }
  const missing = conditional.length - shown.count;
  const withCondition = conditional.flatMap((has, i) => (has ? [i] : []));
  if (missing <= 0 || missing > withCondition.length) return NONE;
  return { places: new Set(withCondition), sure: missing === withCondition.length };
}

/**
 * The places in the file of the messages a step event said (ScenarioMessage.index): exact, also for a step that said
 * none of them; null where a message doesn't say (an event from before messages could be left out: the count tells
 * what it can).
 */
export function indexesOf(messages: readonly { readonly index: number | null }[]): number[] | null {
  const places = messages.map((message) => message.index);
  return places.every((place): place is number => place !== null) ? places : null;
}
