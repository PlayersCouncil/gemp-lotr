import { DOCUMENT } from '@angular/common';
import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { hallAddressOfHash } from '../hall/hall-hash-routes';
import { APP_URL } from './app-paths';

/** The Game Hall's address: the Angular hall, at the app's root. */
export const HALL_PAGE_URL = `${APP_URL}/`;

/** The channel the Game Hall listens on (hallUi.js, HallChannel), and how long it is given to answer (ms). */
export const HALL_CHANNEL = 'gemp-lotr-hall';
const CHANNEL = HALL_CHANNEL;
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

  /** The hall in this tab, showing what the legacy hall link `hash` names (hallAddressOfHash; the hall if nothing). */
  openHere(hash = ''): void {
    const location = this.document.defaultView?.location;
    if (location) location.href = hallPageAddress(hash);
  }
}

/** The page address of what a legacy hall link's fragment names: the Angular hall's route, or the legacy hall's Admin. */
export function hallPageAddress(hash: string): string {
  const address = hallAddressOfHash(hash);
  if (address === null) return HALL_PAGE_URL;
  return 'page' in address ? address.page : APP_URL + address.route;
}

/** A place in the hall another tab asked for (`{type: 'go', hash}`): only hall links (hallUi.js's check). */
const GO_HASH = /^#[A-Za-z0-9/_-]+$/;

/**
 * The hall's side of the channel (hallUi.js, the BroadcastChannel "gemp-lotr-hall"): it answers a tab asking whether
 * a hall is open (`{type: 'anyone'}` → `{type: 'here'}`), so that tab closes instead of opening a second hall, and
 * passes on where that tab asked the hall to go (`{type: 'go', hash}`, a legacy hall link). Page-scoped: the hall's
 * page listens while it is open (listen()), and stops when its injector goes. Nothing in a browser without channels.
 */
@Injectable()
export class HallChannel {
  private readonly view = inject(DOCUMENT).defaultView;
  private channel: BroadcastChannel | null = null;
  private readonly goes = new Subject<string>();
  /** The legacy hall links other tabs asked the hall to show ("#account/scenarios"). */
  readonly goes$: Observable<string> = this.goes.asObservable();

  constructor() {
    inject(DestroyRef).onDestroy(() => this.close());
  }

  listen(): void {
    if (this.channel !== null || typeof this.view?.BroadcastChannel !== 'function') return;
    const channel = new this.view.BroadcastChannel(CHANNEL);
    channel.onmessage = (event: MessageEvent) => {
      const data = event.data as { type?: unknown; hash?: unknown } | null;
      if (data?.type === 'anyone') channel.postMessage({ type: 'here' });
      if (data?.type === 'go' && typeof data.hash === 'string' && GO_HASH.test(data.hash)) this.goes.next(data.hash);
    };
    this.channel = channel;
  }

  close(): void {
    this.channel?.close();
    this.channel = null;
  }
}

/** The message telling every other tab that this browser's session changed (an alias entered, a log-out). */
export const SESSION_CHANGED = 'session-changed';

/**
 * "The session changed", between the app's tabs, on the hall's channel: after an enter, a create that enters, or a
 * log-out, the tab that did it announces it (`{type: 'session-changed'}`), and every other tab of the app reloads, so
 * none goes on acting as the old alias (one that finds itself logged out then goes to the login page, as any page
 * does). One channel for both, so a tab doesn't hear its own announcement. The app's root listens (app.config.ts);
 * the legacy pages (hall-legacy.html, game.html) don't. Nothing in a browser without channels.
 */
@Injectable({ providedIn: 'root' })
export class SessionChannel {
  private readonly view = inject(DOCUMENT).defaultView;
  private channel: BroadcastChannel | null = null;
  private listening = false;

  /** Every other tab: reload. */
  announce(): void {
    this.open()?.postMessage({ type: SESSION_CHANGED });
  }

  /** This tab reloads when another announces a change. */
  reloadOnChange(): void {
    const channel = this.open();
    if (channel === null || this.listening) return;
    this.listening = true;
    channel.onmessage = (event: MessageEvent) => {
      if ((event.data as { type?: unknown } | null)?.type === SESSION_CHANGED) this.view?.location.reload();
    };
  }

  private open(): BroadcastChannel | null {
    if (this.channel === null && typeof this.view?.BroadcastChannel === 'function') {
      this.channel = new this.view.BroadcastChannel(CHANNEL);
    }
    return this.channel;
  }
}
