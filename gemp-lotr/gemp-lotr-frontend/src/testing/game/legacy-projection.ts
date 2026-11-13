/**
 * Test-only: the game model (src/core/game) as the legacy game page shows it, in the shape the browser check reads
 * from that page (visual-check/game/controls.mjs readLegacyModel), so the two can be compared on the recordings.
 * Only what the legacy page's state means is compared: texts its markup alone decides (a hidden line keeping its old
 * text) are left out (see `comparable`).
 */
import type { LegacyGameModel } from '../../../visual-check/game/controls.mjs';
import { GameModel } from '../../core/game/state/game-model';
import { assignmentOf } from '../../core/game/state/local-choices';

/** Decisions that write their text in the alert box (the others open a dialog). */
const ALERT_DECISIONS = ['CARD_ACTION_CHOICE', 'CARD_SELECTION', 'ASSIGN_MINIONS'];

/** The cards the legacy page adds for a waiting decision: a dialog's cards, actions of cards not in play. */
function decisionCards(model: GameModel): string[] {
  const decision = model.decision?.decision;
  if (!decision) return [];
  const blueprints = decision.params['blueprintId'] ?? [];
  if (decision.type === 'ARBITRARY_CARDS' || decision.type === 'ACTION_CHOICE') {
    return blueprints.map((blueprint, i) => `temp${i}:${blueprint}:SPECIAL:`);
  }
  if (decision.type === 'CARD_ACTION_CHOICE') {
    const cardIds = decision.params['cardId'] ?? [];
    return [
      ...new Set(
        blueprints.flatMap((blueprint, i) =>
          blueprint === 'inPlay' ? [] : [`extra${cardIds[i] ?? ''}:${blueprint}:EXTRA:`],
        ),
      ),
    ];
  }
  return [];
}

export function legacyView(model: GameModel): LegacyGameModel {
  const started = model.started;
  const cards = model.cardOrder.flatMap((id) => {
    const card = model.cards[id];
    if (!card) return [];
    const parts = [card.id, card.blueprintId, card.zone, card.owner ?? ''];
    const assigned = assignmentOf(model, card.id);
    if (assigned) parts.push(`assign ${assigned}`);
    if (card.inSkirmish) parts.push('skirmish');
    return [parts.join(':')];
  });
  const stats: Record<string, string | null> = {};
  if (started) {
    model.players.forEach((player, i) => {
      const zones = model.stats?.playerZones[player];
      const threat = model.stats?.threats[player];
      const show = (value: number | null | undefined) => (value === null || value === undefined ? '' : String(value));
      stats[`deck${i}`] = show(zones?.deck);
      stats[`hand${i}`] = show(zones?.hand);
      stats[`threats${i}`] = threat ? (threat.total === '-1' ? threat.value : `${threat.value}/${threat.total}`) : '';
      stats[`showStats${i}`] = '+';
      stats[`discard${i}`] = show(zones?.discard);
      stats[`deadPile${i}`] = show(zones?.dead);
      stats[`adventureDeck${i}`] = show(zones?.adventureDeck);
      stats[`removedPile${i}`] = show(zones?.removed);
    });
  }
  const decision = model.decision?.decision;
  return {
    twilight: started ? String(model.twilight) : null,
    phase: started ? (model.phase ?? '') : null,
    initiative: started
      ? `Initiative: ${model.stats ? (model.stats.initiative === 'FREE_PEOPLE' ? 'Free Peoples' : 'Shadow') : ''}`
      : null,
    ruleOf4: null,
    alert: started ? (decision && ALERT_DECISIONS.includes(decision.type) ? decision.text : '') : null,
    stats,
    cards: [...cards, ...decisionCards(model)].sort(),
  };
}

/** What is compared: the legacy model without the rule of 4 line (its markup keeps an old text while hidden). */
export function comparable(model: LegacyGameModel): Omit<LegacyGameModel, 'ruleOf4'> {
  return {
    twilight: model.twilight,
    phase: model.phase,
    initiative: model.initiative,
    alert: model.alert?.replace(/\s+/g, ' ').trim() ?? null,
    stats: model.stats,
    cards: model.cards,
  };
}
