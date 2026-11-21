import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { HallTabFrameComponent } from './hall-tab-frame.component';

const told: { subTabs: (string | null)[]; shown: boolean[] } = { subTabs: [], shown: [] };

@Component({ template: '<p class="page">page</p>' })
class PageComponent {}

@Component({
  imports: [HallTabFrameComponent],
  template: `<gd-hall-tab-frame tab="account" (subTabChange)="subTab($event)" (pageShownChange)="shown($event)"
    ><span class="extra">projected</span></gd-hall-tab-frame
  >`,
})
class TabComponent {
  subTab(slug: string | null): void {
    told.subTabs.push(slug);
  }
  shown(shown: boolean): void {
    told.shown.push(shown);
  }
}

describe('HallTabFrameComponent', () => {
  async function open(url: string) {
    told.subTabs = [];
    told.shown = [];
    TestBed.configureTestingModule({
      providers: [
        provideRouter([
          {
            path: 'account',
            component: TabComponent,
            children: [
              { path: '', component: PageComponent },
              { path: 'stats', component: PageComponent },
            ],
          },
        ]),
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    return { harness, el: harness.routeNativeElement as HTMLElement };
  }

  it('draws the sub-tabs, the page and what the tab projects, and says which sub-tab shows', async () => {
    const { harness, el } = await open('/account/stats');
    const frame = el.querySelector('gd-hall-tab-frame')!;
    expect(frame.getAttribute('data-tab')).toBe('account');
    expect(frame.getAttribute('data-sub-tab')).toBe('stats');
    expect(frame.querySelector('gd-hall-sub-tabs')).not.toBeNull();
    expect(frame.querySelector('.gd-hall-tab-frame__page[data-page="panel"] .page')).not.toBeNull();
    expect(frame.querySelector('.gd-hall-tab-frame__page + .extra')?.textContent).toBe('projected');
    expect(told.subTabs).toEqual(['stats']);
    expect(told.shown).toEqual([true]);
    await harness.navigateByUrl('/account');
    // a sub-tab whose path is '' at the tab's own address
    expect(told.subTabs).toEqual(['stats', 'overview']);
  });

  it('tells when the window is blurred or the browser tab hidden, and back', async () => {
    await open('/account');
    window.dispatchEvent(new Event('blur'));
    window.dispatchEvent(new Event('blur'));
    window.dispatchEvent(new Event('focus'));
    expect(told.shown).toEqual([true, false, true]);
    const visibility = vi.spyOn(document, 'visibilityState', 'get').mockReturnValue('hidden');
    document.dispatchEvent(new Event('visibilitychange'));
    visibility.mockReturnValue('visible');
    document.dispatchEvent(new Event('visibilitychange'));
    expect(told.shown).toEqual([true, false, true, false, true]);
    visibility.mockRestore();
  });
});
