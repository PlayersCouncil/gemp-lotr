import { Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AccountSubTab } from '../../../core/hall/account/account-routes';
import { AccountTab } from '../../../core/hall/account/account-tab.service';
import { findHallTab, HallTab } from '../../../core/hall/hall-tabs';
import { HallSubTabsComponent } from '../hall-sub-tabs.component';

function accountHallTab(): HallTab {
  const tab = findHallTab('account');
  if (!tab) {
    throw new Error('HALL_TABS has no My Account tab');
  }
  return tab;
}

/** The route data naming the sub-tab a route of the tab shows. */
export const ACCOUNT_SUB_TAB_DATA = 'accountSubTab';

/**
 * The My Account tab (includes/account.html #accountMain): its sub-tabs at the left (`gd-hall-sub-tabs`) and the
 * sub-tab's page beside them (the routes' components). Tells the tab's state which sub-tab is shown (to load it the
 * first time, remember it and refresh it when the viewer comes back) and whether the page is in front of the viewer
 * (the browser tab shown and the window focused: GempHallTabs.onReturn).
 *
 * DOM contract: `gd-account-tab.gd-account-tab` (`data-sub-tab` = the sub-tab shown) holding `gd-hall-sub-tabs` and
 * `div.gd-account-tab__panel` (`role=tabpanel`) with the sub-tab's page.
 */
@Component({
  selector: 'gd-account-tab',
  styleUrl: './account-tab.component.css',
  imports: [RouterOutlet, HallSubTabsComponent],
  host: {
    class: 'gd-account-tab',
    '[attr.data-sub-tab]': 'shown',
    '(document:visibilitychange)': 'visibility($event)',
    '(window:blur)': 'tab.returns.setPageShown(false)',
    '(window:focus)': 'tab.returns.setPageShown(true)',
  },
  template: `
    <gd-hall-sub-tabs [tabs]="hallTab.subTabs" [base]="'/' + hallTab.path" [label]="hallTab.label" />
    <div class="gd-account-tab__panel" role="tabpanel">
      <router-outlet />
    </div>
  `,
})
export class AccountTabComponent {
  protected readonly tab = inject(AccountTab);
  private readonly route = inject(ActivatedRoute);
  protected readonly hallTab = accountHallTab();
  protected shown: AccountSubTab | null = null;

  constructor() {
    inject(Router)
      .events.pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.report());
    this.report();
    inject(DestroyRef).onDestroy(() => this.tab.subTabShown(null));
  }

  protected visibility(event: Event): void {
    this.tab.returns.setPageShown(!(event.target as Document).hidden);
  }

  /** The sub-tab of the child route shown. */
  private report(): void {
    const sub: unknown = this.route.snapshot.firstChild?.data[ACCOUNT_SUB_TAB_DATA];
    const shown = typeof sub === 'string' ? (sub as AccountSubTab) : null;
    if (shown !== this.shown) {
      this.shown = shown;
      this.tab.subTabShown(shown);
    }
  }
}
