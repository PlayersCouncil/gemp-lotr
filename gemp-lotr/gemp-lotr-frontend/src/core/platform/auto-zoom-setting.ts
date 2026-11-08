import { DOCUMENT } from '@angular/common';
import { inject, Injectable, signal } from '@angular/core';
import { readCookie, writeCookie } from './cookies';

/** Shared with the legacy page. */
export const AUTO_ZOOM_COOKIE = 'autoZoomInDeckbuilder';

/**
 * Whether hovering a card shows it large (the toolbar's magnifier), remembered in a cookie; an unset cookie means on.
 * Touch devices have no hover, so it is not offered there (legacy check: the document takes touch events).
 */
@Injectable({ providedIn: 'root' })
export class AutoZoomSetting {
  private readonly document = inject(DOCUMENT);

  readonly available = !('ontouchstart' in this.document.documentElement);
  readonly enabled = signal(readCookie(this.document.cookie, AUTO_ZOOM_COOKIE) !== 'false');

  toggle(): void {
    this.enabled.update((on) => !on);
    writeCookie(this.document, AUTO_ZOOM_COOKIE, `${this.enabled()}`);
  }
}
