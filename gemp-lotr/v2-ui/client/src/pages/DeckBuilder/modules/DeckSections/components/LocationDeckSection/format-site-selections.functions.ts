import {
  LotrLocationNames,
  LotrLocations,
} from '../../../../../../common/types/LotrLocations/lotr-locations.type';
import { FilterableDeckSection } from '../../types/filterable-deck-section.interface';

export const formatSiteSelections = (
  selections: LotrLocations
): FilterableDeckSection[] => {
  return Object.keys(selections).map((key) => {
    return {
      placeholder: `Location ${key}`,
      filterName: key,
      isVertical: false,
      cardBlueprint: selections[key as unknown as LotrLocationNames],
    };
  });
};
