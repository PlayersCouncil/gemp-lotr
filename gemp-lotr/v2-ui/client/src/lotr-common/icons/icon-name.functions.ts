import dwarven from '@gemp-assets/icons/cultures/dwarven.svg';
import dunland from '@gemp-assets/icons/cultures/dunland.svg';
import gandalf from '@gemp-assets/icons/cultures/gandalf.svg';
import gollum from '@gemp-assets/icons/cultures/gollum.svg';
import gondor from '@gemp-assets/icons/cultures/gondor.svg';
import men from '@gemp-assets/icons/cultures/men.svg';
import moria from '@gemp-assets/icons/cultures/moria.svg';
import orc from '@gemp-assets/icons/cultures/orc.svg';
import sauron from '@gemp-assets/icons/cultures/sauron.svg';
import shire from '@gemp-assets/icons/cultures/shire.svg';
import ringwraith from '@gemp-assets/icons/cultures/ringwraith.svg';
import wraith from '@gemp-assets/icons/cultures/wraith.svg';
import isengard from '@gemp-assets/icons/cultures/isengard.svg';
import elven from '@gemp-assets/icons/cultures/elven.svg';
import raider from '@gemp-assets/icons/cultures/raider.svg';
import urukhai from '@gemp-assets/icons/cultures/urukhai.svg';
import { IconName } from './icons.type';

// todo: ideally this would be auto generated when we add icons :shrug: --nickh
// aaaaand there should be a global version of it i guess --nickh
export const getIconByName = (name: IconName): string => {
  switch (name) {
    case 'dwarven-culture':
      return dwarven;
    case 'dunland-culture':
      return dunland;
    case 'elven-culture':
      return elven;
    case 'gandalf-culture':
      return gandalf;
    case 'gollum-culture':
      return gollum;
    case 'gondor-culture':
      return gondor;
    case 'isengard-culture':
      return isengard;
    case 'men-culture':
      return men;
    case 'moria-culture':
      return moria;
    case 'orc-culture':
      return orc;
    case 'raider-culture':
      return raider;
    case 'ringwraith-culture':
      return ringwraith;
    case 'sauron-culture':
      return sauron;
    case 'shire-culture':
      return shire;
    case 'uruk-hai-culture':
      return urukhai;
    case 'wraith-culture':
      return wraith;

    default:
      return raider;
  }
};
