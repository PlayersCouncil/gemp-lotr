import axios, { AxiosRequestConfig } from 'axios';
import {
  CollectionApiParameters,
  CollectionFiltersViewModel,
  PageInformation,
} from './collection-api-parameters.interface';
import {
  convertViewModelToDao,
  getDefaultCollectionApiParameters,
} from './collection-api-parameters.functions';
import {
  CollectionCardViewModel,
  convertGetCollectionFromXml,
} from './collection-api-response.functions';
import { lotrCardTypeFilterMappings } from '../../types/filter-types/card-types';

const doesRequestResultInHorizontalCards = (
  filters: CollectionFiltersViewModel
): boolean => {
  return Boolean(
    filters.cardTypes?.apiName === lotrCardTypeFilterMappings.Sites.apiName ||
      filters.siteNumber
  );
};

const createFilter = (params: CollectionApiParameters): string => {
  let encodedFilter = '';
  if (params.filter.rarity) {
    encodedFilter += encodeURIComponent(`rarity:${params.filter.rarity} `);
  }
  if (params.filter.keywords) {
    encodedFilter += encodeURIComponent(`keyword:${params.filter.keywords} `);
  }
  if (params.filter.cardTypes) {
    encodedFilter += encodeURIComponent(`cardType:${params.filter.cardTypes} `);
  }
  if (params.filter.format) {
    encodedFilter += encodeURIComponent(`set:${params.filter.format} `);
  }
  if (params.filter.title) {
    encodedFilter += encodeURIComponent(`name:${params.filter.title} `);
  }
  if (params.filter.cultures) {
    encodedFilter += encodeURIComponent(
      `culture:${params.filter.cultures.toUpperCase()} `
    );
  }
  if (params.filter.siteNumber) {
    encodedFilter += encodeURIComponent(
      `siteNumber:${params.filter.siteNumber} `
    );
  }
  return encodedFilter;
};

export const collectionApiConfiguration = (
  parameters: Partial<CollectionApiParameters>
): [string, AxiosRequestConfig<CollectionApiParameters>] => {
  const finalParams = {
    ...getDefaultCollectionApiParameters(),
    ...parameters,
  };
  // TODO: --Build query param string builder-- Make it better
  const queryParams = `participantId=null&filter=${createFilter(
    finalParams
  )}&start=${finalParams.start ?? 0}&count=${finalParams.count}`;
  const url = `/gemp-lotr-server/collection/default?${queryParams}`;
  return [
    url,
    {
      method: 'GET',
      headers: { 'Content-Type': 'application/xml' },
      url,
    },
  ];
};

export const executeGetCollectionByFilterRequest = async (
  filters: CollectionFiltersViewModel,
  pageInformation: PageInformation
): Promise<CollectionCardViewModel[]> => {
  const dao = getDefaultCollectionApiParameters(
    convertViewModelToDao(filters),
    {
      ...pageInformation,
    }
  );
  const filterApiConfiguration = collectionApiConfiguration(dao);
  const result = await axios.get(
    filterApiConfiguration[0],
    filterApiConfiguration[1]
  );
  return convertGetCollectionFromXml(
    result.data,
    doesRequestResultInHorizontalCards(filters)
  ).cards;
};
