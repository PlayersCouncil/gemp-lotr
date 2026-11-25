import { Component, computed, ElementRef, input, viewChild } from '@angular/core';
import { EventHistoryBrowser } from '../../../core/hall/events/event-history';
import { orDash } from '../../../core/hall/events/event-months';
import * as words from '../../../core/hall/events/events-messages';
import { CompletedEvent, LeagueDetail, TournamentDetail } from '../../../core/hall/events/events-models';
import { EventsButtonComponent } from './events-button.component';
import { LeagueDetailComponent } from './league-detail.component';
import { TournamentDetailComponent } from './tournament-detail.component';

/**
 * A completed-events panel (eventHistoryUi.js EventHistoryUI with its MonthNav), under Current Leagues or Current
 * Tournaments: the heading, "<", the month (a button opening the dropdown of months with events, grouped by year;
 * Escape or leaving it closes it) and ">", the status line (with Retry after a failure), and the month's table, each
 * row's + Details opening the league's or tournament's detail in a row beneath it. An admin (the answer says so)
 * gets the Admin column.
 *
 * Inputs: `browser` (required).
 *
 * DOM contract: `gd-event-history.gd-event-history` (`data-kind`) holding `div.gd-event-history__heading`,
 * `div.gd-event-history__header` (`button.gd-events-button[data-action=previous|next]`, `span.gd-event-history__month`
 * with `button.gd-event-history__title` (`aria-expanded`, `aria-haspopup`) and, open, `div.gd-event-history__dropdown`
 * (`div.gd-event-history__dd-status` with a Retry button, or `div.gd-event-history__year` groups of
 * `button.gd-event-history__dd-month` (`data-month`, `aria-current`))), `div.gd-event-history__status` (`data-state`)
 * and `table.gd-event-history__table` with `tr.gd-event-history__row` (`data-expanded`) and
 * `tr.gd-event-history__detail-row` per event.
 */
@Component({
  selector: 'gd-event-history',
  styleUrl: './event-history.component.css',
  imports: [EventsButtonComponent, LeagueDetailComponent, TournamentDetailComponent],
  host: {
    class: 'gd-event-history',
    '[attr.data-kind]': 'browser().kind',
    '(focusout)': 'focusOut($event)',
    '(keydown.escape)': 'escape($event)',
  },
  templateUrl: './event-history.component.html',
})
export class EventHistoryComponent {
  readonly browser = input.required<EventHistoryBrowser>();
  private readonly month = viewChild<ElementRef<HTMLElement>>('month');

  protected readonly words = words;
  protected readonly orDash = orDash;
  protected readonly columns = computed(() =>
    words.historyColumns(this.browser().kind, this.browser().shown()?.isAdmin ?? false),
  );

  protected dates(event: CompletedEvent): string {
    return this.browser().kind === 'league'
      ? `${orDash(event.startDate)}${words.DATE_DASH}${orDash(event.endDate)}`
      : orDash(event.startDate);
  }

  /**
   * A drawer's details as the kind this panel shows. The browser's kind is what made the drawer
   * (EventHistoryBrowser.drawer), so the two cannot differ; the types alone do not say so.
   */
  protected asLeague(data: LeagueDetail | TournamentDetail): LeagueDetail {
    return data as LeagueDetail;
  }

  protected asTournament(data: LeagueDetail | TournamentDetail): TournamentDetail {
    return data as TournamentDetail;
  }

  /** The dropdown closes when the focus leaves it and its title (MonthNav closed on a press outside). */
  protected focusOut(event: FocusEvent): void {
    const month = this.month()?.nativeElement;
    const next = event.relatedTarget as Node | null;
    if (month?.contains(event.target as Node) && (next === null || !month.contains(next))) {
      this.browser().closeDropdown();
    }
  }

  /** Escape closes it and gives the focus back to the title. */
  protected escape(event: Event): void {
    const month = this.month()?.nativeElement;
    if (this.browser().dropdownOpen() && month?.contains(event.target as Node)) {
      event.preventDefault();
      this.browser().closeDropdown();
      month.querySelector<HTMLElement>('.gd-event-history__title')?.focus();
    }
  }

  protected pick(key: string, title: HTMLElement): void {
    this.browser().pick(key);
    title.focus();
  }
}
