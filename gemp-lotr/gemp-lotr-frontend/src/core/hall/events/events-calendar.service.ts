import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiError } from '../../api/api-error';
import { Timers } from '../../platform/timers';
import { calendarPreview, CalendarPreview } from './calendar-details';
import {
  buildCalendarGrid,
  CalendarGrid,
  gridRange,
  isoDate,
  sameCalendarEvent,
  shiftMonth,
  utcMonthOf,
  YearMonth,
} from './calendar-grid';
import { EventDrawer } from './event-drawer';
import { EventsApi } from './events-api.service';
import { CALENDAR_BAD_REQUEST, CALENDAR_LOGGED_OUT, eventErrorMessage } from './events-messages';
import { CalendarEvent, LeagueDetail, TournamentDetail } from './events-models';
import { EventsLink, leagueLink, tournamentLink } from './events-routes';
import { LeagueResults } from './league-results.service';
import { TournamentResults } from './tournament-results.service';

/** The calendar's state: nothing yet, a month shown, or the message in its place. */
export type CalendarState =
  | { readonly kind: 'empty' }
  | { readonly kind: 'loaded'; readonly month: YearMonth; readonly events: readonly CalendarEvent[] }
  | { readonly kind: 'error'; readonly text: string };

/** The details panel: the preview of the event clicked, and its drawer (none for the facts alone). */
export interface CalendarSelection {
  readonly preview: CalendarPreview;
  readonly drawer: EventDrawer<LeagueDetail> | EventDrawer<TournamentDetail> | null;
}

/**
 * The Events tab's Calendar (calendarUi.js EventCalendarUI with details, leagueUI and tournamentUI): a month grid of
 * leagues, scheduled tournaments and (for event admins) projected leagues, from GET /calendar. Clicking an event
 * shows its preview under the grid: the row the Current Leagues / Tournaments lists use with its drawer open,
 * holding the league's or tournament's detail (its facts where that cannot be had), and "Go to League" /
 * "Go to Tournament" in place of + Details.
 */
@Injectable()
export class EventsCalendar {
  private readonly api = inject(EventsApi);
  private readonly timers = inject(Timers);
  private readonly leagues = inject(LeagueResults);
  private readonly tournaments = inject(TournamentResults);

  private readonly month_ = signal<YearMonth>(utcMonthOf(this.timers.now()));
  private readonly state_ = signal<CalendarState>({ kind: 'empty' });
  private readonly selection_ = signal<CalendarSelection | null>(null);
  /** Bumped by every load: an answer for an older one is dropped. */
  private generation = 0;

  /** The month asked for (the title follows what is shown: `grid`). */
  readonly month = this.month_.asReadonly();
  readonly state = this.state_.asReadonly();
  readonly selection = this.selection_.asReadonly();
  /** The month shown, as its weeks, days and bars; null before the first load and in place of an error. */
  readonly grid = computed<CalendarGrid | null>(() => {
    const state = this.state_();
    return state.kind === 'loaded'
      ? buildCalendarGrid(state.month, state.events, isoDate(new Date(this.timers.now())))
      : null;
  });

  /** show(): the current month, or the one given. */
  show(month?: YearMonth): Promise<boolean> {
    if (month) {
      this.month_.set(month);
    }
    return this.load();
  }

  previousMonth(): Promise<boolean> {
    this.month_.set(shiftMonth(this.month_(), -1));
    return this.load();
  }

  nextMonth(): Promise<boolean> {
    this.month_.set(shiftMonth(this.month_(), 1));
    return this.load();
  }

  /** "Today": the current UTC month. */
  today(): Promise<boolean> {
    return this.show(utcMonthOf(this.timers.now()));
  }

  /**
   * load: fetches the month asked for and shows it; true once shown. A failure shows its message in the grid's place:
   * 400 "Could not load the calendar.", 401 "You must be logged in to see the calendar.", otherwise what went wrong.
   */
  async load(): Promise<boolean> {
    const month = this.month_();
    const range = gridRange(month);
    const generation = ++this.generation;
    try {
      const answer = await firstValueFrom(this.api.calendar(range.from, range.to));
      if (generation !== this.generation) {
        return false;
      }
      this.state_.set({ kind: 'loaded', month, events: answer.events });
      return true;
    } catch (error) {
      if (generation === this.generation) {
        this.state_.set({ kind: 'error', text: calendarError(ApiError.from(error)) });
      }
      return false;
    }
  }

  /**
   * refresh (the viewer came back): the month shown is fetched again; the event selected stays selected. Its preview
   * is built again if the event changed (joined, started, its queue opened...), otherwise its open drawer is fetched
   * again quietly where it is.
   */
  async refresh(): Promise<void> {
    if (!(await this.load())) {
      return;
    }
    const selection = this.selection_();
    const state = this.state_();
    if (selection === null || state.kind !== 'loaded') {
      return;
    }
    const old = selection.preview.event;
    const fresh = state.events.find((event) => sameCalendarEvent(event, old));
    if (fresh !== undefined && JSON.stringify(fresh) !== JSON.stringify(old)) {
      this.select(fresh);
      return;
    }
    if (selection.drawer?.isOpen()) {
      selection.drawer.refresh(true);
    }
  }

  /** eventClicked: the event's preview replaces the panel's content, its drawer open. */
  select(event: CalendarEvent): void {
    this.selection_()?.drawer?.destroy();
    const preview = calendarPreview(event);
    let drawer: CalendarSelection['drawer'] = null;
    if (preview.body.kind === 'league') {
      drawer = this.leagues.createDrawer(preview.body.code, true);
    } else if (event.kind === 'tournament') {
      // a scheduled tournament that has not started has no tournament to fetch
      drawer = this.tournaments.createDrawer(event.id, { fallback: true, fetch: event.started });
    }
    drawer?.open();
    this.selection_.set({ preview, drawer });
  }

  /** The bar of the event selected is marked (kind and id). */
  isSelected(event: CalendarEvent): boolean {
    const selected = this.selection_()?.preview.event;
    return selected?.kind === event.kind && selected.id === event.id;
  }

  /** "Go to League" / "Go to Tournament": where it leads (the list's sub-tab, that event's drawer open). */
  goTo(event: CalendarEvent): EventsLink | null {
    if (event.kind === 'league') {
      return leagueLink(event.id);
    }
    return event.kind === 'tournament' ? tournamentLink(event.id) : null;
  }
}

function calendarError(error: ApiError): string {
  if (error.is(400)) {
    return CALENDAR_BAD_REQUEST;
  }
  if (error.is(401)) {
    return CALENDAR_LOGGED_OUT;
  }
  return `${CALENDAR_BAD_REQUEST} ${eventErrorMessage(error)}`;
}
