/**
 * The My Account tab's addresses: `account/overview`, `account/history`, `account/stats`, `account/scenarios`, and
 * `account` alone, which opens the sub-tab the viewer last had open (account.html: localStorage
 * `gemp.account.subTab`), else My Account. The legacy hall's links (hallLinks.js GempLinks: `#account`,
 * `#my-account`, `#account/<sub>`) map onto them.
 */

export type AccountSubTab = 'overview' | 'history' | 'stats' | 'scenarios';

export const ACCOUNT_PATH = 'account';
export const DEFAULT_ACCOUNT_SUB_TAB: AccountSubTab = 'overview';

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

function isSubTab(value: string): value is AccountSubTab {
  return (SUB_TABS as readonly string[]).includes(value);
}

/** The sub-tab a remembered href names (account.html's match of the saved href), else My Account. */
export function rememberedSubTab(saved: string | null): AccountSubTab {
  return SUB_TABS.find((sub) => ACCOUNT_SUB_TAB_HREFS[sub] === saved) ?? DEFAULT_ACCOUNT_SUB_TAB;
}

/**
 * A route's sub-tab: `account/<sub>` that sub-tab, `account` alone the remembered one (`saved`, the stored href);
 * null for a sub-tab the tab does not have.
 */
export function parseAccountRoute(sub: string | null, saved: string | null): AccountSubTab | null {
  if (sub === null || sub === '') {
    return rememberedSubTab(saved);
  }
  return isSubTab(sub) ? sub : null;
}

/** The router's commands for a sub-tab: ['account', 'history']. */
export function accountCommands(sub: AccountSubTab): string[] {
  return [ACCOUNT_PATH, sub];
}

/**
 * A legacy hall fragment of the tab: the sub-tab it names, `'remembered'` for the tab alone (`#account`,
 * `#my-account`: the remembered sub-tab opens), or null when it is not one of the tab's (GempLinks.parse: an unknown
 * sub-tab opens the tab alone).
 */
export function legacyAccountLink(fragment: string): AccountSubTab | 'remembered' | null {
  const parts = /^#(account|my-account)(?:\/([A-Za-z0-9-]+))?\/?$/.exec(fragment);
  if (!parts) {
    return null;
  }
  const sub = parts[2];
  return sub !== undefined && isSubTab(sub) ? sub : 'remembered';
}
