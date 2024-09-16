import { useEffect, useState } from 'react';
import { LotrLocations } from '../../../../../../common/types/LotrLocations/lotr-locations.type';
import DeckSectionTemplate from '../DeckSectionTemplate';
import { FilterableDeckSection } from '../../types/filterable-deck-section.interface';
import { formatSiteSelections } from './format-site-selections.functions';
import { getSelectedSiteSectionMappings } from './selected-site-sections';
import { useDeckBuilderStore } from '../../../../state/deckbuilder-state';

type LocationDeckSectionProps = {
  selectedSites: LotrLocations;
  updateFilteredSites: (siteName: string) => void;
};

export default function LocationDeckSection(props: LocationDeckSectionProps) {
  const { updateFilteredSites, selectedSites } = props;
  const [formattedSelections, setFormattedSelections] = useState<
    FilterableDeckSection[]
  >([]);

  const { removeCardFromDeck } = useDeckBuilderStore();

  useEffect(() => {
    setFormattedSelections(formatSiteSelections(selectedSites));
  }, [selectedSites, setFormattedSelections]);

  const doSelectionChanged = (section: string) => {
    const match = getSelectedSiteSectionMappings().find(
      (l) => l.filterName === section
    );
    if (!match) {
      return;
    }
    updateFilteredSites(match.filterName.toString());
  };

  return (
    <DeckSectionTemplate
      selections={formattedSelections}
      onSectionChange={doSelectionChanged}
      onSecondaryAction={cardBlueprint => removeCardFromDeck(cardBlueprint)}
      placeholderWidth="small"
    />
  );
}
