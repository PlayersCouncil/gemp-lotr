/**
 * What the page's player chooses before sending it: minions assigned (sent only with Done), cards chosen without
 * auto-accept. Pure functions on the model, so they can be restored after a reload (GameRestore) and tested alone.
 */
import { GameModel, LocalChoices, NO_CHOICES } from './game-model';

/** A minion assigned to a character (again: it moves); only while an assignment waits. */
export function assignMinion(model: GameModel, minionId: string, characterId: string): GameModel {
  if (model.decision?.decision.type !== 'ASSIGN_MINIONS') return model;
  return { ...model, local: { ...model.local, assignments: { ...model.local.assignments, [minionId]: characterId } } };
}

export function unassignMinion(model: GameModel, minionId: string): GameModel {
  if (!(minionId in model.local.assignments)) return model;
  const assignments = Object.fromEntries(Object.entries(model.local.assignments).filter(([id]) => id !== minionId));
  return { ...model, local: { ...model.local, assignments } };
}

/** A card chosen for the waiting decision (once). */
export function selectCard(model: GameModel, cardId: string): GameModel {
  if (!model.decision || model.local.selectedCardIds.includes(cardId)) return model;
  return { ...model, local: { ...model.local, selectedCardIds: [...model.local.selectedCardIds, cardId] } };
}

export function clearSelection(model: GameModel): GameModel {
  return { ...model, local: { ...model.local, selectedCardIds: [] } };
}

/** The decision answered: it waits no more, and what was chosen for it is gone. */
export function decisionAnswered(model: GameModel): GameModel {
  return { ...model, decision: null, local: NO_CHOICES };
}

/** The character each minion goes to: the player's choice, else the server's assignment. */
export function assignmentOf(model: GameModel, minionId: string): string | null {
  return model.local.assignments[minionId] ?? model.cards[minionId]?.assignedTo ?? null;
}

/**
 * The answer to an assignment decision, as the server takes it: for each free character in the decision's order,
 * "character minion minion", joined by commas (legacy assignMinionsDecision).
 */
export function assignmentAnswer(model: GameModel): string {
  const decision = model.decision?.decision;
  if (decision?.type !== 'ASSIGN_MINIONS') return '';
  const characters = decision.params['freeCharacters'] ?? [];
  const minions = decision.params['minions'] ?? [];
  return characters
    .map((character) => [character, ...minions.filter((minion) => assignmentOf(model, minion) === character)].join(' '))
    .join(',');
}

/** Whether the choices are all still possible for this decision (a restored reload of another decision isn't). */
export function choicesFit(model: GameModel, choices: LocalChoices): boolean {
  const decision = model.decision?.decision;
  if (!decision) return false;
  const minions = decision.params['minions'] ?? [];
  const characters = decision.params['freeCharacters'] ?? [];
  const cards = decision.params['cardId'] ?? [];
  return (
    Object.entries(choices.assignments).every(([m, c]) => minions.includes(m) && characters.includes(c)) &&
    choices.selectedCardIds.every((id) => cards.includes(id))
  );
}
