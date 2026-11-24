import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GempHttp } from '../api/gemp-http.service';
import { ServerStatus, serverStatusLine } from './login-messages';

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
  login(login: string, password: string): Observable<void> {
    return this.http.postText('/login', { login, password }).pipe(map(() => undefined));
  }

  /** POST /register; the answer sets the loggedUser cookie. */
  register(login: string, password: string): Observable<void> {
    return this.http.postText('/register', { login, password }).pipe(map(() => undefined));
  }
}
