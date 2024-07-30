import { expect, describe, it } from 'vitest';
import {
  NO_RINGBEARER_MESSAGE,
  NO_RING_MESSAGE,
  NO_RING_OR_RINGBEARER_MESSAGE,
  ringbearerValidation,
} from './ringbearer-section-validation.function';
import { Deck } from '../../../common/types/deck/Deck';
import { DeckValidityStatus } from '../../../common/types/deck/deck-validity-status.type';
import { Mappings } from '../../../common/types/mappings.interface';

describe('ringbearer-section-validity.function', () => {
  const format = {} as Mappings;
  it('should return an OK status and an empty message if the deck has a ring and a ring-bearer', () => {
    const deck: Deck = {
      ringBearer: {
        cardBlueprintId: '1',
      },
      ring: {
        cardBlueprintId: '1_231',
      },
    } as Deck;
    const result = ringbearerValidation(deck, format);

    expect(result.status).toBe(DeckValidityStatus.Ok);
  });

  it('should return an error message when there is no ring', () => {
    const deck: Deck = {
      ringBearer: {
        cardBlueprintId: '1',
      },
      ring: undefined,
    } as Deck;
    const result = ringbearerValidation(deck, format);

    expect(result.status).toBe(DeckValidityStatus.Error);
    expect(result.message).toBe(NO_RING_MESSAGE);
  });

  it('should return an error message when there is no ringbearer', () => {
    const deck: Deck = {
      ringBearer: undefined,
      ring: {
        cardBlueprintId: '1_231',
      },
    } as Deck;
    const result = ringbearerValidation(deck, format);

    expect(result.status).toBe(DeckValidityStatus.Error);
    expect(result.message).toBe(NO_RINGBEARER_MESSAGE);
  });

  it('should return an error message when there is no ringbearer OR ring', () => {
    const deck: Deck = {
      ringBearer: undefined,
      ring: undefined,
    } as Deck;
    const result = ringbearerValidation(deck, format);

    expect(result.status).toBe(DeckValidityStatus.NotStarted);
    expect(result.message).toBe(NO_RING_OR_RINGBEARER_MESSAGE);
  });
});
