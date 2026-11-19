import { Component, computed, inject } from '@angular/core';
import {
  alikeAfter,
  blankStep,
  convertOp,
  coversUncovered,
  earlierOp,
  intoBlockOp,
  joinOps,
  laterOp,
  nearSkirmishes,
} from '../../../../core/game/authoring/step-ops';
import { isBlock, isOpening, leftOutOf, stepsOf } from '../../../../core/game/authoring/steps';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorSelectComponent } from '../widgets/author-select.component';
import { StepEditor } from './step-editor';

/**
 * What can be done with the step as a whole (a step of the script, or a block; legacy buildStepForm's bar): a fresh
 * game played to it, moved earlier or later, a new step before or after it, taken out, the steps after it that wait
 * for the same joined into it, turned into a block (or a turn's skirmishes), a block turned into the skirmishes, made
 * one of the steps of the block beside it; and what of it was left out when the file was opened. Each sends the form
 * first, and stops if the server refuses it.
 *
 * DOM contract: `gd-author-step-actions` holding `.gd-author-bar` with `button[data-act]` (play, earlier, later,
 * before, after, delete, join, convert, skirmishes, into-before, into-after) and `gd-author-select[data-act=convert]`
 * where a step near the skirmishes can become either; then a `.gd-author-step-actions__left-out` per part left out
 * (with its `pre`).
 */
@Component({
  selector: 'gd-author-step-actions',
  styleUrl: './step-actions.component.css',
  imports: [AuthorButtonDirective, AuthorSelectComponent],
  template: `
    @if (acts(); as acts) {
      <div class="gd-author-bar">
        <button
          gdAuthorButton
          data-act="play"
          title="A fresh game, played by the script up to this step"
          (click)="editor.authoring.goTo(acts.number)"
        >
          ↻ Play to here
        </button>
        <button
          gdAuthorButton
          data-act="earlier"
          title="Move this step earlier"
          [disabled]="acts.index === 0"
          (click)="move(-1)"
        >
          ↑
        </button>
        <button
          gdAuthorButton
          data-act="later"
          title="Move this step later"
          [disabled]="acts.index >= acts.count - 1"
          (click)="move(1)"
        >
          ↓
        </button>
        <button
          gdAuthorButton
          data-act="before"
          title="A new step before this one. It says nothing yet: when the game comes to it, what you do there is written in as what it allows."
          (click)="blank(acts.index)"
        >
          Before
        </button>
        <button
          gdAuthorButton
          data-act="after"
          title="A new step after this one. It says nothing yet: when the game comes to it, what you do there is written in as what it allows."
          (click)="blank(acts.index + 1)"
        >
          After
        </button>
        <button
          gdAuthorButton="danger"
          data-act="delete"
          [title]="
            acts.block ? 'Take this block, and the steps in it, out of the script' : 'Take this step out of the script'
          "
          (click)="remove()"
        >
          Delete
        </button>
        @if (acts.alike > 0) {
          <button
            gdAuthorButton
            data-act="join"
            [title]="
              'The step' +
              (acts.alike === 1 ? ' after this one waits' : 's after this one wait') +
              ' for the same as it does. Joined, this step repeats until the step after them comes: however many of these decisions there are, one or several, they are all this step’s. What each allowed it allows, and their answers are its answers, in order.'
            "
            (click)="join(acts.alike)"
          >
            Join next{{ acts.alike === 1 ? '' : ' ' + acts.alike }}
          </button>
        }
        @if (acts.convert === 'block') {
          <button
            gdAuthorButton
            data-act="convert"
            title="Turn this step into a block, with this step as its first step: for a stretch of the game whose decisions come in no order the script can know. It waits for the turn (and phase) this step does, and ends where the step after it begins."
            (click)="convert(false)"
          >
            Convert
          </button>
        } @else if (acts.convert === 'either') {
          <span
            class="gd-author-step-actions__convert"
            title="Turn this step into a block with this step as its first step: an ordinary one (Block), or a turn's skirmishes, shown skirmish by skirmish (Skirmishes)"
          >
            <gd-author-select
              data-act="convert"
              label="Convert"
              [options]="convertOptions"
              value=""
              (changed)="$event !== '' && convert($event === 'skirmishes')"
            />
          </span>
        } @else if (acts.convert === 'skirmishes') {
          <button
            gdAuthorButton
            data-act="skirmishes"
            title="Show this block's steps skirmish by skirmish, as a turn's assignment and skirmishes are written: what it waits for and ends at stays as it is."
            (click)="toSkirmishes()"
          >
            Convert to Skirmishes
          </button>
        }
        @if (acts.intoBefore) {
          <button
            gdAuthorButton
            data-act="into-before"
            title="Make this step the last of the steps in the block before it"
            (click)="into(acts.index - 1)"
          >
            Into block {{ acts.index }}
          </button>
        }
        @if (acts.intoAfter) {
          <button
            gdAuthorButton
            data-act="into-after"
            title="Make this step the first of the steps in the block after it"
            (click)="into(acts.index + 1)"
          >
            Into block {{ acts.number + 1 }}
          </button>
        }
      </div>
      @for (part of acts.leftOut; track $index) {
        <div class="gd-author-step-actions__left-out" title="Left out when the file was opened">
          <div>Left out{{ part.part === '' ? '' : " of '" + part.part + "'" }}: {{ part.message }}</div>
          <pre>{{ part.was }}</pre>
        </div>
      }
    }
  `,
})
export class StepActionsComponent {
  protected readonly editor = inject(StepEditor);
  protected readonly convertOptions = [
    ['', 'Convert…'],
    ['block', 'Block'],
    ['skirmishes', 'Skirmishes'],
  ] as const;

  protected readonly acts = computed(() => {
    const draft = this.editor.authoring.draft();
    const place = this.editor.place();
    if (!draft || !place) return null;
    const steps = stepsOf(draft);
    const index = place.index;
    const top = steps[index];
    if (!top) return null;
    const block = isBlock(top);
    const opening = isOpening(top);
    const skirmishing = nearSkirmishes(draft, index);
    // (a step of the script can become a block, or near the skirmishes either kind; a block there, the skirmishes)
    const convert =
      place.sub > -1 || opening
        ? null
        : !block
          ? skirmishing
            ? 'either'
            : 'block'
          : !top.skirmishes && skirmishing
            ? 'skirmishes'
            : null;
    return {
      index,
      number: index + 1,
      count: steps.length,
      block,
      alike: alikeAfter(draft, index),
      convert,
      intoBefore: !block && !opening && index > 0 && isBlock(steps[index - 1]),
      intoAfter: !block && !opening && index < steps.length - 1 && isBlock(steps[index + 1]),
      leftOut: leftOutOf(draft, draft.uids[index] ?? null).map((one) => ({
        ...one,
        part: one.where.replace(/^step \d+( \([^)]*\))?\.?/, '').trim(),
      })),
    };
  });

  /** Sends the form; then, if it went, does what was asked. */
  private async then(action: () => Promise<unknown>): Promise<void> {
    if (await this.editor.send()) await action();
  }

  protected move(by: -1 | 1): void {
    const draft = this.editor.authoring.draft();
    const index = this.editor.place()?.index;
    if (!draft || index === undefined) return;
    const number = index + 1;
    void this.then(() =>
      by < 0
        ? this.editor.authoring.changeSteps([earlierOp(draft, index)], number - 1, number - 1)
        : this.editor.authoring.changeSteps([laterOp(draft, index)], number, number + 1),
    );
  }

  /** A new step at `at` (from 0): the step for the decision no step covers, where that is the place for it. */
  protected blank(at: number): void {
    const draft = this.editor.authoring.draft();
    if (!draft) return;
    if (coversUncovered(draft, at)) void this.editor.authoring.stepHere();
    else void this.editor.authoring.insertStep(at, blankStep(draft), true);
  }

  protected async remove(): Promise<void> {
    const draft = this.editor.authoring.draft();
    const index = this.editor.place()?.index;
    const top = index === undefined ? undefined : draft?.draft.steps?.[index];
    if (!draft || index === undefined || !top) return;
    const number = index + 1;
    const kids = top.anyOrder?.length ?? 0;
    const question = isBlock(top)
      ? `Take step ${number} out of the script, with the ${kids} step${kids === 1 ? '' : 's'} in it?`
      : `Take step ${number} out of the script?`;
    if (!(await this.editor.ask(question, 'Delete'))) return;
    const uid = draft.uids[index] ?? '';
    await this.editor.authoring.changeSteps([{ op: 'delete', uid }], number, Math.max(1, number - 1));
  }

  protected join(count: number): void {
    void this.then(async () => {
      const draft = this.editor.authoring.draft();
      const index = this.editor.place()?.index;
      if (!draft || index === undefined) return;
      await this.editor.authoring.changeSteps(joinOps(draft, index, count), index + 1, index + 1);
    });
  }

  protected convert(skirmishes: boolean): void {
    void this.then(async () => {
      const draft = this.editor.authoring.draft();
      const index = this.editor.place()?.index;
      const op = draft && index !== undefined ? convertOp(draft, index, skirmishes) : null;
      if (op && index !== undefined) await this.editor.authoring.changeSteps([op], index + 1, index + 1);
    });
  }

  protected toSkirmishes(): void {
    void this.then(async () => {
      const uid = this.editor.authoring.draft()?.uids[this.editor.place()?.index ?? -1];
      if (uid !== undefined)
        await this.editor.authoring.edit({ steps: [{ op: 'merge', uid, set: { skirmishes: true } }] });
    });
  }

  /** Made one of the steps of the block at `toIndex` (from 0): its last (the block before), or its first (after). */
  protected into(toIndex: number): void {
    void this.then(async () => {
      const draft = this.editor.authoring.draft();
      const index = this.editor.place()?.index;
      if (!draft || index === undefined) return;
      const { op, blockAt, subAt, firstChanged } = intoBlockOp(draft, index, toIndex);
      this.editor.authoring.remember({ subAt });
      await this.editor.authoring.changeSteps([op], firstChanged, blockAt + 1);
    });
  }
}
