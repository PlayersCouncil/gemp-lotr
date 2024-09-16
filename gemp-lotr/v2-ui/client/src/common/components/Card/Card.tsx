/* eslint-disable jsx-a11y/no-noninteractive-element-interactions -- not sure how best to handle this since the image is kinda a button, but not really? :shrug: */
import { Box } from '@mui/joy';
import { useCallback } from 'react';
import styles from './index.module.css';
import { determineCardDimensions } from './types/card-dimension.functions';
import { useCardDetailStore } from '../../../lotr-common/components/LotrCardDetails/card-details.state';
import {
  CardBlueprint,
  CardId,
} from '../../../lotr-common/types/lotr-card/card-blueprint.interface';
import { NotFoundCard } from './NotFoundCard';

interface CardProps {
  card: CardBlueprint;
  title?: string;
  width?: number;
  height?: number;
  fallbackImage: string;
  onPrimaryAction: () => void;
  onSecondaryAction: (_blueprint: CardBlueprint) => void;
  allowHover: boolean;
}

/**
 * This is the (essentially) abstract card that displays an image from a URL.
 * LotrCard for the implementation actually leverages this and does clean separation on top of it
 * @param param
 */
export default function GempCard(props: CardProps) {
  const {
    card,
    title,
    fallbackImage,
    onPrimaryAction,
    onSecondaryAction,
    allowHover,
    height,
    width,
  } = props;
  const { setHoverImage } = useCardDetailStore();

  const doSetHoverImage = useCallback(
    (hoverCardId: CardId | null) => {
      if (!allowHover) {
        return;
      }
      setHoverImage(hoverCardId);
    },
    [allowHover, setHoverImage]
  );

  if (!card) {
    return <NotFoundCard fallbackImage={fallbackImage} />;
  }
  const dimensions = determineCardDimensions(card.isHorizontal, height, width);

  const handleKeyClick = (event: React.KeyboardEvent): void => {
    if (event.key === 'Enter') {
      onPrimaryAction();
    }
  };
  const handleCardClick = (event: React.MouseEvent): void => {
    if (event.type === 'click') {
      onPrimaryAction();
    } else if (event.type === 'contextmenu') {
      onSecondaryAction(card);
      event.preventDefault();
    }
  };

  return (
    <Box
      sx={{
        height: dimensions.height,
        width: dimensions.width,
      }}
    >
      <object
        role="img"
        className={styles.cardFace}
        onMouseOver={() => doSetHoverImage(card.cardBlueprintId)}
        onFocus={() => doSetHoverImage(card.cardBlueprintId)}
        onMouseLeave={() => doSetHoverImage(null)}
        onClick={handleCardClick}
        onKeyUp={handleKeyClick}
        data={card.imageUrl}
        onContextMenu={handleCardClick} // note: this prevents all browser right click actions
      >
        <img
          data-blueprint-id={card.cardBlueprintId}
          onContextMenu={handleCardClick} // note: this prevents all browser right click actions
          className={styles.cardFace}
          // onError={(imageHref = fallbackImage)}
          src={fallbackImage}
          alt={title ?? 'gemp card image'}
        />
      </object>
    </Box>
  );
}
