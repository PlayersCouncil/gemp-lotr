import { DOCUMENT } from '@angular/common';
import { inject, Injectable, signal } from '@angular/core';

/** The Game Hall's address. */
export const HALL_PAGE_URL = '/gemp-lotr/hall.html';

/** The channel the Game Hall listens on (hallUi.js), and how long it is given to answer (ms). */
const CHANNEL = 'gemp-lotr-hall';
const ANSWER_MS = 350;
/** A tab the browser won't let a page close is still there this long after closing (ms). */
const CLOSED_MS = 400;

/**
 * Going back to the Game Hall from a page it opened in a tab of its own. If the hall is still open in another tab,
 * this tab is closed rather than a second hall opened (the hall is told where to go, if anywhere); a tab the browser
 * won't close says so (`elsewhere`) and offers to open the hall here. With no hall about, the hall opens here.
 */
@Injectable({ providedIn: 'root' })
export class HallTab {
  private readonly document = inject(DOCUMENT);
  /** Set when the hall is open in another tab and this one couldn't be closed: the place in the hall asked for. */
  readonly elsewhere = signal<string | null>(null);

  /** `hash`: a place in the hall ("#account/scenarios"), or '' for wherever it is. */
  go(hash = ''): void {
    const view = this.document.defaultView;
    if (!view) return;
    if (typeof view.BroadcastChannel !== 'function') {
      this.openHere(hash);
      return;
    }
    const channel = new view.BroadcastChannel(CHANNEL);
    let answered = false;
    channel.onmessage = (event: MessageEvent) => {
      if (answered || (event.data as { type?: string } | null)?.type !== 'here') return;
      answered = true;
      if (hash !== '') channel.postMessage({ type: 'go', hash });
      channel.close();
      view.close();
      view.setTimeout(() => this.elsewhere.set(hash), CLOSED_MS);
    };
    channel.postMessage({ type: 'anyone' });
    view.setTimeout(() => {
      if (answered) return;
      channel.close();
      this.openHere(hash);
    }, ANSWER_MS);
  }

  /** The hall in this tab. */
  openHere(hash = ''): void {
    const location = this.document.defaultView?.location;
    if (location) location.href = HALL_PAGE_URL + hash;
  }
}
