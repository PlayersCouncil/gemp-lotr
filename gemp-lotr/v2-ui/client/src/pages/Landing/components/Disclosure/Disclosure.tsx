import { Typography } from '@mui/joy';
import styles from './index.module.css';

function Disclosure() {
  return (
    <Typography className={styles.disclosure} sx={{ mt: 2 }}>
      <a
        href="https://wiki.lotrtcgpc.net/wiki/The_Lord_of_the_Rings_TCG"
        target="_blank"
        rel="noreferrer"
      >
        The Lord of the Rings TCG
      </a>{' '}
      was published by Deciper, Inc from 2001-2007. GEMP is a platform for
      playing this excellent game for free in your browser. Maintained by the
      unofficial{' '}
      <a href="https://lotrtcgpc.net/" target="_blank" rel="noreferrer">
        Player's Council
      </a>
    </Typography>
  );
}

export default Disclosure;
