import { Component, computed, inject } from '@angular/core';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import {
  CANCEL,
  CONNECT_PASSWORD_LABEL,
  CONNECT_PC_ACCOUNT,
  CONNECT_SUBMIT,
  MANAGE_PC_ACCOUNT,
  PC_ABOUT,
  PC_BLOCK_HEADING,
  PC_IS_CONNECTED,
  pcNotice,
} from '../../../core/session/pc-account-messages';
import { PcConnect } from '../../../core/session/pc-connect.service';
import { PcSessionService } from '../../../core/session/pc-session.service';
import { AliasListComponent } from '../../shared/pc-account/alias-list.component';
import { PcFormComponent, PcFormField } from '../../shared/pc-account/pc-form.component';

/**
 * My Account's PC Account block (PC-ACCOUNT.md): nothing in `test` mode. A PC account signed in gets the alias list
 * and Manage PC Account (its page at the account service, in a new tab); a password login gets, if its account is not
 * linked, a line on what a PC account is and Connect PC Account, which opens a form in place
 * for the account's password (`gd-pc-form`, PcConnect): accepted, the page leaves for the PC sign-in that links it;
 * else a line saying it is connected. Where a Connect came back to (`?pc=connected`, `linked-elsewhere`, `failed`,
 * `cancelled`) is a line on top.
 *
 * DOM contract: `gd-pc-account-block.gd-pc-account-block` (`data-kind` = pc | password, once known and outside `test`)
 * holding, outside `test` mode, `section` (`aria-labelledby` its `h2`) with `p.gd-pc-account-block__notice`
 * (`role=status`, with a `?pc=` line), `p.gd-pc-account-block__refusal` (`role=alert`, a refusal whose form or list
 * the session then took away, e.g. Connect's `not-allowed`), then for pc `gd-alias-list[data-place=account]` and
 * `a.gd-pc-account-block__manage` (`target=_blank`); for a password login `p.gd-pc-account-block__about` and
 * `button.gd-pc-account-block__connect`, or once pressed `gd-pc-form` (its form's `data-form=connect`, the field
 * `password`) (not linked) or `p.gd-pc-account-block__connected` (linked).
 */
@Component({
  selector: 'gd-pc-account-block',
  styleUrl: './pc-account-block.component.css',
  imports: [AliasListComponent, PcFormComponent],
  providers: [PcConnect],
  host: { class: 'gd-pc-account-block', '[attr.data-kind]': 'shown()' },
  template: `
    @if (shown(); as kind) {
      <section aria-labelledby="gd-pc-account-heading">
        <h2 id="gd-pc-account-heading">{{ text.heading }}</h2>
        @if (notice; as notice) {
          <p class="gd-pc-account-block__notice" role="status">{{ notice }}</p>
        }
        @if (pc.strayRefusal(); as refusal) {
          <p class="gd-pc-account-block__refusal" role="alert">{{ refusal }}</p>
        }
        @if (kind === 'pc') {
          <gd-alias-list place="account" />
          @if (pc.session()?.pcAccount?.manageUrl; as manage) {
            <a class="gd-pc-account-block__manage" [href]="manage" target="_blank" rel="noopener">{{ text.manage }}</a>
          }
        } @else if (pc.session()?.linked) {
          <p class="gd-pc-account-block__connected">{{ text.connected }}</p>
        } @else {
          <p class="gd-pc-account-block__about">{{ text.about }}</p>
          @if (connect.open()) {
            <gd-pc-form
              form="connect"
              [label]="text.connect"
              [fields]="connectFields"
              [values]="{ password: connect.password() }"
              [error]="connect.error()"
              [submitLabel]="text.connectSubmit"
              [cancelLabel]="text.cancel"
              [busy]="connect.busy()"
              (valueChange)="connect.setPassword($event.value)"
              (submitted)="submitConnect()"
              (cancelled)="connect.cancel()"
            />
          } @else {
            <button type="button" class="gd-button gd-pc-account-block__connect" (click)="connect.start()">
              {{ text.connect }}
            </button>
          }
        }
      </section>
    }
  `,
})
export class PcAccountBlockComponent {
  protected readonly pc = inject(PcSessionService);
  protected readonly notice = pcNotice(inject(PAGE_QUERY_STRING), 'account');
  protected readonly connect = inject(PcConnect);
  protected readonly connectFields: readonly PcFormField[] = [
    { name: 'password', label: CONNECT_PASSWORD_LABEL, type: 'password', autocomplete: 'current-password' },
  ];
  protected readonly text = {
    connectSubmit: CONNECT_SUBMIT,
    cancel: CANCEL,
    heading: PC_BLOCK_HEADING,
    manage: MANAGE_PC_ACCOUNT,
    connect: CONNECT_PC_ACCOUNT,
    about: PC_ABOUT,
    connected: PC_IS_CONNECTED,
  };

  /** pc or password: what the block shows; null (nothing) in test mode, for nobody, and until known. */
  protected readonly shown = computed(() => {
    const kind = this.pc.kind();
    return kind === 'pc' || kind === 'password' ? kind : null;
  });

  constructor() {
    void this.pc.load();
  }

  protected submitConnect(): void {
    void this.connect.submit();
  }
}
