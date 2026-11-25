import { afterRenderEffect, Component, computed, ElementRef, inject, linkedSignal } from '@angular/core';
import { AuthoringApi } from '../../../core/game/authoring/authoring-api.service';
import { AuthoringDecision, DecisionOffer, OpponentAnswering } from '../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import {
  answeredBy,
  answersByButton,
  assignRestTerm,
  answersByCards,
  cardsTerm,
  chooseLine,
  decisionKey,
  fallbackOf,
  humanLine,
  numberTerm,
  offerKey,
  offerLabel,
  offersPass,
  restChoices,
  restOffered,
} from '../../../core/game/authoring/opponent-answer';
import {
  answeringLine,
  leftoversOptions,
  leftoversPatch,
  placeForAnswering,
} from '../../../core/game/authoring/opponent-orders';
import { decisionWhere } from '../../../core/game/authoring/notices';
import { FallbackControlComponent } from './opponent/fallback-control.component';
import { OrdersEditor } from './opponent/orders-editor';
import { OrdersSectionComponent } from './opponent/orders-section.component';
import { AuthorFieldComponent } from './widgets/author-field.component';
import { AuthorSelectComponent } from './widgets/author-select.component';
import { scrollerOf } from './widgets/scroller';
import { AuthorButtonDirective } from './widgets/author-button.directive';
import { AuthorCheckComponent } from './widgets/author-check.component';

/**
 * The authoring panel's Opponent tab (the legacy scenarioAuthor.js renderOpponent): what the opponent is asked, what
 * would answer it if the author did nothing (an order, its leftovers, its sandbox, or nothing) and what that comes
 * to, and, where the script holds it for the author ("Ask me for the opponent"), the answers for it: a button per
 * action or choice, cards to tick, who each minion fights (and who "the rest" fight, written as `rest`), a number,
 * Pass, an answer in the script's words, or the least it can do; written into the script or not. Then its standing
 * orders (opponent/: tried in order where no step answers it), what the player is asked and which step applies to
 * it, and what the opponent does where the script does not answer for it (its fallback, the sandbox's cards, its
 * leftovers). What has been ticked or typed for a held decision stays while the state comes and goes, and starts
 * afresh with the next decision. Every change to the opponent brings a fresh game: the tab comes back as it was
 * (OrdersEditor keeps the order open, its form, the scroll).
 *
 * DOM contract: `gd-game-author-opponent` holding four `section.gd-author-opponent__section`s (`data-part`: asked,
 * orders, yours, how), each with `.gd-author-opponent__heading`. In `asked`: `.gd-author-opponent__asked` (the
 * decision's text and where), `.gd-author-opponent__answering` (`data-rule`: order, leftovers, sandbox, nothing) with
 * `.gd-author-opponent__rule`, `code.gd-author-opponent__gives` and `button[data-author=order-for]` when no step has
 * it, `.gd-author-note[data-author=answered-by]` when it is not held, `.gd-author-note[data-author=not-asked]` when
 * there is none; held, `.gd-author-opponent__answers` (`data-kind`: the decision's type) with
 * `button.gd-author-opponent__answer` per option (`data-term`) and for Pass; for cards a
 * `label.gd-author-opponent__card` per card (`data-key`; a checkbox) and `button[data-author=choose]`; for an
 * assignment `.gd-author-opponent__fight[data-author=rest]` (`select[data-rest]`) when every card has a name, a
 * `.gd-author-opponent__fight` per minion (`.gd-author-opponent__minion`, `select[data-minion]`) and
 * `button[data-author=assign]`; for a number `input.gd-author-opponent__number` and `button[data-author=number]`. Then
 * `gd-author-check[data-option=record]`, `.gd-author-opponent__raw` (`input`, `button[data-author=send]`) and
 * `button[data-author=least]`. In `orders`: `gd-author-orders` (opponent/orders-section). In `yours`:
 * `.gd-author-opponent__asked` and a `.gd-author-note`. In `how`: two `label.gd-author-opponent__how` with
 * `input[type=radio][data-ask]` (`me`, `orders`), `gd-author-fallback` and `gd-author-select[data-part=leftovers]`.
 */
@Component({
  selector: 'gd-game-author-opponent',
  styleUrl: './opponent-tab.component.css',
  providers: [OrdersEditor],
  imports: [
    AuthorButtonDirective,
    AuthorCheckComponent,
    AuthorFieldComponent,
    AuthorSelectComponent,
    FallbackControlComponent,
    OrdersSectionComponent,
  ],
  template: `
    @if (authoring.draft(); as draft) {
      <section class="gd-author-opponent__section" data-part="asked">
        @if (asked(); as asked) {
          <h3 class="gd-author-opponent__heading">The opponent is asked</h3>
          <p class="gd-author-opponent__asked">{{ asked.text }} ({{ whereOf(draft, asked) }})</p>
          @if (answering(); as answering) {
            @let rule = answeringLine(answering);
            <div class="gd-author-opponent__answering gd-author-bar" [attr.data-rule]="ruleOf(answering)">
              <span class="gd-author-label" title="What answers it if you do nothing">Answered by</span>
              <span class="gd-author-opponent__rule" [title]="rule.title">{{ rule.text }}</span>
              @if (answering.gives; as gives) {
                <code class="gd-author-opponent__gives" title="What that comes to now">{{ gives }}</code>
              }
              <button
                gdAuthorButton="inline"
                data-author="order-for"
                title="A standing order for this decision: its When from the decision, its answer what you give here"
                (click)="orderFor(asked, answering)"
              >
                + Order
              </button>
            </div>
          }
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
                      #tickBox
                      type="checkbox"
                      [checked]="chosen().has(keyOf(option, $index))"
                      (change)="tick(keyOf(option, $index), tickBox.checked)"
                    />
                    {{ labelOf(option) }}
                  </label>
                }
                <button gdAuthorButton="primary" data-author="choose" (click)="chooseCards(asked)">Choose these</button>
              } @else if (asked.type === 'assign') {
                <p class="gd-author-note">Who each minion fights:</p>
                @if (restOffered(asked.options, asked.characters ?? [])) {
                  <div class="gd-author-opponent__fight" data-author="rest">
                    <span
                      class="gd-author-opponent__minion"
                      title="The minions left at 'the rest' go to this character, written as rest: whatever is left when the answer is given"
                      >the rest</span
                    >
                    <select
                      #restPick
                      class="gd-author-input"
                      data-rest
                      aria-label="Who the rest fight"
                      (change)="rest.set(restPick.value)"
                    >
                      @for (choice of restChoices(asked.characters ?? []); track choice[0]) {
                        <option [value]="choice[0]" [selected]="rest() === choice[0]">{{ choice[1] }}</option>
                      }
                    </select>
                  </div>
                }
                @for (minion of asked.options; track keyOf(minion, $index)) {
                  @let minionKey = keyOf(minion, $index);
                  <div class="gd-author-opponent__fight">
                    <span class="gd-author-opponent__minion">{{ minion.alias || minion.label }}</span>
                    <select
                      #fightPick
                      class="gd-author-input"
                      [attr.data-minion]="minionKey"
                      [attr.aria-label]="'Who ' + (minion.alias || minion.label) + ' fights'"
                      (change)="fight(minionKey, fightPick.value)"
                    >
                      <option value="" [selected]="!fights()[minionKey]">{{ rest() ? 'the rest' : 'nobody' }}</option>
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
                    #numberBox
                    type="number"
                    class="gd-author-input gd-author-opponent__number"
                    aria-label="The number"
                    [attr.min]="asked.min ?? null"
                    [attr.max]="asked.max ?? null"
                    [value]="number()"
                    (input)="number.set(numberBox.value)"
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
                #rawBox
                type="text"
                class="gd-author-input"
                spellcheck="false"
                aria-label="An answer in the script's words"
                placeholder="or in the script's words: play(runner1)"
                [value]="raw()"
                (input)="raw.set(rawBox.value)"
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

      @if (draft.opponent) {
        <section class="gd-author-opponent__section" data-part="orders">
          <h3
            class="gd-author-opponent__heading"
            title="Where no step answers the opponent, the first of these whose When fits answers it"
          >
            Standing orders
          </h3>
          <gd-author-orders />
        </section>
      }

      <section class="gd-author-opponent__section" data-part="yours">
        <h3 class="gd-author-opponent__heading">You are asked</h3>
        @if (draft.waiting.human; as human) {
          <p class="gd-author-opponent__asked">{{ human.text }} ({{ whereOf(draft, human) }})</p>
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
        <gd-author-field label="Fallback" hint="What it does with a decision no step and no standing order covers">
          <gd-author-fallback />
        </gd-author-field>
        @if (draft.opponent; as opponent) {
          <gd-author-field
            label="Leftovers"
            hint="Whoever takes the minions it leaves unassigned: after any assignment of its own, and when nothing answers its assignment"
          >
            <gd-author-select
              data-part="leftovers"
              label="Leftovers"
              [options]="leftoversOptions(opponent)"
              [value]="opponent.leftovers ?? ''"
              (changed)="leftovers($event)"
            />
          </gd-author-field>
        }
      </section>
    }
  `,
})
export class OpponentTabComponent {
  protected readonly authoring = inject(Authoring);
  private readonly api = inject(AuthoringApi);
  private readonly editor = inject(OrdersEditor);
  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  /** The scroll kept from the last page is put back once (when the tab first has the draft). */
  private scrolled = false;
  /** Where a decision is, and its role in a game of two sides (" · as Shadow"). */
  protected readonly whereOf = decisionWhere;

  protected readonly answering = computed(() => this.authoring.draft()?.opponent?.answering ?? null);
  protected readonly answeringLine = answeringLine;
  protected readonly leftoversOptions = leftoversOptions;
  protected readonly restOffered = restOffered;
  protected readonly restChoices = restChoices;

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
  /** Who the minions left at "the rest" go to ('' nobody: then they are left unassigned). */
  protected readonly rest = linkedSignal<string, string>({ source: this.key, computation: () => '' });

  constructor() {
    afterRenderEffect(() => {
      const memory = this.editor.memory();
      if (this.scrolled || !this.authoring.draft()?.opponent || memory.draft === '') return;
      this.scrolled = true;
      this.editor.scroller = scrollerOf(this.element);
      if (memory.scroll > 0) this.editor.scroller?.scrollTo({ top: memory.scroll });
    });
  }

  protected ruleOf(answering: OpponentAnswering): string {
    return answering.order !== null ? 'order' : (answering.rule ?? 'nothing');
  }

  /** The answer the author is giving in the tab, if any: typed, or the cards ticked, or who the minions fight. */
  private given(asked: AuthoringDecision): string | null {
    const raw = this.raw().trim();
    if (raw !== '') return raw;
    if (asked.type === 'assign' && (this.rest() !== '' || Object.values(this.fights()).some(Boolean))) {
      return assignRestTerm(asked.options, asked.characters ?? [], this.fights(), this.rest());
    }
    if (answersByCards(asked) && this.chosen().size > 0) {
      return cardsTerm(asked.options.filter((option, index) => this.chosen().has(offerKey(option, index))));
    }
    return null;
  }

  /** A new standing order for the decision waiting, before the order that answers it now (else at the end). */
  protected orderFor(asked: AuthoringDecision, answering: OpponentAnswering): void {
    this.editor.write(placeForAnswering(answering), {
      when: answering.when,
      answer: this.given(asked) ?? answering.gives ?? answering.answer ?? '',
    });
  }

  protected leftovers(chosen: string): void {
    void this.editor.patch(leftoversPatch(chosen));
  }
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
    this.answer(assignRestTerm(asked.options, asked.characters ?? [], this.fights(), this.rest()));
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
