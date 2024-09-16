import { Grid } from '@mui/joy';
import { useEffect, useRef, useState } from 'react';
import { CollectionCardViewModel } from '../../../../lotr-common/api/collection-api/collection-api-response.functions';
import LotrCard from '../../../../lotr-common/components/LotrCard/LotrCard';
import { useWindowDimensions } from '../../../../common/hooks/useWindowDimensions';
import { useCardQueryStore } from '../../../../lotr-common/state/card-filter.state';
import { CardId } from '../../../../lotr-common/types/lotr-card/card-blueprint.interface';

interface SearchResultProps {
  cards: CollectionCardViewModel[];

  onCardPrimaryAction: (blueprintId: CardId) => void;
  onCardSecondaryAction: (blueprintId: CardId) => void;
}

const getCardWidth = (viewportWidth: number): number => {
  const minWidth = 75; // YEA these are rough and suck...
  const maxWidth = 500;
  const desiredWidth = viewportWidth / 8;

  return Math.max(minWidth, Math.min(maxWidth, desiredWidth));
};

function SearchResults({
  cards,
  onCardPrimaryAction,
  onCardSecondaryAction,
}: SearchResultProps) {
  const viewportWidth = useWindowDimensions();
  const [containerWidth, setContainerWidth] = useState<number>(0);
  const containerRef = useRef<HTMLDivElement | null>(null);
  useEffect(() => {
    if (containerRef.current) {
      const currentWidth = containerRef.current.offsetWidth;
      if (currentWidth !== containerWidth) {
        setContainerWidth(currentWidth);
      }
    }
  }, [viewportWidth, containerWidth]);

  return (
    <Grid ref={containerRef} container spacing={1}>
      {cards.length > 0 ? (
        cards.map((card) => {
          return (
            <Grid key={card.cardBlueprintId}>
              <LotrCard
                card={card}
                width={getCardWidth(viewportWidth)}
                onPrimaryAction={() => {
                  onCardPrimaryAction(card.cardBlueprintId);
                }}
                onSecondaryAction={() =>
                  onCardSecondaryAction(card.cardBlueprintId)
                }
                allowHover
              />
            </Grid>
          );
        })
      ) : (
        <span>No cards found</span>
      )}
    </Grid>
  );
}

export default SearchResults;
