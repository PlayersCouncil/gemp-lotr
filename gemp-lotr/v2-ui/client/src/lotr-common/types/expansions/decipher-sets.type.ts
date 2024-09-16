import { Mappings } from '../../../common/types/mappings.interface';

export type DecipherSets =
  | 'Promo'
  | 'FellowshipOfTheRing'
  | 'MinesOfMoria'
  | 'RealmsOfTheElfLords'
  | 'TheTwoTowers'
  | 'BattleOfHelmsDeep'
  | 'EntsOfFangorn'
  | 'ReturnOfTheKing'
  | 'SiegeOfGondor'
  | 'Reflections'
  | 'MtDoom'
  | 'Shadows'
  | 'BlackRider'
  | 'Bloodlines'
  | 'ExpandedMiddleEarth'
  | 'Hunters'
  | 'WraithCollection'
  | 'RiseOfSaruman'
  | 'TreacheryAndDeceit'
  | 'AgesEnd';

export const decipherSetExpansionMetadata: Record<DecipherSets, Mappings> = {
  Promo: { displayName: 'Promo', apiName: '00' },
  FellowshipOfTheRing: {
    displayName: '01 - The Fellowship of the Ring',
    apiName: '01',
  },
  MinesOfMoria: { displayName: '02 - Mines of Moria', apiName: '02' },
  RealmsOfTheElfLords: {
    displayName: '03 - Realms of the Elf-lords',
    apiName: '03',
  },
  TheTwoTowers: { displayName: '04 - The Two Towers', apiName: '04' },
  BattleOfHelmsDeep: {
    displayName: "05 - Battle of Helm's Deep",
    apiName: '05',
  },
  EntsOfFangorn: { displayName: '06 - Ents of Fangorn', apiName: '06' },
  ReturnOfTheKing: {
    displayName: '07 - The Return of the King',
    apiName: '07',
  },
  SiegeOfGondor: { displayName: '08 - Siege of Gondor', apiName: '08' },
  Reflections: { displayName: '09 - Reflections', apiName: '09' },
  MtDoom: { displayName: '10 - Mount Doom', apiName: '10' },
  Shadows: { displayName: '11 - Shadows', apiName: '11' },
  BlackRider: { displayName: '12 - Black Rider', apiName: '12' },
  Bloodlines: { displayName: '13 - Bloodlines', apiName: '13' },
  ExpandedMiddleEarth: {
    displayName: '14 - Expanded Middle-earth',
    apiName: '14',
  },
  Hunters: { displayName: '15 - The Hunters', apiName: '15' },
  WraithCollection: {
    displayName: '16 - The Wraith Collection',
    apiName: '16',
  },
  RiseOfSaruman: { displayName: '17 - Rise of Saruman', apiName: '17' },
  TreacheryAndDeceit: { displayName: '18 - Treachery & Deceit', apiName: '18' },
  AgesEnd: { displayName: '19 - Ages End', apiName: '19' },
};
