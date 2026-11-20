import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GempHttp } from '../api/gemp-http.service';
import { ServerStatus, serverStatusLine } from './login-messages';

/** What a successful POST /login means: logged in (200), or the password was reset and must be set again (202). */
export type LoginOutcome = 'loggedIn' | 'passwordReset';

/** Same for POST /register: registered and logged in (200), or (202) a password reset (see README.md). */
export type RegisterOutcome = 'registered' | 'passwordUpdated';

/**
 * The login page's server calls (communication.js getStatus, login, register), form-encoded and with the session's
 * participantId like every legacy call. Failures are ApiErrors; their statuses are mapped in LoginService.
 */
@Injectable()
export class LoginApi {
  private readonly http = inject(GempHttp);

  /** GET /status (StatusRequestHandler): the hall's counts, worded as the legacy page's line. */
  serverStatus(): Observable<string> {
    return this.http
      .getJson<Partial<ServerStatus> | null>('/status')
      .pipe(map((status) => serverStatusLine(status ?? {})));
  }

  /** POST /login; the answer sets the loggedUser cookie. */
  login(login: string, password: string): Observable<LoginOutcome> {
    return this.http
      .postForStatus('/login', { login, password })
      .pipe(map(({ status }) => (status === 202 ? 'passwordReset' : 'loggedIn')));
  }

  /** POST /register (also sets a reset account's new password); the answer sets the loggedUser cookie. */
  register(login: string, password: string): Observable<RegisterOutcome> {
    return this.http
      .postForStatus('/register', { login, password })
      .pipe(map(({ status }) => (status === 202 ? 'passwordUpdated' : 'registered')));
  }
}
