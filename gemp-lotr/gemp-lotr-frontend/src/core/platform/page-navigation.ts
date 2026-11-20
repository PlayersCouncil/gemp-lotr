import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/** Leaves the app for another page in the same tab (e.g. the legacy hall after logging in), or opens one in a new tab. */
@Injectable({ providedIn: 'root' })
export class PageNavigation {
  private readonly window = inject(DOCUMENT).defaultView;

  go(url: string): void {
    this.window?.location.assign(url);
  }

  /**
   * Opens `url` in a new tab and focuses it (hallUi.js openOrOffer). False when the browser blocked it: a pop-up
   * opened from a poll answer has no click behind it, so blockers often stop it.
   */
  openInNewTab(url: string): boolean {
    let opened: Window | null;
    try {
      opened = this.window?.open(url, '_blank') ?? null;
    } catch {
      opened = null;
    }
    if (opened === null || opened.closed) {
      return false;
    }
    try {
      opened.focus();
    } catch {
      // focusing is best effort
    }
    return true;
  }
}
