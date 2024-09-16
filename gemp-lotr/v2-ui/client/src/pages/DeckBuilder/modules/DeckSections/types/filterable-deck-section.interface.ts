import { CardBlueprint } from '../../../../../lotr-common/types/lotr-card/card-blueprint.interface';

export interface FilterableDeckSection {
  placeholder: string;

  filterName: string;
  cardBlueprint?: CardBlueprint;
  isVertical: boolean;
}
