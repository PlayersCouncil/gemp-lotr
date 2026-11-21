import { Routes } from '@angular/router';
import { HELP_SERVICES } from '../../../core/hall/help/help-services';
import { HALL_TAB_DATA } from '../hall-route-data';
import { formatsMatcher } from './format-definitions.component';
import { provideHallCards } from './hall-cards';

/**
 * The Help tab's routes (/help/...), loaded with the tab: its frame (the sub-tabs and the card information) and a
 * route per sub-tab. `formats` and `formats/<code>` are one route (formatsMatcher), so the page stays while the address
 * follows the format being read. PC Errata (`errata[/<card id>]`) is another part's: for now its sub-tab shows nothing.
 * Help's state and calls (HELP_SERVICES) and the card code are provided here, with the tab.
 */
export const HELP_ROUTES: Routes = [
  {
    path: '',
    data: { [HALL_TAB_DATA]: 'help' },
    providers: [...HELP_SERVICES, ...provideHallCards()],
    loadComponent: () => import('./help-tab.component').then((m) => m.HelpTabComponent),
    children: [
      // The tab opens on its first sub-tab, as the legacy tab did (a card share link's ?card= goes along)
      { path: '', pathMatch: 'full', redirectTo: 'how-to-play' },
      { path: 'how-to-play', loadComponent: () => import('./how-to-play.component').then((m) => m.HowToPlayComponent) },
      { path: 'conduct', loadComponent: () => import('./conduct.component').then((m) => m.ConductComponent) },
      {
        matcher: formatsMatcher,
        loadComponent: () => import('./format-definitions.component').then((m) => m.FormatDefinitionsComponent),
      },
      { path: 'leagues', loadComponent: () => import('./leagues.component').then((m) => m.LeaguesComponent) },
      // PC Errata: another part's page goes here
      { path: 'errata', children: [{ path: '**', children: [] }] },
      { path: '**', redirectTo: 'how-to-play' },
    ],
  },
];
