import { TestBed } from '@angular/core/testing';
import { accountRequest, answerAccount, setUpAccount } from '../../../testing/account-server';
import { settle } from '../../../testing/fake-notifier';
import { LocalSettings } from '../../platform/local-settings.service';
import { REFRESH_AFTER_MS } from '../events/return-refresh';
import { AccountTab } from './account-tab.service';

describe('AccountTab', () => {
  let backend: ReturnType<typeof setUpAccount>['backend'];
  let clock: { now: number };
  let tab: AccountTab;
  const stored = new Map<string, string>();

  beforeEach(() => {
    stored.clear();
    ({ backend, clock } = setUpAccount([
      {
        provide: LocalSettings,
        useValue: { read: (key: string) => stored.get(key) ?? null, write: (k: string, v: string) => stored.set(k, v) },
      },
    ]));
    tab = TestBed.inject(AccountTab);
  });

  afterEach(() => backend.verify());

  it('loads the ignore list with the tab, and each other sub-tab the first time it shows', async () => {
    tab.subTabShown('stats');
    answerAccount(backend, '/player/ignores', 'ignores.json');
    answerAccount(backend, '/playerStats', 'player-stats.xml');
    tab.subTabShown('overview');
    tab.subTabShown('stats');
    tab.subTabShown('history');
    answerAccount(backend, '/gameHistory/filters', 'game-history-filters.json');
    accountRequest(backend, '/gameHistory').flush('<gameHistory count="0" playerId="a"/>');
    tab.subTabShown('scenarios');
    accountRequest(backend, '/scenarios/mine').flush({ scenarios: [] });
    tab.subTabShown('history');
    await settle();
  });

  it('remembers the sub-tab shown, for `account` alone', () => {
    expect(tab.route(null)).toBe('overview');
    tab.subTabShown('scenarios');
    backend.match(() => true).forEach((request) => request.flush({}));
    expect(stored.get('gemp.account.subTab')).toBe('includes/account/myScenarios.html');
    expect(tab.route(null)).toBe('scenarios');
    expect(tab.route('history')).toBe('history');
    expect(tab.route('admin')).toBeNull();
  });

  it('refreshes the ignore list, the history and the shelf after 10 s away, never the stats', async () => {
    tab.subTabShown('history');
    backend.match(() => true).forEach((request) => request.flush('<gameHistory count="0" playerId="a"/>'));
    tab.subTabShown('stats');
    accountRequest(backend, '/playerStats').flush('<playerStats/>');
    clock.now += REFRESH_AFTER_MS;
    tab.subTabShown('history');
    answerAccount(backend, '/gameHistory/filters', 'game-history-filters.json');
    accountRequest(backend, '/gameHistory').flush('<gameHistory count="0" playerId="a"/>');
    tab.subTabShown('stats');
    clock.now += REFRESH_AFTER_MS;
    tab.subTabShown('overview');
    answerAccount(backend, '/player/ignores', 'ignores.json');
    tab.returns.setPageShown(false);
    clock.now += REFRESH_AFTER_MS;
    tab.subTabShown('stats');
    tab.returns.setPageShown(true);
    await settle();
  });
});
