import { Component, computed, inject } from '@angular/core';
import { fitsHere, SkirmishGroup, skirmishGroups, uncoveredIn } from '../../../../core/game/authoring/skirmish-page';
import { childPause } from '../../../../core/game/authoring/step-ops';
import { DraftStep } from '../../../../core/game/authoring/authoring-state';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { BlockAt } from './block-page.component';
import { BlockPanelComponent, PanelOrder } from './block-panel.component';
import { StepEditor } from './step-editor';

/**
 * A block that is a turn's skirmishes, shown the way they are fought (legacy skirmishPage): the block's own section;
 * what comes before the skirmishes (the assignment); the first choice of skirmish; steps for every skirmish; and for
 * each character's skirmish (then each minion's) its steps in the order they come, and what is said after it. Each
 * group's + Step is for the decision waiting when it is that group's and no step of the block takes it; "+ a skirmish
 * for…" gives another character (or minion) a group of its own before any step is written for it; + Pause.
 *
 * DOM contract: `gd-author-skirmish-page` holding `.gd-author-note`, the block's `gd-author-block-panel`, a
 * `.gd-author-skirmish__group` per group (`data-kind` before, first, every, fight, after; `data-who`) with its
 * `.gd-author-step__heading`, panels and `button[data-add=step]`; then `.gd-author-bar` with
 * `gd-author-select[data-add=fight]` and `button[data-add=pause]`.
 */
@Component({
  selector: 'gd-author-skirmish-page',
  styleUrl: './skirmish-page.component.css',
  imports: [AuthorButtonDirective, AuthorSelectComponent, BlockPanelComponent],
  template: `
    @if (view(); as view) {
      <div
        class="gd-author-note"
        title="The skirmishes of a turn, whichever order they are fought in: what comes first, then each character's skirmish, a step at a time in the order its decisions come, and what comes after it. What you do in the game is written into the skirmish it is done in."
      >
        A turn's skirmishes, in the order they are fought.
      </div>
      <gd-author-block-panel [sub]="-1" />
      @for (group of view.groups; track group.kind + ':' + (group.who ?? '')) {
        <div class="gd-author-skirmish__group" [attr.data-kind]="group.kind" [attr.data-who]="group.who">
          <div class="gd-author-step__heading" [title]="group.hint">{{ group.title }}</div>
          @if (group.note) {
            <div class="gd-author-note" [title]="group.hint">{{ group.note }}</div>
          }
          @for (sub of group.subs; track sub; let at = $index) {
            <gd-author-block-panel [sub]="sub" [order]="orderOf(group, at)" />
          }
          @if (group.add; as add) {
            <div class="gd-author-bar">
              <button gdAuthorButton data-add="step" [title]="add.title" (click)="addStep(view, add.at, add.step)">
                + Step
              </button>
            </div>
          }
        </div>
      }
      <div class="gd-author-bar">
        @if (view.offers.length > 1) {
          <span
            title="Steps for that character's skirmish, or for the skirmish that minion is in, whenever it is fought"
          >
            <gd-author-select
              data-add="fight"
              label="A skirmish for"
              [options]="view.offers"
              value=""
              (changed)="addFight($event)"
            />
          </span>
        }
        <button
          gdAuthorButton
          data-add="pause"
          title="A step of this block that is only read: once, over the first of the player's decisions that fits it"
          (click)="addPause(view)"
        >
          + Pause
        </button>
      </div>
    }
  `,
})
export class SkirmishPageComponent {
  protected readonly editor = inject(StepEditor);

  protected readonly view = computed(() => {
    const draft = this.editor.authoring.draft();
    const place = this.editor.place();
    const top = this.editor.top();
    if (!draft || !place || !top) return null;
    const { groups, offers } = skirmishGroups(draft, top, this.editor.authoring.ui().newFights);
    const options: AuthorOption[] = [
      ['', '+ a skirmish for…'],
      ...offers.map((offer) => [offer.alias, offer.label] as const),
    ];
    return { draft, index: place.index, count: top.anyOrder?.length ?? 0, groups, offers: options };
  });

  protected orderOf(group: SkirmishGroup, at: number): PanelOrder {
    return { prev: group.subs[at - 1] ?? null, next: group.subs[at + 1] ?? null };
  }

  /** A step for this group: the one for the decision waiting, where it is this group's; else one with no answer yet. */
  protected addStep(view: BlockAt, at: number, step: DraftStep): void {
    if (uncoveredIn(view.draft, view.index) && fitsHere(view.draft, step)) void this.editor.authoring.stepHere();
    else void this.editor.insertInBlock(view.index, at, step, true);
  }

  protected async addFight(who: string): Promise<void> {
    if (who === '' || !(await this.editor.send())) return;
    this.editor.authoring.remember({ newFights: [...this.editor.authoring.ui().newFights, who] });
  }

  protected addPause(view: BlockAt): void {
    void this.editor.insertInBlock(view.index, view.count, childPause(view.draft));
  }
}
