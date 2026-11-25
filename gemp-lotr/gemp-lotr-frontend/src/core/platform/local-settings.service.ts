import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/**
 * Settings kept in the browser's localStorage, as the legacy hall kept them (e.g. "gemp.hallChat"). Storage can be
 * unavailable (private mode, blocked site data, the unit tests' DOM): reading then gives null and writing does
 * nothing, so the default applies.
 */
@Injectable({ providedIn: 'root' })
export class LocalSettings {
  private readonly window = inject(DOCUMENT).defaultView;

  read(key: string): string | null {
    try {
      return this.window?.localStorage.getItem(key) ?? null;
    } catch {
      return null;
    }
  }

  write(key: string, value: string): void {
    try {
      this.window?.localStorage.setItem(key, value);
    } catch {
      // not remembered; nothing else to do
    }
  }
}
