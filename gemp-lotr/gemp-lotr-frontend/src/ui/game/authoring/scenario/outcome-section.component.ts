import { Component, computed, inject, input } from '@angular/core';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import {
  loseChoiceOf,
  loseOptions,
  outcomeWithLose,
  outcomeWithMessage,
} from '../../../../core/game/authoring/scenario-settings';
import { AuthorFieldComponent } from '../widgets/author-field.component';
import { AuthorSelectComponent } from '../widgets/author-select.component';
import { AuthorSendDirective } from '../widgets/author-send.directive';
import { sendTop } from './top';

/**
 * The Scenario tab's "How it is lost" (legacy renderOutcome): whether the player loses only as the game is lost, or
 * also when any companion is killed (a file that says something else shows that, and choosing it changes nothing),
 * and what they are told when they lose. Each change sends the whole of `outcome` (taken out when nothing is left).
 *
 * DOM contract: `section.gd-author-scenario__section[data-section=outcome]` with `gd-author-select[data-part=lose]`
 * and `textarea[data-part=lose-message]`.
 */
@Component({
  selector: 'gd-game-author-scenario-outcome',
  imports: [AuthorFieldComponent, AuthorSelectComponent, AuthorSendDirective],
  template: `
    <section class="gd-author-scenario__section" data-section="outcome">
      <h3 class="gd-author-scenario__heading">How it is lost</h3>
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
          gdAuthorSend
          (sent)="message($event)"
        ></textarea>
      </gd-author-field>
    </section>
  `,
})
export class OutcomeSectionComponent {
  protected readonly authoring = inject(Authoring);
  readonly state = input.required<DraftState>();
  protected readonly choice = computed(() => loseChoiceOf(this.state().draft));
  protected readonly options = computed(() => loseOptions(this.choice()));

  protected lose(choice: string): void {
    const draft = this.state().draft;
    // ("as the file says" is only there to show what the file has)
    const outcome = outcomeWithLose(draft, choice);
    if (outcome !== undefined) void sendTop(this.authoring, 'outcome', outcome);
  }

  protected message(typed: string): void {
    void sendTop(this.authoring, 'outcome', outcomeWithMessage(this.state().draft, typed));
  }
}
