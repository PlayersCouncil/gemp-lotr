import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import {
  CollectionFiltersViewModel,
  PageInformation,
} from '../api/collection-api/collection-api-parameters.interface';
import { createDefaultCollectionViewModel } from '../api/collection-api/collection-filter-view-model.functions';
import { CollectionCardViewModel } from '../api/collection-api/collection-api-response.functions';
import { createDefaultPageInformation } from '../api/collection-api/collection-api-parameters.functions';
import { lotrCardTypeFilterMappings } from '../types/filter-types/card-types';

interface CardQueryState {
  filters: CollectionFiltersViewModel;
  results: CollectionCardViewModel[];
  pageInformation: PageInformation;
}

interface CardQueryActions {
  updateFilter: (filter: CollectionFiltersViewModel) => void;
  setResults: (results: CollectionCardViewModel[]) => void;
  setCurrentPage: (pageNumber: PageInformation) => void;
}

type CardQueryStore = CardQueryState & CardQueryActions;

const doUpdateFilter = (
  newFilters: CollectionFiltersViewModel,
  previousState: CardQueryState
): CollectionFiltersViewModel => {
  const filters: CollectionFiltersViewModel = {
    ...previousState.filters,
    ...newFilters,
  };
  if (filters.cardTypes?.apiName !== lotrCardTypeFilterMappings.Sites.apiName) {
    filters.siteNumber = undefined;
  }
  if (!filters.format) {
    filters.format = previousState.filters.format;
  }
  return filters;
};

export const useCardQueryStore = create(
  devtools(
    immer<CardQueryStore>((set) => ({
      filters: createDefaultCollectionViewModel(),
      pageInformation: createDefaultPageInformation(),
      results: [],
      setResults: (results: CollectionCardViewModel[]) =>
        set((state) => ({ ...state, results })),
      setCurrentPage: (pageNumber: PageInformation) =>
        set((state) => {
          const currentPageInfo = state.pageInformation;
          return {
            pageInformation: {
              ...currentPageInfo,
              ...pageNumber,
            },
          };
        }),
      updateFilter: (filters: CollectionFiltersViewModel) =>
        set((prevState) => ({
          ...prevState,
          filters: doUpdateFilter(filters, prevState),
        })),
    }))
  )
);
