import { afterRenderEffect, Component, computed, ElementRef, inject, input } from '@angular/core';
import { childMoveOp, childStep, outOfBlockOp } from '../../../../core/game/authoring/step-ops';
import { whereOf } from '../../../../core/game/authoring/step-model';
import { isHumanSeat, kidsOf, messagesOf, numberOf, stepsOf } from '../../../../core/game/authoring/steps';
import { summary } from '../../../../core/game/authoring/timeline';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { StepEditor } from './step-editor';
import { StepFieldsComponent } from './step-fields.component';

/** On a skirmishes page, the steps either side of one among the steps of the same part (else its neighbours). */
export interface PanelOrder {
  prev: number | null;
  next: number | null;
}

/**
 * One section of a block's page (legacy blockPanel): the block itself (`sub` -1), or one of its steps. Its head says
 * what it is (number, who, what it waits for and does, its marks) and opens it; only one section is open, and it holds
 * the form. A block's step also says what has become of it, and has ▶ (a fresh game up to its first decision), Insert
 * (a step before it: for the decision waiting, where this step holds it), ↑ ↓ within the block (on a skirmishes page,
 * among the steps of the same part), Out (after the block) and ×. A closed section shows what its messages say.
 *
 * DOM contract: `gd-author-block-panel` holding `.gd-author-panel` (`data-sub`, `data-open`, `data-block` for the
 * block's own) with `.gd-author-panel__head` (`button.gd-author-panel__title`, `aria-expanded`, holding
 * `.gd-author-panel__num`, `.gd-author-panel__who[data-who]`, `.gd-author-panel__when`, `.gd-author-panel__act`,
 * `.gd-author-panel__id`, `.gd-author-panel__tag[data-tag]`s; `.gd-author-panel__acts` with
 * `.gd-author-panel__where[data-status]` and `button[data-act]` play, insert, up, down, out, delete), then either
 * `.gd-author-panel__body` with `gd-author-step-fields`, or a `button.gd-author-panel__says` per message.
 */
@Component({
  selector: 'gd-author-block-panel',
  styleUrl: './block-panel.component.css',
  imports: [AuthorButtonDirective, StepFieldsComponent],
  template: `
    @if (view(); as view) {
      <div
        class="gd-author-panel"
        [attr.data-sub]="sub()"
        [attr.data-open]="view.open ? '' : null"
        [attr.data-block]="view.child ? null : ''"
      >
        <div class="gd-author-panel__head">
          <button
            type="button"
            class="gd-author-panel__title"
            [attr.aria-expanded]="view.open"
            [title]="view.open ? '' : 'Open this to change it'"
            (click)="open()"
          >
            <span class="gd-author-panel__twist">{{ view.open ? '▾' : '▸' }}</span>
            @if (view.child) {
              <span class="gd-author-panel__num">{{ view.number }}</span>
            }
            <span class="gd-author-panel__who" [attr.data-who]="view.who">{{ view.who }}</span>
            <span class="gd-author-panel__what">
              <span class="gd-author-panel__when">{{ view.when }}</span>
              @if (view.act !== '') {
                <span class="gd-author-panel__act">{{ view.act }}</span>
              }
              @if (view.id) {
                <span class="gd-author-panel__id">{{ view.id }}</span>
              }
            </span>
            <span class="gd-author-panel__tags">
              @for (tag of view.tags; track tag.kind) {
                <span class="gd-author-panel__tag" [attr.data-tag]="tag.kind" [title]="tag.title">{{ tag.text }}</span>
              }
            </span>
          </button>
          @if (view.child) {
            <div class="gd-author-panel__acts">
              <span class="gd-author-panel__where" [attr.data-status]="view.where.status">{{ view.where.words }}</span>
              <button
                gdAuthorButton
                data-act="play"
                title="A fresh game, played by the script up to the first decision this step takes"
                (click)="editor.authoring.goTo(view.index + 1, sub() + 1)"
              >
                ▶
              </button>
              <button
                gdAuthorButton
                data-act="insert"
                [title]="
                  view.holding
                    ? 'A new step in the block, before this one, for the decision waiting now and no other (in a skirmish, that skirmish’s): it takes the decision from this step. Give it a message, then do what the player should, and that is written in as what it allows.'
                    : 'A new step in the block, before this one. It says nothing yet: what you do at its decision is written in.'
                "
                (click)="insert(view.holding)"
              >
                Insert
              </button>
              <button
                gdAuthorButton
                data-act="up"
                title="Earlier in the block. (Where a step comes matters only when a decision fits more than one of them: it goes to the first.)"
                [disabled]="view.prev === null || view.prev < 0"
                (click)="up(view.prev)"
              >
                ↑
              </button>
              <button
                gdAuthorButton
                data-act="down"
                title="Later in the block"
                [disabled]="view.next === null || view.next > view.kids - 1"
                (click)="down(view.next)"
              >
                ↓
              </button>
              <button
                gdAuthorButton
                data-act="out"
                title="Take this step out of the block, and make it the step of the script after the block"
                (click)="out()"
              >
                Out
              </button>
              <button
                gdAuthorButton="danger"
                data-act="delete"
                title="Take this step out of the script"
                (click)="remove()"
              >
                ×
              </button>
            </div>
          }
        </div>
        @if (view.open) {
          <div class="gd-author-panel__body"><gd-author-step-fields /></div>
        } @else {
          @for (said of view.says; track $index) {
            <button type="button" class="gd-author-panel__says" (click)="open()">{{ said }}</button>
          }
        }
      </div>
    }
  `,
})
export class BlockPanelComponent {
  /** The block's own section (-1), or one of its steps (from 0). */
  readonly sub = input.required<number>();
  readonly order = input<PanelOrder | null>(null);

  protected readonly editor = inject(StepEditor);
  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;

  protected readonly view = computed(() => {
    const draft = this.editor.authoring.draft();
    const place = this.editor.place();
    if (!draft || !place) return null;
    const index = place.index;
    const top = stepsOf(draft)[index];
    const sub = this.sub();
    const child = sub > -1;
    const step = child ? top?.anyOrder?.[sub] : top;
    if (!top || !step) return null;
    const kids = kidsOf(draft, index);
    const said = summary(step, (seat) => isHumanSeat(draft, seat));
    const count = messagesOf(step).length;
    const position = draft.position;
    const order = this.order();
    const tags = [
      ...(step.once ? [{ kind: 'once', text: 'once', title: '' }] : []),
      ...(step.do ? [{ kind: 'do', text: 'do', title: step.do }] : []),
      ...(count > 0
        ? [{ kind: 'messages', text: `▬${count}`, title: `${count} message${count === 1 ? '' : 's'}` }]
        : []),
      ...(step.checkpoint ? [{ kind: 'checkpoint', text: '⚑', title: 'A checkpoint' }] : []),
      ...(step.end ? [{ kind: 'end', text: 'end', title: '' }] : []),
    ];
    return {
      index,
      child,
      open: place.sub === sub,
      number: numberOf(index, sub),
      who: child ? said.who : 'Block',
      when: said.when !== '' ? said.when : child ? 'any decision' : 'every decision',
      act: said.act,
      id: step.id ?? null,
      tags,
      where: whereOf(draft, index, sub),
      holding: position.step === index + 1 && position.inHand && !position.over && position.sub === sub + 1,
      prev: order ? order.prev : sub - 1,
      next: order ? order.next : sub + 1,
      kids: kids.length,
      uid: child ? (kids[sub] ?? '') : (draft.uids[index] ?? ''),
      says: messagesOf(step).map((message) => (typeof message === 'string' ? message : message.text)),
    };
  });

  constructor() {
    // (the section opened is what was asked for: it is brought into sight, once)
    afterRenderEffect(() => {
      const view = this.view();
      if (!view?.open || !view.child || this.editor.panelShown === view.uid) return;
      this.editor.panelShown = view.uid;
      // (not every browser, nor the tests' DOM, has it)
      (this.element as Partial<HTMLElement>).scrollIntoView?.({ block: 'start' });
    });
  }

  protected open(): void {
    if (!this.view()?.open) this.editor.openPanel(this.sub());
  }

  /** The block's index, and this step's name, for the ops. */
  private who(): { index: number; uid: string } | null {
    const view = this.view();
    return view ? { index: view.index, uid: view.uid } : null;
  }

  protected async insert(holding: boolean): Promise<void> {
    const at = this.who();
    const draft = this.editor.authoring.draft();
    if (!at || !draft) return;
    if (holding) {
      this.editor.authoring.remember({ jump: true, tab: 'step' });
      await this.editor.authoring.stepBefore();
      return;
    }
    await this.editor.insertInBlock(at.index, this.sub(), childStep(draft), true);
  }

  protected async up(prev: number | null): Promise<void> {
    const at = this.who();
    const draft = this.editor.authoring.draft();
    if (!at || !draft || prev === null || !(await this.editor.send())) return;
    await this.editor.authoring.changeBlock([childMoveOp(draft, at.index, at.uid, prev)], at.index, prev);
  }

  protected async down(next: number | null): Promise<void> {
    const at = this.who();
    const draft = this.editor.authoring.draft();
    if (!at || !draft || next === null || !(await this.editor.send())) return;
    await this.editor.authoring.changeBlock([childMoveOp(draft, at.index, at.uid, next + 1)], at.index, next);
  }

  protected async out(): Promise<void> {
    const at = this.who();
    const draft = this.editor.authoring.draft();
    if (!at || !draft || !(await this.editor.send())) return;
    await this.editor.authoring.changeSteps([outOfBlockOp(draft, at.index, at.uid)], at.index + 1, at.index + 2);
  }

  protected async remove(): Promise<void> {
    const at = this.who();
    if (!at) return;
    if (!(await this.editor.ask(`Take step ${numberOf(at.index, this.sub())} out of the script?`, 'Delete'))) return;
    if (!(await this.editor.send())) return;
    await this.editor.authoring.changeBlock([{ op: 'delete', uid: at.uid }], at.index, null);
  }
}
