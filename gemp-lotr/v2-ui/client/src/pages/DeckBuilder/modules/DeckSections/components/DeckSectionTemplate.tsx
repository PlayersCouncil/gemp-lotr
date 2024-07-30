import { Grid } from '@mui/joy';
import PlaceholderCard from '../../../../../common/components/PlaceholderCard/PlaceholderCard';
import { FilterableDeckSection } from '../types/filterable-deck-section.interface';
import { CardBlueprint, CardId } from '../../../../../lotr-common/types/lotr-card/card-blueprint.interface';

interface DeckSectionProps {
  selections: FilterableDeckSection[];
  onSectionChange: (section: string) => void;
  onSecondaryAction: (blueprintId: CardBlueprint) => void;
  placeholderWidth: 'small' | 'large';
}

const getDefaultDeckSectionProps = (): DeckSectionProps => ({
  selections: [],
  onSectionChange: () => {},
  onSecondaryAction: (_blueprint: CardBlueprint) => {},
  placeholderWidth: 'small',
});

export default function DeckSectionTemplate(
  props: DeckSectionProps = getDefaultDeckSectionProps()
) {
  const { onSectionChange, selections, placeholderWidth, onSecondaryAction } = props;

  return (
    <Grid gap="1rem" container>
      {(selections ?? []).map((section) => (
        <Grid
          md={placeholderWidth === 'small' ? 3 : 5}
          key={section.placeholder}
        >
          <PlaceholderCard
            placeholder={section.placeholder}
            card={section.cardBlueprint}
            vertical={section.isVertical}
            height={section.isVertical ? 240 : 180}
            onSectionSelect={() => onSectionChange(section.filterName)}
            onCardSecondaryAction={() => onSecondaryAction(section.cardBlueprint!)}
          />
        </Grid>
      ))}
    </Grid>
  );
}
