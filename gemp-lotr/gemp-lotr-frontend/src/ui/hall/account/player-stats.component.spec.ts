import { answerAccount, drawn, failAccount, setUpAccountPages } from '../../../testing/account-server';

describe('PlayerStatsComponent', () => {
  it('draws a table per group, or says there are none', async () => {
    const { backend, harness } = await setUpAccountPages();
    await harness.navigateByUrl('/account/stats');
    answerAccount(backend, '/playerStats', 'player-stats.xml');
    await drawn(harness);
    const host = harness.fixture.nativeElement.querySelector('gd-account-stats') as HTMLElement;
    expect(host.dataset['state']).toBe('loaded');
    expect(host.querySelector('.gd-account-stats__intro')?.textContent).toBe('All times are GMT based.');
    expect(Array.from(host.querySelectorAll('.gd-account-stats__heading')).map((h) => h.textContent)).toEqual([
      'Casual statistics',
      'Competitive statistics',
    ]);
    const cells = Array.from(host.querySelectorAll('[data-group=casual] td')).map((td) => td.textContent);
    expect(cells).toEqual(['Fellowship Block', 'Gondor & friends', '5', '3', '62.5%']);
    expect(host.querySelector('i[data-group=competitive]')?.textContent).toBe(
      'You have not played any games counting for this statistics',
    );
    backend.match(() => true);
  });

  it('says when they could not be loaded', async () => {
    const { backend, harness } = await setUpAccountPages();
    await harness.navigateByUrl('/account/stats');
    failAccount(backend, '/playerStats', 500);
    await drawn(harness);
    expect(harness.fixture.nativeElement.querySelector('.gd-account-stats__failed')?.textContent).toBe(
      'Could not load your stats. Please try again later.',
    );
    backend.match(() => true);
  });
});
