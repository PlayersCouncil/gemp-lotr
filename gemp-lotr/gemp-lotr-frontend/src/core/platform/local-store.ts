import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/**
 * The browser's local storage, as JSON values: kept on this device for this viewer, across visits. For a viewer's
 * conveniences only (the cards they used lately, the search's last filters): storage that is missing, blocked or full
 * (a private window, a browser setting) makes it forget quietly, and the page works without it.
 */
@Injectable({ providedIn: 'root' })
export class LocalStore {
  private readonly storage: Storage | null = (() => {
    try {
      return inject(DOCUMENT).defaultView?.localStorage ?? null;
    } catch {
      return null;
    }
  })();

  /** The value stored under the key, as it was written (the caller checks its shape); null if none. */
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
      // Full or blocked: it will just be forgotten
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
