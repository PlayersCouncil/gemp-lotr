import { Box } from '@mui/joy';
import { CardId } from '../../types/lotr-card/card-blueprint.interface';
import { NonInteractiveLotrCard } from '../LotrCard/NonInteractiveLotrCard';
import { useCardBlueprintStore } from '../../state/card-state';

type CardDetailsHoverProps = {
  cardId: CardId;
};

export function CardDetailsHover(props: CardDetailsHoverProps) {
  const { cardId } = props;

  const card = useCardBlueprintStore().cardBlueprints.get(cardId);
  return card ? (
    <Box
      sx={{
        position: 'absolute',
        left: '25px',
        bottom: '40px',
        height: '400px',
      }}
    >
      <NonInteractiveLotrCard card={card} height={400} />
    </Box>
  ) : (
    <span />
  );
}
