import { Component, computed, inject } from '@angular/core';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { clockText } from './game-clock';

interface PlayerView {
  index: number;
  name: string;
  current: boolean;
  own: boolean;
  clock: string | null;
  /** The band of the player's own clock: under 10 minutes, under 5. */
  clockBand: 'warn' | 'danger' | null;
  deck: string;
  hand: string;
  handRevealed: boolean;
  threats: string;
  discard: string;
  dead: string;
  adventureDeck: string;
  removed: string;
  /** The piles the page's player may look into: their counter opens them. */
  piles: { deck: boolean; discard: boolean; dead: boolean; adventureDeck: boolean; removed: boolean };
}

const shown = (value: number | null | undefined) => (value === null || value === undefined ? '' : String(value));

/**
 * The game state panel: each player with their clock and counters (deck, hand, threats; "+" shows the discard pile,
 * dead pile, adventure deck and removed pile too), the twilight pool, the phase, the decision's clock, who has
 * initiative and the Rule of 4 count.
 *
 * DOM contract: `gd-game-state` holding `.gd-game-player` per player (`data-current`, `data-open`), in it the name,
 * `[data-clock]`, and the counters `[data-stat]` named as the legacy ids (deck0, hand0, threats0, showStats0,
 * discard0, deadPile0, adventureDeck0, removedPile0; piles also `data-pile` and `data-player-index`),
 * `[data-stats-toggle]`; then `[data-game]` twilight, phase, initiative, ruleof4 and the decision's `[data-clock]`.
 */
@Component({
  selector: 'gd-game-state',
  styleUrl: './game-state-panel.component.css',
  template: `
    @for (player of players(); track player.index) {
      <div
        class="gd-game-player"
        [attr.data-current]="player.current ? '' : null"
        [attr.data-open]="open().has(player.index) ? '' : null"
      >
        {{ player.index + 1 }}. {{ player.name
        }}<span
          class="gd-game-player__clock"
          data-clock
          [attr.data-own]="player.own ? '' : null"
          [attr.data-band]="player.own ? player.clockBand : null"
          >{{ player.clock }}</span
        >
        <div class="gd-game-player__stats">
          <span
            class="gd-game-counter"
            data-counter="deck"
            [attr.data-stat]="'deck' + player.index"
            [attr.data-pile]="player.piles.deck ? 'deck' : null"
            [attr.data-player-index]="player.piles.deck ? player.index : null"
            [attr.data-clickable]="player.piles.deck ? '' : null"
            >{{ player.deck }}</span
          >
          <span
            class="gd-game-counter"
            data-counter="hand"
            [attr.data-stat]="'hand' + player.index"
            [attr.data-revealed]="player.handRevealed ? '' : null"
            >{{ player.hand }}</span
          >
          <span class="gd-game-counter" data-counter="threats" [attr.data-stat]="'threats' + player.index">{{
            player.threats
          }}</span>
          <span class="gd-game-player__more" [attr.data-stat]="'showStats' + player.index">
            <button
              type="button"
              class="gd-button gd-game-player__toggle"
              [attr.data-stats-toggle]="player.index"
              [attr.aria-expanded]="open().has(player.index)"
              aria-label="Other piles"
              (click)="toggle(player.index)"
            >
              <span>+</span>
            </button>
          </span>
          @if (open().has(player.index)) {
            <span
              class="gd-game-counter"
              data-counter="discard"
              data-pile="discard"
              [attr.data-clickable]="player.piles.discard ? '' : null"
              [attr.data-player-index]="player.index"
              [attr.data-stat]="'discard' + player.index"
              >{{ player.discard }}</span
            >
            <span
              class="gd-game-counter"
              data-counter="dead"
              data-pile="deadPile"
              [attr.data-clickable]="player.piles.dead ? '' : null"
              [attr.data-player-index]="player.index"
              [attr.data-stat]="'deadPile' + player.index"
              >{{ player.dead }}</span
            >
            <span
              class="gd-game-counter"
              data-counter="adventure-deck"
              data-pile="adventureDeck"
              [attr.data-clickable]="player.piles.adventureDeck ? '' : null"
              [attr.data-player-index]="player.index"
              [attr.data-stat]="'adventureDeck' + player.index"
              >{{ player.adventureDeck }}</span
            >
            <span
              class="gd-game-counter"
              data-counter="removed"
              data-pile="removedPile"
              [attr.data-clickable]="player.piles.removed ? '' : null"
              [attr.data-player-index]="player.index"
              [attr.data-stat]="'removedPile' + player.index"
              >{{ player.removed }}</span
            >
          }
        </div>
      </div>
    }
    <div class="gd-game-state__twilight" data-game="twilight">{{ model().twilight }}</div>
    <div class="gd-game-state__phase" data-game="phase">{{ model().phase }}</div>
    <div class="gd-game-state__decision-clock" data-clock>{{ decisionClock() }}</div>
    <br />
    <div data-game="initiative">
      Initiative:
      @if (initiative() === 'FREE_PEOPLE') {
        <span>Free Peoples</span>
      } @else if (model().stats) {
        <span><b>Shadow</b></span>
      }
    </div>
    <div data-game="ruleof4" [hidden]="ruleOf4().hidden">
      Rule of 4: <span>{{ ruleOf4().count }}</span
      ><span>
        @if (ruleOf4().reached) {
          cards.<br /><b>(Rule of 4 limit reached!)</b>
        } @else if (ruleOf4().count !== '') {
          card
        }
      </span>
    </div>
  `,
})
export class GameStatePanelComponent {
  private readonly session = inject(GameSession);
  protected readonly model = this.session.model;

  private readonly view = inject(GameViewState);
  /** The players whose other piles are shown: kept with the game, so a reload shows them again. */
  protected readonly open = computed<ReadonlySet<number>>(() => new Set(this.view.openPiles()));

  protected readonly players = computed<PlayerView[]>(() => {
    const model = this.model();
    return model.players.map((name, index) => {
      const zones = model.stats?.playerZones[name];
      const threat = model.stats?.threats[name];
      const seconds = model.clocks[name];
      const own = !model.spectator && name === model.participantId;
      return {
        index,
        name,
        current: name === model.currentPlayer,
        own,
        clock: seconds === undefined ? null : clockText(seconds),
        clockBand: seconds === undefined ? null : seconds < 300 ? 'danger' : seconds < 600 ? 'warn' : null,
        deck: shown(zones?.deck),
        hand: shown(zones?.hand),
        handRevealed: model.revealedHands.includes(name),
        threats: threat ? (threat.total === '-1' ? threat.value : `${threat.value}/${threat.total}`) : '',
        discard: shown(zones?.discard),
        dead: shown(zones?.dead),
        adventureDeck: shown(zones?.adventureDeck),
        removed: shown(zones?.removed),
        piles: {
          // One's own deck, once the game has ended
          deck: own && model.ended,
          discard: model.discardPublic || own,
          dead: true,
          adventureDeck: own,
          removed: true,
        },
      };
    });
  });

  /** "elapsed / limit" of the waiting decision, as the server last said. */
  protected readonly decisionClock = computed(() => {
    const { decisionClock, decisionLimit } = this.model().clocks;
    if (decisionClock === undefined) return '';
    return decisionLimit !== undefined && decisionLimit > 0
      ? `${clockText(decisionClock)} / ${clockText(decisionLimit)}`
      : clockText(decisionClock);
  });

  protected readonly initiative = computed(() => (this.model().stats ? this.model().stats?.initiative : null));

  /** The line keeps the last count it showed while the rule doesn't apply, hidden, as the legacy page did. */
  protected readonly ruleOf4 = computed(() => {
    const { stats, lastRuleOf4 } = this.model();
    const count = stats?.ruleOf4;
    // Compared as text, as the legacy page did. Shown until the server first says the rule doesn't apply
    return {
      count: lastRuleOf4 ?? '',
      reached: lastRuleOf4 !== null && lastRuleOf4 >= '4',
      hidden: stats !== null && (count === null || count === undefined || count === '-1'),
    };
  });

  protected toggle(index: number): void {
    this.view.togglePiles(index);
  }
}
