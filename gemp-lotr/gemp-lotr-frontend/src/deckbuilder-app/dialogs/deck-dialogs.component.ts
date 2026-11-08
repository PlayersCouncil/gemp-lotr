import { Component, inject } from '@angular/core';
import { DeckLibraryService } from '../../core/deck/deck-library.service';
import { DeckService } from '../../core/deck/deck.service';
import { UiButtonDirective } from '../shared/ui-button.directive';
import { UiDialogComponent } from '../shared/ui-dialog.component';

/**
 * The deck list and library dialogs, the import dialog and the notes editor, with the legacy markup. The legacy page
 * used one dialog for both lists, so whichever opened first gave both its title; each list now has its own title.
 * Deck names are shown as text (the legacy page inserted them as HTML).
 */
@Component({
  selector: 'gemp-deck-dialogs',
  imports: [UiDialogComponent, UiButtonDirective],
  template: `
    @if (dialogs.deckList(); as list) {
      <gemp-ui-dialog
        [title]="list.kind === 'library' ? 'Library Decks' : 'Your Saved Decks'"
        [width]="700"
        [height]="400"
        [modal]="true"
        (closed)="dialogs.deckList.set(null)"
      >
        @for (entry of list.decks; track $index) {
          <div class="deckItem">
            <button title="Open deck" gempUiButton (click)="dialogs.openDeck(entry)">
              <span class="ui-button-text"><span class="ui-icon ui-icon-folder-open"></span></span>
            </button>
            @if (list.kind === 'decks') {
              <button title="Rename deck" gempUiButton (click)="dialogs.renameDeck(entry)">
                <span class="ui-button-text"><span class="ui-icon ui-icon-pencil"></span></span>
              </button>
              <button title="Share deck list" gempUiButton (click)="dialogs.shareDeck(entry)">
                <span class="ui-button-text"><span class="ui-icon ui-icon-extlink"></span></span>
              </button>
              <button title="Delete deck" gempUiButton (click)="dialogs.deleteDeck(entry)">
                <span class="ui-button-text"><span class="ui-icon ui-icon-trash"></span></span>
              </button>
            } @else {
              <button title="Deck list" gempUiButton (click)="dialogs.showLibraryDeck(entry)">
                <span class="ui-button-text"><span class="ui-icon ui-icon-clipboard"></span></span>
              </button>
            }
            <span
              ><b>[{{ entry.formatName }}]</b> - {{ entry.name }}</span
            >
          </div>
        }
      </gemp-ui-dialog>
    }

    @if (dialogs.importOpen()) {
      <gemp-ui-dialog title="Import deck" [openedEmpty]="true" (closed)="dialogs.importOpen.set(false)">
        @for (round of [dialogs.importRound()]; track round) {
          <div>
            <textarea #decklist rows="5" cols="30" id="deckImport" decklist="decklist"></textarea
            ><button title="Import" gempUiButton (click)="deck.importDecklist(decklist.value)">
              <span class="ui-button-text">Import</span>
            </button>
          </div>
        }
      </gemp-ui-dialog>
    }

    @if (dialogs.notesOpen()) {
      <gemp-ui-dialog
        title="Edit Deck Notes"
        contentClass="notesDialog"
        [width]="700"
        [height]="400"
        [modal]="true"
        (closed)="deck.setNotes(notes.value); dialogs.notesOpen.set(false)"
      >
        <textarea #notes class="notesText" [value]="deck.notes()" (change)="deck.setNotes(notes.value)"></textarea>
      </gemp-ui-dialog>
    }
  `,
})
export class DeckDialogsComponent {
  protected readonly dialogs = inject(DeckLibraryService);
  protected readonly deck = inject(DeckService);
}
