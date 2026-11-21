import { DestroyRef, inject, Injectable, Provider } from '@angular/core';
import { Timers } from '../../platform/timers';
import { EventHistoryBrowser, EventHistorySource } from './event-history';
import { EventsApi } from './events-api.service';
import { EventsCalendar } from './events-calendar.service';
import { EVENTS_LEAGUE_JOIN_LISTENER } from './events-hooks';
import { CompletedEventsMonth, EventKind } from './events-models';
import { EventsLink, EventsSubTab } from './events-routes';
import { LeagueResults } from './league-results.service';
import { ReturnRefresh } from './return-refresh';
import { TournamentResults } from './tournament-results.service';

/**
 * The Events tab (includes/events.html's script): its three sub-tabs' state, started together the first time the tab
 * opens. The completed-events panels start once the live list above each has been shown (or failed), so they never
 * hold up the current events. Joining a league marks it on the calendar (the calendar loads again) and tells the
 * hall (EVENTS_LEAGUE_JOIN_LISTENER, if provided). Each sub-tab's
 * live data is fetched again when the viewer comes back to it after 10 s or more (`returns`); open drawers, the
 * event selected and the completed-events panels stay as they are.
 */
@Injectable()
export class EventsTab {
  private readonly api = inject(EventsApi);
  private readonly timers = inject(Timers);
  readonly calendar = inject(EventsCalendar);
  readonly leagues = inject(LeagueResults);
  readonly tournaments = inject(TournamentResults);

  private readonly history: EventHistorySource = {
    months: (kind) => this.api.eventHistoryMonths(kind),
    month: (month, kind) => this.api.eventHistory(month, kind),
    league: (code) => this.api.league(code),
    tournament: (id) => this.api.tournament(id),
    monthsCache: new Map<EventKind, string[]>(),
    monthCache: new Map<string, CompletedEventsMonth>(),
  };
  /** Completed leagues, under Current Leagues. */
  readonly leagueHistory = new EventHistoryBrowser('league', this.history, this.timers);
  /** Completed tournaments, under Current Tournaments. */
  readonly tournamentHistory = new EventHistoryBrowser('tournament', this.history, this.timers);
  /** The view reports which sub-tab is shown, and whether the page is in front of the viewer. */
  readonly returns = new ReturnRefresh(this.timers);

  private started = false;

  constructor() {
    const listener = inject(EVENTS_LEAGUE_JOIN_LISTENER, { optional: true });
    const joined = this.leagues.joined$.subscribe((code) => {
      void this.calendar.load();
      listener?.leagueJoined(code);
    });
    inject(DestroyRef).onDestroy(() => joined.unsubscribe());
    this.returns.register('calendar', () => void this.calendar.refresh());
    this.returns.register('leagues', () => this.leagues.refreshList());
    this.returns.register('tournaments', () => this.tournaments.refreshList());
  }

  /** The tab's first opening: everything loads, as the legacy page did when its include loaded. */
  start(): void {
    if (this.started) {
      return;
    }
    this.started = true;
    void this.leagues.loadList().then(() => this.leagueHistory.start());
    void this.tournaments.loadList().then(() => this.tournamentHistory.start());
    void this.calendar.show();
  }

  /** The sub-tab shown, for the return refresh (null: the Events tab is not showing). */
  subTabShown(sub: EventsSubTab | null): void {
    for (const name of ['calendar', 'leagues', 'tournaments'] as const) {
      this.returns.setShown(name, name === sub);
    }
  }

  /** A route of the tab was opened: a league's or tournament's link opens its drawer (GempEventsPage). */
  open(link: EventsLink): void {
    this.start();
    if (link.sub === 'leagues' && link.code !== null) {
      void this.leagues.showLeague(link.code);
    } else if (link.sub === 'tournaments' && link.id !== null) {
      void this.tournaments.showTournament(link.id);
    }
  }
}

/** The Events tab's own state and calls, for its route's providers. GempHttp, Timers and the Notifier are app-wide. */
export const EVENTS_SERVICES: Provider[] = [EventsApi, LeagueResults, TournamentResults, EventsCalendar, EventsTab];
