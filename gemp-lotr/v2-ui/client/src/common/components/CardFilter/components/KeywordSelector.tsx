import { lotrCardKeywordTypeMappings } from '../../../../lotr-common/types/filter-types/keyword.types';
import GempDropdown from '../../GempDropdown/GempDropdown';
import { CardFilterDropdownProps } from './CardFilter-props.interface';

function KeywordSelector({
  filterChanged,
  selectedValue,
}: CardFilterDropdownProps) {
  return (
    <GempDropdown
      selectedValue={selectedValue}
      selectionChanged={filterChanged}
      placeholder="Filter by Keyword"
      options={Object.values(lotrCardKeywordTypeMappings)}
    />
  );
}

export default KeywordSelector;
