import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GempHttp } from '../api/gemp-http.service';
import { parsePcSession, PcSession } from './pc-account';

/** Which log-out: `alias` ends the alias and keeps the PC account signed in; `all` ends everything. */
export type LogoutScope = 'alias' | 'all';

/**
 * The PC account's data calls (PC-ACCOUNT.md, "Data"), all under /gemp-lotr-server/argonauth, form-encoded. The
 * player comes from the session cookie and nothing else. A refusal is an ApiError whose
 * `said` is the server's code (`{"error": "..."}`).
 */
@Injectable({ providedIn: 'root' })
export class PcAccountApi {
  private readonly http = inject(GempHttp);

  /** GET /argonauth/session: who this browser is; always 200, in every mode. */
  session(): Observable<PcSession> {
    return this.http.getJson<unknown>('/argonauth/session').pipe(map(parsePcSession));
  }

  /** POST /argonauth/enter: plays as the alias. */
  enter(playerId: number): Observable<PcSession> {
    return this.post('/argonauth/enter', { playerId: String(playerId) });
  }

  /** POST /argonauth/create: a new alias, entered. */
  create(name: string): Observable<PcSession> {
    return this.post('/argonauth/create', { name });
  }

  /** POST /argonauth/link: adds an old account by its username and password; it is not entered. */
  link(login: string, password: string): Observable<PcSession> {
    return this.post('/argonauth/link', { login, password });
  }

  /**
   * POST /argonauth/connect: connects the account this browser is logged in to, by its password. The answer is where
   * to send the browser: the PC sign-in that links it.
   */
  connect(password: string): Observable<string> {
    return this.next('/argonauth/connect', { password });
  }

  /** POST /argonauth/logout: where to send the browser next. */
  logout(scope: LogoutScope): Observable<string> {
    return this.next('/argonauth/logout', { scope });
  }

  /** A POST answered `{"next": "<address>"}`: the address, or '' without one. */
  private next(path: string, data: Record<string, string>): Observable<string> {
    return this.http
      .postJson<{ next?: unknown } | null>(path, data)
      .pipe(map((answer) => (typeof answer?.next === 'string' ? answer.next : '')));
  }

  private post(path: string, data: Record<string, string>): Observable<PcSession> {
    return this.http.postJson<unknown>(path, data).pipe(map(parsePcSession));
  }
}
