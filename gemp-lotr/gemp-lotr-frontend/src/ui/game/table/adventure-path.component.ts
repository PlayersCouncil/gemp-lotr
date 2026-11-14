import { Component, inject, input } from '@angular/core';
import { GameCardViews } from '../../../core/game/table/game-card-views';
import { TableSite } from '../../../core/game/table/game-table';
import { CardComponent } from '../../shared/card/card.component';
import { GameCardLayersComponent } from './game-card-layers.component';
import { GameCardDirective } from './game-card.directive';

/**
 * The adventure path (the legacy AdvPathCardGroup): the sites in play one under the other in their order, each with
 * a marker per player standing on it (the player's number; lit for the player whose turn it is).
 *
 * DOM contract: `gd-game-path` with `--gd-path-count` (the number of sites), holding one `button.gd-card` per site
 * with `--gd-path-index` (from 0).
 */
@Component({
  selector: 'gd-game-path',
  styleUrl: './adventure-path.component.css',
  imports: [CardComponent, GameCardDirective, GameCardLayersComponent],
  host: { '[style.--gd-path-count]': 'sites().length' },
  template: `
    @for (site of sites(); track site.card.id; let index = $index) {
      <button [gdCard]="views.of(site.card)" [gdGameCard]="site.card" [style.--gd-path-index]="index">
        <gd-game-card-layers [card]="site.card" [tokens]="markers(site)" />
      </button>
    }
  `,
})
export class AdventurePathComponent {
  readonly sites = input.required<readonly TableSite[]>();

  protected readonly views = inject(GameCardViews);

  /** The players on a site as tokens: "1-a" for player 1 on their turn, "2-i" for player 2 waiting. */
  protected markers(site: TableSite): Record<string, number> {
    return Object.fromEntries(site.players.map(({ number, current }) => [`${number}-${current ? 'a' : 'i'}`, 1]));
  }
}
