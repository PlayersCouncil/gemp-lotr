import { Component, computed, effect, ElementRef, inject, signal, untracked, viewChild } from '@angular/core';
import { inPlayWords, passWhenInPlay, playsOf, untilInPlay } from '../../../../core/game/authoring/step-model';
import { stepsOf } from '../../../../core/game/authoring/steps';
import { nextUntil, untilFromNext } from '../../../../core/game/authoring/terms';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { HjsonAreaDirective } from '../widgets/hjson-area.directive';
import { StepEditor } from './step-editor';
import { StepFieldDirective } from './step-field.directive';

const blank = (value: string | undefined) => (value ?? '').trim() === '';

/**
 * "Repeat until" (legacy repeatRow): a step of the script that goes on applying to every decision that fits its
 * When, until one arrives that fits its Until (which goes to the step after it). Ticked, Until is filled in with
 * the When of the next step that waits for something else, or the phase after the step's (or the next turn); the
 * field is given the focus when nothing can be guessed. While it repeats: Pass only once the cards it plays are in
 * play (Pass when), or the step goes on until they are (Until written out with them).
 *
 * DOM contract: `gd-author-repeat` holding `.gd-author-repeat` with `input[data-flag=repeat]`, the Until field
 * (`data-field=until`) and, while it repeats, `.gd-author-repeat__more` with `button[data-act]` pass-once,
 * until-in-play.
 */
@Component({
  selector: 'gd-author-repeat',
  styleUrl: './repeat-row.component.css',
  imports: [AuthorButtonDirective, HjsonAreaDirective, StepFieldDirective],
  template: `
    @if (view(); as view) {
      <div class="gd-author-repeat">
        <div class="gd-author-repeat__line">
          <label
            class="gd-author-check"
            title="Go on applying to every decision that fits When, not only the first, until a decision arrives that fits what is given here"
          >
            <input type="checkbox" data-flag="repeat" [checked]="view.on" (change)="tick(view.guess, $event)" />
            repeat until
          </label>
          <textarea
            #until
            gdHjsonArea
            gdStepField="until"
            rows="1"
            aria-label="Until"
            title="The first decision that fits this ends the repeating, and goes to the step after this one. Written like When: phase(shadow), turn(4), freeps, action, phase(regroup) ... or in full as the file has it, in HJSON."
            [text]="view.until"
          ></textarea>
        </div>
        @if (view.on) {
          <div class="gd-author-repeat__more">
            <div
              class="gd-author-note"
              title="It applies to every decision that fits When, until one arrives that fits this, which goes to the next step. Play it through as the player should: each thing you do is added to Answer, in order, and to Allow."
            >
              Play it through: each thing you do is added to Answer and Allow.
            </div>
            @if (view.passOnce) {
              <div class="gd-author-bar">
                <button
                  gdAuthorButton
                  data-act="pass-once"
                  [title]="
                    view.plays.length === 0
                      ? 'Allow play(card) first: this makes Pass wait until each card played here is on the table'
                      : 'Sets Pass when: Pass is not allowed until each of these is on the table. (Say what to tell a player who passes too early in Otherwise.)'
                  "
                  [disabled]="view.plays.length === 0"
                  (click)="passOnce(view.plays)"
                >
                  Pass only once {{ view.words }} in play
                </button>
              </div>
            }
            <div class="gd-author-bar">
              <button
                gdAuthorButton
                data-act="until-in-play"
                [title]="
                  view.plays.length === 0
                    ? 'Allow play(card) first: this makes the step go on until each card played here is on the table'
                    : 'Sets Until: the step goes on until each of these is on the table, and the player’s next decision after that goes to the next step'
                "
                [disabled]="view.plays.length === 0 || view.until === view.inPlay"
                (click)="untilPlayed(view.inPlay)"
              >
                Until {{ view.words }} in play
              </button>
            </div>
          </div>
        }
      </div>
    }
  `,
})
export class RepeatRowComponent {
  protected readonly editor = inject(StepEditor);
  private readonly untilField = viewChild<ElementRef<HTMLTextAreaElement>>('until');
  /** Ticked with nothing to fill Until with: it stays ticked while the author writes one. */
  private readonly ticked = signal(false);

  protected readonly view = computed(() => {
    const work = this.editor.work();
    const place = this.editor.place();
    const draft = this.editor.authoring.draft();
    if (!work || !place || !draft) return null;
    const plays = playsOf(work);
    const until = work.until ?? '';
    return {
      // (ticked: Until is filled in with what comes next, or what the When's phase or turn has after it)
      guess: untilFromNext(stepsOf(draft), place.index) ?? nextUntil(work.when),
      until,
      on: !blank(until) || this.ticked(),
      plays,
      words: inPlayWords(plays),
      passOnce: blank(work.allow.passWhen),
      inPlay: untilInPlay(draft.draft.human ?? 'freeps', plays),
    };
  });

  constructor() {
    // (another step's form: its own box)
    effect(() => {
      this.editor.form();
      untracked(() => this.ticked.set(false));
    });
  }

  protected tick(guess: string, event: Event): void {
    const on = (event.target as HTMLInputElement).checked;
    this.ticked.set(on);
    if (on && blank(guess)) {
      this.untilField()?.nativeElement.focus();
      return;
    }
    this.editor.put('until', on ? guess : '');
    this.editor.now();
  }

  protected passOnce(plays: readonly string[]): void {
    this.editor.put('allow.passWhen', passWhenInPlay(plays));
    this.editor.wrongClicksOpen.set(true);
    this.editor.now();
  }

  protected untilPlayed(until: string): void {
    this.editor.put('until', until);
    this.editor.now();
  }
}
