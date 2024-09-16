import { Mappings } from '../../../common/types/mappings.interface';

enum CardRarity {
  'All' = 'All',
  'Common' = 'Common',
  'Uncommon' = 'Uncommon',
  'Rare' = 'Rare',
  'RarePlus' = 'RarePlus',
  'AlternateImage' = 'AlternateImage',
  'Promo' = 'Promo',
  'Fixed' = 'Fixed',
  'Poormans' = 'Poormans',
}

export const rarityMappings: Record<CardRarity, Mappings> = {
  All: {
    apiName: '',
    displayName: 'All',
  },
  Common: {
    apiName: 'C',
    displayName: 'Common',
  },
  Uncommon: {
    apiName: 'U',
    displayName: 'Uncommon',
  },
  Rare: {
    apiName: 'R',
    displayName: 'Rare',
  },
  RarePlus: {
    apiName: 'X',
    displayName: 'Rare+',
  },
  AlternateImage: {
    apiName: 'A',
    displayName: 'Alternate Image',
  },
  Promo: {
    apiName: 'P',
    displayName: 'Promo',
  },
  Fixed: {
    apiName: 'S',
    displayName: 'Fixed',
  },
  Poormans: {
    apiName: 'C,U,P,S',
    displayName: "Poorman's",
  },
};
