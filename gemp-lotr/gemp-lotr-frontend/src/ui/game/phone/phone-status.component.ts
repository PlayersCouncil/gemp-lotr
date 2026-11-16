import { Component, computed, inject } from '@angular/core';
import { GameClocks } from '../../../core/game/clock/game-clocks.service';
import { chatLines, GameSheets } from '../../../core/game/phone/game-sheets';
import { GameLog } from '../../../core/game/session/game-log';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { clockText } from '../panel/game-clock';
import { GameThemeButtonComponent } from '../panel/game-theme-button.component';

/** The phase as a word, as the phone shows it ("Fellowship" for FELLOWSHIP). */
export function phaseLabel(phase: string | null): string {
  if (!phase) return '';
  return phase
    .split('_')
    .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
}

interface Pill {
  key: string;
  text: string;
  title: string;
  warn: boolean;
}

/**
 * The phone layout's top (G7): the status bar (menu, phase, twilight, "Your move", the pills for initiative, moves,
 * archery and the Rule of 4, the chat with its unread count, the red theme button) and a chip per player (number,
 * name, clock, hand, deck, threats, dead pile) that opens the player's piles.
 *
 * DOM contract: `gd-phone-status` holding `.gd-phone-status__bar` with `button[data-phone=menu]`, `[data-game=phase]`,
 * `[data-game=twilight]`, `[data-phone=your-move]`, `.gd-phone-pill` (`data-pill`), `button[data-phone=chat]` (with
 * `.gd-phone-unread`), the theme button; then `.gd-phone-players` with a `button.gd-phone-player` per player
 * (`data-player`, `data-current`), each with `[data-clock]` and `[data-count]` hand, deck, threats, dead.
 */
@Component({
  selector: 'gd-phone-status',
  styleUrl: './phone-status.component.css',
  imports: [GameThemeButtonComponent],
  template: `
    <div class="gd-phone-status__bar">
      <button type="button" class="gd-phone-icon" data-phone="menu" aria-label="Menu" (click)="open('menu')">☰</button>
      <span class="gd-phone-status__phase" data-game="phase">{{ phase() }}</span>
      <span class="gd-phone-status__twilight" data-game="twilight" title="Twilight pool"
        ><i aria-hidden="true"></i>{{ model().twilight }}</span
      >
      @if (yourMove()) {
        <span class="gd-phone-status__move" data-phone="your-move">Your move</span>
      }
      <span class="gd-phone-status__gap"></span>
      <span class="gd-phone-status__pills">
        @for (pill of pills(); track pill.key) {
          <span
            class="gd-phone-pill"
            [attr.data-pill]="pill.key"
            [attr.data-warn]="pill.warn ? '' : null"
            [title]="pill.title"
            >{{ pill.text }}</span
          >
        }
      </span>
      <button
        type="button"
        class="gd-phone-icon"
        data-phone="chat"
        aria-label="Chat and game log"
        (click)="open('chat')"
      >
        💬
        @if (unread() > 0) {
          <span class="gd-phone-unread">{{ unread() }}</span>
        }
      </button>
      <gd-game-theme-button class="gd-phone-status__theme" />
    </div>
    <div class="gd-phone-players">
      @for (player of players(); track player.name) {
        <button
          type="button"
          class="gd-phone-player"
          [attr.data-player]="player.name"
          [attr.data-current]="player.current ? '' : null"
          [attr.aria-label]="player.name + ': piles'"
          (click)="sheets.open({ kind: 'piles', player: player.name, focus: null })"
        >
          <span class="gd-phone-player__no" [attr.data-no]="player.number">{{ player.number }}</span>
          <span class="gd-phone-player__name">{{ player.name }}</span>
          <span class="gd-phone-player__clock" data-clock [attr.data-band]="player.band">{{ player.clock }}</span>
          <span class="gd-phone-player__counts">
            <span data-count="hand" title="Hand">✋{{ player.hand }}</span>
            <span data-count="deck" title="Draw deck">▤{{ player.deck }}</span>
            <span data-count="threats" title="Threats">👁{{ player.threats }}</span>
            <span data-count="dead" title="Dead pile">✝{{ player.dead }}</span>
          </span>
        </button>
      }
    </div>
  `,
})
export class PhoneStatusComponent {
  private readonly session = inject(GameSession);
  private readonly clocks = inject(GameClocks);
  private readonly log = inject(GameLog);
  private readonly viewState = inject(GameViewState);
  protected readonly sheets = inject(GameSheets);
  protected readonly model = this.session.model;
  /** Lines of the chat, counted for the unread number (a room's lines, not the game's). */
  private readonly chatLines = computed(() => chatLines(this.log.entries()));

  protected readonly phase = computed(() => phaseLabel(this.model().phase));
  protected readonly yourMove = computed(
    () => this.model().decision !== null && !this.session.replaying() && !this.model().ended,
  );
  protected readonly unread = computed(() => Math.max(0, this.chatLines() - this.viewState.phone().chatSeen));

  protected readonly pills = computed<Pill[]>(() => {
    const model = this.model();
    const stats = model.stats;
    if (!stats) return [];
    const pills: Pill[] = [];
    if (stats.initiative) {
      const free = stats.initiative === 'FREE_PEOPLE';
      pills.push({
        key: 'initiative',
        text: free ? '⚑ FP' : '⚑ Sh',
        title: `Initiative: ${free ? 'Free Peoples' : 'Shadow'}`,
        warn: false,
      });
    }
    if (stats.moveCount !== null && stats.moveLimit !== null) {
      pills.push({
        key: 'moves',
        text: `⇢ ${stats.moveCount}/${stats.moveLimit}`,
        title: 'Moves made / allowed',
        warn: false,
      });
    }
    if (model.phase === 'ARCHERY') {
      pills.push({
        key: 'archery',
        text: `➶ ${stats.fellowshipArchery ?? 0}:${stats.shadowArchery ?? 0}`,
        title: 'Archery: Free Peoples : Shadow',
        warn: false,
      });
    }
    const rule = stats.ruleOf4;
    if (rule !== null && rule !== '-1' && model.lastRuleOf4 !== null) {
      pills.push({
        key: 'ruleof4',
        text: `R4 ${model.lastRuleOf4}/4`,
        title: 'Rule of 4: cards drawn this turn',
        warn: model.lastRuleOf4 >= '4',
      });
    }
    return pills;
  });

  protected readonly players = computed(() => {
    const model = this.model();
    const clocks = this.clocks.view();
    return model.players.map((name, i) => {
      const zones = model.stats?.playerZones[name];
      const threat = model.stats?.threats[name];
      const seconds = clocks.banks[name];
      const own = !model.spectator && name === model.participantId;
      const shown = (n: number | null | undefined) => (n === null || n === undefined ? '–' : String(n));
      return {
        name,
        number: i + 1,
        current: name === model.currentPlayer,
        clock: seconds === undefined ? '' : clockText(seconds),
        band: own ? clocks.ownBand : null,
        hand: shown(zones?.hand),
        deck: shown(zones?.deck),
        dead: shown(zones?.dead),
        threats: threat ? (threat.total === '-1' ? threat.value : `${threat.value}/${threat.total}`) : '–',
      };
    });
  });

  protected open(kind: 'menu' | 'chat'): void {
    this.sheets.open({ kind });
  }
}
