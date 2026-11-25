import { Component, computed, inject } from '@angular/core';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import {
  leftOutNotice,
  mayRetake,
  problemsNotice,
  uncoveredNotice,
  uncoveredOf,
} from '../../../core/game/authoring/notices';
import { AuthorButtonDirective } from './widgets/author-button.directive';

/**
 * What the authoring panel says above its tabs, top to bottom: what went wrong with the last call; the parts of the
 * file left out so that it could be opened; the decision that is waiting with no step to cover it (and the button
 * that writes one); the server's last notice (where the script stopped: "Answer it again" for a step it could not
 * play); the latest of what went wrong with the script in this game; how the last save went; and the instruction for
 * what the author is doing (picking something on the table). Each that can be put away has its ×.
 *
 * DOM contract: `gd-game-author-notices` holding `.gd-author-notices` (absent with nothing to say) with a
 * `.gd-author-notice` per notice (`data-notice`: problem, left-out, uncovered, notice, problems, saved, told, recording; `data-tone`:
 * error, warning, good, told, uncovered), each with its `button.gd-author-notice__dismiss` where it can be put away;
 * the left-out notice's `.gd-author-notice__head` and a `.gd-author-notice__part` per part (`.gd-author-notice__where`,
 * `button[data-author=show]`, `pre.gd-author-notice__was`); the uncovered one's `.gd-author-notice__head`,
 * `.gd-author-notice__how` (`button[data-author=add-here]`) and `.gd-author-notice__or`; `button[data-author=retake]`;
 * `button[data-author=see-why]`; the recording one's `button[data-author=stop-recording]`.
 */
@Component({
  selector: 'gd-game-author-notices',
  styleUrl: './authoring-notices.component.css',
  imports: [AuthorButtonDirective],
  template: `
    @if (any()) {
      <div class="gd-author-notices">
        @if (problemLines(); as lines) {
          <div class="gd-author-notice" data-notice="problem" data-tone="error">
            @for (line of lines; track $index) {
              <div class="gd-author-notice__text">{{ line }}</div>
            }
            <button
              class="gd-author-notice__dismiss"
              gdAuthorButton
              title="Dismiss"
              (click)="authoring.dismissProblem()"
            >
              ×
            </button>
          </div>
        }
        @if (leftOut(); as notice) {
          <div class="gd-author-notice" data-notice="left-out" data-tone="error">
            <div class="gd-author-notice__head">{{ notice.head }}</div>
            @for (part of notice.lines; track $index) {
              <div class="gd-author-notice__part">
                <span class="gd-author-notice__where">{{ part.where }}</span
                >: {{ part.message }}
                @if (part.step !== null) {
                  <button
                    gdAuthorButton="inline"
                    data-author="show"
                    [title]="'Go to step ' + part.step"
                    (click)="authoring.select(part.step - 1)"
                  >
                    Show
                  </button>
                }
                <pre class="gd-author-notice__was">{{ part.was }}</pre>
              </div>
            }
            <button
              class="gd-author-notice__dismiss"
              gdAuthorButton
              title="Put this away for good. What the parts said is not kept anywhere else once the draft is saved."
              (click)="authoring.seenLeftOut()"
            >
              ×
            </button>
          </div>
        }
        @if (uncovered(); as notice) {
          <div class="gd-author-notice" data-notice="uncovered" data-tone="uncovered">
            <div class="gd-author-notice__head">{{ notice.head }}</div>
            <div>{{ notice.asked }}</div>
            @if (notice.problem; as problem) {
              <div class="gd-author-notice__how">{{ problem }}</div>
            } @else {
              <div class="gd-author-notice__how">
                <button
                  gdAuthorButton="primary"
                  data-author="add-here"
                  title="Writes a step in the right place, waiting for exactly this decision. What is then done in the game becomes its answer; messages can be put on it first."
                  (click)="authoring.stepHere()"
                >
                  Add a step for this decision
                </button>
                {{ notice.how }}
              </div>
              <div class="gd-author-notice__or" [title]="notice.or.title">{{ notice.or.text }}</div>
            }
          </div>
        }
        @if (notice(); as text) {
          <div class="gd-author-notice" data-notice="notice" data-tone="warning">
            <span class="gd-author-notice__text">{{ text }}</span>
            @if (retake()) {
              <button gdAuthorButton="inline" data-author="retake" [title]="retakeTitle()" (click)="authoring.retake()">
                Answer it again
              </button>
            }
            <button class="gd-author-notice__dismiss" gdAuthorButton title="Dismiss" (click)="authoring.readNotice()">
              ×
            </button>
          </div>
        }
        @if (problems(); as text) {
          <div class="gd-author-notice" data-notice="problems" data-tone="warning">
            <span class="gd-author-notice__text">{{ text }}</span>
            <button class="gd-author-notice__dismiss" gdAuthorButton title="Dismiss" (click)="authoring.readProblems()">
              ×
            </button>
          </div>
        }
        @if (authoring.saved(); as saved) {
          <div class="gd-author-notice" data-notice="saved" [attr.data-tone]="saved.good ? 'good' : 'warning'">
            <span class="gd-author-notice__text">{{ saved.text }}</span>
            @if (saved.why && authoring.ui().tab !== 'scenario') {
              <button
                gdAuthorButton="inline"
                data-author="see-why"
                title="The whole report, under Scenario"
                (click)="seeWhy()"
              >
                See why
              </button>
            }
            <button class="gd-author-notice__dismiss" gdAuthorButton title="Dismiss" (click)="authoring.dismissSaved()">
              ×
            </button>
          </div>
        }
        @if (authoring.told(); as told) {
          <div class="gd-author-notice" data-notice="told" data-tone="told">
            <span class="gd-author-notice__text">{{ told }}</span>
          </div>
        }
        @if (recording(); as name) {
          <div class="gd-author-notice" data-notice="recording" data-tone="told">
            <span
              class="gd-author-notice__text"
              title="What you answer that the script leaves open goes into this puzzle solution, not into the script"
              >Recording solution {{ name }}</span
            >
            <button
              gdAuthorButton="inline"
              data-author="stop-recording"
              title="Stop recording (the solution stays)"
              (click)="authoring.solution('stop', null)"
            >
              Stop
            </button>
          </div>
        }
      </div>
    }
  `,
})
export class AuthoringNoticesComponent {
  protected readonly authoring = inject(Authoring);

  protected readonly problemLines = computed(() => this.authoring.problem()?.split('\n') ?? null);
  private readonly draft = computed(() => (this.authoring.view() === 'none' ? null : this.authoring.draft()));
  protected readonly leftOut = computed(() => {
    const draft = this.draft();
    return draft ? leftOutNotice(draft) : null;
  });
  private readonly open = computed(() => {
    const draft = this.draft();
    return draft ? uncoveredOf(draft) : null;
  });
  protected readonly uncovered = computed(() => {
    const draft = this.draft();
    const open = this.open();
    return draft && open ? uncoveredNotice(draft, open) : null;
  });
  /** The server's notice, unless it was put away or is where the script stopped at the decision said above. */
  protected readonly notice = computed(() => {
    const draft = this.draft();
    if (!draft?.notice || draft.notice === this.authoring.noticeRead()) return null;
    return this.open() !== null && draft.stopped === 'uncovered' ? null : draft.notice;
  });
  protected readonly retake = computed(() => {
    const draft = this.draft();
    return draft !== null && mayRetake(draft);
  });
  protected readonly retakeTitle = computed(
    () =>
      `Step ${this.draft()?.position.step ?? ''} forgets what it allows and how it is answered, and takes both from ` +
      'what you do next in the game. Its messages stay.',
  );
  protected readonly problems = computed(() => {
    const draft = this.draft();
    return draft ? problemsNotice(draft.problems, this.authoring.problemsRead()) : null;
  });
  protected readonly any = computed(
    () =>
      this.authoring.problem() !== null ||
      this.leftOut() !== null ||
      this.uncovered() !== null ||
      this.notice() !== null ||
      this.problems() !== null ||
      this.authoring.saved() !== null ||
      this.authoring.told() !== null ||
      this.recording() !== null,
  );
  /** The puzzle solution being recorded, or null. */
  protected readonly recording = computed(() => this.draft()?.puzzle?.recording ?? null);

  protected seeWhy(): void {
    this.authoring.dismissSaved();
    this.authoring.showTab('scenario');
  }
}
