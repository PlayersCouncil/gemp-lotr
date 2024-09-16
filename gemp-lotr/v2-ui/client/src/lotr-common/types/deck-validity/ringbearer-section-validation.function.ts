import { DeckValidityStatus } from '../../../common/types/deck/deck-validity-status.type';
import { DeckValidityFunction } from './deck-validity-function.type';

export const NO_RINGBEARER_MESSAGE = 'A Ring-bearer is required.';
export const NO_RING_MESSAGE = 'The One Ring is required.';
export const NO_RING_OR_RINGBEARER_MESSAGE =
  'The One Ring and a Ring-bearer are required.';

export const ringbearerValidation: DeckValidityFunction = (deck) => {
  if (!deck.ringBearer?.cardBlueprintId && !deck.ring?.cardBlueprintId) {
    return {
      status: DeckValidityStatus.NotStarted,
      message: NO_RING_OR_RINGBEARER_MESSAGE,
    };
  }
  if (!deck.ring?.cardBlueprintId) {
    return { status: DeckValidityStatus.Error, message: NO_RING_MESSAGE };
  }
  if (!deck.ringBearer?.cardBlueprintId) {
    return { status: DeckValidityStatus.Error, message: NO_RINGBEARER_MESSAGE };
  }
  return { status: DeckValidityStatus.Ok, message: '' };
};
