import { useState, useEffect } from 'react';
import { Deck } from '../../../common/types/deck/Deck';
import { DeckValidityStatus } from '../../../common/types/deck/deck-validity-status.type';
import {
  DeckValidationResult,
  DeckValidityFunction,
} from './deck-validity-function.type';
import { Mappings } from '../../../common/types/mappings.interface';

const defaultValidityState: DeckValidationResult = {
  status: DeckValidityStatus.NotStarted,
  message: '',
};

export const useIsDeckSectionValid = (
  deck: Deck,
  format: Mappings,
  validator: DeckValidityFunction
) => {
  const [isValid, setIsValid] = useState(defaultValidityState);
  useEffect(() => {
    setIsValid(validator(deck, format));
  }, [deck, validator, format]);

  return isValid;
};
