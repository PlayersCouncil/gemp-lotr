import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

/** What a delivery header says is waiting on the server (communication.js deliveryCheck). */
export type DeliveryKind = 'package' | 'announcement' | 'league';

/** The response headers the server sets while something waits (LotroServerRequestHandler.processDeliveryServiceNotification). */
export const DELIVERY_HEADERS: Readonly<Record<DeliveryKind, string>> = {
  package: 'Delivery-Service-Package',
  announcement: 'Delivery-Service-Announcement',
  league: 'Delivery-Service-League',
};

/**
 * Tells whoever shows deliveries (the hall's "New items", announcement and Race to Mount Doom dialogs) that a server
 * response said one is waiting. The opened-pack header has its own announcer (OpenedPackAnnouncements).
 */
@Injectable({ providedIn: 'root' })
export class DeliveryAnnouncements {
  private readonly announced = new Subject<DeliveryKind>();

  readonly announced$: Observable<DeliveryKind> = this.announced.asObservable();

  announce(kind: DeliveryKind): void {
    this.announced.next(kind);
  }
}
