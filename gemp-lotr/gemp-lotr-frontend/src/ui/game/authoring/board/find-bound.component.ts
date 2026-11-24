import { Component, inject, input, output } from '@angular/core';
import { BoundFinder } from '../../table-edit/bound-finder';
import { AuthorButtonDirective } from '../widgets/author-button.directive';

/**
 * "Find…" beside a field that takes a deck builder filter (a slot's bound, a puzzle's pool): it opens the game page's
 * search window over every card (BoundFinder: the shared filter panel, nothing bounding it), whose "Use as bound"
 * hands the filters set there, as the filter text, to `used` (the field's own setter; for a slot,
 * Authoring.offerSlotBound).
 *
 * DOM contract: `button.gd-author-button[data-action=find]` with `data-for` (what it fills: a slot's id, `pool`).
 */
@Component({
  selector: 'gd-author-find-bound',
  imports: [AuthorButtonDirective],
  host: { style: 'display: contents' },
  template: `
    @if (finder) {
      <button
        gdAuthorButton="inline"
        data-action="find"
        [attr.data-for]="part()"
        [title]="'Find the filter with the card search: ' + what()"
        (click)="find()"
      >
        Find…
      </button>
    }
  `,
})
export class FindBoundComponent {
  /** What the found filter fills (a slot's id, `pool`), as `data-for`. */
  readonly part = input.required<string>();
  /** What it is called in the window's title ("bound of ally", "pool"). */
  readonly what = input.required<string>();
  /** The filter text the person chose. */
  readonly used = output<string>();

  /** (a page without the card search, a panel's spec: no Find…) */
  protected readonly finder = inject(BoundFinder, { optional: true });

  protected find(): void {
    this.finder?.open(this.what(), (filter) => this.used.emit(filter));
  }
}
