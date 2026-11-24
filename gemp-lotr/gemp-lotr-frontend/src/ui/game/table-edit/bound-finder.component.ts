import { Component, computed, inject } from '@angular/core';
import { BoundFinder } from './bound-finder';
import { SearchWindowComponent } from './search-window.component';

/**
 * The search window over every card, opened to find a filter (BoundFinder: the authoring panel's Find…): nothing
 * bounds it, and its "Use as bound" hands the filters set there on as the deck builder's filter text. A card clicked
 * there does nothing. Drawn by the game page beside the authoring panel.
 *
 * DOM contract: `gd-bound-finder` holding `gd-edit-search` (`data-for=bound`), with `button[data-action=use]` while
 * open.
 */
@Component({
  selector: 'gd-bound-finder',
  imports: [SearchWindowComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-edit-search
      data-for="bound"
      [open]="finder.finding() !== null"
      [heading]="heading()"
      use="Use as bound"
      (used)="finder.use($event)"
      (closed)="finder.close()"
    />
  `,
})
export class BoundFinderComponent {
  protected readonly finder = inject(BoundFinder);
  protected readonly heading = computed(() => `Find: ${this.finder.finding()?.what ?? ''}`);
}
