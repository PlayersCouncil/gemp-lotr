import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, Observable } from 'rxjs';
import { ApiError } from '../api/api-error';
import { loginTarget, returnPath } from '../login/return-link';
import { LOGIN_PAGE_URL } from '../platform/app-paths';
import { SessionChannel } from '../platform/hall-tab.service';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { PageNavigation } from '../platform/page-navigation';
import { Alias, NO_PC_SESSION, PcSession, PcSessionKind, pcSignInAddress, sessionKind } from './pc-account';
import { LogoutScope, PcAccountApi } from './pc-account-api.service';
import { PcAction, refusalCode, refusalMessage } from './pc-account-messages';

/** Why an action was refused: the server's code (or the likeliest, see refusalCode) and its wording. */
export interface Refusal {
  readonly code: string | null;
  readonly message: string;
}

/**
 * The PC account's state, shared by the login page, My Account and the hall (app-wide, like PlayerInfo): the session
 * (GET /argonauth/session, asked once per page and again after anything that changes it) and what its actions do.
 *
 * - Entering an alias, or creating one (which enters it), tells the other tabs (SessionChannel) and leaves for the
 *   page's `?return=` place, else the hall.
 * - Linking keeps the page: the answer's session (the list with the linked alias) replaces the one shown.
 * - A refusal is given back for the page to show beside what it concerns, and the session is asked for again (an alias
 *   may have been unlinked, or another become busy). `401 reauth` instead sends the browser through a fresh PC
 *   sign-in, which comes back with that form open (`?open=create` or `link`).
 * - Logging out tells the other tabs and goes where the server says (`next`), else the login page.
 * - A session the server can't give (an older server, a failure) is `test` mode with nobody: the pages then are as
 *   they always were.
 */
@Injectable({ providedIn: 'root' })
export class PcSessionService {
  private readonly api = inject(PcAccountApi);
  private readonly navigation = inject(PageNavigation);
  private readonly channel = inject(SessionChannel);
  private readonly queryString = inject(PAGE_QUERY_STRING);
  private request: Promise<PcSession> | null = null;
  private readonly session_ = signal<PcSession | null>(null);
  private readonly strayRefusal_ = signal<string | null>(null);

  /**
   * A refusal whose page part the session asked for again took away (the alias list or the Connect form gone, e.g.
   * the PC sign-in ended): its wording, for the page to show at its own level; null again at the next action.
   */
  readonly strayRefusal = this.strayRefusal_.asReadonly();

  /** The session, or null until the server has answered. */
  readonly session = this.session_.asReadonly();
  readonly kind = computed(() => {
    const session = this.session_();
    return session === null ? null : sessionKind(session);
  });

  /** Asks for the session once per page; later calls share the answer. */
  load(): Promise<PcSession> {
    this.request ??= this.ask();
    return this.request;
  }

  /** Asks for the session again. */
  refresh(): Promise<PcSession> {
    this.request = this.ask();
    return this.request;
  }

  /** Enter Gemp on an alias of the PC account. Null once on the way; else why not. */
  enter(alias: Alias): Promise<Refusal | null> {
    return this.act('enter', this.api.enter(alias.id), () => this.leaveForTarget());
  }

  /** Create New Gemp Profile: the new alias is entered. */
  create(name: string): Promise<Refusal | null> {
    return this.act('create', this.api.create(name), () => this.leaveForTarget());
  }

  /** Link Existing Gemp Account: the linked alias joins the list and is not entered. */
  link(login: string, password: string): Promise<Refusal | null> {
    return this.act('link', this.api.link(login, password), () => undefined);
  }

  /**
   * Connect PC Account, for a password login not linked: the account's password, then (a full navigation) the PC
   * sign-in the server names, which comes back to My Account (`?pc=`). A refusal is given back; `not-allowed` means
   * this browser is no password login any more, and the session asked for again shows what it is.
   */
  async connect(password: string): Promise<Refusal | null> {
    const before = this.acting();
    try {
      const next = await firstValueFrom(this.api.connect(password));
      if (next === '') throw new Error('No address to go to');
      this.navigation.go(next);
      return null;
    } catch (error) {
      const failure = ApiError.from(error);
      const code = refusalCode('connect', failure.status, failure.said);
      return this.refused(before, { code, message: refusalMessage(code, 'connect') });
    }
  }

  /** Enter Gemp for a password login: already logged in as it, so straight to the page. */
  enterLoggedIn(): void {
    this.navigation.go(loginTarget(this.queryString));
  }

  /** Log Out: `alias` from the hall, `all` from the alias list (and for a password login). */
  async logout(scope: LogoutScope): Promise<void> {
    this.acting();
    let next = '';
    try {
      next = await firstValueFrom(this.api.logout(scope));
    } catch {
      // The server ends the session whatever happens to its answer; the login page asks again
    }
    this.channel.announce();
    this.navigation.go(next || LOGIN_PAGE_URL);
  }

  /** A fresh PC sign-in for `then` (create or link), coming back with that form open. */
  signInFor(then: 'create' | 'link'): void {
    this.navigation.go(pcSignInAddress(then, returnPath(this.queryString)));
  }

  private async act(
    action: Exclude<PcAction, 'connect'>,
    call: Observable<PcSession>,
    done: () => void,
  ): Promise<Refusal | null> {
    const before = this.acting();
    try {
      this.show(await firstValueFrom(call));
      done();
      return null;
    } catch (error) {
      const failure = ApiError.from(error);
      if (failure.said === 'reauth' && action !== 'enter') {
        this.signInFor(action);
        return null;
      }
      const code = refusalCode(action, failure.status, failure.said);
      return this.refused(before, { code, message: refusalMessage(code, action) });
    }
  }

  /** An action begins: the last stray refusal goes. Gives who the browser is now, to tell whether that changes. */
  private acting(): PcSessionKind | null {
    this.strayRefusal_.set(null);
    return this.kind();
  }

  /**
   * After a refusal the session is asked for again (an alias may be unlinked, another busy, the sign-in ended). If
   * that makes the browser someone else, the part that would show the refusal is gone with it: it is kept as stray.
   */
  private refused(before: PcSessionKind | null, refusal: Refusal): Refusal {
    void this.refresh().then((session) => {
      if (sessionKind(session) !== before) this.strayRefusal_.set(refusal.message);
    });
    return refusal;
  }

  private leaveForTarget(): void {
    this.channel.announce();
    this.navigation.go(loginTarget(this.queryString));
  }

  private show(session: PcSession): void {
    this.session_.set(session);
    this.request = Promise.resolve(session);
  }

  /** Never fails: without an answer the session stays as it was, or is `test` mode with nobody (an older server). */
  private async ask(): Promise<PcSession> {
    let session: PcSession;
    try {
      session = await firstValueFrom(this.api.session());
    } catch {
      session = this.session_() ?? NO_PC_SESSION;
    }
    this.session_.set(session);
    return session;
  }
}
