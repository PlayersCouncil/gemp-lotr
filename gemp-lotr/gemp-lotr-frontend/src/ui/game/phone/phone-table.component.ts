import {
  afterNextRender,
  afterRenderEffect,
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  Injector,
  untracked,
  viewChild,
} from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { PageCues } from '../../../core/platform/page-cues';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { GameSheets } from '../../../core/game/phone/game-sheets';
import { PhoneFolding, phoneFolding, phoneTable } from '../../../core/game/phone/phone-table';
import { decisionKey } from '../../../core/game/session/game-restore';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { GameTableState } from '../../../core/game/table/game-table.service';
import { PhoneSettings } from '../../../core/game/phone/phone-settings';
import { CardComponent } from '../../shared/card/card.component';
import { GameCardDirective } from '../table/game-card.directive';
import { PhoneCardsComponent } from './phone-cards.component';
import { ScrollSettledDirective } from './scroll-settled.directive';

/**
 * The phone layout's table (G7 of trymarz/gemp-lotr-private#27), scrolled up and down: the whole adventure path, then
 * the opponent's support and characters, the minions, the skirmish and assignments as pairs, the player's characters
 * and support. Each row scrolls sideways. When a decision offers cards on the table, the table scrolls to the first
 * row holding one (the path scrolls away; it is always one swipe up). Where it is scrolled is kept with the game (D10).
 *
 * DOM contract: `gd-phone-table` holding `section.gd-phone-zone` (`data-zone`: path, support-opponent,
 * characters-opponent, shadow, pairs, characters-player, support-player; `data-side`), each with `.gd-phone-zone__label`
 * and its `gd-phone-cards` (or `.gd-phone-path` with a `.gd-phone-site` per place: `data-site`, `data-here`, a site's
 * `button.gd-card`, or `button.gd-phone-site__empty` opening the adventure deck; `data-big` on the site where the
 * game is happening (the turn's player stands there), shown big and brought to the road's middle); or `.gd-phone-pair` per fight with
 * `data-pair` skirmish or assignment: upright, its minions, the Shadow's and the Free Peoples' strengths, its characters;
 * `data-reversed` the other way up, for the Shadow player).
 */
/** The most cards each of two short rows holds to stand side by side on an upright tablet. */
const HALF_ROW = 3;

@Component({
  selector: 'gd-phone-table',
  styleUrl: './phone-table.component.css',
  imports: [PhoneCardsComponent, CardComponent, GameCardDirective, NgTemplateOutlet],
  hostDirectives: [{ directive: ScrollSettledDirective, outputs: ['gdScrollSettled'] }],
  host: { '(gdScrollSettled)': 'keepScroll()' },
  template: `
    <section class="gd-phone-zone" data-zone="path" aria-label="Adventure path">
      <h3 class="gd-phone-zone__label">The road</h3>
      <div #road class="gd-phone-path" data-row="path">
        @for (site of phone().path; track site.number) {
          <div
            class="gd-phone-site"
            [attr.data-site]="site.number"
            [attr.data-here]="site.here ? '' : null"
            [attr.data-big]="site.here ? '' : null"
            [attr.data-ahead]="site.ahead ? '' : null"
          >
            @if (site.card; as card) {
              <button [gdCard]="views.of(card)" [gdGameCard]="card"></button>
            } @else {
              <button
                type="button"
                class="gd-phone-site__empty"
                [attr.aria-label]="'Site ' + site.number + ': your adventure deck'"
                (click)="openAdventureDeck()"
              >
                {{ site.number }}
              </button>
            }
            @if (site.card) {
              <span class="gd-phone-site__no">{{ site.number }}</span>
            }
            <span class="gd-phone-site__markers">
              @for (player of site.players; track player.number) {
                <span class="gd-phone-site__marker" [attr.data-no]="player.number" [title]="player.name">{{
                  player.number
                }}</span>
              }
            </span>
          </div>
        }
      </div>
    </section>
    @for (row of above(); track row.key) {
      <ng-container *ngTemplateOutlet="zone; context: { $implicit: row }" />
    }
    @if (phone().pairs.length > 0) {
      <section class="gd-phone-zone" data-zone="pairs" data-side="fight" [attr.aria-label]="pairsLabel()">
        <h3 class="gd-phone-zone__label">
          {{ pairsLabel() }} <span class="gd-phone-zone__count">{{ phone().pairs.length }}</span>
        </h3>
        <div class="gd-phone-pairs" data-row="pairs">
          @for (pair of phone().pairs; track pair.key) {
            <!-- Upright: the minions on top, the strengths between, the characters under them (the owner, 2026-11-18) -->
            <div class="gd-phone-pair" [attr.data-pair]="pair.kind" [attr.data-reversed]="shadowSide() ? '' : null">
              <gd-phone-cards [cards]="pair.minions" empty="" [layered]="true" />
              @if (pair.kind === 'skirmish') {
                <!-- The strengths on one line between the sides: a shorter fight (the owner, 2026-11-19) -->
                <span class="gd-phone-pair__score">
                  <span
                    class="gd-phone-strength"
                    data-side="shadow"
                    [attr.data-overwhelmed]="shadowOverwhelmed() ? '' : null"
                    >{{ shown()?.shadowStrength ?? '–' }}
                    @if (bonus(shown()?.shadowDamageBonus); as b) {
                      <small>{{ b }}</small>
                    }
                  </span>
                  <span class="gd-phone-pair__vs">vs</span>
                  <span
                    class="gd-phone-strength"
                    data-side="free-peoples"
                    [attr.data-overwhelmed]="overwhelmed() ? '' : null"
                    >{{ shown()?.fellowshipStrength ?? '–' }}
                    @if (bonus(shown()?.fellowshipDamageBonus); as b) {
                      <small>{{ b }}</small>
                    }
                  </span>
                </span>
              } @else {
                <span class="gd-phone-pair__vs">vs</span>
              }
              <gd-phone-cards [cards]="pair.characters" empty="" />
            </div>
          }
        </div>
      </section>
    }
    @for (row of phone().below; track row.key) {
      <ng-container *ngTemplateOutlet="zone; context: { $implicit: row }" />
    }
    <ng-template #zone let-row>
      @let fold = foldings()[row.key];
      @let hides = fold?.how === 'hide' && !fold.offered;
      <section
        class="gd-phone-zone"
        [attr.data-zone]="row.key"
        [attr.data-side]="row.side"
        [attr.data-half]="halves().has(row.key) ? '' : null"
        [attr.aria-label]="row.label"
      >
        <!-- An empty row is one line: its label and "none" (the owner, 2026-11-17) -->
        <h3 class="gd-phone-zone__label">
          {{ row.label }}
          @if (row.cards.length > 0) {
            <span class="gd-phone-zone__count">{{ row.cards.length }}</span>
          } @else {
            <span class="gd-phone-zone__none">none</span>
          }
          <!-- The opponent's cards not active this turn: shown and hidden from the row's title line (the owner, 2026-11-18) -->
          @if (hides) {
            <button
              type="button"
              class="gd-phone-zone__fold"
              data-fold="hide"
              [attr.aria-expanded]="!fold.folded"
              [attr.aria-label]="(fold.folded ? 'Show ' : 'Hide ') + notActive(fold.ids.size)"
              (click)="toggleFold(row.key)"
            >
              {{ fold.folded ? 'Show ' + fold.ids.size + ' ▸' : '◂ Hide' }}
            </button>
          }
          <span class="gd-phone-zone__rule" aria-hidden="true"></span>
        </h3>
        <!-- Every card hidden: the title line is the whole row -->
        @if (row.cards.length > 0 && !(hides && fold.folded && fold.ids.size === row.cards.length)) {
          <gd-phone-cards
            [cards]="row.cards"
            [key]="row.key"
            [shadowFrom]="row.shadowFrom"
            [fold]="row.fold"
            [opened]="unfolded().includes(row.key)"
            (foldToggled)="toggleFold(row.key)"
          />
        }
      </section>
    </ng-template>
  `,
})
export class PhoneTableComponent {
  private readonly session = inject(GameSession);
  private readonly decisions = inject(GameDecisions);
  private readonly sheets = inject(GameSheets);
  private readonly viewState = inject(GameViewState);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  protected readonly views = inject(GameCardViews);
  private readonly table = inject(GameTableState).table;
  private readonly phoneSettings = inject(PhoneSettings);

  protected readonly phone = computed(() => phoneTable(this.table(), this.session.model()));
  /** The rows above the fights; an empty minions' row only while no fight takes its place. */
  protected readonly above = computed(() => {
    const phone = this.phone();
    return phone.above.filter((row) => row.key !== 'shadow' || row.cards.length > 0 || phone.pairs.length === 0);
  });
  protected readonly pairsLabel = computed(() =>
    this.phone().pairs[0]?.kind === 'skirmish' ? 'Skirmish' : 'Assignments',
  );
  /**
   * On an upright tablet, a player's support and characters side by side when both are short (up to HALF_ROW cards
   * each): its width was mostly empty (the layout review, 2026-11-19).
   */
  protected readonly halves = computed(() => {
    const halves = new Set<string>();
    if (this.phoneSettings.device() !== 'tablet') return halves;
    const rows = [...this.phone().above, ...this.phone().below];
    const short = (key: string) => {
      const row = rows.find((r) => r.key === key);
      return row !== undefined && row.cards.length <= HALF_ROW;
    };
    for (const pair of [
      ['support-opponent', 'characters-opponent'],
      ['characters-player', 'support-player'],
    ]) {
      if (pair.every(short)) pair.forEach((key) => halves.add(key));
    }
    return halves;
  });
  /** The rows whose cards not active this turn the player opened, this turn. */
  protected readonly unfolded = computed(() => this.viewState.phone().unfolded);
  /** Each row's folding (phoneFolding), by its key: the title line holds the opponent's button. */
  protected readonly foldings = computed(() => {
    const marks = this.decisions.view().marks;
    const unfolded = this.unfolded();
    const phone = this.phone();
    const foldings: Record<string, PhoneFolding | null> = {};
    for (const row of [...phone.above, ...phone.below]) {
      foldings[row.key] = phoneFolding(row.cards, row.fold, unfolded.includes(row.key), marks);
    }
    return foldings;
  });
  protected readonly shown = computed(() => this.session.model().skirmish?.shown ?? null);
  protected readonly overwhelmed = computed(() => this.shown()?.fpOverwhelmed === 'true');
  protected readonly shadowOverwhelmed = computed(() => this.shown()?.shadowOverwhelmed === 'true');
  /**
   * The player looking is the Shadow player (a spectator: the player at the bottom): the fights upside down, the
   * companions on top and the minions at the bottom, near the player (the owner, 2026-11-18).
   */
  protected readonly shadowSide = computed(() => {
    const current = this.session.model().currentPlayer;
    return current !== null && this.table().bottom !== null && this.table().bottom !== current;
  });
  /** The site where the game is happening (shown big), by its number. */
  private readonly bigSite = computed(() => this.phone().path.find((site) => site.here)?.number ?? null);
  private readonly road = viewChild.required<ElementRef<HTMLElement>>('road');

  constructor() {
    const injector = inject(Injector);
    // Where it was before a reload, once the cards are there and the text set in its own fonts (the modern theme's
    // come after the page: restored before, the table moved when they came)
    const cues = inject(PageCues);
    const destroyed = inject(DestroyRef);
    let alive = true;
    destroyed.onDestroy(() => (alive = false));
    afterNextRender({
      write: () => {
        const top = this.viewState.phone().tableTop;
        this.host.scrollTop = top;
        // Again once the fonts are in, unless the player has scrolled meanwhile (the fonts moved it a pixel or two)
        const set = this.host.scrollTop;
        void cues.fontsReady().then(() => {
          if (alive && Math.abs(this.host.scrollTop - set) <= 2) this.host.scrollTop = top;
        });
      },
    });

    // The site where the game is happening in the road's middle: at first, and whenever it changes (the road scrolls
    // freely in between)
    let centred: number | null = null;
    afterRenderEffect({
      write: () => {
        const big = this.bigSite();
        if (big === null || big === centred) return;
        const road = this.road().nativeElement;
        const site = road.querySelector<HTMLElement>('.gd-phone-site[data-big]');
        if (!site) return;
        centred = big;
        road.scrollLeft = Math.max(0, site.offsetLeft - (road.clientWidth - site.offsetWidth) / 2);
      },
    });

    // New fights (assignments made, a skirmish begun) brought into view once, at the table's top; not the first seen
    // (after a reload the table is where the player left it), nor while replaying (the owner, 2026-11-19)
    let lastFights: string | undefined;
    effect(() => {
      const fights = this.phone()
        .pairs.map((pair) => `${pair.kind}:${pair.key}`)
        .join(',');
      untracked(() => {
        const first = lastFights === undefined;
        const changed = fights !== lastFights;
        lastFights = fights;
        if (first || !changed || fights === '' || this.session.replaying()) return;
        afterNextRender({ write: () => this.showFights() }, { injector });
      });
    });

    // A new turn folds the cards not active in it again (the first turn seen, after a reload too, keeps them)
    let lastTurn: string | null = null;
    effect(() => {
      const current = this.session.model().currentPlayer;
      if (current === null) return;
      untracked(() => {
        if (lastTurn !== null && current !== lastTurn) {
          this.viewState.phone.update((phone) => (phone.unfolded.length === 0 ? phone : { ...phone, unfolded: [] }));
        }
        lastTurn = current;
      });
    });

    // A decision offering cards on the table brings the first of them into view
    let lastDecision: string | null | undefined;
    effect(() => {
      const pending = this.session.model().decision;
      const marks = this.decisions.view().marks;
      untracked(() => {
        const key = pending ? decisionKey(pending.decision) : null;
        // The first decision seen (after a reload) is where the player left it
        if (lastDecision === undefined || key === lastDecision) {
          lastDecision = key;
          return;
        }
        lastDecision = key;
        if (key === null || this.session.replaying()) return;
        const offered = Object.entries(marks).filter(([, mark]) => mark === 'selectable' || mark === 'actionable');
        if (offered.length === 0) return;
        afterNextRender({ write: () => this.reveal(offered.map(([id]) => id)) }, { injector });
      });
    });
  }

  protected notActive(count: number): string {
    return `${count} ${count === 1 ? 'card' : 'cards'} not active this turn`;
  }

  protected bonus(value: string | null | undefined): string | null {
    return value === null || value === undefined || Number(value) === 0 ? null : `+${value}`;
  }

  protected openAdventureDeck(): void {
    const model = this.session.model();
    if (model.spectator || model.participantId === null) return;
    this.sheets.open({ kind: 'piles', player: model.participantId, focus: 'adventureDeck' });
  }

  /** A row's cards not active this turn opened, or folded again. */
  protected toggleFold(key: string): void {
    this.viewState.phone.update((phone) => ({
      ...phone,
      unfolded: phone.unfolded.includes(key) ? phone.unfolded.filter((k) => k !== key) : [...phone.unfolded, key],
    }));
  }

  /** Where the table is scrolled, kept with the game once it stops (D10). */
  protected keepScroll(): void {
    const top = Math.round(this.host.scrollTop);
    this.viewState.phone.update((phone) => (phone.tableTop === top ? phone : { ...phone, tableTop: top }));
  }

  /** The fights at the table's top (or as near as the table scrolls). */
  private showFights(): void {
    const fights = this.host.querySelector<HTMLElement>('.gd-phone-zone[data-zone="pairs"]');
    if (fights) this.host.scrollTop = Math.max(0, fights.offsetTop - 4);
  }

  /** The first offered card's row in view (below the path, unless the path holds it), and the card in its row. */
  private reveal(ids: readonly string[]): void {
    const cards = ids.flatMap((id) => [
      ...this.host.querySelectorAll<HTMLElement>(`.gd-card[data-card-id="${CSS.escape(id)}"]`),
    ]);
    if (cards.length === 0) return;
    const zones = [...this.host.querySelectorAll<HTMLElement>('.gd-phone-zone')];
    const first = zones.find((zone) => cards.some((card) => zone.contains(card)));
    if (!first) return;
    const path = zones[0];
    const view = this.host.clientHeight;
    let top = this.host.scrollTop;
    if (first !== path) {
      const min = path?.offsetHeight ?? 0;
      const bottom = first.offsetTop + first.offsetHeight;
      if (first.offsetTop < top || bottom > top + view) top = Math.max(min, bottom - view);
      top = Math.max(top, min);
      if (first.offsetTop < top) top = first.offsetTop;
    } else {
      top = 0;
    }
    this.host.scrollTop = top;
    for (const scroller of this.host.querySelectorAll<HTMLElement>('.gd-phone-cards__scroll')) {
      const card = cards.find((c) => scroller.contains(c));
      if (card && card.offsetLeft + card.offsetWidth > scroller.scrollLeft + scroller.clientWidth) {
        scroller.scrollLeft = Math.max(0, card.offsetLeft - 12);
      }
    }
  }
}
