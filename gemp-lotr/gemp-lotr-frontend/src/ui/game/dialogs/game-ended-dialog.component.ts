import { DOCUMENT } from '@angular/common';
import { Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { GameEnding, replayUrl } from '../../../core/game/session/game-ending';
import { GameLog } from '../../../core/game/session/game-log';
import { GameSession } from '../../../core/game/session/game-session.service';
import { PageCues } from '../../../core/platform/page-cues';
import { PageLifetime } from '../../../core/platform/page-lifetime.service';
import { DialogComponent } from '../../shared/dialog/dialog.component';
import { HALL_URL } from './problem-dialog.component';

/** How long "Link copied" shows on the copy button (ms). */
const COPIED_MS = 2500;

/**
 * The game has ended (the owner, 2026-11-20; the modern theme, classic keeps game.html's feedback dialog): who won and
 * why, and for a player their replay of the game: watch it, or copy its link to share. The server's thanks with its
 * feedback form come in it too. It opens once the game's last events have played; closing it leaves the table as it
 * was, and the phone layout's menu and divider open it again.
 *
 * DOM contract: a modal `gd-dialog` with `data-dialog=game-ended`: `[data-ended=result]` (`data-won`, `data-lost`),
 * `[data-ended=reason]`, `[data-ended=replay]` (`data-state`: looking, found, missing) with
 * `button[data-ended=watch]`, `button[data-ended=copy]` (`data-copied`) and `input[data-ended=link]`, the feedback in
 * `[data-ended=feedback]`, and `button[data-ended=hall]`, `button[data-ended=stay]`.
 */
@Component({
  selector: 'gd-game-ended',
  styleUrl: './game-ended-dialog.component.css',
  imports: [DialogComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog name="game-ended" [heading]="heading()" [focusSelf]="true" [open]="open()" (closed)="ending.close()">
      @if (ending.outcome(); as outcome) {
        <div class="gd-game-ended">
          <p
            class="gd-game-ended__result"
            data-ended="result"
            [attr.data-won]="won() ? '' : null"
            [attr.data-lost]="lost() ? '' : null"
          >
            {{ result() }}
          </p>
          @if (outcome.reason) {
            <p class="gd-game-ended__reason" data-ended="reason">{{ outcome.reason }}</p>
          }
          @if (ending.replay(); as replay) {
            @if (replay.state !== 'none') {
              <section class="gd-game-ended__replay" data-ended="replay" [attr.data-state]="replay.state">
                <h3>Your replay</h3>
                @if (replay.state === 'looking') {
                  <p class="gd-game-ended__note">Looking for it…</p>
                } @else if (replay.state === 'found') {
                  <input
                    #link
                    class="gd-game-ended__link"
                    data-ended="link"
                    readonly
                    aria-label="The replay's link"
                    [value]="url(replay.replayId)"
                    (focus)="select($event)"
                  />
                  <div class="gd-game-ended__buttons">
                    <button type="button" class="gd-button" data-ended="watch" (click)="watch(replay.replayId)">
                      Watch the replay
                    </button>
                    <button
                      type="button"
                      class="gd-button"
                      data-ended="copy"
                      [attr.data-copied]="copied() ? '' : null"
                      (click)="copy(replay.replayId)"
                    >
                      {{ copied() ? 'Link copied' : 'Copy the link' }}
                    </button>
                  </div>
                } @else {
                  <p class="gd-game-ended__note">Not found yet: it will be in your game history in the hall.</p>
                }
              </section>
            }
          }
          @if (log.feedback(); as feedback) {
            <div class="gd-game-ended__feedback" data-ended="feedback" [innerHTML]="feedback"></div>
          }
        </div>
      }
      <div class="gd-dialog__actions" gdDialogActions>
        <button type="button" class="gd-button" data-ended="hall" (click)="hall()">Back to the game hall</button>
        <button type="button" class="gd-button" data-ended="stay" (click)="ending.close()">Stay at the table</button>
      </div>
    </gd-dialog>
  `,
})
export class GameEndedDialogComponent {
  protected readonly ending = inject(GameEnding);
  protected readonly log = inject(GameLog);
  private readonly session = inject(GameSession);
  private readonly cues = inject(PageCues);
  private readonly lifetime = inject(PageLifetime);
  private readonly window = inject(DOCUMENT).defaultView;

  constructor() {
    this.ending.showReplay();
  }

  protected readonly copied = signal(false);
  private readonly link = viewChild<ElementRef<HTMLInputElement>>('link');

  /** Once what happened last has played: the end comes after the last card moved. */
  protected readonly open = computed(() => this.ending.dialogOpen() && !this.session.busy());

  private readonly me = computed(() => {
    const model = this.session.model();
    return model.spectator ? null : model.participantId;
  });
  protected readonly won = computed(() => {
    const winner = this.ending.outcome()?.winner;
    return !!winner && winner === this.me();
  });
  protected readonly lost = computed(() => {
    const winner = this.ending.outcome()?.winner;
    return !!winner && this.me() !== null && winner !== this.me();
  });

  protected readonly heading = computed(() =>
    this.won() ? 'You won!' : this.lost() ? 'You lost' : 'The game has ended',
  );

  protected readonly result = computed(() => {
    const winner = this.ending.outcome()?.winner;
    if (!winner) return 'The game is over, with no winner.';
    return this.won() ? 'You won the game.' : `${winner} won the game.`;
  });

  protected url(replayId: string): string {
    return replayUrl(this.window?.location.origin ?? '', replayId);
  }

  protected watch(replayId: string): void {
    this.cues.go(this.url(replayId));
  }

  /** Copies the replay's link; where the browser won't, the link's field is selected for the player to copy. */
  protected copy(replayId: string): void {
    const clipboard = this.window?.navigator.clipboard;
    const done = () => {
      this.copied.set(true);
      this.lifetime.later(() => this.copied.set(false), COPIED_MS);
    };
    const field = () => this.link()?.nativeElement.select();
    if (!clipboard) {
      field();
      return;
    }
    clipboard.writeText(this.url(replayId)).then(done, field);
  }

  protected select(event: Event): void {
    (event.target as HTMLInputElement).select();
  }

  protected hall(): void {
    this.cues.go(HALL_URL);
  }
}
