import { CardBlueprint } from '../../../lotr-common/types/lotr-card/card-blueprint.interface';
import { createDefaultLocations } from '../LotrLocations/lotr-location-functions';
import { LotrLocations } from '../LotrLocations/lotr-locations.type';

export interface Deck {
  name?: string;
  owner?: string;
  ringBearer?: CardBlueprint;
  sites: LotrLocations;
  ring?: CardBlueprint;
  cards: CardBlueprint[];
}

export const createDefaultDeck = (): Deck => ({
  name: 'foobar',
  owner: '',
  ringBearer: undefined,
  ring: undefined,
  cards: [],
  sites: createDefaultLocations(),
});
