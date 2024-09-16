import { Override } from '../../../types/override.interface';
import { aprilErrata } from './april-errata.list';
import { decemberErrata } from './december-errata.list';
import { holidayPromos } from './holiday-promos.list';
import { leaguePromos } from './league-promos.list';
import { mayErrata } from './may-errata.list';
import { vpack1 } from './vpack1.list';
import { vsetOneShadowOfThePast } from './vset-1-a-shadow-of-the-past.list';

export const updatesFrom2021: Override[] = [
  ...aprilErrata,
  ...vpack1,
  ...mayErrata,
  ...leaguePromos,
  ...vsetOneShadowOfThePast,
  ...decemberErrata,
  ...holidayPromos,
];
