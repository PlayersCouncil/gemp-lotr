import { Component, inject, Injectable, signal } from '@angular/core';
import { GENERIC_COMMUNICATION_ERROR } from '../core/api/api-error';
import { DeckbuilderApi } from '../core/api/deckbuilder-api.service';
import { DeckSummary } from '../core/api/models';
import { DeckService } from '../core/deck/deck.service';
import { UiButtonDirective } from '../shared/ui-button.directive';
import { UiDialogComponent } from '../shared/ui-dialog.component';

export interface DeckListState {
  kind: 'decks' | 'library';
  decks: DeckSummary[];
}

/**
 * The toolbar's dialogs (deckBuildingUi.loadDeckList, loadLibraryList, importDecklist, editNotes): which are open and
 * what they show. Leaving the current deck (deck list, library, import) first offers to save it.
 */
@Injectable({ providedIn: 'root' })
export class DeckDialogsService {
  private readonly api = inject(DeckbuilderApi);
  private readonly deck = inject(DeckService);

  readonly deckList = signal<DeckListState | null>(null);
  readonly importOpen = signal(false);
  /** Changes each time the import button is used: the dialog then starts empty again. */
  readonly importRound = signal(0);
  readonly notesOpen = signal(false);

  openDeckList(): void {
    this.deck.savePrompt();
    this.loadDeckList();
  }

  openLibrary(): void {
    this.deck.savePrompt();
    this.api.listLibraryDecks().subscribe({
      next: (decks) => this.deckList.set({ kind: 'library', decks }),
      error: () => alert(GENERIC_COMMUNICATION_ERROR),
    });
  }

  openImport(): void {
    this.deck.savePrompt();
    this.importRound.update((n) => n + 1);
    this.importOpen.set(true);
  }

  openNotes(): void {
    this.notesOpen.set(true);
  }

  // --- Deck list entries ---

  openDeck(entry: DeckSummary): void {
    if (this.deckList()?.kind === 'library') {
      this.deck.loadLibraryDeck(entry.name);
    } else {
      this.deck.loadDeck(entry.name);
    }
  }

  renameDeck(entry: DeckSummary): void {
    const oldName = entry.name;
    this.deck.renameDeck(oldName, (newName) => {
      this.deckList.update((list) =>
        list ? { ...list, decks: list.decks.map((d) => (d === entry ? { ...d, name: newName } : d)) } : list,
      );
      this.deck.deckRenamed(oldName, newName);
    });
  }

  /** "Share deck list": the server's public page of the deck, in a new tab. */
  shareDeck(entry: DeckSummary): void {
    this.api.shareDeckUrl(entry.name).subscribe({
      next: (url) => window.open(url, '_blank'),
      error: () => alert(GENERIC_COMMUNICATION_ERROR),
    });
  }

  /** The library's "Deck list": the deck as a web page, in a new tab. */
  showLibraryDeck(entry: DeckSummary): void {
    window.open(this.api.libraryDeckHtmlUrl(entry.name), '_blank');
  }

  deleteDeck(entry: DeckSummary): void {
    if (!confirm('Are you sure you want to delete this deck?')) {
      return;
    }
    this.api.deleteDeck(entry.name).subscribe({
      next: () => {
        this.deck.deckDeleted(entry.name);
        this.loadDeckList();
      },
      error: () => alert(GENERIC_COMMUNICATION_ERROR),
    });
  }

  private loadDeckList(): void {
    this.api.listDecks().subscribe({
      next: (decks) => this.deckList.set({ kind: 'decks', decks }),
      error: () => alert(GENERIC_COMMUNICATION_ERROR),
    });
  }
}

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
  protected readonly dialogs = inject(DeckDialogsService);
  protected readonly deck = inject(DeckService);
}
