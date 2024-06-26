import TheOneRing from '../../common/svg/TheOneRing/TheOneRing';
import MainForm from './components/MainForm/MainForm';
import styles from './index.module.css';

function Landing() {
  return (
    <div className={styles.landingPage}>
      <TheOneRing classes={styles.oneRing} />
      <MainForm />
    </div>
  );
}

export default Landing;
