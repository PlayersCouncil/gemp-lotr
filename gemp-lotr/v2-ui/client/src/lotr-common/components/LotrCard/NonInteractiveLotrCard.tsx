import { CardBlueprint } from '../../types/lotr-card/card-blueprint.interface';
import LotrCard from './LotrCard';

type NonInteractiveLotrCardProps = {
  card: CardBlueprint;
  height?: number;
  width?: number;
};

export function NonInteractiveLotrCard(props: NonInteractiveLotrCardProps) {
  return (
    <LotrCard
      onPrimaryAction={() => {}}
      onSecondaryAction={() => {}}
      allowHover={false}
      {...props}
    />
  );
}
