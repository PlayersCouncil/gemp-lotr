import { Mappings } from '../../../common/types/mappings.interface';

export interface PageInformation {
  start: number;
  count: number;
}

export interface CollectionApiParameters extends PageInformation {
  participantId: string | null;
  filter: CollectionApiFilterDAO;
}

/**
 * This is the model we use on the UI with friendly typings to allow swapping to display name etc
 */
export interface CollectionFiltersViewModel {
  format: Mappings;
  side?: Mappings;

  rarity?: Mappings;
  words?: Mappings;
  cardTypes?: Mappings;
  cultures?: string[];
  keywords?: Mappings;
  siteNumber?: string;
  races?: Mappings;
  itemClasses?: Mappings;
  phases?: Mappings;
  cardName?: string;

  // This is commented out until we support grabbing packs, foils, AIs etc
  // type?: Mappings;
}

/**
 * This is the model sent to the API with no syntactic sugar
 */
export interface CollectionApiFilterDAO {
  format: string;
  side: string;
  type: string;
  rarity: string;
  words: string;
  cardTypes: string;
  cultures: string;
  keywords: string;
  siteNumber: string;
  races: string;
  itemClasses: string;
  phases: string;
  title: string;
}
