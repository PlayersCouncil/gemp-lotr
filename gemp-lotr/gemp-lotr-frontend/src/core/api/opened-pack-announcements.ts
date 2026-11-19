import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

/** Tells whoever shows opened packs that a server response said their contents are ready to be fetched. */
@Injectable({ providedIn: 'root' })
export class OpenedPackAnnouncements {
  private readonly announced = new Subject<void>();

  readonly announced$ = this.announced.asObservable();

  announce(): void {
    this.announced.next();
  }
}
