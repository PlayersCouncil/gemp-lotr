import { Component, inject } from '@angular/core';
import { ScenarioGuide } from '../../../core/game/scenario/scenario-guide.service';
import { GameLog } from '../../../core/game/session/game-log';
import { DialogComponent } from '../../shared/dialog/dialog.component';

/**
 * "Give us feedback!": at the end of a game the server thanks the players and links a feedback form filled in with
 * the game's replays; the page shows that message in a dialog too (as well as in the chat box).
 *
 * The dialog itself has the focus when it opens, not its link (the legacy page opened it before it had the text).
 *
 * A scripted game (a scenario) has no such dialog: the panel it ends with has the form's link.
 *
 * DOM contract: a modal `gd-dialog` with `data-dialog=feedback`, holding the server's message.
 */
@Component({
  selector: 'gd-game-feedback',
  imports: [DialogComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog
      name="feedback"
      heading="Give us feedback!"
      [focusSelf]="true"
      [open]="log.feedback() !== null && guide.info() === null"
      (closed)="log.closeFeedback()"
    >
      @if (log.feedback(); as message) {
        <div [innerHTML]="message"></div>
      }
    </gd-dialog>
  `,
})
export class FeedbackDialogComponent {
  protected readonly log = inject(GameLog);
  /** A scripted game ends with a panel of its own, which has the form's link. */
  protected readonly guide = inject(ScenarioGuide);
}
