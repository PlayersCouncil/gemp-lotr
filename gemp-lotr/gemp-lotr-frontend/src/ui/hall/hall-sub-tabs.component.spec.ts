import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { HallSubTab } from '../../core/hall/hall-tabs';
import { HallSubTabsComponent } from './hall-sub-tabs.component';

const TABS: HallSubTab[] = [
  { slug: 'overview', path: '', label: 'My Account' },
  { slug: 'history', path: 'history', label: 'My Game History' },
  { slug: 'stats', path: 'stats', label: 'My Stats' },
];

@Component({
  imports: [HallSubTabsComponent],
  template: `<gd-hall-sub-tabs [tabs]="tabs" base="/account" label="My Account" />`,
})
class Page {
  readonly tabs = TABS;
}

describe('HallSubTabsComponent', () => {
  let harness: RouterTestingHarness;

  beforeEach(async () => {
    TestBed.configureTestingModule({ providers: [provideRouter([{ path: '**', component: Page }])] });
    harness = await RouterTestingHarness.create();
  });

  async function open(url: string) {
    await harness.navigateByUrl(url);
    await harness.fixture.whenStable();
    return Array.from(harness.fixture.nativeElement.querySelectorAll('.gd-hall-sub-tabs__tab')) as HTMLAnchorElement[];
  }

  it('draws a tab list of router links, the one at the address (or above it) selected', async () => {
    let tabs = await open('/account');
    const list = harness.fixture.nativeElement.querySelector('.gd-hall-sub-tabs__list') as HTMLElement;
    expect([list.getAttribute('role'), list.getAttribute('aria-label'), list.getAttribute('aria-orientation')]).toEqual(
      ['tablist', 'My Account', 'vertical'],
    );
    expect(tabs.map((tab) => [tab.getAttribute('role'), tab.textContent, tab.getAttribute('href')])).toEqual([
      ['tab', 'My Account', '/account'],
      ['tab', 'My Game History', '/account/history'],
      ['tab', 'My Stats', '/account/stats'],
    ]);
    expect(tabs.map((tab) => tab.getAttribute('aria-selected'))).toEqual(['true', 'false', 'false']);

    tabs = await open('/account/history/game-12');
    expect(tabs.map((tab) => tab.getAttribute('aria-selected'))).toEqual(['false', 'true', 'false']);
    expect(tabs.map((tab) => tab.dataset['subTab'])).toEqual(['overview', 'history', 'stats']);
  });

  it('opens a sub-tab when clicked, and moves the focus with the arrow keys, Home and End', async () => {
    const tabs = await open('/account');
    document.body.appendChild(harness.fixture.nativeElement);
    tabs[2]?.click();
    await harness.fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/account/stats');

    const key = (tab: HTMLElement | undefined, name: string) =>
      tab?.dispatchEvent(new KeyboardEvent('keydown', { key: name, cancelable: true }));
    tabs[0]?.focus();
    key(tabs[0], 'ArrowDown');
    expect(document.activeElement).toBe(tabs[1]);
    key(tabs[1], 'ArrowRight');
    expect(document.activeElement).toBe(tabs[2]);
    key(tabs[2], 'ArrowDown');
    expect(document.activeElement).toBe(tabs[0]);
    key(tabs[0], 'ArrowUp');
    expect(document.activeElement).toBe(tabs[2]);
    key(tabs[2], 'Home');
    expect(document.activeElement).toBe(tabs[0]);
    key(tabs[0], 'End');
    expect(document.activeElement).toBe(tabs[2]);
    expect(key(tabs[2], 'a')).toBe(true);
    harness.fixture.nativeElement.remove();
  });
});
