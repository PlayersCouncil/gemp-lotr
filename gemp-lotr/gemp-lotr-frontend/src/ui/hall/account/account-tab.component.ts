import { Component, DestroyRef, inject } from '@angular/core';
import { AccountSubTab } from '../../../core/hall/account/account-routes';
import { AccountTab } from '../../../core/hall/account/account-tab.service';
import { HallTabFrameComponent } from '../shared/hall-tab-frame.component';

const SUB_TABS: readonly string[] = ['overview', 'history', 'stats', 'scenarios'] satisfies AccountSubTab[];

/** The route data naming the sub-tab a route of the tab shows. */
export const ACCOUNT_SUB_TAB_DATA = 'accountSubTab';

/**
 * The My Account tab (includes/account.html #accountMain): its sub-tabs and the sub-tab's page (the routes'
 * components) in the hall's tab frame. Tells the tab's state which sub-tab is shown (to load it the first time,
 * remember it and refresh it when the viewer comes back) and whether the page is in front of the viewer (the browser
 * tab shown and the window focused: GempHallTabs.onReturn).
 *
 * DOM contract: `gd-account-tab.gd-account-tab` (`display: contents`; `data-sub-tab` = the sub-tab shown) holding
 * `gd-hall-tab-frame[data-tab=account]`.
 */
@Component({
  selector: 'gd-account-tab',
  imports: [HallTabFrameComponent],
  host: {
    style: 'display: contents',
    class: 'gd-account-tab',
    '[attr.data-sub-tab]': 'shown',
  },
  template: `
    <gd-hall-tab-frame
      tab="account"
      (subTabChange)="report($event)"
      (pageShownChange)="tab.returns.setPageShown($event)"
    />
  `,
})
export class AccountTabComponent {
  protected readonly tab = inject(AccountTab);
  protected shown: AccountSubTab | null = null;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.tab.subTabShown(null));
  }

  /** The sub-tab shown (the frame reads it from the address). */
  protected report(slug: string | null): void {
    const shown = slug !== null && SUB_TABS.includes(slug) ? (slug as AccountSubTab) : null;
    if (shown !== this.shown) {
      this.shown = shown;
      this.tab.subTabShown(shown);
    }
  }
}
