import { Component, inject } from '@angular/core';
import { GameUndo } from '../../../core/game/undo/game-undo.service';
import { UndoMarkComponent } from './undo-mark.component';

/**
 * The banner at the top of the window that says what happened: the game taken back (with the tier's mark: "✓ Ketura
 * undid: Played Gimli"), or a request declined or refused. It goes after ten seconds, fading, or when pressed.
 *
 * DOM contract: `button[data-undo=banner]` (`data-fading` for its last second), `gd-undo-mark` first when it has a tier.
 */
@Component({
  selector: 'gd-undo-banner',
  styleUrl: './undo-banner.component.css',
  imports: [UndoMarkComponent],
  host: { style: 'display: contents' },
  template: `
    @if (undo.banner(); as banner) {
      <button
        type="button"
        class="gd-undo-banner"
        data-undo="banner"
        role="status"
        [attr.data-fading]="banner.fading ? '' : null"
        (click)="undo.dismissBanner()"
      >
        @if (banner.tier) {
          <gd-undo-mark [tier]="banner.tier" />
        }
        <span>{{ ' ' + banner.message }}</span>
      </button>
    }
  `,
})
export class UndoBannerComponent {
  protected readonly undo = inject(GameUndo);
}
