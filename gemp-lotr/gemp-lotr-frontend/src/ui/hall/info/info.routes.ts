import { Routes } from '@angular/router';
import { INFO_SERVICES } from '../../../core/hall/info/info-services';
import { HALL_TAB_DATA } from '../hall-route-data';
import { provideHallCards } from '../help/hall-cards';
import { patchNotesMatcher } from './patch-notes.component';

/**
 * The Server Info tab's routes (/info/...), loaded with the tab: its frame (the sub-tabs and the card information) and
 * a route per sub-tab. `patch-notes` and `patch-notes/<slug>` are one route (patchNotesMatcher), so the page stays while
 * the address follows the updates shown. Server Info's state and calls (INFO_SERVICES) and the card code (a patch
 * note's card links) are provided here, with the tab.
 */
export const INFO_ROUTES: Routes = [
  {
    path: '',
    data: { [HALL_TAB_DATA]: 'info' },
    providers: [...INFO_SERVICES, ...provideHallCards()],
    loadComponent: () => import('./info-tab.component').then((m) => m.InfoTabComponent),
    children: [
      // The tab opens on its first sub-tab, as the legacy tab did
      { path: '', pathMatch: 'full', redirectTo: 'stats' },
      { path: 'stats', loadComponent: () => import('./server-stats.component').then((m) => m.ServerStatsComponent) },
      {
        matcher: patchNotesMatcher,
        loadComponent: () => import('./patch-notes.component').then((m) => m.PatchNotesComponent),
      },
      { path: 'contribute', loadComponent: () => import('./contribute.component').then((m) => m.ContributeComponent) },
      { path: 'about', loadComponent: () => import('./about.component').then((m) => m.AboutComponent) },
      { path: '**', redirectTo: 'stats' },
    ],
  },
];
