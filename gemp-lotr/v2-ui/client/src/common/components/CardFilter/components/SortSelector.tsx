import { lotrSortTypeMappings } from '../../../../lotr-common/types/filter-types/sort-types';
import GempDropdown from '../../GempDropdown/GempDropdown';
import { CardFilterDropdownProps } from './CardFilter-props.interface';

function SortSelector({ filterChanged }: CardFilterDropdownProps) {
  return (
    <GempDropdown
      placeholder="Sort by"
      selectionChanged={filterChanged}
      options={Object.values(lotrSortTypeMappings)}
    />
  );
}

export default SortSelector;
