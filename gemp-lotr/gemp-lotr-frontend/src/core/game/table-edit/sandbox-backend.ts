import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GempHttp, RequestOptions } from '../../api/gemp-http.service';
import { EditBoard, readEditBoard } from './edit-board';
import { EditOp } from './edit-ops';
import { TableEditBackend } from './table-edit-backend';

const SANDBOX = '/scenarios/sandbox';
/** A sandbox is its player's own: the server takes no `participantId` there, so none is sent. */
const OWN: RequestOptions = { participant: false };

/**
 * A sandbox's table (ScenarioRequestHandler, `/scenarios/sandbox/*`): every change is made to the live game at once,
 * and the game's own events show it on the page. A refusal is a 400 with the server's words (ApiError.said). Page-scoped.
 */
@Injectable()
export class SandboxBackend extends TableEditBackend {
  readonly level = 'sandbox' as const;
  private readonly http = inject(GempHttp);

  state(gameId: string): Observable<EditBoard> {
    return this.http.getJson<unknown>(`${SANDBOX}/state`, { gameId }, OWN).pipe(map(readEditBoard));
  }

  apply(gameId: string, ops: readonly EditOp[]): Observable<EditBoard> {
    return this.changed('edit', { gameId, ops: JSON.stringify(ops) });
  }

  stop(gameId: string): Observable<EditBoard> {
    return this.changed('stop', { gameId });
  }

  start(gameId: string): Observable<EditBoard> {
    return this.changed('start', { gameId });
  }

  reset(gameId: string): Observable<string> {
    return this.http
      .postJson<{ gameId?: unknown }>(`${SANDBOX}/reset`, { gameId }, OWN)
      .pipe(map((answer) => String(answer.gameId)));
  }

  private changed(what: string, form: Record<string, string>): Observable<EditBoard> {
    return this.http
      .postJson<{ state?: unknown }>(`${SANDBOX}/${what}`, form, OWN)
      .pipe(map((answer) => readEditBoard(answer.state)));
  }
}
