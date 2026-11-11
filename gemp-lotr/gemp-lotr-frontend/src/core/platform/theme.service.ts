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
}

export const THEME_BEHAVIOUR: Readonly<Record<ThemeName, ThemeBehaviour>> = {
  classic: { cardInfoModal: false, dialogsReturnFocus: false },
  modern: { cardInfoModal: true, dialogsReturnFocus: true },
};

/** Shown in the theme picker. */
export const THEME_LABELS: Readonly<Record<ThemeName, string>> = { classic: 'Classic', modern: 'Modern' };

/**
 * Whether players get the toolbar's theme picker, to switch between the themes at any time (the choice is remembered on
 * the device). On, as the owner decided (2026-11-10); the legacy page had no picker. Without it a theme is chosen only
 * with `?theme=` in the address.
 */
export const THEME_PICKER = new InjectionToken<boolean>('THEME_PICKER', { providedIn: 'root', factory: () => true });

/** Computers get the legacy look. */
const DEFAULT_THEME: ThemeName = 'classic';
/** Phones and tablets (see isMobileDevice): the legacy look too, until the redesign is approved. */
const MOBILE_THEME: ThemeName = 'classic';

/** Remembers the player's choice on this device (path /gemp-lotr, like the legacy settings cookies). */
export const THEME_COOKIE = 'gempTheme';

/** Set on <html>: it picks the theme's values in src/styles/themes.css. */
export const THEME_ATTRIBUTE = 'data-theme';

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
 * Chooses the theme. `init()` runs before the app starts (an app initializer), so the page is drawn in its theme from
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

  /** Resolves the theme and applies it. A `?theme=` in the address is remembered, as a choice. */
  init(): void {
    const fromQuery = new URLSearchParams(this.queryString).get('theme');
    if (isThemeName(fromQuery)) {
      this.remember(fromQuery);
    }
    this.apply(resolveTheme(fromQuery, readCookie(this.document.cookie, THEME_COOKIE), this.mobileDevice()));
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
    this.current.set(theme);
  }

  private remember(theme: ThemeName): void {
    writeCookie(this.document, THEME_COOKIE, theme);
  }

  private mobileDevice(): boolean {
    return isMobileDevice(this.document.defaultView);
  }
}
