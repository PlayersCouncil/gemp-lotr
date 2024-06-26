import { LotrCollectionCardGroups } from '../card/lotr-card-collection-groups.type';
import { DecipherSets, PlayersCommitteeSets } from '../expansions';

export type CardId = number | string;

/** This descrbies the abstract concept of the card. All cards must have these things */
export interface CardBlueprint {
  cardBlueprintId: CardId;

  /** The number of the card in the set (eg. the 12th card in set 1 is 1_12 */
  cardNumber: number;
  isHorizontal: boolean;
  imageUrl: string;

  // this should be on the LOTR part, but I sceweed up somewhere.
  set: DecipherSets | PlayersCommitteeSets;
  formattedCardNumber: string;
  formattedSetNumber: string;
  group: LotrCollectionCardGroups;
}
