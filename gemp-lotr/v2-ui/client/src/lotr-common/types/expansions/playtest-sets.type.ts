import { Mappings } from '../../../common/types/mappings.interface';

export type PlaytestSets =
  | 'AllPlaytest'
  | 'ExpandedPlaytest'
  | 'FellowshipPlaytest'
  | 'MovieBlockPlaytest';

export const playtestSetExpansionMetadata: Record<PlaytestSets, Mappings> = {
  AllPlaytest: {
    displayName: "All Player's Council Playtest Cards",
    apiName: '70-89,100,150-200',
  },
  ExpandedPlaytest: {
    displayName: 'PLAYTEST - Expanded (PC)',
    apiName: 'test_pc_fotr_block',
  },
  FellowshipPlaytest: {
    displayName: 'PLAYTEST - Fellowship Block (PC)',
    apiName: 'test_pc_fotr',
  },
  MovieBlockPlaytest: {
    displayName: 'PLAYTEST - Movie Block (PC)',
    apiName: 'test_pc_movie',
  },
};
