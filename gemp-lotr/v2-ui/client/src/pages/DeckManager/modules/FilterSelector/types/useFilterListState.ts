/* eslint-disable no-param-reassign -- this is fine since it's an immer draft... i think? */
import { devtools } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import { create } from 'zustand';
import { useEffect } from 'react';
import { CollectionFiltersViewModel } from '../../../../../lotr-common/api/collection-api/collection-api-parameters.interface';
import { rarityMappings } from '../../../../../lotr-common/types/filter-types/rarity-filter.type';
import { lotrCardTypeFilterMappings } from '../../../../../lotr-common/types/filter-types/card-types';
import { lotrCardKeywordTypeMappings } from '../../../../../lotr-common/types/filter-types/keyword.types';
import { commonFormatsMetadata } from '../../../../../lotr-common/types/expansions';

type FilterListState = Required<
  Pick<
    CollectionFiltersViewModel,
    'cardName' | 'cardTypes' | 'format' | 'keywords' | 'rarity'
  > & {
    freeCultures: string[];
    twilightCultures: string[];
  }
>;

interface FilterListActions {
  resetFilters: () => void;

  updateFilter: <T extends keyof FilterListStore>(
    filterName: T,
    filterValue: FilterListStore[T]
  ) => void;
  setPreviousFilters(filters: CollectionFiltersViewModel): void;
}

type FilterListStore = FilterListState & FilterListActions;

const createDefaultFilterListState = (
  input?: CollectionFiltersViewModel
): FilterListState => {
  return {
    cardName: input?.cardName ?? '',
    cardTypes: input?.cardTypes ?? lotrCardTypeFilterMappings.All,
    format: input?.format ?? commonFormatsMetadata.All,
    keywords: input?.keywords ?? lotrCardKeywordTypeMappings.All,
    rarity: input?.rarity ?? rarityMappings.All,
    freeCultures: input?.cultures ?? [],
    twilightCultures: input?.cultures ?? [],
  };
};

const filterListState = create<FilterListStore>()(
  devtools(
    immer((set, get) => ({
      resetFilters: () => {},
      setPreviousFilters: (filters: CollectionFiltersViewModel) => {
        set(createDefaultFilterListState(filters));
      },
      updateFilter: (filterName, filterValue) => {
        set((state) => {
          state[filterName] = filterValue;
        });
      },
      activeDeckSection: '',
      cardName: '',
      cardTypes: lotrCardTypeFilterMappings.All,
      format: commonFormatsMetadata.All,
      keywords: lotrCardKeywordTypeMappings.All,
      rarity: rarityMappings.All,
      twilightCultures: [],
      freeCultures: [],
    }))
  )
);

export const useFilterListState = (
  currentValues: CollectionFiltersViewModel
) => {
  const state = filterListState();

  useEffect(() => {
    state.setPreviousFilters(currentValues);
    // eslint-disable-next-line react-hooks/exhaustive-deps -- we only want to run this when the applied filters are updated
  }, [currentValues]);

  return {
    ...state,
  };
};
