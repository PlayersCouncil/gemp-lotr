import { Component, computed, inject, input, signal } from '@angular/core';
import { DraftState, FoundCard } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { PuzzleSolution } from '../../../../core/game/authoring/puzzle-state';
import {
  answerLines,
  answersCount,
  picksLine,
  SolutionChange,
  solutionNameProblem,
  solutionsWith,
  solvedBy,
  verdictLine,
} from '../../../../core/game/authoring/puzzle-solutions';
import { Notifier } from '../../../../core/notifier/notifier';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { askAuthor } from '../widgets/author-confirm';
import { AuthorKeepDirective } from '../widgets/author-keep.directive';
import { AuthorSendDirective } from '../widgets/author-send.directive';
import { ShownPick, SlotPicksComponent } from './slot-picks.component';
import { sendTop } from './top';

/**
 * One of a puzzle's solutions in the Scenario tab: its name (✎ renames it), whether the game in hand is played with
 * its cards, its picks by name, how many answers it has, and what the runner said of it (the last Check; the game in
 * hand's verdict for the solution it is played with); Play (a fresh game, the script and its answers playing
 * themselves), Use (the draft's games played with its cards), × (asked first). ▸ opens what it is made of: its note,
 * the card in each slot, its answers (a line each) — each sent as the solutions as the file has them.
 *
 * DOM contract: `div.gd-author-solution` (`data-name`; `data-playing` when the game in hand has its cards;
 * `data-solved` = yes, no once the runner has said) holding `.gd-author-solution__head` (`.gd-author-solution__name`
 * or `input[data-part=solution-rename]`, `.gd-author-solution__tag`, `button[data-author]`: rename, play, use,
 * details, delete), `.gd-author-solution__picks`, `.gd-author-solution__answers`, `.gd-author-solution__verdict`
 * once said; opened, `.gd-author-solution__details` with `textarea[data-part]` solution-note, solution-answers and
 * `gd-author-slot-picks`.
 */
@Component({
  selector: 'gd-author-solution',
  styleUrl: './solution-row.component.css',
  imports: [AuthorButtonDirective, AuthorKeepDirective, AuthorSendDirective, SlotPicksComponent],
  host: { style: 'display: contents' },
  template: `
    @let one = solution();
    <div
      class="gd-author-solution"
      [attr.data-name]="one.name"
      [attr.data-playing]="playing() ? '' : null"
      [attr.data-solved]="said() === null ? null : solved() ? 'yes' : 'no'"
    >
      <div class="gd-author-solution__head">
        @if (renaming()) {
          <input
            #rename
            type="text"
            class="gd-author-input"
            data-part="solution-rename"
            aria-label="New name"
            [attr.data-bad]="renameProblem() !== null ? '' : null"
            [title]="renameProblem() ?? 'Enter: rename it. Escape: leave it.'"
            [value]="one.name"
            (input)="typed.set(rename.value)"
            (keydown.enter)="renameTo(rename.value)"
            (keydown.escape)="renaming.set(false)"
          />
        } @else {
          <span class="gd-author-solution__name" [title]="one.note ?? ''">{{ one.name }}</span>
          @if (playing()) {
            <span class="gd-author-solution__tag" title="The game in hand has its cards in the slots">in play</span>
          }
          <button gdAuthorButton="inline" data-author="rename" title="Rename it" (click)="startRename()">✎</button>
        }
        <span class="gd-author-solution__actions">
          <button
            gdAuthorButton
            data-author="play"
            title="A fresh game in which the script plays itself, and this solution's answers for the player as far as they go"
            (click)="act('play')"
          >
            Play
          </button>
          <button
            gdAuthorButton
            data-author="use"
            title="Play the draft's games with these cards in the slots (a fresh game at the step you are at)"
            [disabled]="playing()"
            (click)="act('use')"
          >
            Use
          </button>
          <button
            gdAuthorButton="inline"
            data-author="details"
            [attr.aria-expanded]="open()"
            [title]="open() ? 'Close' : 'Its note, cards and answers'"
            (click)="open.set(!open())"
          >
            {{ open() ? '▾' : '▸' }}
          </button>
          <button gdAuthorButton="danger" data-author="delete" title="Delete this solution" (click)="remove()">
            ×
          </button>
        </span>
      </div>
      @if (one.picks && picks() !== '') {
        <div class="gd-author-solution__picks" [title]="picks()">{{ picks() }}</div>
      }
      <div class="gd-author-solution__answers" [title]="one.answers.join('\\n')">{{ count() }}</div>
      @if (said(); as verdict) {
        <div class="gd-author-solution__verdict" [title]="verdict">{{ verdict }}</div>
      }
      @if (open()) {
        <div class="gd-author-solution__details">
          <textarea
            class="gd-author-input"
            rows="2"
            data-part="solution-note"
            aria-label="Note"
            title="A note on it, for whoever works on the puzzle (never shown to a player)"
            placeholder="note"
            [value]="one.note ?? ''"
            [gdAuthorKeep]="'solution.' + one.name + '.note'"
            gdAuthorSend
            (sent)="change({ note: $event })"
          ></textarea>
          @if (slots().length > 0) {
            <gd-author-slot-picks [slots]="slots()" [picks]="shownPicks()" (picked)="pick($event)" />
          }
          <textarea
            class="gd-author-input gd-author-code"
            rows="3"
            data-part="solution-answers"
            aria-label="Answers"
            title="The player's answers to their decisions the script leaves open, in order: one or more to a line, as a step's answer (use(trap), choose(ally), pass)"
            [value]="one.answers.join('\\n')"
            [gdAuthorKeep]="'solution.' + one.name + '.answers'"
            gdAuthorSend
            (sent)="change({ answers: lines($event) })"
          ></textarea>
        </div>
      }
    </div>
  `,
})
export class SolutionRowComponent {
  private readonly authoring = inject(Authoring);
  private readonly notifier = inject(Notifier);
  readonly state = input.required<DraftState>();
  readonly solution = input.required<PuzzleSolution>();
  /** What the last report said of it, or null. */
  readonly verdict = input<string | null>(null);

  protected readonly open = signal(false);
  protected readonly renaming = signal(false);
  protected readonly typed = signal('');

  protected readonly playing = computed(() => this.state().puzzle?.playingWith === this.solution().name);
  protected readonly picks = computed(() => picksLine(this.solution()));
  protected readonly count = computed(() => answersCount(this.solution()));
  protected readonly slots = computed(() => this.state().puzzle?.slots ?? []);
  /** The game in hand's verdict when it is played with it; else what the last Check said. */
  protected readonly said = computed(() => {
    const puzzle = this.state().puzzle;
    return (puzzle ? verdictLine(puzzle, this.solution().name) : null) ?? this.verdict();
  });
  protected readonly solved = computed(() => {
    const said = this.said() ?? '';
    return solvedBy(said) || said.startsWith('won');
  });
  protected readonly renameProblem = computed(() =>
    solutionNameProblem(
      this.typed(),
      (this.state().puzzle?.solutions ?? []).map((one) => one.name),
      this.solution().name,
    ),
  );
  protected readonly shownPicks = computed(() => {
    const solution = this.solution();
    const shown: Record<string, ShownPick> = {};
    for (const [slot, blueprint] of Object.entries(solution.picks)) {
      shown[slot] = { blueprint, name: solution.pickNames[slot] ?? null };
    }
    return shown;
  });

  protected lines(typed: string): string[] {
    return answerLines(typed);
  }

  protected act(op: 'play' | 'use'): void {
    void this.authoring.solution(op, this.solution().name);
  }

  protected async remove(): Promise<void> {
    const name = this.solution().name;
    if (!(await askAuthor(this.notifier, `Delete the solution ${name}?`, 'Delete'))) return;
    await this.authoring.solution('delete', name);
  }

  protected startRename(): void {
    this.typed.set(this.solution().name);
    this.renaming.set(true);
  }

  protected async renameTo(typed: string): Promise<void> {
    const name = typed.trim();
    if (name === this.solution().name) {
      this.renaming.set(false);
      return;
    }
    if (this.renameProblem() !== null) return;
    if (await this.change({ name })) this.renaming.set(false);
  }

  protected pick({ slot, card }: { slot: string; card: FoundCard }): void {
    void this.change({ pick: { slot, blueprint: card.id } });
  }

  /** Sends the solutions with this one changed. */
  protected async change(change: SolutionChange): Promise<boolean> {
    const solutions = solutionsWith(this.state().draft, this.solution().name, change);
    return (await sendTop(this.authoring, 'solutions', solutions)) !== null;
  }
}
