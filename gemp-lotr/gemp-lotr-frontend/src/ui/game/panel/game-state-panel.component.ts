import { Component, computed, inject } from '@angular/core';
import { GameClocks } from '../../../core/game/clock/game-clocks.service';
import { displayName } from '../../../core/game/scenario/scenario-state';
import { GameSession } from '../../../core/game/session/game-session.service';
import { handRevealed, PileName } from '../../../core/game/table/game-table';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { boardZones, sandboxPileAccess } from '../../../core/game/table-edit/edit-piles';
import { seatOf } from '../../../core/game/table-edit/edit-seats';
import { EditSeating } from '../../../core/game/table-edit/edit-seating.service';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { CounterEditComponent } from '../table-edit/counter-edit.component';
import { clockText } from './game-clock';
import { PulseDirective } from './pulse.directive';

interface PlayerView {
  index: number;
  name: string;
  /** The name the page shows (a scripted opponent goes by its scenario's name for it). */
  shown: string;
  current: boolean;
  own: boolean;
  clock: string | null;
  /** The band of the player's own clock: under 10 minutes, under 5. */
  clockBand: 'warn' | 'danger' | null;
  /** Their clock ran out and decided the game. */
  expired: boolean;
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
  /** The pile the hand's counter opens (a revealed hand; in a sandbox, the hand as it is), or null. */
  handPile: PileName | null;
  /** A sandbox: a counter that opens a pile reads as a link. */
  link: boolean;
  /** The threats can be changed here (a sandbox stopped, with `counters`: the fellowship's, whose turn it is). */
  threatsEdit: boolean;
  /**
   * The threats as the sandbox's table has them, while they are changed here (the game's own stats are sent only as
   * the game moves, not after an edit): with the game's limit, as the stats show it.
   */
  threatsNow: string;
}

const shown = (value: number | null | undefined) => (value === null || value === undefined ? '' : String(value));
/** Threats as the stats show them: "1/3" with a limit, "1" without (-1), from the number given. */
const threatsNow = (value: number, total: string | undefined) =>
  total === undefined || total === '-1' ? String(value) : `${value}/${total}`;

/**
 * The game state panel: each player with their clock and counters (deck, hand, threats; "+" shows the discard pile,
 * dead pile, adventure deck and removed pile too), the twilight pool, the phase, the decision's clock, who has
 * initiative and the Rule of 4 count.
 *
 * DOM contract: `gd-game-state` holding `.gd-game-player` per player (`data-current`, `data-open`), in it the name,
 * `[data-clock]`, and the counters `[data-stat]` named as the legacy ids (deck0, hand0, threats0, showStats0,
 * discard0, deadPile0, adventureDeck0, removedPile0; piles also `data-pile` and `data-player-index`),
 * `[data-stats-toggle]`; then `[data-game]` twilight, phase, initiative, ruleof4 and the decision's `[data-clock]`.
 *
 * In a sandbox every pile of both players opens (the hidden ones with its `piles` tool): their counters have
 * `data-link` (underlined), the hand's opens `data-pile=hand`. While it is edited with its `counters` tool, the
 * twilight pool and the threats of the player whose turn it is are `gd-edit-counter`s, and `[data-game=burdens]`
 * shows the burdens (one too). While it is edited the piles' counts are the sandbox's table's, as are the threats
 * (the game sends its own stats only as it moves).
 */
@Component({
  selector: 'gd-game-state',
  styleUrl: './game-state-panel.component.css',
  imports: [PulseDirective, CounterEditComponent],
  template: `
    @for (player of players(); track player.index) {
      <div
        class="gd-game-player"
        [attr.data-current]="player.current ? '' : null"
        [attr.data-open]="open().has(player.index) ? '' : null"
      >
        <span class="gd-game-player__no" [attr.data-no]="player.index + 1">{{ player.index + 1 }}</span
        ><span class="gd-game-player__dot">. </span>{{ player.shown
        }}<span
          class="gd-game-player__clock"
          data-clock
          [attr.data-own]="player.own ? '' : null"
          [attr.data-band]="player.own ? player.clockBand : null"
          [attr.data-expired]="player.expired ? '' : null"
          [gdPulse]="player.own ? clocks.bankPulse() : 0"
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
            [attr.data-link]="player.link && player.piles.deck ? '' : null"
            [attr.role]="player.piles.deck ? 'button' : null"
            [attr.tabindex]="player.piles.deck ? 0 : null"
            (click)="openPile('deck', player.piles.deck, player.name)"
            (keydown.enter)="openPile('deck', player.piles.deck, player.name)"
            >{{ player.deck }}</span
          >
          <span
            class="gd-game-counter"
            data-counter="hand"
            [attr.data-stat]="'hand' + player.index"
            [attr.data-revealed]="player.handRevealed ? '' : null"
            [attr.data-clickable]="player.handPile ? '' : null"
            [attr.data-link]="player.link && player.handPile ? '' : null"
            [attr.data-pile]="player.handPile"
            [attr.data-player-index]="player.handPile ? player.index : null"
            [attr.title]="player.handRevealed ? handTitle(player.own) : null"
            [attr.role]="player.handPile ? 'button' : null"
            [attr.tabindex]="player.handPile ? 0 : null"
            (click)="openHand(player)"
            (keydown.enter)="openHand(player)"
            >{{ player.hand }}</span
          >
          @if (player.threatsEdit) {
            <span class="gd-game-counter" data-counter="threats" [attr.data-stat]="'threats' + player.index"
              ><gd-edit-counter counter="threats" [shown]="player.threatsNow"
            /></span>
          } @else {
            <span class="gd-game-counter" data-counter="threats" [attr.data-stat]="'threats' + player.index">{{
              player.threats
            }}</span>
          }
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
              [attr.data-link]="player.link && player.piles.discard ? '' : null"
              [attr.role]="player.piles.discard ? 'button' : null"
              [attr.tabindex]="player.piles.discard ? 0 : null"
              (click)="openPile('discard', player.piles.discard, player.name)"
              (keydown.enter)="openPile('discard', player.piles.discard, player.name)"
              [attr.data-player-index]="player.index"
              [attr.data-stat]="'discard' + player.index"
              >{{ player.discard }}</span
            >
            <span
              class="gd-game-counter"
              data-counter="dead"
              data-pile="deadPile"
              [attr.data-clickable]="player.piles.dead ? '' : null"
              [attr.data-link]="player.link && player.piles.dead ? '' : null"
              [attr.role]="player.piles.dead ? 'button' : null"
              [attr.tabindex]="player.piles.dead ? 0 : null"
              (click)="openPile('deadPile', player.piles.dead, player.name)"
              (keydown.enter)="openPile('deadPile', player.piles.dead, player.name)"
              [attr.data-player-index]="player.index"
              [attr.data-stat]="'deadPile' + player.index"
              >{{ player.dead }}</span
            >
            <span
              class="gd-game-counter"
              data-counter="adventure-deck"
              data-pile="adventureDeck"
              [attr.data-clickable]="player.piles.adventureDeck ? '' : null"
              [attr.data-link]="player.link && player.piles.adventureDeck ? '' : null"
              [attr.role]="player.piles.adventureDeck ? 'button' : null"
              [attr.tabindex]="player.piles.adventureDeck ? 0 : null"
              (click)="openPile('adventureDeck', player.piles.adventureDeck, player.name)"
              (keydown.enter)="openPile('adventureDeck', player.piles.adventureDeck, player.name)"
              [attr.data-player-index]="player.index"
              [attr.data-stat]="'adventureDeck' + player.index"
              >{{ player.adventureDeck }}</span
            >
            <span
              class="gd-game-counter"
              data-counter="removed"
              data-pile="removedPile"
              [attr.data-clickable]="player.piles.removed ? '' : null"
              [attr.data-link]="player.link && player.piles.removed ? '' : null"
              [attr.role]="player.piles.removed ? 'button' : null"
              [attr.tabindex]="player.piles.removed ? 0 : null"
              (click)="openPile('removedPile', player.piles.removed, player.name)"
              (keydown.enter)="openPile('removedPile', player.piles.removed, player.name)"
              [attr.data-player-index]="player.index"
              [attr.data-stat]="'removedPile' + player.index"
              >{{ player.removed }}</span
            >
          }
        </div>
      </div>
    }
    @if (counters()) {
      <div class="gd-game-state__twilight" data-game="twilight">
        <gd-edit-counter counter="twilight" [shown]="model().twilight" />
      </div>
    } @else {
      <div class="gd-game-state__twilight" data-game="twilight">{{ model().twilight }}</div>
    }
    <div class="gd-game-state__phase" data-game="phase">{{ model().phase }}</div>
    <div
      class="gd-game-state__decision-clock"
      data-clock
      [attr.data-band]="clocks.view().decisionBand"
      [attr.data-expired]="clocks.view().decisionExpired ? '' : null"
      [gdPulse]="clocks.decisionPulse()"
      [attr.data-empty]="decisionClock() === '' ? '' : null"
    >
      {{ decisionClock() }}
    </div>
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
    @if (counters()) {
      <div data-game="burdens">Burdens: <gd-edit-counter counter="burdens" [shown]="edit.board()?.burdens ?? 0" /></div>
    }
  `,
})
export class GameStatePanelComponent {
  private readonly session = inject(GameSession);
  protected readonly clocks = inject(GameClocks);
  protected readonly model = this.session.model;

  private readonly view = inject(GameViewState);
  /** A sandbox: every pile opens; edited, its counters change here. */
  protected readonly edit = inject(TableEdit);
  protected readonly counters = computed(() => this.edit.may('counters'));
  private readonly seating = inject(EditSeating);
  /** The players whose other piles are shown: kept with the game, so a reload shows them again. */
  protected readonly open = computed<ReadonlySet<number>>(() => new Set(this.view.openPiles()));

  protected readonly players = computed<PlayerView[]>(() => {
    const model = this.model();
    const clocks = this.clocks.view();
    return model.players.map((name, index) => {
      // (while a sandbox is edited, its piles as its table counts them: the game's stats come only as it moves)
      const counted = this.edit.editing() ? boardZones(this.edit.board(), seatOf(this.seating.seats(), name)) : null;
      const zones = counted ?? model.stats?.playerZones[name];
      const threat = model.stats?.threats[name];
      const seconds = clocks.banks[name];
      const own = !model.spectator && name === model.participantId;
      const sandbox = this.edit.level() === 'sandbox';
      const access = sandboxPileAccess(own, this.edit.tools().includes('piles'));
      const revealed = handRevealed(model, name);
      return {
        index,
        name,
        shown: displayName(model.scenario, name),
        current: name === model.currentPlayer,
        own,
        clock: seconds === undefined ? null : clockText(seconds),
        clockBand: own ? clocks.ownBand : null,
        expired: clocks.expired.includes(name),
        deck: shown(zones?.deck),
        hand: shown(zones?.hand),
        handRevealed: revealed,
        threats: threat ? (threat.total === '-1' ? threat.value : `${threat.value}/${threat.total}`) : '',
        discard: shown(zones?.discard),
        dead: shown(zones?.dead),
        adventureDeck: shown(zones?.adventureDeck),
        removed: shown(zones?.removed),
        piles: sandbox
          ? { ...access, deck: access.deck || (own && model.ended) }
          : {
              // One's own deck, once the game has ended
              deck: own && model.ended,
              discard: model.discardPublic || own,
              dead: true,
              adventureDeck: own,
              removed: true,
            },
        handPile: sandbox ? (access.hand || revealed ? 'hand' : null) : revealed ? 'revealedHand' : null,
        link: sandbox,
        threatsEdit: this.counters() && name === model.currentPlayer,
        threatsNow: threatsNow(this.edit.board()?.threats ?? 0, threat?.total),
      };
    });
  });

  /** "elapsed / limit" of the waiting decision, as the server last said. */
  protected readonly decisionClock = computed(() => {
    const decision = this.clocks.view().decision;
    if (!decision) return '';
    const elapsed = clockText(decision.elapsed);
    return decision.limit > 0 ? `${elapsed} / ${clockText(decision.limit)}` : elapsed;
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

  /** A counter clicked: its pile, if the player may look into it. */
  protected openPile(pile: PileName, allowed: boolean, player: string): void {
    if (allowed) this.view.openPile(pile, player);
  }

  /** The hand's counter clicked: its pile, if it opens one. */
  protected openHand(player: PlayerView): void {
    if (player.handPile) this.view.openPile(player.handPile, player.name);
  }

  protected handTitle(own: boolean): string {
    return own
      ? 'Your hand is revealed to your opponent and spectators. Click to see it as they do.'
      : 'This hand is revealed. Click to view it.';
  }

  protected toggle(index: number): void {
    this.view.togglePiles(index);
  }
}
