import { DeckValidityStatus } from '../../../common/types/deck/deck-validity-status.type';
import { DeckValidityFunction } from './deck-validity-function.type';

// TODO: This function will validate that all cards in the deck will be valid for the selected format.
export const isDeckValidForSelectedFormat: DeckValidityFunction = (deck) => {
  return { status: DeckValidityStatus.Ok, message: '' };
};
