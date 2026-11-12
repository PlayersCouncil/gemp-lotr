import { TestBed } from '@angular/core/testing';
import { PAGE_QUERY_STRING } from './page-location';
import {
  resolveTheme,
  THEME_ATTRIBUTE,
  THEME_BEHAVIOUR,
  THEME_COOKIE,
  defaultViewport,
  THEME_VIEWPORT,
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

describe('ThemeService', () => {
  function service(queryString = ''): ThemeService {
    TestBed.configureTestingModule({ providers: [{ provide: PAGE_QUERY_STRING, useValue: queryString }] });
    return TestBed.inject(ThemeService);
  }

  afterEach(() => {
    document.documentElement.removeAttribute(THEME_ATTRIBUTE);
    vi.restoreAllMocks();
  });

  describe('resolveTheme', () => {
    it('takes the address first, then the cookie, then modern', () => {
      expect(resolveTheme('modern', 'classic')).toBe('modern');
      expect(resolveTheme('classic', 'modern')).toBe('classic');
      expect(resolveTheme(null, 'classic')).toBe('classic');
      expect(resolveTheme(null, null)).toBe('modern');
    });

    it('ignores unknown names', () => {
      expect(resolveTheme('neon', 'classic')).toBe('classic');
      expect(resolveTheme('neon', 'retro')).toBe('modern');
      expect(resolveTheme('', 'CLASSIC')).toBe('modern');
      expect(resolveTheme('toString', '__proto__')).toBe('modern');
    });
  });

  describe('init', () => {
    it('uses the modern theme, and remembers nothing', () => {
      const { written } = fakeCookies();
      const themes = service();
      themes.init();
      expect(themes.theme()).toBe('modern');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('modern');
      expect(written).toEqual([]);
    });

    it('uses the remembered choice', () => {
      fakeCookies({ [THEME_COOKIE]: 'classic' });
      const themes = service();
      themes.init();
      expect(themes.theme()).toBe('classic');
    });

    it("has its own cookie: the old gempTheme one isn't read", () => {
      expect(THEME_COOKIE).toBe('gempDeckbuilderTheme');
      fakeCookies({ gempTheme: 'classic' });
      const themes = service();
      themes.init();
      expect(themes.theme()).toBe('modern');
    });

    it('prefers ?theme= to the cookie, and remembers it', () => {
      const { jar, written } = fakeCookies({ [THEME_COOKIE]: 'modern' });
      const themes = service('?participantId=bob&theme=classic');
      themes.init();
      expect(themes.theme()).toBe('classic');
      expect(jar.get(THEME_COOKIE)).toBe('classic');
      expect(written[0]).toMatch(new RegExp(`^${THEME_COOKIE}=classic; expires=.*; path=/gemp-lotr; SameSite=Lax$`));
    });

    it('ignores, and does not remember, an unknown ?theme= or cookie value', () => {
      const { written } = fakeCookies({ [THEME_COOKIE]: 'retro' });
      const themes = service('?theme=neon');
      themes.init();
      expect(themes.theme()).toBe('modern');
      expect(written).toEqual([]);
    });
  });

  describe('select', () => {
    function started(): ThemeService {
      fakeCookies();
      const themes = service();
      themes.init();
      return themes;
    }

    it('switches the theme and remembers the choice', () => {
      const themes = started();
      themes.select('classic');
      expect(themes.theme()).toBe('classic');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('classic');
      expect(document.cookie).toContain(`${THEME_COOKIE}=classic`);
      themes.select('modern');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('modern');
    });

    it('ignores unknown names', () => {
      const themes = started();
      const before = document.cookie;
      themes.select('neon');
      expect(themes.theme()).toBe('modern');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('modern');
      expect(document.cookie).toBe(before);
    });
  });

  it('says how each theme behaves', () => {
    expect(THEME_BEHAVIOUR.classic).toEqual({
      cardInfoModal: false,
      dialogsReturnFocus: false,
      guardSecondTap: false,
    });
    expect(THEME_BEHAVIOUR.modern).toEqual({ cardInfoModal: true, dialogsReturnFocus: true, guardSecondTap: true });
  });

  it('lays modern out at the device width, and classic as the legacy page (no viewport, or its default after modern)', () => {
    const viewport = () => document.head.querySelector<HTMLMetaElement>('meta[name="viewport"]');
    vi.spyOn(document, 'cookie', 'set').mockImplementation(() => undefined);
    const themes = service();
    themes.init();
    expect(viewport()!.content).toBe('width=device-width, initial-scale=1');
    Object.defineProperty(window, 'visualViewport', { value: { width: 390, scale: 1 }, configurable: true });
    themes.select('classic');
    // A phone would keep the device's width if it were removed: the default a page without one gets instead
    expect(viewport()!.content).toBe(defaultViewport(390));
    // ...fitting the screen as the device turns
    Object.defineProperty(window, 'visualViewport', { value: { width: 844, scale: 1 }, configurable: true });
    window.dispatchEvent(new Event('resize'));
    expect(viewport()!.content).toBe(defaultViewport(844));
    themes.select('modern');
    window.dispatchEvent(new Event('resize'));
    expect(document.head.querySelectorAll('meta[name="viewport"]')).toHaveLength(1);
    expect(viewport()!.content).toBe(THEME_VIEWPORT.modern);
    viewport()!.remove();
    Reflect.deleteProperty(window, 'visualViewport');
  });

  it('gives a page that had a viewport what one without gets: 980px wide or the screen, zoomed out to fit', () => {
    expect(defaultViewport(390)).toBe(`width=980, initial-scale=${390 / 980}`);
    expect(defaultViewport(844)).toBe(`width=980, initial-scale=${844 / 980}`);
    expect(defaultViewport(1180)).toBe('width=1180, initial-scale=1');
  });

  it('opens classic with no viewport, as the legacy page', () => {
    vi.spyOn(document, 'cookie', 'set').mockImplementation(() => undefined);
    service('?theme=classic').init();
    expect(document.head.querySelector('meta[name="viewport"]')).toBeNull();
  });
});
