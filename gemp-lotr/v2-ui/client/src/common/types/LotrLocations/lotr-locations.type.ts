import { CardBlueprint } from '../../../lotr-common/types/lotr-card/card-blueprint.interface';

export enum LotrLocationNames {
  SiteOne = 1,
  SiteTwo = 2,
  SiteThree = 3,
  SiteFour = 4,
  SiteFive = 5,
  SiteSix = 6,
  SiteSeven = 7,
  SiteEight = 8,
  SiteNine = 9,
}

export type LotrLocations = Record<
  LotrLocationNames,
  CardBlueprint | undefined
>;
