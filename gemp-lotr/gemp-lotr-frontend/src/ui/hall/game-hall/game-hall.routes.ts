import { Routes } from '@angular/router';
import { HALL_TAB_DATA } from '../hall-route-data';

/**
 * The Game Hall tab's routes (/hall). Its page: the link buttons and the message of the day, then the sections of
 * tables, queues and tournaments.
 */
export const GAME_HALL_ROUTES: Routes = [
  {
    path: '**',
    data: { [HALL_TAB_DATA]: 'game-hall' },
    loadComponent: () => import('./game-hall-tab.component').then((m) => m.GameHallTabComponent),
  },
];
