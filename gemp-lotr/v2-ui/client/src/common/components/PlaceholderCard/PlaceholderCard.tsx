import { Box } from '@mui/joy';
import LotrCard from '../../../lotr-common/components/LotrCard/LotrCard';
import { determineCardDimensions } from '../Card/types/card-dimension.functions';
import { CardBlueprint } from '../../../lotr-common/types/lotr-card/card-blueprint.interface';

type CardPlaceholderProps = {
  card?: CardBlueprint;
  placeholder: string;
  vertical?: boolean;
  height?: number;
  onSectionSelect: VoidFunction;
  onCardSecondaryAction?: VoidFunction;
};

/**
 * A Component that will display either an inputted Blueprint ID or a placeholder with a Title.
 * This is intended for use in places where the card displayed here may change (such as the Ring-Bearer in a deckbuilder experience)
 * @param {@see CardPlaceholderProps} Props
 * @returns
 */
export default function PlaceholderCard({
  placeholder,
  vertical = true,
  height = 220,
  onSectionSelect: onSelect,
  card,
  onCardSecondaryAction: onSecondaryAction = () => {},
}: CardPlaceholderProps) {
  let dimensions = determineCardDimensions(!vertical, height, undefined);
  if (vertical) {
    dimensions = {
      width: Math.min(dimensions.height, dimensions.width),
      height: Math.max(dimensions.height, dimensions.width),
    };
  } else {
    dimensions = {
      width: Math.max(dimensions.height, dimensions.width),
      height: Math.min(dimensions.height, dimensions.width),
    };
  }
  if (!card) {
    return (
      <Box
        onClick={onSelect}
        sx={{
          width: `${dimensions.width}px`,
          height: `${dimensions.height}px`,
          backgroundColor: 'neutral.light',
          border: 1,
          borderColor: 'neutral.main',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          cursor: 'pointer',
        }}
      >
        <span>{placeholder}</span>
      </Box>
    );
  }
  return (
    <LotrCard
      card={card}
      width={dimensions.width}
      height={dimensions.height}
      allowHover
      onPrimaryAction={onSelect}
      onSecondaryAction={onSecondaryAction}
    />
  );
}
