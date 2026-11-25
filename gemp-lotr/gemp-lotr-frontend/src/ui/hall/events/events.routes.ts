import { Routes } from '@angular/router';
import { EVENTS_SERVICES } from '../../../core/hall/events/events-tab.service';
import { LEAGUE_CODE_PARAM, TOURNAMENT_ID_PARAM } from '../../../core/hall/events/events-routes';
import { HALL_TAB_DATA } from '../hall-route-data';

/**
 * The Events tab's routes (/events/...): its frame (EventsTabComponent, with the tab's state: EVENTS_SERVICES) and a
 * route per sub-tab and deep link: calendar, leagues[/<code>], tournaments[/<id>]. The tab opens on its first sub-tab;
 * any other address under it goes there too. The services live as long as the route's injector, so open drawers, the
 * month shown and the event selected stay when the viewer switches tabs and comes back, as the legacy tab (loaded
 * once) kept them.
 */
export const EVENTS_ROUTES: Routes = [
  {
    path: '',
    data: { [HALL_TAB_DATA]: 'events' },
    providers: EVENTS_SERVICES,
    loadComponent: () => import('./events-tab.component').then((m) => m.EventsTabComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'calendar' },
      {
        path: 'calendar',
        loadComponent: () => import('./events-calendar.component').then((m) => m.EventsCalendarComponent),
      },
      {
        path: 'leagues',
        loadComponent: () => import('./events-leagues.component').then((m) => m.EventsLeaguesComponent),
      },
      {
        path: `leagues/:${LEAGUE_CODE_PARAM}`,
        loadComponent: () => import('./events-leagues.component').then((m) => m.EventsLeaguesComponent),
      },
      {
        path: 'tournaments',
        loadComponent: () => import('./events-tournaments.component').then((m) => m.EventsTournamentsComponent),
      },
      {
        path: `tournaments/:${TOURNAMENT_ID_PARAM}`,
        loadComponent: () => import('./events-tournaments.component').then((m) => m.EventsTournamentsComponent),
      },
      { path: '**', redirectTo: 'calendar' },
    ],
  },
];
