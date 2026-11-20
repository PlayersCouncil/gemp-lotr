import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { GempHttp } from '../../api/gemp-http.service';
import { parseFormatDefinitions } from './format-definitions';
import { FormatDefinition } from './format-definitions-models';

/** communication.js getFormatDefinitions: GET /hall/formats/json (public; the participant goes along, as it did). */
@Injectable()
export class FormatDefinitionsApi {
  private readonly http = inject(GempHttp);

  formatDefinitions(): Observable<FormatDefinition[]> {
    return this.http.getJson<unknown>('/hall/formats/json').pipe(map(parseFormatDefinitions));
  }
}
