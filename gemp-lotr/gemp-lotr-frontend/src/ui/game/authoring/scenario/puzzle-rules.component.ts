import { Component, computed, inject, input, signal } from '@angular/core';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import {
  FORBID_LABELS,
  filterValue,
  puzzleLike,
  rulesWithForbid,
} from '../../../../core/game/authoring/puzzle-settings';
import { AuthorCheckComponent } from '../widgets/author-check.component';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { FilterFieldComponent } from '../widgets/filter-field.component';
import { FindBoundComponent } from '../board/find-bound.component';
import { sendTop } from './top';

/**
 * The Scenario tab's rules that puzzles brought (ep-notes.md P10, P2): what the player may not do (`rules.forbid`: a
 * tick each, as the server names them; doing it fails the puzzle), and, for a puzzle, its pool (`pool`: the deck
 * builder's filter every slot's card has to pass), with how many cards it finds or why the server refused it. Forbid
 * sends the whole of `rules`; the pool is a part of its own.
 *
 * DOM contract: `.gd-author-scenario__ticks[data-part=forbid]` with a `gd-author-check[data-forbid]` per choice; for
 * a puzzle, `gd-author-filter` (data-part=pool) in a `gd-author-field`, with `gd-author-find-bound` (Find…: the card
 * search, whose "Use as bound" sends its filters as the pool).
 */
@Component({
  selector: 'gd-game-author-scenario-puzzle-rules',
  imports: [AuthorCheckComponent, AuthorFieldComponent, FilterFieldComponent, FindBoundComponent],
  template: `
    @if (state().puzzle; as puzzle) {
      <div class="gd-author-scenario__ticks" data-part="forbid">
        <span class="gd-author-label" title="What the player may not do: doing it ends the scenario, failed"
          >Forbid</span
        >
        @for (what of puzzle.forbidChoices; track what) {
          <gd-author-check
            [attr.data-forbid]="what"
            [label]="labels[what]?.[0] ?? what"
            [hint]="labels[what]?.[1] ?? ''"
            [checked]="puzzle.forbid.includes(what)"
            (changed)="forbid(what, $event)"
          />
        }
      </div>
      @if (shown()) {
        <gd-author-field
          label="Pool"
          hint="The deck builder's filter every slot's card has to pass, such as set:1-6 for a puzzle of July 2003. Empty: any card."
        >
          <gd-author-filter
            part="pool"
            key="puzzle.pool"
            label="Pool"
            hint="A deck builder filter: set:1-6 side:FREE_PEOPLE culture:ROHAN cardtype:CONDITION…"
            placeholder="any card"
            [value]="puzzle.pool ?? ''"
            [refused]="refused()"
            (sent)="pool($event)"
          />
          <gd-author-find-bound part="pool" what="pool" (used)="pool($event)" />
        </gd-author-field>
      }
    }
  `,
})
export class PuzzleRulesComponent {
  private readonly authoring = inject(Authoring);
  readonly state = input.required<DraftState>();
  protected readonly labels = FORBID_LABELS;
  protected readonly shown = computed(() => puzzleLike(this.state().draft));
  /** Why the pool sent last was refused (null once one goes through). */
  protected readonly refused = signal<string | null>(null);

  protected forbid(what: string, on: boolean): void {
    const state = this.state();
    const puzzle = state.puzzle;
    if (!puzzle) return;
    void sendTop(this.authoring, 'rules', rulesWithForbid(state.draft, puzzle.forbidChoices, puzzle.forbid, what, on));
  }

  protected async pool(typed: string): Promise<void> {
    const done = await sendTop(this.authoring, 'pool', filterValue(typed));
    this.refused.set(done === null ? (this.authoring.problem() ?? 'Refused') : null);
  }
}
