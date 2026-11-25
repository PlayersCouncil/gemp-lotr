import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { INFO_ROUTES } from './info.routes';

describe('the Server Info tab (info.routes)', () => {
  async function open(url: string) {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        provideRouter([{ path: 'info', children: INFO_ROUTES }]),
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    TestBed.inject(HttpTestingController).match(() => true);
    await harness.fixture.whenStable();
    return { harness, el: harness.routeNativeElement as HTMLElement, router: TestBed.inject(Router) };
  }

  it('opens on Server Stats, its sub-tabs at the left', async () => {
    const { el, router } = await open('/info');
    expect(router.url).toBe('/info/stats');
    expect(el.querySelector('gd-hall-tab-frame')?.getAttribute('data-tab')).toBe('info');
    expect(Array.from(el.querySelectorAll('.gd-hall-sub-tabs__tab')).map((tab) => tab.textContent?.trim())).toEqual([
      'Server Stats',
      'Patch Notes',
      'Contribute',
      'About',
    ]);
    expect(el.querySelector('gd-server-stats')).not.toBeNull();
  });

  it('shows Contribute and About, their links in the same tab as the legacy ones', async () => {
    const { harness, el } = await open('/info/contribute');
    expect(el.querySelector('gd-contribute h1')?.textContent).toBe('How can I help?');
    const links = Array.from(el.querySelectorAll<HTMLAnchorElement>('gd-contribute a'));
    expect(links.length).toBe(6);
    expect(links.every((a) => !a.target)).toBe(true);
    expect(el.querySelector<HTMLImageElement>('gd-contribute img')?.getAttribute('src')).toBe(
      'https://liberapay.com/assets/widgets/donate.svg',
    );
    await harness.navigateByUrl('/info/about');
    const about = harness.routeNativeElement as HTMLElement;
    expect(about.querySelector('gd-about h1')?.textContent).toBe('About');
    expect(Array.from(about.querySelectorAll('li')).map((li) => li.textContent)).toEqual([
      'MarcinS',
      'TBiesty',
      'ketura',
      'PhallenCassidy',
      'dmaz',
    ]);
    expect(about.querySelector('p ul')).toBeNull();
  });

  it('sends an unknown address to Server Stats', async () => {
    const { router } = await open('/info/nothing');
    expect(router.url).toBe('/info/stats');
  });
});
