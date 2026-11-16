/**
 * What the phone layout shows in the hand's place: the hand, or cards the waiting decision offers from off the table
 * (a deck, a pile, the adventure deck when a site is to be played) under a label saying what they are (G7 of
 * trymarz/gemp-lotr-private#27). Pure.
 */
import { DecisionView, decisionCard } from '../decision/decision-view';
import { GameCard, GameModel } from '../state/game-model';

export interface OffTableCard {
  card: GameCard;
  /** Written on the card for the decision (an action's text, HTML), or null. */
  text: string | null;
}

export interface OffTableChoices {
  /** Changes with each new offer, so a closed offer comes back for the next decision. */
  key: string;
  /** What the cards are ("Your adventure deck"); HTML when it is the decision's own title. */
  label: string;
  labelIsHtml: boolean;
  cards: readonly OffTableCard[];
}

/** What a pile's cards are called, from the zone the server keeps them in. */
const ZONE_LABELS: Readonly<Record<string, string>> = {
  ADVENTURE_DECK: 'adventure deck',
  DECK: 'draw deck',
  DISCARD: 'discard pile',
  DEAD: 'dead pile',
  REMOVED: 'removed from game',
  STACKED: 'stacked cards',
};

/** The cards the waiting decision offers from off the table, or null when it offers none. */
export function offTableChoices(view: DecisionView, model: GameModel): OffTableChoices | null {
  const dialog = view.dialog;
  if (dialog?.kind === 'cards') {
    return {
      key: `dialog:${model.decision?.arrivedAt ?? 0}`,
      label: dialog.title,
      labelIsHtml: true,
      cards: dialog.cards.map((card) => ({
        card: decisionCard(card.id, card.blueprintId, card.testingText, 'SPECIAL'),
        text: card.text,
      })),
    };
  }
  if (view.extraCards.length > 0) {
    // Named after where the cards lie, when the page knows them (its own piles' cards): `extra<id>` stands for card id
    const zones = new Set(view.extraCards.map((card) => model.cards[card.id.replace(/^extra/, '')]?.zone ?? 'EXTRA'));
    const zone = zones.size === 1 ? [...zones][0] : null;
    const pile = zone ? ZONE_LABELS[zone] : undefined;
    return {
      key: `extra:${model.decision?.arrivedAt ?? 0}`,
      label: pile ? `Your ${pile}` : 'Cards from elsewhere',
      labelIsHtml: false,
      // The decision's own copies: what the decision offers is marked by their ids
      cards: view.extraCards.map((card) => ({ card, text: null })),
    };
  }
  return null;
}
