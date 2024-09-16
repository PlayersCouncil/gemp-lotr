import { christmasErrata } from './2022-christmas-errata.list';
import { adhocUpdates } from './ad-hoc-updates.list';
import { nov22Gala } from './nov-sealed-gala.list';
import { weekendPromos } from './weekend-promos.list';

export const updatesFrom2022 = [
  ...weekendPromos,
  ...nov22Gala,
  ...christmasErrata,
  ...adhocUpdates,
];
