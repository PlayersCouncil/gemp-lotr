import { TestBed } from '@angular/core/testing';
import { accountRequest, answerAccount, drawn, setUpAccountPages } from '../../../testing/account-server';
import { HallComms } from '../../../core/hall/hall-comms.service';
import { Notifier } from '../../../core/notifier/notifier';
import { PlayerInfo } from '../../../core/session/player-info.service';
import { FakeNotifier } from '../../../testing/fake-notifier';

describe('AccountOverviewComponent', () => {
  it('shows the pocket and the ignore list, and ignores the name typed', async () => {
    const { backend, harness } = await setUpAccountPages();
    await harness.navigateByUrl('/account');
    TestBed.inject(HallComms).currency.set(1234);
    accountRequest(backend, '/player/ignores').flush({ ignored: [] });
    await drawn(harness);
    const host = harness.fixture.nativeElement as HTMLElement;
    expect(host.querySelector('.gd-account-overview__heading')?.textContent?.trim()).toBe('My Account:');
    expect(host.querySelector('.gd-account-overview__pocket')?.textContent?.replace(/\s+/g, ' ').trim()).toBe('12 34');
    const price = host.querySelector('.gd-account-overview__pocket gd-price');
    expect(price?.getAttribute('aria-label')).toBe('12 gold 34 silver');
    expect(
      Array.from(price?.querySelectorAll('[data-coin]') ?? []).map((coin) => coin.getAttribute('data-coin')),
    ).toEqual(['gold', 'silver']);
    expect(host.querySelector('.gd-account-overview__migrate')).toBeNull();
    expect(host.querySelector('.gd-account-overview__empty')?.textContent).toBe('You are not ignoring anyone.');

    const field = host.querySelector('#gd-account-ignore-name') as HTMLInputElement;
    field.value = 'erin';
    field.dispatchEvent(new Event('input'));
    await drawn(harness);
    (host.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    answerAccount(backend, '/player/ignores/add', 'ignore-added.json', 'POST');
    await drawn(harness);
    expect(host.querySelector('.gd-account-overview__status')?.textContent).toBe('erin is now ignored.');
    expect(field.value).toBe('');
    const rows = Array.from(host.querySelectorAll('.gd-account-overview__list li'));
    expect(rows.map((li) => li.querySelector('.gd-account-overview__ignored')?.textContent)).toEqual(['Carol', 'erin']);
    const remove = rows[1]?.querySelector('button') as HTMLButtonElement;
    expect([remove.textContent?.trim(), remove.getAttribute('aria-label')]).toEqual(['Remove', 'Stop ignoring erin']);
    remove.click();
    accountRequest(backend, '/player/ignores/remove', 'POST').flush({ message: 'erin removed.', ignored: ['Carol'] });
    await drawn(harness);
    expect(host.querySelectorAll('.gd-account-overview__list li')).toHaveLength(1);
    backend.match(() => true);
  });

  it('offers the trophies migration to a player who has trophies, asking first', async () => {
    const { backend, harness } = await setUpAccountPages();
    TestBed.inject(PlayerInfo).load().subscribe();
    accountRequest(backend, '/player').flush({ name: 'alice', type: 'u', hasTrophies: true });
    await harness.navigateByUrl('/account');
    accountRequest(backend, '/player/ignores').flush({ ignored: [] });
    await drawn(harness);
    const host = harness.fixture.nativeElement as HTMLElement;
    expect(host.querySelector('.gd-account-overview__heading')?.textContent?.trim()).toBe('My Account: alice');
    const migrate = host.querySelector<HTMLButtonElement>('.gd-account-overview__migrate')!;
    expect(migrate.textContent?.trim()).toBe('Migrate Trophies to My Cards');
    migrate.click();
    await drawn(harness);
    expect((TestBed.inject(Notifier) as FakeNotifier).dialogs[0]).toMatch(/^confirm: This will permanently migrate/);
    backend.expectNone((request) => request.url.endsWith('/collection/migrate'));
    backend.match(() => true);
  });
});
