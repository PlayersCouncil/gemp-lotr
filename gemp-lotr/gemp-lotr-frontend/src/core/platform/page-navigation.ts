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

  /**
   * A tab opened at once, while a click is still a user gesture, to be pointed at a page once the server has answered
   * (PlayScenario.reserveWindow): a tab opened from the answer has no gesture behind it and pop-up blockers stop it.
   * Null when the browser gives none.
   */
  reserveTab(): ReservedTab | null {
    let opened: Window | null;
    try {
      opened = this.window?.open('', '_blank') ?? null;
    } catch {
      opened = null;
    }
    if (opened === null || opened.closed) {
      return null;
    }
    const tab = opened;
    try {
      tab.document.title = 'Gemp-LotR';
      tab.document.body.style.background = '#111';
    } catch {
      // only cosmetic
    }
    return {
      go: (url: string) => {
        if (tab.closed) {
          return false;
        }
        tab.location.href = url;
        try {
          tab.focus();
        } catch {
          // focusing is best effort
        }
        return true;
      },
      close: () => {
        try {
          tab.close();
        } catch {
          // already gone
        }
      },
    };
  }
}

/** A tab reserved by PageNavigation.reserveTab. */
export interface ReservedTab {
  /** Points the tab at `url` and focuses it; false when the player has closed it meanwhile. */
  go(url: string): boolean;
  close(): void;
}
