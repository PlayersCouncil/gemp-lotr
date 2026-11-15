/**
 * What a waiting decision asks of the page's player, and what the page shows for it (the legacy page's
 * integerDecision, multipleChoiceDecision, arbitraryCardsDecision, cardActionChoiceDecision, actionChoiceDecision,
 * cardSelectionDecision and assignMinionsDecision, without the drawing). Pure: computed from the model and the
 * settings, so every state of every decision can be checked without a page.
 */
import { Decision } from '../protocol/game-events';
import { ALERT_DECISIONS, GameCard, GameModel } from '../state/game-model';
import { assignmentOf } from '../state/local-choices';

/** How a card is offered: to choose it, to use one of its actions; or it is chosen already. */
export type CardMark = 'selectable' | 'actionable' | 'selected';

/** A button answering or changing the choice, by the legacy button's text. */
export type DecisionButton = 'OK' | 'Pass' | 'Done' | 'Reset choice' | 'Clear selection';

/** An action of a card, as the server names and describes it (HTML). */
export interface CardAction {
  actionId: string;
  text: string;
}

/** A card shown for the decision in a dialog (not a card of the game). */
export interface DecisionCard {
  id: string;
  blueprintId: string;
  /** Written on the card (an action's text, HTML), or null. */
  text: string | null;
  testingText: string | null;
}

export type DecisionDialog =
  | { kind: 'integer'; text: string; min: number; max: number; value: number; answerable: boolean }
  | {
      kind: 'choice';
      answerable: boolean;
      text: string;
      /** The answers (HTML). */
      options: readonly string[];
      /** A list with OK; otherwise a button per answer. */
      dropDown: boolean;
    }
  | { kind: 'cards'; title: string; cards: readonly DecisionCard[]; buttons: readonly DecisionButton[] };

export interface DecisionView {
  /** The alert box: the decision's text (HTML), '' when a dialog asks or nothing waits. */
  alert: string;
  /**
   * What kind of choice the alert box frames: actions to take, or cards to choose; 'ended': nothing any more, but the
   * frame keeps its width (a choice was open when the game ended, or a replay's alert box asked before).
   */
  frame: 'action' | 'selection' | 'ended' | null;
  /** The alert box's buttons, in order. */
  buttons: readonly DecisionButton[];
  /** Done stands at the right of the alert box (not when assigning minions, where it is the only button). */
  doneOnRight: boolean;
  /** How each card on the page is offered, by card id. */
  marks: Readonly<Record<string, CardMark>>;
  /** Each offered card's actions, by card id. */
  actions: Readonly<Record<string, readonly CardAction[]>>;
  /**
   * Cards that aren't on the table but have an action (in a pile, say): shown see-through in the hand's row while the
   * decision waits (legacy createVirtualCard).
   */
  extraCards: readonly GameCard[];
  dialog: DecisionDialog | null;
}

export interface DecisionSettings {
  autoAccept: boolean;
  alwaysDropDown: boolean;
  /** A replay shows each decision, without anything to answer it with. */
  replay?: boolean;
}

export const NO_DECISION: DecisionView = {
  alert: '',
  frame: null,
  buttons: [],
  doneOnRight: false,
  marks: {},
  actions: {},
  extraCards: [],
  dialog: null,
};

const param = (decision: Decision, name: string) => decision.params[name] ?? [];
const first = (decision: Decision, name: string) => decision.params[name]?.[0] ?? null;
const text = (value: string | undefined) => (value === undefined || value === 'null' ? null : value);

function marksOf(ids: readonly string[], mark: CardMark): Record<string, CardMark> {
  return Object.fromEntries(ids.map((id) => [id, mark]));
}

/**
 * A card the decision shows that isn't a card on the table, drawn from what the decision says of it: in the hand's
 * row (zone EXTRA) or in the decision's dialog (SPECIAL).
 */
export function decisionCard(
  id: string,
  blueprintId: string,
  testingText: string | null,
  zone: 'EXTRA' | 'SPECIAL' = 'EXTRA',
): GameCard {
  return {
    id,
    blueprintId,
    zone,
    owner: null,
    hindered: false,
    index: null,
    attachedTo: null,
    stackedOn: null,
    attachedAt: 0,
    tokens: {},
    inactive: false,
    assignedTo: null,
    inSkirmish: false,
    testingText,
    backSideTestingText: null,
  };
}

/** The least number of cards a decision takes ("min"; none said: 0). */
export function minimum(decision: Decision): number {
  return Number(first(decision, 'min') ?? 0) || 0;
}

/** For a choice of a card's actions: the card each action is offered on (a card not on the page gets a stand-in). */
export function actionCardIds(decision: Decision): string[] {
  const blueprints = param(decision, 'blueprintId');
  return param(decision, 'cardId').map((id, i) => (blueprints[i] === 'inPlay' ? id : `extra${id}`));
}

/** Cards chosen up to the most allowed, or not: the cards still offered, the chosen ones marked. */
function cardChoice(
  offered: readonly string[],
  max: number,
  { selectedCardIds }: GameModel['local'],
): Record<string, CardMark> {
  // With the most chosen, nothing more is offered; before that, every card not chosen yet
  const more = selectedCardIds.length < max;
  return {
    ...(more
      ? marksOf(
          offered.filter((id) => !selectedCardIds.includes(id)),
          'selectable',
        )
      : {}),
    ...marksOf(selectedCardIds, 'selected'),
  };
}

export function decisionView(model: GameModel, settings: DecisionSettings): DecisionView {
  const view = askedView(model, settings);
  if (!settings.replay) return view;
  // A replay: the same, with no buttons; once the alert box asked, its frame stays wide (the legacy page only put the
  // width back on an answer)
  const dialog = view.dialog?.kind === 'cards' ? { ...view.dialog, buttons: [] } : view.dialog;
  const frame = view.frame ?? (model.alertAsked ? 'ended' : null);
  return { ...view, buttons: [], dialog, frame };
}

function askedView(model: GameModel, settings: DecisionSettings): DecisionView {
  const decision = model.decision?.decision;
  if (!decision) {
    // The game ended while the alert box asked: the legacy page took the question away but left the frame wide
    return model.closedByEnd !== null && ALERT_DECISIONS.includes(model.closedByEnd)
      ? { ...NO_DECISION, frame: 'ended' }
      : NO_DECISION;
  }
  const local = model.local;
  const chosen = local.selectedCardIds.length;
  switch (decision.type) {
    case 'INTEGER':
      return {
        ...NO_DECISION,
        dialog: {
          kind: 'integer',
          text: decision.text,
          min: Number(first(decision, 'min') ?? 0),
          max: Number(first(decision, 'max') ?? 1000),
          value: Number(first(decision, 'defaultValue') ?? 0),
          answerable: !settings.replay,
        },
      };
    case 'MULTIPLE_CHOICE': {
      const options = param(decision, 'results');
      return {
        ...NO_DECISION,
        dialog: {
          kind: 'choice',
          text: decision.text,
          options,
          dropDown: options.length > 2 || settings.alwaysDropDown,
          answerable: !settings.replay,
        },
      };
    }
    case 'ARBITRARY_CARDS': {
      const ids = param(decision, 'cardId');
      const blueprints = param(decision, 'blueprintId');
      const selectable = param(decision, 'selectable');
      const testing = param(decision, 'testingText');
      const max = Number(first(decision, 'max') ?? 0);
      return {
        ...NO_DECISION,
        marks: cardChoice(
          ids.filter((_, i) => selectable[i] === 'true'),
          max,
          local,
        ),
        dialog: {
          kind: 'cards',
          title: decision.text,
          cards: blueprints.map((blueprintId, i) => ({
            id: ids[i] ?? '',
            blueprintId,
            text: null,
            testingText: text(testing[i]),
          })),
          buttons: [
            ...(chosen > 0 ? (['Clear selection'] as const) : []),
            ...(chosen >= minimum(decision) ? (['Done'] as const) : []),
          ],
        },
      };
    }
    case 'ACTION_CHOICE': {
      const blueprints = param(decision, 'blueprintId');
      const texts = param(decision, 'actionText');
      const testing = param(decision, 'testingText');
      const ids = blueprints.map((_, i) => `temp${i}`);
      return {
        ...NO_DECISION,
        marks: chosen > 0 ? marksOf(local.markedCardIds, 'selected') : marksOf(ids, 'selectable'),
        dialog: {
          kind: 'cards',
          title: decision.text,
          cards: blueprints.map((blueprintId, i) => ({
            id: `temp${i}`,
            blueprintId,
            text: texts[i] ?? null,
            testingText: text(testing[i]),
          })),
          buttons: chosen > 0 ? ['Clear selection', 'Done'] : [],
        },
      };
    }
    case 'CARD_ACTION_CHOICE': {
      const cardIds = actionCardIds(decision);
      const blueprints = param(decision, 'blueprintId');
      const actionIds = param(decision, 'actionId');
      const texts = param(decision, 'actionText');
      const testing = param(decision, 'testingText');
      const actions: Record<string, CardAction[]> = {};
      const extraCards: GameCard[] = [];
      cardIds.forEach((cardId, i) => {
        if (blueprints[i] !== 'inPlay' && !actions[cardId]) {
          extraCards.push(decisionCard(cardId, blueprints[i] ?? '', text(testing[i])));
        }
        (actions[cardId] ??= []).push({ actionId: actionIds[i] ?? '', text: texts[i] ?? '' });
      });
      return {
        ...NO_DECISION,
        alert: decision.text,
        frame: 'action',
        doneOnRight: true,
        buttons: chosen === 0 ? ['Pass'] : ['Reset choice', 'Done'],
        marks: chosen === 0 ? marksOf(cardIds, 'actionable') : marksOf(local.markedCardIds, 'selected'),
        actions,
        extraCards,
      };
    }
    case 'CARD_SELECTION': {
      const max = Number(first(decision, 'max') ?? 0);
      return {
        ...NO_DECISION,
        alert: decision.text,
        frame: 'selection',
        doneOnRight: true,
        buttons: [
          ...(chosen > 0 ? (['Reset choice'] as const) : []),
          ...(chosen >= minimum(decision) ? (['Done'] as const) : []),
        ],
        marks: cardChoice(param(decision, 'cardId'), max, local),
      };
    }
    case 'ASSIGN_MINIONS': {
      const minions = param(decision, 'minions');
      // A minion clicked: the characters it may go to, and itself (to take it back)
      const offered = local.minion === null ? minions : [...param(decision, 'freeCharacters'), local.minion];
      return {
        ...NO_DECISION,
        alert: decision.text,
        frame: 'selection',
        buttons: ['Done'],
        marks: marksOf(offered, 'selectable'),
      };
    }
  }
}

/** Whether a minion of the waiting assignment has no character yet (Done then asks first). */
export function minionUnassigned(model: GameModel): boolean {
  const decision = model.decision?.decision;
  return (
    decision?.type === 'ASSIGN_MINIONS' &&
    param(decision, 'minions').some((minion) => model.cards[minion] && assignmentOf(model, minion) === null)
  );
}
