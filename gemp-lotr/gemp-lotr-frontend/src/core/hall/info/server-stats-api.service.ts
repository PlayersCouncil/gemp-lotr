import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GempHttp } from '../../api/gemp-http.service';
import { parseServerStats } from './server-stats';
import { ServerStats, StatsPeriod } from './server-stats-models';

/** communication.js getStats: GET /stats (public; the participant goes along, as it did). A bad start day is a 400. */
@Injectable()
export class ServerStatsApi {
  private readonly http = inject(GempHttp);

  stats(startDay: string, period: StatsPeriod): Observable<ServerStats> {
    return this.http.getJson<unknown>('/stats', { startDay, length: period }).pipe(map(parseServerStats));
  }
}
