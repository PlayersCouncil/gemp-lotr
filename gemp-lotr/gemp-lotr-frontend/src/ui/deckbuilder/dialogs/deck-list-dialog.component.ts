import { Component, computed, inject } from '@angular/core';
import { DeckSummary } from '../../../core/api/models';
import { DeckLibraryService } from '../../../core/deck/deck-library.service';
import { DialogComponent } from '../../shared/dialog/dialog.component';

interface EntryAction {
  action: string;
  label: string;
  run: (entry: DeckSummary) => unknown;
}

/**
 * The player's saved decks ("Your Saved Decks") or the library ("Library Decks"): one line per deck with its buttons
 * and "[format] - name". Saved decks can be opened, renamed, shared (their public page, in a new tab) and deleted;
 * library decks opened, or shown as a deck list in a new tab. Deck names are shown as text.
 *
 * DOM contract: a modal `gd-dialog` with `data-dialog` = deck-list or library, holding `ul.gd-deck-list` with an
 * `li.gd-deck-list__item` per deck: a `button.gd-button` per action (`data-action` = open, rename, share, delete for
 * saved decks; open, deck-list for the library; `title` and `aria-label` name it) holding its
 * `.gd-deck-list__icon`, then `.gd-deck-list__name` with
 * `b.gd-deck-list__format` ("[format]") and the name.
 */
@Component({
  selector: 'gd-deck-list-dialog',
  styleUrl: './deck-list-dialog.component.css',
  imports: [DialogComponent],
  host: { style: 'display: contents' },
  template: `
    <gd-dialog
      [name]="list()?.kind === 'library' ? 'library' : 'deck-list'"
      [heading]="list()?.kind === 'library' ? 'Library Decks' : 'Your Saved Decks'"
      [open]="list() !== null"
      (closed)="library.deckList.set(null)"
    >
      <ul class="gd-deck-list">
        @for (entry of list()?.decks ?? []; track entry.name) {
          <li class="gd-deck-list__item">
            @for (button of actions(); track button.action) {
              <button
                type="button"
                class="gd-button"
                [attr.data-action]="button.action"
                [title]="button.label"
                [attr.aria-label]="button.label + ': ' + entry.name"
                (click)="button.run(entry)"
              >
                <span class="gd-deck-list__icon"></span>
              </button>
            }
            <span class="gd-deck-list__name"
              ><b class="gd-deck-list__format">[{{ entry.formatName }}]</b> - {{ entry.name }}</span
            >
          </li>
        }
      </ul>
    </gd-dialog>
  `,
})
export class DeckListDialogComponent {
  protected readonly library = inject(DeckLibraryService);
  protected readonly list = this.library.deckList;

  private readonly deckActions: readonly EntryAction[] = [
    { action: 'open', label: 'Open deck', run: (entry) => this.library.openDeck(entry) },
    { action: 'rename', label: 'Rename deck', run: (entry) => this.library.renameDeck(entry) },
    { action: 'share', label: 'Share deck list', run: (entry) => this.library.shareDeck(entry) },
    { action: 'delete', label: 'Delete deck', run: (entry) => this.library.deleteDeck(entry) },
  ];
  private readonly libraryActions: readonly EntryAction[] = [
    { action: 'open', label: 'Open deck', run: (entry) => this.library.openDeck(entry) },
    {
      action: 'deck-list',
      label: 'Deck list',
      run: (entry) => this.library.showLibraryDeck(entry),
    },
  ];

  protected readonly actions = computed(() =>
    this.list()?.kind === 'library' ? this.libraryActions : this.deckActions,
  );
}
