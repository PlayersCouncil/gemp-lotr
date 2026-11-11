import { TestBed } from '@angular/core/testing';
import { PAGE_QUERY_STRING } from './page-location';
import {
  isMobileDevice,
  resolveTheme,
  THEME_ATTRIBUTE,
  THEME_COOKIE,
  ThemeService,
  THEME_BEHAVIOUR,
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
    document.documentElement.removeAttribute(THEME_ATTRIBUTE);
    vi.restoreAllMocks();
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
    it('uses the classic theme on a computer, and remembers nothing', () => {
      const { written } = fakeCookies();
      const themes = service();
      themes.init();
      expect(themes.theme()).toBe('classic');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('classic');
      expect(written).toEqual([]);
    });

    it('uses the classic theme on a phone or tablet too, until the redesign', () => {
      fakeCookies();
      fakeDevice(true, 820, 1180);
      const themes = service();
      themes.init();
      expect(themes.theme()).toBe('classic');
    });

    it('prefers the remembered choice to the device', () => {
      fakeCookies({ [THEME_COOKIE]: 'classic' });
      fakeDevice(true, 390, 844);
      const themes = service();
      themes.init();
      expect(themes.theme()).toBe('classic');
    });

    it('prefers ?theme= to the cookie, and remembers it', () => {
      const { jar, written } = fakeCookies({ [THEME_COOKIE]: 'classic' });
      const themes = service('?participantId=bob&theme=modern');
      themes.init();
      expect(themes.theme()).toBe('modern');
      expect(jar.get(THEME_COOKIE)).toBe('modern');
      expect(written[0]).toMatch(new RegExp(`^${THEME_COOKIE}=modern; expires=.*; path=/gemp-lotr; SameSite=Lax$`));
    });

    it('ignores, and does not remember, an unknown ?theme= or cookie value', () => {
      const { written } = fakeCookies({ [THEME_COOKIE]: 'retro' });
      const themes = service('?theme=neon');
      themes.init();
      expect(themes.theme()).toBe('classic');
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
      themes.select('modern');
      expect(themes.theme()).toBe('modern');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('modern');
      expect(document.cookie).toContain(`${THEME_COOKIE}=modern`);
      themes.select('classic');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('classic');
    });

    it('ignores unknown names', () => {
      const themes = started();
      const before = document.cookie;
      themes.select('neon');
      expect(themes.theme()).toBe('classic');
      expect(document.documentElement.getAttribute(THEME_ATTRIBUTE)).toBe('classic');
      expect(document.cookie).toBe(before);
    });
  });

  it('says how each theme behaves', () => {
    expect(THEME_BEHAVIOUR.classic).toEqual({ cardInfoModal: false, dialogsReturnFocus: false });
    expect(THEME_BEHAVIOUR.modern).toEqual({ cardInfoModal: true, dialogsReturnFocus: true });
  });
});
