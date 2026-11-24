import { Component, computed, inject } from '@angular/core';
import { PAGE_QUERY_STRING } from '../../../core/platform/page-location';
import { pcSignInAddress } from '../../../core/session/pc-account';
import {
  CONNECT_PC_ACCOUNT,
  MANAGE_PC_ACCOUNT,
  PC_ABOUT,
  PC_BLOCK_HEADING,
  PC_IS_CONNECTED,
  pcNotice,
} from '../../../core/session/pc-account-messages';
import { PcSessionService } from '../../../core/session/pc-session.service';
import { AliasListComponent } from '../../shared/pc-account/alias-list.component';

/**
 * My Account's PC Account block (PC-ACCOUNT.md): nothing in `test` mode. A PC account signed in gets the alias list
 * and Manage PC Account (its page at the account service, in a new tab); a password login gets, if its account is not
 * linked, a line on what a PC account is and Connect PC Account (a PC sign-in that links the account logged in), else
 * a line saying it is connected. Where a Connect came back to (`?pc=connected`, `linked-elsewhere`, `failed`,
 * `cancelled`) is a line on top.
 *
 * DOM contract: `gd-pc-account-block.gd-pc-account-block` (`data-kind` = pc | password, once known and outside `test`)
 * holding, outside `test` mode, `section` (`aria-labelledby` its `h2`) with `p.gd-pc-account-block__notice`
 * (`role=status`, with a `?pc=` line), then for pc `gd-alias-list[data-place=account]` and
 * `a.gd-pc-account-block__manage` (`target=_blank`); for a password login `p.gd-pc-account-block__about` and
 * `a.gd-pc-account-block__connect` (not linked) or `p.gd-pc-account-block__connected` (linked).
 */
@Component({
  selector: 'gd-pc-account-block',
  styleUrl: './pc-account-block.component.css',
  imports: [AliasListComponent],
  host: { class: 'gd-pc-account-block', '[attr.data-kind]': 'shown()' },
  template: `
    @if (shown(); as kind) {
      <section aria-labelledby="gd-pc-account-heading">
        <h2 id="gd-pc-account-heading">{{ text.heading }}</h2>
        @if (notice; as notice) {
          <p class="gd-pc-account-block__notice" role="status">{{ notice }}</p>
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
          <a class="gd-pc-account-block__connect" [href]="connect">{{ text.connect }}</a>
        }
      </section>
    }
  `,
})
export class PcAccountBlockComponent {
  protected readonly pc = inject(PcSessionService);
  protected readonly notice = pcNotice(inject(PAGE_QUERY_STRING), 'account');
  protected readonly connect = pcSignInAddress('connect');
  protected readonly text = {
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
}
