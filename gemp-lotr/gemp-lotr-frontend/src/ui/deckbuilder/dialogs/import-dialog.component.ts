import { Component, inject } from '@angular/core';
import { DeckLibraryService } from '../../../core/deck/deck-library.service';
import { DeckOpener } from '../../../core/deck/deck-opener.service';
import { DialogComponent } from '../../shared/dialog/dialog.component';

/**
 * "Import deck": a decklist to paste and the Import button. Not modal; it starts empty each time the toolbar's import
 * button is used. As the legacy dialog did, it focuses itself, not the text field, when it opens.
 *
 * DOM contract: a non-modal `gd-dialog` with `data-dialog=import`, holding `.gd-import` with
 * `textarea.gd-import__text` and `button.gd-button[data-action=import]` ("Import").
 */
@Component({
  selector: 'gd-import-dialog',
  styleUrl: './import-dialog.component.css',
  imports: [DialogComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog
      name="import"
      heading="Import deck"
      [modal]="false"
      [focusSelf]="true"
      [open]="library.importOpen()"
      (closed)="library.importOpen.set(false)"
    >
      @for (round of library.importOpen() ? [library.importRound()] : []; track round) {
        <div class="gd-import">
          <textarea #decklist class="gd-import__text" rows="5" cols="30" aria-label="Decklist"></textarea
          ><button
            type="button"
            class="gd-button"
            data-action="import"
            title="Import"
            (click)="opener.importDecklist(decklist.value)"
          >
            Import
          </button>
        </div>
      }
    </gd-dialog>
  `,
})
export class ImportDialogComponent {
  protected readonly library = inject(DeckLibraryService);
  protected readonly opener = inject(DeckOpener);
}
