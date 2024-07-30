import Drawer from '@mui/material/Drawer';
import { useEffect, useState } from 'react';
import { Box, Grid } from '@mui/joy';
import { CollectionFiltersViewModel } from '../../lotr-common/api/collection-api/collection-api-parameters.interface';
import LotrCardDetails from '../../lotr-common/components/LotrCardDetails/LotrCardDetails';
import { Deck, createDefaultDeck } from '../../common/types/deck/Deck';
import {
  createLocationFilters,
  createOneRingFilters,
  createRingbearerFilters,
} from '../../lotr-common/api/collection-api/collection-filter-view-model.functions';
import { LotrLocationNames } from '../../common/types/LotrLocations/lotr-locations.type';
import { useCardQueryStore } from '../../lotr-common/state/card-filter.state';
import FilterToggle from '../DeckManager/components/FilterToggle/FilterToggle';
import CardBank from '../DeckManager/modules/CardBank/CardBank';
import FiltersList from '../DeckManager/modules/FilterSelector/FilterList';
import { useCardDetailStore } from '../../lotr-common/components/LotrCardDetails/card-details.state';
import DeckSections from './modules/DeckSections/DeckSections';
import DeckBuilderMenu from './components/DeckBuilderMenu';
import { CardBlueprint, CardId } from '../../lotr-common/types/lotr-card/card-blueprint.interface';
import { useCardBlueprintStore } from '../../lotr-common/state/card-state';
import { executeGetCollectionByFilterRequest } from '../../lotr-common/api/collection-api/collection-api.configuration';
import { useDeckBuilderStore } from './state/deckbuilder-state';
import { getCurrentDeck } from './state/deckbuilder-state.selectors';
import { CardDetailsHover } from '../../lotr-common/components/CardDetailsHover/CardDetailsHover';
import { saveDeck } from '../../lotr-common/api/deck-api/save-deck-api.function';
import { LotrSiteCardBlueprint } from '../../lotr-common/types/lotr-card/lotr-site-card';

// Note to self: this is hot ass garbage, i'm too lazy to figure how I'd rather do this.
const locationNotBeingUsed = 'no-active-location';

type SiteSelectionOptions = LotrLocationNames | 'no-active-location';

export default function DeckBuilderContainer() {
  const { updateFilter, filters, results, setResults, pageInformation } =
    useCardQueryStore();
  const { cardBlueprints, addCardBlueprints } = useCardBlueprintStore();

  const [isOpen, setIsOpen] = useState(false);
  const [currentLocation, setCurrentLocation] =
    useState<SiteSelectionOptions>(locationNotBeingUsed);
  const { addCardToDeck } = useDeckBuilderStore();
  const deck = useDeckBuilderStore(getCurrentDeck);
  const [cardModalState, setCardModalState] = useState<{
    isOpen: boolean;
    cardId: CardId;
    availableCards: Array<CardId>;
  }>({ isOpen: false, cardId: '', availableCards: [] });

  const { iniatlizeModal, hoverImage } = useCardDetailStore();

  const closeModal = (): void => {
    setCardModalState({ isOpen: false, cardId: '', availableCards: [] });
  };

  useEffect(() => {
    (async () => {
      const apiResults = await executeGetCollectionByFilterRequest(
        filters,
        pageInformation
      );
      setResults(apiResults);
      addCardBlueprints(apiResults);
    })();
  }, [filters, setResults, addCardBlueprints, pageInformation]);

  const toggleDrawer =
    (open: boolean) => (event: React.KeyboardEvent | React.MouseEvent) => {
      if (
        event.type === 'keydown' &&
        ((event as React.KeyboardEvent).key === 'Tab' ||
          (event as React.KeyboardEvent).key === 'Shift')
      ) {
        return;
      }

      setIsOpen(open);
    };

  const onFilterUpdate = (
    updatedFilters: Partial<CollectionFiltersViewModel>
  ): void => {
    updateFilter({
      ...filters,
      ...updatedFilters,
    });
    setIsOpen(false);
  };

  const handleDeckbuilderFilterUpdate = (
    filterName: string,
    additionalFilter = ''
  ) => {
    let newlySelectedLocation = locationNotBeingUsed;
    if (filterName === 'ring') {
      updateFilter({ ...createOneRingFilters(), format: filters.format });
    } else if (filterName === 'ring-bearer') {
      updateFilter({ ...createRingbearerFilters(), format: filters.format });
    } else if (filterName === 'site') {
      newlySelectedLocation = additionalFilter;
      updateFilter({
        ...createLocationFilters(additionalFilter),
        format: filters.format,
      });
      if (currentLocation !== newlySelectedLocation) {
        // TODO: this is really crappy, but whatever, i'm not in the mood for type gymnaastics
        setCurrentLocation(
          newlySelectedLocation as unknown as LotrLocationNames
        );
      }
    }
  };

  const handleAddCardToDeck = (cardId: CardId): void => {
    const card = cardBlueprints.get(cardId);
    if (!card) {
      return;
    }
    if (card && currentLocation !== locationNotBeingUsed) {
      // tODO: This got really dumb really fast
      addCardToDeck({
        ...card,
        siteNumber: currentLocation
      } as CardBlueprint)
    } else {
      addCardToDeck(card);
    }
  };

  const openCardDetails = (cardId: CardId): void => {
    const availableCards = results.map((card) => card.cardBlueprintId);
    iniatlizeModal(cardId, availableCards);
    setCardModalState({ isOpen: true, cardId, availableCards });
  };

  return (
    <>
      <Grid
        display="flex"
        flexDirection="column"
        sx={{ height: '100%', maxHeight: '100vh' }}
      >
        <DeckBuilderMenu
          disableDeckSave
          onDeckSave={() => saveDeck(deck, filters.format)}
        />
        <Grid display="flex" flexDirection="row" sx={{ height: '100%' }}>
          <Grid sx={{ minWidth: '40%;', height: '100%' }}>
            <DeckSections
              deck={deck}
              filterRequest={handleDeckbuilderFilterUpdate}
              selectedFormat={filters.format}
            />
          </Grid>
          <Grid
            sx={{
              width: '100%',
              height: '100%',
            }}
          >
            <CardBank
              onCardPrimaryAction={handleAddCardToDeck}
              onCardSecondaryAction={openCardDetails}
            />
            <FilterToggle showFilters={toggleDrawer(true)} />
          </Grid>
          <Drawer
            sx={{ zIndex: '900' }} // this zIndex hack is because the dropdowns are getting a zindex of 1000, but the drawer was 1200 by default.
            anchor="right"
            open={isOpen}
            onClose={toggleDrawer(false)}
          >
            <FiltersList
              appliedFilters={filters}
              applyFilters={onFilterUpdate}
            />
          </Drawer>
        </Grid>
      </Grid>
      <LotrCardDetails isOpen={cardModalState.isOpen} onClose={closeModal} />
      {hoverImage && <CardDetailsHover cardId={hoverImage} />}
    </>
  );
}
