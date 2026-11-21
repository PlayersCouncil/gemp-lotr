import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { answerAccount, drawn, setUpAccountPages } from '../../../testing/account-server';

describe('AccountTabComponent and the tab’s routes', () => {
  it('shows each sub-tab at its address, with the sub-tab bar, and remembers it', async () => {
    const { backend, harness, stored } = await setUpAccountPages();
    await harness.navigateByUrl('/account/stats');
    await drawn(harness);
    const host = harness.fixture.nativeElement as HTMLElement;
    expect(host.querySelector('gd-account-tab')?.getAttribute('data-sub-tab')).toBe('stats');
    expect(Array.from(host.querySelectorAll('.gd-hall-sub-tabs__tab')).map((tab) => tab.textContent)).toEqual([
      'My Account',
      'My Game History',
      'My Stats',
      'My Scenarios',
    ]);
    answerAccount(backend, '/player/ignores', 'ignores.json');
    answerAccount(backend, '/playerStats', 'player-stats.xml');
    await drawn(harness);
    expect(host.querySelector('gd-account-stats')).not.toBeNull();
    expect(stored.get('gemp.account.subTab')).toBe('includes/account/yourStats.html');

    await harness.navigateByUrl('/account/nothing');
    await drawn(harness);
    expect(TestBed.inject(Router).url).toBe('/account');
    expect(host.querySelector('gd-account-overview')).not.toBeNull();
    backend.match(() => true);
  });

  it('opens the sub-tab last shown when the tab is entered from another tab', async () => {
    const stored = new Map([['gemp.account.subTab', 'includes/account/myScenarios.html']]);
    const { backend, harness } = await setUpAccountPages(stored);
    const router = TestBed.inject(Router);
    await harness.navigateByUrl('/hall');
    await harness.navigateByUrl('/account');
    expect(router.url).toBe('/account/scenarios');
    // the sub-tab bar's My Account, from inside the tab
    await harness.navigateByUrl('/account');
    expect(router.url).toBe('/account');
    backend.match(() => true);
  });

  it('shows My Account for a link to it (the page opened at its address)', async () => {
    const stored = new Map([['gemp.account.subTab', 'includes/account/myScenarios.html']]);
    const { backend, harness } = await setUpAccountPages(stored);
    await harness.navigateByUrl('/account');
    expect(TestBed.inject(Router).url).toBe('/account');
    backend.match(() => true);
  });
});
