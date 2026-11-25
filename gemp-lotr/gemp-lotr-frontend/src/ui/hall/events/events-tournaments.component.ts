import { afterRenderEffect, Component, computed, DestroyRef, ElementRef, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import {
  LISTS_INTRO,
  NO_RUNNING_TOURNAMENTS,
  TOURNAMENT_REPORT_TITLE,
} from '../../../core/hall/events/events-messages';
import { TournamentDetail } from '../../../core/hall/events/events-models';
import { tournamentReportUrl } from '../../../core/hall/events/events-parsers';
import { parseEventsRoute, TOURNAMENT_ID_PARAM } from '../../../core/hall/events/events-routes';
import { EventsTab } from '../../../core/hall/events/events-tab.service';
import { hasPlayerType, PlayerType } from '../../../core/session/player';
import { PlayerInfo } from '../../../core/session/player-info.service';
import { EventHistoryComponent } from './event-history.component';
import { EventRowComponent } from './event-row.component';
import { revealRow } from './events-view';
import { TournamentDetailComponent } from './tournament-detail.component';

/**
 * Events › Current Tournaments (events.html #tournaments, tournamentResultsUi.js in list mode): the intro, a row per
 * live tournament with its drawer, a rule, and Completed tournaments. `events/tournaments/<id>` opens that
 * tournament's drawer. Admins and league admins see each tournament's id as a link to its report (a page the server
 * draws, in a new tab).
 *
 * DOM contract: `gd-events-tournaments.gd-events-tournaments` holding `p.gd-events__intro`, `div.gd-events__list` (a
 * `gd-event-row[data-kind=tournament]` per tournament, with `span.gd-events__code` > `a` (`target=_blank`) for admins;
 * or `i.gd-events__empty`, or `div.gd-events__error`), `hr.gd-events__rule`, `gd-event-history[data-kind=tournament]`.
 */
@Component({
  selector: 'gd-events-tournaments',
  styleUrl: './events-lists.component.css',
  imports: [EventHistoryComponent, EventRowComponent, TournamentDetailComponent],
  host: { class: 'gd-events-tournaments' },
  template: `
    <p class="gd-events__intro">{{ intro }}</p>
    <div class="gd-events__list">
      @switch (tournaments.list().kind) {
        @case ('error') {
          <div class="gd-events__error">{{ error() }}</div>
        }
        @case ('loaded') {
          @for (row of tournaments.rows(); track row.key) {
            <gd-event-row
              kind="tournament"
              [key]="row.key"
              [drawer]="row.drawer"
              [title]="row.title()"
              [date]="row.date()"
            >
              @if (admin() && row.key !== '') {
                <span class="gd-events__code" gdRowExtras
                  ><a target="_blank" [href]="report(row.key)" [title]="reportTitle">{{ row.key }}</a></span
                >
              }
              @if (row.drawer.content(); as content) {
                @if (content.kind === 'data') {
                  <gd-tournament-detail [tournament]="asTournament(content.data)" />
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
    <gd-event-history [browser]="tab.tournamentHistory" />
  `,
})
export class EventsTournamentsComponent {
  protected readonly tab = inject(EventsTab);
  protected readonly tournaments = this.tab.tournaments;
  private readonly player = inject(PlayerInfo);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  protected readonly intro = LISTS_INTRO;
  protected readonly empty = NO_RUNNING_TOURNAMENTS;
  protected readonly reportTitle = TOURNAMENT_REPORT_TITLE;
  protected readonly admin = computed(() => {
    const player = this.player.player();
    return hasPlayerType(player, PlayerType.admin) || hasPlayerType(player, PlayerType.leagueAdmin);
  });
  protected readonly error = computed(() => {
    const state = this.tournaments.list();
    return state.kind === 'error' ? state.text : null;
  });

  constructor() {
    const params = inject(ActivatedRoute).paramMap.subscribe((map) => {
      const link = parseEventsRoute('tournaments', map.get(TOURNAMENT_ID_PARAM));
      if (link) {
        this.tab.open(link);
      }
    });
    inject(DestroyRef).onDestroy(() => params.unsubscribe());
    afterRenderEffect(() => revealRow(this.host, this.tournaments.revealed()));
  }

  protected report(id: string): string {
    return tournamentReportUrl(id);
  }

  protected asTournament(data: unknown): TournamentDetail {
    return data as TournamentDetail;
  }
}
