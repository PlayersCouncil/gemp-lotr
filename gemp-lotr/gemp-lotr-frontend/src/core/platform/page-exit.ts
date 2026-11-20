import { DOCUMENT } from '@angular/common';
import { inject, Injectable } from '@angular/core';

/**
 * The page going away (closed, reloaded, left for another), and the little that can still be said to the server as it
 * goes: a POST the browser sends after the page has gone (navigator.sendBeacon). The browser, behind a service.
 */
@Injectable({ providedIn: 'root' })
export class PageExit {
  private readonly document = inject(DOCUMENT);

  /** Calls `leaving` when the page is hidden for good (pagehide); the function returned stops that. */
  onLeave(leaving: () => void): () => void {
    const view = this.document.defaultView;
    if (!view) return () => undefined;
    view.addEventListener('pagehide', leaving);
    return () => view.removeEventListener('pagehide', leaving);
  }

  /**
   * POSTs the fields form-encoded to `url` in a way that outlives the page. A courtesy: false when the browser can't
   * (or wouldn't), and nothing is heard of the answer.
   */
  beacon(url: string, fields: Readonly<Record<string, string>>): boolean {
    const navigator = this.document.defaultView?.navigator;
    if (!navigator || typeof navigator.sendBeacon !== 'function') return false;
    try {
      return navigator.sendBeacon(url, new URLSearchParams(fields));
    } catch {
      return false;
    }
  }
}
