import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';
import { readCookie, writeCookie } from './cookies';

/** The deck builder's own settings cookies (kept for a year, path /gemp-lotr), for the parts of core off the platform. */
@Injectable({ providedIn: 'root' })
export class SettingCookies {
  private readonly document = inject(DOCUMENT);

  read(name: string): string | null {
    return readCookie(this.document.cookie, name);
  }

  /** Under /gemp-lotr, or under `path` when given (a cookie for the server). */
  write(name: string, value: string, path?: string): void {
    writeCookie(this.document, name, value, new Date(), path);
  }
}
