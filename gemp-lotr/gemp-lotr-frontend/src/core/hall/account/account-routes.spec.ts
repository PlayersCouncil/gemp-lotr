import {
  ACCOUNT_SUB_TAB_HREFS,
  accountCommands,
  legacyAccountLink,
  parseAccountRoute,
  rememberedSubTab,
} from './account-routes';

describe('account routes', () => {
  it('opens a sub-tab by its slug, and the remembered one for the tab alone', () => {
    expect(parseAccountRoute('history', null)).toBe('history');
    expect(parseAccountRoute('nope', null)).toBeNull();
    expect(parseAccountRoute(null, null)).toBe('overview');
    expect(parseAccountRoute(null, ACCOUNT_SUB_TAB_HREFS.scenarios)).toBe('scenarios');
    expect(parseAccountRoute('', 'includes/account/yourStats.html')).toBe('stats');
    expect(rememberedSubTab('something else')).toBe('overview');
    expect(accountCommands('stats')).toEqual(['account', 'stats']);
  });

  it("maps the legacy hall's links", () => {
    expect(legacyAccountLink('#account')).toBe('remembered');
    expect(legacyAccountLink('#my-account')).toBe('remembered');
    expect(legacyAccountLink('#account/scenarios')).toBe('scenarios');
    expect(legacyAccountLink('#account/overview/')).toBe('overview');
    expect(legacyAccountLink('#account/unknown')).toBe('remembered');
    expect(legacyAccountLink('#events')).toBeNull();
  });
});
