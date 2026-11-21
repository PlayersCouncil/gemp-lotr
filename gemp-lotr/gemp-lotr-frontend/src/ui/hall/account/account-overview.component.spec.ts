import { TestBed } from '@angular/core/testing';
import { accountRequest, answerAccount, drawn, setUpAccountPages } from '../../../testing/account-server';
import { HallComms } from '../../../core/hall/hall-comms.service';

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
    expect(host.querySelectorAll('.gd-account-overview__pocket img')).toHaveLength(2);
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
});
