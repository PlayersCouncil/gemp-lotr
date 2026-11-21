import { inject, Injectable, Injector, signal } from '@angular/core';
import { firstValueFrom, Observable, Subject } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { Notifier } from '../../notifier/notifier';
import { EventDrawer, eventRow } from './event-drawer';
import { EventList } from './event-list';
import { EventsApi } from './events-api.service';
import { LeagueDetail, LeagueSummary } from './events-models';
import { Price } from '../price';
import {
  eventErrorMessage,
  LEAGUE_NOT_DISPLAYED,
  leagueFailed,
  leaguesFailed,
  NO_LEAGUE_DETAILS,
  NOT_ENOUGH_FUNDS,
  shownStatus,
} from './events-messages';

/** The join question (displayBuyAction): "Do you want to join the league by paying <price>?" with Yes and No. */
export interface JoinQuestion {
  readonly code: string;
  readonly cost: Price;
}

/** "Starts to ends"; a missing end, or one equal to the start, shows the start alone (LeagueResultsUI.dateRange). */
export function dateRange(start: string, end: string): string {
  return end === '' || end === start ? start : `${start} to ${end}`;
}

/**
 * Current Leagues (leagueResultsUi.js LeagueResultsUI in list mode): one row per league with its drawer holding
 * the league's detail, several open at once; and the join flow, for every drawer showing a league (the list's and
 * the calendar's). Joining re-renders every live drawer of that league in place and tells `joined$` (the hall's
 * league formats and the calendar's "joined" marks follow it).
 */
@Injectable()
export class LeagueResults {
  private readonly api = inject(EventsApi);
  /** The Notifier is asked for when a join fails, so the tab's state can be made without one (the frame's specs). */
  private readonly injector = inject(Injector);

  private readonly joinQuestion_ = signal<JoinQuestion | null>(null);
  private readonly joining_ = signal(false);
  private readonly joined = new Subject<string>();
  /** league code -> the drawers made for it (list rows, calendar previews). */
  private readonly drawers = new Map<string, EventDrawer<LeagueDetail>[]>();
  private readonly list_ = new EventList<LeagueSummary, LeagueDetail>({
    fetch: () => this.api.leagues(),
    failed: leaguesFailed,
    key: (league) => league.code,
    title: (league) => league.name,
    date: (league) => dateRange(league.start, league.end),
    create: (league) =>
      eventRow(league.code, league.name, dateRange(league.start, league.end), this.createDrawer(league.code)),
  });

  readonly list = this.list_.state;
  readonly rows = this.list_.rows;
  readonly revealed = this.list_.revealed;
  readonly joinQuestion = this.joinQuestion_.asReadonly();
  /** A join is being sent. */
  readonly joining = this.joining_.asReadonly();
  /** The code of each league the viewer joined (joinCallback). */
  readonly joined$: Observable<string> = this.joined.asObservable();

  /** loadList: fetches the list and shows it afresh (EventList.load). */
  loadList(): Promise<void> {
    return this.list_.load();
  }

  /** refreshList: updates the list in place (EventList.refresh). */
  refreshList(): void {
    this.list_.refresh();
  }

  /** showLeague (#league-<code>): opens that league's row, loading the list first if need be, and reveals it. */
  showLeague(code: string): Promise<void> {
    return this.list_.show(code);
  }

  /**
   * A drawer showing a league's detail (createLeagueRow's). `fallback`: a failed fetch shows the drawer's fallback (the
   * calendar's facts) instead of an error.
   */
  createDrawer(code: string, fallback = false): EventDrawer<LeagueDetail> {
    const drawer = new EventDrawer<LeagueDetail>({
      load: () => this.api.league(code),
      failure: (error) => leagueFailed(shownStatus(error), eventErrorMessage(error)),
      unreadableText: LEAGUE_NOT_DISPLAYED,
      emptyText: NO_LEAGUE_DETAILS,
      fallback,
    });
    const list = (this.drawers.get(code) ?? []).filter((known) => known.alive);
    list.push(drawer);
    this.drawers.set(code, list);
    return drawer;
  }

  /** "Join league": asks whether to pay the league's cost. */
  requestJoin(league: LeagueDetail): void {
    this.joinQuestion_.set({ code: league.code, cost: league.cost });
  }

  /**
   * The answer to the join question. Yes joins (POST /league/{code}); then every live drawer of that league is
   * fetched again where it is, and `joined$` tells the code. Refused for want of money (409): "You don't have
   * enough funds to join this league."; any other failure says what went wrong.
   */
  async answerJoin(yes: boolean): Promise<void> {
    const question = this.joinQuestion_();
    this.joinQuestion_.set(null);
    if (!yes || question === null || this.joining_()) {
      return;
    }
    this.joining_.set(true);
    try {
      await firstValueFrom(this.api.joinLeague(question.code));
    } catch (error) {
      const failure = ApiError.from(error);
      await this.injector.get(Notifier).alert(failure.is(409) ? NOT_ENOUGH_FUNDS : eventErrorMessage(failure));
      return;
    } finally {
      this.joining_.set(false);
    }
    this.leagueJoined(question.code);
    this.joined.next(question.code);
  }

  /** leagueJoined: each drawer made for that league that is still shown is fetched again, open or shut. */
  private leagueJoined(code: string): void {
    const live = (this.drawers.get(code) ?? []).filter((drawer) => drawer.alive);
    this.drawers.set(code, live);
    for (const drawer of live) {
      drawer.refresh();
    }
  }
}
