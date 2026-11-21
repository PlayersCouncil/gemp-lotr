import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { HELP_ROUTES } from './help.routes';

describe('the Help tab (help.routes)', () => {
  beforeEach(() => {
    Element.prototype.scrollIntoView = vi.fn();
  });

  async function open(url: string) {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'help', children: HELP_ROUTES }]),
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    await harness.fixture.whenStable();
    return { harness, el: harness.routeNativeElement as HTMLElement, router: TestBed.inject(Router) };
  }

  it('opens on How Do I Play?, its sub-tabs at the left', async () => {
    const { el, router } = await open('/help');
    expect(router.url).toBe('/help/how-to-play');
    const frame = el.querySelector('gd-hall-tab-frame');
    expect(frame?.getAttribute('data-tab')).toBe('help');
    const tabs = Array.from(el.querySelectorAll('.gd-hall-sub-tabs__tab'));
    expect(tabs.map((tab) => [tab.textContent?.trim(), tab.getAttribute('aria-selected')])).toEqual([
      ['How Do I Play?', 'true'],
      ['Code of Conduct', 'false'],
      ['Format Definitions', 'false'],
      ['What Are Leagues?', 'false'],
      ['PC Errata', 'false'],
    ]);
    const videos = Array.from(el.querySelectorAll<HTMLIFrameElement>('.gd-how-to-play__video'));
    expect(videos.map((video) => video.getAttribute('src'))).toEqual([
      'https://www.youtube.com/embed/sBUS8yt4G_8',
      'https://www.youtube.com/embed/azvdgl4LScs',
      'https://www.youtube.com/embed/BKE_81bm9fo',
    ]);
    expect(el.querySelector('gd-hall-article h1#manual')?.textContent).toBe('Manual');
  });

  it('scrolls to the Manual for #manual (the legacy #help/manual)', async () => {
    await open('/help/how-to-play#manual');
    expect(Element.prototype.scrollIntoView).toHaveBeenCalled();
  });

  it('scrolls to the Manual when #manual is added while the page shows', async () => {
    const { harness } = await open('/help/how-to-play');
    const scroll = vi.mocked(Element.prototype.scrollIntoView);
    expect(scroll).not.toHaveBeenCalled();
    await harness.navigateByUrl('/help/how-to-play#manual');
    await harness.fixture.whenStable();
    expect(scroll).toHaveBeenCalledTimes(1);
    expect(scroll.mock.contexts[0]).toBe(harness.routeNativeElement!.querySelector('#manual'));
  });

  it('shows the prose pages with the legacy wording and fixed markup', async () => {
    const { harness, el } = await open('/help/conduct');
    expect(el.querySelector('gd-conduct h2')?.textContent).toBe("Don't Cheat");
    await harness.navigateByUrl('/help/leagues');
    const leagues = harness.routeNativeElement as HTMLElement;
    expect(Array.from(leagues.querySelectorAll('h1')).map((h) => h.id)).toEqual([
      '',
      'join',
      'sealed',
      'gold',
      'prizes',
      'cheating',
    ]);
    expect(leagues.querySelector('p figure, p div, p ol')).toBeNull();
    expect(leagues.querySelector<HTMLImageElement>('figure img')?.getAttribute('src')).toBe(
      '/gemp-lotr/images/help/league-standings.jpg',
    );
    expect(leagues.querySelector('figcaption')?.textContent).toContain('Some players ranked in a past League');
    expect(leagues.querySelectorAll('table.gd-hall-article__table')).toHaveLength(2);
    expect(leagues.querySelector('a.gd-leagues__link')?.getAttribute('href')).toBe('/events/leagues');
    expect(leagues.querySelector('a.gd-leagues__link')?.parentElement?.textContent).toBe(
      'Navigate to the Events > Current Leagues tab:',
    );
  });

  it('opens PC Errata on its sub-tab, at the card its address names', async () => {
    const { el, router, harness } = await open('/help/errata/1_5?format=pc_movie');
    await harness.fixture.whenStable();
    expect(router.url.startsWith('/help/errata')).toBe(true);
    expect(el.querySelector('.gd-hall-sub-tabs__tab[data-sub-tab="pc-errata"]')?.getAttribute('aria-selected')).toBe(
      'true',
    );
    expect(el.querySelector('gd-errata-page')).not.toBeNull();
  });

  it("shows a card share link's card over the sub-tab, and takes the parameter away once it is closed", async () => {
    const { harness, router } = await open('/help?card=1_5');
    const info = harness.routeDebugElement!.injector.get(CardInfoService);
    expect(info.card()?.blueprintId).toBe('1_5');
    expect(router.url).toBe('/help/how-to-play?card=1_5');
    info.close();
    await harness.fixture.whenStable();
    expect(router.url).toBe('/help/how-to-play');
  });

  it('sends an unknown address to How Do I Play?', async () => {
    const { router } = await open('/help/nothing');
    expect(router.url).toBe('/help/how-to-play');
  });
});
