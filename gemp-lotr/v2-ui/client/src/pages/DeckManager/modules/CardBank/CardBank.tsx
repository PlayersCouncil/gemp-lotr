import { Box, Grid, Typography } from '@mui/joy';
import SearchResults from './SearchResults';
import PageSelector from '../../components/PageSelector/PageSelector';
import { useCardQueryStore } from '../../../../lotr-common/state/card-filter.state';
import { CardId } from '../../../../lotr-common/types/lotr-card/card-blueprint.interface';

type CardBankProps = {
  // yes these prop drills make me want to use a context, but I'm jamming
  onCardPrimaryAction: (cardId: CardId) => void;
  onCardSecondaryAction: (cardId: CardId) => void;
};

function CardBank(props: CardBankProps) {
  const { onCardPrimaryAction, onCardSecondaryAction } = props;
  const { results, setCurrentPage } = useCardQueryStore();
  return (
    <Grid
      container
      display="flex"
      flexDirection="column"
      sx={{ height: '100%', maxHeight: '100%' }}
    >
      <Grid>
        <Typography level="h3">Available Cards</Typography>
      </Grid>
      <Box sx={{ flex: '1 1 1px', overflowY: 'auto', overflowX: 'hidden' }}>
        <SearchResults
          cards={results}
          onCardPrimaryAction={onCardPrimaryAction}
          onCardSecondaryAction={onCardSecondaryAction}
        />
      </Box>
      <Grid justifySelf="flex-end" mb={3}>
        <PageSelector pageUpdated={setCurrentPage} />
      </Grid>
    </Grid>
  );
}

export default CardBank;
