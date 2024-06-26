import { FilterableDeckSection } from '../types/filterable-deck-section.interface';
import DeckSectionTemplate from './DeckSectionTemplate';
import { CardBlueprint } from '../../../../../lotr-common/types/lotr-card/card-blueprint.interface';

const getDefaultDeckSectionProps = (
  ring: CardBlueprint | undefined,
  ringBearer: CardBlueprint | undefined
): FilterableDeckSection[] => [
  {
    placeholder: 'Ring-Bearer',
    filterName: 'ring-bearer',
    cardBlueprint: ringBearer,
    isVertical: true,
  },
  {
    placeholder: 'The One Ring',
    filterName: 'ring',
    cardBlueprint: ring,
    isVertical: true,
  },
];

interface RingBearerDeckSectionProps {
  ring?: CardBlueprint;
  ringBearer?: CardBlueprint;
  updateFilter: (filterName: string) => void;
}

export default function RingBearerDeckSection(
  props: RingBearerDeckSectionProps
) {
  const { ring, ringBearer, updateFilter } = props;

  return (
    <DeckSectionTemplate
      selections={getDefaultDeckSectionProps(ring, ringBearer)}
      onSectionChange={updateFilter}
      placeholderWidth="large"
    />
  );
}
