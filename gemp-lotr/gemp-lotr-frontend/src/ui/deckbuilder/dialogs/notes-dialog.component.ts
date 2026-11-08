import { Component, ElementRef, inject, viewChild } from '@angular/core';
import { DeckLibraryService } from '../../../core/deck/deck-library.service';
import { DeckService } from '../../../core/deck/deck.service';
import { DialogComponent } from '../../shared/dialog/dialog.component';

/**
 * "Edit Deck Notes": the deck's notes in a text area, kept when it changes and when the dialog closes.
 *
 * DOM contract: a modal `gd-dialog` with `data-dialog=notes`, holding `textarea.gd-notes__text`.
 */
@Component({
  selector: 'gd-notes-dialog',
  imports: [DialogComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog name="notes" heading="Edit Deck Notes" [open]="library.notesOpen()" (closed)="closed()">
      @if (library.notesOpen()) {
        <textarea
          #notes
          class="gd-notes__text"
          aria-label="Deck notes"
          [value]="deck.notes()"
          (change)="deck.setNotes(notes.value)"
        ></textarea>
      }
    </gd-dialog>
  `,
})
export class NotesDialogComponent {
  protected readonly library = inject(DeckLibraryService);
  protected readonly deck = inject(DeckService);

  private readonly notes = viewChild<ElementRef<HTMLTextAreaElement>>('notes');

  /** Closing keeps the text, even when the text area had no change event yet (it still had the focus). */
  protected closed(): void {
    const notes = this.notes();
    if (notes) {
      this.deck.setNotes(notes.nativeElement.value);
    }
    this.library.notesOpen.set(false);
  }
}
