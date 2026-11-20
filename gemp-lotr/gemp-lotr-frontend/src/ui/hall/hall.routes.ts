import { inject } from '@angular/core';
import { Routes } from '@angular/router';
import { HALL_TAB_STORAGE_KEY, rememberedHallTab } from '../../core/hall/hall-tabs';
import { LocalSettings } from '../../core/platform/local-settings.service';

/**
 * The hall's tabs, under its frame (HallShellComponent, at the app's root: /gemp-lotr/ng/<tab>/...). Each tab's routes
 * are its own file, loaded with the tab (game-hall/, help/, events/, info/, account/): a part of the hall changes only
 * its tab's folder. The hall's address alone opens the tab last opened (as the legacy hall), else the Game Hall;
 * any other address goes to the Game Hall. Admin is the legacy hall's (a link) and Users isn't drawn.
 */
export const HALL_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: () => `/${rememberedHallTab(inject(LocalSettings).read(HALL_TAB_STORAGE_KEY)).path ?? 'hall'}`,
  },
  {
    path: 'hall',
    loadChildren: () => import('./game-hall/game-hall.routes').then((m) => m.GAME_HALL_ROUTES),
  },
  {
    path: 'help',
    loadChildren: () => import('./help/help.routes').then((m) => m.HELP_ROUTES),
  },
  {
    path: 'events',
    loadChildren: () => import('./events/events.routes').then((m) => m.EVENTS_ROUTES),
  },
  {
    path: 'info',
    loadChildren: () => import('./info/info.routes').then((m) => m.INFO_ROUTES),
  },
  {
    path: 'account',
    loadChildren: () => import('./account/account.routes').then((m) => m.ACCOUNT_ROUTES),
  },
  { path: '**', redirectTo: 'hall' },
];
