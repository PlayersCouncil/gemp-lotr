import { computed, inject, Injectable, signal } from '@angular/core';
import { PAGE_QUERY_STRING } from '../platform/page-location';
import { Alias, canEnter, isAliasName, openedForm } from './pc-account';
import { INVALID_ALIAS_NAME } from './pc-account-messages';
import { PcSessionService } from './pc-session.service';

/** The aliases shown at once. */
export const ALIAS_PAGE_SIZE = 10;

/** The form open under the list. */
export type AliasForm = 'create' | 'link';

/**
 * One alias list's state (the login page's and My Account's, each its own: provided by the component): the page of
 * aliases shown, the create or link form and its fields, and each refusal beside what it concerns (an alias's Enter
 * Gemp, a form, Log Out). The session and the calls are PcSessionService's.
 *
 * Create and Link open their form at once with a fresh PC sign-in; with a lapsed one (`pcAccount.fresh` false) they
 * go through a sign-in first, which comes back to the login page with `?open=create` or `?open=link`: that form is then
 * open. A new alias's name is checked here first (Gemp's rule), so a name the server would refuse isn't sent.
 */
@Injectable()
export class AliasList {
  private readonly pc = inject(PcSessionService);
  private readonly opened = openedForm(inject(PAGE_QUERY_STRING));

  private readonly page_ = signal(0);
  private readonly form_ = signal<AliasForm | null>(null);
  private readonly name_ = signal('');
  private readonly login_ = signal('');
  private readonly password_ = signal('');
  private readonly aliasError_ = signal<{ id: number; message: string } | null>(null);
  private readonly formError_ = signal<string | null>(null);
  private readonly busy_ = signal(false);

  readonly session = this.pc.session;
  readonly kind = this.pc.kind;
  readonly form = this.form_.asReadonly();
  readonly name = this.name_.asReadonly();
  readonly login = this.login_.asReadonly();
  readonly password = this.password_.asReadonly();
  /** The refusal of the last Enter Gemp, beside that alias. */
  readonly aliasError = this.aliasError_.asReadonly();
  /**
   * The refusal of the last Enter Gemp when its alias is no longer listed (unlinked at the account service: the
   * session asked for again lost it), for the list to show at its own level; null at the next action.
   */
  readonly listError = computed(() => {
    const error = this.aliasError_();
    return error !== null && !this.aliases().some((alias) => alias.id === error.id) ? error.message : null;
  });
  /** The refusal of the open form's last submit, or its name check. */
  readonly formError = this.formError_.asReadonly();
  /** A call is under way: the list's buttons wait. */
  readonly busy = this.busy_.asReadonly();

  readonly aliases = computed(() => this.session()?.aliases ?? []);
  readonly blockedBy = computed(() => this.session()?.blockedBy ?? null);
  readonly pages = computed(() => Math.max(1, Math.ceil(this.aliases().length / ALIAS_PAGE_SIZE)));
  /** The page shown, from 0, within the pages there are. */
  readonly page = computed(() => Math.min(this.page_(), this.pages() - 1));
  readonly shown = computed(() =>
    this.aliases().slice(this.page() * ALIAS_PAGE_SIZE, (this.page() + 1) * ALIAS_PAGE_SIZE),
  );

  /** Asks for the session (once per page), then opens the form a sign-in came back for. */
  async start(): Promise<void> {
    const session = await this.pc.load();
    if (this.opened !== null && session.pcAccount !== null) {
      this.showForm(this.opened);
    }
  }

  canEnter(alias: Alias): boolean {
    const session = this.session();
    return session !== null && canEnter(session, alias);
  }

  previousPage(): void {
    this.page_.set(Math.max(0, this.page() - 1));
  }

  nextPage(): void {
    this.page_.set(Math.min(this.pages() - 1, this.page() + 1));
  }

  async enter(alias: Alias): Promise<void> {
    if (this.busy_() || !this.canEnter(alias)) return;
    this.aliasError_.set(null);
    const refusal = await this.during(() => this.pc.enter(alias));
    if (refusal) this.aliasError_.set({ id: alias.id, message: refusal.message });
  }

  /**
   * Create New Gemp Profile (Link Existing Gemp Account): its row at the end of the list (the other's, if open, goes),
   * after a sign-in if the last one has lapsed.
   */
  open(form: AliasForm): void {
    this.aliasError_.set(null);
    if (form === 'create' && this.blockedBy() !== null) return;
    if (this.session()?.pcAccount?.fresh === false) {
      this.pc.signInFor(form);
      return;
    }
    this.showForm(form);
    this.formError_.set(null);
  }

  /** The row being added sits at the list's end: its last page is shown. */
  private showForm(form: AliasForm): void {
    this.form_.set(form);
    this.page_.set(this.pages() - 1);
  }

  cancel(): void {
    this.aliasError_.set(null);
    this.form_.set(null);
    this.formError_.set(null);
    this.name_.set('');
    this.login_.set('');
    this.password_.set('');
  }

  setName(value: string): void {
    this.name_.set(value);
  }

  setLogin(value: string): void {
    this.login_.set(value);
  }

  setPassword(value: string): void {
    this.password_.set(value);
  }

  /** The open form's submit. */
  submit(): Promise<void> {
    this.aliasError_.set(null);
    return this.form_() === 'link' ? this.link() : this.create();
  }

  /** Log Out from the list: everything, the PC account's sign-in included. */
  async logout(): Promise<void> {
    if (this.busy_()) return;
    this.aliasError_.set(null);
    await this.during(() => this.pc.logout('all'));
  }

  /** Enter Gemp for a password login's one account. */
  enterLoggedIn(): void {
    this.pc.enterLoggedIn();
  }

  private async create(): Promise<void> {
    if (this.busy_()) return;
    const name = this.name_().trim();
    if (!isAliasName(name)) {
      this.formError_.set(INVALID_ALIAS_NAME);
      return;
    }
    this.formError_.set(null);
    const refusal = await this.during(() => this.pc.create(name));
    if (refusal) this.formError_.set(refusal.message);
  }

  private async link(): Promise<void> {
    if (this.busy_()) return;
    this.formError_.set(null);
    const before = new Set(this.aliases().map((alias) => alias.id));
    const refusal = await this.during(() => this.pc.link(this.login_(), this.password_()));
    if (refusal) {
      this.formError_.set(refusal.message);
      this.password_.set('');
      return;
    }
    this.cancel();
    // The page with the linked alias on it
    const added = this.aliases().findIndex((alias) => !before.has(alias.id));
    if (added >= 0) this.page_.set(Math.floor(added / ALIAS_PAGE_SIZE));
  }

  private async during<T>(call: () => Promise<T>): Promise<T> {
    this.busy_.set(true);
    try {
      return await call();
    } finally {
      this.busy_.set(false);
    }
  }
}
