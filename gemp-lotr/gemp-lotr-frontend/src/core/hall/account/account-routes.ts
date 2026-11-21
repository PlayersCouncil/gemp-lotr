/**
 * The My Account tab's addresses: `account` (My Account itself), `account/history`, `account/stats`,
 * `account/scenarios`. The legacy hall's links (`#account`, `#my-account`, `#account/<sub>`) are mapped onto them by
 * the hall's `hall-hash-routes.ts`.
 *
 * The legacy tab opened on the sub-tab last shown (account.html: localStorage `gemp.account.subTab`). Here `account`
 * is My Account's own address, so the remembered sub-tab is opened instead only when the tab is entered from
 * elsewhere: from another tab of the hall, or from the hall's own address (`shouldRestoreSubTab`). A link to
 * `account`, or a reload of it, shows My Account.
 */

export type AccountSubTab = 'overview' | 'history' | 'stats' | 'scenarios';

export const ACCOUNT_PATH = 'account';
export const DEFAULT_ACCOUNT_SUB_TAB: AccountSubTab = 'overview';

/** Each sub-tab's route under `account` ('' for My Account), as HALL_TABS has them. */
export const ACCOUNT_SUB_TAB_PATHS: Readonly<Record<AccountSubTab, string>> = {
  overview: '',
  history: 'history',
  stats: 'stats',
  scenarios: 'scenarios',
};

/**
 * localStorage key of the sub-tab last opened (account.html SUB_TAB_KEY). Its value is the legacy sub-tab link's
 * href, so both halls read each other's: `ACCOUNT_SUB_TAB_HREFS`.
 */
export const ACCOUNT_SUB_TAB_STORAGE_KEY = 'gemp.account.subTab';

export const ACCOUNT_SUB_TAB_HREFS: Readonly<Record<AccountSubTab, string>> = {
  overview: '#account',
  history: 'includes/account/gameHistory.html',
  stats: 'includes/account/yourStats.html',
  scenarios: 'includes/account/myScenarios.html',
};

const SUB_TABS: readonly AccountSubTab[] = ['overview', 'history', 'stats', 'scenarios'];

/** The sub-tab a remembered href names (account.html's match of the saved href), else My Account. */
export function rememberedSubTab(saved: string | null): AccountSubTab {
  return SUB_TABS.find((sub) => ACCOUNT_SUB_TAB_HREFS[sub] === saved) ?? DEFAULT_ACCOUNT_SUB_TAB;
}

/** The sub-tab of a route path under `account` ('' My Account); null for one the tab does not have. */
export function parseAccountRoute(path: string): AccountSubTab | null {
  return SUB_TABS.find((sub) => ACCOUNT_SUB_TAB_PATHS[sub] === path) ?? null;
}

/** The router's commands for a sub-tab: ['/account'] or ['/account', 'history']. */
export function accountCommands(sub: AccountSubTab): string[] {
  const path = ACCOUNT_SUB_TAB_PATHS[sub];
  return path === '' ? [`/${ACCOUNT_PATH}`] : [`/${ACCOUNT_PATH}`, path];
}

/** Whether an address (a path, with or without its query) is the tab's or under it. */
export function isAccountAddress(url: string): boolean {
  const path = url.split(/[?#]/)[0] ?? '';
  return path === `/${ACCOUNT_PATH}` || path.startsWith(`/${ACCOUNT_PATH}/`);
}

/**
 * A navigation to `account` itself opens the remembered sub-tab when it entered the tab from elsewhere: it was asked
 * for an address outside the tab (the hall's own address, redirected here), or it came from an address outside the
 * tab (the tab bar's My Account from another tab). `initialUrl`: what the navigation was asked for; `previousUrl`:
 * the address shown before it, null for the page's first.
 */
export function shouldRestoreSubTab(initialUrl: string, previousUrl: string | null): boolean {
  return !isAccountAddress(initialUrl) || (previousUrl !== null && !isAccountAddress(previousUrl));
}
