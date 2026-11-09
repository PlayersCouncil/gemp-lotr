import { Injectable, signal } from '@angular/core';
import { DeckState, EMPTY_DECK } from './deck-model';

/** The deck name shown in the toolbar; a modified deck is shown in orange with a "*". */
export interface DeckLabel {
  text: string;
  modified: boolean;
}

/** The deck being edited: its cards, name and notes, and whether it has unsaved changes. Only state, no behaviour. */
@Injectable({ providedIn: 'root' })
export class DeckDraft {
  readonly deck = signal<DeckState>(EMPTY_DECK);
  /** Null for a deck that was never saved. */
  readonly deckName = signal<string | null>(null);
  readonly notes = signal('');
  readonly label = signal<DeckLabel>({ text: 'New deck', modified: false });
  /** Unsaved changes. */
  readonly contentsDirty = signal(false);

  markModified(modified: boolean): void {
    this.contentsDirty.set(modified);
    this.label.set({ text: this.deckName() ?? 'New deck', modified });
  }
}
