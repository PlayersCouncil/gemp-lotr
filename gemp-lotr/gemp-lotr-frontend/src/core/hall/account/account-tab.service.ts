import { inject, Injectable, Provider } from '@angular/core';
import { AuthoringApi } from '../../game/authoring/authoring-api.service';
import { ScenarioApi } from '../../game/scenario/scenario-api.service';
import { LocalSettings } from '../../platform/local-settings.service';
import { Timers } from '../../platform/timers';
import { ReturnRefresh } from '../return-refresh';
import { AccountApi } from './account-api.service';
import { AccountOverview } from './account-overview.service';
import { ACCOUNT_SUB_TAB_HREFS, ACCOUNT_SUB_TAB_STORAGE_KEY, AccountSubTab, rememberedSubTab } from './account-routes';
import { GameHistory } from './game-history.service';
import { MyScenarios } from './my-scenarios.service';
import { PlayerStatsView } from './player-stats.service';

/**
 * The My Account tab (includes/account.html and its sub-tabs' pages): My Account's own part (the ignore list) loads
 * when the tab first opens, whichever sub-tab shows; each other sub-tab loads the first time it shows, and keeps what
 * it shows when the viewer comes back (account.html's beforeLoad). The sub-tab shown is remembered (localStorage
 * `gemp.account.subTab`), to open when the tab is entered from elsewhere. The ignore list, the game history and My Scenarios are fetched again
 * when the viewer comes back to them after 10 s or more (`returns`); My Stats is not.
 */
@Injectable()
export class AccountTab {
  private readonly settings = inject(LocalSettings);
  readonly overview = inject(AccountOverview);
  readonly history = inject(GameHistory);
  readonly stats = inject(PlayerStatsView);
  readonly scenarios = inject(MyScenarios);
  /** The view reports which sub-tab is shown, and whether the page is in front of the viewer. */
  readonly returns = new ReturnRefresh(inject(Timers));

  private started = false;
  private readonly loaded = new Set<AccountSubTab>();

  /** The sub-tab last shown (the legacy href kept in localStorage), else My Account. */
  remembered(): AccountSubTab {
    return rememberedSubTab(this.settings.read(ACCOUNT_SUB_TAB_STORAGE_KEY));
  }

  /**
   * The sub-tab shown (null: the My Account tab is not showing): the tab starts, the sub-tab loads the first time,
   * is remembered, and counts for the return refresh.
   */
  subTabShown(sub: AccountSubTab | null): void {
    if (sub !== null) {
      this.start();
      this.settings.write(ACCOUNT_SUB_TAB_STORAGE_KEY, ACCOUNT_SUB_TAB_HREFS[sub]);
      this.load(sub);
    }
    for (const name of ['overview', 'history', 'scenarios'] as const) {
      this.returns.setShown(name, name === sub);
    }
  }

  /** The tab's first opening: My Account's own part (AccountIgnores.init). */
  private start(): void {
    if (!this.started) {
      this.started = true;
      void this.overview.loadIgnores();
      this.returns.register('overview', () => void this.overview.loadIgnores());
    }
  }

  /** A sub-tab's first showing; its return refresh counts from then. */
  private load(sub: AccountSubTab): void {
    if (this.loaded.has(sub)) {
      return;
    }
    this.loaded.add(sub);
    if (sub === 'history') {
      this.history.start();
      this.returns.register('history', () => this.history.refresh());
    } else if (sub === 'stats') {
      void this.stats.start();
    } else if (sub === 'scenarios') {
      this.scenarios.start();
      this.returns.register('scenarios', () => void this.scenarios.refresh());
    }
  }
}

/**
 * The My Account tab's own state and calls, for its route's providers. It sits under the hall's route, whose
 * HALL_SERVICES give it the chat (the ignore list), the hall's poll (the pocket) and its session; GempHttp,
 * PlayerInfo, PageNavigation, LocalSettings, Timers and the Notifier are app-wide.
 */
export const ACCOUNT_SERVICES: Provider[] = [
  AccountApi,
  AuthoringApi,
  ScenarioApi,
  AccountOverview,
  GameHistory,
  PlayerStatsView,
  MyScenarios,
  AccountTab,
];
