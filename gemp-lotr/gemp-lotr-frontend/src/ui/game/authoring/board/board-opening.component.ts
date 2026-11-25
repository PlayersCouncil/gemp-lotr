import { Component, inject, signal } from '@angular/core';
import { BoardChanges } from '../../../../core/game/authoring/board-changes.service';
import { Opening, OPENING_WORDS } from '../../../../core/game/authoring/opening';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';

const OPENS_HINT =
  'With the board laid out: play begins on the board the setup describes. With the game’s own opening: the bid, ' +
  'the choice of going first, the starting fellowships and the opening draw are played, and the script answers ' +
  'them (its first decision is the bid); the setup then says only the order of each deck, the opening draw coming ' +
  'off the top. The Free Peoples seat has to go first: the script bids more, or chooses Go first.';

/**
 * The Board tab's "How the game opens" (setup.opening): with the board laid out as the setup says, or with the game's
 * own opening played (the bid, the seat choice, the starting fellowships, the opening draw, each a decision of the
 * script's). Changed at once (a fresh game), after the page's own question where the change loses setup the other
 * way cannot have (or leaves steps that will not be reached). With the game's own opening, a note says what is
 * played; before the cards are dealt (until the seats are chosen), that there are none yet.
 *
 * DOM contract: `gd-author-board-opening` holding `.gd-author-board-opening` (`data-opening`: board, game) with
 * `.gd-author-label` and `.gd-author-board-opening__pick` with `gd-author-select[data-part=opening]`, and with the game's own opening
 * `.gd-author-note[data-note=opening]` (and `[data-note=undealt]` before the cards are dealt).
 */
@Component({
  selector: 'gd-author-board-opening',
  styleUrl: './board-opening.component.css',
  imports: [AuthorSelectComponent],
  template: `
    @let opening = changes.opening();
    <div class="gd-author-board-opening" [attr.data-opening]="opening">
      <span class="gd-author-label" [title]="hint">Opens</span>
      <span class="gd-author-board-opening__pick">
        @for (shown of [kept()]; track shown) {
          <gd-author-select
            data-part="opening"
            label="How the game opens"
            [hint]="hint"
            [options]="options"
            [value]="opening"
            (changed)="choose($event)"
          />
        }
      </span>
    </div>
    @if (opening === 'game') {
      <p class="gd-author-note" data-note="opening" [title]="hint">Bid, seats, fellowships and draw are played.</p>
      @if (changes.board() === null) {
        <p
          class="gd-author-note"
          data-note="undealt"
          title="The game makes the cards once the seats are chosen: the board, and the cards' names, come from then on"
        >
          No cards yet: they are dealt once the seats are chosen.
        </p>
      }
    }
  `,
})
export class BoardOpeningComponent {
  protected readonly changes = inject(BoardChanges);
  protected readonly hint = OPENS_HINT;
  protected readonly options: readonly AuthorOption[] = (['board', 'game'] as const).map(
    (one) => [one, OPENING_WORDS[one]] as const,
  );
  /** Bumped when a change is not made after all: the select is made afresh, as the draft has it. */
  protected readonly kept = signal(0);

  protected async choose(value: string): Promise<void> {
    if (!(await this.changes.setOpening(value as Opening))) this.kept.update((count) => count + 1);
  }
}
