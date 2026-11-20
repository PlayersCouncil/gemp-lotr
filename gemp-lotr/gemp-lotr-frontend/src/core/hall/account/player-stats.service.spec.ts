import { TestBed } from '@angular/core/testing';
import { answerAccount, failAccount, setUpAccount } from '../../../testing/account-server';
import { settle } from '../../../testing/fake-notifier';
import { PlayerStatsView } from './player-stats.service';

describe('PlayerStatsView', () => {
  let backend: ReturnType<typeof setUpAccount>['backend'];
  let stats: PlayerStatsView;

  beforeEach(() => {
    ({ backend } = setUpAccount());
    stats = TestBed.inject(PlayerStatsView);
  });

  afterEach(() => backend.verify());

  it('loads once', async () => {
    expect(stats.state()).toEqual({ kind: 'loading' });
    void stats.start();
    void stats.start();
    answerAccount(backend, '/playerStats', 'player-stats.xml');
    await settle();
    expect(stats.state().kind).toBe('loaded');
  });

  it('says when it could not load', async () => {
    void stats.start();
    failAccount(backend, '/playerStats', 500);
    await settle();
    expect(stats.state()).toEqual({ kind: 'failed' });
  });
});
