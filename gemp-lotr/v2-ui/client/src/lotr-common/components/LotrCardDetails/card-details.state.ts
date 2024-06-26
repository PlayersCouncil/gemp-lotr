import { create } from 'zustand';
import { CardId } from '../../types/lotr-card/card-blueprint.interface';

function doUpdateCurrentCard(state: CardDetailModalStore, pointer: string) {
  const currentIndex = state.displayedCardId
    ? state.availableCardIds.indexOf(state.displayedCardId)
    : 0;
  let nextIndex = currentIndex;
  switch (pointer) {
    case 'next':
      if (currentIndex === state.availableCardIds.length) {
        nextIndex = 0;
      } else {
        nextIndex = currentIndex + 1;
      }
      break;
    case 'previous':
    default:
      if (currentIndex === 0) {
        nextIndex = state.availableCardIds.length - 1;
      } else {
        nextIndex = currentIndex - 1;
      }
      break;
  }
  return { ...state, displayedCardId: state.availableCardIds[nextIndex] };
}

interface CardDetailModalState {
  availableCardIds: Array<CardId>;
  displayedCardId: CardId;
  hoverImage: CardId | null;
}

interface CardDetailModalActions {
  updateCurrentCard: (pointer: 'next' | 'previous') => void;
  iniatlizeModal: (cardId: CardId, cardIds: Array<CardId>) => void;

  setHoverImage(cardId: CardId | null): void;
}

type CardDetailModalStore = CardDetailModalState & CardDetailModalActions;

export const useCardDetailStore = create<CardDetailModalStore>()((set) => ({
  availableCardIds: [],
  displayedCardId: '',
  hoverImage: null,
  setHoverImage: (cardId: CardId | null) =>
    set((state) => {
      return { ...state, hoverImage: cardId };
    }),
  iniatlizeModal: (cardId: CardId, availableCardIds: Array<CardId>) =>
    set((state) => ({ ...state, displayedCardId: cardId, availableCardIds })),
  updateCurrentCard: (pointer: 'next' | 'previous') =>
    set((state) => {
      return doUpdateCurrentCard(state, pointer);
    }),
}));
