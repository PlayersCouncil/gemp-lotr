import { Component, computed, inject } from '@angular/core';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { PuzzleBackend } from '../../../core/game/table-edit/puzzle-backend';
import { TableEdit } from '../../../core/game/table-edit/table-edit.service';
import { PuzzlePopups } from './puzzle-popups';

/**
 * A puzzle's edit bar, in the scripted game's menu in place of the sandbox's (ScenarioPanelsComponent). While its
 * slots are being filled: "Choosing", said plainly, and Submit (enabled once every slot has a card; its title says
 * which are still empty), which starts the game with them (TableEdit.submit). In play: the menu's own Start again
 * (the same cards again) and Change cards (the puzzle again with its slots empty, asked about first; not in a draft's
 * game). A refusal of Submit is said under it, in the server's words.
 *
 * DOM contract: `gd-puzzle-bar` with `data-mode` (edit, play) and `data-busy` while Submit is on its way, holding in
 * edit mode `.gd-edit-bar__mode` ("Choosing") and `button[data-puzzle-bar=submit]` (`data-main`; disabled with a
 * slot empty), in play mode `button[data-scenario-menu=restart]` and `button[data-puzzle-bar=change]`; and
 * `.gd-edit-bar__problem` (`role=alert`) while a refusal is up.
 */
@Component({
  selector: 'gd-puzzle-bar',
  styleUrl: './edit-bar.component.css',
  host: {
    '[attr.data-mode]': 'edit.mode()',
    '[attr.data-busy]': "edit.busy() ? '' : null",
  },
  template: `
    @if (edit.mode() === 'edit') {
      <span class="gd-edit-bar__mode" title="Choose a card for each slot, then Submit">Choosing</span>
      <button
        type="button"
        class="gd-edit-bar__button"
        data-puzzle-bar="submit"
        data-main
        [title]="submitTitle()"
        [disabled]="puzzle.missing().length > 0 || edit.busy()"
        (click)="edit.submit()"
      >
        Submit
      </button>
    } @else {
      <button type="button" data-scenario-menu="restart" title="The same cards again" (click)="guide.askRestart()">
        Start again
      </button>
      @if (changes()) {
        <button
          type="button"
          data-puzzle-bar="change"
          title="Start over with the slots empty"
          (click)="guide.changeCards()"
        >
          Change cards
        </button>
      }
    }
    @if (barProblem(); as problem) {
      <span class="gd-edit-bar__problem" role="alert">{{ problem }}</span>
    }
  `,
})
export class PuzzleBarComponent {
  protected readonly edit = inject(TableEdit);
  protected readonly guide = inject(ScenarioGuide);
  protected readonly puzzle = inject(PuzzleBackend);
  private readonly popups = inject(PuzzlePopups);

  protected readonly submitTitle = computed(() => {
    const missing = this.puzzle.missing();
    return missing.length > 0 ? `Still empty: ${missing.join(', ')}` : 'Play with these cards';
  });
  /** Change cards: a game of the puzzle from the library (a draft's cards are its author's to change). */
  protected readonly changes = computed(() => {
    const info = this.guide.info();
    return !!info && !info.author && !info.draftTest;
  });
  /** A refusal said by nothing else: Submit's (a card refused in the search window is said there). */
  protected readonly barProblem = computed(() => {
    const problem = this.edit.problem();
    return problem?.at === null && this.popups.search() === null ? problem.text : null;
  });
}
