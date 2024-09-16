import { Deck } from '../../../common/types/deck/Deck';
import { Mappings } from '../../../common/types/mappings.interface';
import { DeckValidationResult } from './deck-validity-function.type';
import { freePeopleValidator } from './free-peoples-section-validation.function';
import { ringbearerValidation } from './ringbearer-section-validation.function';
import { sitePathValidator } from './site-path-validation.function';
import { twilightSectionValidator } from './twilight-section-validation.function';
import { useIsDeckSectionValid } from './useIsDeckSectionValid';

export type LotrDeckValidity = {
  isDeckValid: boolean;
  deckValidity: {
    ringbearer: DeckValidationResult;
    freePeoples: DeckValidationResult;
    sitePath: DeckValidationResult;
    twilight: DeckValidationResult;
    errors: string[];
    warnings: string[];
  };
};

export default function useLotrDeckValidity(
  deck: Deck,
  format: Mappings
): LotrDeckValidity {
  const isRingbearerSectionValid = useIsDeckSectionValid(
    deck,
    format,
    ringbearerValidation
  );

  const isFreePeopleSectionValid = useIsDeckSectionValid(
    deck,
    format,
    freePeopleValidator
  );

  const isTwilightSectionValid = useIsDeckSectionValid(
    deck,
    format,
    twilightSectionValidator
  );

  const isSitePathValid = useIsDeckSectionValid(
    deck,
    format,
    sitePathValidator
  );

  return {
    isDeckValid: false,
    deckValidity: {
      ringbearer: isRingbearerSectionValid,
      freePeoples: isFreePeopleSectionValid,
      sitePath: isSitePathValid,
      twilight: isTwilightSectionValid,
      errors: [],
      warnings: [],
    },
  };
}
