import { CardBlueprint } from '../../../lotr-common/types/lotr-card/card-blueprint.interface';

// this is a dupe of CollectionCardViewModel... i need to clean these up. I'm in OOP hell
export interface CardBlueprintWithCount extends CardBlueprint {
  count: number;
}
