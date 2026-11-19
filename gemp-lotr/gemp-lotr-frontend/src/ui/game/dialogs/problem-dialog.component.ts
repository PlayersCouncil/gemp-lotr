import { Component, computed, inject, signal } from '@angular/core';
import { GameSession, ProblemAction } from '../../../core/game/session/game-session.service';
import { LOGIN_PAGE_URL } from '../../../core/platform/app-paths';
import { PageCues } from '../../../core/platform/page-cues';
import { DialogComponent } from '../../shared/dialog/dialog.component';

/** The legacy hall, where "Go to Game Hall" leads. */
export const HALL_URL = '/gemp-lotr/hall.html';

const LABELS: Readonly<Record<ProblemAction, string>> = {
  reload: 'Refresh page',
  mainPage: 'Go to main page',
  hall: 'Go to Game Hall',
};

/**
 * What stops the game on this page, when the legacy page said it in a dialog (no connection, not logged in, a private
 * game, the game open in another window, removed for inactivity): its title and text, and the way on (reload the page,
 * the main page, the hall). Modal; closing it leaves the page as it is.
 *
 * DOM contract: a modal `gd-dialog` with `data-dialog=game-problem`, its buttons `button[data-problem-action]` (reload,
 * mainPage, hall).
 */
@Component({
  selector: 'gd-game-problem',
  imports: [DialogComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog name="game-problem" [heading]="problem()?.title ?? ''" [open]="open()" (closed)="dismiss()">
      @if (problem(); as shown) {
        {{ shown.lines.join(' ') }}
        @if (shown.actions.length > 0) {
          <div class="gd-dialog__actions" gdDialogActions>
            @for (action of shown.actions; track action) {
              <button type="button" class="gd-button" [attr.data-problem-action]="action" (click)="go(action)">
                {{ labels[action] }}
              </button>
            }
          </div>
        }
      }
    </gd-dialog>
  `,
})
export class ProblemDialogComponent {
  private readonly session = inject(GameSession);
  private readonly cues = inject(PageCues);
  protected readonly labels = LABELS;
  /** The problem the player closed: the dialog stays closed for it. */
  private readonly dismissed = signal<unknown>(null);

  protected readonly problem = this.session.problem;

  protected readonly open = computed(() => {
    const problem = this.problem();
    return problem !== null && problem.title !== null && problem !== this.dismissed();
  });

  protected dismiss(): void {
    this.dismissed.set(this.problem());
  }

  protected go(action: ProblemAction): void {
    if (action === 'reload') this.cues.reload();
    else this.cues.go(action === 'mainPage' ? LOGIN_PAGE_URL : HALL_URL);
  }
}
