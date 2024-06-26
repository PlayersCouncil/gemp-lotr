import { create } from 'zustand';
import { CollectionFiltersViewModel } from '../../../lotr-common/api/collection-api/collection-api-parameters.interface';
import { createDefaultCollectionViewModel } from '../../../lotr-common/api/collection-api/collection-filter-view-model.functions';
import { Mappings } from '../../../common/types/mappings.interface';

interface DeckManagerState {
}

interface DeckManagerActions {
}

type DeckManagerStore = DeckManagerState & DeckManagerActions;

export const useDeckManagerStore = create<DeckManagerStore>()((set) => ({

}));
