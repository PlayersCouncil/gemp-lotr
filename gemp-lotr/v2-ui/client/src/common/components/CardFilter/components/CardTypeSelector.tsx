import { lotrCardTypeFilterMappings } from '../../../../lotr-common/types/filter-types/card-types';
import GempDropdown from '../../GempDropdown/GempDropdown';
import { CardFilterDropdownProps } from './CardFilter-props.interface';

function CardTypeSelector(props: CardFilterDropdownProps) {
  const { selectedValue, filterChanged } = props;
  return (
    <GempDropdown
      selectedValue={selectedValue}
      selectionChanged={filterChanged}
      placeholder="Filter by Card Type"
      options={Object.values(lotrCardTypeFilterMappings)}
    />
  );
}

export default CardTypeSelector;
