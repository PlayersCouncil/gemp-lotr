import { Component, DestroyRef, inject, Injectable } from '@angular/core';
import { CardActions } from '../../../core/cards/card-actions';
import { provideCardInteractions } from '../../../core/cards/card-interactions';
import * as words from '../../../core/hall/events/events-messages';
import { EventsTab } from '../../../core/hall/events/events-tab.service';
import { EventsSubTab } from '../../../core/hall/events/events-routes';
import { LeagueResults } from '../../../core/hall/events/league-results.service';
import { CardInfoComponent } from '../../shared/card-info/card-info.component';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { HallTabFrameComponent } from '../shared/hall-tab-frame.component';
import { EventsButtonComponent } from './events-button.component';
import { PriceComponent } from '../shared/price.component';

/** The cookie the Events tab would keep a hover zoom setting in (it shows no hover zoom; the card helpers need one). */
export const EVENTS_AUTO_ZOOM_COOKIE = 'eventsAutoZoom';

/** Clicking a card on the Events tab (an RTMD meta-site, a format's card) only shows it. */
@Injectable()
export class EventsCardActions extends CardActions {
  activate(): void {
    // the card buttons show the card themselves
  }
}

/**
 * The Events tab (includes/events.html): its sub-tabs (Calendar, Current Leagues, Current Tournaments) and the
 * sub-tab's page in the hall's tab frame (its page laid out as the sub-tabs' .article). It starts the tab's data on
 * its first showing, and passes on which sub-tab is showing and whether the page is in front of the viewer (hidden,
 * blurred), so a sub-tab the viewer comes back to after 10 s is fetched again. It holds what every sub-tab shares: the
 * join question ("League operation") and the card information.
 *
 * DOM contract: `gd-events.gd-events` (`display: contents`; `data-sub-tab`) holding `gd-hall-tab-frame[data-tab=events]`
 * with, after the page, `gd-dialog[data-dialog=league-join]` (`p.gd-events__join-question` with `gd-price`, then
 * `button.gd-events-button[data-action=yes|no]`) and `gd-card-info`.
 */
@Component({
  selector: 'gd-events',
  styleUrl: './events-tab.component.css',
  imports: [CardInfoComponent, DialogComponent, EventsButtonComponent, HallTabFrameComponent, PriceComponent],
  providers: [provideCardInteractions({ actions: EventsCardActions, autoZoomCookie: EVENTS_AUTO_ZOOM_COOKIE })],
  host: {
    style: 'display: contents',
    class: 'gd-events',
    '[attr.data-sub-tab]': 'sub',
  },
  template: `
    <gd-hall-tab-frame
      tab="events"
      page="article"
      (subTabChange)="subTabShown($event)"
      (pageShownChange)="tab.returns.setPageShown($event)"
    >
      <gd-dialog
        name="league-join"
        [heading]="words.JOIN_DIALOG_TITLE"
        [open]="leagues.joinQuestion() !== null"
        (closed)="leagues.answerJoin(false)"
      >
        @if (leagues.joinQuestion(); as question) {
          <p class="gd-events__join-question">
            {{ words.JOIN_QUESTION_BEFORE_PRICE }}<gd-price [price]="question.cost" />{{
              words.JOIN_QUESTION_AFTER_PRICE
            }}
          </p>
        }
        <div class="gd-dialog__actions" gdDialogActions>
          <button gdEventsButton data-action="yes" (click)="leagues.answerJoin(true)">{{ words.JOIN_YES }}</button>
          <button gdEventsButton data-action="no" (click)="leagues.answerJoin(false)">{{ words.JOIN_NO }}</button>
        </div>
      </gd-dialog>
      <gd-card-info />
    </gd-hall-tab-frame>
  `,
})
export class EventsTabComponent {
  protected readonly tab = inject(EventsTab);
  protected readonly leagues = inject(LeagueResults);
  protected readonly words = words;
  protected sub: EventsSubTab | null = null;

  constructor() {
    this.tab.start();
    inject(DestroyRef).onDestroy(() => this.tab.subTabShown(null));
  }

  /** The sub-tab of the address (/events/<sub>/...). */
  protected subTabShown(slug: string | null): void {
    this.sub = slug === 'calendar' || slug === 'leagues' || slug === 'tournaments' ? slug : null;
    this.tab.subTabShown(this.sub);
  }
}
