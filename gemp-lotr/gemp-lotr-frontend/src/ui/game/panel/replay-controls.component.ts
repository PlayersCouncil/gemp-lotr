import { Component, computed, inject } from '@angular/core';
import { GameSession } from '../../../core/game/session/game-session.service';
import { siteImage } from '../../../core/platform/app-paths';
import { CompactLayout } from '../../../core/platform/compact-layout.service';
import { ThemeService } from '../../../core/platform/theme.service';

/** The slowest and the fastest a replay plays: its pauses at most 16 times, at least a sixteenth of normal. */
const SLOWEST = 16;
const FASTEST = 0.0625;

/** How fast a replay plays, as the modern controls say it: "×2" for twice as fast, "×1/4" for a quarter. */
export function speedText(factor: number): string {
  return factor <= 1 ? `×${Math.round(1 / factor)}` : `×1/${Math.round(factor)}`;
}

/**
 * A replay's controls: Slower and Faster, and the play/pause picture under them. Classic has game.html's icon
 * buttons (triangles left and right). The modern theme, on a phone and a computer alike, lays them in one row: Slower,
 * the play button in the middle in the main button's colours with how fast it plays above it, and Faster, those two
 * written out in the secondary buttons' colours, each turned off at its limit (the owner, 2026-11-20: the triangles
 * read as previous and next). In the phone layout they fold away while the replay plays: only the play button stays,
 * small, at the row's right; pressing it (pause) brings the row back, both ways animated (the owner, 2026-11-20).
 *
 * DOM contract: `gd-game-replay` (`data-replay-done` once it has played to its end, `data-words` in the modern theme)
 * holding `button[data-replay]` slower, faster and play (`aria-pressed` while it plays), and in the modern theme
 * `output[data-replay=speed]`; `data-compact` in the phone layout, `data-playing` while it plays there (folded).
 */
@Component({
  selector: 'gd-game-replay',
  styleUrl: './replay-controls.component.css',
  host: {
    '[attr.data-replay-done]': "session.replayDone() ? '' : null",
    '[attr.data-words]': "modern() ? '' : null",
    '[attr.data-compact]': "compact() ? '' : null",
    '[attr.data-playing]': "folded() ? '' : null",
  },
  template: `
    @if (modern()) {
      <button
        type="button"
        class="gd-game-replay__side"
        data-replay="slower"
        [disabled]="session.speed() >= slowest"
        (click)="session.slower()"
      >
        Slower
      </button>
      <span class="gd-game-replay__middle">
        <output class="gd-game-replay__value" data-replay="speed" title="How fast the replay plays">{{
          speed()
        }}</output>
        <button
          type="button"
          class="gd-game-replay__main"
          data-replay="play"
          [attr.aria-label]="session.replayPlaying() ? 'Pause' : 'Play'"
          [attr.aria-pressed]="session.replayPlaying()"
          (click)="session.togglePlay()"
        >
          <svg width="22" height="22" viewBox="0 0 22 22" aria-hidden="true">
            @if (session.replayPlaying()) {
              <path d="M5 3h4v16H5zM13 3h4v16h-4z" />
            } @else {
              <path d="M6 3l13 8-13 8z" />
            }
          </svg>
        </button>
      </span>
      <button
        type="button"
        class="gd-game-replay__side"
        data-replay="faster"
        [disabled]="session.speed() <= fastest"
        (click)="session.faster()"
      >
        Faster
      </button>
    } @else {
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
    }
    @if (!modern()) {
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
    }
  `,
})
export class ReplayControlsComponent {
  protected readonly session = inject(GameSession);
  private readonly theme = inject(ThemeService).theme;
  protected readonly modern = computed(() => this.theme() === 'modern');
  /** The phone layout (its divider holds the controls). */
  protected readonly compact = inject(CompactLayout).gameActive;
  /** Folded to the small play button: in the phone layout, while the replay plays (not once it has played out). */
  protected readonly folded = computed(
    () => this.compact() && this.session.replayPlaying() && !this.session.replayDone(),
  );
  protected readonly speed = computed(() => speedText(this.session.speed()));
  protected readonly slowest = SLOWEST;
  protected readonly fastest = FASTEST;
  protected readonly picture = computed(() => siteImage(this.session.replayPlaying() ? 'pause.png' : 'play.png'));
}
