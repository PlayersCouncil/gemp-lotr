import {
    CardBlueprint,
    CardId,
} from '../../../lotr-common/types/lotr-card/card-blueprint.interface';
import { CardBlueprintWithCount } from './card-blueprint-with-count';

export const doRemoveCardFromDeck = (
    card: CardBlueprint,
    cards: Map<CardId, CardBlueprintWithCount>
): Map<CardId, CardBlueprintWithCount> => {
    const existingCard = cards.get(card.cardBlueprintId);
    if (!existingCard) {
        return cards;
    }
    if (existingCard && existingCard.count > 1) {
        existingCard.count--;
        cards.set(card.cardBlueprintId, existingCard);
    } else {
        cards.delete(card.cardBlueprintId);

    }
    return cards;
};
