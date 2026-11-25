import { Component, computed, inject } from '@angular/core';
import { otherSeat } from '../../../core/game/table-edit/edit-ops';
import { TableFlip } from '../../../core/game/table-edit/table-flip.service';
import { OTHER_SEAT_LABELS, OTHER_SEAT_TITLES, OtherSeat, SEAT_NAMES } from '../../../core/game/table-edit/table-seat';
import { FlipButtonComponent } from './flip-button.component';

/**
 * The edit bar's seat controls (flip table, ef-notes Part 2): who plays the seat the person does not sit in (the
 * script, Gemp's bot or the person: the server's choices, each with a sentence as its title), labelled by that seat;
 * which seat the page looks from, when it is the other one ("As Shadow"); and Flip. Both work in either mode; the bar
 * follows the scenario's info event, which the server sends again after each change. A refusal is said under the bar.
 *
 * DOM contract: `gd-seat-controls` (`data-flipped` while the page looks from the other seat, `data-busy` while a call
 * is on its way) holding `.gd-seat-controls__looking` ("As Shadow", while flipped), `label.gd-seat-controls__other`
 * (the other seat's name) with `select[data-seat-control=other]` (an `option[value]` per choice: script, bot,
 * person), `gd-flip-button` (when the table can be flipped) and `.gd-seat-controls__problem` (`role=alert`) while a
 * refusal is up.
 */
@Component({
  selector: 'gd-seat-controls',
  styleUrl: './seat-controls.component.css',
  imports: [FlipButtonComponent],
  host: {
    '[attr.data-flipped]': "flip.flipped() ? '' : null",
    '[attr.data-busy]': "flip.busy() ? '' : null",
  },
  template: `
    @if (flip.looking(); as looking) {
      <span class="gd-seat-controls__looking" title="The table as this seat sees it">{{ looking }}</span>
    }
    @if (flip.otherSeats().length > 0) {
      <label class="gd-seat-controls__other" [title]="'Who plays the ' + otherName() + ' seat'">
        {{ otherName() }}:
        <select
          class="gd-seat-controls__select"
          data-seat-control="other"
          [disabled]="flip.busy()"
          (change)="choose($any($event.target).value)"
        >
          @for (choice of flip.otherSeats(); track choice) {
            <option [value]="choice" [selected]="choice === flip.otherSeat()" [title]="titles[choice]">
              {{ labels[choice] }}
            </option>
          }
        </select>
      </label>
    }
    @if (flip.canFlip()) {
      <gd-flip-button />
    }
    @if (flip.problem(); as problem) {
      <span class="gd-seat-controls__problem" role="alert">{{ problem }}</span>
    }
  `,
})
export class SeatControlsComponent {
  protected readonly flip = inject(TableFlip);
  protected readonly labels = OTHER_SEAT_LABELS;
  protected readonly titles = OTHER_SEAT_TITLES;
  /** The seat the person does not sit in, by its side ("Shadow"). */
  protected readonly otherName = computed(() => SEAT_NAMES[otherSeat(this.flip.humanSeat())]);

  protected choose(value: string): void {
    void this.flip.setOtherSeat(value as OtherSeat);
  }
}
