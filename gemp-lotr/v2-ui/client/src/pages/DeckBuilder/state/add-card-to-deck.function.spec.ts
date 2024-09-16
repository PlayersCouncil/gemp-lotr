import { describe, expect, it } from 'vitest';
import { doAddCardToDeck } from './add-card-to-deck.function';
import { CardBlueprint } from '../../../lotr-common/types/lotr-card/card-blueprint.interface';
import { CardBlueprintWithCount } from './card-blueprint-with-count';

describe('doAddCardToDeck', () => {
  it('should add a card to the deck', () => {
    const card = { cardBlueprintId: '1' } as CardBlueprint;
    const cards = new Map();
    const result = doAddCardToDeck(card, cards);
    expect(result.get('1')).toEqual({ cardBlueprintId: '1', count: 1 });
  });

  it('should increment the count if the card is already in the deck', () => {
    const card = { cardBlueprintId: '1' } as CardBlueprint;
    const cards = new Map();
    cards.set('1', {
      cardBlueprintId: '1',
      count: 1,
    } as CardBlueprintWithCount);
    const result = doAddCardToDeck(card, cards);
    expect(result.get('1')).toEqual({ cardBlueprintId: '1', count: 2 });
  });

  it('should not increment the count if the card is already in the deck and the count is 4', () => {
    const card = { cardBlueprintId: '1' } as CardBlueprint;
    const cards = new Map();
    cards.set('1', {
      cardBlueprintId: '1',
      count: 4,
    } as CardBlueprintWithCount);
    const result = doAddCardToDeck(card, cards);
    expect(result.get('1')).toEqual({ cardBlueprintId: '1', count: 4 });
  });
});
