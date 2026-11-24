import { Component, computed, inject, input } from '@angular/core';
import { AliasList } from '../../../core/session/alias-list.service';
import { PcFormComponent, PcFormField } from './pc-form.component';
import { Alias, ALIAS_NAME_MAX, PcSession } from '../../../core/session/pc-account';
import {
  ALIAS_CURRENT,
  ALIAS_NAME_LABEL,
  ALIAS_PLAYING,
  ALIASES_HEADING,
  blockedReason,
  CANCEL,
  CREATE_ALIAS,
  CREATE_SUBMIT,
  ENTER_GEMP,
  LINK_ACCOUNT,
  LINK_LOGIN_LABEL,
  LINK_NOTE,
  LINK_PASSWORD_LABEL,
  LINK_SUBMIT,
  LOG_OUT,
  NO_ALIASES,
  PAGE_NEXT,
  PAGE_PREVIOUS,
  pageLine,
  LOGGED_IN_AS,
  SIGNED_IN_AS,
} from '../../../core/session/pc-account-messages';

/**
 * The alias list (PC-ACCOUNT.md, "What A Person Does"; on screen the aliases are "Gemp profiles"), the same on the
 * login page and on My Account. Its first line says who is signed in ("Signed in as" the PC account's name, or for a
 * password login "Logged in as" its account, the name in bold), with Log Out at the line's end; on the login page the
 * sentence is centred. Then the aliases, ten to a page, each with Enter Gemp; then, further down, Create New Gemp
 * Profile and Link Existing Gemp Account (a PC account; centred on the login page). Either opens a row at the end of
 * the list, where the next alias would show, laid out as an alias's row: the field(s) in place of the name and the
 * confirming button and Cancel where Enter Gemp is (`gd-pc-form`), its note and its refusal (red) under it. Only one
 * such row is open; the page with the list's end is shown while it is. A password login has its one account, Enter
 * Gemp and Log Out. While the alias this browser plays is busy (`blockedBy`) every other Enter Gemp is disabled, and
 * Create too, with the reason above the list. Each refusal shows beside what it concerns. The state is AliasList's
 * (core/session), one per list; nothing is drawn until the session is known, or for anyone else (test mode, nobody).
 *
 * DOM contract: `gd-alias-list.gd-alias-list` (`data-place` = login | account, `data-kind` = pc | password,
 * `data-busy` and `aria-busy` while a call is under way) holding `div.gd-alias-list__head` (`p.gd-alias-list__signed-in`
 * with `b.gd-alias-list__who`, and `button.gd-button[data-action=logout]`), then `p.gd-alias-list__blocked` (`id`,
 * while blocked), `p.gd-alias-list__refusal` (`role=alert`: a refused Enter Gemp whose alias has left the list),
 * `p.gd-alias-list__empty` (pc, no aliases), `ul.gd-alias-list__aliases` (`aria-label`) of `li.gd-alias-list__alias`
 * (`data-alias` = its name, `data-current`, `data-busy`) with `span.gd-alias-list__name`, `span.gd-alias-list__tag`
 * (`data-tag` = current | playing), `button.gd-button[data-action=enter]` (`aria-describedby` the blocked line when
 * disabled for it) and `p.gd-alias-list__error` (`role=alert`, that alias's refusal), and last, with a row open,
 * `li.gd-alias-list__new` (`data-form` = create | link) holding `gd-pc-form` (its form's `data-form` the same; fields
 * `name`, or `login` and `password`; see PcFormComponent); `div.gd-alias-list__pager` (more than one page:
 * `button.gd-button[data-action=previous|next]`, `span.gd-alias-list__page`); `div.gd-alias-list__actions` (pc) with
 * `button.gd-button[data-action]` = create, link (`aria-expanded` while its row is open).
 */
@Component({
  selector: 'gd-alias-list',
  styleUrl: './alias-list.component.css',
  imports: [PcFormComponent],
  providers: [AliasList],
  host: {
    class: 'gd-alias-list',
    '[attr.data-place]': 'place()',
    '[attr.data-kind]': 'kind()',
    '[attr.data-busy]': "list.busy() ? '' : null",
    '[attr.aria-busy]': 'list.busy()',
  },
  template: `
    @if (list.session(); as session) {
      @if (kind() !== null) {
        <div class="gd-alias-list__head">
          <p class="gd-alias-list__signed-in">
            <span>{{ signedIn(session) }}</span
            >&ngsp;<b class="gd-alias-list__who">{{ who(session) }}</b>
          </p>
          <button type="button" class="gd-button" data-action="logout" [disabled]="list.busy()" (click)="logout()">
            {{ text.logout }}
          </button>
        </div>
        @if (list.blockedBy(); as busy) {
          <p class="gd-alias-list__blocked" id="gd-alias-list-blocked">{{ blockedReason(busy) }}</p>
        }
        @if (list.listError(); as refusal) {
          <p class="gd-alias-list__refusal" role="alert">{{ refusal }}</p>
        }
        @if (kind() === 'pc' && list.aliases().length === 0) {
          <p class="gd-alias-list__empty">{{ text.empty }}</p>
        }
        <ul class="gd-alias-list__aliases" [attr.aria-label]="text.heading">
          @if (kind() === 'pc') {
            @for (alias of list.shown(); track alias.id) {
              <li
                class="gd-alias-list__alias"
                [attr.data-alias]="alias.name"
                [attr.data-current]="alias.current ? '' : null"
                [attr.data-busy]="alias.busy ? '' : null"
              >
                <span class="gd-alias-list__name">{{ alias.name }}</span>
                @if (alias.current) {
                  <span class="gd-alias-list__tag" data-tag="current">{{ text.current }}</span>
                }
                @if (alias.busy) {
                  <span class="gd-alias-list__tag" data-tag="playing">{{ text.playing }}</span>
                }
                <button
                  type="button"
                  class="gd-button"
                  data-action="enter"
                  [disabled]="list.busy() || !list.canEnter(alias)"
                  [attr.aria-describedby]="list.canEnter(alias) ? null : 'gd-alias-list-blocked'"
                  (click)="enter(alias)"
                >
                  {{ text.enter }}
                </button>
                @if (list.aliasError(); as error) {
                  @if (error.id === alias.id) {
                    <p class="gd-alias-list__error" role="alert">{{ error.message }}</p>
                  }
                }
              </li>
            }
            @if (list.form(); as form) {
              <li class="gd-alias-list__new" [attr.data-form]="form">
                <gd-pc-form
                  [place]="place()"
                  [form]="form"
                  [label]="form === 'create' ? text.create : text.link"
                  [fields]="form === 'create' ? createFields : linkFields"
                  [values]="{ name: list.name(), login: list.login(), password: list.password() }"
                  [note]="form === 'link' ? text.linkNote : null"
                  [error]="list.formError()"
                  [submitLabel]="form === 'create' ? text.createSubmit : text.linkSubmit"
                  [cancelLabel]="text.cancel"
                  [busy]="list.busy()"
                  (valueChange)="change($event)"
                  (submitted)="submit()"
                  (cancelled)="list.cancel()"
                />
              </li>
            }
          } @else if (session.player; as player) {
            <li class="gd-alias-list__alias" [attr.data-alias]="player" data-current>
              <span class="gd-alias-list__name">{{ player }}</span>
              <button
                type="button"
                class="gd-button"
                data-action="enter"
                [disabled]="list.busy()"
                (click)="list.enterLoggedIn()"
              >
                {{ text.enter }}
              </button>
            </li>
          }
        </ul>
        @if (kind() === 'pc' && list.pages() > 1) {
          <div class="gd-alias-list__pager">
            <button
              type="button"
              class="gd-button"
              data-action="previous"
              [disabled]="list.page() === 0"
              (click)="list.previousPage()"
            >
              {{ text.previous }}
            </button>
            <span class="gd-alias-list__page">{{ pageLine(list.page() + 1, list.pages()) }}</span>
            <button
              type="button"
              class="gd-button"
              data-action="next"
              [disabled]="list.page() === list.pages() - 1"
              (click)="list.nextPage()"
            >
              {{ text.next }}
            </button>
          </div>
        }
        @if (kind() === 'pc') {
          <div class="gd-alias-list__actions">
            <button
              type="button"
              class="gd-button"
              data-action="create"
              [disabled]="list.busy() || list.blockedBy() !== null"
              [attr.aria-describedby]="list.blockedBy() !== null ? 'gd-alias-list-blocked' : null"
              [attr.aria-expanded]="list.form() === 'create'"
              (click)="list.open('create')"
            >
              {{ text.create }}
            </button>
            <button
              type="button"
              class="gd-button"
              data-action="link"
              [disabled]="list.busy()"
              [attr.aria-expanded]="list.form() === 'link'"
              (click)="list.open('link')"
            >
              {{ text.link }}
            </button>
          </div>
        }
      }
    }
  `,
})
export class AliasListComponent {
  /** Where the list is: the login page (its form's place and look) or My Account. */
  readonly place = input<'login' | 'account'>('account');

  protected readonly list = inject(AliasList);
  /** pc or password; null for anyone else, who gets nothing here. */
  protected readonly kind = computed(() => {
    const kind = this.list.kind();
    return kind === 'pc' || kind === 'password' ? kind : null;
  });

  protected readonly blockedReason = blockedReason;
  protected readonly pageLine = pageLine;
  /** The first line's words, before the name: a PC account, or a password login's account. */
  protected readonly signedIn = (session: PcSession) => (session.pcAccount ? SIGNED_IN_AS : LOGGED_IN_AS);
  protected readonly who = (session: PcSession) => session.pcAccount?.name ?? session.player ?? '';
  protected readonly createFields: readonly PcFormField[] = [
    { name: 'name', label: ALIAS_NAME_LABEL, type: 'text', autocomplete: 'off', maxLength: ALIAS_NAME_MAX },
  ];
  protected readonly linkFields: readonly PcFormField[] = [
    { name: 'login', label: LINK_LOGIN_LABEL, type: 'text', autocomplete: 'username' },
    { name: 'password', label: LINK_PASSWORD_LABEL, type: 'password', autocomplete: 'current-password' },
  ];
  protected readonly text = {
    heading: ALIASES_HEADING,
    empty: NO_ALIASES,
    current: ALIAS_CURRENT,
    playing: ALIAS_PLAYING,
    enter: ENTER_GEMP,
    create: CREATE_ALIAS,
    link: LINK_ACCOUNT,
    logout: LOG_OUT,
    previous: PAGE_PREVIOUS,
    next: PAGE_NEXT,
    createSubmit: CREATE_SUBMIT,
    linkSubmit: LINK_SUBMIT,
    cancel: CANCEL,
    linkNote: LINK_NOTE,
  };

  constructor() {
    void this.list.start();
  }

  protected change({ name, value }: { name: string; value: string }): void {
    if (name === 'name') this.list.setName(value);
    else if (name === 'login') this.list.setLogin(value);
    else this.list.setPassword(value);
  }

  protected enter(alias: Alias): void {
    void this.list.enter(alias);
  }

  protected submit(): void {
    void this.list.submit();
  }

  protected logout(): void {
    void this.list.logout();
  }
}
