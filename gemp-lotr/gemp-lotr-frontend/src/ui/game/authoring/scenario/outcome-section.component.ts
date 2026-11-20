import { Component, computed, inject, input } from '@angular/core';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import {
  loseChoiceOf,
  loseOptions,
  outcomeWithLose,
  outcomeWithMessage,
} from '../../../../core/game/authoring/scenario-settings';
import { DraftEnd, DraftMessage } from '../../../../core/game/authoring/authoring-state';
import {
  ENDINGS,
  Ending,
  endingMessages,
  endingResult,
  outcomeWithEnding,
} from '../../../../core/game/authoring/outcome-endings';
import { ClosingMessagesComponent } from '../widgets/closing-messages.component';
import { AuthorKeepDirective } from '../widgets/author-keep.directive';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorSelectComponent } from '../widgets/author-select.component';
import { AuthorSendDirective } from '../widgets/author-send.directive';
import { sendTop } from './top';

/**
 * The Scenario tab's "How it ends" (legacy renderOutcome): whether the player loses only as the game is lost, or
 * also when any companion is killed (a file that says something else shows that, and choosing it changes nothing),
 * and what they are told when they lose; then its endings: what is said when the win or lose condition comes true,
 * and, for the game ending by itself won or lost, what that counts as (complete or fail) and what is said. Each
 * change sends the whole of `outcome` (taken out when nothing is left), every part it does not change copied from
 * the draft.
 *
 * DOM contract: `section.gd-author-scenario__section[data-section=outcome]` with `gd-author-select[data-part=lose]`,
 * `textarea[data-part=lose-message]`, then a `.gd-author-ending[data-ending]` per ending (winMessages, loseMessages,
 * gameWon, gameLost) with `.gd-author-ending__label`, for the last two `gd-author-select[data-part=result]`, and
 * `gd-author-closing-messages`.
 */
@Component({
  selector: 'gd-game-author-scenario-outcome',
  styleUrl: './outcome-section.component.css',
  imports: [
    AuthorFieldComponent,
    AuthorKeepDirective,
    AuthorSelectComponent,
    AuthorSendDirective,
    ClosingMessagesComponent,
  ],
  template: `
    <section class="gd-author-scenario__section" data-section="outcome">
      <h3 class="gd-author-scenario__heading">How it ends</h3>
      <gd-author-field
        label="The player loses"
        hint="As the game is lost: the Ring-bearer killed or corrupted. As the file says: under 'outcome' in The rest of the file."
      >
        <gd-author-select
          data-part="lose"
          label="The player loses"
          [options]="options()"
          [value]="choice()"
          (changed)="lose($event)"
        />
      </gd-author-field>
      <gd-author-field
        label="They are told"
        hint="What the player reads when they lose, with the way back to the last checkpoint"
      >
        <textarea
          class="gd-author-input"
          rows="3"
          data-part="lose-message"
          [value]="state().draft.outcome?.loseMessage ?? ''"
          gdAuthorKeep="outcome.loseMessage"
          gdAuthorSend
          (sent)="message($event)"
        ></textarea>
      </gd-author-field>
      @for (ending of endings; track ending.kind) {
        <div class="gd-author-ending" [attr.data-ending]="ending.kind">
          <div class="gd-author-ending__head">
            <span class="gd-author-ending__label" [title]="ending.hint">{{ ending.label }}</span>
            @if (ending.stock) {
              <gd-author-select
                data-part="result"
                [label]="ending.label + ': counts as'"
                hint="What it counts as: complete (the scenario is done) or fail (the player can go back to the last checkpoint)"
                [options]="results"
                [value]="resultOf(ending)"
                (changed)="result(ending, $event)"
              />
            }
          </div>
          <gd-author-closing-messages
            [name]="'outcome.' + ending.kind"
            [messages]="messagesOf(ending)"
            [conditions]="conditionsOf(ending)"
            (changed)="messages(ending, $event)"
          />
        </div>
      }
    </section>
  `,
})
export class OutcomeSectionComponent {
  protected readonly authoring = inject(Authoring);
  readonly state = input.required<DraftState>();
  protected readonly choice = computed(() => loseChoiceOf(this.state().draft));
  protected readonly options = computed(() => loseOptions(this.choice()));
  protected readonly endings = ENDINGS;
  protected readonly results: readonly (readonly [string, string])[] = [
    ['complete', 'complete'],
    ['fail', 'fail'],
  ];

  protected lose(choice: string): void {
    const draft = this.state().draft;
    // ("as the file says" is only there to show what the file has)
    const outcome = outcomeWithLose(draft, choice);
    if (outcome !== undefined) void sendTop(this.authoring, 'outcome', outcome);
  }

  protected message(typed: string): void {
    void sendTop(this.authoring, 'outcome', outcomeWithMessage(this.state().draft, typed));
  }

  protected resultOf(ending: Ending): string {
    return endingResult(this.state().draft.outcome, ending.kind) ?? '';
  }

  protected messagesOf(ending: Ending): DraftMessage[] {
    return endingMessages(this.state().draft.outcome, ending.kind);
  }

  /** The server's text of each closing message's condition (outcomeTexts), by its place. */
  protected conditionsOf(ending: Ending): (string | undefined)[] {
    const texts = this.state().outcomeTexts ?? {};
    const prefix =
      ending.kind === 'winMessages' || ending.kind === 'loseMessages' ? ending.kind : `${ending.kind}.messages`;
    return this.messagesOf(ending).map((_, at) => texts[`${prefix}.${at}.require`]);
  }

  protected result(ending: Ending, chosen: string): void {
    const result = chosen as DraftEnd['result'];
    void sendTop(this.authoring, 'outcome', outcomeWithEnding(this.state().draft, ending.kind, { result }));
  }

  protected messages(ending: Ending, messages: DraftMessage[]): void {
    void sendTop(this.authoring, 'outcome', outcomeWithEnding(this.state().draft, ending.kind, { messages }));
  }
}
