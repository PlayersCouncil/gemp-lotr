import { Cheerio, load, Element } from 'cheerio';
import { CardBlueprint } from '../../types/lotr-card/card-blueprint.interface';
import { getBlueprintByCardId } from '../../types/lotr-card/lotr-card-formatting.functions';
import { LotrCollectionCardGroups } from '../../types/card/lotr-card-collection-groups.type';

export interface CollectionCardViewModel extends CardBlueprint {
  /** Either the number of this card that exists in the current collection call OR the number of copies of this card available */
  count: number;
}

export interface CollectionApiResponse {
  cards: CollectionCardViewModel[];
  count: number;
}

export const convertCollectionCardXmlToViewModel = (
  input: Element,
  areResultsHorizontal = false
): CollectionCardViewModel => {
  const bp = input.attribs.blueprintId ?? input.attribs.blueprintid;
  return {
    ...getBlueprintByCardId(
      bp,
      input.attribs.group as LotrCollectionCardGroups,
      areResultsHorizontal
    ),
    count: Number(input.attribs.count),
  };
};

export const convertCardsToViewModel = (
  cardElements: Cheerio<Element>,
  areResultsHorizontal = false
): CollectionCardViewModel[] => {
  const cards: CollectionCardViewModel[] = [];
  cardElements.each((_, card) => {
    cards.push(convertCollectionCardXmlToViewModel(card, areResultsHorizontal));
  });

  return cards;
};

export const convertGetCollectionFromXml = (
  xml: string,
  areResultsHorizontal = false
): CollectionApiResponse => {
  const data = load(xml, {
    xmlMode: true,
  });
  const cardsElements = data('collection').children('card');
  return {
    count: Number(data('collection').attr('count')),
    cards: convertCardsToViewModel(cardsElements, areResultsHorizontal),
  };
};
