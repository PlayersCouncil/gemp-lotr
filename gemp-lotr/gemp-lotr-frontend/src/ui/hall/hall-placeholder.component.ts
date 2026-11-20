import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { findHallTab, HallTab } from '../../core/hall/hall-tabs';
import { HALL_TAB_DATA } from './hall-route-data';
import { HallSubTabsComponent } from './hall-sub-tabs.component';

/** What each tab will show, for its placeholder's name. */
const CONTENT: Readonly<Record<string, string>> = {
  'game-hall': 'Tables',
  help: 'Help',
  events: 'Events',
  info: 'Server Info',
  account: 'My Account',
};

/**
 * Where a later part of the hall port draws a tab's content (HallTab.part: tables 4.2, Help and Server Info 4.3,
 * Events and My Account 4.4), after kermit's (kermit/feature/frontend-themes). The tab is the route's
 * (`data: { hallTab: '<slug>' }`); a tab with sub-tabs shows them (gd-hall-sub-tabs), so each sub-tab already has
 * its address. Each tab's own routes file replaces its placeholder with its own component.
 *
 * DOM contract: `gd-hall-placeholder.gd-hall-placeholder` (`data-tab`, `data-part`) holding, for a tab with
 * sub-tabs, `gd-hall-sub-tabs`, then `div.gd-hall-placeholder__content` (`role=region`, `aria-label` "Help — coming
 * in 4.3"; empty).
 */
@Component({
  selector: 'gd-hall-placeholder',
  styleUrl: './hall-placeholder.component.css',
  imports: [HallSubTabsComponent],
  host: {
    class: 'gd-hall-placeholder',
    '[attr.data-tab]': 'tab.slug',
    '[attr.data-part]': 'tab.part',
  },
  template: `
    @if (tab.subTabs.length > 0 && tab.path !== null) {
      <gd-hall-sub-tabs [tabs]="tab.subTabs" [base]="'/' + tab.path" [label]="tab.label" />
    }
    <div class="gd-hall-placeholder__content" role="region" [attr.aria-label]="label"></div>
  `,
})
export class HallPlaceholderComponent {
  protected readonly tab: HallTab = this.routeTab();
  protected readonly label = `${CONTENT[this.tab.slug] ?? this.tab.label} — coming in ${this.tab.part}`;

  private routeTab(): HallTab {
    let route: ActivatedRoute | null = inject(ActivatedRoute);
    while (route) {
      const slug: unknown = route.snapshot.data[HALL_TAB_DATA];
      const tab = typeof slug === 'string' ? findHallTab(slug) : null;
      if (tab) return tab;
      route = route.parent;
    }
    throw new Error('A hall placeholder needs its route to name its tab (data.hallTab)');
  }
}
