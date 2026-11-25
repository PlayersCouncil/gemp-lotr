/**
 * What the phone layout shows in the hand's place: the hand, or cards the waiting decision offers from off the table
 * (a deck, a pile, the adventure deck when a site is to be played) under a label saying what they are (G7 of
 * trymarz/gemp-lotr-private#27). Pure.
 */
import { CardMark, DecisionView, decisionCard } from '../decision/decision-view';
import { GameCard, GameModel } from '../state/game-model';
import { PhoneCard, PhoneTable } from './phone-table';

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

/**
 * Cards lying under a card on the table (stacked on it, attached to it) that the waiting decision offers, with that
 * card: the hand's place shows them before its own cards, smaller than them, (the owner, 2026-11-17: the small copies under a
 * card were too small to tap safely, e.g. minions stacked on a Web; an action such as putting on the Ring must be
 * seen first). The small copies stay lit too.
 */
export interface UnderOffer {
  host: GameCard;
  how: 'stacked' | 'attached';
  cards: readonly PhoneCard[];
}

/** Offered, or chosen already for the decision (so a card chosen among several stays where it was tapped). */
const OFFERED: ReadonlySet<CardMark> = new Set<CardMark>(['selectable', 'actionable', 'selected']);

export function offeredUnder(table: PhoneTable, marks: Readonly<Record<string, CardMark>>): UnderOffer[] {
  const hosts = [
    ...table.above.flatMap((row) => row.cards),
    ...table.pairs.flatMap((pair) => [...pair.characters, ...pair.minions]),
    ...table.below.flatMap((row) => row.cards),
  ];
  return hosts.flatMap(({ card: host, under }) =>
    (['stacked', 'attached'] as const).flatMap((how) => {
      const cards = under.filter((card) => {
        const mark = marks[card.id];
        return (card.stackedOn !== null) === (how === 'stacked') && mark !== undefined && OFFERED.has(mark);
      });
      return cards.length > 0 ? [{ host, how, cards: cards.map((card) => ({ card, under: [] })) }] : [];
    }),
  );
}
