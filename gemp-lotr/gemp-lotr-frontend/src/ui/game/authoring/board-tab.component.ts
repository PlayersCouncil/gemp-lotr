import { Component, inject } from '@angular/core';
import { Authoring } from '../../../core/game/authoring/authoring.service';

/**
 * The authoring panel's Board tab (inventory §3 of the port's notes; the legacy scenarioAuthor.js renderBoard).
 * The cards of the scenario and where each is, the twilight, burdens and threats, the adventure path, the Ring-bearer and the Ring: changes gathered, then made to the board as it stands or to the one the scenario starts with.
 *
 * Not ported yet: a placeholder until it is.
 *
 * DOM contract: `gd-game-author-board` holding `.gd-author-note[data-placeholder]`.
 */
@Component({
  selector: 'gd-game-author-board',
  template: `<p class="gd-author-note" data-placeholder>The Board tab is not here yet.</p>`,
})
export class BoardTabComponent {
  protected readonly authoring = inject(Authoring);
}
