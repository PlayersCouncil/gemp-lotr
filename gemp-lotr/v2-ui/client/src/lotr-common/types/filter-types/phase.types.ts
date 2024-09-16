import { Mappings } from '../../../common/types/mappings.interface';

enum TurnPhase {
  Fellowship = 'Fellowship',
  Skirmish = 'Skirmish',
  Response = 'Response',
  Maneuver = 'Maneuver',
  Archery = 'Archery',
  Shadow = 'Shadow',
  Assignment = 'Assignment',
  Regroup = 'Regroup',
}

export const turnPhaseMappings: Record<TurnPhase, Mappings> = {
  Fellowship: {
    apiName: 'fellowship',
    displayName: 'Fellowship',
  },
  Skirmish: {
    apiName: 'skirmish',
    displayName: 'Skirmish',
  },
  Response: {
    apiName: 'response',
    displayName: 'Response',
  },
  Maneuver: {
    apiName: 'maneuver',
    displayName: 'Maneuver',
  },
  Archery: {
    apiName: 'archery',
    displayName: 'Archery',
  },
  Shadow: {
    apiName: 'shadow',
    displayName: 'Shadow',
  },
  Assignment: {
    apiName: 'assignment',
    displayName: 'Assignment',
  },
  Regroup: {
    apiName: 'regroup',
    displayName: 'Regroup',
  },
};
