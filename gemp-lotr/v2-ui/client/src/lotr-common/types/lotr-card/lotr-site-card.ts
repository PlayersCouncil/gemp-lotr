import { LotrLocationNames } from '../../../common/types/LotrLocations/lotr-locations.type';
import { CardBlueprint } from './card-blueprint.interface';

export interface LotrSiteCardBlueprint extends CardBlueprint {
  siteNumber: LotrLocationNames;
}
