import { Box, IconButton } from '@mui/joy';
import { getIconByName } from '../../../lotr-common/icons/icon-name.functions';
import { IconName } from '../../../lotr-common/icons/icons.type';
import * as styles from './index.module.css';

interface GempIconProps {
  name: string;
  alt?: string;
  selected?: boolean;
  iconClicked?: VoidFunction;
}

function GempIcon({ iconClicked, name, alt, selected }: GempIconProps) {
  const onKeyUp = (e: React.KeyboardEvent): void => {
    if (!iconClicked) {
      return;
    }
    e.preventDefault();
    if (e.code === 'Space') {
      iconClicked();
    }
  };
  return (
    <IconButton
      variant={selected ? 'solid' : 'outlined'}
      onClick={iconClicked}
      onKeyUp={onKeyUp}
      sx={{ maxHeight: '20px', maxWidth: '20px' }}
    >
      <img
        className={styles.gempIcon}
        style={{ maxWidth: '20px', maxHeight: '20px' }}
        src={getIconByName(name as IconName)}
        alt={alt ?? 'unlabeled icon'}
      />
    </IconButton>
    // </Box>
  );
}

export default GempIcon;
