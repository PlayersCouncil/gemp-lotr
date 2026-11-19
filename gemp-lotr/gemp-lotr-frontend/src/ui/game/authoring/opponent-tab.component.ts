import { Component, inject } from '@angular/core';
import { Authoring } from '../../../core/game/authoring/authoring.service';

/**
 * The authoring panel's Opponent tab (inventory §3 of the port's notes; the legacy scenarioAuthor.js renderOpponent).
 * What the opponent is asked, and the answers for it where the script does not say (written into the script or not); what the player is asked; and what the opponent does where the script does not answer for it.
 *
 * Not ported yet: a placeholder until it is.
 *
 * DOM contract: `gd-game-author-opponent` holding `.gd-author-note[data-placeholder]`.
 */
@Component({
  selector: 'gd-game-author-opponent',
  template: `<p class="gd-author-note" data-placeholder>The Opponent tab is not here yet.</p>`,
})
export class OpponentTabComponent {
  protected readonly authoring = inject(Authoring);
}
