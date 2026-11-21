/**
 * The Scenario tab's game rules that the scenario engine added: the format the scenario is played in, and whether the
 * Shadow seat's marker keeps level with the fellowship's in a one-sided game (rules.markers). Pure.
 */
import { Draft } from './authoring-state';
import { Rules, rulesWithOneSided } from './scenario-settings';

/** The formats made for scenarios, with what each is for (the server gives no list of its formats). */
export const SCENARIO_FORMATS: readonly (readonly [code: string, label: string])[] = [
  ['scenario', 'scenario (sites with numbers)'],
  ['scenario_shadows', 'scenario_shadows (sites without)'],
];

/** The format choices: the scenarios' own, and the one the file names if it is another (kept as it is). */
export function formatChoices(current: string | undefined): (readonly [string, string])[] {
  const choices = [...SCENARIO_FORMATS];
  if (current !== undefined && current !== '' && !choices.some(([code]) => code === current)) {
    choices.push([current, current]);
  }
  return choices;
}

/** The format the file names (scenario when it names none). */
export function formatOf(draft: Draft): string {
  return draft.format ?? 'scenario';
}

function withoutMarkers(rules: Rules): Rules {
  const next = { ...rules };
  delete next.markers;
  return next;
}

/** The rules with "one-sided" ticked or not: a game of two sides has no markers keeping together. */
export function rulesOneSided(draft: Draft, on: boolean): Rules | null {
  const rules = rulesWithOneSided(draft, on);
  if (on || rules?.markers === undefined) return rules;
  return rulesWithOneSided({ ...draft, rules: withoutMarkers(rules) }, false);
}

/** The rules with "markers keep together" ticked or not (the default, apart, is not written). */
export function rulesWithMarkers(draft: Draft, together: boolean): Rules | null {
  const rest = withoutMarkers(draft.rules ?? {});
  const rules: Rules = together ? { ...rest, markers: 'together' } : rest;
  return Object.keys(rules).length === 0 ? null : rules;
}

/**
 * The opponent's leftovers that a change of sides would have the server refuse, or null: they go to a character of
 * the Free Peoples seat's in a one-sided game, and of the person's seat's in a two-sided one (ringBearer is whoever
 * bears the Ring, either way).
 */
export function leftoversLost(draft: Draft, oneSided: boolean): string | null {
  const leftovers = draft.opponent?.leftovers;
  if (leftovers === undefined || leftovers === 'ringBearer') return null;
  const seat = oneSided ? 'freeps' : (draft.human ?? 'freeps');
  return draft.cards?.[seat]?.[leftovers] === undefined ? leftovers : null;
}
