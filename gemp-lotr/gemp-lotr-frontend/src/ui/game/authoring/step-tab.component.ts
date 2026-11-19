import { Component, inject } from '@angular/core';
import { Authoring } from '../../../core/game/authoring/authoring.service';

/**
 * The authoring panel's Step tab (inventory §3 of the port's notes; the legacy scenarioAuthor.js renderStep).
 * The step selected: when it applies, what it allows and answers, its messages (with where each bubble sits and what it points at, picked on the table), its block's steps, and the buttons that move, join, convert or delete it.
 *
 * Not ported yet: a placeholder until it is.
 *
 * DOM contract: `gd-game-author-step` holding `.gd-author-note[data-placeholder]`.
 */
@Component({
  selector: 'gd-game-author-step',
  template: `<p class="gd-author-note" data-placeholder>The Step tab is not here yet.</p>`,
})
export class StepTabComponent {
  protected readonly authoring = inject(Authoring);
}
