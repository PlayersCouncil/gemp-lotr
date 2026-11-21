import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideGempHttp } from '../../../../core/api/gemp-interceptors';
import { PcErrataService } from '../../../../core/hall/help/errata/errata.service';
import { readServerResponse } from '../../../../testing/files';
import { ERRATA_ROUTE } from './errata.routes';

const URL = '/gemp-lotr-server/hall/errata/json';

describe('ErrataPageComponent', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'help', children: [ERRATA_ROUTE, { path: 'other', children: [] }] }]),
      ],
    });
    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.verify());

  async function open(url: string, answer = true): Promise<HTMLElement> {
    await harness.navigateByUrl(url);
    if (answer) {
      backend.expectOne((req) => req.url === URL).flush(readServerResponse('errata.json'));
    }
    await settle();
    return harness.fixture.nativeElement as HTMLElement;
  }

  async function settle(): Promise<void> {
    for (let i = 0; i < 3; i++) {
      await harness.fixture.whenStable();
      harness.fixture.detectChanges();
    }
  }

  const texts = (root: HTMLElement, selector: string) =>
    Array.from(root.querySelectorAll(selector)).map((element) => element.textContent?.trim().replace(/\s+/g, ' '));
  const router = () => TestBed.inject(Router);

  it('says "Loading the errata…" in the table until the list comes', async () => {
    const page = await open('/help/errata', false);
    expect(texts(page, '.gd-errata-table__loading')).toEqual(['Loading the errata…']);
    expect(page.querySelector('.gd-errata-page__filters')?.children.length).toBe(0);
    backend.expectOne((req) => req.url === URL).flush(readServerResponse('errata.json'));
    await settle();
    expect(page.querySelectorAll('tr.gd-errata-table__row')).toHaveLength(7);
  });

  it('draws the title, chips, filters, table and viewer, the first card on show', async () => {
    const page = await open('/help/errata');
    expect(texts(page, 'h1')).toEqual(['PC Errata i']);
    expect(texts(page, '.gd-errata-page__chip')).toEqual(['All7', 'Latest3']);
    expect(page.querySelector('.gd-errata-page__chip')?.getAttribute('aria-checked')).toBe('true');
    expect(texts(page, '#gd-errata-set option')[1]).toBe('1 – The Fellowship of the Ring (3)');
    expect(texts(page, '#gd-errata-format option')).toEqual([
      'All formats',
      'PC-FotR Block (2)',
      'PC-Movie Block (5)',
      'PC Playtest (1)',
    ]);
    expect(texts(page, '.gd-errata-page__status')).toEqual(['7 cards']);
    expect(texts(page, 'th button')).toEqual(['Card ▲', 'Culture', 'Title', '#']);
    expect(texts(page, 'tr.gd-errata-table__row:first-child td')).toEqual([
      '1R45New',
      '',
      'Gandalf, The Grey Wizard',
      '1',
    ]);
    expect(page.querySelector('td[data-column=culture] img')?.getAttribute('src')).toBe(
      '/gemp-lotr/images/cultures/gandalf.png',
    );
    expect(page.querySelector('tr[aria-current]')?.getAttribute('data-id')).toBe('51_45');
    expect(texts(page, '.gd-errata-viewer__counter')).toEqual(['1 of 7']);
    expect(page.querySelector('gd-errata-frame')?.getAttribute('data-id')).toBe('51_45');
    expect(page.querySelector('.gd-errata-page__summary')?.textContent).toBe(
      '7 cards: 5 PC errata of Decipher cards and 2 revised V-set cards.',
    );
    // the bare sub-tab leaves the address as it is
    expect(router().url).toBe('/help/errata');
  });

  it('opens a link’s card, then keeps the address on the card on show', async () => {
    const page = await open('/help/errata/5_10?show=recent');
    expect(page.querySelector('gd-errata-frame')?.getAttribute('data-id')).toBe('55_10');
    expect(texts(page, '.gd-errata-viewer__counter')).toEqual(['2 of 3']);
    expect(router().url).toBe('/help/errata/55_10?show=recent');
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'j' }));
    await settle();
    expect(router().url).toBe('/help/errata/103_19?show=recent');
    expect(texts(page, '.gd-errata-viewer__counter')).toEqual(['3 of 3']);
  });

  it('selects a clicked row, filters with the lists and the chips, sorts with the headers', async () => {
    const page = await open('/help/errata');
    (page.querySelectorAll('tr.gd-errata-table__row')[4] as HTMLElement).click();
    await settle();
    expect(page.querySelector('tr[aria-current]')?.getAttribute('data-id')).toBe('55_10');
    expect(router().url).toBe('/help/errata/55_10');

    const set = page.querySelector('#gd-errata-set') as HTMLSelectElement;
    set.value = '1';
    set.dispatchEvent(new Event('change'));
    await settle();
    expect(texts(page, '.gd-errata-page__status')).toEqual(['3 of 7 cards']);
    expect(router().url).toBe('/help/errata/51_45?set=1');

    (page.querySelectorAll('.gd-errata-page__chip')[1] as HTMLElement).click();
    await harness.fixture.whenStable();
    expect(texts(page, 'tr.gd-errata-table__row')).toHaveLength(1);

    (page.querySelectorAll('th button')[3] as HTMLElement).click();
    await harness.fixture.whenStable();
    expect(page.querySelectorAll('th')[3]?.getAttribute('aria-sort')).toBe('ascending');
  });

  it('says why nothing is listed, in the table and the viewer', async () => {
    const page = await open('/help/errata?format=fotr_block');
    expect(texts(page, '.gd-errata-table__empty')).toEqual(['No PC errata are in effect in Fellowship Block.']);
    expect(texts(page, '.gd-errata-viewer__empty p')).toEqual([
      'No PC errata are in effect in Fellowship Block.',
      'Change the search, or pick another set, format or view.',
    ]);
    expect(page.querySelector('.gd-errata-page__status')?.hasAttribute('data-empty')).toBe(true);
    expect(texts(page, '#gd-errata-format option').at(-1)).toBe('Fellowship Block (0)');
  });

  it('shows the load failure in the table', async () => {
    const page = await open('/help/errata', false);
    backend.expectOne((req) => req.url === URL).flush('', { status: 404, statusText: 'Not Found' });
    await settle();
    expect(texts(page, '.gd-errata-table__error')).toEqual(['The errata list could not be found.']);
  });

  it('browses with the keys, but not from a field or under a dialog; Escape closes Enlarge', async () => {
    const page = await open('/help/errata');
    const errata = harness.routeDebugElement?.injector.get(PcErrataService) as PcErrataService;
    const press = (key: string, target: EventTarget = document) => {
      target.dispatchEvent(new KeyboardEvent('keydown', { key, bubbles: true }));
    };
    press('End');
    expect(errata.index()).toBe(6);
    press('k');
    expect(errata.index()).toBe(5);
    press('Home');
    expect(errata.index()).toBe(0);
    press('o');
    expect(errata.showOriginal()).toBe(true);
    press('j', page.querySelector('#gd-errata-q') as HTMLElement);
    expect(errata.index()).toBe(0);
    errata.setEnlarged(true);
    await harness.fixture.whenStable();
    expect(page.querySelector('.gd-errata-page__backdrop')?.hasAttribute('hidden')).toBe(false);
    press('Escape');
    expect(errata.enlarged()).toBe(false);
  });

  it('stops hearing the keys once the page is left', async () => {
    await open('/help/errata');
    const errata = harness.routeDebugElement?.injector.get(PcErrataService) as PcErrataService;
    const listen = vi.spyOn(errata, 'step');
    await harness.navigateByUrl('/help/other');
    expect(harness.fixture.nativeElement.querySelector('gd-errata-page')).toBeNull();
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'j' }));
    expect(listen).not.toHaveBeenCalled();
  });

  it('opens and closes the (i) text', async () => {
    const page = await open('/help/errata');
    const about = page.querySelector('#gd-errata-about') as HTMLElement;
    expect(about.hidden).toBe(true);
    const toggle = page.querySelector('h1 [data-info-toggle]') as HTMLButtonElement;
    expect([toggle.title, toggle.getAttribute('aria-expanded')]).toEqual(['What is this?', 'false']);
    toggle.click();
    await harness.fixture.whenStable();
    expect(about.hidden).toBe(false);
    expect(toggle.getAttribute('aria-expanded')).toBe('true');
  });
});
