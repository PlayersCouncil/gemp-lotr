import { DeckValidityStatus } from '../../../common/types/deck/deck-validity-status.type';
import { DeckValidityFunction } from './deck-validity-function.type';

export const twilightSectionValidator: DeckValidityFunction = (deck) => {
  return { status: DeckValidityStatus.Ok, message: '' };
};
