import { Check } from '@mui/icons-material';
import Option, { optionClasses } from '@mui/joy/Option';
import ListItemDecorator, {
  listItemDecoratorClasses,
} from '@mui/joy/ListItemDecorator';
import {
  Chip,
  List,
  ListDivider,
  ListItem,
  Select,
  Typography,
} from '@mui/joy';
import React from 'react';
import {
  decipherSetExpansionMetadata,
  otherFormatMetadata,
  playtestSetExpansionMetadata,
  commonFormatsMetadata,
  allExpansionsMetadata,
} from '../../../../lotr-common/types/expansions';
import { CardFilterDropdownProps } from './CardFilter-props.interface';
import { Mappings } from '../../../types/mappings.interface';

const sections = {
  'Common Formats': commonFormatsMetadata,
  'Other Formats': otherFormatMetadata,
  'Individual Sets': decipherSetExpansionMetadata,
  Playtest: playtestSetExpansionMetadata,
};

const allOptions = Object.values(allExpansionsMetadata);

function ExpansionsFilter({
  selectedValue,
  filterChanged,
}: CardFilterDropdownProps<Mappings>) {
  const handleOnChange = (newValue: string): void => {
    filterChanged(allOptions.find((option) => option.displayName === newValue));
  };
  return (
    <Select
      sx={{ width: '100%' }}
      placeholder="Filter by Set"
      value={selectedValue?.displayName}
      onChange={(e, newValue) => handleOnChange(newValue?.toString() || '')}
      slotProps={{
        listbox: {
          component: 'div',
          sx: {
            maxHeight: 240,
            overflow: 'auto',
            '--List-padding': '0px',
          },
        },
      }}
    >
      {Object.entries(sections).map(([name, options], index) => {
        const sectionOptions = Object.entries(options);
        return (
          <React.Fragment key={name}>
            {index !== 0 && <ListDivider role="none" />}
            <List
              aria-labelledby={`select-group-${name}`}
              sx={{ '--ListItemDecorator-size': '28px' }}
            >
              <ListItem id={`select-group-${name}`} sticky>
                <Typography
                  level="body3"
                  textTransform="uppercase"
                  letterSpacing="md"
                >
                  {name} ({sectionOptions.length})
                </Typography>
              </ListItem>
              {sectionOptions.map(([_, option]) => (
                <Option
                  key={option.apiName}
                  value={option.displayName}
                  label={
                    <>
                      <Chip
                        size="sm"
                        color="primary"
                        sx={{ borderRadius: 'xs', mr: 1, ml: -0.5 }}
                      >
                        {name}
                      </Chip>{' '}
                      {option.displayName}
                    </>
                  }
                  sx={{
                    [`&.${optionClasses.selected} .${listItemDecoratorClasses.root}`]:
                      {
                        opacity: 1,
                      },
                  }}
                >
                  <ListItemDecorator sx={{ opacity: 0 }}>
                    <Check />
                  </ListItemDecorator>
                  {option.displayName}
                </Option>
              ))}
            </List>
          </React.Fragment>
        );
      })}
    </Select>
  );
}

export default ExpansionsFilter;
