import { OpenedPackAnnouncements } from '../api/opened-pack-announcements';
import { DestroyRef, inject, Injectable } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { map, Observable, Subject } from 'rxjs';
import { GempHttp } from '../api/gemp-http.service';
import { PackDelivery } from '../api/models';
import { parsePackDelivery } from '../api/parsers';

/**
 * Opened-pack contents ("New items" popup), like the legacy deckbuilderDeliveryService.js.
 *
 * Fetching consumes the delivery on the server, so this service is deliberately created lazily: only the UI that
 * shows the popup (Phase 6) injects it. From then on it fetches whenever a response announces new contents, and the
 * UI should call fetch() once on start, as the legacy page did (forcePackDelivery).
 */
@Injectable()
export class PackDeliveryService {
  private readonly http = inject(GempHttp);
  private readonly deliveries = new Subject<PackDelivery[]>();

  /** Emits each non-empty delivery. */
  readonly deliveries$: Observable<PackDelivery[]> = this.deliveries.asObservable();

  constructor() {
    inject(OpenedPackAnnouncements)
      .announced$.pipe(takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(() => this.fetch());
  }

  fetch(): void {
    this.http
      .getXml('/delivery/packContents')
      .pipe(map(parsePackDelivery))
      .subscribe({
        next: (delivery) => {
          if (delivery.length > 0) {
            this.deliveries.next(delivery);
          }
        },
        // 404 = nothing to deliver; the legacy client ignored every failure here too
        error: () => undefined,
      });
  }
}
