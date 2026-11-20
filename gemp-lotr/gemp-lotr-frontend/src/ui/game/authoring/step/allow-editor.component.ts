import { Component, computed, inject } from '@angular/core';
import { refusalBadge, refuses, refusalTerm, withRefusal } from '../../../../core/game/authoring/refusals';
import { ScenarioGuide } from '../../../../core/game/scenario/scenario-guide.service';
import { AskedPickerComponent } from './asked-picker.component';
import { tableAliases, WorkStep } from '../../../../core/game/authoring/step-model';
import { isHumanSeat } from '../../../../core/game/authoring/steps';
import { SEATS, splitTerms } from '../../../../core/game/authoring/terms';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { HjsonAreaDirective } from '../widgets/hjson-area.directive';
import { StepEditor } from './step-editor';
import { StepFieldDirective } from './step-field.directive';

const ALLOW_HINT =
  'What the player may do: play(card), use(card), choose(card, card), option(text), number(n), assign(character: minion + minion), pass. ' +
  'In assign(...), ringBearer is whoever bears the Ring, and rest (or all) every minion still to be given out: assign(aragorn: orc1, ringBearer: rest). Several, separated by commas: each is allowed when it is on offer (the other copy of a card, a way the player did not go), and something in it has to be. Empty: anything. ' +
  "choose(...) can pick a card by its skirmish and narrow it with card filters and a number: choose(skirmishing), choose(skirmishing(aragorn), title(Orthanc Warrior), 2), choose(aragorn, not(exhausted)). Sites are site1, site2... (site1opp... for the opponent's).";
const ANSWER_HINT =
  "What the script plays: the opponent's answer, or the player's when the script plays itself. Empty: the first thing Allow lets the player do that is on offer. " +
  'assign(...) can say ringBearer and rest (or all): assign(aragorn: orc1, ringBearer: rest).';
const REFUSES_HINT =
  'Answers Allow must refuse, proved by Check: a line of terms (choose(boromir), pass, assign(...)), or a line each. ' +
  "For a step that repeats, a line can start with the decision's number: 2: choose(boromir) is about its second decision only.";
const DO_HINT =
  "Changes to the board when the step's decision arrives: move(card, seat.zone), draw(n), wound(card, n), heal(card, n), twilight(n), burdens(n), attach(card, bearer), stack(card, host). " +
  'The decision itself was worked out before them: what they put in a hand can be played from the next decision on.';

/**
 * What is done at a step that is not only read (legacy buildFields' "What is done"): "the player may do anything here"
 * (the player's step: what is written is then only what the script plays), Allow, what else the decision in hand
 * offers (a click allows it too), Answer, Do, and under "Wrong clicks, giving up" what is said of a click that is not
 * allowed, Pass when, what is said of a click on one particular card, and Skip when (not for a block's step).
 *
 * DOM contract: `gd-author-allow` holding `.gd-author-step__section` with `input[data-flag=free]`, the fields
 * (`data-field` allow.answers, refuses, answer, do, allow.otherwise, allow.passWhen, allow.hints.<alias>, skipWhen),
 * for the step in hand `.gd-author-allow__refusals` with a `.gd-author-allow__refusal[data-now]` per refusal (refused,
 * allowed, notOffered, elsewhere) and, after a click on the table the step did not take, `button[data-act=add-refusal]`,
 * `gd-author-asked` for Skip when,
 * `.gd-author-allow__offered` with a `button[data-term]` per term on offer, and `details.gd-author-allow__more`
 * holding `.gd-author-allow__hints` (a `.gd-author-allow__hint[data-alias]` per hint, with its ×) and
 * `gd-author-select[data-act=add-hint]`.
 */
@Component({
  selector: 'gd-author-allow',
  styleUrl: './allow-editor.component.css',
  imports: [
    AuthorButtonDirective,
    AuthorFieldComponent,
    AuthorSelectComponent,
    HjsonAreaDirective,
    StepFieldDirective,
    AskedPickerComponent,
  ],
  template: `
    @if (view(); as view) {
      <div class="gd-author-step__section">
        <div class="gd-author-step__heading">What is done</div>
        @if (view.players) {
          <label
            class="gd-author-check gd-author-step__block"
            title="Ticked, the step allows anything, and its Answer is only what the script plays when it plays itself (as Check does)"
          >
            <input type="checkbox" data-flag="free" [checked]="view.free" (change)="free(view.work, $event)" />
            the player may do anything here
          </label>
        }
        <gd-author-field label="Allow" [hint]="allowHint">
          <input class="gd-author-input" gdStepField="allow.answers" [disabled]="view.players && view.free" />
        </gd-author-field>
        @if (view.offered.length > 0) {
          <div class="gd-author-note">Also on offer here:</div>
          <div class="gd-author-allow__offered">
            @for (term of view.offered; track term) {
              <button
                gdAuthorButton="inline"
                title="Allow this as well"
                [attr.data-term]="term"
                (click)="offer(view.work, term)"
              >
                + {{ term }}
              </button>
            }
          </div>
        }
        @if (view.players) {
          <gd-author-field label="Refuses" [hint]="refusesHint">
            <textarea
              gdHjsonArea
              gdStepField="refuses"
              rows="1"
              placeholder="2: choose(boromir)"
              [text]="view.refuses"
              [disabled]="!view.mayRefuse"
              [title]="view.mayRefuse ? '' : 'A step that allows anything refuses nothing: give it an Allow first'"
            ></textarea>
          </gd-author-field>
          @if (view.refusals.length > 0) {
            <div class="gd-author-allow__refusals">
              @for (refusal of view.refusals; track $index) {
                <span class="gd-author-allow__refusal" [attr.data-now]="refusal.now" [title]="refusal.title">
                  {{ refusal.refusal }} <b>{{ refusal.word }}</b>
                </span>
              }
            </div>
          }
          @if (view.clicked; as clicked) {
            <div class="gd-author-allow__offered">
              <button
                gdAuthorButton="inline"
                data-act="add-refusal"
                [title]="'Not taken on the table just now: add ' + clicked + ' to what the step must refuse'"
                (click)="refuse(view.work.refuses, clicked)"
              >
                + refuses {{ clicked }}
              </button>
            </div>
          }
        }
        <gd-author-field label="Answer" [hint]="answerHint">
          <input class="gd-author-input" gdStepField="answer" />
        </gd-author-field>
        <gd-author-field label="Do" [hint]="doHint">
          <input class="gd-author-input" gdStepField="do" />
        </gd-author-field>
        <details class="gd-author-allow__more" [open]="view.open">
          <summary>{{ view.child ? 'Wrong clicks' : 'Wrong clicks, giving up' }}</summary>
          <gd-author-field
            label="Otherwise"
            hint="What to say when the player clicks something legal that is not allowed. Empty: the stock line."
          >
            <textarea class="gd-author-input" gdStepField="allow.otherwise" rows="2"></textarea>
          </gd-author-field>
          <gd-author-field
            label="Pass when"
            hint="Requirements (as on a card) that have to hold before Pass is allowed, as the file has them, in HJSON: { type: canSpot, filter: alias(aragorn) }"
          >
            <textarea gdHjsonArea gdStepField="allow.passWhen" rows="1" [text]="view.passWhen"></textarea>
          </gd-author-field>
          <div class="gd-author-allow__hints">
            <div class="gd-author-label" title="What to say about a click on one particular card">
              Said of a click on:
            </div>
            @for (hint of view.hints; track hint) {
              <div class="gd-author-allow__hint" [attr.data-alias]="hint">
                <span class="gd-author-allow__alias">{{ hint }}</span>
                <input
                  class="gd-author-input"
                  [gdStepField]="'allow.hints.' + hint"
                  [attr.aria-label]="'Said of ' + hint"
                />
                <button gdAuthorButton title="Take this hint out" (click)="dropHint(hint)">×</button>
              </div>
            }
            <gd-author-select
              data-act="add-hint"
              label="A hint for a card"
              [options]="view.aliases"
              value=""
              (changed)="addHint($event)"
            />
          </div>
          @if (!view.child) {
            <gd-author-field label="Skip when" hint="Given up on if a decision that fits this arrives first">
              <textarea gdHjsonArea gdStepField="skipWhen" rows="1" [text]="view.skipWhen"></textarea>
            </gd-author-field>
            <div class="gd-author-step__block"><gd-author-asked path="skipWhen" /></div>
          }
        </details>
      </div>
    }
  `,
})
export class AllowEditorComponent {
  protected readonly editor = inject(StepEditor);
  private readonly guide = inject(ScenarioGuide);
  protected readonly allowHint = ALLOW_HINT;
  protected readonly answerHint = ANSWER_HINT;
  protected readonly refusesHint = REFUSES_HINT;
  protected readonly doHint = DO_HINT;

  protected readonly view = computed(() => {
    const work = this.editor.work();
    const place = this.editor.place();
    const draft = this.editor.authoring.draft();
    if (!work || !place || !draft) return null;
    const child = place.kind === 'child';
    const seat = (splitTerms(work.when)[0] ?? '').toLowerCase();
    // (a step that names no seat, or the player's, is the player's to answer)
    const players = !work.when || !(SEATS as readonly string[]).includes(seat) || isHumanSeat(draft, seat);
    const position = draft.position;
    const inHandNow =
      place.index + 1 === position.step && position.inHand && position.sub === (child ? place.sub + 1 : 0);
    const asked = draft.waiting.human;
    const allowed = splitTerms(work.allow.answers);
    const offered =
      players && inHandNow && asked
        ? [
            ...asked.options.map((option) => option.term).filter((term): term is string => term !== null),
            // (an assignment's own words: every minion still to be given out to whoever bears the Ring)
            ...(asked.type === 'assign' ? ['assign(ringBearer: rest)'] : []),
            ...(asked.pass ? ['pass'] : []),
          ].filter((term, at, all) => !allowed.includes(term) && all.indexOf(term) === at)
        : [];
    const mayRefuse = (work.allow.answers ?? '').trim() !== '' || (work.refuses ?? '').trim() !== '';
    // (a click on the table the step did not take, just now: it can be made one of the step's refusals)
    const hint = players && inHandNow && mayRefuse ? this.guide.hint() : null;
    const clicked = refusalTerm(asked, hint);
    const hints = Object.keys(work.allow.hints);
    return {
      work,
      child,
      players,
      free: (work.allow.answers ?? '').trim() === '' && (work.answer ?? '').trim() !== '',
      offered,
      passWhen: work.allow.passWhen ?? '',
      refuses: work.refuses ?? '',
      mayRefuse,
      refusals: inHandNow ? (asked?.refuses ?? []).map((refusal) => ({ ...refusal, ...refusalBadge(refusal) })) : [],
      clicked: clicked !== null && refuses(work.refuses, clicked) ? null : clicked,
      skipWhen: work.skipWhen ?? '',
      hints,
      aliases: [
        ['', '+ a hint for…'],
        ...tableAliases(draft).filter((alias) => !hints.includes(alias)),
      ] as AuthorOption[],
      open:
        this.editor.wrongClicksOpen() ||
        work.allow.passWhen !== undefined ||
        Boolean(work.allow.otherwise) ||
        work.skipWhen !== undefined,
    };
  });

  /** "The player may do anything here": what is allowed becomes only what the script plays, and back. */
  protected free(work: WorkStep, event: Event): void {
    if ((event.target as HTMLInputElement).checked) {
      if ((work.answer ?? '').trim() === '') this.editor.put('answer', work.allow.answers ?? '');
      this.editor.put('allow.answers', '');
    } else {
      this.editor.put('allow.answers', work.answer ?? '');
    }
    this.editor.now();
  }

  /** A click the step did not take, added to what it must refuse. */
  protected refuse(refuses: string | undefined, term: string): void {
    this.editor.put('refuses', withRefusal(refuses, term));
    this.editor.now();
  }

  protected offer(work: WorkStep, term: string): void {
    this.editor.put('allow.answers', [...splitTerms(work.allow.answers), term].join(', '));
    this.editor.now();
  }

  /** A hint for a card, to be written (it goes with its text). */
  protected addHint(alias: string): void {
    if (alias !== '') this.editor.put(`allow.hints.${alias}`, '');
  }

  protected dropHint(alias: string): void {
    this.editor.put(`allow.hints.${alias}`, undefined);
    this.editor.now();
  }
}
