import {
  formatCardNumber,
  formatSetNumber,
} from '../lotr-card-formatting.functions';
import { mainImageLocation, rulesImageHref } from '../lotr-card.constants';

export const generateImageFileName = (
  setNumber: number,
  cardNumber: number
): string => {
  return `LOTR${formatSetNumber(setNumber)}${formatCardNumber(cardNumber)}`;
};

const generateImageUrl = (setNumber: number, cardNumber: number): string => {
  return `${mainImageLocation}/${generateImageFileName(
    setNumber,
    cardNumber
  )}.jpg`;
};

export const getCardImage = (blueprintId: string): string | undefined => {
  if (blueprintId === 'rules') {
    return rulesImageHref;
  }
  const [setNumber, cardNumber] = blueprintId.split('_');

  return generateImageUrl(Number(setNumber), Number(cardNumber));
};
