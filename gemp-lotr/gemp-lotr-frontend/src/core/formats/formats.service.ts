import { inject, Injectable } from '@angular/core';
import { map, Observable, shareReplay } from 'rxjs';
import { DeckbuilderApi } from '../api/deckbuilder-api.service';
import { GempFormat } from '../api/models';

/** The formats from POST /deck/formats, fetched once and shared. */
@Injectable()
export class FormatsService {
  private readonly api = inject(DeckbuilderApi);

  readonly formats$: Observable<Record<string, GempFormat>> = this.api.getFormats().pipe(
    map((readout) => readout.Formats),
    shareReplay(1),
  );
}
