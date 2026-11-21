import {
  ACCOUNT_SUB_TAB_HREFS,
  accountCommands,
  isAccountAddress,
  parseAccountRoute,
  rememberedSubTab,
  shouldRestoreSubTab,
} from './account-routes';

describe('account routes', () => {
  it("names each sub-tab's address, My Account's being the tab's own", () => {
    expect(parseAccountRoute('')).toBe('overview');
    expect(parseAccountRoute('history')).toBe('history');
    expect(parseAccountRoute('overview')).toBeNull();
    expect(accountCommands('overview')).toEqual(['/account']);
    expect(accountCommands('stats')).toEqual(['/account', 'stats']);
  });

  it('remembers the sub-tab by its legacy href', () => {
    expect(rememberedSubTab(null)).toBe('overview');
    expect(rememberedSubTab(ACCOUNT_SUB_TAB_HREFS.scenarios)).toBe('scenarios');
    expect(rememberedSubTab('includes/account/yourStats.html')).toBe('stats');
    expect(rememberedSubTab('something else')).toBe('overview');
  });

  it('opens the remembered sub-tab only when the tab is entered from elsewhere', () => {
    expect(isAccountAddress('/account?x=1')).toBe(true);
    expect(isAccountAddress('/account/history')).toBe(true);
    expect(isAccountAddress('/accounts')).toBe(false);
    // the tab bar's My Account from another tab
    expect(shouldRestoreSubTab('/account', '/hall')).toBe(true);
    // the hall's own address, redirected to the tab last opened
    expect(shouldRestoreSubTab('/', null)).toBe(true);
    // a link to it, or a reload, and the sub-tab bar's My Account
    expect(shouldRestoreSubTab('/account', null)).toBe(false);
    expect(shouldRestoreSubTab('/account', '/account/history')).toBe(false);
  });
});
