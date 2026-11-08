import { TestBed } from '@angular/core/testing';
import { PAGE_QUERY_STRING } from './page-location';
import {
  isMobileDevice,
  resolveTheme,
  THEME_ATTRIBUTE,
  THEME_COOKIE,
  THEME_LINK_ATTRIBUTE,
  THEME_LOAD_TIMEOUT_MS,
  THEME_VIEWPORT_ATTRIBUTE,
  THEME_VIEWPORTS,
  ThemeService,
} from './theme.service';

/** document.cookie, kept in memory: the tests' page isn't under /gemp-lotr, where the cookie is written. */
function fakeCookies(initial: Record<string, string> = {}) {
  const jar = new Map(Object.entries(initial));
  const written: string[] = [];
  vi.spyOn(document, 'cookie', 'get').mockImplementation(() =>
    [...jar].map(([name, value]) => `${name}=${value}`).join('; '),
  );
  vi.spyOn(document, 'cookie', 'set').mockImplementation((cookie: string) => {
    written.push(cookie);
    const [pair] = cookie.split(';');
    const separator = pair.indexOf('=');
    jar.set(pair.substring(0, separator), pair.substring(separator + 1));
  });
  return { jar, written };
}

/** A phone or tablet, or a computer, as far as isMobileDevice can tell. */
function fakeDevice(coarsePointer: boolean, width: number, height: number): void {
  vi.spyOn(window, 'matchMedia').mockImplementation(
    (query: string) => ({ matches: coarsePointer && query === '(pointer: coarse)' }) as MediaQueryList,
  );
  vi.spyOn(window.screen, 'width', 'get').mockReturnValue(width);
  vi.spyOn(window.screen, 'height', 'get').mockReturnValue(height);
}

const themeLinks = () => Array.from(document.head.querySelectorAll<HTMLLinkElement>(`link[${THEME_LINK_ATTRIBUTE}]`));
const loadLink = (theme: string, event = 'load') =>
  document.head.querySelector(`link[${THEME_LINK_ATTRIBUTE}="${theme}"]`)!.dispatchEvent(new Event(event));

/** Whether a promise has settled, without waiting for it. */
async function settled(promise: Promise<unknown>): Promise<boolean> {
  let done = false;
  void promise.then(() => (done = true));
  for (let i = 0; i < 5; i++) {
    await Promise.resolve();
  }
  return done;
}

describe('ThemeService', () => {
  function service(queryString = ''): ThemeService {
    TestBed.configureTestingModule({ providers: [{ provide: PAGE_QUERY_STRING, useValue: queryString }] });
    return TestBed.inject(ThemeService);
  }

  beforeAll(() => {
    // jsdom has no matchMedia; give it one to spy on
    window.matchMedia ??= (() => ({ matches: false })) as unknown as typeof window.matchMedia;
  });

  beforeEach(() => fakeDevice(false, 1920, 1080));

  afterEach(() => {
    themeLinks().forEach((link) => link.remove());
    document.head.querySelectorAll(`meta[${THEME_VIEWPORT_ATTRIBUTE}]`).forEach((meta) => meta.remove());
    document.head.querySelectorAll('base').forEach((base) => base.remove());
    document.documentElement.removeAttribute(THEME_ATTRIBUTE);
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  describe('resolveTheme', () => {
    it('takes the address first, then the cookie, then the device', () => {
      expect(resolveTheme('modern', 'classic', false)).toBe('modern');
      expect(resolveTheme(null, 'modern', false)).toBe('modern');
      expect(resolveTheme(null, 'classic', true)).toBe('classic');
      expect(resolveTheme(null, null, true)).toBe('classic');
      expect(resolveTheme(null, null, false)).toBe('classic');
    });

    it('ignores unknown names', () => {
      expect(resolveTheme('neon', 'modern', false)).toBe('modern');
      expect(resolveTheme('neon', 'retro', false)).toBe('classic');
      expect(resolveTheme('', 'MODERN', true)).toBe('classic');
      expect(resolveTheme('toString', '__proto__', false)).toBe('classic');
    });
  });

  describe('isMobileDevice (the rule of mobileView.js)', () => {
    it.each([
      [true, 390, 844, true],
      [true, 1024, 1366, true],
      [true, 1080, 1920, true],
      [true, 1200, 1920, false],
      [false, 390, 844, false],
      [false, 1920, 1080, false],
    ])('coarse pointer %s, screen %i x %i: %s', (coarse, width, height, mobile) => {
      fakeDevice(coarse, width, height);
      expect(isMobileDevice(window)).toBe(mobile);
    });

    it('is false without a window or matchMedia', () => {
      expect(isMobileDevice(null)).toBe(false);
      expect(isMobileDevice({ screen: { width: 390, height: 844 } } as unknown as Window)).toBe(false);
    });
  });

  describe('init', () => {
    it('uses the classic theme on a computer, and remembers nothing', async () => {
      const { written } = fakeCookies();
      const themes = service();
      const starting = themes.init();
      loadLink('classic');
      await starting;
      expect(themes.theme()).toBe('classic');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('classic');
      expect(written).toEqual([]);
    });

    it('uses the classic theme on a phone or tablet too, until the redesign', async () => {
      fakeCookies();
      fakeDevice(true, 820, 1180);
      const themes = service();
      const starting = themes.init();
      loadLink('classic');
      await starting;
      expect(themes.theme()).toBe('classic');
    });

    it('prefers the remembered choice to the device', async () => {
      fakeCookies({ [THEME_COOKIE]: 'classic' });
      fakeDevice(true, 390, 844);
      const themes = service();
      const starting = themes.init();
      loadLink('classic');
      await starting;
      expect(themes.theme()).toBe('classic');
    });

    it('prefers ?theme= to the cookie, and remembers it', async () => {
      const { jar, written } = fakeCookies({ [THEME_COOKIE]: 'classic' });
      const themes = service('?participantId=bob&theme=modern');
      const starting = themes.init();
      loadLink('modern');
      await starting;
      expect(themes.theme()).toBe('modern');
      expect(jar.get(THEME_COOKIE)).toBe('modern');
      expect(written[0]).toMatch(new RegExp(`^${THEME_COOKIE}=modern; expires=.*; path=/gemp-lotr$`));
    });

    it('ignores, and does not remember, an unknown ?theme= or cookie value', async () => {
      const { written } = fakeCookies({ [THEME_COOKIE]: 'retro' });
      const themes = service('?theme=neon');
      const starting = themes.init();
      loadLink('classic');
      await starting;
      expect(themes.theme()).toBe('classic');
      expect(written).toEqual([]);
    });

    it("waits for the theme's stylesheet before the page is drawn (no unstyled flash)", async () => {
      fakeCookies();
      const starting = service().init();
      expect(await settled(starting)).toBe(false);
      expect(document.documentElement.hasAttribute(THEME_ATTRIBUTE)).toBe(false);
      loadLink('classic');
      expect(await settled(starting)).toBe(true);
    });

    it('shows the page anyway when the stylesheet fails or never loads', async () => {
      fakeCookies();
      const failing = service().init();
      loadLink('classic', 'error');
      expect(await settled(failing)).toBe(true);

      themeLinks().forEach((link) => link.remove());
      document.head.querySelectorAll(`meta[${THEME_VIEWPORT_ATTRIBUTE}]`).forEach((meta) => meta.remove());
      TestBed.resetTestingModule();
      vi.useFakeTimers();
      const hanging = service().init();
      expect(await settled(hanging)).toBe(false);
      vi.advanceTimersByTime(THEME_LOAD_TIMEOUT_MS);
      expect(await settled(hanging)).toBe(true);
    });

    it("links the app's theme-<name>.css, relative to the <base href> it is served under", async () => {
      fakeCookies();
      const base = document.createElement('base');
      base.href = 'http://localhost/gemp-lotr/ng/';
      document.head.prepend(base);
      const starting = service().init();
      loadLink('classic');
      await starting;
      const [link] = themeLinks();
      expect(link.rel).toBe('stylesheet');
      expect(link.href).toBe('http://localhost/gemp-lotr/ng/theme-classic.css');
    });
  });

  describe('select', () => {
    async function started(): Promise<ThemeService> {
      fakeCookies();
      const themes = service();
      const starting = themes.init();
      loadLink('classic');
      await starting;
      return themes;
    }

    it('loads the new stylesheet before removing the old one, then remembers the choice', async () => {
      const themes = await started();
      const switching = themes.select('modern');
      expect(themeLinks().map((link) => link.getAttribute(THEME_LINK_ATTRIBUTE))).toEqual(['classic', 'modern']);
      expect(themes.theme()).toBe('classic');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('classic');
      loadLink('modern');
      await switching;
      expect(themeLinks().map((link) => link.getAttribute(THEME_LINK_ATTRIBUTE))).toEqual(['modern']);
      expect(themes.theme()).toBe('modern');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('modern');
      expect(document.cookie).toContain(`${THEME_COOKIE}=modern`);
    });

    it('lets the last choice win when an earlier one loads later', async () => {
      const themes = await started();
      const toModern = themes.select('modern');
      const backToClassic = themes.select('classic');
      loadLink('modern');
      await toModern;
      expect(themes.theme()).toBe('classic');
      document.head.querySelectorAll(`link[${THEME_LINK_ATTRIBUTE}="classic"]`).forEach((link) => {
        link.dispatchEvent(new Event('load'));
      });
      await backToClassic;
      expect(themeLinks().map((link) => link.getAttribute(THEME_LINK_ATTRIBUTE))).toEqual(['classic']);
      expect(themes.theme()).toBe('classic');
    });

    it('keeps the stylesheet when the theme is already in use', async () => {
      const themes = await started();
      const [link] = themeLinks();
      await themes.select('classic');
      expect(themeLinks()).toEqual([link]);
    });

    it("sets the theme's viewport: the device's width for modern, the default for classic", async () => {
      const viewports = () =>
        Array.from(document.head.querySelectorAll<HTMLMetaElement>('meta[name="viewport"]')).map((m) => m.content);
      const themes = await started();
      expect(viewports()).toEqual([]);
      const switching = themes.select('modern');
      loadLink('modern');
      await switching;
      expect(viewports()).toEqual([THEME_VIEWPORTS.modern]);
      const back = themes.select('classic');
      loadLink('classic');
      await back;
      expect(viewports()).toEqual([]);
    });

    it('ignores unknown names', async () => {
      const themes = await started();
      const before = document.cookie;
      await themes.select('neon');
      expect(themes.theme()).toBe('classic');
      expect(themeLinks()).toHaveLength(1);
      expect(document.cookie).toBe(before);
    });
  });
});
