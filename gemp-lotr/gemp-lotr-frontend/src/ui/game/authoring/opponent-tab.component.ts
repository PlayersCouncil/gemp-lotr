import { Component, computed, inject, linkedSignal } from '@angular/core';
import { AuthoringApi } from '../../../core/game/authoring/authoring-api.service';
import { AuthoringDecision, DecisionOffer } from '../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import {
  answeredBy,
  answersByButton,
  answersByCards,
  assignTerm,
  cardsTerm,
  chooseLine,
  decisionKey,
  fallbackOf,
  humanLine,
  numberTerm,
  offerKey,
  offerLabel,
  offersPass,
} from '../../../core/game/authoring/opponent-answer';
import { AuthorButtonDirective } from './widgets/author-button.directive';
import { AuthorCheckComponent } from './widgets/author-check.component';

/**
 * The authoring panel's Opponent tab (the legacy scenarioAuthor.js renderOpponent): what the opponent is asked, and,
 * where the script holds it for the author ("Ask me for the opponent"), the answers for it: a button per action or
 * choice, cards to tick, who each minion fights, a number, Pass, an answer in the script's words, or the least it can
 * do; written into the script or not. Then what the player is asked and which step applies to it, and what the
 * opponent does where the script does not answer for it. What has been ticked or typed for a held decision stays
 * while the state comes and goes, and starts afresh with the next decision.
 *
 * DOM contract: `gd-game-author-opponent` holding three `section.gd-author-opponent__section`s (`data-part`: asked,
 * yours, how), each with `.gd-author-opponent__heading`. In `asked`: `.gd-author-opponent__asked` (the decision's
 * text and where), `.gd-author-note[data-author=answered-by]` when it is not held, `.gd-author-note[data-author=not-asked]`
 * when there is none; held, `.gd-author-opponent__answers` (`data-kind`: the decision's type) with
 * `button.gd-author-opponent__answer` per option (`data-term`) and for Pass; for cards a
 * `label.gd-author-opponent__card` per card (`data-key`; a checkbox) and `button[data-author=choose]`; for an
 * assignment a `.gd-author-opponent__fight` per minion (`.gd-author-opponent__minion`, `select[data-minion]`) and
 * `button[data-author=assign]`; for a number `input.gd-author-opponent__number` and `button[data-author=number]`. Then
 * `gd-author-check[data-option=record]`, `.gd-author-opponent__raw` (`input`, `button[data-author=send]`) and
 * `button[data-author=least]`. In `yours`: `.gd-author-opponent__asked` and a `.gd-author-note`. In `how`: two
 * `label.gd-author-opponent__how` with `input[type=radio][data-ask]` (`me`, `orders`).
 */
@Component({
  selector: 'gd-game-author-opponent',
  styleUrl: './opponent-tab.component.css',
  imports: [AuthorButtonDirective, AuthorCheckComponent],
  template: `
    @if (authoring.draft(); as draft) {
      <section class="gd-author-opponent__section" data-part="asked">
        @if (asked(); as asked) {
          <h3 class="gd-author-opponent__heading">The opponent is asked</h3>
          <p class="gd-author-opponent__asked">{{ asked.text }} ({{ asked.where }})</p>
          @if (!asked.held) {
            @let line = answeredBy(asked);
            <p class="gd-author-note" data-author="answered-by" [title]="line.title">{{ line.text }}</p>
          } @else {
            <div class="gd-author-opponent__answers" [attr.data-kind]="asked.type">
              @if (answersByButton(asked)) {
                @for (option of asked.options; track $index) {
                  <button
                    gdAuthorButton
                    class="gd-author-opponent__answer"
                    [attr.data-term]="option.term"
                    [title]="option.term ?? ''"
                    (click)="answer(option.term)"
                  >
                    {{ option.label }}
                  </button>
                }
              } @else if (answersByCards(asked)) {
                <p class="gd-author-note">{{ chooseLine(asked) }}</p>
                @for (option of asked.options; track keyOf(option, $index)) {
                  <label class="gd-author-check gd-author-opponent__card" [attr.data-key]="keyOf(option, $index)">
                    <input
                      type="checkbox"
                      [checked]="chosen().has(keyOf(option, $index))"
                      (change)="tick(keyOf(option, $index), $any($event.target).checked)"
                    />
                    {{ labelOf(option) }}
                  </label>
                }
                <button gdAuthorButton="primary" data-author="choose" (click)="chooseCards(asked)">Choose these</button>
              } @else if (asked.type === 'assign') {
                <p class="gd-author-note">Who each minion fights:</p>
                @for (minion of asked.options; track keyOf(minion, $index)) {
                  @let minionKey = keyOf(minion, $index);
                  <div class="gd-author-opponent__fight">
                    <span class="gd-author-opponent__minion">{{ minion.alias || minion.label }}</span>
                    <select
                      class="gd-author-input"
                      [attr.data-minion]="minionKey"
                      [attr.aria-label]="'Who ' + (minion.alias || minion.label) + ' fights'"
                      (change)="fight(minionKey, $any($event.target).value)"
                    >
                      <option value="" [selected]="!fights()[minionKey]">nobody</option>
                      @for (character of asked.characters ?? []; track keyOf(character, $index)) {
                        <option
                          [value]="keyOf(character, $index)"
                          [selected]="fights()[minionKey] === keyOf(character, $index)"
                        >
                          {{ labelOf(character) }}
                        </option>
                      }
                    </select>
                  </div>
                }
                <button gdAuthorButton="primary" data-author="assign" (click)="assign(asked)">Assign</button>
              } @else if (asked.type === 'number') {
                <div class="gd-author-bar">
                  <input
                    type="number"
                    class="gd-author-input gd-author-opponent__number"
                    aria-label="The number"
                    [attr.min]="asked.min ?? null"
                    [attr.max]="asked.max ?? null"
                    [value]="number()"
                    (input)="number.set($any($event.target).value)"
                  />
                  <button
                    gdAuthorButton="primary"
                    data-author="number"
                    [disabled]="numberAnswer() === null"
                    (click)="answerNumber()"
                  >
                    Answer
                  </button>
                </div>
              }
              @if (offersPass(asked)) {
                <button
                  gdAuthorButton
                  class="gd-author-opponent__answer"
                  data-term="pass"
                  title="pass"
                  (click)="answer('pass')"
                >
                  Pass
                </button>
              }
            </div>
            <gd-author-check
              data-option="record"
              label="write it into the script"
              hint="Unticked: the opponent does it this once, and the script is left as it is"
              [checked]="record()"
              (changed)="record.set($event)"
            />
            <div class="gd-author-opponent__raw gd-author-bar">
              <input
                type="text"
                class="gd-author-input"
                spellcheck="false"
                aria-label="An answer in the script's words"
                placeholder="or in the script's words: play(runner1)"
                [value]="raw()"
                (input)="raw.set($any($event.target).value)"
                (keydown.enter)="sendRaw()"
              />
              <button gdAuthorButton data-author="send" (click)="sendRaw()">Send</button>
            </div>
            <button
              gdAuthorButton
              data-author="least"
              title="What its fallback would do. Nothing is written into the script."
              (click)="least()"
            >
              The least it can, unwritten
            </button>
          }
        } @else {
          <p class="gd-author-note" data-author="not-asked">The opponent is not being asked anything.</p>
        }
      </section>

      <section class="gd-author-opponent__section" data-part="yours">
        <h3 class="gd-author-opponent__heading">You are asked</h3>
        @if (draft.waiting.human; as human) {
          <p class="gd-author-opponent__asked">{{ human.text }} ({{ human.where }})</p>
          @let line = humanLine(human, draft.recording);
          <p class="gd-author-note" [title]="line.title">{{ line.text }}</p>
        } @else {
          <p class="gd-author-note">Nothing, for now.</p>
        }
      </section>

      <section class="gd-author-opponent__section" data-part="how">
        <h3 class="gd-author-opponent__heading" title="Where the script does not answer for the opponent">
          Where the script is silent
        </h3>
        <label class="gd-author-check gd-author-opponent__how">
          <input
            type="radio"
            name="gd-author-opponent-how"
            data-ask="me"
            [checked]="draft.askOpponent"
            (change)="authoring.settings({ askOpponent: true })"
          />
          it waits for me, here
        </label>
        <label class="gd-author-check gd-author-opponent__how" title="What a player's game would have it do">
          <input
            type="radio"
            name="gd-author-opponent-how"
            data-ask="orders"
            [checked]="!draft.askOpponent"
            (change)="authoring.settings({ askOpponent: false })"
          />
          it plays by its orders, then fallback: {{ fallbackOf(draft.draft) }}
        </label>
      </section>
    }
  `,
})
export class OpponentTabComponent {
  protected readonly authoring = inject(Authoring);
  private readonly api = inject(AuthoringApi);

  protected readonly asked = computed(() => this.authoring.draft()?.waiting.opponent ?? null);
  private readonly key = computed(() => decisionKey(this.asked()));
  // (what the template says of the decision)
  protected readonly answeredBy = answeredBy;
  protected readonly humanLine = humanLine;
  protected readonly fallbackOf = fallbackOf;
  protected readonly answersByButton = answersByButton;
  protected readonly answersByCards = answersByCards;
  protected readonly offersPass = offersPass;
  protected readonly chooseLine = chooseLine;

  // What the author has ticked and typed for the decision held: kept until another decision comes
  protected readonly chosen = linkedSignal<string, ReadonlySet<string>>({
    source: this.key,
    computation: () => new Set(),
  });
  protected readonly fights = linkedSignal<string, Readonly<Record<string, string>>>({
    source: this.key,
    computation: () => ({}),
  });
  protected readonly number = linkedSignal<string, string>({
    source: this.key,
    computation: () => String(this.asked()?.min ?? 0),
  });
  protected readonly record = linkedSignal<string, boolean>({ source: this.key, computation: () => true });
  protected readonly raw = linkedSignal<string, string>({ source: this.key, computation: () => '' });
  protected readonly numberAnswer = computed(() => numberTerm(this.number()));

  protected keyOf(offer: DecisionOffer, index: number): string {
    return offerKey(offer, index);
  }

  protected labelOf(offer: DecisionOffer): string {
    return offerLabel(offer);
  }

  protected answer(term: string | null): void {
    if (term === null || term.trim() === '') return;
    void this.authoring.send(this.api.answer(term.trim(), this.record()));
  }

  protected tick(key: string, on: boolean): void {
    this.chosen.update((chosen) => {
      const next = new Set(chosen);
      if (on) next.add(key);
      else next.delete(key);
      return next;
    });
  }

  protected chooseCards(asked: AuthoringDecision): void {
    const chosen = this.chosen();
    this.answer(cardsTerm(asked.options.filter((option, index) => chosen.has(offerKey(option, index)))));
  }

  protected fight(minion: string, character: string): void {
    this.fights.update((fights) => ({ ...fights, [minion]: character }));
  }

  protected assign(asked: AuthoringDecision): void {
    this.answer(assignTerm(asked.options, asked.characters ?? [], this.fights()));
  }

  protected answerNumber(): void {
    this.answer(this.numberAnswer());
  }

  protected sendRaw(): void {
    this.answer(this.raw());
  }

  protected least(): void {
    void this.authoring.send(this.api.answerLeast());
  }
}
