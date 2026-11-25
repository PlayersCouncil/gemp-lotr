import { Component, computed, inject, linkedSignal } from '@angular/core';
import { GameUndo } from '../../../core/game/undo/game-undo.service';
import { UndoAnswer } from '../../../core/game/undo/undo-protocol';
import { clockLine, requestHead, when } from '../../../core/game/undo/undo-view';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { UndoHelpComponent } from './undo-help.component';
import { UndoInfoComponent } from './undo-info.component';
import { UndoMarkComponent } from './undo-mark.component';
import { UndoWhyComponent } from './undo-why.component';

/**
 * A revert request waiting for an answer, as undo.js shows it: the place (its mark, what was done there, its turn and
 * phase), the reasons in bold, and the countdown ("Game paused · auto-declines in 57s"). The asker may withdraw it; the
 * player asked approves, declines, or declines this and every later request; somebody watching only sees it. It has no
 * close button and nothing in it is the default: the dialog itself has the focus when it opens, so Enter does nothing
 * and the first Tab goes to the first answer (Approve); the "i" button comes after the answers. Not modal: the cards can
 * still be looked at and the chat used. It closes when the server says the request was resolved.
 *
 * DOM contract: `gd-dialog` with `data-dialog=undo-request`, holding `[data-undo=request]` (`data-tier`):
 * `[data-undo=head]`, `gd-undo-help`, `[data-undo=target]` (`gd-undo-mark`, `[data-undo=label]`, `[data-undo=when]`),
 * `gd-undo-why`, `[data-undo=clock]`; its actions a `button[data-undo-reply]` per answer (cancel: Withdraw; approve,
 * decline, declineAll: Decline all) and then `gd-undo-info`.
 */
@Component({
  selector: 'gd-undo-request',
  styleUrl: './undo-request.component.css',
  imports: [DialogComponent, UndoHelpComponent, UndoInfoComponent, UndoMarkComponent, UndoWhyComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog
      name="undo-request"
      [heading]="head()?.title ?? ''"
      [modal]="false"
      [closable]="false"
      [focusSelf]="true"
      [open]="pending() !== null"
    >
      @if (pending(); as shown) {
        <div class="gd-undo-request" data-undo="request" [attr.data-tier]="shown.tier">
          <div class="gd-undo-request__head" data-undo="head">
            <span>{{ head()?.head }}</span>
          </div>
          <gd-undo-help [policy]="undo.policy()" [hidden]="!help()" />
          <div class="gd-undo-request__target" data-undo="target">
            <gd-undo-mark [tier]="shown.tier" /><span class="gd-undo-request__label" data-undo="label">{{
              shown.label
            }}</span
            ><span class="gd-undo-request__when" data-undo="when">{{ placeWhen() }}</span>
          </div>
          @if (shown.reasons.length > 0) {
            <gd-undo-why class="gd-undo-request__why" [reasons]="shown.reasons" />
          }
          <div class="gd-undo-request__clock" data-undo="clock">{{ clock() }}</div>
        </div>
      }
      @if (pending()) {
        <div class="gd-undo-request__actions" gdDialogActions>
          @for (answer of answers(); track answer.reply) {
            <button
              type="button"
              class="gd-button gd-undo-request__button"
              [attr.data-undo-reply]="answer.reply"
              [attr.title]="answer.title"
              (click)="undo.reply(answer.reply)"
            >
              <span>{{ answer.label }}</span>
            </button>
          }
          <gd-undo-info class="gd-undo-request__info" [(open)]="help" />
        </div>
      }
    </gd-dialog>
  `,
})
export class UndoRequestComponent {
  protected readonly undo = inject(GameUndo);
  protected readonly pending = this.undo.pending;
  /** The explanation of the tiers: closed for each new request. */
  protected readonly help = linkedSignal({ source: this.pending, computation: () => false });
  protected readonly head = computed(() => {
    const pending = this.pending();
    return pending ? requestHead(pending, this.undo.mine()) : null;
  });
  protected readonly placeWhen = computed(() => {
    const pending = this.pending();
    return pending ? when(pending.turn, pending.phase) : '';
  });
  protected readonly clock = computed(() => clockLine(this.undo.secondsLeft()));
  /** The asker's Withdraw, or the asked player's three answers; nothing for somebody watching. */
  protected readonly answers = computed<{ reply: UndoAnswer; label: string; title: string | null }[]>(() => {
    if (this.undo.mine()) return [{ reply: 'cancel', label: 'Withdraw', title: null }];
    if (!this.undo.asked()) return [];
    return [
      { reply: 'approve', label: 'Approve', title: null },
      { reply: 'decline', label: 'Decline', title: null },
      {
        reply: 'declineAll',
        label: 'Decline all',
        title: 'Decline this and every later revert request in this game',
      },
    ];
  });
}
