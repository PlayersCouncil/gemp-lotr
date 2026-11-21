import { Component, computed, input } from '@angular/core';
import { TournamentDetail } from '../../../core/hall/events/events-models';
import {
  COLLECTION_LABEL,
  FORMAT_LABEL,
  ROUND_LABEL,
  ROUNDS_LABEL,
  TOURNAMENT_STANDING_HEADERS,
} from '../../../core/hall/events/events-messages';
import { tournamentLines } from '../../../core/hall/events/league-view';

/**
 * A tournament's detail in a drawer (tournamentResultsUi.js loadedTournament): "Rounds:" first where asked, Format,
 * Collection, "Round:" while games are played, and the standings; once finished each player's name opens their deck
 * (a page the server draws) in a new tab.
 *
 * Inputs: `tournament` (required), `showRounds` (default true; Completed tournaments leave it out).
 *
 * DOM contract: `gd-tournament-detail.gd-tournament-detail` (`data-context=history` without the Rounds line: it then
 * takes the completed-events table's cell look) holding `div.gd-tournament-detail__line` (`data-line` =
 * rounds, format, collection, round) and `table.gd-tournament-detail__standings`, a finished tournament's players
 * `a.gd-tournament-detail__deck` (`target=_blank`).
 */
@Component({
  selector: 'gd-tournament-detail',
  styleUrl: './tournament-detail.component.css',
  host: { class: 'gd-tournament-detail', '[attr.data-context]': "showRounds() ? null : 'history'" },
  template: `
    @if (lines().rounds !== null) {
      <div class="gd-tournament-detail__line" data-line="rounds">
        <b>{{ words.rounds }}</b> {{ lines().rounds }}
      </div>
    }
    <div class="gd-tournament-detail__line" data-line="format">
      <b>{{ words.format }}</b> {{ lines().format }}
    </div>
    <div class="gd-tournament-detail__line" data-line="collection">
      <b>{{ words.collection }}</b> {{ lines().collection }}
    </div>
    @if (lines().round !== null) {
      <div class="gd-tournament-detail__line" data-line="round">
        <b>{{ words.round }}</b> {{ lines().round }}
      </div>
    }
    @if (tournament().standings.length > 0) {
      <table class="gd-tournament-detail__standings">
        <tr>
          @for (header of headers; track header) {
            <th>{{ header }}</th>
          }
        </tr>
        @for (standing of tournament().standings; track $index) {
          <tr>
            <td>{{ standing.standing }}</td>
            <td>
              @if (standing.deckUrl; as url) {
                <a class="gd-tournament-detail__deck" [href]="url" target="_blank">{{ standing.player }}</a>
              } @else {
                {{ standing.player }}
              }
            </td>
            <td>{{ standing.points }}</td>
            <td>{{ standing.gamesPlayed }}</td>
            <td>{{ standing.medianScore }}</td>
            <td>{{ standing.cumulativeScore }}</td>
            <td>{{ standing.opponentWin }}</td>
          </tr>
        }
      </table>
    }
  `,
})
export class TournamentDetailComponent {
  readonly tournament = input.required<TournamentDetail>();
  readonly showRounds = input(true);

  protected readonly lines = computed(() => tournamentLines(this.tournament(), this.showRounds()));
  protected readonly headers = TOURNAMENT_STANDING_HEADERS;
  protected readonly words = {
    rounds: ROUNDS_LABEL,
    format: FORMAT_LABEL,
    collection: COLLECTION_LABEL,
    round: ROUND_LABEL,
  };
}
