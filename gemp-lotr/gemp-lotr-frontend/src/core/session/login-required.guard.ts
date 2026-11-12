import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { ApiError } from '../api/api-error';
import { GempHttp } from '../api/gemp-http.service';
import { LoginPage } from '../platform/login-page.service';

/**
 * Only a logged-in player gets past this: the server answers 401 to anyone else, who goes to the login page before the
 * page is loaded. Any other failure (a server restarting, a lost connection) says nothing about the login, so the page
 * opens and reports its own errors as usual.
 */
export const loginRequired: CanActivateFn = () => {
  const loginPage = inject(LoginPage);
  return inject(GempHttp)
    .getText('/player')
    .pipe(
      map(() => true),
      catchError((error: unknown) => {
        if (!ApiError.from(error).is(401)) {
          return of(true);
        }
        loginPage.open();
        return of(false);
      }),
    );
};
