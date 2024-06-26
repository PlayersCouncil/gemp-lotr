import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { immer } from 'zustand/middleware/immer';
import { enableMapSet } from 'immer';
import {
  CardBlueprint,
  CardId,
} from '../types/lotr-card/card-blueprint.interface';

// this is needed since we are using maps... i wonder if that's a good idea
enableMapSet();

interface CardBlueprintState {
  cardIds: CardId[];
  cardBlueprints: Map<CardId, CardBlueprint>;
}

interface CardBlueprintActions {
  addCardBlueprint: (cardBlueprint: CardBlueprint) => void;
  addCardBlueprints: (cardBlueprints: CardBlueprint[]) => void;
}

type CardStore = CardBlueprintState & CardBlueprintActions;

export const useCardBlueprintStore = create<CardStore>()(
  devtools(
    immer<CardStore>((set) => ({
      cardIds: [],
      cardBlueprints: new Map<string, CardBlueprint>(),
      addCardBlueprint: (cardBlueprint: CardBlueprint) => {
        set((state) => {
          state.cardBlueprints.set(
            cardBlueprint.cardBlueprintId,
            cardBlueprint
          );
          state.cardIds.push(cardBlueprint.cardBlueprintId);
        });
      },
      addCardBlueprints: (cardBlueprints: CardBlueprint[]) => {
        set((state) => {
          cardBlueprints.forEach((cardBlueprint) => {
            state.cardBlueprints.set(
              cardBlueprint.cardBlueprintId,
              cardBlueprint
            );
            state.cardIds.push(cardBlueprint.cardBlueprintId);
          });
        });
      },
    }))
  )
);
