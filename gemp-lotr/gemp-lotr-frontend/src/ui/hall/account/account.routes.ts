import { inject } from '@angular/core';
import { CanActivateFn, Router, Routes } from '@angular/router';
import { accountCommands, shouldRestoreSubTab } from '../../../core/hall/account/account-routes';
import { ACCOUNT_SERVICES, AccountTab } from '../../../core/hall/account/account-tab.service';
import { HALL_TAB_DATA } from '../hall-route-data';
import { ACCOUNT_SUB_TAB_DATA } from './account-tab.component';

/**
 * My Account itself, entered from elsewhere (the tab bar from another tab, or the hall's own address), opens the
 * sub-tab last shown instead, as the legacy tab did (account-routes.ts shouldRestoreSubTab).
 */
export const restoreAccountSubTab: CanActivateFn = () => {
  const router = inject(Router);
  const navigation = router.currentNavigation();
  if (!navigation) {
    return true;
  }
  const previous = navigation.previousNavigation?.finalUrl;
  const restore = shouldRestoreSubTab(
    router.serializeUrl(navigation.initialUrl),
    previous ? router.serializeUrl(previous) : null,
  );
  const remembered = inject(AccountTab).remembered();
  return restore && remembered !== 'overview' ? router.createUrlTree(accountCommands(remembered)) : true;
};

/**
 * The My Account tab's routes (/account/...), loaded with the tab, with its state (ACCOUNT_SERVICES, under the hall's):
 * `account` My Account, `account/history`, `account/stats`, `account/scenarios`; anything else under it goes to
 * `account`.
 */
export const ACCOUNT_ROUTES: Routes = [
  {
    path: '',
    data: { [HALL_TAB_DATA]: 'account' },
    providers: ACCOUNT_SERVICES,
    loadComponent: () => import('./account-tab.component').then((m) => m.AccountTabComponent),
    children: [
      {
        path: '',
        pathMatch: 'full',
        canActivate: [restoreAccountSubTab],
        data: { [ACCOUNT_SUB_TAB_DATA]: 'overview' },
        loadComponent: () => import('./account-overview.component').then((m) => m.AccountOverviewComponent),
      },
      {
        path: 'history',
        data: { [ACCOUNT_SUB_TAB_DATA]: 'history' },
        loadComponent: () => import('./game-history.component').then((m) => m.GameHistoryComponent),
      },
      {
        path: 'stats',
        data: { [ACCOUNT_SUB_TAB_DATA]: 'stats' },
        loadComponent: () => import('./player-stats.component').then((m) => m.PlayerStatsComponent),
      },
      {
        path: 'scenarios',
        data: { [ACCOUNT_SUB_TAB_DATA]: 'scenarios' },
        loadComponent: () => import('./my-scenarios.component').then((m) => m.MyScenariosComponent),
      },
      { path: '**', redirectTo: '' },
    ],
  },
];
