import { Component, computed, inject } from '@angular/core';
import { ChatRoom } from '../../core/chat/chat-room.service';
import { provideCardInteractions } from '../../core/cards/card-interactions';
import { provideGameState } from '../../core/game/game-state';
import { GameSession } from '../../core/game/session/game-session.service';
import { GameViewState } from '../../core/game/session/game-view-state';
import { GameSettings } from '../../core/game/settings/game-settings';
import { GameCardActions } from '../../core/game/table/game-card-actions';
import { GameTableState } from '../../core/game/table/game-table.service';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { AutoZoomComponent } from '../shared/auto-zoom/auto-zoom.component';
import { CardInfoComponent } from '../shared/card-info/card-info.component';
import { FeedbackDialogComponent } from './dialogs/feedback-dialog.component';
import { GameAlertComponent } from './panel/game-alert.component';
import { GameStatePanelComponent } from './panel/game-state-panel.component';
import { GameStatsBarComponent } from './panel/game-stats-bar.component';
import { PreGamePanelComponent } from './pregame/pre-game-panel.component';
import { AdventurePathComponent } from './table/adventure-path.component';
import { GameCardsComponent } from './table/game-cards.component';
import { GameTabsComponent } from './tabs/game-tabs.component';

/** The cookie the game page remembers its hover zoom setting in (the legacy game page's). */
export const GAME_AUTO_ZOOM_COOKIE = 'autoZoomInGame';

/**
 * The game page: the table where a game is played or watched (the legacy game.html). On the left the adventure path
 * with the stats bar under it, the game state panel with the alert box under it, and the tab box below both; the rest
 * is the table's rows: the opponent's support area and characters, the minions, the player's characters and support
 * area, and the player's hand (a spectator has none). Minions assigned to characters, and the skirmish being fought,
 * lie in boxes beside the minions' row, between it and the characters whose player it is not.
 *
 * DOM contract: `gemp-game.gd-game` with `data-started` once the players are known, `data-spectator`, `data-turn`
 * (bottom, top: whose turn it is), `data-old-stack` (the player's setting) and `--gd-game-fights` (how many boxes of
 * assignments and skirmish there are). In it `.gd-game__path`, `gd-game-stats`, `gd-game-state`, `gd-game-alert`,
 * `gd-game-tabs`, and `.gd-game__table` holding a `section.gd-game__row` per row (`data-row`: support-opponent,
 * characters-opponent, shadow, characters-player, support-player, hand) and a `section.gd-game__fight` per skirmish
 * (`data-fight="skirmish"`) and assignment (`data-fight="assignment"`), each with `.gd-game__side`
 * (`data-side`: shadow, free-peoples). Cards: see GameCardDirective.
 */
@Component({
  selector: 'gemp-game',
  styleUrl: './game-page.component.css',
  providers: [
    ...provideGameState(),
    ...provideCardInteractions({ actions: GameCardActions, autoZoomCookie: GAME_AUTO_ZOOM_COOKIE }),
  ],
  imports: [
    AdventurePathComponent,
    GameCardsComponent,
    GameStatePanelComponent,
    GameStatsBarComponent,
    GameAlertComponent,
    GameTabsComponent,
    PreGamePanelComponent,
    FeedbackDialogComponent,
    CardInfoComponent,
    AutoZoomComponent,
  ],
  host: {
    class: 'gd-game',
    '[attr.data-started]': "model().started ? '' : null",
    '[attr.data-spectator]': "model().spectator ? '' : null",
    '[attr.data-turn]': "table().bottomTurn ? 'bottom' : 'top'",
    '[attr.data-old-stack]': "settings.oldStackVisuals() ? '' : null",
    '[style.--gd-game-fights]': 'fights()',
  },
  template: `
    @if (model().started) {
      <section class="gd-game__path" aria-label="Adventure path">
        <gd-game-path [sites]="table().sites" />
      </section>
      <gd-game-stats class="gd-game__stats" />
      <gd-game-state class="gd-game__state" />
      <gd-game-alert class="gd-game__alert" />
      <div class="gd-game__table">
        <section class="gd-game__row" data-row="support-opponent" aria-label="Opponent's support area">
          <gd-game-cards [stacks]="table().supportOpponent" />
        </section>
        <section class="gd-game__row" data-row="characters-opponent" aria-label="Opponent's characters">
          <gd-game-cards [stacks]="table().charactersOpponent" [placed]="placed()" />
        </section>
        <section class="gd-game__row" data-row="shadow" aria-label="Minions">
          <gd-game-cards [stacks]="table().shadow" [placed]="placed()" />
        </section>
        <section class="gd-game__row" data-row="characters-player" aria-label="Your characters">
          <gd-game-cards [stacks]="table().charactersPlayer" [placed]="placed()" />
        </section>
        <section class="gd-game__row" data-row="support-player" aria-label="Your support area">
          <gd-game-cards [stacks]="table().supportPlayer" />
        </section>
        @if (!model().spectator) {
          <section class="gd-game__row" data-row="hand" aria-label="Your hand">
            <gd-game-cards [stacks]="table().hand" />
          </section>
        }
        @if (table().skirmish; as skirmish) {
          <section class="gd-game__fight" data-fight="skirmish" aria-label="Skirmish">
            <gd-game-cards class="gd-game__side" data-side="shadow" [stacks]="skirmish.minions" [placed]="placed()" />
            <gd-game-cards
              class="gd-game__side"
              data-side="free-peoples"
              [stacks]="skirmish.characters"
              [placed]="placed()"
            />
            <span class="gd-game__strength" data-side="free-peoples" [attr.data-overwhelmed]="overwhelmed()">{{
              stats()?.fellowshipStrength
            }}</span>
            <span class="gd-game__damage" data-side="free-peoples" [attr.data-none]="noBonus(fpBonus())"
              >+{{ fpBonus() }}</span
            >
            <span class="gd-game__strength" data-side="shadow">{{ stats()?.shadowStrength }}</span>
            <span class="gd-game__damage" data-side="shadow" [attr.data-none]="noBonus(shadowBonus())"
              >+{{ shadowBonus() }}</span
            >
          </section>
        }
        @for (assignment of table().assignments; track assignment.characterId) {
          <section class="gd-game__fight" data-fight="assignment" aria-label="Assignment">
            <gd-game-cards class="gd-game__side" data-side="shadow" [stacks]="assignment.minions" [placed]="placed()" />
            <gd-game-cards
              class="gd-game__side"
              data-side="free-peoples"
              [stacks]="assignment.characters"
              [placed]="placed()"
            />
          </section>
        }
      </div>
    } @else if (model().preGame; as preGame) {
      <gd-game-pregame class="gd-game__pregame" [info]="preGame" />
    }
    <gd-game-tabs class="gd-game__tabs" />
    <gd-game-feedback />
    <gd-card-info />
    <gd-auto-zoom />
  `,
})
export class GamePageComponent {
  private readonly session = inject(GameSession);
  protected readonly settings = inject(GameSettings);
  protected readonly model = this.session.model;
  protected readonly table = inject(GameTableState).table;

  protected readonly stats = computed(() => this.model().stats);
  protected readonly fights = computed(() => this.table().assignments.length + (this.table().skirmish ? 1 : 0));
  /** Changes whenever the table's boxes may have moved: how many fights there are, and whose turn it is. */
  protected readonly placed = computed(() => `${this.fights()}${this.table().bottomTurn ? 'b' : 't'}`);
  protected readonly overwhelmed = computed(() => (this.stats()?.fpOverwhelmed === 'true' ? '' : null));
  protected readonly fpBonus = computed(() => this.stats()?.fellowshipDamageBonus ?? null);
  protected readonly shadowBonus = computed(() => this.stats()?.shadowDamageBonus ?? null);

  constructor() {
    const query = new URLSearchParams(inject(PAGE_QUERY_STRING));
    const gameId = query.get('gameId');
    if (gameId !== null) {
      const chat = inject(ChatRoom);
      inject(GameViewState).restore(gameId);
      this.session.open(gameId, () => chat.join(`Game${gameId}`));
    }
  }

  /** A damage bonus of none isn't shown (it keeps its place). */
  protected noBonus(bonus: string | null): '' | null {
    return bonus === null || Number(bonus) === 0 ? '' : null;
  }
}
