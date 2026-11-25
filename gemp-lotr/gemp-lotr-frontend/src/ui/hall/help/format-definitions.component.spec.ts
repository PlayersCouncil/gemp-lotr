import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { provideGempHttp } from '../../../core/api/gemp-interceptors';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { FILTER_DELAY_MS, FORMAT_IMAGES_KEY } from '../../../core/hall/help/format-definitions.service';
import { LocalSettings } from '../../../core/platform/local-settings.service';
import { readServerResponse } from '../../../testing/files';
import { formatsMatcher } from './format-definitions.component';
import { HELP_ROUTES } from './help.routes';

describe('FormatDefinitionsComponent', () => {
  let settings: Map<string, string>;
  let scrolled: string[];

  beforeEach(() => {
    settings = new Map();
    scrolled = [];
    Element.prototype.scrollIntoView = function (this: Element) {
      scrolled.push(this.id);
    };
  });

  // jsdom has no scrollIntoView, and the spec files share one window: what stands in for it goes with the test
  afterEach(() => {
    delete (Element.prototype as { scrollIntoView?: unknown }).scrollIntoView;
  });

  async function open(url: string, answer: string | number = readServerResponse('format-definitions.json')) {
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'help', children: HELP_ROUTES }]),
        {
          provide: LocalSettings,
          useValue: {
            read: (key: string) => settings.get(key) ?? null,
            write: (k: string, v: string) => settings.set(k, v),
          },
        },
      ],
    });
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    const request = TestBed.inject(HttpTestingController).expectOne((req) => req.url.endsWith('/hall/formats/json'));
    if (typeof answer === 'number') {
      request.flush('', { status: answer, statusText: 'x' });
    } else {
      request.flush(answer);
    }
    await harness.fixture.whenStable();
    harness.detectChanges();
    return { harness, el: harness.routeNativeElement as HTMLElement };
  }

  it('matches formats and formats/<code> as one route', () => {
    const segment = (path: string) => ({ path, parameters: {}, parameterMap: null as never });
    expect(formatsMatcher([segment('formats')] as never)?.posParams).toEqual({});
    expect(formatsMatcher([segment('formats'), segment('pc_movie')] as never)?.posParams?.['code']?.path).toBe(
      'pc_movie',
    );
    expect(formatsMatcher([segment('leagues')] as never)).toBeNull();
  });

  it('draws the formats: the list, each section with its badges, facts and card lists', async () => {
    const { el } = await open('/help/formats');
    expect(Array.from(el.querySelectorAll('.gd-formats__nav-name')).map((n) => n.textContent)).toEqual([
      'Fellowship Block (PC)',
      'Anything Goes',
      'PC Playtest',
    ]);
    expect(el.querySelector('.gd-formats__nav-meta')?.textContent).toBe('X4 R1');
    const first = el.querySelector<HTMLElement>('section.gd-format')!;
    expect(first.id).toBe('format-pc_fotr_block');
    expect(first.querySelector('[data-badge="pc"]')?.textContent?.trim()).toBe('PC errata');
    expect(Array.from(first.querySelectorAll('dt')).map((dt) => dt.textContent)).toEqual([
      'Sets',
      'Sites',
      'Ring-bearer skirmish cancel',
      'Errata',
    ]);
    expect(first.querySelector<HTMLAnchorElement>('.gd-format__errata')?.getAttribute('href')).toBe(
      '/help/errata?format=pc_fotr_block',
    );
    expect(Array.from(first.querySelectorAll('.gd-format__set')).map((s) => s.textContent)).toEqual([
      '1',
      '2',
      'V1',
      '9',
      '1',
    ]);
    expect(first.querySelector('.gd-format__chip')?.getAttribute('title')).toBe('·Ottar, Man of Laketown (1U44)');
    expect(el.querySelectorAll('section.gd-format')[2]?.querySelector('.gd-format__no-lists')?.textContent).toContain(
      'every card from its sets is legal',
    );
    expect(el.querySelector('.gd-format__name-chip')?.textContent).toBe('Gandalf');
    expect(el.querySelector('.gd-formats__help a.gd-formats__errata-link')?.getAttribute('href')).toBe('/help/errata');
  });

  it("says what went wrong in the page's place", async () => {
    const { el } = await open('/help/formats', 500);
    expect(el.querySelector('.gd-formats__status[data-error]')?.textContent).toContain('Could not load the format');
  });

  it('shows a card from its name (click or right-click)', async () => {
    const { el, harness } = await open('/help/formats');
    const info = harness.routeDebugElement!.injector.get(CardInfoService);
    el.querySelector<HTMLButtonElement>('.gd-format__chip')!.click();
    expect(info.card()?.blueprintId).toBe('1_45');
    // headed by the card's name, as the legacy preview's data-title
    expect(info.title()).toBe('·Ottar, Man of Laketown');
    el.querySelectorAll<HTMLButtonElement>('.gd-format__chip')[1]!.dispatchEvent(
      new MouseEvent('contextmenu', { bubbles: true, cancelable: true }),
    );
    expect(info.card()?.blueprintId).toBe('2_32');
  });

  it('filters the formats and marks the cards found', async () => {
    {
      const { el, harness } = await open('/help/formats');
      const filter = el.querySelector<HTMLInputElement>('.gd-formats__filter')!;
      filter.value = 'eowyn';
      filter.dispatchEvent(new Event('input'));
      harness.detectChanges();
      expect(Array.from(el.querySelectorAll<HTMLElement>('section.gd-format')).every((s) => !s.hidden)).toBe(true);
      await new Promise((resolve) => setTimeout(resolve, FILTER_DELAY_MS + 20));
      harness.detectChanges();
      const sections = Array.from(el.querySelectorAll<HTMLElement>('section.gd-format'));
      expect(sections.map((s) => s.hidden)).toEqual([false, true, true]);
      expect(el.querySelector('.gd-formats__filter-status')?.textContent).toBe('1 of 3 formats (1 by a listed card)');
      expect(el.querySelector('.gd-format__chip[data-hit]')?.textContent?.trim()).toBe('·Éowyn, Lady of Rohan');
    }
  });

  it('shows the lists as card images, remembered on the device', async () => {
    const { el, harness } = await open('/help/formats');
    const box = el.querySelector<HTMLInputElement>('.gd-formats__images input')!;
    box.checked = true;
    box.dispatchEvent(new Event('change'));
    harness.detectChanges();
    expect(settings.get(FORMAT_IMAGES_KEY)).toBe('1');
    expect(el.querySelectorAll('.gd-format__thumb button.gd-card').length).toBe(8);
    expect(el.querySelector('.gd-format__thumb-name')?.textContent).toBe('·Ottar, Man of Laketown');
    // a picture is used as the name was: the card, headed by its name
    const info = harness.routeDebugElement!.injector.get(CardInfoService);
    el.querySelector<HTMLButtonElement>('.gd-format__thumb button.gd-card')!.click();
    expect(info.card()?.blueprintId).toBe('1_45');
    expect(info.title()).toBe('·Ottar, Man of Laketown');
  });

  it('brings the format an address names into view, highlighted', async () => {
    const { el } = await open('/help/formats/pc_playtest');
    expect(scrolled).toContain('format-pc_playtest');
    expect(el.querySelector('section.gd-format[data-highlight]')?.id).toBe('format-pc_playtest');
    expect(el.querySelector('.gd-formats__nav li[data-active] .gd-formats__nav-name')?.textContent).toBe('PC Playtest');
  });

  it('opens a format from the list, putting it in the address', async () => {
    const { el, harness } = await open('/help/formats');
    el.querySelectorAll<HTMLAnchorElement>('.gd-formats__nav a')[1]!.click();
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe('/help/formats/rev_tow_sta');
    expect(scrolled).toContain('format-rev_tow_sta');
  });

  describe('the panel', () => {
    /** Each element's box, by a test-chosen key; anything else has an empty box. */
    let boxes: Map<Element, { top: number; bottom: number }>;

    beforeEach(() => {
      boxes = new Map();
      vi.spyOn(Element.prototype, 'getBoundingClientRect').mockImplementation(function (this: Element) {
        const box = boxes.get(this) ?? { top: 0, bottom: 0 };
        return {
          top: box.top,
          bottom: box.bottom,
          height: box.bottom - box.top,
          left: 0,
          right: 0,
          width: 0,
        } as DOMRect;
      });
    });

    afterEach(() => {
      vi.restoreAllMocks();
      vi.unstubAllGlobals();
    });

    const frame = (el: HTMLElement) =>
      el.closest('gd-hall-tab-frame') ?? el.ownerDocument.querySelector('gd-hall-tab-frame')!;
    const sections = (el: HTMLElement) => Array.from(el.querySelectorAll<HTMLElement>('section.gd-format'));

    /** The panel shows 0 to 500; the formats are 300px tall from `top` (the first) on. */
    function layOut(el: HTMLElement, top: number): void {
      boxes.set(frame(el), { top: 0, bottom: 500 });
      sections(el).forEach((section, i) => boxes.set(section, { top: top + i * 300, bottom: top + (i + 1) * 300 }));
    }

    async function scroll(harness: RouterTestingHarness, el: HTMLElement): Promise<void> {
      frame(el).dispatchEvent(new Event('scroll'));
      await new Promise((resolve) => setTimeout(resolve, 40));
      await harness.fixture.whenStable();
      harness.detectChanges();
    }

    const active = (el: HTMLElement) =>
      el.querySelector('.gd-formats__nav li[data-active] .gd-formats__nav-name')?.textContent;

    it('keeps the list as tall as the panel inside its padding (--gd-formats-view-height)', async () => {
      let resized: (() => void) | null = null;
      vi.stubGlobal(
        'ResizeObserver',
        class {
          constructor(callback: () => void) {
            resized = callback;
          }
          observe = vi.fn();
          disconnect = vi.fn();
        },
      );
      const { el, harness } = await open('/help/formats');
      const scroller = frame(el) as HTMLElement;
      Object.defineProperty(scroller, 'clientHeight', { configurable: true, value: 600 });
      scroller.style.padding = '10px 0 20px';
      resized!();
      harness.detectChanges();
      expect(
        el.querySelector<HTMLElement>('gd-format-definitions')!.style.getPropertyValue('--gd-formats-view-height'),
      ).toBe('570px');
    });

    it('follows the reading: the format at the top of the panel is marked and put in the address', async () => {
      const { el, harness } = await open('/help/formats');
      const router = TestBed.inject(Router);
      layOut(el, -320);
      await scroll(harness, el);
      expect(router.url).toBe('/help/formats/rev_tow_sta');
      expect(active(el)).toBe('Anything Goes');
      // the address it put there reveals nothing
      expect(scrolled).toEqual([]);
      layOut(el, -500);
      await scroll(harness, el);
      expect(router.url).toBe('/help/formats/rev_tow_sta');
      layOut(el, -580);
      await scroll(harness, el);
      expect(router.url).toBe('/help/formats/pc_playtest');
    });

    it('holds on to a format just revealed while the panel scrolls to it, until the reader scrolls by hand', async () => {
      const { el, harness } = await open('/help/formats/pc_playtest');
      const router = TestBed.inject(Router);
      layOut(el, 0);
      await scroll(harness, el);
      expect(router.url).toBe('/help/formats/pc_playtest');
      expect(active(el)).toBe('PC Playtest');
      frame(el).dispatchEvent(new Event('wheel'));
      await scroll(harness, el);
      expect(router.url).toBe('/help/formats/pc_fotr_block');
      expect(active(el)).toBe('Fellowship Block (PC)');
    });

    it('lets go of a revealed format the filter hides', async () => {
      const { el, harness } = await open('/help/formats/pc_playtest');
      sections(el)[2]!.hidden = true;
      layOut(el, 0);
      await scroll(harness, el);
      expect(TestBed.inject(Router).url).toBe('/help/formats/pc_fotr_block');
    });
  });

  it('opens and closes the (i) text', async () => {
    const { el, harness } = await open('/help/formats');
    const toggle = el.querySelector<HTMLButtonElement>('.gd-formats__info')!;
    const help = el.querySelector<HTMLElement>('.gd-formats__help')!;
    expect([toggle.getAttribute('aria-expanded'), help.hidden]).toEqual(['false', true]);
    toggle.click();
    harness.detectChanges();
    expect([toggle.getAttribute('aria-expanded'), help.hidden]).toEqual(['true', false]);
    toggle.click();
    harness.detectChanges();
    expect(help.hidden).toBe(true);
  });

  it('says so when there are no formats', async () => {
    const { el } = await open('/help/formats', '{"formats":[]}');
    expect(el.querySelector('.gd-formats__main .gd-formats__status')?.textContent).toBe(
      'No formats are available right now.',
    );
  });

  it('reveals a format from its permalink, even when the address names it already', async () => {
    const { el, harness } = await open('/help/formats/rev_tow_sta');
    scrolled.length = 0;
    el.querySelectorAll<HTMLAnchorElement>('.gd-format__permalink')[1]!.click();
    await harness.fixture.whenStable();
    harness.detectChanges();
    expect(scrolled).toEqual(['format-rev_tow_sta']);
    expect(TestBed.inject(Router).url).toBe('/help/formats/rev_tow_sta');
  });
});
