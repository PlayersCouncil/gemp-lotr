// TODO: REFACTORING NOTE. This is the pattern we are going with.
// a private enum that string => string maps. (Or we could do an array of the items)
// exposing a Record that maps from the enum to the mappings.
// that would let us do Object.entries(foobar).map(([name, mappings])) and get the api or display name
// it would also allow foobar.Value.apiName to get it directly if we wnat to do logic for a specific thing
// example would be "if activeSet === sets.Hobbit.apiName then ......"

import { Mappings } from '../../../common/types/mappings.interface';

enum PossessionTypes {
  HandWeapon = 'HandWeapon',
  Armor = 'Armor',
  Shield = 'Shield',
  Helm = 'Helm',
  Mount = 'Mount',
  RangedWeapon = 'RangedWeapon',
  Cloak = 'Cloak',
  Pipe = 'Pipe',
  Bracers = 'Bracers',
  Staff = 'Staff',
  Ring = 'Ring',
  Brooch = 'Brooch',
  Gauntlets = 'Gauntlets',
  Box = 'Box',
  Palantir = 'Palatir',
  Phial = 'Phial',
  Horn = 'Horn',
  Classless = 'Classless',
  Pony = 'Pony',
}

export const possesionTypeMappings: Record<PossessionTypes, Mappings> = {
  [PossessionTypes.HandWeapon]: {
    displayName: 'Hand Weapon',
    apiName: 'hand_weapon',
  },
  [PossessionTypes.Armor]: {
    displayName: 'Armor',
    apiName: 'armor',
  },
  [PossessionTypes.Shield]: {
    displayName: 'Shield',
    apiName: 'shield',
  },
  [PossessionTypes.Helm]: {
    displayName: 'Helm',
    apiName: 'helm',
  },
  [PossessionTypes.Mount]: {
    displayName: 'Mount',
    apiName: 'mount',
  },
  [PossessionTypes.RangedWeapon]: {
    displayName: 'Ranged Weapon',
    apiName: 'ranged_weapon',
  },
  [PossessionTypes.Cloak]: {
    displayName: 'Cloak',
    apiName: 'cloak',
  },
  [PossessionTypes.Pipe]: {
    displayName: 'Pipe',
    apiName: 'pipe',
  },
  [PossessionTypes.Bracers]: {
    displayName: 'Bracers',
    apiName: 'bracers',
  },
  [PossessionTypes.Staff]: {
    displayName: 'Staff',
    apiName: 'staff',
  },
  [PossessionTypes.Ring]: {
    displayName: 'Ring',
    apiName: 'ring',
  },
  [PossessionTypes.Brooch]: {
    displayName: 'Brooch',
    apiName: 'brooch',
  },
  [PossessionTypes.Gauntlets]: {
    displayName: 'Gauntlets',
    apiName: 'gauntlets',
  },
  [PossessionTypes.Box]: {
    displayName: 'Box',
    apiName: 'box',
  },
  [PossessionTypes.Palantir]: {
    displayName: 'Palantir',
    apiName: 'palantir',
  },
  [PossessionTypes.Phial]: {
    displayName: 'Phial',
    apiName: 'phial',
  },
  [PossessionTypes.Horn]: {
    displayName: 'Horn',
    apiName: 'horn',
  },
  [PossessionTypes.Classless]: {
    displayName: 'Classless',
    apiName: 'classless',
  },
  [PossessionTypes.Pony]: {
    displayName: 'Pony',
    apiName: 'pony',
  },
};
