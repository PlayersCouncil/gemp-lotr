import { lotrCardTypeFilterMappings } from '../../types/filter-types/card-types';
import {
  CollectionApiFilterDAO,
  CollectionApiParameters,
  CollectionFiltersViewModel,
  PageInformation,
} from './collection-api-parameters.interface';

export const createDefaultPageInformation = (): PageInformation => ({
  count: 18,
  start: 0,
});

export const getDefaultCollectionFilterState = (
  filters: Partial<CollectionApiFilterDAO> = {}
): CollectionApiFilterDAO => ({
  format: '',
  side: '',
  type: '',
  rarity: '',
  words: '',
  cardTypes: '',
  cultures: '',
  keywords: '',
  siteNumber: '',
  races: '',
  itemClasses: '',
  phases: '',
  title: '',
  ...filters,
});

export const getDefaultCollectionApiParameters = (
  filters: Partial<CollectionApiFilterDAO> = {},
  pageInformation = createDefaultPageInformation()
): CollectionApiParameters => {
  return {
    participantId: null,
    count: pageInformation.count,
    start: pageInformation.start,
    filter: getDefaultCollectionFilterState(filters),
  };
};

const determineSiteNumber = ({
  siteNumber,
  cardTypes,
}: CollectionFiltersViewModel): string => {
  if (cardTypes?.apiName !== lotrCardTypeFilterMappings.Sites.apiName) {
    return '';
  }
  return siteNumber ?? '';
};

export const convertViewModelToDao = (
  input: CollectionFiltersViewModel
): CollectionApiFilterDAO => {
  return {
    format: input.format?.apiName ?? '',
    rarity: input.rarity?.apiName ?? '',
    cardTypes: input.cardTypes?.apiName ?? '',
    siteNumber: determineSiteNumber(input),
    keywords: input.keywords?.apiName ?? '',
    cultures: input.cultures?.join(',') ?? '',
    title: input.cardName ?? '',
    // not yet supported props
    side: '',
    words: '',
    itemClasses: '',
    races: '',
    phases: '',
    type: '',
  };
};
