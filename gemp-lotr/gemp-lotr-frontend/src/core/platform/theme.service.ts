import { DOCUMENT } from '@angular/common';
import { computed, inject, Injectable, signal } from '@angular/core';
import { readCookie, writeCookie } from './cookies';
import { PAGE_QUERY_STRING } from './page-location';

/** The deck builder's themes: each is a stylesheet bundle (angular.json, `theme-<name>.css`) of src/themes/<name>/. */
export const THEMES = ['classic', 'modern'] as const;
export type ThemeName = (typeof THEMES)[number];

/** What a theme decides about behaviour, not looks. */
export interface ThemeBehaviour {
  /** The card information blocks the page behind it. */
  cardInfoModal: boolean;
  /** Closing a dialog gives the focus back to what had it (the legacy page left it nowhere). */
  dialogsReturnFocus: boolean;
}

export const THEME_BEHAVIOUR: Readonly<Record<ThemeName, ThemeBehaviour>> = {
  classic: { cardInfoModal: false, dialogsReturnFocus: false },
  modern: { cardInfoModal: true, dialogsReturnFocus: true },
};

/** Shown in the theme picker. */
export const THEME_LABELS: Readonly<Record<ThemeName, string>> = { classic: 'Classic', modern: 'Modern' };

/** Computers get the legacy look. */
const DEFAULT_THEME: ThemeName = 'classic';
/** Phones and tablets (see isMobileDevice): the legacy look too, until the redesign is approved. */
const MOBILE_THEME: ThemeName = 'classic';

/** Remembers the player's choice on this device (path /gemp-lotr, like the legacy settings cookies). */
export const THEME_COOKIE = 'gempTheme';

/** Set on <html>, so a theme's CSS can tell which theme is active if it needs to. */
export const THEME_ATTRIBUTE = 'data-theme';
/** Marks the theme's <link> (and only it), so a switch finds the one to replace. */
export const THEME_LINK_ATTRIBUTE = 'data-theme-stylesheet';

/**
 * The page's `<meta name="viewport">` per theme. The classic theme keeps the legacy page's default (none: phones lay it
 * out 980px wide and zoom out); the modern theme is laid out at the device's width, edge to edge (`viewport-fit=cover`,
 * so its CSS can keep clear of notches with the safe-area insets).
 */
export const THEME_VIEWPORTS: Readonly<Record<ThemeName, string | null>> = {
  classic: null,
  modern: 'width=device-width, initial-scale=1, viewport-fit=cover',
};
/** Marks the viewport <meta> the service added, so it only ever removes its own. */
export const THEME_VIEWPORT_ATTRIBUTE = 'data-theme-viewport';

/** How long the first paint waits for the theme stylesheet before showing the page anyway. */
export const THEME_LOAD_TIMEOUT_MS = 10_000;

function isThemeName(value: unknown): value is ThemeName {
  return typeof value === 'string' && (THEMES as readonly string[]).includes(value);
}

/**
 * A phone or tablet: a touch screen as the main input, and no side longer than a small laptop's. The same rule as
 * the legacy mobile pages (js/gemp-022/mobileView.js, GempView.isMobileDevice).
 */
export function isMobileDevice(window: Window | null): boolean {
  if (!window) {
    return false;
  }
  const coarse = typeof window.matchMedia === 'function' && window.matchMedia('(pointer: coarse)').matches;
  // Not every browser environment has a screen
  const screen = window.screen as Screen | undefined;
  const shortSide = Math.min(screen?.width ?? Infinity, screen?.height ?? Infinity);
  return coarse && shortSide < 1100;
}

/** The theme to use: the address (?theme=), then the remembered choice, then the device. Unknown names are ignored. */
export function resolveTheme(fromQuery: string | null, fromCookie: string | null, mobile: boolean): ThemeName {
  if (isThemeName(fromQuery)) {
    return fromQuery;
  }
  if (isThemeName(fromCookie)) {
    return fromCookie;
  }
  return mobile ? MOBILE_THEME : DEFAULT_THEME;
}

/**
 * Chooses the theme and loads its stylesheet. `init()` runs before the app starts (an app initializer), so nothing
 * is drawn before the theme's CSS has loaded. `select()` switches at runtime: the new stylesheet loads next to the
 * old one, which is removed only then, so the page never shows unstyled. Only the CSS changes: the app's state
 * (the deck being edited, the collection page) stays as it is.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly document = inject(DOCUMENT);
  private readonly queryString = inject(PAGE_QUERY_STRING);
  private readonly current = signal<ThemeName>(DEFAULT_THEME);
  /** The latest select(), so an earlier switch that finishes loading later doesn't win. */
  private requested: ThemeName | null = null;

  readonly theme = this.current.asReadonly();
  readonly behaviour = computed(() => THEME_BEHAVIOUR[this.current()]);

  /** Resolves the theme and waits for its stylesheet. A `?theme=` in the address is remembered, as a choice. */
  init(): Promise<void> {
    const fromQuery = new URLSearchParams(this.queryString).get('theme');
    if (isThemeName(fromQuery)) {
      this.remember(fromQuery);
    }
    const theme = resolveTheme(fromQuery, readCookie(this.document.cookie, THEME_COOKIE), this.mobileDevice());
    return this.apply(theme);
  }

  /** The player's choice: remembered, and applied once its stylesheet has loaded. Unknown names are ignored. */
  async select(theme: string): Promise<void> {
    if (!isThemeName(theme)) {
      return;
    }
    this.remember(theme);
    await this.apply(theme);
  }

  private async apply(theme: ThemeName): Promise<void> {
    this.requested = theme;
    const previous = this.document.head.querySelectorAll(`link[${THEME_LINK_ATTRIBUTE}]`);
    if (previous.length === 1 && previous[0]?.getAttribute(THEME_LINK_ATTRIBUTE) === theme) {
      this.show(theme);
      return;
    }
    const link = this.document.createElement('link');
    link.rel = 'stylesheet';
    link.href = this.stylesheetUrl(theme);
    link.setAttribute(THEME_LINK_ATTRIBUTE, theme);
    const loaded = this.whenLoaded(link);
    // After the last theme link, so it wins over it (and over the page's own stylesheets) while both are present
    const last = previous.length > 0 ? previous[previous.length - 1] : null;
    if (last) {
      last.after(link);
    } else {
      this.document.head.appendChild(link);
    }
    await loaded;
    if (this.requested !== theme) {
      // A later switch replaced this one while it loaded
      link.remove();
      return;
    }
    for (const old of Array.from(this.document.head.querySelectorAll(`link[${THEME_LINK_ATTRIBUTE}]`))) {
      if (old !== link) {
        old.remove();
      }
    }
    this.show(theme);
  }

  private show(theme: ThemeName): void {
    this.document.documentElement.setAttribute(THEME_ATTRIBUTE, theme);
    this.setViewport(THEME_VIEWPORTS[theme]);
    this.current.set(theme);
  }

  private setViewport(content: string | null): void {
    const head = this.document.head;
    let meta = head.querySelector<HTMLMetaElement>(`meta[${THEME_VIEWPORT_ATTRIBUTE}]`);
    if (content === null) {
      meta?.remove();
      return;
    }
    if (!meta) {
      meta = this.document.createElement('meta');
      meta.name = 'viewport';
      meta.setAttribute(THEME_VIEWPORT_ATTRIBUTE, '');
      head.appendChild(meta);
    }
    meta.content = content;
  }

  /** Next to the app's other files, whatever the address it is served under (`<base href>`). */
  private stylesheetUrl(theme: ThemeName): string {
    return new URL(`theme-${theme}.css`, this.document.baseURI).href;
  }

  /** Resolves on load, on error (the page then shows without the theme rather than not at all) or on timeout. */
  private whenLoaded(link: HTMLLinkElement): Promise<void> {
    return new Promise((resolve) => {
      const done = () => {
        clearTimeout(timer);
        resolve();
      };
      const timer = setTimeout(done, THEME_LOAD_TIMEOUT_MS);
      link.addEventListener('load', done, { once: true });
      link.addEventListener('error', done, { once: true });
    });
  }

  private remember(theme: ThemeName): void {
    writeCookie(this.document, THEME_COOKIE, theme);
  }

  private mobileDevice(): boolean {
    return isMobileDevice(this.document.defaultView);
  }
}
