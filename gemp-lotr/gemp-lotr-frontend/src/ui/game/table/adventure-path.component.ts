import { Component, computed, DestroyRef, ElementRef, inject, input, signal } from '@angular/core';
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
 * with `--gd-path-index` (from 0) and, once the path is measured, `--gd-path-top`.
 */
@Component({
  selector: 'gd-game-path',
  styleUrl: './adventure-path.component.css',
  imports: [CardComponent, GameCardDirective, GameCardLayersComponent],
  host: { '[style.--gd-path-count]': 'sites().length' },
  template: `
    @for (site of sites(); track site.card.id; let index = $index) {
      <button
        [gdCard]="views.of(site.card)"
        [gdGameCard]="site.card"
        [style.--gd-path-index]="index"
        [style.--gd-path-top.px]="tops()[index] ?? null"
      >
        <gd-game-card-layers [card]="site.card" [tokens]="markers(site)" />
      </button>
    }
  `,
})
export class AdventurePathComponent {
  readonly sites = input.required<readonly TableSite[]>();

  protected readonly views = inject(GameCardViews);

  /** The path's size, once the browser has laid it out (none in jsdom: the CSS places the sites then). */
  private readonly box = signal<{ width: number; height: number } | null>(null);

  /**
   * Where each site's top is: as the legacy group added them up, one site and one gap at a time in floating point,
   * each rounded down (CSS can only multiply, which lands a pixel lower for some heights, e.g. six sites in 533px).
   */
  protected readonly tops = computed<readonly number[]>(() => {
    const box = this.box();
    const count = this.sites().length;
    if (!box) return [];
    const siteHeight = Math.floor((box.width * 350) / 490);
    const gap = Math.min(5, (box.height - count * siteHeight) / (count - 1));
    let y = 0;
    return this.sites().map(() => {
      const top = Math.floor(y);
      y += siteHeight + gap;
      return top;
    });
  });

  constructor() {
    if (typeof ResizeObserver === 'undefined') return;
    const observer = new ResizeObserver(([entry]) => {
      if (entry) this.box.set({ width: entry.contentRect.width, height: entry.contentRect.height });
    });
    observer.observe(inject<ElementRef<HTMLElement>>(ElementRef).nativeElement);
    inject(DestroyRef).onDestroy(() => observer.disconnect());
  }

  /** The players on a site as tokens: "1-a" for player 1 on their turn, "2-i" for player 2 waiting. */
  protected markers(site: TableSite): Record<string, number> {
    return Object.fromEntries(site.players.map(({ number, current }) => [`${number}-${current ? 'a' : 'i'}`, 1]));
  }
}
