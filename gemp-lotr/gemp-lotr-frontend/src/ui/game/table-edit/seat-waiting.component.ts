import { Component, computed, inject } from '@angular/core';
import { TableFlip } from '../../../core/game/table-edit/table-flip.service';
import { waitingText } from '../../../core/game/table-edit/table-seat';
import { FlipButtonComponent } from './flip-button.component';

/**
 * Where the decision would be, when the game waits on the seat this page does not look from and the person plays it
 * (TableFlip.elsewhere): "Shadow to choose", with Flip beside it. The page does not turn by itself.
 *
 * DOM contract: `gd-seat-waiting` holding `.gd-seat-waiting[data-seat]` (the seat waited on: freeps, shadow) with
 * `.gd-seat-waiting__text` and `gd-flip-button`; nothing while no seat out of sight is waited on.
 */
@Component({
  selector: 'gd-seat-waiting',
  styleUrl: './seat-waiting.component.css',
  imports: [FlipButtonComponent],
  template: `
    @if (flip.elsewhere(); as seat) {
      <span class="gd-seat-waiting" [attr.data-seat]="seat" role="status">
        <span class="gd-seat-waiting__text">{{ text() }}</span>
        <gd-flip-button />
      </span>
    }
  `,
})
export class SeatWaitingComponent {
  protected readonly flip = inject(TableFlip);
  protected readonly text = computed(() => {
    const seat = this.flip.elsewhere();
    return seat === null ? '' : waitingText(seat);
  });
}
