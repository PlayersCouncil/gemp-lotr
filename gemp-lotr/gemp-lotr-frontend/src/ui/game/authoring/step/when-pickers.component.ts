import { Component, computed, inject } from '@angular/core';
import { PHASES, pickersOf, putTerm, SEATS, TYPES, WhenPicker } from '../../../../core/game/authoring/terms';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { AskedPickerComponent } from './asked-picker.component';
import { RolePickerComponent } from './role-picker.component';
import { WhenHost } from './when-host';

/**
 * The When composer: seat, role (in a game of two sides), kind of decision, what is being asked, phase and turn, each
 * putting its term into the When, keeping the others, in the order the server writes them; hidden for a When written
 * out in full. It works on whatever provides the WhenHost: the Step tab's form (each change sent at once) or an
 * opponent's order (sent with its Apply).
 *
 * DOM contract: `gd-author-when-pickers` holding `.gd-author-when__pickers` with, each in a `.gd-author-when__pick`,
 * `gd-author-select[data-picker]` seat, kind, phase, `gd-author-role` and `gd-author-asked`; then
 * `input[data-picker=turn]`.
 */
@Component({
  selector: 'gd-author-when-pickers',
  styleUrl: './when-pickers.component.css',
  imports: [AuthorSelectComponent, AskedPickerComponent, RolePickerComponent],
  template: `
    @if (pickers(); as pickers) {
      <div class="gd-author-when__pickers">
        <span class="gd-author-when__pick">
          <gd-author-select
            data-picker="seat"
            label="Seat"
            [options]="seats"
            [value]="pickers.seat"
            (changed)="pick('seat', $event)"
          />
        </span>
        <span class="gd-author-when__pick">
          <gd-author-role path="when" />
        </span>
        <span class="gd-author-when__pick">
          <gd-author-select
            data-picker="kind"
            label="Kind of decision"
            [options]="kinds"
            [value]="pickers.kind"
            (changed)="pick('kind', $event)"
          />
        </span>
        <span class="gd-author-when__pick">
          <gd-author-asked path="when" />
        </span>
        <span class="gd-author-when__pick">
          <gd-author-select
            data-picker="phase"
            label="Phase"
            [options]="phases"
            [value]="pickers.phase"
            (changed)="pick('phase', $event)"
          />
        </span>
        <input
          type="number"
          min="1"
          class="gd-author-input gd-author-step__short gd-author-when__turn"
          data-picker="turn"
          placeholder="turn"
          aria-label="Turn"
          [value]="pickers.turn"
          (change)="pick('turn', $any($event.target).value)"
        />
      </div>
    }
  `,
})
export class WhenPickersComponent {
  private readonly host = inject(WhenHost);
  protected readonly seats: readonly AuthorOption[] = [['', 'either seat'], ...SEATS];
  protected readonly kinds: readonly AuthorOption[] = [['', 'any kind'], ...TYPES];
  protected readonly phases: readonly AuthorOption[] = [
    ['', 'any phase'],
    ...PHASES,
    ['between_turns', 'between turns'],
  ];

  protected readonly pickers = computed(() => {
    const when = this.host.whenText('when');
    return when === null ? null : pickersOf(when);
  });

  /** A picker changed: its term is put into the When, in its place. */
  protected pick(picker: WhenPicker, value: string): void {
    this.host.putWhen('when', putTerm(this.host.whenText('when'), picker, value.trim()));
    this.host.pickedWhen();
  }
}
