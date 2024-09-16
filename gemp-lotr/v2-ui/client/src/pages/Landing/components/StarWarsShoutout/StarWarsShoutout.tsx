import { Typography } from '@mui/joy';
import styles from './index.module.css';

function StarWarsShoutout() {
  return (
    <div className={styles.starWarsRow}>
      <img
        className={styles.lightsaber}
        src="https://res.starwarsccg.org/gemp/lightForce-42.png"
        alt="green lightsaber"
      />
      <Typography textAlign="center">
        Also check out{' '}
        <a
          href="https://gemp.starwarsccg.org/gemp-swccg/"
          target="_blank"
          rel="noreferrer"
        >
          Star Wars CCG on GEMP
        </a>{' '}
        run by the{' '}
        <a href="https://www.starwarsccg.org/" target="_blank" rel="noreferrer">
          Star Wars Players Committee
        </a>
      </Typography>
      <img
        className={styles.lightsaber}
        src="https://res.starwarsccg.org/gemp/darkForce-42.png"
        alt="red lightsaber"
      />
    </div>
  );
}

export default StarWarsShoutout;
