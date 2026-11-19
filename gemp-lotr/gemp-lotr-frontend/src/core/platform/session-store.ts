import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/**
 * The browser tab's session storage, as JSON values: kept across reloads of the tab, gone when the tab closes. For
 * what only a page holds and must survive a reload (D10 of trymarz/gemp-lotr-private#27). Storage that is missing or
 * full (a private window, a browser setting) makes it forget quietly: the page then simply starts afresh.
 */
@Injectable({ providedIn: 'root' })
export class SessionStore {
  private readonly storage: Storage | null = (() => {
    try {
      return inject(DOCUMENT).defaultView?.sessionStorage ?? null;
    } catch {
      return null;
    }
  })();

  /** The value stored under the key, as it was written (the caller knows its shape); null if none. */
  read(key: string): unknown {
    try {
      const text = this.storage?.getItem(key);
      return text ? (JSON.parse(text) as unknown) : null;
    } catch {
      return null;
    }
  }

  write(key: string, value: unknown): void {
    try {
      this.storage?.setItem(key, JSON.stringify(value));
    } catch {
      // Full or blocked: the page just won't come back to this
    }
  }

  remove(key: string): void {
    try {
      this.storage?.removeItem(key);
    } catch {
      // as above
    }
  }
}
