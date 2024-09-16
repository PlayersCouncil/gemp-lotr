import { Option, Select } from '@mui/joy';
import { Mappings } from '../../types/mappings.interface';

interface GempDropdownProps {
  options: Mappings[];
  placeholder: string;
  selectionChanged: (selection: Mappings) => void;
  selectedValue?: Mappings;
}

function GempDropdown({
  options,
  placeholder = '',
  selectionChanged,
  selectedValue,
}: GempDropdownProps) {
  const handleOnChange = (newValue: string): void => {
    const index = options.findIndex((option) => option.apiName === newValue);
    if (index === -1) return;
    selectionChanged(options[index]);
  };
  return (
    <Select
      sx={{ width: '100%' }}
      placeholder={placeholder}
      onChange={(_, newValue) => handleOnChange(newValue || '')}
      // onChange={(_, newValue) => selectionChanged(newValue?.toString() || '')}
      value={selectedValue?.apiName}
    >
      {options.map(({ apiName, displayName }) => (
        <Option key={apiName} value={apiName}>
          {displayName}
        </Option>
      ))}
    </Select>
  );
}

export default GempDropdown;
