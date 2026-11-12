import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/**
 * Opens pages outside the app (shared deck lists, library deck pages) in a new tab. An address can come from the
 * server, so only web pages (http, https) are opened, and the new page gets no handle on this one.
 */
@Injectable({ providedIn: 'root' })
export class ExternalLinks {
  private readonly window = inject(DOCUMENT).defaultView;

  open(url: string): void {
    const window = this.window;
    if (!window) {
      return;
    }
    let address: URL;
    try {
      address = new URL(url.trim(), window.location.href);
    } catch {
      return;
    }
    if (address.protocol === 'http:' || address.protocol === 'https:') {
      window.open(address.href, '_blank', 'noopener');
    }
  }
}
