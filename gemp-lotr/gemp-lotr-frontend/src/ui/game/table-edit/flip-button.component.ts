import { Component, inject } from '@angular/core';
import { TableFlip } from '../../../core/game/table-edit/table-flip.service';

/**
 * Flip: the table shown from the other seat, or back (TableFlip). Its title says where it turns the table to ("Look
 * from Shadow's side", "Back to your side (Free Peoples)"). The edit bar and the alert box's "Shadow to choose" have
 * one; the page reads the game again when the server has turned it (the resync notice).
 *
 * DOM contract: `gd-flip-button` holding `button.gd-flip-button[data-flip]` ("Flip"; `data-flipped` while the page
 * looks from the other seat; disabled while a call is on its way).
 */
@Component({
  selector: 'gd-flip-button',
  styleUrl: './flip-button.component.css',
  host: { style: 'display: contents' },
  template: `
    <button
      type="button"
      class="gd-flip-button"
      data-flip
      [attr.data-flipped]="flip.flipped() ? '' : null"
      [title]="flip.flipTitle()"
      [disabled]="flip.busy()"
      (click)="flip.flip()"
    >
      Flip
    </button>
  `,
})
export class FlipButtonComponent {
  protected readonly flip = inject(TableFlip);
}
