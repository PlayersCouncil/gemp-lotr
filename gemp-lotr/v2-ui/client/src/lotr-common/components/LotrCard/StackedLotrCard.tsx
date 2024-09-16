import { Box } from '@mui/joy';
import GempCard from '../../../common/components/Card/Card';
import { notFoundImageUrl } from '../../types/image-resolver/types/card-image.constants';
import { LotrCardProps } from './LotrCard';

interface StackedLotrCardProps extends LotrCardProps {
  count: number;
}

// TODO: This should just be something that accepts the component that we wnat to render as stacked, but I'm working with a newborn onmy lap so this is what we get.

export default function StackedLotrCard(props: StackedLotrCardProps) {
  const { card, count, height } = props;

  if (!card) {
    return (
      <GempCard
        fallbackImage={notFoundImageUrl}
        title="Unknown Card"
        {...props}
      />
    );
  }

  const cards = Array.from({ length: count }, () => card);

  return (
    <Box
      sx={{
        display: 'flex',
      }}
    >
      {cards.map((current, ix) => (
        <Box
          key={`${current.cardBlueprintId}-${ix}`}
          sx={{
            marginLeft: ix > 0 ? '-88px' : '0',
          }}
        >
          <GempCard fallbackImage={notFoundImageUrl} title="" {...props} />
        </Box>
      ))}
    </Box>
  );
}
