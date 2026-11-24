import { afterRenderEffect, Component, computed, ElementRef, inject, viewChild } from '@angular/core';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import {
  afterSelected,
  beforeSelected,
  blankStep,
  blockStep,
  coversUncovered,
  mayAddOpening,
  mayRewrite,
  openingStep,
  pauseStep,
} from '../../../core/game/authoring/step-ops';
import { stepsOf } from '../../../core/game/authoring/steps';
import { SILENT, timelineEnd, TimelineItem, timelineItems } from '../../../core/game/authoring/timeline';
import { AuthorButtonDirective } from './widgets/author-button.directive';
import { AuthorCheckComponent } from './widgets/author-check.component';

/** The list is brought to where the game is with this much of the list above it (px). */
const CURRENT_MARGIN = 60;

/**
 * The authoring panel's Timeline tab: getting about the script (each a fresh game, played by the script up to the
 * step: ⏮ the start, ⚑ the last checkpoint, ◀ back one, ↻ this step, ⏭ the end), the panel's options, the steps as a
 * list grouped by turn and phase (each phase's colour down the rows' edge), with ▶ on each row to play to it and a
 * click to turn to it in the Step tab, what is said under the last row, and the buttons that put steps in, rewrite the
 * script from where the game is, and test the draft. The list follows the game (when "Follow the game" is ticked) and
 * otherwise stays where it was scrolled.
 *
 * DOM contract: `gd-game-author-timeline` holding `.gd-author-timeline__nav` (`button[data-go]`: start, checkpoint,
 * back, this, end), `.gd-author-timeline__options` (`gd-author-check`s, `data-option`: recording, ask-opponent,
 * follow; in a puzzle whose author has not said, `.gd-author-note[data-note=recording]` "Off in a puzzle"), `.gd-author-timeline__way` (the forks' flags, `data-flag`), `.gd-author-timeline__list` with
 * `.gd-author-timeline__group` headings (`data-phase`, `data-even`) and `.gd-author-timeline__row`s (`data-step` the
 * number, `data-status`, `data-phase`, `data-even`, `data-sub` for a block's step, `data-block`, `data-current`,
 * `data-selected`, `data-silent`) each with `button.gd-author-timeline__pick` (`.gd-author-timeline__num`,
 * `.gd-author-timeline__who[data-who]`, `.gd-author-timeline__what` with `__when`, `__act`, `__id`;
 * `.gd-author-timeline__tags` with a `.gd-author-timeline__tag[data-tag]` per mark, `data-tag=role` first in a game
 * of two sides where the step's seat plays the role its name does not say) and `button.gd-author-timeline__go`
 * (▶); then `.gd-author-timeline__end` (`data-current` past the script's end; `button[data-author=add-here]`), and
 * `.gd-author-timeline__foot` (`button[data-add]`: opening, pause, step, block, skirmishes; `button[data-author]`:
 * rewrite, test-here, test-start).
 */
@Component({
  selector: 'gd-game-author-timeline',
  styleUrl: './timeline-tab.component.css',
  imports: [AuthorButtonDirective, AuthorCheckComponent],
  template: `
    @if (authoring.draft(); as draft) {
      <div class="gd-author-timeline__nav gd-author-bar" role="toolbar" aria-label="Go to">
        <button gdAuthorButton data-go="start" title="A fresh game from step 1" (click)="authoring.goTo(1)">
          ⏮ Start
        </button>
        <button
          gdAuthorButton
          data-go="checkpoint"
          title="A fresh game, played by the script up to the last checkpoint before here"
          (click)="authoring.goTo(authoring.lastCheckpoint(false))"
        >
          ⚑ Checkpoint
        </button>
        <button
          gdAuthorButton
          data-go="back"
          title="A fresh game, played by the script up to the step before this one"
          (click)="authoring.goTo(back())"
        >
          ◀ Back one
        </button>
        <button
          gdAuthorButton
          data-go="this"
          title="A fresh game, played by the script up to the step the game is at now"
          (click)="authoring.goTo(draft.position.step)"
        >
          ↻ This step
        </button>
        <button
          gdAuthorButton
          data-go="end"
          title="A fresh game, with the whole script played"
          (click)="authoring.goTo(count() + 1)"
        >
          ⏭ End
        </button>
      </div>
      <div class="gd-author-timeline__options">
        <gd-author-check
          data-option="recording"
          label="Write down what I do"
          hint="Where the script has nothing to say, what you do (and what you have the opponent do) becomes steps"
          [checked]="draft.recording"
          (changed)="authoring.settings({ recording: $event })"
        />
        @if (draft.draft.kind === 'puzzle' && draft.recordingSaid !== true) {
          <span
            class="gd-author-note"
            data-note="recording"
            title="A puzzle is played by its solutions: steps written for what you do would take the decisions they answer. Tick it to write steps on purpose."
            >Off in a puzzle</span
          >
        }
        <gd-author-check
          data-option="ask-opponent"
          label="Ask me for the opponent"
          hint="Where the script does not answer for the opponent, it waits for you (see the Opponent tab). Otherwise it does what its orders and its fallback say."
          [checked]="draft.askOpponent"
          (changed)="authoring.settings({ askOpponent: $event })"
        />
        <gd-author-check
          data-option="follow"
          label="Follow the game"
          hint="When the game comes to another step, that step is selected (and the Step tab turns to it)"
          [checked]="authoring.ui().follow"
          (changed)="authoring.setFollow($event)"
        />
      </div>
      @if (draft.flags.length > 0) {
        <div class="gd-author-timeline__way">
          <span
            class="gd-author-label"
            title="Which of a pause's choices the script takes when it plays itself to a step. Without one, it takes the first."
            >At the forks, choose</span
          >
          @for (flag of draft.flags; track flag) {
            <gd-author-check
              [attr.data-flag]="flag"
              [label]="flag"
              [checked]="draft.way.includes(flag)"
              (changed)="choose(flag, $event)"
            />
          }
        </div>
      }
      <div #list class="gd-author-timeline__list">
        @for (item of items(); track keyOf(item, $index)) {
          @if (item.kind === 'group') {
            <div
              class="gd-author-timeline__group"
              [attr.data-phase]="item.phase"
              [attr.data-even]="item.even ? '' : null"
            >
              {{ item.label }}
            </div>
          } @else {
            <div
              class="gd-author-timeline__row"
              [attr.data-step]="item.number"
              [attr.data-status]="item.status"
              [attr.data-phase]="item.phase"
              [attr.data-even]="item.even ? '' : null"
              [attr.data-sub]="item.sub > -1 ? '' : null"
              [attr.data-block]="item.block ? '' : null"
              [attr.data-current]="item.current ? '' : null"
              [attr.data-selected]="item.selected ? '' : null"
              [attr.data-silent]="item.silent ? '' : null"
              [title]="item.silent ? silent : ''"
            >
              <button
                type="button"
                class="gd-author-timeline__pick"
                [attr.aria-current]="item.current ? 'step' : null"
                [attr.aria-pressed]="item.selected"
                (click)="authoring.select(item.index, item.sub > -1 ? item.uid : null)"
              >
                <span class="gd-author-timeline__num">{{ item.number }}</span>
                <span class="gd-author-timeline__who" [attr.data-who]="item.summary.who">{{ item.summary.who }}</span>
                <span class="gd-author-timeline__what">
                  <span class="gd-author-timeline__when">{{ item.summary.when }}</span>
                  @if (item.summary.act !== '') {
                    <span class="gd-author-timeline__act">{{ item.summary.act }}</span>
                  }
                  @if (item.id !== null) {
                    <span class="gd-author-timeline__id">{{ item.id }}</span>
                  }
                </span>
                <span class="gd-author-timeline__tags">
                  @if (item.role) {
                    <span
                      class="gd-author-timeline__tag"
                      data-tag="role"
                      [title]="
                        item.role === 'shadow'
                          ? 'Played as the Shadow player: on the other seat’s turn'
                          : 'Played as the Free Peoples player: on this seat’s own turn'
                      "
                      >as {{ item.role === 'shadow' ? 'Shadow' : 'FP' }}</span
                    >
                  }
                  @for (tag of item.tags; track tag.kind) {
                    <span class="gd-author-timeline__tag" [attr.data-tag]="tag.kind" [title]="tag.title">{{
                      tag.text
                    }}</span>
                  }
                </span>
              </button>
              <button
                gdAuthorButton
                class="gd-author-timeline__go"
                [title]="
                  item.sub > -1
                    ? 'A fresh game, played by the script up to the first decision this step takes'
                    : 'A fresh game, played by the script up to this step'
                "
                [attr.aria-label]="'Play to step ' + item.number"
                (click)="authoring.goTo(item.index + 1, item.sub > -1 ? item.sub + 1 : null)"
              >
                ▶
              </button>
            </div>
          }
        }
        @if (end(); as end) {
          <div class="gd-author-timeline__end" [attr.data-current]="end.frontier !== null ? '' : null">
            @if (end.over) {
              {{ end.over }}
            }
            @if (end.frontier) {
              <div>{{ end.frontier }}</div>
            }
            @if (end.asked) {
              <div class="gd-author-timeline__asked">{{ end.asked }}</div>
              <button
                gdAuthorButton="primary"
                data-author="add-here"
                title="A step to hang messages on before the decision is answered; its answer is filled in when it is given"
                (click)="authoring.stepHere()"
              >
                Add a step for this decision
              </button>
            }
          </div>
        }
      </div>
      <div class="gd-author-timeline__foot gd-author-bar">
        @if (opening()) {
          <button
            gdAuthorButton
            data-add="opening"
            title="A first step that is read before the first turn begins, on the board as the scenario lays it out: nothing drawn, nothing asked. What it does (drawing a hand, say) it does once it has been read."
            (click)="authoring.insertStep(0, openingStep())"
          >
            + Opening
          </button>
        }
        <button
          gdAuthorButton
          data-add="pause"
          title="A step that is only read, put in before the selected step (or at the end)"
          (click)="addPause()"
        >
          + Pause
        </button>
        <button
          gdAuthorButton
          data-add="step"
          title="A step after the selected one. It says nothing yet: when the game comes to it, what you do there is written in as what it allows. (After the step the game is at, with a decision waiting that no step covers, it is the step for that decision.)"
          (click)="addStep()"
        >
          + Step after
        </button>
        <button
          gdAuthorButton
          data-add="block"
          title="A block, after the selected step: steps for a stretch of the game whose decisions come in no order the script can know (which skirmish is fought first, how often each side acts in it). Each decision goes to the first of the block's steps that fits it, until one arrives that fits the block's Until. It starts as this turn, until the regroup phase; with the game in it, what you do is written into it."
          (click)="addBlock(false)"
        >
          + Block
        </button>
        <button
          gdAuthorButton
          data-add="skirmishes"
          title="A turn's assignment and skirmishes, after the selected step: a block whose steps are shown skirmish by skirmish (whichever is fought first), and what comes after each. It starts as this turn, until the regroup phase; with the game in it, what you do is written into the skirmish you do it in."
          (click)="addBlock(true)"
        >
          + Skirmishes
        </button>
        <button
          gdAuthorButton="danger"
          data-author="rewrite"
          title="Take the step the game is at, and every step after it, out of the script, and play on from here writing new ones"
          [disabled]="!rewrite()"
          (click)="authoring.rewriteFromHere()"
        >
          Rewrite from here
        </button>
        <button
          gdAuthorButton
          data-author="test-here"
          title="A player's game of the draft from the selected step: no panel, no author"
          (click)="authoring.startTest(authoring.ui().selected ?? 1)"
        >
          Test from here
        </button>
        <button
          gdAuthorButton
          data-author="test-start"
          title="A player's game of the draft"
          (click)="authoring.startTest(1)"
        >
          Test from the start
        </button>
      </div>
    }
  `,
})
export class TimelineTabComponent {
  protected readonly authoring = inject(Authoring);
  protected readonly silent = SILENT;
  protected readonly openingStep = openingStep;
  private readonly list = viewChild<ElementRef<HTMLElement>>('list');

  protected readonly items = computed(() => {
    const draft = this.authoring.draft();
    const { selected, sub } = this.authoring.ui();
    return draft ? timelineItems(draft, { selected, sub }) : [];
  });
  protected readonly end = computed(() => {
    const draft = this.authoring.draft();
    return draft ? timelineEnd(draft) : null;
  });
  protected readonly count = computed(() => {
    const draft = this.authoring.draft();
    return draft ? stepsOf(draft).length : 0;
  });
  protected readonly back = computed(() => Math.max(1, (this.authoring.draft()?.position.step ?? 1) - 1));
  protected readonly opening = computed(() => {
    const draft = this.authoring.draft();
    return draft !== null && mayAddOpening(draft);
  });
  protected readonly rewrite = computed(() => {
    const draft = this.authoring.draft();
    return draft !== null && mayRewrite(draft);
  });
  /** Where the game is (in which game): when it changes, the list is brought to it. */
  private readonly seenAt = computed(() => {
    const draft = this.authoring.draft();
    return draft ? `${draft.position.step}/${draft.position.inHand}/${draft.gameId}` : '';
  });
  private lastSeen: string | null = null;

  constructor() {
    afterRenderEffect(() => {
      const seen = this.seenAt();
      const follow = this.authoring.ui().follow;
      const list = this.list()?.nativeElement;
      if (!list || seen === this.lastSeen) return;
      this.lastSeen = seen;
      const current = list.querySelector<HTMLElement>('.gd-author-timeline__row[data-current]');
      if (follow && current) list.scrollTop = Math.max(0, current.offsetTop - CURRENT_MARGIN);
    });
  }

  protected keyOf(item: TimelineItem, index: number): string {
    return item.kind === 'group' ? `g${index}:${item.label}` : `r${item.number}:${item.uid ?? ''}`;
  }

  protected choose(flag: string, on: boolean): void {
    const way = this.authoring.draft()?.way ?? [];
    void this.authoring.settings({ way: [...way.filter((name) => name !== flag), ...(on ? [flag] : [])] });
  }

  protected addPause(): void {
    const draft = this.authoring.draft();
    if (draft) void this.authoring.insertStep(beforeSelected(draft, this.authoring.ui().selected), pauseStep());
  }

  /** "+ Step after": at the decision no step covers, the step for it; else one that waits for its answer. */
  protected addStep(): void {
    const draft = this.authoring.draft();
    if (!draft) return;
    const index = afterSelected(draft, this.authoring.ui().selected);
    if (coversUncovered(draft, index)) void this.authoring.stepHere();
    else void this.authoring.insertStep(index, blankStep(draft), true);
  }

  protected addBlock(skirmishes: boolean): void {
    const draft = this.authoring.draft();
    if (!draft) return;
    void this.authoring.insertStep(
      afterSelected(draft, this.authoring.ui().selected),
      blockStep(draft.position.turn, skirmishes),
    );
  }
}
