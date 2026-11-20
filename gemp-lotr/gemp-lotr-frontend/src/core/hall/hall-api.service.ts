import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiError, INVALID_RESPONSE_STATUS } from '../api/api-error';
import { GempHttp } from '../api/gemp-http.service';
import { withPollTimeout } from '../api/poll-timeout';
import { Timers } from '../platform/timers';
import { HallUpdate } from './hall-models';
import { parseHallUpdate } from './hall-parsers';

/**
 * The hall long-poll's two calls (HallRequestHandler), as communication.js made them: participantId added by
 * GempHttp, not cached, 20 s time-out (PollTimeoutError). Failed calls arrive as ApiError; an answer that is not a
 * <hall> as ApiError with INVALID_RESPONSE_STATUS.
 */
@Injectable()
export class HallApi {
  private readonly http = inject(GempHttp);
  private readonly timers = inject(Timers);

  /** getHall: GET /hall signs up for a new channel and answers with a full snapshot. */
  signUp(): Observable<HallUpdate> {
    return this.poll(this.http.getXml('/hall'), true);
  }

  /**
   * updateHall: POST /hall/update long-polls the channel; the server answers when the hall changes, or after about
   * 5 s with no changes. 409: the player's hall channel is another one (opened in another window); 410: the server
   * dropped the channel (no request for 5 minutes).
   */
  update(channelNumber: number): Observable<HallUpdate> {
    return this.poll(this.http.postXml('/hall/update', { channelNumber }), false);
  }

  private poll(call$: Observable<Document>, full: boolean): Observable<HallUpdate> {
    return withPollTimeout(call$, this.timers).pipe(
      map((doc) => {
        const update = parseHallUpdate(doc, full);
        if (!update) {
          throw new ApiError(INVALID_RESPONSE_STATUS, 'The hall answer is not a <hall>');
        }
        return update;
      }),
    );
  }
}
