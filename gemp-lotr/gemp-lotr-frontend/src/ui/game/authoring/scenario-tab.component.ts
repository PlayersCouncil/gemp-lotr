import { Component, inject, ViewEncapsulation } from '@angular/core';
import { Authoring } from '../../../core/game/authoring/authoring.service';
import { AboutSectionComponent } from './scenario/about-section.component';
import { FileSectionComponent } from './scenario/file-section.component';
import { OutcomeSectionComponent } from './scenario/outcome-section.component';
import { PartsSectionComponent } from './scenario/parts-section.component';
import { PlaysSectionComponent } from './scenario/plays-section.component';
import { RulesSectionComponent } from './scenario/rules-section.component';

/**
 * The authoring panel's Scenario tab: the scenario as a whole (the legacy scenarioAuthor.js renderScenario). Its
 * file (scenario/file-section: saving, Save as and Rename, Check, Try other answers, See the file, Submit for review,
 * Close the draft, and the report of the last of them), what it is (about-section), how it plays (plays-section), its
 * rules (rules-section), how it is lost (outcome-section), and the rest of the file as HJSON (parts-section). Every
 * field is bound to the draft as the server last said it, so a fresh state changes what has changed and leaves
 * alone a field being typed in.
 *
 * Its stylesheet is not scoped: it draws its sections too, and the submit dialog in them (every rule starts at
 * `gd-game-author-scenario`).
 *
 * DOM contract: `gd-game-author-scenario` holding, in order, `gd-game-author-scenario-file`, `-about`, `-plays`,
 * `-rules`, `-outcome`, `-parts`, each a `section.gd-author-scenario__section[data-section]` with
 * `h3.gd-author-scenario__heading`.
 */
@Component({
  selector: 'gd-game-author-scenario',
  styleUrl: './scenario-tab.component.css',
  encapsulation: ViewEncapsulation.None,
  imports: [
    FileSectionComponent,
    AboutSectionComponent,
    PlaysSectionComponent,
    RulesSectionComponent,
    OutcomeSectionComponent,
    PartsSectionComponent,
  ],
  template: `
    @if (authoring.draft(); as state) {
      <gd-game-author-scenario-file [state]="state" />
      <gd-game-author-scenario-about [state]="state" />
      <gd-game-author-scenario-plays [state]="state" />
      <gd-game-author-scenario-rules [state]="state" />
      <gd-game-author-scenario-outcome [state]="state" />
      <gd-game-author-scenario-parts [state]="state" />
    }
  `,
})
export class ScenarioTabComponent {
  protected readonly authoring = inject(Authoring);
}
