import { Component, computed, inject, input } from '@angular/core';
import { AliasList } from '../../../core/session/alias-list.service';
import { PcFormComponent, PcFormField } from './pc-form.component';
import { Alias, ALIAS_NAME_MAX } from '../../../core/session/pc-account';
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
  LINK_PASSWORD_LABEL,
  LINK_SUBMIT,
  LOG_OUT,
  NO_ALIASES,
  PAGE_NEXT,
  PAGE_PREVIOUS,
  pageLine,
  signedInAs,
} from '../../../core/session/pc-account-messages';

/**
 * The alias list (PC-ACCOUNT.md, "What A Person Does"), the same on the login page and on My Account: for a signed-in
 * PC account its name, its aliases ten to a page each with Enter Gemp, Create New Gemp Alias and Link Existing Gemp
 * Account (each opening its form under the list) and Log Out; for a password login its one account with Enter Gemp
 * and Log Out. While the alias this browser plays is busy (`blockedBy`) every other Enter Gemp is disabled, and Create too,
 * with the reason above the list. Each refusal shows beside what it concerns. The state is AliasList's (core/session),
 * one per list; nothing is drawn until the session is known, or for anyone else (test mode, nobody).
 *
 * DOM contract: `gd-alias-list.gd-alias-list` (`data-place` = login | account, `data-kind` = pc | password,
 * `data-busy` and `aria-busy` while a call is under way) holding `p.gd-alias-list__signed-in` (pc), then
 * `p.gd-alias-list__blocked` (`id`, while blocked), `p.gd-alias-list__empty` (pc, no aliases),
 * `ul.gd-alias-list__aliases` (`aria-label`) of `li.gd-alias-list__alias` (`data-alias` = its name, `data-current`,
 * `data-busy`) with `span.gd-alias-list__name`, `span.gd-alias-list__tag` (`data-tag` = current | playing),
 * `button.gd-button[data-action=enter]` (`aria-describedby` the blocked line when disabled for it) and
 * `p.gd-alias-list__error` (`role=alert`, that alias's refusal); `div.gd-alias-list__pager` (more than one page:
 * `button.gd-button[data-action=previous|next]`, `span.gd-alias-list__page`); `div.gd-alias-list__actions` with
 * `button.gd-button[data-action]` = create, link (pc), logout; and with a form open `gd-pc-form` (its form's `data-form` =
 * create | link; fields `name`, or `login` and `password`; see PcFormComponent).
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
      @if (session.pcAccount; as account) {
        <p class="gd-alias-list__signed-in">{{ signedInAs(account.name) }}</p>
      }
      @if (kind() !== null) {
        @if (list.blockedBy(); as busy) {
          <p class="gd-alias-list__blocked" id="gd-alias-list-blocked">{{ blockedReason(busy) }}</p>
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
        <div class="gd-alias-list__actions">
          @if (kind() === 'pc') {
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
          }
          <button type="button" class="gd-button" data-action="logout" [disabled]="list.busy()" (click)="logout()">
            {{ text.logout }}
          </button>
        </div>
        @if (kind() === 'pc' && list.form(); as form) {
          <gd-pc-form
            [place]="place()"
            [form]="form"
            [label]="form === 'create' ? text.create : text.link"
            [fields]="form === 'create' ? createFields : linkFields"
            [values]="{ name: list.name(), login: list.login(), password: list.password() }"
            [error]="list.formError()"
            [submitLabel]="form === 'create' ? text.createSubmit : text.linkSubmit"
            [cancelLabel]="text.cancel"
            [busy]="list.busy()"
            (valueChange)="change($event)"
            (submitted)="submit()"
            (cancelled)="list.cancel()"
          />
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

  protected readonly signedInAs = signedInAs;
  protected readonly blockedReason = blockedReason;
  protected readonly pageLine = pageLine;
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
