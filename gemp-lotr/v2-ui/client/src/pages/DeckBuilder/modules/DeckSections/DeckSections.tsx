import { Grid, TabList, TabPanel, Tabs } from '@mui/joy';
import * as React from 'react';
import { Deck } from '../../../../common/types/deck/Deck';
import LocationDeckSection from './components/LocationDeckSection/LocationDeckSection';
import RingBearerDeckSection from './components/RingBearerDeckSection';
import TabWithStatus from './components/TabWithStatus';
import useLotrDeckValidity, {
  LotrDeckValidity,
} from '../../../../lotr-common/types/deck-validity/useLotrDeckValidity';
import { useDeckBuilderStore } from '../../state/deckbuilder-state';
import { PlayerSideDeckSection } from './components/PlayerSideDeckSection';
import { Mappings } from '../../../../common/types/mappings.interface';

type DeckbuilderProps = {
  deck: Deck;
  selectedFormat: Mappings;
  filterRequest: (filterName: string, additionalFilter?: string) => void;
};

const getTabNames = (isValid: LotrDeckValidity) => [
  {
    tabName: 'Ring and Ring-Bearer',
    status: isValid.deckValidity.ringbearer.status,
  },
  {
    tabName: 'Sites',
    status: isValid.deckValidity.sitePath.status,
  },
  {
    tabName: 'Free People',
    status: isValid.deckValidity.freePeoples.status,
  },
  {
    tabName: 'Shadow',
    status: isValid.deckValidity.twilight.status,
  },
];

export default function DeckSections(props: DeckbuilderProps) {
  const { deck } = props;
  const [currentTabIndex, setCurrentTabIndex] = React.useState(0);
  const { filterRequest, selectedFormat } = props;
  const isValid = useLotrDeckValidity(deck, selectedFormat);
  const { ring, ringBearer, sites, freePeople, shadow } = useDeckBuilderStore();
  const handleChange = (
    _: React.SyntheticEvent | null,
    newValue: string | number | null
  ) => {
    setCurrentTabIndex(+(newValue ?? 0));
  };
  return (
    <Grid
      display="flex"
      flexDirection="column"
      sx={(theme) => ({
        boxShadow: theme.shadow.lg,
        backgroundColor: 'neutral.dark',
        height: '100%',
      })}
    >
      <Tabs
        value={currentTabIndex}
        onChange={handleChange}
        sx={{ height: '100%' }}
      >
        <TabList>
          {getTabNames(isValid).map((tab) => (
            <TabWithStatus
              key={tab.tabName}
              tabName={tab.tabName}
              status={tab.status}
            />
          ))}
        </TabList>
        <TabPanel value={0}>
          <RingBearerDeckSection
            ring={ring}
            ringBearer={ringBearer}
            updateFilter={filterRequest}
          />
        </TabPanel>
        <TabPanel value={1}>
          <LocationDeckSection
            updateFilteredSites={(siteFilter) =>
              filterRequest('site', siteFilter)
            }
            selectedSites={sites}
          />
        </TabPanel>
        <TabPanel value={2}>
          <PlayerSideDeckSection cards={freePeople} />
        </TabPanel>
        <TabPanel value={3}>
          <PlayerSideDeckSection cards={shadow} />
        </TabPanel>
      </Tabs>
    </Grid>
  );
}
