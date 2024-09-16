import { Deck } from '../../../common/types/deck/Deck';
import { DeckBuilderStore } from './deckbuilder-state';

export const getCurrentDeck = (state: DeckBuilderStore): Deck => {
  const { ring, ringBearer, sites, freePeople, shadow } = state;
  return {
    ring,
    ringBearer,
    sites,
    cards: [...freePeople.values(), ...shadow.values()], // BUGTODO: this does not account for the duplciates caused by count :)
  };
};
