import axios from 'axios';
import { Deck } from '../../../common/types/deck/Deck';
import { Mappings } from '../../../common/types/mappings.interface';

const convertDeckToDeckContentsString = (deck: Deck): string => {
  let deckContents = '';
  deckContents += `${deck.ring?.cardBlueprintId}|${deck.ringBearer?.cardBlueprintId}|`;
  // do sites
  deckContents += deck.cards.join(',');
  return deckContents;
};

export const saveDeck = async (deck: Deck, targetFormat: Mappings) => {
  const body = {
    participantId: null,
    targetFormat: targetFormat.apiName,
    deckContents: convertDeckToDeckContentsString(deck),
  };

  await axios.post('/gemp-lotr-server/deck', body, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    data: body,
  });
};
