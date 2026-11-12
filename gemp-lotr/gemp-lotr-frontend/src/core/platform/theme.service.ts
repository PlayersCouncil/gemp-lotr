import { DOCUMENT } from '@angular/common';
import { computed, inject, Injectable, InjectionToken, signal } from '@angular/core';
import { readCookie, writeCookie } from './cookies';
import { PAGE_QUERY_STRING } from './page-location';

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
}

export const THEME_BEHAVIOUR: Readonly<Record<ThemeName, ThemeBehaviour>> = {
  classic: { cardInfoModal: false, dialogsReturnFocus: false, guardSecondTap: false },
  modern: { cardInfoModal: true, dialogsReturnFocus: true, guardSecondTap: true },
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

/** Every player lands on the modern look, on every device, until they choose another (owner, 2026-11-11). */
const DEFAULT_THEME: ThemeName = 'modern';

/**
 * Remembers the player's choice on this device (path /gemp-lotr, like the legacy settings cookies). The deck builder's
 * own cookie: the rest of the site's looks are chosen apart from it. Replaces `gempTheme`, which is no longer read, so
 * everyone starts on the modern look.
 */
export const THEME_COOKIE = 'gempDeckbuilderTheme';

/** Set on <html>: it picks the theme's values in src/styles/themes.css. */
export const THEME_ATTRIBUTE = 'data-theme';

function isThemeName(value: unknown): value is ThemeName {
  return typeof value === 'string' && (THEMES as readonly string[]).includes(value);
}

/** The theme to use: the address (?theme=), then the remembered choice, then modern. Unknown names are ignored. */
export function resolveTheme(fromQuery: string | null, fromCookie: string | null): ThemeName {
  if (isThemeName(fromQuery)) {
    return fromQuery;
  }
  if (isThemeName(fromCookie)) {
    return fromCookie;
  }
  return DEFAULT_THEME;
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
  private readonly current = signal<ThemeName>(DEFAULT_THEME);

  readonly theme = this.current.asReadonly();
  readonly behaviour = computed(() => THEME_BEHAVIOUR[this.current()]);
  /** Keeps the default viewport (defaultViewport) fitting the screen as the device turns, while it is set. */
  private followScreen: (() => void) | null = null;

  /** Resolves the theme and applies it. A `?theme=` in the address is remembered, as a choice. */
  init(): void {
    const fromQuery = new URLSearchParams(this.queryString).get('theme');
    if (isThemeName(fromQuery)) {
      this.remember(fromQuery);
    }
    this.apply(resolveTheme(fromQuery, readCookie(this.document.cookie, THEME_COOKIE)));
  }

  /** The player's choice: remembered and applied. Unknown names are ignored. */
  select(theme: string): void {
    if (!isThemeName(theme)) {
      return;
    }
    this.remember(theme);
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
    writeCookie(this.document, THEME_COOKIE, theme);
  }
}
