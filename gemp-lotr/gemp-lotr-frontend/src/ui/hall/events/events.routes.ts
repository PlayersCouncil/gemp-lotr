import { Routes } from '@angular/router';
import { HALL_TAB_DATA } from '../hall-route-data';

/**
 * The Events tab's routes (/events/...), loaded with the tab. For now its placeholder answers every address under it, so each
 * sub-tab and anything below one (as hallAddressOfHash gives them) already opens the tab. The tab's own part replaces
 * it with its pages.
 */
export const EVENTS_ROUTES: Routes = [
  // The tab opens on its first sub-tab, as the legacy tab did
  { path: '', pathMatch: 'full', redirectTo: 'calendar' },
  {
    path: '**',
    data: { [HALL_TAB_DATA]: 'events' },
    loadComponent: () => import('../hall-placeholder.component').then((m) => m.HallPlaceholderComponent),
  },
];
