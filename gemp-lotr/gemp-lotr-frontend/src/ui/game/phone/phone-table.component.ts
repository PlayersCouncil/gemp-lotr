import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  Injector,
  untracked,
} from '@angular/core';
import { GameDecisions } from '../../../core/game/decision/game-decisions.service';
import { GameSheets } from '../../../core/game/phone/game-sheets';
import { phoneTable } from '../../../core/game/phone/phone-table';
import { decisionKey } from '../../../core/game/session/game-restore';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameViewState } from '../../../core/game/session/game-view-state';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { GameTableState } from '../../../core/game/table/game-table.service';
import { CardComponent } from '../../shared/card/card.component';
import { GameCardDirective } from '../table/game-card.directive';
import { PhoneCardsComponent } from './phone-cards.component';

/**
 * The phone layout's table (G7 of trymarz/gemp-lotr-private#27), scrolled up and down: the whole adventure path, then
 * the opponent's support and characters, the minions, the skirmish and assignments as pairs, the player's characters
 * and support. Each row scrolls sideways. When a decision offers cards on the table, the table scrolls to the first
 * row holding one (the path scrolls away; it is always one swipe up). Where it is scrolled is kept with the game (D10).
 *
 * DOM contract: `gd-phone-table` holding `section.gd-phone-zone` (`data-zone`: path, support-opponent,
 * characters-opponent, shadow, pairs, characters-player, support-player; `data-side`), each with `.gd-phone-zone__label`
 * and its `gd-phone-cards` (or `.gd-phone-path` with a `.gd-phone-site` per place: `data-site`, `data-here`, a site's
 * `button.gd-card`, or `button.gd-phone-site__empty` opening the adventure deck; or `.gd-phone-pair` per fight with
 * `data-pair` skirmish or assignment).
 */
@Component({
  selector: 'gd-phone-table',
  styleUrl: './phone-table.component.css',
  imports: [PhoneCardsComponent, CardComponent, GameCardDirective],
  host: { '(scroll)': 'scrolled()' },
  template: `
    <section class="gd-phone-zone" data-zone="path" aria-label="Adventure path">
      <h3 class="gd-phone-zone__label">Adventure path</h3>
      <div class="gd-phone-path" data-row="path">
        @for (site of phone().path; track site.number) {
          <div
            class="gd-phone-site"
            [attr.data-site]="site.number"
            [attr.data-here]="site.here ? '' : null"
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
      <section
        class="gd-phone-zone"
        [attr.data-zone]="row.key"
        [attr.data-side]="row.side"
        [attr.aria-label]="row.label"
      >
        <h3 class="gd-phone-zone__label">
          {{ row.label }} <span class="gd-phone-zone__count">{{ row.cards.length }}</span>
        </h3>
        <gd-phone-cards [cards]="row.cards" [key]="row.key" [shadowFrom]="row.shadowFrom" />
      </section>
    }
    @if (phone().pairs.length > 0) {
      <section class="gd-phone-zone" data-zone="pairs" data-side="fight" [attr.aria-label]="pairsLabel()">
        <h3 class="gd-phone-zone__label">
          {{ pairsLabel() }} <span class="gd-phone-zone__count">{{ phone().pairs.length }}</span>
        </h3>
        <div class="gd-phone-pairs" data-row="pairs">
          @for (pair of phone().pairs; track pair.key) {
            <div class="gd-phone-pair" [attr.data-pair]="pair.kind">
              <gd-phone-cards [cards]="pair.characters" empty="" />
              @if (pair.kind === 'skirmish') {
                <span
                  class="gd-phone-strength"
                  data-side="free-peoples"
                  [attr.data-overwhelmed]="overwhelmed() ? '' : null"
                  >{{ shown()?.fellowshipStrength ?? '–' }}
                  @if (bonus(shown()?.fellowshipDamageBonus); as b) {
                    <small>{{ b }}</small>
                  }
                </span>
                <span class="gd-phone-pair__vs">vs</span>
                <span class="gd-phone-strength" data-side="shadow"
                  >{{ shown()?.shadowStrength ?? '–' }}
                  @if (bonus(shown()?.shadowDamageBonus); as b) {
                    <small>{{ b }}</small>
                  }
                </span>
              } @else {
                <span class="gd-phone-pair__vs">vs</span>
              }
              <gd-phone-cards [cards]="pair.minions" empty="" />
            </div>
          }
        </div>
      </section>
    }
    @for (row of phone().below; track row.key) {
      <section
        class="gd-phone-zone"
        [attr.data-zone]="row.key"
        [attr.data-side]="row.side"
        [attr.aria-label]="row.label"
      >
        <h3 class="gd-phone-zone__label">
          {{ row.label }} <span class="gd-phone-zone__count">{{ row.cards.length }}</span>
        </h3>
        <gd-phone-cards [cards]="row.cards" [key]="row.key" [shadowFrom]="row.shadowFrom" />
      </section>
    }
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

  protected readonly phone = computed(() => phoneTable(this.table(), this.session.model()));
  /** The rows above the fights; an empty minions' row only while no fight takes its place. */
  protected readonly above = computed(() => {
    const phone = this.phone();
    return phone.above.filter((row) => row.key !== 'shadow' || row.cards.length > 0 || phone.pairs.length === 0);
  });
  protected readonly pairsLabel = computed(() =>
    this.phone().pairs[0]?.kind === 'skirmish' ? 'Skirmish' : 'Assignments',
  );
  protected readonly shown = computed(() => this.session.model().skirmish?.shown ?? null);
  protected readonly overwhelmed = computed(() => this.shown()?.fpOverwhelmed === 'true');
  private saveTimer: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    const injector = inject(Injector);
    const destroy = inject(DestroyRef);
    destroy.onDestroy(() => {
      if (this.saveTimer !== null) clearTimeout(this.saveTimer);
    });
    // Where it was before a reload, once the cards are there
    afterNextRender({ write: () => (this.host.scrollTop = this.viewState.phone().tableTop) });

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

  protected bonus(value: string | null | undefined): string | null {
    return value === null || value === undefined || Number(value) === 0 ? null : `+${value}`;
  }

  protected openAdventureDeck(): void {
    const model = this.session.model();
    if (model.spectator || model.participantId === null) return;
    this.sheets.open({ kind: 'piles', player: model.participantId, focus: 'adventureDeck' });
  }

  protected scrolled(): void {
    if (this.saveTimer !== null) clearTimeout(this.saveTimer);
    this.saveTimer = setTimeout(() => {
      this.saveTimer = null;
      const top = Math.round(this.host.scrollTop);
      this.viewState.phone.update((phone) => (phone.tableTop === top ? phone : { ...phone, tableTop: top }));
    }, 150);
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
