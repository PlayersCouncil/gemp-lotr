import { Component, inject } from '@angular/core';
import { Authoring } from '../../../core/game/authoring/authoring.service';

/**
 * The authoring panel's Scenario tab (inventory §3 of the port's notes; the legacy scenarioAuthor.js renderScenario).
 * The file (where it is, Save as, Rename, Check, Try other answers, See the file, Submit for review, Close the draft), what the scenario is about, how it plays, its rules and how it is lost, and the rest of the file as HJSON.
 *
 * Not ported yet: a placeholder until it is.
 *
 * DOM contract: `gd-game-author-scenario` holding `.gd-author-note[data-placeholder]`.
 */
@Component({
  selector: 'gd-game-author-scenario',
  template: `<p class="gd-author-note" data-placeholder>The Scenario tab is not here yet.</p>`,
})
export class ScenarioTabComponent {
  protected readonly authoring = inject(Authoring);
}
