/**
 * What a scenario's step lets the player do next about the waiting decision: which cards may be clicked, which
 * actions taken, which buttons pressed. Pure: worked out each time from the decision, what the step allows and what
 * the player has chosen so far (the game model's local choices), so it can't fall out of step with the page.
 */
import { CardAction } from '../decision/decision-view';
import { DecisionGesture } from '../decision/decision-gate';
import { Decision } from '../protocol/game-events';
import { LocalChoices } from '../state/game-model';
import { ScenarioAllow, ScenarioMessage, ScenarioStep } from './scenario-events';

/** The messages a step opens with (the others belong to a moment inside the answering). */
export function openingMessages(step: ScenarioStep): readonly ScenarioMessage[] {
  return step.messages.filter((message) => message.on === null || message.on.moment === 'start');
}

export interface ScenarioPlan {
  /** The cards that may be clicked next. */
  liveCards: readonly string[];
  /** Whether what has been chosen is a whole allowed answer, with only the confirming click left. */
  ready: boolean;
  /** Whether passing is allowed now. */
  pass: boolean;
  /** The answers of a multiple choice that may be given (their places in the list). */
  options: readonly number[];
  /** The numbers that may be given to a decision asking for one (the bid of the game's own opening, say). */
  numbers: readonly number[];
}

/** What the plan needs of the page. */
export interface PlanContext {
  decision: Decision;
  allow: ScenarioAllow;
  local: LocalChoices;
  /** Each offered card's actions (DecisionView.actions). */
  actions: Readonly<Record<string, readonly CardAction[]>>;
}

const sets = (allow: ScenarioAllow): string[][] => [
  ...allow.answers.map((answer) => (answer === '' ? [] : answer.split(','))),
  ...(allow.pass ? [[]] : []),
];

const sameSet = (a: readonly string[], b: readonly string[]) =>
  a.length === b.length && a.every((id) => b.includes(id));

interface Pairing {
  minion: string;
  character: string;
}

/**
 * The allowed answers to an assignment as their pairings in the order the scenario wrote them, which is the order
 * they are to be clicked in: what a message says about "this one" and "now the other" is only true if the player
 * can't take them the other way round.
 */
function orders(allow: ScenarioAllow): Pairing[][] {
  const written = allow.answers.map((answer) =>
    (answer === '' ? [] : answer.split(',')).flatMap((group) => {
      const [character, ...minions] = group.trim().split(' ');
      return character === undefined ? [] : minions.map((minion) => ({ minion, character }));
    }),
  );
  return allow.pass ? [...written, []] : written;
}

/** The orders that what has been assigned so far could still become. */
function possibleOrders(allow: ScenarioAllow, assigned: Readonly<Record<string, string>>): Pairing[][] {
  return orders(allow).filter((pairs) =>
    Object.entries(assigned).every(([minion, character]) =>
      pairs.some((pair) => pair.minion === minion && pair.character === character),
    ),
  );
}

/** Whether an action (the answer of a choice of actions) has been taken. */
const chosen = (local: LocalChoices) => local.selectedCardIds.length > 0;

function liveCards({ decision, allow, local, actions }: PlanContext): string[] {
  const live = new Set<string>();
  switch (decision.type) {
    case 'CARD_ACTION_CHOICE':
      if (!chosen(local)) {
        for (const [cardId, offered] of Object.entries(actions)) {
          if (offered.some((action) => allow.answers.includes(action.actionId))) live.add(cardId);
        }
      }
      break;
    case 'ACTION_CHOICE':
      if (!chosen(local)) {
        (decision.params['actionId'] ?? []).forEach((actionId, index) => {
          if (allow.answers.includes(actionId)) live.add(`temp${index}`);
        });
      }
      break;
    case 'CARD_SELECTION':
    case 'ARBITRARY_CARDS':
      for (const set of sets(allow)) {
        if (!local.selectedCardIds.every((id) => set.includes(id))) continue;
        for (const id of set) if (!local.selectedCardIds.includes(id)) live.add(id);
      }
      break;
    case 'ASSIGN_MINIONS':
      for (const pairs of possibleOrders(allow, local.assignments)) {
        if (local.minion !== null) {
          for (const pair of pairs) if (pair.minion === local.minion) live.add(pair.character);
        } else {
          const next = pairs.find((pair) => local.assignments[pair.minion] !== pair.character);
          if (next) live.add(next.minion);
        }
      }
      break;
    default:
      break;
  }
  return [...live];
}

function ready({ decision, allow, local }: PlanContext): boolean {
  switch (decision.type) {
    case 'CARD_ACTION_CHOICE':
    case 'ACTION_CHOICE':
      return chosen(local);
    case 'CARD_SELECTION':
    case 'ARBITRARY_CARDS':
      return sets(allow).some((set) => sameSet(set, local.selectedCardIds));
    case 'ASSIGN_MINIONS':
      return (
        local.minion === null &&
        possibleOrders(allow, local.assignments).some((pairs) =>
          pairs.every((pair) => local.assignments[pair.minion] === pair.character),
        )
      );
    default:
      return false;
  }
}

export function scenarioPlan(context: PlanContext): ScenarioPlan {
  const { decision, allow, local } = context;
  const choosesAction = decision.type === 'CARD_ACTION_CHOICE' || decision.type === 'ACTION_CHOICE';
  return {
    liveCards: liveCards(context),
    ready: ready(context),
    pass: allow.pass && !(choosesAction && chosen(local)),
    options: decision.type === 'MULTIPLE_CHOICE' ? allow.answers.map(Number).filter(Number.isInteger) : [],
    numbers: decision.type === 'INTEGER' ? allow.answers.map(Number).filter(Number.isInteger) : [],
  };
}

/** Whether the step lets a gesture go ahead. */
export function planAllows(context: PlanContext, gesture: DecisionGesture): boolean {
  const plan = scenarioPlan(context);
  switch (gesture.kind) {
    case 'card':
      return plan.liveCards.includes(gesture.cardId);
    case 'action':
      return context.allow.answers.includes(gesture.actionId);
    case 'button':
      switch (gesture.button) {
        case 'Pass':
          return plan.pass;
        case 'Done':
          return plan.ready;
        default:
          // Taking a choice back is always fine (and OK is only ever the dialogs' own, asked as a number or a choice)
          return true;
      }
    case 'number':
      // (as the game reads it: "3", not "03")
      return context.allow.answers.includes(String(Number.parseInt(gesture.value, 10)));
    case 'choice':
      return plan.options.includes(gesture.index);
    case 'escape':
      return plan.ready;
  }
}

/**
 * Whether a character now has every minion that every answer still possible gives it (the moment "assigned" of the
 * character).
 */
export function characterComplete(allow: ScenarioAllow, local: LocalChoices, characterId: string): boolean {
  const possible = possibleOrders(allow, local.assignments);
  return (
    possible.length > 0 &&
    possible.every((pairs) =>
      pairs.every((pair) => pair.character !== characterId || local.assignments[pair.minion] === characterId),
    )
  );
}
