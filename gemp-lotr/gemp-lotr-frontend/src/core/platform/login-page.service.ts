import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';
import { isReturnPath, LOGIN_PAGE_URL } from './app-paths';

/** The login page, opened by a page that needs a logged-in player and has none. */
@Injectable({ providedIn: 'root' })
export class LoginPage {
  private readonly window = inject(DOCUMENT).defaultView;

  /**
   * Replaces the current page, so the browser's Back button doesn't return to one that would only send them here.
   * `returnTo`: the place in the app to come back to once logged in (the router's url); the hall without one.
   */
  open(returnTo: string | null = null): void {
    this.window?.location.replace(loginPageAddress(returnTo));
  }
}

/** The login page's address, naming the place to come back to if it is one in the app. */
export function loginPageAddress(returnTo: string | null = null): string {
  return returnTo !== null && isReturnPath(returnTo)
    ? `${LOGIN_PAGE_URL}?return=${encodeURIComponent(returnTo)}`
    : LOGIN_PAGE_URL;
}
