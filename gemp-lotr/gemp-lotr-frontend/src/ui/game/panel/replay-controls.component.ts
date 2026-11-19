import { Component, computed, inject } from '@angular/core';
import { GameSession } from '../../../core/game/session/game-session.service';
import { siteImage } from '../../../core/platform/app-paths';

/**
 * A replay's controls: Slower and Faster (icon buttons), and the play/pause picture under them.
 *
 * DOM contract: `gd-game-replay` (`data-replay-done` once it has played to its end) holding `button[data-replay]`
 * slower, faster and play (`aria-pressed` while it plays).
 */
@Component({
  selector: 'gd-game-replay',
  styleUrl: './replay-controls.component.css',
  host: { '[attr.data-replay-done]': "session.replayDone() ? '' : null" },
  template: `
    <button
      type="button"
      class="gd-button gd-game-replay__speed"
      data-replay="slower"
      data-icon="slower"
      title="Slower"
      aria-label="Slower"
      (click)="session.slower()"
    ></button>
    <button
      type="button"
      class="gd-button gd-game-replay__speed"
      data-replay="faster"
      data-icon="faster"
      title="Faster"
      aria-label="Faster"
      (click)="session.faster()"
    ></button>
    <br />
    <button
      type="button"
      class="gd-button gd-game-replay__play"
      data-replay="play"
      [attr.aria-label]="session.replayPlaying() ? 'Pause' : 'Play'"
      [attr.aria-pressed]="session.replayPlaying()"
      (click)="session.togglePlay()"
    >
      <img [src]="picture()" width="64" height="64" alt="" />
    </button>
  `,
})
export class ReplayControlsComponent {
  protected readonly session = inject(GameSession);
  protected readonly picture = computed(() => siteImage(this.session.replayPlaying() ? 'pause.png' : 'play.png'));
}
