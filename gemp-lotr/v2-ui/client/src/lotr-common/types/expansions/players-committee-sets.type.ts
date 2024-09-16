import { Mappings } from '../../../common/types/mappings.interface';

export type PlayersCommitteeSets =
  | 'HobbitMain'
  | 'HobbitExpansionOne'
  | 'HobbitExpansionTwo'
  | 'HobbitExpansionThree'
  | 'ShadowsOfThePast';

export const playersCommitteeExpansionMetadata: Record<
  PlayersCommitteeSets,
  Mappings
> = {
  HobbitMain: { apiName: '30', displayName: '30 - The Hobbit: Main Deck' },
  HobbitExpansionOne: {
    apiName: '31',
    displayName: '31 - The Hobbit: Expansion 1',
  },
  HobbitExpansionTwo: {
    apiName: '32',
    displayName: '32 - The Hobbit: Expansion 2',
  },
  HobbitExpansionThree: {
    apiName: '33',
    displayName: '33 - The Hobbit: Expansion 3',
  },
  ShadowsOfThePast: {
    apiName: '150,151',
    displayName: 'V1 - Shadow of the Past (PC)',
  },
};
