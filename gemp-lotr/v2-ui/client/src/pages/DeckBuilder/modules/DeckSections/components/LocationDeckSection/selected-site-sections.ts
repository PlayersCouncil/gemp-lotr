import { LotrLocationNames } from '../../../../../../common/types/LotrLocations/lotr-locations.type';
import { FilterableDeckSection } from '../../types/filterable-deck-section.interface';

const defaults = {
  cardId: '',
  isVertical: false,
};

const selectedSiteSectionMappings: FilterableDeckSection[] = [
  {
    placeholder: 'Location 1',
    filterName: LotrLocationNames.SiteOne.toString(),
    ...defaults,
  },
  {
    placeholder: 'Location 2',
    filterName: LotrLocationNames.SiteTwo.toString(),
    ...defaults,
  },
  {
    placeholder: 'Location 3',
    filterName: LotrLocationNames.SiteThree.toString(),
    ...defaults,
  },
  {
    placeholder: 'Location 4',
    filterName: LotrLocationNames.SiteFour.toString(),
    ...defaults,
  },
  {
    placeholder: 'Location 5',
    filterName: LotrLocationNames.SiteFive.toString(),
    ...defaults,
  },
  {
    placeholder: 'Location 6',
    filterName: LotrLocationNames.SiteSix.toString(),
    ...defaults,
  },
  {
    placeholder: 'Location 7',
    filterName: LotrLocationNames.SiteSeven.toString(),
    ...defaults,
  },
  {
    placeholder: 'Location 8',
    filterName: LotrLocationNames.SiteEight.toString(),
    ...defaults,
  },
  {
    placeholder: 'Location 9',
    filterName: LotrLocationNames.SiteNine.toString(),
    ...defaults,
  },
];

export const getSelectedSiteSectionMappings = (): FilterableDeckSection[] =>
  selectedSiteSectionMappings;
