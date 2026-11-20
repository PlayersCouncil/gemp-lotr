import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { ApiError } from '../api/api-error';
import { LoginPage } from '../platform/login-page.service';
import { PlayerInfo } from './player-info.service';

/**
 * Only a logged-in player gets past this: the server answers 401 to anyone else, who goes to the login page before the
 * page is loaded. Any other failure (a server restarting, a lost connection) says nothing about the login, so the page
 * opens and reports its own errors as usual.
 */
export const loginRequired: CanActivateFn = () => {
  const loginPage = inject(LoginPage);
  return inject(PlayerInfo)
    .load()
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

/**
 * The game page, which needs a login for a game but not for a replay: a replay link (`?replayId=`, which the page opens
 * instead of any game) works for anyone it is shared with, logged in or not, as on game.html; the server serves
 * replays without a login (the owner, 2026-11-20). Everything else on the page still needs one.
 */
export const loginRequiredUnlessReplay: CanActivateFn = (route, state) =>
  route.queryParamMap.has('replayId') ? true : loginRequired(route, state);
