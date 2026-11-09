import { inject, Injectable } from '@angular/core';
import { ApiError, errorMessage } from '../api/api-error';
import { DeckbuilderApi } from '../api/deckbuilder-api.service';
import { Deck } from '../api/models';
import { CollectionBrowser } from '../collection/collection-browser.service';
import { Notifier } from '../notifier/notifier';
import { DeckChanges } from './deck-changes.service';
import { DeckDraft } from './deck-draft.service';
import { DeckFormatSelection } from './deck-format-selection.service';
import { formatDecklist } from './deck-import';
import { addImportedCards, deckStateOf } from './deck-model';

/** Bringing a deck in from the server: a saved deck, a library deck, a pasted decklist, or the PC errata conversion. */
@Injectable()
export class DeckOpener {
  private readonly api = inject(DeckbuilderApi);
  private readonly draft = inject(DeckDraft);
  private readonly changes = inject(DeckChanges);
  private readonly formats = inject(DeckFormatSelection);
  private readonly collection = inject(CollectionBrowser);
  private readonly notifier = inject(Notifier);

  /** Opens one of the player's decks. */
  loadDeck(name: string): void {
    this.api.getDeck(name).subscribe({
      next: (deck) => deck && this.setupDeck(deck, name),
      error: (error: unknown) => this.notifier.tell(errorMessage(error)),
    });
  }

  /** Opens a library deck: it keeps the library deck's name and counts as unsaved. */
  loadLibraryDeck(name: string): void {
    this.api.getLibraryDeck(name).subscribe({
      next: (deck) => {
        if (deck) {
          this.setupDeck(deck, name);
          this.changes.deckModified(true);
        }
      },
      error: (error: unknown) => this.notifier.tell(errorMessage(error)),
    });
  }

  /** Shows a deck that came from the server. */
  setupDeck(deck: Deck, name: string | null): void {
    this.changes.clearDeck();
    this.draft.deckName.set(name);
    this.draft.label.set({ text: name ?? 'New deck', modified: false });
    this.draft.notes.set(deck.notes);
    this.draft.deck.set(deckStateOf(deck));
    if (deck.drawDeck.length > 0) {
      // Can enable Convert for an Anything Goes deck; the mark itself is cleared below
      this.changes.deckModified(true);
    }
    if (deck.targetFormat) {
      this.changes.selectFormat(this.formats.indexForDeck(deck.targetFormat));
    }
    this.collection.load();
    this.changes.deckModified(false);
  }

  /**
   * Replaces the deck with the cards the server recognises in a pasted decklist. The deck has no name and is shown as
   * "Imported Deck (unsaved)".
   */
  importDecklist(text: string): void {
    this.draft.deckName.set(null);
    this.changes.clearDeck();
    this.api.importDecklist(formatDecklist(text)).subscribe({
      next: (items) => {
        this.draft.deck.update((deck) => addImportedCards(deck, items));
        this.changes.deckModified(true);
        this.draft.label.set({ text: 'Imported Deck (unsaved)', modified: false });
      },
      error: (error: unknown) => this.notifier.tell(errorMessage(error, { 414: 'Deck too large to import.' })),
    });
  }

  convertErrata(): void {
    const notes = this.draft.notes();
    this.api.convertErrata(this.formats.effectiveFormat(), this.formats.contentsOf(this.draft.deck())).subscribe({
      next: (deck) => {
        if (deck) {
          this.setupDeck(deck, this.draft.deckName());
          // The converted deck comes back without notes
          this.draft.notes.set(notes);
        }
        this.changes.deckModified(true);
        this.changes.convertErrataEnabled.set(false);
      },
      error: (error: unknown) => {
        if (ApiError.from(error).is(400)) {
          this.changes.convertErrataEnabled.set(true);
        }
        this.notifier.tell(errorMessage(error, { 400: 'Error processing errata conversion.' }));
      },
    });
  }
}
