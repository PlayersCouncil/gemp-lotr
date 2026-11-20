import { Component, computed, inject } from '@angular/core';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { BoardChanges } from '../../../core/game/authoring/board-changes.service';
import { BOARD_SEATS } from '../../../core/game/authoring/board-queue';
import { CardHintsDirective } from '../table/card-hints.directive';
import { BoardAddComponent } from './board/board-add.component';
import { BoardCountersComponent } from './board/board-counters.component';
import { BoardGatheredComponent } from './board/board-gathered.component';
import { BoardPathComponent } from './board/board-path.component';
import { BoardSeatComponent } from './board/board-seat.component';
import { BoardStartComponent } from './board/board-start.component';
import { BoardOpeningComponent } from './board/board-opening.component';

/** Where the Board's changes go, as the radios offer it: its words, and a short note with the rest as its tooltip. */
interface WhereChoice {
  value: 'now' | 'setup';
  label: string;
  note: string;
  hint: string;
}

/**
 * The authoring panel's Board tab (the legacy scenarioAuthor.js renderBoard): what is on the table, changed. The
 * changes to the board (cards put in and taken out, moved, wounded, ordered; the totals; the markers and who
 * controls a site; where the story starts) are gathered under the bar
 * at the top, shown as if made, and kept across fresh games until they are submitted (to the board as it stands, or
 * to the one the scenario starts with) or reset. The adventure path, the Ring-bearer and the Ring, a card's version
 * and the names of cards and sites are changed at once. A card's name shows the card under the mouse and opens its
 * information on a click. Fields being typed in keep what is in them when the state comes again.
 *
 * With the game's own opening (B10), the board is the cards as dealt, and none before the seats are chosen (then only
 * how the game opens is shown); to the board the scenario starts with, only a deck's order can be changed.
 *
 * DOM contract: `gd-game-author-board` holding `p.gd-author-note[data-empty]` until the board is laid out (with the
 * game's own opening, `gd-author-board-opening` under a `.gd-author-board__heading`); else
 * `.gd-author-board` (where a card's name opens its information) with `gd-author-board-gathered`, `.gd-author-board__where` (`.gd-author-board__heading`, a
 * `label.gd-author-board__choice` per place, `data-where`: now, setup, with `input[type=radio]` and
 * `.gd-author-note`; `.gd-author-board__pending` with changes waiting for a step), `gd-author-board-counters` (not
 * where only a deck's order can be changed),
 * `gd-author-board-start`, `gd-author-board-path`, a `gd-author-board-seat` per seat and `gd-author-board-add`.
 */
@Component({
  selector: 'gd-game-author-board',
  styleUrl: './board-tab.component.css',
  providers: [BoardChanges],
  imports: [
    CardHintsDirective,
    BoardAddComponent,
    BoardCountersComponent,
    BoardGatheredComponent,
    BoardPathComponent,
    BoardSeatComponent,
    BoardStartComponent,
    BoardOpeningComponent,
  ],
  template: `
    @if (authoring.draft(); as draft) {
      @if (changes.board() === null) {
        @if (changes.opening() === 'game') {
          <div class="gd-author-board__heading">Where the story starts</div>
          <gd-author-board-opening />
        } @else {
          <p class="gd-author-note" data-empty>The board has not been laid out.</p>
        }
      } @else {
        <div class="gd-author-board" gdCardHints>
          <gd-author-board-gathered />
          <div class="gd-author-board__where">
            <div class="gd-author-board__heading">Changes are made</div>
            @for (choice of choices(); track choice.value) {
              <label class="gd-author-board__choice" [attr.data-where]="choice.value" [title]="choice.hint">
                <input
                  type="radio"
                  name="gd-author-board-where"
                  [checked]="authoring.ui().where === choice.value"
                  (click)="where($event, choice.value)"
                />
                {{ choice.label }}
                <span class="gd-author-note">{{ choice.note }}</span>
              </label>
            }
            @if (draft.pending.length > 0) {
              <p
                class="gd-author-note gd-author-board__pending"
                title="Changes made to the board as it stands that no step carries yet: the next step you write does them"
              >
                Waiting for a step to carry them: {{ draft.pending.join(', ') }}
              </p>
            }
          </div>
          @if (!changes.deckOnly()) {
            <gd-author-board-counters />
          }
          <gd-author-board-start />
          <gd-author-board-path />
          @for (seat of seats; track seat) {
            <gd-author-board-seat [seat]="seat" />
          }
          <gd-author-board-add />
        </div>
      }
    }
  `,
})
export class BoardTabComponent {
  protected readonly authoring = inject(Authoring);
  protected readonly changes = inject(BoardChanges);
  protected readonly seats = BOARD_SEATS;

  protected readonly choices = computed<WhereChoice[]>(() => {
    const position = this.authoring.draft()?.position;
    const now = position?.inHand
      ? {
          note: `Step ${position.step} does it on every play.`,
          hint:
            `Done once submitted, and by step ${position.step} whenever the script is played. The decision on the ` +
            'screen was worked out before the change: it shows in what the game offers from the next decision on.',
        }
      : {
          note: 'The next step you write does it.',
          hint: 'Done once submitted, and by the next step you write. The decision on the screen was worked out before the change.',
        };
    return [
      { value: 'now', label: 'to the board as it stands', ...now },
      {
        value: 'setup',
        label: 'to the board the scenario starts with',
        note: 'Replays the script to here.',
        hint: 'The setup is changed, and a fresh game is played by the script back to here.',
      },
    ];
  });

  /** (changing it with changes gathered is refused: the radio stays where it was) */
  protected where(event: Event, where: 'now' | 'setup'): void {
    if (!this.changes.setWhere(where)) event.preventDefault();
  }
}
