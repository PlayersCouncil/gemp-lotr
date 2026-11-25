import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GempHttp } from '../../api/gemp-http.service';
import { asTableCall } from '../tables/hall-call';
import { HallDeckSource, TableCallResult } from '../tables/hall-table-models';

/**
 * The queue and tournament calls of communication.js (HallRequestHandler /hall/queue/..., /hall/tournament/...). Every
 * one answers <error message> for a refusal (status 200), <response message> or nothing for a success; none fails
 * (an HTTP error becomes a refusal with TableFlow.errorMap's sentence).
 */
@Injectable()
export class HallQueuesApi {
  private readonly http = inject(GempHttp);

  /** POST /hall/queue/{id}: sign up; a Constructed queue takes a deck, the others "" (deckSource "" then). */
  joinQueue(queueId: string, deckName: string, deckSource: HallDeckSource | ''): Observable<TableCallResult> {
    return this.call(`/hall/queue/${encodeURIComponent(queueId)}`, { deckName, deckSource });
  }

  /** POST /hall/queue/{id}/leave (always OK). */
  leaveQueue(queueId: string): Observable<TableCallResult> {
    return this.call(`/hall/queue/${encodeURIComponent(queueId)}/leave`, {});
  }

  /** POST /hall/queue/{id}/start: start a player-made queue early. */
  startQueue(queueId: string): Observable<TableCallResult> {
    return this.call(`/hall/queue/${encodeURIComponent(queueId)}/start`, {});
  }

  /** POST /hall/queue/{id}/ready: confirm the ready check (always OK). */
  confirmReadyCheck(queueId: string): Observable<TableCallResult> {
    return this.call(`/hall/queue/${encodeURIComponent(queueId)}/ready`, {});
  }

  /** POST /hall/tournament/{id}/join: join a running tournament late (<response message> on success). */
  joinTournamentLate(id: string, deckName: string, deckSource: HallDeckSource | ''): Observable<TableCallResult> {
    return this.call(`/hall/tournament/${encodeURIComponent(id)}/join`, { deckName, deckSource });
  }

  /** POST /hall/tournament/{id}/registerdeck: the deck built for a limited tournament. */
  registerDeck(id: string, deckName: string, deckSource: HallDeckSource): Observable<TableCallResult> {
    return this.call(`/hall/tournament/${encodeURIComponent(id)}/registerdeck`, { deckName, deckSource });
  }

  /** POST /hall/tournament/{id}/leave: abandon a tournament. */
  dropFromTournament(id: string): Observable<TableCallResult> {
    return this.call(`/hall/tournament/${encodeURIComponent(id)}/leave`, {});
  }

  private call(path: string, data: Record<string, string>): Observable<TableCallResult> {
    return asTableCall(this.http.postText(path, data));
  }
}
