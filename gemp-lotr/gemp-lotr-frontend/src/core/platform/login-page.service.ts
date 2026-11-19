import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';
import { LOGIN_PAGE_URL } from './app-paths';

/** The site's login page, outside the app. */
@Injectable({ providedIn: 'root' })
export class LoginPage {
  private readonly window = inject(DOCUMENT).defaultView;

  /** Replaces the current page, so the browser's Back button doesn't return to one that would only send them here. */
  open(): void {
    this.window?.location.replace(LOGIN_PAGE_URL);
  }
}
