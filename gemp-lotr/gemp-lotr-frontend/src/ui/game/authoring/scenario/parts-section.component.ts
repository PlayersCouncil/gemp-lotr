import { Component, inject, input } from '@angular/core';
import { DraftState } from '../../../../core/game/authoring/authoring-state';
import { Authoring } from '../../../../core/game/authoring/authoring.service';
import { FILE_PARTS, FilePart, hasPart } from '../../../../core/game/authoring/scenario-settings';
import { AuthorButtonDirective } from '../widgets/author-button.directive';
import { HjsonAreaDirective } from '../widgets/hjson-area.directive';

/** The most rows a part's text grows to (the legacy 22). */
const PART_ROWS = 22;

/**
 * The Scenario tab's "The rest of the file": each part of the file the tab has no controls for (and the ones it
 * has), as the file has it in HJSON, folded away until opened. Apply sends the text as it is (`topText`): the server
 * reads it as it reads the file, and what is wrong with it is said at the top of the panel, the text kept here to be
 * put right. Changing one starts a fresh game, played by the script back to here.
 *
 * DOM contract: `section.gd-author-scenario__section[data-section=parts]` with a `details.gd-author-scenario__part`
 * per part (`data-part`; `data-none` when the file has none) holding `summary`, `textarea.gd-author-code` and
 * `button[data-author=apply]`.
 */
@Component({
  selector: 'gd-game-author-scenario-parts',
  imports: [AuthorButtonDirective, HjsonAreaDirective],
  template: `
    <section class="gd-author-scenario__section" data-section="parts">
      <h3 class="gd-author-scenario__heading">The rest of the file</h3>
      <p
        class="gd-author-note"
        title="What is copied out of a scenario's file can be pasted here (with or without its name in front), and the other way about. Changing one starts a fresh game, played by the script back to here."
      >
        Each part as the file has it, in HJSON.
      </p>
      @for (name of parts; track name) {
        <details
          class="gd-author-scenario__part"
          [attr.data-part]="name"
          [attr.data-none]="has(name, state().draft) ? null : ''"
        >
          <summary>{{ name }}{{ has(name, state().draft) ? '' : ' (none)' }}</summary>
          <textarea
            #area
            rows="4"
            gdHjsonArea="always"
            [max]="rows"
            [value]="state().parts[name] ?? ''"
            [text]="state().parts[name] ?? ''"
            [attr.aria-label]="name"
          ></textarea>
          <div class="gd-author-bar">
            <button gdAuthorButton data-author="apply" (click)="apply(name, area.value)">Apply</button>
          </div>
        </details>
      }
    </section>
  `,
})
export class PartsSectionComponent {
  protected readonly authoring = inject(Authoring);
  readonly state = input.required<DraftState>();
  protected readonly parts = FILE_PARTS;
  protected readonly rows = PART_ROWS;

  protected has(name: FilePart, draft: Parameters<typeof hasPart>[0]): boolean {
    return hasPart(draft, name);
  }

  protected apply(name: FilePart, text: string): void {
    void this.authoring.edit({ topText: { [name]: text } });
  }
}
