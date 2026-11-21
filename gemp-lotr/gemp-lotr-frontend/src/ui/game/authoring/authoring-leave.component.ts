import { Component, computed, inject } from '@angular/core';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { DialogComponent } from '../../shared/dialog/dialog.component';

/**
 * Asked on the way back to the Game Hall while the draft has changes in no file: save them and go, discard them and
 * go (the draft is closed either way), or stay. A step the Step tab still has, which the server would not take, keeps
 * the author here after the save (the rest is saved); anything that fails is said in the dialog.
 *
 * DOM contract: `dialog.gd-dialog[data-dialog=author-leave]` with `p.gd-author-leave__text`, `p.gd-author-leave__said`
 * (what went wrong, when anything did) and `.gd-dialog__actions` with `button.gd-button[data-action]`: save, discard,
 * stay (all disabled while an answer is on its way).
 */
@Component({
  selector: 'gd-game-author-leave',
  styleUrl: './authoring-leave.component.css',
  imports: [DialogComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog
      name="author-leave"
      heading="You have unsaved changes."
      [open]="question() !== null"
      (closed)="authoring.stay()"
    >
      <p class="gd-author-leave__text">{{ text() }}</p>
      @if (question()?.said; as said) {
        <p class="gd-author-leave__said">{{ said }}</p>
      }
      <div class="gd-dialog__actions" gdDialogActions>
        <button
          type="button"
          class="gd-button"
          data-action="save"
          title="Write the draft to its file, and go back to the Game Hall"
          autofocus
          [disabled]="busy()"
          (click)="authoring.saveAndExit()"
        >
          Save and exit
        </button>
        <button
          type="button"
          class="gd-button"
          data-action="discard"
          title="Go back to the Game Hall. The file stays as it was last saved; what has been changed since is lost."
          [disabled]="busy()"
          (click)="authoring.discardAndExit()"
        >
          Discard changes
        </button>
        <button
          type="button"
          class="gd-button"
          data-action="stay"
          title="Carry on writing"
          [disabled]="busy()"
          (click)="authoring.stay()"
        >
          Stay here
        </button>
      </div>
    </gd-dialog>
  `,
})
export class AuthoringLeaveComponent {
  protected readonly authoring = inject(Authoring);
  protected readonly question = this.authoring.leaveQuestion;
  protected readonly busy = computed(() => this.question()?.busy === true);
  protected readonly text = computed(() => {
    const draft = this.authoring.draft();
    if (!draft) return '';
    const path = draft.path ? ` (${draft.path}).` : '.';
    return `${draft.draft.title ?? draft.draft.id} has changes that are not in its file${path}`;
  });
}
