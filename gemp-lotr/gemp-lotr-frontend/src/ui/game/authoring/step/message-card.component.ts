import { Component, computed, inject, input, output, signal } from '@angular/core';
import { STOCK_GAP } from '../../../../core/game/scenario/bubble-placement';
import { MessageSide } from '../../../../core/game/scenario/scenario-events';
import { moveMessageOps, splitToPauseOps } from '../../../../core/game/authoring/step-ops';
import { isOpeningWork, valueAt, WorkMessage, WorkStep, written } from '../../../../core/game/authoring/step-model';
import { isBlock, stepsOf } from '../../../../core/game/authoring/steps';
import { AnchorFieldComponent, AnchorPicked } from '../widgets/anchor-field.component';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { PictureChooserComponent } from '../widgets/picture-chooser.component';
import { FormPlace, StepEditor } from './step-editor';
import { StepForm } from '../../../../core/game/authoring/send-queue';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { StepFieldDirective } from './step-field.directive';

const TEXT_HINT =
  'What it says. **bold**, [[Card Name]] or [[alias]], [gondor], (2) for twilight, {strength:alias} and the like for numbers from the game.';
const NAME = /^[A-Za-z][A-Za-z0-9_-]*$/;
const PARTS =
  'strength, vitality, resistance, siteNumber, wounds, cost, title, culture, type, text, art, stats, signet, shadowNumber';

/**
 * One message of a step, as a card (legacy drawMessages): its head (its number; Insert a message before it; for a
 * step of the script ◀ Earlier / Later ▶ to the step either side; Before… to make it and the ones before it a pause
 * of their own; ↑ ↓ within the step; ×), its text, once, what it points at, where its bubble sits (tried on the
 * table at once, as the gap is typed too), what it outlines, and folded away: its heading, what it keeps clear of,
 * what takes it away, when it is shown, what it shows large, its parts, how magnified, a pause's choice, its note. Its
 * fields are sent a moment after typing pauses, so that its bubble on the table shows them; the focus in it puts it
 * on the table (when its step is up). A closing message (Ends the scenario) has only its text and heading, sent when
 * a field is left.
 *
 * DOM contract: `gd-author-message` holding `.gd-author-message` (`data-index`, `data-closing`) with
 * `.gd-author-message__head` (`.gd-author-message__num`, `button[data-act]`: insert, earlier, later, pause, up, down,
 * delete, pictures), the fields (`data-field` messages.N.text, .header, .gap, .advance, .on, .zoom, .focus, .magnify,
 * .note, .choices.<name>), a `gd-author-anchor` each for what it points at, outlines and keeps clear of (`data-field`
 * anchor, highlight, avoid: picked on the table, or typed), `input[data-flag=once]`, `gd-author-select[data-pick]`
 * side, style, `details.gd-author-message__more` (with `gd-author-pictures` while Picture… is on) and, on a pause's
 * last message, `.gd-author-message__choices`.
 */
@Component({
  selector: 'gd-author-message',
  styleUrl: './message-card.component.css',
  imports: [
    AnchorFieldComponent,
    AuthorButtonDirective,
    AuthorFieldComponent,
    AuthorSelectComponent,
    PictureChooserComponent,
    StepFieldDirective,
  ],
  template: `
    @if (view(); as view) {
      <div
        class="gd-author-message"
        [attr.data-index]="index()"
        [attr.data-closing]="closing() ? '' : null"
        (focusin)="focused()"
      >
        <div class="gd-author-message__head">
          <span class="gd-author-message__num">{{ index() + 1 }}.</span>
          <button gdAuthorButton data-act="insert" title="A new message before this one" (click)="inserted.emit()">
            Insert
          </button>
          @if (!closing()) {
            @if (view.script) {
              <button
                gdAuthorButton
                data-act="earlier"
                title="Move this message to the step before"
                [disabled]="!view.earlier"
                (click)="moveTo(view, -1)"
              >
                ◀ Earlier
              </button>
              <button
                gdAuthorButton
                data-act="later"
                title="Move this message to the step after"
                [disabled]="!view.later"
                (click)="moveTo(view, 1)"
              >
                Later ▶
              </button>
            }
            <button
              gdAuthorButton
              data-act="pause"
              title="Take this message (and the ones before it in this step) out into a pause of their own, read before this step's decision is answered"
              (click)="toPause(view)"
            >
              Before…
            </button>
          }
          <button
            gdAuthorButton
            data-act="up"
            title="Earlier in this step"
            [disabled]="index() === 0"
            (click)="move(-1)"
          >
            ↑
          </button>
          <button
            gdAuthorButton
            data-act="down"
            title="Later in this step"
            [disabled]="index() >= count() - 1"
            (click)="move(1)"
          >
            ↓
          </button>
          <button gdAuthorButton="danger" data-act="delete" title="Delete this message" (click)="remove()">×</button>
        </div>
        <textarea
          class="gd-author-input gd-author-message__text"
          rows="4"
          placeholder="What it says: **bold**, [[Card]], [gondor], (2), {strength:alias}"
          aria-label="Text"
          [title]="textHint"
          [gdStepField]="base() + '.text'"
          [sends]="sends()"
        ></textarea>
        @if (closing()) {
          <gd-author-field label="Heading">
            <input class="gd-author-input" [gdStepField]="base() + '.header'" />
          </gd-author-field>
        } @else {
          <label
            class="gd-author-check"
            title="Ticked: said the first time the step applies, and not when it applies again (a step that repeats, or a block's step that is not 'once'). Unticked: said every time."
          >
            <input type="checkbox" data-flag="once" [checked]="view.message.once !== false" (change)="once($event)" />
            once
          </label>
          <gd-author-anchor
            data-field="anchor"
            label="Points at"
            hint="What the bubble sits beside and points at. Several things, separated by commas, and it points at each of them. Empty: the middle of the screen."
            [many]="true"
            [value]="view.message.anchor ?? ''"
            (typed)="typed('anchor', $event)"
            (committed)="put('anchor', $event)"
            (picked)="anchorPicked($event)"
          />
          <div class="gd-author-message__row">
            <span class="gd-author-message__label" title="Where the bubble goes, of what it points at">Sits</span>
            <span
              class="gd-author-message__side"
              title="Which side of what it points at the bubble goes on. A side named here is the side it goes on, whatever else that covers; left to itself it goes where it covers least."
            >
              <gd-author-select
                data-pick="side"
                label="Sits"
                [options]="sides"
                [value]="view.message.side ?? ''"
                (changed)="side($event)"
              />
            </span>
            <span class="gd-author-message__label" data-inline title="How far from what it points at">gap</span>
            <input
              class="gd-author-input gd-author-step__short"
              aria-label="Gap"
              [gdStepField]="base() + '.gap'"
              sends="typing"
              [placeholder]="view.stockGap"
              [title]="
                'How far from what it points at the bubble sits. A plain number is hundredths of the window’s shorter side, so it keeps its proportion to the cards on any screen; 20px is pixels whatever the screen. Empty: the scenario’s own (' +
                view.stockGap +
                '). Up and down arrows change it a little at a time.'
              "
              (input)="gapTyped($any($event.target).value)"
              (keydown.arrowup)="nudge($event, 1)"
              (keydown.arrowdown)="nudge($event, -1)"
            />
          </div>
          <div class="gd-author-message__row">
            <gd-author-anchor
              data-field="highlight"
              label="Outlines"
              hint="Things to draw an outline around, separated by commas"
              [many]="true"
              [addOnly]="true"
              [value]="view.message.highlight ?? ''"
              (typed)="typed('highlight', $event)"
              (committed)="put('highlight', $event)"
            />
            <gd-author-select
              data-pick="style"
              label="Outline style"
              [options]="outlines"
              [value]="view.message.highlightStyle ?? ''"
              (changed)="put('highlightStyle', $event)"
            />
          </div>
          <details class="gd-author-message__more" [open]="view.open">
            <summary title="Heading, what takes it away, a card or a picture shown large">More…</summary>
            <gd-author-field label="Heading" hint="Most messages have none">
              <input class="gd-author-input" [gdStepField]="base() + '.header'" sends="typing" />
            </gd-author-field>
            <gd-author-anchor
              data-field="avoid"
              label="Keeps clear of"
              hint="Things the bubble must not cover"
              [many]="true"
              [addOnly]="true"
              [value]="view.message.avoid ?? ''"
              (typed)="typed('avoid', $event)"
              (committed)="put('avoid', $event)"
            />
            <gd-author-field
              label="Goes away on"
              hint="Empty: Next, except the step's last message, which stays up while the decision is answered. inspect: the player opens a card's information window; close: they close it."
            >
              <!-- advance field: a card in inspect(card) may become gd-author-anchor -->
              <input
                class="gd-author-input"
                placeholder="next, decision, inspect, inspect(card), close"
                [gdStepField]="base() + '.advance'"
                sends="typing"
              />
            </gd-author-field>
            @if (view.shownWhen) {
              <gd-author-field
                label="Shown when"
                hint="A moment inside the answering: the player has clicked that card; that character has all its minions; the answer is complete and only Done remains. Empty: when the decision arrives."
              >
                <input
                  class="gd-author-input"
                  placeholder="picked(card), assigned(character), ready"
                  [gdStepField]="base() + '.on'"
                  sends="typing"
                />
              </gd-author-field>
            }
            <div class="gd-author-message__row">
              <span
                class="gd-author-message__label"
                title="A card, or a picture, shown as large as the window allows while the message is up, with the bubble beside it. card:alias is one of the scenario's cards, site:3 a site on the path, blueprint:1_83 any card at all, image:file.png a picture from the server's images/scenarios folder."
                >Shows large</span
              >
              <input
                class="gd-author-input"
                aria-label="Shows large"
                placeholder="card:alias, site:3, blueprint:1_83, image:file.png"
                [gdStepField]="base() + '.zoom'"
                sends="typing"
              />
              <button
                gdAuthorButton
                data-act="pictures"
                title="Choose one of the server's pictures"
                [attr.aria-pressed]="pictures()"
                (click)="pictures.set(!pictures())"
              >
                Picture…
              </button>
            </div>
            @if (pictures()) {
              <gd-author-pictures
                [value]="view.message.zoom ?? ''"
                (chosen)="put('zoom', $event)"
                (closed)="pictures.set(false)"
              />
            }
            <gd-author-field
              label="Its parts"
              [hint]="
                'The parts of what is shown large that the message is about: each is outlined, and the rest is dimmed. On a card: ' +
                parts +
                '. On anything, a picture included: rect(left, top, width, height), in percent of it. Several, separated by commas.'
              "
            >
              <input
                class="gd-author-input"
                [placeholder]="view.large ? 'strength, text, rect(l, t, w, h)' : '(nothing is shown large)'"
                [disabled]="!view.large && !view.message.focus"
                [gdStepField]="base() + '.focus'"
                sends="typing"
              />
            </gd-author-field>
            <gd-author-field
              label="Magnified"
              hint="How many times larger than fits the window it is drawn: 2 is twice as large, up to 6; empty (or 1) is the whole of it. It is seen through a frame the size the whole would have been, with Its parts in the middle of the frame (its own middle, if there are none)."
            >
              <input
                class="gd-author-input gd-author-step__short"
                [placeholder]="view.large ? '1' : '(nothing)'"
                [disabled]="!view.large && (view.message.magnify ?? '') === ''"
                [gdStepField]="base() + '.magnify'"
                sends="typing"
              />
            </gd-author-field>
            @if (view.choices; as choices) {
              <div class="gd-author-message__choices">
                <div
                  class="gd-author-label"
                  title="Ends in a choice: buttons in place of Next; later steps can say 'if' and 'unless'"
                >
                  Ends in a choice
                </div>
                @for (choice of choices; track choice) {
                  <div class="gd-author-message__row" [attr.data-choice]="choice">
                    <span class="gd-author-message__name">{{ choice }}</span>
                    <input
                      class="gd-author-input"
                      [attr.aria-label]="'Button ' + choice"
                      [gdStepField]="base() + '.choices.' + choice"
                      sends="typing"
                    />
                    <button gdAuthorButton title="Take this button out" (click)="dropChoice(choice)">×</button>
                  </div>
                }
                <div class="gd-author-message__row">
                  <input
                    class="gd-author-input gd-author-step__short"
                    placeholder="name"
                    aria-label="New button's name"
                    data-choice-name
                    [attr.data-bad]="badName() ? '' : null"
                    [value]="newChoice()"
                    (input)="newChoice.set($any($event.target).value)"
                    (keydown.enter)="addChoice(view.message)"
                  />
                  <button
                    gdAuthorButton
                    data-act="add-choice"
                    title="A choice needs at least two"
                    (click)="addChoice(view.message)"
                  >
                    + button
                  </button>
                </div>
              </div>
            }
            <gd-author-field label="Note" hint="Never shown to a player">
              <input class="gd-author-input" [gdStepField]="base() + '.note'" sends="typing" />
            </gd-author-field>
          </details>
        }
      </div>
    }
  `,
})
export class MessageCardComponent {
  /** Which message of the list (from 0), the list's path in the working copy, how many it has. */
  readonly index = input.required<number>();
  readonly path = input.required<string>();
  readonly count = input.required<number>();
  /** One of the messages that close the scenario. */
  readonly closing = input(false);
  /** Insert: a new message before this one. */
  readonly inserted = output();

  protected readonly editor = inject(StepEditor);
  protected readonly textHint = TEXT_HINT;
  protected readonly parts = PARTS;
  protected readonly sides: readonly AuthorOption[] = [
    ['', 'wherever fits best'],
    ['right', 'to its right'],
    ['left', 'to its left'],
    ['above', 'above it'],
    ['below', 'below it'],
  ];
  protected readonly outlines: readonly AuthorOption[] = [['', 'gold'], 'red', 'green', 'blue'];
  protected readonly newChoice = signal('');
  protected readonly badName = signal(false);
  /** The picture chooser is open under Shows large. */
  protected readonly pictures = signal(false);

  protected readonly base = computed(() => `${this.path()}.${this.index()}`);
  protected readonly sends = computed(() => (this.closing() ? 'change' : 'typing'));

  protected readonly view = computed(() => {
    const form = this.editor.form();
    const work = this.editor.work();
    const place = this.editor.place();
    const draft = this.editor.authoring.draft();
    const message = valueAt(work, this.base()) as WorkMessage | undefined;
    if (!form || !work || !place || !draft || !message) return null;
    const steps = stepsOf(draft);
    const pause = work.pause === true;
    const opening = isOpeningWork(work);
    const filled = (name: keyof WorkMessage) => {
      const value = message[name];
      return value !== undefined && value !== null && value !== '';
    };
    const choices = message.choices ?? {};
    return {
      form,
      place,
      draft,
      work,
      message,
      // (from one step of the script to the next: the steps in a block come in no order to move it along)
      script: place.sub < 0,
      earlier: place.index > 0 && !isBlock(steps[place.index - 1]),
      later: place.index < steps.length - 1 && !isBlock(steps[place.index + 1]),
      stockGap: draft.draft.options?.bubbleGap ?? STOCK_GAP,
      shownWhen: !pause && !opening,
      large: (message.zoom ?? '').trim() !== '',
      open:
        ['header', 'side', 'avoid', 'advance', 'on', 'zoom', 'note'].some((name) => filled(name)) ||
        Object.keys(choices).length > 0,
      choices: pause && !this.closing() && this.index() === this.count() - 1 ? Object.keys(choices) : null,
    };
  });

  protected focused(): void {
    if (!this.closing()) this.editor.focusMessage(this.index());
  }

  private messages(): WorkMessage[] {
    return [...((valueAt(this.editor.work(), this.path()) as WorkMessage[] | undefined) ?? [])];
  }

  /** Changes a part of this message, and sends it at once. */
  protected put(name: string, value: unknown): void {
    this.editor.put(`${this.base()}.${name}`, value === '' ? undefined : value);
    this.editor.now();
  }

  /** What is typed in an anchor field: into the working copy, and sent when typing pauses. */
  protected typed(name: string, value: string): void {
    this.editor.put(`${this.base()}.${name}`, value);
    this.editor.later();
  }

  /**
   * Something picked on the table for Points at (the field has sent it): Outlines follows while it says nothing of
   * its own — empty, it takes what was picked; holding the card that "Whichever … is fighting" took the place of,
   * it takes that place too.
   */
  protected anchorPicked(picked: AnchorPicked): void {
    const outlines = (this.view()?.message.highlight ?? '').trim();
    if (picked.replacing === null ? outlines === '' : outlines === picked.replacing) {
      this.put('highlight', picked.anchor);
    }
  }

  protected once(event: Event): void {
    this.put('once', (event.target as HTMLInputElement).checked ? undefined : false);
  }

  protected side(value: string): void {
    this.put('side', value);
    this.tryPlace({ side: (value === '' ? 'auto' : value) as MessageSide });
  }

  /** A gap that is a number (or pixels), tried on the table as it is typed. */
  protected gapTyped(value: string): void {
    if (/^\s*-?\d+(\.\d+)?\s*(px)?\s*$/i.test(value) || value.trim() === '') {
      this.tryPlace({ gap: value.trim() === '' ? null : value.trim() });
    }
  }

  /** ↑ / ↓ in the gap: a little more or less (a quarter, or 2px). */
  protected nudge(event: Event, by: 1 | -1): void {
    const field = event.target as HTMLInputElement;
    const typed = field.value.trim() === '' ? (this.view()?.stockGap ?? STOCK_GAP) : field.value.trim().toLowerCase();
    const pixels = typed.includes('px');
    const amount = Number.parseFloat(typed);
    if (Number.isNaN(amount)) return;
    event.preventDefault();
    field.value = `${Math.round((amount + by * (pixels ? 2 : 0.25)) * 100) / 100}${pixels ? 'px' : ''}`;
    field.dispatchEvent(new Event('input', { bubbles: true }));
  }

  private tryPlace(place: { side?: MessageSide; gap?: string | null }): void {
    const at = this.editor.messageAt(this.index());
    if (at) this.editor.authoring.tryPlace(at, place);
  }

  protected move(by: -1 | 1): void {
    const messages = this.messages();
    messages.splice(this.index() + by, 0, ...messages.splice(this.index(), 1));
    this.editor.put(this.path(), messages);
    this.editor.now();
  }

  protected remove(): void {
    const messages = this.messages();
    messages.splice(this.index(), 1);
    this.editor.put(this.path(), messages);
    this.editor.now();
  }

  /** To the step before or after (`by`): the last thing that step says, or the first. */
  protected moveTo(view: CardAt, by: -1 | 1): void {
    const { form, place, draft } = view;
    const toIndex = place.index + by;
    const moved = moveMessageOps(draft, form.uid, written(view.work), form.dirty(), this.index(), place.index, toIndex);
    if (!moved) return;
    this.editor.authoring.focusMessage.set({ step: toIndex + 1, sub: 0, index: moved.at });
    void this.editor.authoring.changeSteps(moved.ops, Number.MAX_SAFE_INTEGER, toIndex + 1);
  }

  /** This message, and the ones before it, a pause of their own before the step (in a block, one of its steps). */
  protected toPause(view: CardAt): void {
    const { form, place } = view;
    const ops = splitToPauseOps(form.uid, written(view.work), form.dirty(), this.index());
    if (place.sub > -1) void this.editor.authoring.changeBlock(ops, place.index, place.sub);
    else void this.editor.authoring.changeSteps(ops, place.index + 1, place.index + 1);
  }

  protected addChoice(message: WorkMessage): void {
    const name = this.newChoice().trim();
    const choices = message.choices ?? {};
    if (!NAME.test(name) || name in choices) {
      this.badName.set(true);
      return;
    }
    this.badName.set(false);
    this.newChoice.set('');
    this.put(`choices.${name}`, name);
  }

  protected dropChoice(name: string): void {
    this.editor.put(`${this.base()}.choices.${name}`, undefined);
    this.editor.now();
  }
}

/** What a card's moves need: the form, where its step is, the draft, the working copy. */
interface CardAt {
  form: StepForm;
  place: FormPlace;
  draft: DraftState;
  work: WorkStep;
}
