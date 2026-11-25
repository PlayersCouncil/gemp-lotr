import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';
import { readCookie, writeCookie } from './cookies';

/** The legacy pages' cookie settings (see cookies.ts), for core services that remember choices the way they did. */
@Injectable({ providedIn: 'root' })
export class LegacyCookies {
  private readonly document = inject(DOCUMENT);

  read(name: string): string | null {
    return readCookie(this.document.cookie, name);
  }

  write(name: string, value: string): void {
    writeCookie(this.document, name, value);
  }
}
