import { Mappings } from '../../../common/types/mappings.interface';

export type OtherFormats =
  | 'EnhancedTowersStandard'
  | 'Expanded'
  | 'ExpandedPc'
  | 'FellowshipBlock'
  | 'FellowshipBlockPc'
  | 'French'
  // | 'Hobbit'
  | 'HuntersBlock'
  | 'KingBlock'
  | 'KingStandard'
  | 'Movie'
  | 'MovieBlockPc'
  // | 'PcErrata'
  // | 'PcVsets'
  | 'Standard'
  | 'TowersBlock'
  | 'TowersStandard'
  | 'WarOftheRingBlock'
  | 'WarOfTheRingStandard';

export type CommonFormats =
  | 'All'
  | 'AllPc'
  | 'DeciperSets'
  | 'HobbitSets'
  | 'AllPcErrata'
  | 'AllPcVSets';

export const commonFormatsMetadata: Record<CommonFormats, Mappings> = {
  All: { displayName: 'All Sets', apiName: '0-34,50-200' },
  AllPc: {
    displayName: "All Player's Council Cards",
    apiName: '50-69,100-149',
  },
  DeciperSets: { displayName: 'Official Decipher Sets', apiName: '0-19' },
  HobbitSets: { displayName: 'The Hobbit Sets', apiName: '30-33' },
  AllPcErrata: { displayName: "All Player's Errata", apiName: '50-69,100' },
  AllPcVSets: { displayName: "All Player's VSets", apiName: '100-149' },
};

export const otherFormatMetadata: Record<OtherFormats, Mappings> = {
  EnhancedTowersStandard: {
    displayName: 'Enhanced Towers Standard',
    apiName: 'ts_reflections',
  },
  Expanded: { displayName: 'Expanded', apiName: 'expanded' },
  ExpandedPc: { displayName: 'Expanded (PC)', apiName: 'pc_expanded' },
  FellowshipBlock: { displayName: 'Fellowship Block', apiName: 'fotr_block' },
  FellowshipBlockPc: {
    displayName: 'Fellowship Block (PC)',
    apiName: 'pc_fotr_block',
  },
  French: { displayName: 'French Format', apiName: 'french' },
  // Hobbit: { displayName: 'The Hobbit Sets', value: '30-33' },
  HuntersBlock: { displayName: 'Hunters Block', apiName: 'hunter_block' },
  KingBlock: { displayName: 'King Block', apiName: 'king_block' },
  KingStandard: { displayName: 'King Standard', apiName: 'rotk_sta' },
  Movie: { displayName: 'Movie Block', apiName: 'movie' },
  MovieBlockPc: { displayName: 'Movie Block (PC)', apiName: 'pc_movie' },
  // PcErrata: { displayName: "Player's Council Errata", value: '50-69,100' },
  // PcVsets: { displayName: "Player's Council VSets", value: '100-149' },
  Standard: { displayName: 'Standard', apiName: 'standard' },
  TowersBlock: { displayName: 'Towers Block', apiName: 'ttt_block' },
  TowersStandard: {
    displayName: 'Towers Standard',
    apiName: 'towers_standard',
  },
  WarOftheRingBlock: {
    displayName: 'War of the Ring Block',
    apiName: 'war_block',
  },
  WarOfTheRingStandard: {
    displayName: 'War of the Ring Standard',
    apiName: 'war_standard',
  },
};
