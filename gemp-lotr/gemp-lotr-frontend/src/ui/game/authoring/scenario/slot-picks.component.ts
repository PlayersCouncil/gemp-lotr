import { Component, input, output, signal } from '@angular/core';
import { FoundCard } from '../../../../core/game/authoring/authoring-state';
import { PuzzleSlot } from '../../../../core/game/authoring/puzzle-state';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { CardSearchComponent } from '../widgets/card-search.component';
import { CardHoverDirective } from '../widgets/card-hover.directive';

/** A slot's pick as the list shows it: the blueprint, and its name where it is known. */
export interface ShownPick {
  blueprint: string;
  name: string | null;
}

/**
 * A puzzle's slots, a line each, with the card picked for each (its name shows the card under the mouse) and Change,
 * which finds another card by its name (a site slot: a site): for Record's picks and a solution's. Says the card
 * picked for a slot; the server checks it against the slot's bound when it is sent.
 *
 * DOM contract: `ul.gd-author-picks` with an `li.gd-author-picks__slot` per slot (`data-slot`) holding
 * `.gd-author-picks__id`, `.gd-author-picks__card` (`data-empty` with none), `button[data-action=change]`, or
 * `gd-author-card-search` while one is being found.
 */
@Component({
  selector: 'gd-author-slot-picks',
  styleUrl: './slot-picks.component.css',
  imports: [AuthorButtonDirective, CardHoverDirective, CardSearchComponent],
  template: `
    <ul class="gd-author-picks">
      @for (slot of slots(); track slot.id) {
        @let pick = picks()[slot.id];
        <li class="gd-author-picks__slot" [attr.data-slot]="slot.id">
          <span class="gd-author-picks__id" [title]="slot.label ?? slot.bound">{{ slot.id }}</span>
          @if (finding() === slot.id) {
            <gd-author-card-search
              [sites]="slot.zone === 'site'"
              [current]="pick?.blueprint ?? null"
              (picked)="chose(slot.id, $event)"
              (cancelled)="finding.set(null)"
            />
          } @else {
            @if (pick) {
              <span class="gd-author-picks__card" [gdCardHover]="pick.blueprint" [title]="pick.blueprint">{{
                pick.name ?? pick.blueprint
              }}</span>
            } @else {
              <span class="gd-author-picks__card" data-empty>(empty)</span>
            }
            <button
              gdAuthorButton="inline"
              data-action="change"
              [title]="
                'Find another card for ' + slot.id + (slot.bound ? ' (it takes what ' + slot.bound + ' finds)' : '')
              "
              (click)="finding.set(slot.id)"
            >
              Change
            </button>
          }
        </li>
      }
    </ul>
  `,
})
export class SlotPicksComponent {
  readonly slots = input.required<readonly PuzzleSlot[]>();
  readonly picks = input.required<Readonly<Partial<Record<string, ShownPick>>>>();
  readonly picked = output<{ slot: string; card: FoundCard }>();
  /** The slot a card is being found for. */
  protected readonly finding = signal<string | null>(null);

  protected chose(slot: string, card: FoundCard): void {
    this.finding.set(null);
    this.picked.emit({ slot, card });
  }
}
