import { Component, computed, inject, input, signal } from '@angular/core';
import { Draft, DraftOptions, DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import {
  autoAcceptValue,
  bubbleGapValue,
  optionsWith,
  revealValue,
  speedValue,
  SPEEDS,
} from '../../../../core/game/authoring/scenario-settings';
import {
  AutoPass,
  autoPassValue,
  autoPassView,
  Role,
  ROLE_NAMES,
  withByRole,
  withPhase,
} from '../../../../core/game/authoring/auto-pass';
import { Notifier } from '../../../../core/notifier/notifier';
import { askAuthor } from '../widgets/author-confirm';
import { settingsOf } from '../../../../core/game/authoring/steps';
import { PHASES } from '../../../../core/game/authoring/terms';
import { STOCK_GAP } from '../../../../core/game/scenario/bubble-placement';
import { AuthorCheckComponent } from '../widgets/author-check.component';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorOption, AuthorSelectComponent } from '../widgets/author-select.component';
import { AuthorSendDirective } from '../widgets/author-send.directive';
import { sendTop } from './top';
import { FallbackControlComponent } from '../opponent/fallback-control.component';

/**
 * The Scenario tab's "How it plays": what the opponent is called and does with a decision nothing covers (the
 * Opponent tab's own control, which leaves its standing orders alone), whether
 * the player's every decision needs a step, the player's seat, the phases the player is passed through (in a game of
 * two sides, or a file that says so, for each role the player takes: two rows of ticks), the player's
 * own settings the scenario fixes for its game (auto-accept; auto-dismissing a deck reveal, on for a tutorial unless
 * set otherwise; the animation speed, the player's own unless one is chosen), and the bubbles' gap. The ticks show the
 * settings as the game has them (settingsOf), which is also what the table plays by while the draft is written (the
 * scenario guide fixes them from the draft as it changes).
 *
 * DOM contract: `section.gd-author-scenario__section[data-section=plays]` with `input[data-part=opponent-name]`,
 * `gd-author-fallback` (the fallback and the sandbox's cards: opponent/fallback-control),
 * `gd-author-select[data-part]` (coverage, human, animation-speed), `.gd-author-scenario__ticks[data-part=
 * auto-pass]` with a `gd-author-check[data-phase]` per phase (`data-role` freeps, shadow on each row of a list for each
 * role, then a row each) and `gd-author-check[data-part=by-role]`, `gd-author-check[data-part]` (auto-accept,
 * auto-dismiss-deck-reveal), and `input[data-part=bubble-gap]`.
 */
@Component({
  selector: 'gd-game-author-scenario-plays',
  imports: [
    AuthorCheckComponent,
    AuthorFieldComponent,
    AuthorSelectComponent,
    AuthorSendDirective,
    FallbackControlComponent,
  ],
  template: `
    <section class="gd-author-scenario__section" data-section="plays">
      <h3 class="gd-author-scenario__heading">How it plays</h3>
      <gd-author-field
        label="Opponent is called"
        hint="What the opponent goes by on screen, and in the log beside what the messages say"
      >
        <input
          type="text"
          class="gd-author-input"
          data-part="opponent-name"
          [value]="state().draft.opponent?.name ?? ''"
          gdAuthorSend
          (sent)="opponentName($event)"
        />
      </gd-author-field>
      <gd-author-field
        label="Opponent, unscripted"
        hint="What the opponent does with a decision no step and no standing order covers; with the sandbox, the cards it may play (none ticked: all)"
      >
        <gd-author-fallback />
      </gd-author-field>
      <gd-author-field
        label="Player, unscripted"
        hint="Whether the player's every decision has to be covered by a step. strict: every decision needs a step; guided: the player is free between steps."
      >
        <gd-author-select
          data-part="coverage"
          label="Player, unscripted"
          [options]="coverages"
          [value]="state().draft.coverage ?? 'strict'"
          (changed)="top('coverage', $event)"
        />
      </gd-author-field>
      <gd-author-field label="Player sits">
        <gd-author-select
          data-part="human"
          label="Player sits"
          [options]="seats"
          [value]="state().draft.human ?? 'freeps'"
          (changed)="top('human', $event)"
        />
      </gd-author-field>
      @let passing = passed();
      <div class="gd-author-scenario__ticks" data-part="auto-pass">
        <span
          class="gd-author-label"
          title="Phases in which the player is not asked to pass when they have nothing to play (unless the next step is about that very pass)"
          >Passed through</span
        >
        @if (!passing.byRole) {
          @for (phase of phases; track phase) {
            <gd-author-check
              [attr.data-phase]="phase"
              [label]="phase"
              [checked]="passing.all.includes(phase)"
              (changed)="autoPass(null, phase, $event)"
            />
          }
        }
        @if (state().twoSided || passing.byRole) {
          <!-- (made afresh when the author says no to the question: the tick goes back) -->
          @for (shown of [kept()]; track shown) {
            <gd-author-check
              data-part="by-role"
              label="for each role"
              hint="A list of phases for the player's own turns (as the Free Peoples player) and one for the opponent's (as the Shadow player). Unticked, one list for both."
              [checked]="passing.byRole"
              (changed)="byRole($event)"
            />
          }
        }
      </div>
      @if (passing.byRole) {
        @for (role of roles; track role) {
          <div class="gd-author-scenario__ticks" data-part="auto-pass" [attr.data-role]="role">
            <span
              class="gd-author-label"
              [title]="
                role === 'freeps'
                  ? 'On the player’s own turns, as the Free Peoples player'
                  : 'On the opponent’s turns, as the Shadow player'
              "
              >{{ roleNames[role] }}</span
            >
            @for (phase of phases; track phase) {
              <gd-author-check
                [attr.data-phase]="phase"
                [attr.data-role]="role"
                [label]="phase"
                [checked]="passing[role].includes(phase)"
                (changed)="autoPass(role, phase, $event)"
              />
            }
          </div>
        }
      }
      <div class="gd-author-scenario__settings">
        <gd-author-check
          data-part="auto-accept"
          label="Auto-accept after selecting a card or action"
          hint="A click that completes a choice is sent at once, with no Done to press. Whatever the player's own setting is."
          [checked]="settings().autoAccept === true"
          (changed)="accept($event)"
        />
        <gd-author-check
          data-part="auto-dismiss-deck-reveal"
          label="Auto-dismiss deck reveal when searching their own deck"
          hint="When the player searches their own draw deck, the popup showing the whole deck is skipped (it is answered at once, as if they had pressed Done: a step written for that popup goes by unread). Whatever the player's own setting is. Unless it is set otherwise, on for a tutorial and off for anything else."
          [checked]="settings().autoDismissDeckReveal === true"
          (changed)="reveal($event)"
        />
      </div>
      <gd-author-field
        label="Animation speed"
        hint="The speed the game's animations play at, whatever the player's own setting is (the Settings tab's slider, in steps): +2 is 4× as fast, 0 the game's usual speed"
      >
        <gd-author-select
          data-part="animation-speed"
          label="Animation speed"
          [options]="speeds"
          [value]="speed()"
          (changed)="speedOf($event)"
        />
      </gd-author-field>
      <gd-author-field
        label="Bubble gap"
        [hint]="
          'How far from what it points at a bubble sits, for every message that does not say. A plain number is hundredths of the window’s shorter side; 20px is pixels. Empty: ' +
          stockGap +
          '.'
        "
      >
        <input
          type="text"
          class="gd-author-input gd-author-scenario__short"
          data-part="bubble-gap"
          [placeholder]="stockGap"
          [value]="state().draft.options?.bubbleGap ?? ''"
          gdAuthorSend
          (sent)="gapOf($event)"
        />
      </gd-author-field>
    </section>
  `,
})
export class PlaysSectionComponent {
  protected readonly authoring = inject(Authoring);
  protected readonly phases = PHASES;
  protected readonly speeds = SPEEDS;
  protected readonly stockGap = STOCK_GAP;
  protected readonly coverages: readonly AuthorOption[] = [
    ['strict', 'strict: a step for each'],
    ['guided', 'guided: free between steps'],
  ];
  protected readonly seats = ['freeps', 'shadow'];
  protected readonly roles: readonly Role[] = ['freeps', 'shadow'];
  protected readonly roleNames = ROLE_NAMES;
  private readonly notifier = inject(Notifier);
  /** Bumped when "for each role" is kept after all (its tick is made afresh, as the draft has it). */
  protected readonly kept = signal(0);

  readonly state = input.required<DraftState>();
  private readonly draft = computed(() => this.state().draft);
  protected readonly passed = computed(() => autoPassView(this.draft().options));
  protected readonly settings = computed(() => settingsOf(this.draft()));
  protected readonly speed = computed(() => {
    const speed = this.draft().options?.animationSpeed;
    return speed === undefined ? '' : String(speed);
  });

  protected top(name: string, value: unknown): void {
    void sendTop(this.authoring, name, value);
  }

  /** Sends a part made from the draft as it is. */
  private put(name: string, make: (draft: Draft) => unknown): void {
    this.top(name, make(this.draft()));
  }

  /** The opponent's name, by the opponent patch (its standing orders are left as they are). */
  protected opponentName(name: string): void {
    const named = name.trim();
    void this.authoring.edit({ opponent: named === '' ? { unset: ['name'] } : { set: { name: named } } });
  }

  /** A phase ticked or not, in the one list (`role` null) or in a role's: the whole of options goes. */
  protected autoPass(role: Role | null, phase: string, on: boolean): void {
    this.sendPassing(withPhase(this.passed(), role, phase, on));
  }

  /** "For each role" ticked or not; unticked where the roles' lists differ, after the page's own question. */
  protected async byRole(on: boolean): Promise<void> {
    const { view, lost } = withByRole(this.passed(), on);
    if (lost !== null && !(await askAuthor(this.notifier, lost, 'One list'))) {
      this.kept.update((count) => count + 1);
      return;
    }
    this.sendPassing(view);
  }

  private sendPassing(view: AutoPass): void {
    this.put('options', (draft) => optionsWith(draft, 'autoPass', autoPassValue(view)));
  }

  protected option<K extends keyof DraftOptions>(name: K, value: (draft: Draft) => DraftOptions[K] | null): void {
    this.put('options', (draft) => optionsWith(draft, name, value(draft)));
  }

  protected accept(on: boolean): void {
    this.option('autoAccept', () => autoAcceptValue(on));
  }

  protected reveal(on: boolean): void {
    this.option('autoDismissDeckReveal', (draft) => revealValue(draft, on));
  }

  protected speedOf(chosen: string): void {
    this.option('animationSpeed', () => speedValue(chosen));
  }

  protected gapOf(typed: string): void {
    this.option('bubbleGap', () => bubbleGapValue(typed));
  }
}
