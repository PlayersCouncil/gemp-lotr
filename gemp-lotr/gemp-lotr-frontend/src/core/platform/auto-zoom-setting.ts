import { DOCUMENT } from '@angular/common';
import { inject, Injectable, InjectionToken, signal } from '@angular/core';
import { readCookie, writeCookie } from './cookies';

/** The cookie the page's setting is kept in: each page has its own (see provideCardInteractions). */
export const AUTO_ZOOM_COOKIE_NAME = new InjectionToken<string>('AUTO_ZOOM_COOKIE_NAME');

/**
 * Whether hovering a card shows it large (the toolbar's magnifier), remembered in a cookie; an unset cookie means on.
 * Touch devices have no hover, so it is not offered there (legacy check: the document takes touch events).
 */
@Injectable()
export class AutoZoomSetting {
  private readonly document = inject(DOCUMENT);
  private readonly cookie = inject(AUTO_ZOOM_COOKIE_NAME);

  readonly available = !('ontouchstart' in this.document.documentElement);
  readonly enabled = signal(readCookie(this.document.cookie, this.cookie) !== 'false');

  toggle(): void {
    this.enabled.update((on) => !on);
    writeCookie(this.document, this.cookie, `${this.enabled()}`);
  }
}
