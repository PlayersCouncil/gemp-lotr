import { Button, Modal, ModalClose, Sheet, Typography } from '@mui/joy';
import { NonInteractiveLotrCard } from '../LotrCard/NonInteractiveLotrCard';
import { useWindowDimensions } from '../../../common/hooks/useWindowDimensions';
import { useCardDetailStore } from './card-details.state';
import { useCardBlueprintStore } from '../../state/card-state';

interface LotrCardDetailsProps {
  isOpen: boolean;
  onClose: VoidFunction;
}

const determineModalWidth = (screenWidth: number): number => {
  if (screenWidth < 900) {
    return screenWidth * 0.75;
  }
  if (screenWidth < 1300) {
    return screenWidth * 0.5;
  }
  return screenWidth * 0.5;
};

function LotrCardDetails({ isOpen, onClose }: LotrCardDetailsProps) {
  const screenWidth = useWindowDimensions();
  const { displayedCardId, updateCurrentCard } = useCardDetailStore();

  const card = useCardBlueprintStore().cardBlueprints.get(displayedCardId);

  return (
    <Modal
      open={isOpen}
      onClose={onClose}
      sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}
    >
      <Sheet
        variant="outlined"
        sx={{
          borderRadius: 'md',
          p: 3,
          boxShadow: 'lg',
        }}
      >
        <ModalClose
          variant="outlined"
          sx={{
            top: 'calc(-1/4 * var(--IconButton-size))',
            right: 'calc(-1/4 * var(--IconButton-size))',
            boxShadow: '0 2px 12px 0 rgba(0 0 0 / 0.2)',
            borderRadius: '50%',
            bgcolor: 'background.body',
          }}
        />
        <Typography
          component="h2"
          id="modal-title"
          level="h4"
          textColor="inherit"
          fontWeight="lg"
          mb={1}
        >
          Card Information
        </Typography>
        {card && (
          <NonInteractiveLotrCard
            card={card}
            width={determineModalWidth(screenWidth)}
          />
        )}
        <Button onClick={() => updateCurrentCard('next')}>Next</Button>
        <Button onClick={() => updateCurrentCard('previous')}>Previous</Button>
      </Sheet>
    </Modal>
  );
}

export default LotrCardDetails;
