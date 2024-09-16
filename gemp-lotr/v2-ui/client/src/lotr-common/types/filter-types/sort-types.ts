import { Mappings } from '../../../common/types/mappings.interface';

enum LotrSortingTypes {
  Name = 'Name',
  Culture = 'Culture',
  Twilight = 'Twilight',
  SiteNumber = 'SiteNumber',
  Strength = 'Strength',
  Vitality = 'Vitality',
  CardType = 'CardType',
}

export const lotrSortTypeMappings: Record<LotrSortingTypes, Mappings> = {
  Name: { apiName: 'name', displayName: 'Name' },
  Culture: { apiName: 'culture,name', displayName: 'Culture' },
  Twilight: { apiName: 'twilight,name', displayName: 'Twilight' },
  Strength: { apiName: 'strength,name', displayName: 'Strength' },
  Vitality: { apiName: 'vitality,name', displayName: 'Vitality' },
  CardType: { apiName: 'cardType,name', displayName: 'Card Type' },
  SiteNumber: { apiName: 'siteNumber,name', displayName: 'Site Number' },
};
