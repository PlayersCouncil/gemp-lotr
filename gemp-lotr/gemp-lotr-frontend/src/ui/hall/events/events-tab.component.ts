import { DOCUMENT } from '@angular/common';
import { Component, DestroyRef, inject, Injectable } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { CardActions } from '../../../core/cards/card-actions';
import { provideCardInteractions } from '../../../core/cards/card-interactions';
import { findHallTab, HallTab } from '../../../core/hall/hall-tabs';
import * as words from '../../../core/hall/events/events-messages';
import { EventsTab } from '../../../core/hall/events/events-tab.service';
import { EventsSubTab } from '../../../core/hall/events/events-routes';
import { LeagueResults } from '../../../core/hall/events/league-results.service';
import { CardInfoComponent } from '../../shared/card-info/card-info.component';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { HallSubTabsComponent } from '../hall-sub-tabs.component';
import { EventsButtonComponent } from './events-button.component';
import { PriceComponent } from './price.component';

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
 * The Events tab (includes/events.html): its sub-tabs (Calendar, Current Leagues, Current Tournaments) at the left and
 * the sub-tab's page beside them, scrolling. It starts the tab's data on its first showing, and reports which sub-tab
 * is showing and whether the page is in front of the viewer (hidden, blurred), so a sub-tab the viewer comes back to
 * after 10 s is fetched again. It holds what every sub-tab shares: the join question ("League operation") and the
 * card information.
 *
 * DOM contract: `gd-events.gd-events` (`data-sub-tab`) holding `gd-hall-sub-tabs`, `div.gd-events__page` (the
 * sub-tab's page), `gd-dialog[data-dialog=league-join]` (`p.gd-events__join-question` with `gd-price`, then
 * `button.gd-events-button[data-action=yes|no]`) and `gd-card-info`.
 */
@Component({
  selector: 'gd-events',
  styleUrl: './events-tab.component.css',
  imports: [
    CardInfoComponent,
    DialogComponent,
    EventsButtonComponent,
    HallSubTabsComponent,
    PriceComponent,
    RouterOutlet,
  ],
  providers: [provideCardInteractions({ actions: EventsCardActions, autoZoomCookie: EVENTS_AUTO_ZOOM_COOKIE })],
  host: {
    class: 'gd-events',
    '[attr.data-sub-tab]': 'sub',
    '(document:visibilitychange)': 'pageChanged()',
    '(window:blur)': 'blurred = true; pageChanged()',
    '(window:focus)': 'blurred = false; pageChanged()',
  },
  template: `
    <gd-hall-sub-tabs [tabs]="hallTab.subTabs" [base]="'/' + hallTab.path" [label]="hallTab.label" />
    <div class="gd-events__page"><router-outlet /></div>
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
  `,
})
export class EventsTabComponent {
  private readonly tab = inject(EventsTab);
  private readonly router = inject(Router);
  private readonly document = inject(DOCUMENT);
  protected readonly leagues = inject(LeagueResults);
  protected readonly words = words;
  protected readonly hallTab: HallTab = eventsHallTab();
  protected sub: EventsSubTab | null = null;
  protected blurred = false;

  constructor() {
    this.tab.start();
    this.pageChanged();
    this.followRoute();
    const routes = this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => this.followRoute());
    inject(DestroyRef).onDestroy(() => {
      routes.unsubscribe();
      this.tab.subTabShown(null);
    });
  }

  /** GempHallTabs.watch: the browser tab hidden, or the window blurred, counts as being away. */
  protected pageChanged(): void {
    this.tab.returns.setPageShown(this.document.visibilityState !== 'hidden' && !this.blurred);
  }

  /** The sub-tab of the address (/events/<sub>/...). */
  private followRoute(): void {
    const segments = this.router.parseUrl(this.router.url).root.children['primary']?.segments ?? [];
    const sub = segments[1]?.path;
    this.sub = sub === 'calendar' || sub === 'leagues' || sub === 'tournaments' ? sub : null;
    this.tab.subTabShown(this.sub);
  }
}

function eventsHallTab(): HallTab {
  const tab = findHallTab('events');
  if (tab === null) {
    throw new Error('hall-tabs.ts has no Events tab');
  }
  return tab;
}
