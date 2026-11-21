import { afterRenderEffect, Component, computed, DestroyRef, ElementRef, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LEAGUE_CODE_TITLE, LISTS_INTRO, NO_CURRENT_LEAGUES } from '../../../core/hall/events/events-messages';
import { LeagueDetail } from '../../../core/hall/events/events-models';
import { LEAGUE_CODE_PARAM, parseEventsRoute } from '../../../core/hall/events/events-routes';
import { EventsTab } from '../../../core/hall/events/events-tab.service';
import { hasPlayerType, PlayerType } from '../../../core/session/player';
import { PlayerInfo } from '../../../core/session/player-info.service';
import { EventHistoryComponent } from './event-history.component';
import { EventRowComponent } from './event-row.component';
import { LeagueDetailComponent } from './league-detail.component';
import { revealRow } from './events-view';

/**
 * Events › Current Leagues (events.html #leagues, leagueResultsUi.js in list mode): the intro, a row per current
 * league with its drawer (several may be open), a rule, and Completed leagues. `events/leagues/<code>` opens that
 * league's drawer and scrolls it into view. League admins see each league's code (a click selects it: `user-select: all`).
 *
 * DOM contract: `gd-events-leagues.gd-events-leagues` holding `p.gd-events__intro`, `div.gd-events__list` (a
 * `gd-event-row[data-kind=league]` per league, with `span.gd-events__code` for league admins; or
 * `i.gd-events__empty`, or `div.gd-events__error`), `hr.gd-events__rule`, `gd-event-history[data-kind=league]`.
 */
@Component({
  selector: 'gd-events-leagues',
  styleUrl: './events-lists.component.css',
  imports: [EventHistoryComponent, EventRowComponent, LeagueDetailComponent],
  host: { class: 'gd-events-leagues' },
  template: `
    <p class="gd-events__intro">{{ intro }}</p>
    <div class="gd-events__list">
      @switch (leagues.list().kind) {
        @case ('error') {
          <div class="gd-events__error">{{ error() }}</div>
        }
        @case ('loaded') {
          @for (row of leagues.rows(); track row.key) {
            <gd-event-row kind="league" [key]="row.key" [drawer]="row.drawer" [title]="row.title()" [date]="row.date()">
              @if (leagueAdmin()) {
                <span class="gd-events__code" gdRowExtras [title]="codeTitle">{{ row.key }}</span>
              }
              @if (row.drawer.content(); as content) {
                @if (content.kind === 'data') {
                  <gd-league-detail [league]="asLeague(content.data)" />
                }
              }
            </gd-event-row>
          } @empty {
            <i class="gd-events__empty">{{ empty }}</i>
          }
        }
      }
    </div>
    <hr class="gd-events__rule" />
    <gd-event-history [browser]="tab.leagueHistory" />
  `,
})
export class EventsLeaguesComponent {
  protected readonly tab = inject(EventsTab);
  protected readonly leagues = this.tab.leagues;
  private readonly player = inject(PlayerInfo);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  protected readonly intro = LISTS_INTRO;
  protected readonly empty = NO_CURRENT_LEAGUES;
  protected readonly codeTitle = LEAGUE_CODE_TITLE;
  protected readonly leagueAdmin = computed(() => hasPlayerType(this.player.player(), PlayerType.leagueAdmin));
  protected readonly error = computed(() => {
    const state = this.leagues.list();
    return state.kind === 'error' ? state.text : null;
  });

  constructor() {
    const params = inject(ActivatedRoute).paramMap.subscribe((map) => {
      const link = parseEventsRoute('leagues', map.get(LEAGUE_CODE_PARAM));
      if (link) {
        this.tab.open(link);
      }
    });
    inject(DestroyRef).onDestroy(() => params.unsubscribe());
    afterRenderEffect(() => revealRow(this.host, this.leagues.revealed()));
  }

  protected asLeague(data: unknown): LeagueDetail {
    return data as LeagueDetail;
  }
}
