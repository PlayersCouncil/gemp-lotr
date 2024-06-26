import GempCard from '../../../common/components/Card/Card';
import { notFoundImageUrl } from '../../types/image-resolver/types/card-image.constants';
import { CardBlueprint } from '../../types/lotr-card/card-blueprint.interface';

export interface LotrCardProps {
  card: CardBlueprint;
  height?: number;
  width?: number;
  onPrimaryAction: () => void;
  onSecondaryAction: (_blueprint: CardBlueprint) => void;
  allowHover: boolean;
}

export default function LotrCard(props: LotrCardProps) {
  const { card } = props;

  if (!card) {
    return (
      <GempCard
        fallbackImage={notFoundImageUrl}
        title="Unknown Card"
        {...props}
      />
    );
  }

  return <GempCard fallbackImage={notFoundImageUrl} title="" {...props} />;
}
