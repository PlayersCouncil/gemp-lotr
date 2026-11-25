import { inject, Injectable } from '@angular/core';
import { EventDrawer, eventRow } from './event-drawer';
import { EventList } from './event-list';
import { EventsApi } from './events-api.service';
import { TournamentDetail, TournamentSummary } from './events-models';
import {
  eventErrorMessage,
  NO_TOURNAMENT_DETAILS,
  shownStatus,
  TOURNAMENT_NOT_DISPLAYED,
  tournamentFailed,
  tournamentsFailed,
} from './events-messages';

/**
 * Current Tournaments (tournamentResultsUi.js TournamentResultsUI in list mode): one row per live tournament with
 * its drawer holding the tournament's detail (standings, and once it is finished each player's deck, a page the
 * server draws). Its drawers show the "Rounds:" line first.
 */
@Injectable()
export class TournamentResults {
  private readonly api = inject(EventsApi);

  private readonly list_ = new EventList<TournamentSummary, TournamentDetail>({
    fetch: () => this.api.liveTournaments(),
    failed: tournamentsFailed,
    key: (tournament) => tournament.id,
    title: (tournament) => tournament.name,
    date: (tournament) => tournament.start,
    create: (tournament) =>
      eventRow(tournament.id, tournament.name, tournament.start, this.createDrawer(tournament.id)),
  });

  readonly list = this.list_.state;
  readonly rows = this.list_.rows;
  readonly revealed = this.list_.revealed;

  /** loadList (EventList.load). */
  loadList(): Promise<void> {
    return this.list_.load();
  }

  /** refreshList (EventList.refresh). */
  refreshList(): void {
    this.list_.refresh();
  }

  /**
   * showTournament (#tournament-<id>): opens that tournament's row, loading the list first if need be. One that is
   * not live (a scheduled one that has not started) just leaves the list showing.
   */
  showTournament(id: string): Promise<void> {
    return this.list_.show(id);
  }

  /**
   * A drawer showing a tournament's detail (createTournamentRow's). `fallback`: a failed fetch shows the drawer's
   * fallback; `fetch` false: the server is not asked (the tournament has not started), the fallback shows at once.
   */
  createDrawer(id: string, options: { fallback?: boolean; fetch?: boolean } = {}): EventDrawer<TournamentDetail> {
    return new EventDrawer<TournamentDetail>({
      load: () => this.api.tournament(id),
      failure: (error) => tournamentFailed(shownStatus(error), eventErrorMessage(error)),
      unreadableText: TOURNAMENT_NOT_DISPLAYED,
      emptyText: NO_TOURNAMENT_DETAILS,
      fallback: options.fallback ?? false,
      fetch: options.fetch ?? true,
    });
  }
}
