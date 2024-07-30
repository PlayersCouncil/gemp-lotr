import { Mappings } from '../../../common/types/mappings.interface';

export type PlayersCommitteeErrata =
  | 'AgesEndErrata'
  | 'BloodlinesErrata'
  | 'ElfLordsErrata'
  | 'FellowshipErrata'
  | 'HuntersErrata'
  | 'MoriaErrata'
  | 'ReflectionsErrata'
  | 'ReturnOfTheKingErrata'
  | 'ShadowsErrata'
  | 'SiegeOfGondorErrata'
  | 'TwoTowerErrata';

export const playersCommitteeErrataMetadata: Record<
  PlayersCommitteeErrata,
  Mappings
> = {
  AgesEndErrata: { displayName: '19E - Ages End (PC Errata)', apiName: '69' },
  BloodlinesErrata: {
    displayName: '13E - Bloodlines (PC Errata)',
    apiName: '63',
  },
  ElfLordsErrata: {
    displayName: '03E - Realms of the Elf-lords (PC Errata)',
    apiName: '53',
  },
  FellowshipErrata: {
    displayName: '01E - The Fellowship of the Ring (PC Errata)',
    apiName: '51',
  },
  HuntersErrata: {
    displayName: '15E - The Hunters (PC Errata)',
    apiName: '65',
  },
  MoriaErrata: {
    displayName: '02E - Mines of Moria (PC Errata)',
    apiName: '52',
  },
  ReflectionsErrata: {
    displayName: '09E - Reflections (PC Errata)',
    apiName: '59',
  },
  ReturnOfTheKingErrata: {
    displayName: '07E - The Return of the King (PC Errata)',
    apiName: '57',
  },
  ShadowsErrata: { displayName: '11E - Shadows (PC Errata)', apiName: '61' },
  SiegeOfGondorErrata: {
    displayName: '08E - Siege of Gondor (PC Errata)',
    apiName: '58',
  },
  TwoTowerErrata: {
    displayName: '04E - The Two Towers (PC Errata)',
    apiName: '54',
  },
};
