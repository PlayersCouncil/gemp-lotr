import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/** Opens pages outside the app (shared deck lists, library deck pages) in a new tab. */
@Injectable({ providedIn: 'root' })
export class ExternalLinks {
  private readonly window = inject(DOCUMENT).defaultView;

  open(url: string): void {
    this.window?.open(url, '_blank');
  }
}
