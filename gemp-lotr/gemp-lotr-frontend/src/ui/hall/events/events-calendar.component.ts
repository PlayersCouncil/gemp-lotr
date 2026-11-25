import { Component, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CalendarBar } from '../../../core/hall/events/calendar-grid';
import { EVENTS_QUEUE_JOINER } from '../../../core/hall/events/events-hooks';
import * as words from '../../../core/hall/events/events-messages';
import { CalendarQueueOffer, LeagueDetail, TournamentDetail } from '../../../core/hall/events/events-models';
import { eventsCommands } from '../../../core/hall/events/events-routes';
import { EventsTab } from '../../../core/hall/events/events-tab.service';
import { CalendarFactsComponent } from './calendar-facts.component';
import { EventRowComponent } from './event-row.component';
import { EventsButtonComponent } from './events-button.component';
import { LeagueDetailComponent } from './league-detail.component';
import { TournamentDetailComponent } from './tournament-detail.component';

/**
 * Events › Calendar (events.html #calendar, calendarUi.js EventCalendarUI): the intro, the month's header ("<", the
 * month, ">", Today, the legend), the grid of weeks with the events' bars, a rule, and the details: a placeholder
 * until an event is clicked, then its preview row with its drawer open ("Go to League" / "Go to Tournament" opens the
 * list's sub-tab with that event's drawer open). A scheduled tournament whose hall queue is open gets the hall's
 * Join Queue where the hall provides it (EVENTS_QUEUE_JOINER).
 *
 * DOM contract: `gd-events-calendar.gd-events-calendar` holding `p.gd-events-calendar__intro`, then either
 * `div.gd-events-calendar__error` or `div.gd-events-calendar__header` (`button.gd-events-button[data-action=previous|
 * next|today]`, `span.gd-events-calendar__title`, `span.gd-events-calendar__legend` of `span.gd-events-chip[data-kind]`)
 * and `div.gd-events-calendar__grid` (`div.gd-events-calendar__weekdays`, a `div.gd-events-calendar__week` per week
 * (`--gd-lanes`, `data-lanes`) with `div.gd-events-calendar__day` backgrounds and numbers (`data-other-month`,
 * `data-today`) and a `button.gd-events-calendar__bar` per bar (`data-kind`, `data-joined`, `data-error`,
 * `data-selected`, `data-continues-before`, `data-continues-after`, `title`, `--gd-column`, `--gd-span`, `--gd-lane`)
 * of `span.gd-events-calendar__part`s (`data-serie-start`, `--gd-share`)); then `hr.gd-events__rule` and
 * `div.gd-events-calendar__details` holding `div.gd-events-calendar__placeholder` or the preview's `gd-event-row`.
 */
@Component({
  selector: 'gd-events-calendar',
  styleUrl: './events-calendar.component.css',
  imports: [
    CalendarFactsComponent,
    EventRowComponent,
    EventsButtonComponent,
    LeagueDetailComponent,
    TournamentDetailComponent,
  ],
  host: { class: 'gd-events-calendar' },
  templateUrl: './events-calendar.component.html',
})
export class EventsCalendarComponent {
  protected readonly calendar = inject(EventsTab).calendar;
  private readonly router = inject(Router);
  private readonly joiner = inject(EVENTS_QUEUE_JOINER, { optional: true });

  protected readonly words = words;
  protected readonly canJoinQueue = this.joiner !== null;
  protected readonly error = computed(() => {
    const state = this.calendar.state();
    return state.kind === 'error' ? state.text : null;
  });

  protected asLeague(data: unknown): LeagueDetail {
    return data as LeagueDetail;
  }

  protected asTournament(data: unknown): TournamentDetail {
    return data as TournamentDetail;
  }

  protected select(bar: CalendarBar): void {
    this.calendar.select(bar.event);
  }

  protected go(): void {
    const selection = this.calendar.selection();
    const link = selection ? this.calendar.goTo(selection.preview.event) : null;
    if (link) {
      void this.router.navigate(['/', ...eventsCommands(link)]);
    }
  }

  protected joinQueue(offer: CalendarQueueOffer): void {
    this.joiner?.joinQueue(offer);
  }
}
