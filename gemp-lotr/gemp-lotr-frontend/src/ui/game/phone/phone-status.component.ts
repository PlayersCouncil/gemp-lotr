import { NgTemplateOutlet } from '@angular/common';
import {
  afterRenderEffect,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { CompactLayout } from '../../../core/platform/compact-layout.service';
import { PageCues } from '../../../core/platform/page-cues';
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
  /** Drawn before the text: the computer layout's icon (the theme's) for the pill's key. */
  icon?: boolean;
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
  imports: [GameThemeButtonComponent, NgTemplateOutlet],
  template: `
    <div class="gd-phone-status__bar">
      <button type="button" class="gd-phone-icon" data-phone="menu" aria-label="Menu" (click)="open('menu')">☰</button>
      <span class="gd-phone-status__phase" data-game="phase">{{ phase() }}</span>
      <span class="gd-phone-status__twilight" data-game="twilight" title="Twilight pool"
        ><svg class="gd-phone-status__gem" width="16" height="18" viewBox="0 0 18 20" aria-hidden="true">
          <path class="gd-phone-status__gem-stone" d="M9 1l7 6-7 12L2 7z" />
          <path class="gd-phone-status__gem-facets" d="M2 7h14M9 1L6 7l3 12 3-12z" /></svg
        >{{ model().twilight }}</span
      >
      @if (yourMove()) {
        <span class="gd-phone-status__move" data-phone="your-move">Your move</span>
      }
      <span class="gd-phone-status__gap"></span>
      <!-- Upright, pills that don't fit beside the phase take a line of their own under the bar (the private mobile
           page): both places are always there, one shown, so nothing is drawn anew when they move -->
      <span #pillsInline class="gd-phone-status__pills" data-place="bar" [hidden]="pillsWrapped()">
        <ng-container [ngTemplateOutlet]="pillList" />
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
    <div #pillsLine class="gd-phone-status__pills" data-place="line" [hidden]="!pillsWrapped()">
      <ng-container [ngTemplateOutlet]="pillList" />
    </div>
    <ng-template #pillList>
      @for (pill of pills(); track pill.key) {
        <span
          class="gd-phone-pill"
          [attr.data-pill]="pill.key"
          [attr.data-warn]="pill.warn ? '' : null"
          [title]="pill.title"
        >
          @if (pill.icon) {
            <i class="gd-phone-pill__icon" aria-hidden="true"></i>
          }
          <!-- Line icons for the initiative's banner and the moves' road (no text glyphs, the owner, 2026-11-17) -->
          @if (pill.key === 'initiative') {
            <svg class="gd-phone-pill__line" width="11" height="13" viewBox="0 0 11 13" aria-hidden="true">
              <path d="M1.5 12V1.5h7l-1.8 2.5 1.8 2.5h-7" />
            </svg>
          } @else if (pill.key === 'moves') {
            <svg class="gd-phone-pill__line" width="14" height="12" viewBox="0 0 14 12" aria-hidden="true">
              <path d="M2 11c3-2 1-5 5-6s4-3 5-4" stroke-dasharray="2 2" />
            </svg>
          }
          {{ pill.text }}</span
        >
      }
    </ng-template>
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
            <!-- The computer layout's icons (the theme's), the dead pile's a cross -->
            <span data-count="hand" title="Hand"
              ><i class="gd-phone-player__icon" aria-hidden="true"></i>{{ player.hand }}</span
            >
            <span data-count="deck" title="Draw deck"
              ><i class="gd-phone-player__icon" aria-hidden="true"></i>{{ player.deck }}</span
            >
            <span data-count="threats" title="Threats"
              ><i class="gd-phone-player__icon" aria-hidden="true"></i>{{ player.threats }}</span
            >
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

  /** The pills on a line of their own under the bar: upright, when they don't fit beside the phase. */
  protected readonly pillsWrapped = signal(false);
  private readonly device = inject(CompactLayout).device;
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private readonly pillsInline = viewChild.required<ElementRef<HTMLElement>>('pillsInline');
  private readonly pillsLine = viewChild.required<ElementRef<HTMLElement>>('pillsLine');

  constructor() {
    afterRenderEffect({
      read: () => {
        this.pills();
        this.device();
        this.pillsWrapped();
        this.fitPills();
      },
    });
    const destroyed = inject(DestroyRef);
    if (typeof ResizeObserver === 'function') {
      const observer = new ResizeObserver(() => this.fitPills());
      observer.observe(this.host);
      destroyed.onDestroy(() => observer.disconnect());
    }
    // The theme's fonts come after the page and change every width: measured again then
    let alive = true;
    destroyed.onDestroy(() => (alive = false));
    void inject(PageCues)
      .fontsReady()
      .then(() => alive && this.fitPills());
  }

  /**
   * Beside the phase while they fit; otherwise (upright only: sideways the bar shares its line with the players) on a
   * line of their own, until the bar's free space holds them again.
   */
  private fitPills(): void {
    const box = (this.pillsWrapped() ? this.pillsLine() : this.pillsInline()).nativeElement;
    if (this.device() === 'sideways') {
      if (this.pillsWrapped()) this.pillsWrapped.set(false);
      return;
    }
    // The pills' own width: right aligned, an overflow goes left, where scrollWidth doesn't count it
    const pills = Array.from(box.children) as HTMLElement[];
    const needed = pills.reduce((width, pill) => width + pill.offsetWidth, 0) + 4 * Math.max(0, pills.length - 1);
    if (!this.pillsWrapped()) {
      if (needed > box.clientWidth + 1) this.pillsWrapped.set(true);
      return;
    }
    const free = this.host.querySelector<HTMLElement>('.gd-phone-status__gap')?.clientWidth ?? 0;
    // The bar's gap between items (5px) is taken again by the pills beside the phase
    if (needed + 5 <= free) this.pillsWrapped.set(false);
  }

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
    // Archery first, so a narrow phone never cuts it off; whenever the server gives the totals, as the computer
    // layout's stats bar (it writes "null" outside the archery phase)
    const archery = (total: string | null) => (total === null || total === 'null' ? null : total);
    const fp = archery(stats.fellowshipArchery);
    const shadow = archery(stats.shadowArchery);
    if (fp !== null) {
      pills.push({ key: 'fp-archery', text: `FP ${fp}`, title: 'Free Peoples archery', warn: false, icon: true });
    }
    if (shadow !== null) {
      pills.push({ key: 'shadow-archery', text: `Sh ${shadow}`, title: 'Shadow archery', warn: false, icon: true });
    }
    if (stats.initiative) {
      const free = stats.initiative === 'FREE_PEOPLE';
      pills.push({
        key: 'initiative',
        text: free ? 'FP' : 'Sh',
        title: `Initiative: ${free ? 'Free Peoples' : 'Shadow'}`,
        warn: false,
      });
    }
    if (stats.moveCount !== null && stats.moveLimit !== null) {
      pills.push({
        key: 'moves',
        text: `${stats.moveCount}/${stats.moveLimit}`,
        title: 'Moves made / allowed',
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
