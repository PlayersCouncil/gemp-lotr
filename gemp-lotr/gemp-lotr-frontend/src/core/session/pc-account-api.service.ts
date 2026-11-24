import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GempHttp, RequestOptions } from '../api/gemp-http.service';
import { parsePcSession, PcSession } from './pc-account';

/** Which log-out: `alias` ends the alias and keeps the PC account signed in; `all` ends everything. */
export type LogoutScope = 'alias' | 'all';

/**
 * The PC account's data calls (PC-ACCOUNT.md, "Data"), all under /gemp-lotr-server/argonauth, form-encoded. The
 * player comes from the session cookie and nothing else, so no participantId is sent. A refusal is an ApiError whose
 * `said` is the server's code (`{"error": "..."}`).
 */
@Injectable({ providedIn: 'root' })
export class PcAccountApi {
  private readonly http = inject(GempHttp);
  private readonly options: RequestOptions = { participant: false };

  /** GET /argonauth/session: who this browser is; always 200, in every mode. */
  session(): Observable<PcSession> {
    return this.http.getJson<unknown>('/argonauth/session', {}, this.options).pipe(map(parsePcSession));
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

  /** POST /argonauth/logout: where to send the browser next. */
  logout(scope: LogoutScope): Observable<string> {
    return this.http
      .postJson<{ next?: unknown } | null>('/argonauth/logout', { scope }, this.options)
      .pipe(map((answer) => (typeof answer?.next === 'string' ? answer.next : '')));
  }

  private post(path: string, data: Record<string, string>): Observable<PcSession> {
    return this.http.postJson<unknown>(path, data, this.options).pipe(map(parsePcSession));
  }
}
