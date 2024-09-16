import { Box, Typography } from '@mui/joy';
import styles from './index.module.css';

type NotFoundCardProps = {
  fallbackImage: string;
};

export function NotFoundCard(props: NotFoundCardProps) {
  const { fallbackImage } = props;
  return (
    <Box
      sx={{
        height: '100%',
        width: '100%',
        backgroundColor: 'red',
      }}
    >
      <img
        className={styles.cardFace}
        // onError={(imageHref = fallbackImage)}
        src={fallbackImage}
        alt="unknown card"
      />
    </Box>
  );
}
