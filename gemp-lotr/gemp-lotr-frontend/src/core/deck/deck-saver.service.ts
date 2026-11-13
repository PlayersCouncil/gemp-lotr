import { PageLifetime } from '../platform/page-lifetime.service';
import { inject, Injectable } from '@angular/core';
import { errorMessage } from '../api/api-error';
import { DeckbuilderApi } from '../api/deckbuilder-api.service';
import { Notifier } from '../notifier/notifier';
import { DeckChanges } from './deck-changes.service';
import { DeckDraft } from './deck-draft.service';
import { DeckFormatSelection } from './deck-format-selection.service';
import { DECK_NAME_LENGTH_MESSAGE, isValidDeckName } from './deck-name';

/** Keeping the deck: New, Save, Rename and Copy, and offering to save before unsaved changes would be lost. */
@Injectable()
export class DeckSaver {
  private readonly api = inject(DeckbuilderApi);
  private readonly lifetime = inject(PageLifetime);
  private readonly draft = inject(DeckDraft);
  private readonly changes = inject(DeckChanges);
  private readonly formats = inject(DeckFormatSelection);
  private readonly notifier = inject(Notifier);

  async newDeck(): Promise<void> {
    if (await this.savePromptOnNew()) {
      this.draft.deckName.set(null);
      this.draft.notes.set('');
      this.changes.clearDeck();
    }
  }

  /** Saves, asking for a name first if the deck has none. False if the player cancelled or the name was invalid. */
  async saveCurrentDeck(): Promise<boolean> {
    if (this.draft.deckName() !== null) {
      this.saveDeck();
      return true;
    }
    const newName = await this.notifier.askText('Enter the name of the deck', '');
    if (newName === null) {
      return false;
    }
    if (!isValidDeckName(newName)) {
      await this.notifier.alert(DECK_NAME_LENGTH_MESSAGE);
      return false;
    }
    this.draft.deckName.set(newName);
    this.draft.label.set({ text: newName, modified: false });
    this.saveDeck();
    return true;
  }

  async renameCurrentDeck(): Promise<void> {
    const name = this.draft.deckName();
    if (name === null) {
      await this.saveCurrentDeck();
      return;
    }
    await this.renameDeck(name, async (newName) => {
      // As on the legacy page, the changes are saved before the name changes, so they land in a new deck under the old
      // name and the renamed deck keeps its previous cards. A known bug the owner chose to keep for now.
      if (this.draft.contentsDirty() && (await this.notifier.confirm('Do you wish to save this deck?'))) {
        this.saveDeck();
      }
      this.draft.deckName.set(newName);
      this.changes.deckModified(this.draft.contentsDirty());
    });
  }

  /** Asks for a new name and renames the saved deck on the server (also used by the deck list). */
  async renameDeck(oldName: string, renamed: (newName: string) => void | Promise<void>): Promise<void> {
    const newName = await this.notifier.askText('Enter new name for the deck', oldName);
    if (newName === null) {
      return;
    }
    if (!isValidDeckName(newName)) {
      await this.notifier.alert(DECK_NAME_LENGTH_MESSAGE);
      return;
    }
    this.lifetime.write(this.api.renameDeck(oldName, newName), {
      next: () => void renamed(newName),
      error: (error: unknown) =>
        this.notifier.tell(errorMessage(error, { 404: "Couldn't find the deck to rename on the server." })),
    });
  }

  /** "Copy deck to new": the same cards, saved under a new name next time. */
  copyDeck(): void {
    this.draft.deckName.set(null);
    this.changes.deckModified(true);
  }

  /** A saved deck was renamed from the deck list: the open deck follows if it was that one. */
  deckRenamed(oldName: string, newName: string): void {
    if (this.draft.deckName() === oldName) {
      this.draft.deckName.set(newName);
      this.changes.deckModified(this.draft.contentsDirty());
    }
  }

  /** A saved deck was deleted from the deck list: if it was the open one, a new deck takes its place. */
  deckDeleted(name: string): void {
    if (this.draft.deckName() === name) {
      this.draft.deckName.set(null);
      this.changes.clearDeck();
    }
  }

  /**
   * Before leaving the deck (deck list, library, import): offers to save unsaved changes. False if the player wanted
   * to save but the save did not start.
   */
  async savePrompt(): Promise<boolean> {
    return (
      !this.draft.contentsDirty() ||
      ((await this.notifier.confirm(
        'Your deck has been modified.  Would you like to save your deck before proceeding?',
      )) &&
        (await this.saveCurrentDeck()))
    );
  }

  /** Before a new deck: OK saves, Cancel discards. False if saving was cancelled at the name prompt. */
  private async savePromptOnNew(): Promise<boolean> {
    if (!this.draft.contentsDirty()) {
      return true;
    }
    const save = await this.notifier.confirm(
      "Your deck has been modified.  Click 'OK' to save, or 'Cancel' to discard your changes without saving.",
    );
    return save ? this.saveCurrentDeck() : true;
  }

  private saveDeck(): void {
    const name = this.draft.deckName();
    if (name === null) {
      return;
    }
    // The RTMD option's value is the league code, which the server stores so the deck stays with the league
    const save = this.api.saveDeck(
      name,
      this.formats.selectedFormat().value,
      this.draft.notes(),
      this.formats.contentsOf(this.draft.deck()),
    );
    this.lifetime.write(save, {
      next: () => {
        this.changes.deckModified(false);
        this.notifier.tell('Deck was saved.  Refresh the Game Hall to see it!');
      },
      error: (error: unknown) => this.notifier.tell(errorMessage(error, { 400: 'Invalid deck format.' })),
    });
  }
}
