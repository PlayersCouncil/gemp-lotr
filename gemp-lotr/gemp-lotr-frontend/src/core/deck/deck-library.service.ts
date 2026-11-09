import { inject, Injectable, signal } from '@angular/core';
import { GENERIC_COMMUNICATION_ERROR } from '../api/api-error';
import { DeckbuilderApi } from '../api/deckbuilder-api.service';
import { DeckSummary } from '../api/models';
import { Notifier } from '../notifier/notifier';
import { ExternalLinks } from '../platform/external-links.service';
import { DeckOpener } from './deck-opener.service';
import { DeckSaver } from './deck-saver.service';

export interface DeckListState {
  kind: 'decks' | 'library';
  decks: DeckSummary[];
}

/**
 * The player's saved decks and the library, decklist import and notes (deckBuildingUi.loadDeckList, loadLibraryList,
 * importDecklist, editNotes): which of their dialogs are open and what they show. Leaving the current deck (deck list, library, import) first offers to save it.
 */
@Injectable()
export class DeckLibraryService {
  private readonly api = inject(DeckbuilderApi);
  private readonly saver = inject(DeckSaver);
  private readonly opener = inject(DeckOpener);
  private readonly notifier = inject(Notifier);
  private readonly links = inject(ExternalLinks);

  readonly deckList = signal<DeckListState | null>(null);
  readonly importOpen = signal(false);
  /** Changes each time the import button is used: the dialog then starts empty again. */
  readonly importRound = signal(0);
  readonly notesOpen = signal(false);

  /** As in the legacy page, the list opens whatever the player answered to the save prompt. */
  async openDeckList(): Promise<void> {
    await this.saver.savePrompt();
    this.loadDeckList();
  }

  async openLibrary(): Promise<void> {
    await this.saver.savePrompt();
    this.api.listLibraryDecks().subscribe({
      next: (decks) => this.deckList.set({ kind: 'library', decks }),
      error: () => this.tell(GENERIC_COMMUNICATION_ERROR),
    });
  }

  async openImport(): Promise<void> {
    await this.saver.savePrompt();
    this.importRound.update((n) => n + 1);
    this.importOpen.set(true);
  }

  openNotes(): void {
    this.notesOpen.set(true);
  }

  // --- Deck list entries ---

  openDeck(entry: DeckSummary): void {
    if (this.deckList()?.kind === 'library') {
      this.opener.loadLibraryDeck(entry.name);
    } else {
      this.opener.loadDeck(entry.name);
    }
  }

  async renameDeck(entry: DeckSummary): Promise<void> {
    const oldName = entry.name;
    await this.saver.renameDeck(oldName, (newName) => {
      this.deckList.update((list) =>
        list ? { ...list, decks: list.decks.map((d) => (d === entry ? { ...d, name: newName } : d)) } : list,
      );
      this.saver.deckRenamed(oldName, newName);
    });
  }

  /** "Share deck list": the server's public page of the deck, in a new tab. */
  shareDeck(entry: DeckSummary): void {
    this.api.shareDeckUrl(entry.name).subscribe({
      next: (url) => this.links.open(url),
      error: () => this.tell(GENERIC_COMMUNICATION_ERROR),
    });
  }

  /** The library's "Deck list": the deck as a web page, in a new tab. */
  showLibraryDeck(entry: DeckSummary): void {
    this.links.open(this.api.libraryDeckHtmlUrl(entry.name));
  }

  async deleteDeck(entry: DeckSummary): Promise<void> {
    if (!(await this.notifier.confirm('Are you sure you want to delete this deck?'))) {
      return;
    }
    this.api.deleteDeck(entry.name).subscribe({
      next: () => {
        this.saver.deckDeleted(entry.name);
        this.loadDeckList();
      },
      error: () => this.tell(GENERIC_COMMUNICATION_ERROR),
    });
  }

  private loadDeckList(): void {
    this.api.listDecks().subscribe({
      next: (decks) => this.deckList.set({ kind: 'decks', decks }),
      error: () => this.tell(GENERIC_COMMUNICATION_ERROR),
    });
  }

  private tell(message: string): void {
    void this.notifier.alert(message);
  }
}
