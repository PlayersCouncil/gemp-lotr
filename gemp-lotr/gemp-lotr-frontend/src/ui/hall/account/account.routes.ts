import { Routes } from '@angular/router';
import { HALL_TAB_DATA } from '../hall-route-data';

/**
 * The My Account tab's routes (/account/...), loaded with the tab. For now its placeholder answers every address under it, so each
 * sub-tab and anything below one (as hallAddressOfHash gives them) already opens the tab. The tab's own part replaces
 * it with its pages.
 */
export const ACCOUNT_ROUTES: Routes = [
  {
    path: '**',
    data: { [HALL_TAB_DATA]: 'account' },
    loadComponent: () => import('../hall-placeholder.component').then((m) => m.HallPlaceholderComponent),
  },
];
