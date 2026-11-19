import { GameThemeButtonComponent } from './panel/game-theme-button.component';
import { GAME_THEME_PICKER, ThemeService } from '../../core/platform/theme.service';
import { DOCUMENT } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { ChatRoom } from '../../core/chat/chat-room.service';
import { provideCardInteractions } from '../../core/cards/card-interactions';
import { GameAttention } from '../../core/game/clock/game-attention.service';
import { GameDecisions } from '../../core/game/decision/game-decisions.service';
import { provideGameState } from '../../core/game/game-state';
import { GameSession } from '../../core/game/session/game-session.service';
import { GameViewState } from '../../core/game/session/game-view-state';
import { GameSettings } from '../../core/game/settings/game-settings';
import { GameCardActions } from '../../core/game/table/game-card-actions';
import { GameCardInfo } from '../../core/game/table/game-card-info';
import { GameTableState } from '../../core/game/table/game-table.service';
import { PAGE_QUERY_STRING } from '../../core/platform/page-location';
import { AutoZoomComponent } from '../shared/auto-zoom/auto-zoom.component';
import { CardInfoComponent } from '../shared/card-info/card-info.component';
import { CardHintsDirective } from './table/card-hints.directive';
import { ActionMenuComponent } from './decision/action-menu.component';
import { DecisionDialogsComponent } from './decision/decision-dialogs.component';
import { FeedbackDialogComponent } from './dialogs/feedback-dialog.component';
import { PileDialogsComponent } from './dialogs/pile-dialogs.component';
import { ReplayControlsComponent } from './panel/replay-controls.component';
import { GameAnimationComponent } from './table/game-animation.component';
import { ProblemDialogComponent } from './dialogs/problem-dialog.component';
import { GameAlertComponent } from './panel/game-alert.component';
import { GameStatePanelComponent } from './panel/game-state-panel.component';
import { GameStatsBarComponent } from './panel/game-stats-bar.component';
import { PreGamePanelComponent } from './pregame/pre-game-panel.component';
import { AdventurePathComponent } from './table/adventure-path.component';
import { GameCardsComponent } from './table/game-cards.component';
import { GameTabsComponent } from './tabs/game-tabs.component';
import { PhoneGameComponent } from './phone/phone-game.component';
import { CompactLayout } from '../../core/platform/compact-layout.service';
import { GameSheets } from '../../core/game/phone/game-sheets';
import { PhoneBridge } from '../../core/game/phone/phone-bridge';
import { GameUndo } from '../../core/game/undo/game-undo.service';
import { UndoBannerComponent } from './undo/undo-banner.component';
import { UndoListComponent } from './undo/undo-list.component';
import { UndoRequestComponent } from './undo/undo-request.component';

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
 * (`data-side`: shadow, free-peoples). `data-animating` while an event holds the page (its animation). Cards: see GameCardDirective.
 * Taking the game back (in both layouts): `gd-undo-list`, `gd-undo-request` and `gd-undo-banner` (see ./undo).
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
    ProblemDialogComponent,
    PileDialogsComponent,
    ReplayControlsComponent,
    GameAnimationComponent,
    DecisionDialogsComponent,
    ActionMenuComponent,
    GameThemeButtonComponent,
    CardInfoComponent,
    CardHintsDirective,
    AutoZoomComponent,
    PhoneGameComponent,
    UndoListComponent,
    UndoRequestComponent,
    UndoBannerComponent,
  ],
  host: {
    class: 'gd-game',
    '[attr.data-started]': "model().started ? '' : null",
    '[attr.data-spectator]': "model().spectator ? '' : null",
    '[attr.data-turn]': "table().bottomTurn ? 'bottom' : 'top'",
    '[attr.data-old-stack]': "settings.oldStackVisuals() ? '' : null",
    '[style.--gd-game-fights]': 'fights()',
    '[attr.data-animating]': "session.busy() ? '' : null",
    '[attr.data-layout]': "phone() ? 'phone' : null",
    '(document:keydown.escape)': 'escape()',
    '[style.--gd-window-height]': "windowHeight() + 'px'",
    '(window:resize)': 'measureWindow()',
  },
  template: `
    @if (phone() && (model().started || model().preGame)) {
      <!-- The phone layout also before the game: the pre-game information scrolls, the bid is asked in its divider -->
      <gd-game-phone />
    } @else if (model().started) {
      <section class="gd-game__path" aria-label="Adventure path">
        <gd-game-path [sites]="table().sites" />
      </section>
      <gd-game-stats class="gd-game__stats" />
      <gd-game-state class="gd-game__state" />
      <gd-game-alert class="gd-game__alert" />
      <div class="gd-game__table">
        @if (themePicker) {
          <span class="gd-game__theme"><gd-game-theme-button /></span>
        }
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
              shown()?.fellowshipStrength
            }}</span>
            <span class="gd-game__damage" data-side="free-peoples" [attr.data-none]="noBonus(fpBonus())">{{
              bonusText(fpBonus())
            }}</span>
            <span class="gd-game__strength" data-side="shadow" [attr.data-overwhelmed]="shadowOverwhelmed()">{{
              shown()?.shadowStrength
            }}</span>
            <span class="gd-game__damage" data-side="shadow" [attr.data-none]="noBonus(shadowBonus())">{{
              bonusText(shadowBonus())
            }}</span>
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
    @if (!phone()) {
      <gd-game-tabs class="gd-game__tabs" />
      @if (session.replaying()) {
        <gd-game-replay class="gd-game__replay" />
      }
    }
    <gd-game-animation />
    @if (!phone()) {
      <gd-game-decision-dialogs />
      <gd-game-action-menu />
      <gd-game-piles />
    }
    <gd-game-feedback />
    <gd-game-problem />
    <gd-undo-list />
    <gd-undo-request />
    <gd-undo-banner />
    @if (!phone()) {
      <gd-card-info gdCardHints />
      <!-- In the page: the dialogs it shows over are never modal here (the card information isn't, in classic) -->
      <gd-auto-zoom [topLayer]="false" [flipOnShift]="zoomFlipsOnShift()" />
    }
  `,
})
export class GamePageComponent {
  protected readonly session = inject(GameSession);
  protected readonly settings = inject(GameSettings);
  protected readonly decisions = inject(GameDecisions);
  protected readonly model = this.session.model;
  protected readonly table = inject(GameTableState).table;
  /** The theme button (GAME_THEME_PICKER, on). */
  protected readonly themePicker = inject(GAME_THEME_PICKER);
  private readonly themes = inject(ThemeService);
  /** The modern theme on a phone or an upright tablet (CompactLayout): the phone layout (G7). */
  protected readonly phone = inject(CompactLayout).gameActive;
  private readonly sheets = inject(GameSheets);
  private readonly document = inject(DOCUMENT);
  /**
   * The window's height in whole pixels, as the legacy page measured it ($(window).height()): the layout's rows are
   * placed from it. On a phone, a page without a viewport tag is a fraction of a pixel higher than that (its 980px
   * wide layout scaled to the screen), which 100vh would keep, pushing the table's foot off the screen.
   */
  protected readonly windowHeight = signal(this.document.documentElement.clientHeight);
  /**
   * Shift flips the zoomed card in the modern view only, as the deck builder's zoom does; the legacy game page never
   * flipped it (its key handler got jQuery 1.6's event, which has no key), and classic keeps that (owner, 2026-11-15).
   */
  protected readonly zoomFlipsOnShift = computed(() => this.themes.theme() === 'modern');

  protected readonly stats = computed(() => this.model().stats);
  protected readonly fights = computed(() => this.table().assignments.length + (this.table().skirmish ? 1 : 0));
  /** Changes whenever the table's boxes may have moved: how many fights there are, and whose turn it is. */
  protected readonly placed = computed(() => `${this.fights()}${this.table().bottomTurn ? 'b' : 't'}`);
  /** The numbers the skirmish's boxes show: those of the stats sent since it started (Skirmish). */
  protected readonly shown = computed(() => this.model().skirmish?.shown ?? null);
  protected readonly overwhelmed = computed(() => (this.shown()?.fpOverwhelmed === 'true' ? '' : null));
  protected readonly shadowOverwhelmed = computed(() => (this.shown()?.shadowOverwhelmed === 'true' ? '' : null));
  protected readonly fpBonus = computed(() => this.shown()?.fellowshipDamageBonus ?? null);
  protected readonly shadowBonus = computed(() => this.shown()?.shadowDamageBonus ?? null);

  constructor() {
    // The tab's title and the sound when the game waits for the player
    inject(GameAttention);
    // A card of the game shown in the card information gets the server's modifiers
    inject(GameCardInfo);
    // The phone layout's own handling of card details, answers and alerts
    inject(PhoneBridge);
    const query = new URLSearchParams(inject(PAGE_QUERY_STRING));
    const gameId = query.get('gameId');
    const replayId = query.get('replayId');
    if (replayId !== null) {
      // As the game history links it: "player$recording", sent as it is
      this.session.openReplay(replayId);
    } else if (gameId !== null) {
      const chat = inject(ChatRoom);
      const undo = inject(GameUndo);
      inject(GameViewState).restore(gameId);
      // Once the game is there: its chat, and taking it back (the notices' poll, what can be taken back)
      this.session.open(gameId, () => {
        chat.join(`Game${gameId}`);
        undo.start(gameId);
      });
    }
  }

  protected measureWindow(): void {
    this.windowHeight.set(this.document.documentElement.clientHeight);
  }

  /**
   * Escape: in the phone layout it closes a card's actions or the sheet shown (one level) first; otherwise it finishes
   * a choice of cards shown in a dialog, as on the legacy page.
   */
  protected escape(): void {
    if (this.phone() && this.decisions.menu()) {
      this.decisions.closeMenu();
    } else if (this.phone() && this.sheets.top()) {
      this.sheets.back();
    } else {
      this.decisions.escape();
    }
  }

  /** A damage bonus of none isn't shown (it keeps its place). */
  /** Hidden for a bonus of 0; before any stats of the skirmish an empty box, as on the legacy page. */
  protected noBonus(bonus: string | null): '' | null {
    return bonus !== null && Number(bonus) === 0 ? '' : null;
  }

  protected bonusText(bonus: string | null): string {
    return bonus === null ? '' : `+${bonus}`;
  }
}
