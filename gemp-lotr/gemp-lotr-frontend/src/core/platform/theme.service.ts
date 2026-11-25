import { PageCues } from './page-cues';
import { DOCUMENT } from '@angular/common';
import { computed, inject, Injectable, InjectionToken, signal } from '@angular/core';
import { readCookie, writeCookie } from './cookies';
import { PAGE_PATH, PAGE_QUERY_STRING } from './page-location';

/**
 * The deck builder's themes: each is a set of values (colours, fonts, borders, images) in src/styles/themes.css, chosen
 * by `data-theme` on <html>. The components' styles are the same in every theme.
 */
export const THEMES = ['classic', 'modern'] as const;
export type ThemeName = (typeof THEMES)[number];

/** What a theme decides about behaviour, not looks. */
export interface ThemeBehaviour {
  /** The card information blocks the page behind it. */
  cardInfoModal: boolean;
  /** Closing a dialog gives the focus back to what had it (the legacy page left it nowhere). */
  dialogsReturnFocus: boolean;
  /**
   * On a touch screen, the second tap of a double tap does nothing if it lands on another control than the first (a
   * menu or dialog the first tap closed or opened moved what is under the finger): SecondTapGuard.
   */
  guardSecondTap: boolean;
  /**
   * A card's menu of actions opened by a tap shows at the window's top-left corner, as game.html's does: its swipe
   * plugin hands the click handler the touch event, which has no place (the mouse's click opens it where clicked).
   */
  tapMenuInCorner: boolean;
}

export const THEME_BEHAVIOUR: Readonly<Record<ThemeName, ThemeBehaviour>> = {
  classic: { cardInfoModal: false, dialogsReturnFocus: false, guardSecondTap: false, tapMenuInCorner: true },
  modern: { cardInfoModal: true, dialogsReturnFocus: true, guardSecondTap: true, tapMenuInCorner: false },
};

/**
 * The page's `<meta name="viewport">` in each theme. Modern lays out at the device's width (its compact layout is
 * for phones and tablets); classic has none, as the legacy page: phones show it as a desktop page, zoomed out.
 */
export const THEME_VIEWPORT: Readonly<Record<ThemeName, string | null>> = {
  classic: null,
  modern: 'width=device-width, initial-scale=1',
};

/** The width phones lay a page with no viewport out at, unless the screen is wider (then the screen's). */
export const NO_VIEWPORT_WIDTH = 980;

/**
 * What a page with no viewport gets on a screen `screenWidth` px wide: the default width (or the screen's, if wider),
 * zoomed out to fit. Set when a theme with none follows one that had one: taking the viewport away doesn't give the
 * browser's default back (a phone keeps the device's width and zoom, and the classic layout runs off the screen).
 */
export function defaultViewport(screenWidth: number): string {
  const width = Math.max(NO_VIEWPORT_WIDTH, Math.round(screenWidth));
  // Exactly the zoom that fits it: a rounded one draws the page a fraction larger or smaller than one loaded so
  const scale = screenWidth / width;
  return `width=${width}, initial-scale=${scale}`;
}

/** Shown in the theme picker. */
export const THEME_LABELS: Readonly<Record<ThemeName, string>> = { classic: 'Classic', modern: 'Modern' };

/**
 * Whether players get the toolbar's theme picker, to switch between the themes at any time (the choice is remembered on
 * the device). On, as the owner decided (2026-11-10); the legacy page had no picker. Without it a theme is chosen only
 * with `?theme=` in the address.
 */
export const THEME_PICKER = new InjectionToken<boolean>('THEME_PICKER', { providedIn: 'root', factory: () => true });

/**
 * Whether the game page shows its theme button (G6 of trymarz/gemp-lotr-private#27): the deck builder's red button,
 * in the top right corner of the table. On, as the owner decided (2026-11-15); the legacy page had none, so the game
 * checks hide it from their screenshots. Without it the game's theme is chosen only with `?theme=`.
 */
export const GAME_THEME_PICKER = new InjectionToken<boolean>('GAME_THEME_PICKER', {
  providedIn: 'root',
  factory: () => true,
});

/** Every player lands on the modern look, on every device, until they choose another (owner, 2026-11-11). */
const DEFAULT_THEME: ThemeName = 'modern';

/**
 * Remembers the player's choice on this device (path /gemp-lotr, like the legacy settings cookies). The deck builder's
 * own cookie: the rest of the site's looks are chosen apart from it. Replaces `gempTheme`, which is no longer read, so
 * everyone starts on the modern look.
 */
export const THEME_COOKIE = 'gempDeckbuilderTheme';

/**
 * Pages with a theme choice of their own, by the last part of their address: its cookie and the theme without one. The
 * game page remembers its choice apart from the deck builder's (`gempGameTheme`, owner 2026-11-13); a player who
 * hasn't chosen gets the modern look (owner, 2026-11-16; it was classic until then), one who chose classic keeps it.
 */
export const PAGE_THEMES: Readonly<Record<string, PageTheme>> = {
  game: { cookie: 'gempGameTheme', fallback: 'modern', reloadForNoViewport: true },
};

export interface PageTheme {
  cookie: string;
  fallback: ThemeName;
  /**
   * The page has this look alone so far: whatever the address or the player's choice elsewhere says, it is shown in it,
   * and nothing is remembered.
   */
  only?: ThemeName;
  /**
   * Switching to a theme without a viewport tag (classic) loads the page again, when it has one: a phone gives a page
   * that has had a viewport tag no way back to having none (removing the tag leaves its layout), and imitating it
   * resets the player's zoom whenever the window changes size (the browser's bars sliding away). Opened again (with
   * `?theme=`, which init reads and takes out of the address), the page is exactly as game.html opened on a phone. For a
   * page that comes back as it was (the game: D10).
   */
  reloadForNoViewport?: boolean;
}

/**
 * The hall's pages (the hall with its tabs, and the login page): classic alone until the hall has a modern look of its
 * own. Ketura, 2026-11-20: for now the hall looks the same in both themes, as it does today, and gets its modern look
 * once the port works. Taking `only` away gives these pages a choice of their own (the cookie is already theirs).
 */
export const HALL_PAGE_THEME: PageTheme = { cookie: 'gempHallTheme', fallback: 'classic', only: 'classic' };

/**
 * The theme choice of the page at `path` (the browser's: "/gemp-lotr/ng/game"), by the part of the address that
 * names the page: null for the deck builder (the app's own choice, THEME_COOKIE), the game's, and the hall's for every
 * other page of the app (the hall is its start page, with a route per tab).
 */
export function pageThemeOf(path: string): PageTheme | null {
  const parts = path.split('/').filter(Boolean);
  const app = parts.indexOf('ng');
  if (app < 0 && parts.length === 0) {
    return null;
  }
  const page = parts[app + 1] ?? '';
  if (page === 'deckbuilder') {
    return null;
  }
  return PAGE_THEMES[page] ?? HALL_PAGE_THEME;
}

/** Set on <html>: it picks the theme's values in src/styles/themes.css. */
export const THEME_ATTRIBUTE = 'data-theme';

function isThemeName(value: unknown): value is ThemeName {
  return typeof value === 'string' && (THEMES as readonly string[]).includes(value);
}

/** The theme to use: the address (?theme=), then the remembered choice, then modern. Unknown names are ignored. */
export function resolveTheme(
  fromQuery: string | null,
  fromCookie: string | null,
  fallback: ThemeName = DEFAULT_THEME,
): ThemeName {
  if (isThemeName(fromQuery)) {
    return fromQuery;
  }
  if (isThemeName(fromCookie)) {
    return fromCookie;
  }
  return fallback;
}

/**
 * Chooses the theme, and the page's viewport for it (THEME_VIEWPORT). `init()` runs before the app starts (an app initializer), so the page is drawn in its theme from
 * the start; `select()` switches at runtime. Only the values change: the app's state (the deck being edited, the
 * collection page) stays as it is.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly queryString = inject(PAGE_QUERY_STRING);
  /** The page's own cookie and default, if it has them (PAGE_THEMES). */
  private readonly page = pageThemeOf(inject(PAGE_PATH));
  private readonly current = signal<ThemeName>(DEFAULT_THEME);
  private readonly cues = inject(PageCues);

  readonly theme = this.current.asReadonly();
  readonly behaviour = computed(() => THEME_BEHAVIOUR[this.current()]);
  /** Keeps the default viewport (defaultViewport) fitting the screen as the device turns, while it is set. */
  private followScreen: (() => void) | null = null;

  /** Resolves the theme and applies it. A `?theme=` in the address is remembered, as a choice. */
  init(): void {
    if (this.page?.only) {
      this.apply(this.page.only);
      return;
    }
    const fromQuery = new URLSearchParams(this.queryString).get('theme');
    if (isThemeName(fromQuery)) {
      this.remember(fromQuery);
      // Remembered: out of the address, so a later switch isn't undone by reloading it (the game opens itself anew)
      const window = this.document.defaultView;
      if (this.page?.reloadForNoViewport && window) {
        const address = new URL(window.location.href);
        address.searchParams.delete('theme');
        window.history.replaceState(window.history.state as unknown, '', address.toString());
      }
    }
    const cookie = this.page?.cookie ?? THEME_COOKIE;
    this.apply(resolveTheme(fromQuery, readCookie(this.document.cookie, cookie), this.page?.fallback));
  }

  /** The player's choice: remembered and applied (or, see PageTheme.reloadForNoViewport, the page opened again). */
  select(theme: string): void {
    if (!isThemeName(theme) || this.page?.only) {
      return;
    }
    this.remember(theme);
    const viewport = this.document.head.querySelector('meta[name="viewport"]');
    const location = this.document.defaultView?.location;
    if (THEME_VIEWPORT[theme] === null && viewport && this.page?.reloadForNoViewport && location) {
      // A new page, not a reload: a reload would bring back the zoom of the page that had a viewport tag
      const address = new URL(location.href);
      address.searchParams.set('theme', theme);
      this.cues.replace(address.toString());
      return;
    }
    this.apply(theme);
  }

  private apply(theme: ThemeName): void {
    this.document.documentElement.setAttribute(THEME_ATTRIBUTE, theme);
    this.setViewport(THEME_VIEWPORT[theme]);
    this.current.set(theme);
  }

  private setViewport(content: string | null): void {
    let meta = this.document.head.querySelector<HTMLMetaElement>('meta[name="viewport"]');
    const window = this.document.defaultView;
    if (this.followScreen) {
      window?.removeEventListener('resize', this.followScreen);
      this.followScreen = null;
    }
    if (content === null) {
      // A page loaded with none keeps none; one that had one gets what having none gives (defaultViewport)
      if (meta && window?.visualViewport) {
        const kept = meta;
        const fit = () => {
          // The screen's width in CSS px, whatever the zoom
          const viewport = window.visualViewport;
          if (viewport) {
            kept.content = defaultViewport(viewport.width * viewport.scale);
          }
        };
        fit();
        this.followScreen = fit;
        window.addEventListener('resize', fit);
      }
      return;
    }
    if (!meta) {
      meta = this.document.createElement('meta');
      meta.name = 'viewport';
      this.document.head.append(meta);
    }
    meta.content = content;
  }

  private remember(theme: ThemeName): void {
    writeCookie(this.document, this.page?.cookie ?? THEME_COOKIE, theme);
  }
}
