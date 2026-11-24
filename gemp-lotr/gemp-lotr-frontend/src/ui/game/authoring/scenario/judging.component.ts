import { Component, computed, inject, input, signal } from '@angular/core';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import {
  JudgeForm,
  judgeAt,
  MOMENT_LABELS,
  momentTakesPhase,
  outcomeTextWithScore,
  outcomeWithJudge,
  outcomeWithScore,
  readsCard,
  ScoreForm,
  scoreForm,
} from '../../../../core/game/authoring/puzzle-settings';
import { JudgeMomentKind } from '../../../../core/game/authoring/puzzle-state';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorKeepDirective } from '../widgets/author-keep.directive';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { AuthorSendDirective } from '../widgets/author-send.directive';
import { HjsonAreaDirective } from '../widgets/hjson-area.directive';
import { sendTop } from './top';

/** The seats a hand can be counted for ({hand:human}). */
const SEATS = ['human', 'opponent', 'freeps', 'shadow'];

/**
 * The Scenario tab's "How it ends", its judge and its score (ep-notes.md P1, P7). Judged at: never, or a moment (the
 * start or end of a phase, the end of the turn, the first skirmish's resolution), of any turn or one; its
 * requirements as HJSON under it (won if they hold then, failed if not; none: the moment is the end). Scored by: none,
 * a number the game has (a total, or a card's: of which), or a custom value as HJSON; whether higher or lower is
 * better; what it is called. The game in hand's verdict is said once it has been judged. Each change sends the whole
 * outcome, every other part of it as the draft has it (a custom value as the outcome's text).
 *
 * A choice that is not yet a whole score (a card's value with no card, custom with no text) waits here, unsent, until
 * the rest is chosen; a fresh state of the score lets go of it.
 *
 * DOM contract: `.gd-author-judging` holding `.gd-author-judging__row[data-part=judge]` (`gd-author-select[data-part]`
 * judge-moment, judge-phase; `input[data-part=judge-turn]`), `textarea[data-part=judge-win]` with a moment,
 * `p.gd-author-note[data-part=verdict]` once judged; `.gd-author-judging__row[data-part=score]`
 * (`gd-author-select[data-part]` score-value, score-of, score-better; `input[data-part=score-label]`) and
 * `textarea[data-part=score-text]` for a custom value.
 */
@Component({
  selector: 'gd-game-author-scenario-judging',
  styleUrl: './judging.component.css',
  imports: [AuthorFieldComponent, AuthorKeepDirective, AuthorSelectComponent, AuthorSendDirective, HjsonAreaDirective],
  template: `
    @if (state().puzzle; as puzzle) {
      @let judge = judgeForm();
      <div class="gd-author-judging">
        <gd-author-field
          label="Judged at"
          hint="The scenario is over at this moment: won if the requirements below hold then (and Lose does not), failed if not. Win and lose are still looked at all the time, before it too."
        >
          <span class="gd-author-judging__row" data-part="judge">
            <gd-author-select
              data-part="judge-moment"
              label="Judged at"
              [options]="moments()"
              [value]="judge.moment"
              (changed)="moment($event)"
            />
            @if (takesPhase(judge.moment)) {
              <gd-author-select
                data-part="judge-phase"
                label="Phase"
                [options]="puzzle.moments.phases"
                [value]="judge.phase"
                (changed)="phase($event)"
              />
            }
            @if (judge.moment !== '') {
              <input
                type="number"
                min="1"
                class="gd-author-input gd-author-judging__turn"
                data-part="judge-turn"
                aria-label="Turn"
                title="Only that turn's moment. Empty: the first time it comes."
                placeholder="turn"
                [value]="judge.turn ?? ''"
                gdAuthorSend
                (sent)="turn($event)"
              />
            }
          </span>
        </gd-author-field>
        @if (judge.moment !== '') {
          <textarea
            class="gd-author-input"
            rows="2"
            data-part="judge-win"
            aria-label="Won if"
            title="Won if these requirements hold at the moment (as a card's, in HJSON: one, or a [list] of them all). Empty: the moment coming is the end, complete."
            placeholder="won if… (empty: complete)"
            gdHjsonArea
            [text]="puzzle.judge?.win ?? ''"
            [value]="puzzle.judge?.win ?? ''"
            gdAuthorKeep="puzzle.judge.win"
            gdAuthorSend
            (sent)="win($event)"
          ></textarea>
        }
        @if (verdict(); as said) {
          <p class="gd-author-note" data-part="verdict" title="How the game in hand was judged">{{ said }}</p>
        }
        @let score = scoreShown();
        <gd-author-field
          label="Scored by"
          hint="A number read from the game as the scenario ends (at its judging moment, if it has one). A scored puzzle with no win is complete once played to its end."
        >
          <span class="gd-author-judging__row" data-part="score">
            <gd-author-select
              data-part="score-value"
              label="Scored by"
              [options]="scoreValues()"
              [value]="score.what"
              (changed)="scoreWhat($event)"
            />
            @if (ofChoices(score.what); as choices) {
              <gd-author-select
                data-part="score-of"
                label="Of"
                [hint]="score.what === 'hand' ? 'Whose hand: none, the player and the opponent together' : 'Whose'"
                [options]="choices"
                [value]="score.of"
                (changed)="scoreOf($event)"
              />
            }
            @if (score.what !== '') {
              <gd-author-select
                data-part="score-better"
                label="Better"
                [options]="betters"
                [value]="score.better"
                (changed)="scoreBetter($event)"
              />
            }
          </span>
        </gd-author-field>
        @if (score.what !== '') {
          <gd-author-field label="Called" hint="What the page calls the score (Sméagol's strength)">
            <input
              type="text"
              class="gd-author-input"
              data-part="score-label"
              [value]="score.label"
              gdAuthorKeep="puzzle.score.label"
              gdAuthorSend
              (sent)="scoreLabel($event)"
            />
          </gd-author-field>
        }
        @if (score.what === 'custom') {
          <textarea
            class="gd-author-input"
            rows="2"
            data-part="score-text"
            aria-label="Value"
            title="A value as card definitions write one, in HJSON, a part to a line (or as JSON)"
            [placeholder]="customExample"
            gdHjsonArea
            [text]="score.text"
            [value]="score.text"
            gdAuthorKeep="puzzle.score.text"
            gdAuthorSend
            (sent)="scoreText($event)"
          ></textarea>
        }
      </div>
    }
  `,
})
export class JudgingComponent {
  private readonly authoring = inject(Authoring);
  readonly state = input.required<DraftState>();
  protected readonly takesPhase = momentTakesPhase;
  /** (HJSON reads a value up to the end of its line: a part to a line) */
  protected readonly customExample = '{\n\ttype: forEachWound\n\tfilter: companion\n}';
  protected readonly betters: readonly AuthorOption[] = [
    ['higher', 'higher is better'],
    ['lower', 'lower is better'],
  ];

  private readonly puzzle = computed(() => this.state().puzzle ?? null);

  protected readonly moments = computed<AuthorOption[]>(() => [
    ['', 'never', 'Not judged at a moment: it ends as a step, win, lose or the game says'],
    ...(this.puzzle()?.moments.moments ?? []).map((moment): AuthorOption => [
      moment,
      MOMENT_LABELS[moment]?.[0] ?? moment,
      MOMENT_LABELS[moment]?.[1] ?? '',
    ]),
  ]);

  protected readonly judgeForm = computed<JudgeForm>(() => {
    const judge = this.puzzle()?.judge ?? null;
    if (judge === null) return { moment: '', phase: '', turn: null };
    return { moment: judge.moment, phase: judge.phase ?? '', turn: judge.turn };
  });

  protected readonly verdict = computed(() => {
    const verdict = this.puzzle()?.verdict ?? null;
    if (verdict === null) return null;
    const score = verdict.score === null ? '' : `, scoring ${verdict.score}`;
    return `This game: ${verdict.won ? 'won' : 'failed'} at ${verdict.moment}${score}.`;
  });

  private sendJudge(form: JudgeForm, win?: string): void {
    void sendTop(this.authoring, 'outcome', outcomeWithJudge(this.state().draft, judgeAt(form), win));
  }

  protected moment(chosen: string): void {
    const now = this.judgeForm();
    const moment = chosen as JudgeMomentKind | '';
    const phase = now.phase || (this.puzzle()?.moments.phases[0] ?? 'fellowship');
    this.sendJudge({ moment, phase, turn: moment === '' ? null : now.turn });
  }

  protected phase(phase: string): void {
    this.sendJudge({ ...this.judgeForm(), phase });
  }

  protected turn(typed: string): void {
    const turn = Number.parseInt(typed, 10);
    this.sendJudge({ ...this.judgeForm(), turn: Number.isNaN(turn) || turn < 1 ? null : turn });
  }

  protected win(typed: string): void {
    this.sendJudge(this.judgeForm(), typed);
  }

  /* ---------------------------------------------------------------- the score */

  /** The score as the state has it... */
  private readonly scoreSaid = computed(() => scoreForm(this.puzzle()?.score ?? null));
  /** ...and a choice not yet whole enough to send, for the score it was made on. */
  private readonly waiting = signal<{ on: string; form: ScoreForm } | null>(null);
  protected readonly scoreShown = computed(() => {
    const waiting = this.waiting();
    const said = this.scoreSaid();
    return waiting !== null && waiting.on === JSON.stringify(said) ? waiting.form : said;
  });

  protected readonly scoreValues = computed<AuthorOption[]>(() => {
    const values = this.puzzle()?.scoreValues ?? { totals: [], perCard: [] };
    return [
      ['', 'none'],
      ...values.totals.map((what): AuthorOption => [what, what]),
      ...values.perCard.map((what): AuthorOption => [what, `${what} of…`]),
      [
        'custom',
        'custom',
        'A value as card definitions write one, in HJSON a part to a line (type: forEachWound, filter: companion)',
      ],
    ];
  });

  /** What a value is of: a card's (the scenario's cards and slots), a hand's seat (or none); null for the rest. */
  protected ofChoices(what: string): AuthorOption[] | null {
    const perCard = this.puzzle()?.scoreValues.perCard ?? [];
    const of = this.scoreShown().of;
    let choices: string[];
    if (what === 'hand') choices = ['', ...SEATS];
    else if (readsCard(what, perCard)) {
      const draft = this.state().draft;
      choices = [
        '',
        ...Object.keys(draft.cards?.freeps ?? {}),
        ...Object.keys(draft.cards?.shadow ?? {}),
        ...Object.keys(draft.slots ?? {}),
      ];
    } else return null;
    // (what the file says is shown even when it is no card of the scenario's)
    if (of !== '' && !choices.includes(of)) choices.push(of);
    return choices.map((choice): AuthorOption => [
      choice,
      choice === '' ? (what === 'hand' ? 'both' : 'which?') : choice,
    ]);
  }

  /** Sends the score, or keeps the choice here until it is whole (a card's value with its card, custom with text). */
  private sendScore(form: ScoreForm, text = false): void {
    const perCard = this.puzzle()?.scoreValues.perCard ?? [];
    const draft = this.state().draft;
    const kept = typeof draft.outcome?.score?.value === 'object';
    const whole =
      !(readsCard(form.what, perCard) && form.of === '') &&
      !(form.what === 'custom' && form.text.trim() === '' && !kept);
    if (!whole) {
      this.waiting.set({ on: JSON.stringify(this.scoreSaid()), form });
      return;
    }
    this.waiting.set(null);
    // (a custom value typed goes as the outcome's text; one the draft has, with another label say, as it is)
    if (form.what === 'custom' && (text || !kept)) {
      void this.authoring.edit({ topText: { outcome: outcomeTextWithScore(draft, form) } });
      return;
    }
    void sendTop(this.authoring, 'outcome', outcomeWithScore(draft, form));
  }

  protected scoreWhat(what: string): void {
    const now = this.scoreShown();
    // (a card's value keeps its card when another of a card's is chosen; anything else starts with none)
    const perCard = this.puzzle()?.scoreValues.perCard ?? [];
    const of = readsCard(what, perCard) && readsCard(now.what, perCard) ? now.of : '';
    this.sendScore({ ...now, what, of });
  }

  protected scoreOf(of: string): void {
    this.sendScore({ ...this.scoreShown(), of });
  }

  protected scoreBetter(better: string): void {
    this.sendScore({ ...this.scoreShown(), better: better === 'lower' ? 'lower' : 'higher' });
  }

  protected scoreLabel(label: string): void {
    this.sendScore({ ...this.scoreShown(), label });
  }

  protected scoreText(text: string): void {
    this.sendScore({ ...this.scoreShown(), text }, true);
  }
}
