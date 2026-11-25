import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GempHttp } from '../../../api/gemp-http.service';
import { parseErrataCatalog } from './errata-catalog';
import { ErrataCatalog } from './errata-models';

/** communication.js getErrata: GET /hall/errata/json (public; the participant goes along, as it did). */
@Injectable()
export class ErrataApi {
  private readonly http = inject(GempHttp);

  errata(): Observable<ErrataCatalog> {
    return this.http.getJson<unknown>('/hall/errata/json').pipe(map(parseErrataCatalog));
  }
}
