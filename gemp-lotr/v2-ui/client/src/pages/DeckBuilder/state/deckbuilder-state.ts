import { create } from 'zustand';
import {
  CardBlueprint,
  CardId,
} from '../../../lotr-common/types/lotr-card/card-blueprint.interface';
import { LotrLocations } from '../../../common/types/LotrLocations/lotr-locations.type';
import { doAddCardToDeck } from './add-card-to-deck.function';
import { CardBlueprintWithCount } from './card-blueprint-with-count';
import { LotrSiteCardBlueprint } from '../../../lotr-common/types/lotr-card/lotr-site-card';
import { doRemoveCardFromDeck } from './remove-card-from-deck.function';

interface DeckBuilderState {
  ring?: CardBlueprint;
  ringBearer?: CardBlueprint;
  sites: LotrLocations;
  freePeople: Map<CardId, CardBlueprintWithCount>;
  shadow: Map<CardId, CardBlueprintWithCount>;
}

interface DeckBuilderActions {
  addCardToDeck: (card: CardBlueprint) => void;
  removeCardFromDeck: (card: CardBlueprint) => void;
}

export type DeckBuilderStore = DeckBuilderState & DeckBuilderActions;

export const useDeckBuilderStore = create<DeckBuilderStore>()((set) => ({
  removeCardFromDeck: (card: CardBlueprint) => {
    console.log(card);
    switch (card.group) {
      case 'site': {
        const { siteNumber } = card as LotrSiteCardBlueprint;
        set((state) => ({ sites: { ...state.sites, [siteNumber]: undefined } }));
        break;
      }
      case 'fp': {
        set((state) => {
          return { freePeople: doRemoveCardFromDeck(card, state.freePeople) };
        });
        break;
      }
      case 'shadow': {
        set((state) => {
          return { shadow: doRemoveCardFromDeck(card, state.shadow) };
        });
        break;
      }
      case 'ring':
        set(() => ({ ring: undefined }));
        break;
      case 'ringBearer':
        set(() => ({ ringBearer: undefined }));
        break;
      default:
        break;
    }
  },
  addCardToDeck: (card: CardBlueprint) => {
    switch (card.group) {
      case 'site': {
        const { siteNumber } = card as LotrSiteCardBlueprint;
        set((state) => ({ sites: { ...state.sites, [siteNumber]: card } }));
        break;
      }
      case 'fp': {
        set((state) => {
          return { freePeople: doAddCardToDeck(card, state.freePeople) };
        });
        break;
      }
      case 'shadow': {
        set((state) => {
          return { shadow: doAddCardToDeck(card, state.shadow) };
        });
        break;
      }
      case 'ring':
        set(() => ({ ring: card }));
      case 'ringBearer':
        set(() => ({ ringBearer: card }));
      default:
        break;
    }
  },
  freePeople: new Map(),
  shadow: new Map(),
  sites: {
    1: undefined,
    2: undefined,
    3: undefined,
    4: undefined,
    5: undefined,
    6: undefined,
    7: undefined,
    8: undefined,
    9: undefined,
  },
  ring: undefined,
  ringBearer: undefined,
}));
