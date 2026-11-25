import { Component, inject } from '@angular/core';
import { TableFlip } from '../../../core/game/table-edit/table-flip.service';
import { AuthorButtonDirective } from './widgets/author-button.directive';

/**
 * The authoring panel's Flip (ef-notes Part 2 D): an author at the table may look from the opponent's seat, where the
 * page is shown the opponent's decisions; an answer given there is taken for a decision the tool has left for the
 * author (Opponent tab: "it waits for me, here") and written into the script as the Opponent tab's answer would be.
 * The server turns the page (the resync notice); the panel stays docked. Shown while the table can be flipped.
 *
 * DOM contract: `gd-author-flip` holding `button.gd-author-flip[data-author=flip]` ("Flip", or "As Shadow" while the
 * page looks from the other seat; `data-flipped` then; its title where it turns the table to).
 */
@Component({
  selector: 'gd-author-flip',
  imports: [AuthorButtonDirective],
  host: { style: 'display: contents' },
  template: `
    @if (flip.canFlip()) {
      <button
        gdAuthorButton
        class="gd-author-flip"
        data-author="flip"
        [attr.data-flipped]="flip.flipped() ? '' : null"
        [title]="flip.flipTitle()"
        [disabled]="flip.busy()"
        (click)="flip.flip()"
      >
        {{ flip.looking() ?? 'Flip' }}
      </button>
    }
  `,
})
export class AuthorFlipComponent {
  protected readonly flip = inject(TableFlip);
}
