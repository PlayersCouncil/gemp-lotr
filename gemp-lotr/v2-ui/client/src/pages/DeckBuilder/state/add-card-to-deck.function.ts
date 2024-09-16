import {
  CardBlueprint,
  CardId,
} from '../../../lotr-common/types/lotr-card/card-blueprint.interface';
import { CardBlueprintWithCount } from './card-blueprint-with-count';

export const doAddCardToDeck = (
  card: CardBlueprint,
  cards: Map<CardId, CardBlueprintWithCount>
): Map<CardId, CardBlueprintWithCount> => {
  const existingCard = cards.get(card.cardBlueprintId);
  if (existingCard && existingCard.count === 4) {
    return cards;
  }
  if (existingCard) {
    existingCard.count++;
    cards.set(card.cardBlueprintId, existingCard);
  } else {
    cards.set(card.cardBlueprintId, { ...card, count: 1 });
  }
  return cards;
};
