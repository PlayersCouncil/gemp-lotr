import { Mappings } from '../../../types/mappings.interface';

export interface CardFilterDropdownProps<T = Mappings> {
  selectedValue?: T;
  filterChanged: (filterValue: T) => void;
}
