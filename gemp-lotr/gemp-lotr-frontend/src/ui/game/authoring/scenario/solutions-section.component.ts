import { Component, computed, inject, input, signal } from '@angular/core';
import { DraftState, FoundCard } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { puzzleLike } from '../../../../core/game/authoring/puzzle-settings';
import { freshSolutionName, recordPicks, solutionNameProblem } from '../../../../core/game/authoring/puzzle-solutions';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { ShownPick, SlotPicksComponent } from './slot-picks.component';
import { SolutionRowComponent } from './solution-row.component';

/**
 * The Scenario tab's "Solutions" of a puzzle (ep-notes.md P4), after "How it ends": the solutions are what shows its
 * ending can be reached (Check plays each, and says which solve it; Submit for review wants one that does), so they
 * sit with the ending and the Check that proves them. A banner with Stop while one is being recorded; each solution
 * (solution-row); and Record: a name, and the card for each slot (the game in hand's, until another is chosen), which
 * starts a fresh game from the start in which what the author answers that the script leaves open goes into the
 * solution.
 *
 * DOM contract: `section.gd-author-scenario__section[data-section=solutions]` with `h3.gd-author-scenario__heading`,
 * `.gd-author-solutions__recording` (`button[data-author=stop]`) while recording, `.gd-author-solutions` with a
 * `gd-author-solution` per solution (or `p.gd-author-note[data-empty]`), then `.gd-author-solutions__record` with
 * `input[data-part=solution-name]`, `gd-author-slot-picks` for a puzzle with slots, and `button[data-author=record]`.
 */
@Component({
  selector: 'gd-game-author-scenario-solutions',
  styleUrl: './solutions-section.component.css',
  imports: [AuthorButtonDirective, AuthorFieldComponent, SlotPicksComponent, SolutionRowComponent],
  template: `
    @if (shown() && state().puzzle; as puzzle) {
      <section class="gd-author-scenario__section" data-section="solutions">
        <h3 class="gd-author-scenario__heading">Solutions</h3>
        @if (puzzle.recording; as recording) {
          <div class="gd-author-solutions__recording" data-tone="told">
            <span title="What you answer that the script leaves open goes into this solution, not into the script"
              >Recording {{ recording }}</span
            >
            <button gdAuthorButton data-author="stop" title="Stop recording (the solution stays)" (click)="stop()">
              Stop
            </button>
          </div>
        }
        @if (puzzle.solutions.length > 0) {
          <div class="gd-author-solutions">
            @for (solution of puzzle.solutions; track solution.name) {
              <gd-author-solution
                [state]="state()"
                [solution]="solution"
                [verdict]="verdicts().get(solution.name) ?? null"
              />
            }
          </div>
        } @else {
          <p class="gd-author-note" data-empty title="A puzzle is submitted with a solution that solves it">
            None yet.
          </p>
        }
        <div class="gd-author-solutions__record">
          <gd-author-field
            label="Record"
            hint="A fresh game from the start with these cards in the slots: what you answer that the script leaves open goes into the solution of this name"
          >
            <input
              #name
              type="text"
              class="gd-author-input"
              data-part="solution-name"
              aria-label="Name of the solution"
              [attr.data-bad]="nameProblem() !== null ? '' : null"
              [title]="nameProblem() ?? 'Its name: letters, digits, dashes and underscores'"
              [value]="typedName() ?? fresh()"
              (input)="typedName.set(name.value)"
              (keydown.enter)="record(name.value)"
            />
            <button
              gdAuthorButton="primary"
              data-author="record"
              title="Record a solution of this name (one there is starts again empty)"
              [disabled]="nameProblem() !== null"
              (click)="record(name.value)"
            >
              Record
            </button>
          </gd-author-field>
          @if (puzzle.slots.length > 0) {
            <gd-author-slot-picks [slots]="puzzle.slots" [picks]="recordShown()" (picked)="choose($event)" />
          }
        </div>
      </section>
    }
  `,
})
export class SolutionsSectionComponent {
  private readonly authoring = inject(Authoring);
  readonly state = input.required<DraftState>();
  protected readonly shown = computed(() => puzzleLike(this.state().draft));
  private readonly names = computed(() => (this.state().puzzle?.solutions ?? []).map((one) => one.name));
  protected readonly fresh = computed(() => freshSolutionName(this.names()));
  protected readonly typedName = signal<string | null>(null);
  /** (Record may be made again for a solution there is: it starts again empty) */
  protected readonly nameProblem = computed(() => solutionNameProblem(this.typedName() ?? this.fresh(), []));
  /** What the last Check (or save) said of each solution. */
  protected readonly verdicts = this.authoring.solutionVerdicts;
  /** The cards chosen for Record, slot by slot (the game in hand's where none is). */
  private readonly chosen = signal<Readonly<Record<string, FoundCard>>>({});

  protected readonly recordShown = computed(() => {
    const puzzle = this.state().puzzle;
    const shown: Record<string, ShownPick> = {};
    for (const slot of puzzle?.slots ?? []) {
      const chosen = this.chosen()[slot.id];
      if (chosen) shown[slot.id] = { blueprint: chosen.id, name: chosen.name };
      else if (slot.pick) shown[slot.id] = { blueprint: slot.pick, name: slot.pickName };
    }
    return shown;
  });

  protected choose({ slot, card }: { slot: string; card: FoundCard }): void {
    this.chosen.update((chosen) => ({ ...chosen, [slot]: card }));
  }

  protected async record(name: string): Promise<void> {
    const puzzle = this.state().puzzle;
    if (!puzzle || solutionNameProblem(name.trim(), []) !== null) return;
    const chosen = Object.fromEntries(Object.entries(this.chosen()).map(([slot, card]) => [slot, card.id]));
    const picks = recordPicks(
      puzzle.slots.map((slot) => slot.id),
      puzzle.picks,
      chosen,
    );
    if (await this.authoring.solution('record', name.trim(), picks)) {
      this.chosen.set({});
      this.typedName.set(null);
    }
  }

  protected stop(): void {
    void this.authoring.solution('stop', null);
  }
}
