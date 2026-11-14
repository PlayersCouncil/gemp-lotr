import { Component, computed, inject, input } from '@angular/core';
import { GameSession } from '../../../core/game/session/game-session.service';
import { GameCard } from '../../../core/game/state/game-model';
import { siteImage } from '../../../core/platform/app-paths';

/** A column of tokens holds this many; more start another column (layoutTokens). */
const TOKENS_PER_COLUMN = 10;

interface TokenView {
  image: string;
  /** Its column, from 1, and its place in it, from 0. */
  column: number;
  row: number;
  /** How many steps right it stands of its column: every second token of a kind moves a step, and stays there. */
  shift: number;
}

/** The Ring-bearer's resistance is drawn apart for some of them ("RF5": Frodo with 5). */
const RESISTANCE_OWNERS: Readonly<Record<string, string>> = { A: 'aragorn', F: 'frodo', G: 'gandalf', T: 'theoden' };

interface NumbersView {
  strength: string;
  vitality: string;
  /** A minion's site number, or null. */
  site: string | null;
  resistance: string | null;
  resistanceOwner: string | null;
}

/**
 * What a game draws over a card (in `button[gdCard]`): its tokens (wounds, burdens, culture tokens; on a site, the
 * players standing there) and, for a character, its strength, vitality and site number or resistance as the server
 * counts them. Sizes and places are shares of the card, in the stylesheet.
 *
 * DOM contract: `gd-game-card-layers` holding `img.gd-game-token` (with `--gd-token-column`, `--gd-token-row`,
 * `--gd-token-shift` and, on the host, `--gd-token-columns`) and `.gd-game-number` with `data-number` (strength,
 * vitality, site, resistance).
 */
@Component({
  selector: 'gd-game-card-layers',
  styleUrl: './game-card-layers.component.css',
  host: { '[style.--gd-token-columns]': 'columns()' },
  template: `
    @for (token of tokenViews(); track $index) {
      <img
        class="gd-game-token"
        [src]="token.image"
        alt=""
        draggable="false"
        [style.--gd-token-column]="token.column"
        [style.--gd-token-row]="token.row"
        [style.--gd-token-shift]="token.shift"
      />
    }
    @if (numbers(); as n) {
      <img class="gd-game-number__icon" data-number="strength" [src]="icons.strength" alt="" draggable="false" />
      <span class="gd-game-number" data-number="strength">{{ n.strength }}</span>
      <img class="gd-game-number__icon" data-number="vitality" [src]="icons.vitality" alt="" draggable="false" />
      <span class="gd-game-number" data-number="vitality">{{ n.vitality }}</span>
      @if (n.site !== null) {
        <img class="gd-game-number__icon" data-number="site" [src]="icons.site" alt="" draggable="false" />
        <span class="gd-game-number" data-number="site">{{ n.site }}</span>
      }
      @if (n.resistance !== null) {
        @if (n.resistanceOwner === null) {
          <img
            class="gd-game-number__icon"
            data-number="resistance"
            [src]="icons.resistance"
            alt=""
            draggable="false"
          />
        }
        <span class="gd-game-number" data-number="resistance" [attr.data-owner]="n.resistanceOwner">{{
          n.resistance
        }}</span>
      }
    }
  `,
})
export class GameCardLayersComponent {
  readonly card = input.required<GameCard>();
  /** The tokens to draw instead of the card's own (a site shows the players on it). */
  readonly tokens = input<Readonly<Record<string, number>> | null>(null);

  private readonly session = inject(GameSession);

  /** The pictures the numbers are written on: part of the game's art, the same in every theme. */
  protected readonly icons = {
    strength: siteImage('o_icon_strength.png'),
    vitality: siteImage('o_icon_vitality.png'),
    site: siteImage('o_icon_compass.png'),
    resistance: siteImage('o_icon_resistance.png'),
  };

  private readonly counts = computed(() =>
    Object.entries(this.tokens() ?? this.card().tokens).filter(([, count]) => count > 0),
  );

  protected readonly columns = computed(() =>
    this.counts().reduce((sum, [, count]) => sum + 1 + Math.floor((count - 1) / TOKENS_PER_COLUMN), 0),
  );

  protected readonly tokenViews = computed<TokenView[]>(() => {
    const views: TokenView[] = [];
    let column = 1;
    for (const [token, count] of this.counts()) {
      const image = siteImage(`tokens/${token.toLowerCase()}.png`);
      let row = 0;
      let shift = 0;
      for (let i = 0; i < count; i++) {
        if (row === TOKENS_PER_COLUMN) {
          row = 0;
          column++;
          shift = 0;
        }
        if (i % 2 === 1) shift++;
        views.push({ image, column, row, shift });
        row++;
      }
      column++;
    }
    return views;
  });

  protected readonly numbers = computed<NumbersView | null>(() => {
    const card = this.card();
    const stats = this.session.model().stats?.charStats[card.id];
    // A hindered card lies face down: its numbers aren't shown
    if (!stats || card.hindered) return null;
    const third = stats[2];
    const resistance = third?.startsWith('R') ? third.slice(1) : null;
    const owner = resistance !== null ? (RESISTANCE_OWNERS[resistance.charAt(0)] ?? null) : null;
    return {
      strength: stats[0] ?? '',
      vitality: stats[1] ?? '',
      site: third !== undefined && resistance === null ? third : null,
      resistance: resistance === null ? null : owner ? resistance.slice(1) : resistance,
      resistanceOwner: owner,
    };
  });
}
