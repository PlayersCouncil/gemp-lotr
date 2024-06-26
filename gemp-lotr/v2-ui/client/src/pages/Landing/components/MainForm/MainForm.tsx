import { Theme } from '@mui/joy';
import Sheet from '@mui/joy/Sheet';
import Typography from '@mui/joy/Typography';
import useMediaQuery from '@mui/material/useMediaQuery';
import { Outlet } from 'react-router-dom';
import Disclosure from '../Disclosure/Disclosure';
import StarWarsShoutout from '../StarWarsShoutout/StarWarsShoutout';
import styles from './index.module.css';

const computeStyles = (
  isSmallScreen: boolean
): { width: string; height: string } =>
  isSmallScreen
    ? { width: '70%', height: '60%' }
    : { width: '30%', height: '40%' };

function MainForm() {
  const isSmallScreen = useMediaQuery((theme: Theme) =>
    theme.breakpoints.down('md')
  );

  const { width, height } = computeStyles(isSmallScreen);

  return (
    <Sheet
      className={styles.form}
      sx={{
        backgroundColor: 'neutral.300',
        opacity: 0.9,
        p: 2,
        width,
        minHeight: height,
      }}
    >
      <Typography sx={{ mb: 1 }} level="h3">
        Lord of the Rings TCG on GEMP
      </Typography>
      <Outlet />
      <Disclosure />
      <StarWarsShoutout />
    </Sheet>
  );
}

export default MainForm;
