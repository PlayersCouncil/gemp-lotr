import { Component, computed, inject, input } from '@angular/core';
import { AliasList } from '../../../core/session/alias-list.service';
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
 * `button.gd-button[data-action]` = create, link (pc), logout; and with a form open `form.gd-alias-list__form`
 * (`data-form` = create | link) holding a `label.gd-alias-list__field` per field (`data-field` = name | login |
 * password; `span.gd-alias-list__label`, `input.gd-alias-list__input`), `p.gd-alias-list__error` (`role=alert`) and
 * `div.gd-alias-list__form-actions` with `button.gd-button[data-action=submit]` and `[data-action=cancel]`.
 */
@Component({
  selector: 'gd-alias-list',
  styleUrl: './alias-list.component.css',
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
          <form
            class="gd-alias-list__form"
            [attr.data-form]="form"
            [attr.aria-label]="form === 'create' ? text.create : text.link"
            (submit)="$event.preventDefault(); submit()"
          >
            @if (form === 'create') {
              <label class="gd-alias-list__field" data-field="name">
                <span class="gd-alias-list__label">{{ text.name }}</span>
                <input
                  class="gd-alias-list__input"
                  type="text"
                  name="name"
                  autocomplete="off"
                  autocapitalize="none"
                  spellcheck="false"
                  [attr.maxlength]="nameMax"
                  [value]="list.name()"
                  (input)="list.setName(value($event))"
                />
              </label>
            } @else {
              <label class="gd-alias-list__field" data-field="login">
                <span class="gd-alias-list__label">{{ text.login }}</span>
                <input
                  class="gd-alias-list__input"
                  type="text"
                  name="login"
                  autocomplete="username"
                  autocapitalize="none"
                  spellcheck="false"
                  [value]="list.login()"
                  (input)="list.setLogin(value($event))"
                />
              </label>
              <label class="gd-alias-list__field" data-field="password">
                <span class="gd-alias-list__label">{{ text.password }}</span>
                <input
                  class="gd-alias-list__input"
                  type="password"
                  name="password"
                  autocomplete="current-password"
                  [value]="list.password()"
                  (input)="list.setPassword(value($event))"
                />
              </label>
            }
            @if (list.formError(); as error) {
              <p class="gd-alias-list__error" role="alert">{{ error }}</p>
            }
            <div class="gd-alias-list__form-actions">
              <button type="submit" class="gd-button" data-action="submit" [disabled]="list.busy()">
                {{ form === 'create' ? text.createSubmit : text.linkSubmit }}
              </button>
              <button
                type="button"
                class="gd-button"
                data-action="cancel"
                [disabled]="list.busy()"
                (click)="list.cancel()"
              >
                {{ text.cancel }}
              </button>
            </div>
          </form>
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
  protected readonly nameMax = ALIAS_NAME_MAX;
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
    name: ALIAS_NAME_LABEL,
    login: LINK_LOGIN_LABEL,
    password: LINK_PASSWORD_LABEL,
    createSubmit: CREATE_SUBMIT,
    linkSubmit: LINK_SUBMIT,
    cancel: CANCEL,
  };

  constructor() {
    void this.list.start();
  }

  protected value(event: Event): string {
    return (event.target as HTMLInputElement).value;
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
