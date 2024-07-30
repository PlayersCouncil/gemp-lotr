import Box from '@mui/material/Box';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import { Input, Typography } from '@mui/joy';
import { useEffect } from 'react';
import CardTypeSelector from '../../../../common/components/CardFilter/components/CardTypeSelector';
import RaritySelector from '../../../../common/components/CardFilter/components/RaritySelector';
import ExpansionsFilter from '../../../../common/components/CardFilter/components/ExpansionsFilter';
import KeywordSelector from '../../../../common/components/CardFilter/components/KeywordSelector';
import { CollectionFiltersViewModel } from '../../../../lotr-common/api/collection-api/collection-api-parameters.interface';
import { CultureOptionContainer } from './components/CultureOptionContainer/CultureOptionContainer';
import { getCulturesBySelectedSet } from '../../../../lotr-common/types/cultures/culture.functions';
import { FilterActions } from './components/FilterActions/FilterActions';
import { useFilterListState } from './types/useFilterListState';

interface FiltersListProps {
  appliedFilters: CollectionFiltersViewModel;
  applyFilters: ({
    rarity,
    cardTypes,
    keywords,
    format,
    cultures,
    cardName,
  }: Partial<CollectionFiltersViewModel>) => void;
}

export default function FiltersList(props: FiltersListProps) {
  const { appliedFilters, applyFilters } = props;
  const {
    cardName,
    cardTypes,
    format,
    freeCultures,
    keywords,
    rarity,
    twilightCultures,
    setPreviousFilters,
    updateFilter,
  } = useFilterListState(appliedFilters);

  const handleSubmitFilters = (): void => {
    applyFilters({
      cardName,
      cardTypes,
      format,
      keywords,
      rarity,
      cultures: [...freeCultures, ...twilightCultures],
    });
  };

  useEffect(() => {
    setPreviousFilters(appliedFilters);
  }, [appliedFilters, setPreviousFilters]);

  const handleResetFilters = (): void => {
    // basically set back to inital state....
  };

  return (
    <Box sx={{ width: 400, p: 1 }} role="presentation">
      <Typography level="h3">Filters</Typography>
      <List>
        <ListItem>
          <Input
            sx={{ width: '100%' }}
            placeholder="Title"
            value={cardName}
            onChange={(e) => updateFilter('cardName', e.target.value)}
          />
        </ListItem>
        <ListItem>
          <CultureOptionContainer
            title="Free People Cultures"
            cultures={getCulturesBySelectedSet(format).freePeople}
            selectionUpdated={(update) => updateFilter('freeCultures', update)}
            currentSelectedCultures={[...freeCultures, ...twilightCultures]}
          />
        </ListItem>
        <ListItem>
          <CultureOptionContainer
            title="Shadow Cultures"
            cultures={getCulturesBySelectedSet(format).twilight}
            selectionUpdated={(update) =>
              updateFilter('twilightCultures', update)
            }
            currentSelectedCultures={[...freeCultures, ...twilightCultures]}
          />
        </ListItem>
        <ListItem>
          <RaritySelector
            selectedValue={rarity}
            filterChanged={(update) => updateFilter('rarity', update)}
          />
        </ListItem>
        <ListItem>
          <CardTypeSelector
            selectedValue={cardTypes}
            filterChanged={(update) => updateFilter('cardTypes', update)}
          />
        </ListItem>
        <ListItem>
          <KeywordSelector
            selectedValue={keywords}
            filterChanged={(update) => updateFilter('keywords', update)}
          />
        </ListItem>
        <ListItem>
          <ExpansionsFilter
            selectedValue={format}
            filterChanged={(update) => updateFilter('format', update)}
          />
        </ListItem>
        {/* <ListItem>
          <SortSelector filterChanged={setSort} />
        </ListItem> */}
      </List>
      <FilterActions
        applyFilters={handleSubmitFilters}
        resetFilter={handleResetFilters}
      />
    </Box>
  );
}
