import { Component, computed, inject } from '@angular/core';
import { BASE_COLLECTION_TYPES, CollectionService } from '../../../core/collection/collection.service';
import { DeckLibraryService } from '../../../core/deck/deck-library.service';
import { DeckService } from '../../../core/deck/deck.service';
import { AutoZoomSetting } from '../../../core/platform/auto-zoom-setting';
import { THEME_LABELS, THEMES, ThemeService } from '../../../core/platform/theme.service';

/** A deck button: what it does (`data-action`, which the theme draws an icon for) and its name. */
interface ToolbarAction {
  action: string;
  label: string;
  run: () => unknown;
}

/**
 * The deck toolbar: the deck buttons, the format, the notes, the deck's name, the hover zoom toggle, the collection
 * and the theme. Every control is a native element.
 *
 * DOM contract: `.gd-toolbar` (`role=toolbar`) holding `.gd-toolbar__deck-actions` with a `button.gd-button` per
 * action (`data-action` = new, save, rename, copy, import, library, deck-list, convert-errata; `title` and
 * `aria-label` name it), `select.gd-toolbar__format`, `button.gd-button[data-action=notes]`, `.gd-toolbar__deck-name`
 * (`data-modified` while it has unsaved changes, with the legacy "*name - modified" text), then
 * `button.gd-button[data-action=auto-zoom]` (`aria-pressed`; absent on touch devices), `select.gd-toolbar__collection`
 * and `select.gd-toolbar__theme`.
 */
@Component({
  selector: 'gd-deck-toolbar',
  host: { class: 'gd-toolbar', role: 'toolbar', 'aria-label': 'Deck' },
  template: `
    <div class="gd-toolbar__deck-actions">
      @for (button of actions; track button.action) {
        <button
          type="button"
          class="gd-button"
          [attr.data-action]="button.action"
          [title]="button.label"
          [attr.aria-label]="button.label"
          [disabled]="button.action === 'convert-errata' && !deck.convertErrataEnabled()"
          (click)="button.run()"
        ></button>
      }
    </div>
    <select class="gd-toolbar__format" aria-label="Format" (change)="selectFormat($event)">
      @for (option of deck.formatOptions(); track $index) {
        <option [value]="option.value" [disabled]="option.disabled" [selected]="$index === deck.selectedFormatIndex()">
          {{ option.label }}
        </option>
      }
    </select>
    <button
      type="button"
      class="gd-button"
      data-action="notes"
      title="Add/Edit Notes"
      aria-label="Add/Edit Notes"
      (click)="library.openNotes()"
    ></button>
    <span class="gd-toolbar__deck-name" [attr.data-modified]="deck.label().modified ? '' : null">{{ deckName() }}</span>
    @if (zoom.available) {
      <button
        type="button"
        class="gd-button"
        data-action="auto-zoom"
        title="Auto-zoom cards on hover"
        aria-label="Auto-zoom cards on hover"
        [attr.aria-pressed]="zoom.enabled()"
        (click)="zoom.toggle()"
      ></button>
    }
    <select class="gd-toolbar__collection" aria-label="Collection" (change)="selectCollection($event)">
      @for (collection of collections(); track collection.type) {
        <option [value]="collection.type" [selected]="collection.type === collectionType()">
          {{ collection.name }}
        </option>
      }
    </select>
    <select class="gd-toolbar__theme" aria-label="Theme" title="Theme" (change)="selectTheme($event)">
      @for (option of themes; track option.name) {
        <option [value]="option.name" [selected]="option.name === theme.theme()">{{ option.label }}</option>
      }
    </select>
  `,
})
export class DeckToolbarComponent {
  protected readonly deck = inject(DeckService);
  protected readonly library = inject(DeckLibraryService);
  protected readonly zoom = inject(AutoZoomSetting);
  protected readonly theme = inject(ThemeService);
  private readonly collection = inject(CollectionService);

  protected readonly actions: readonly ToolbarAction[] = [
    { action: 'new', label: 'New deck', run: () => this.deck.newDeck() },
    { action: 'save', label: 'Save deck', run: () => this.deck.saveCurrentDeck() },
    { action: 'rename', label: 'Rename deck', run: () => this.deck.renameCurrentDeck() },
    { action: 'copy', label: 'Copy deck to new', run: () => this.deck.copyDeck() },
    { action: 'import', label: 'Import Deck', run: () => this.library.openImport() },
    { action: 'library', label: 'Open Library', run: () => this.library.openLibrary() },
    { action: 'deck-list', label: 'Deck list', run: () => this.library.openDeckList() },
    { action: 'convert-errata', label: 'Convert to PC Errata', run: () => this.deck.convertErrata() },
  ];

  protected readonly deckName = computed(() => {
    const { text, modified } = this.deck.label();
    return modified ? `*${text} - modified` : text;
  });
  protected readonly collectionType = this.collection.collectionType;
  protected readonly collections = computed(() => [...BASE_COLLECTION_TYPES, ...this.collection.limitedCollections()]);
  protected readonly themes = THEMES.map((name) => ({ name, label: THEME_LABELS[name] }));

  protected selectFormat(event: Event): void {
    this.deck.selectFormat((event.target as HTMLSelectElement).selectedIndex);
  }

  protected selectCollection(event: Event): void {
    this.collection.selectCollectionType((event.target as HTMLSelectElement).value);
  }

  protected selectTheme(event: Event): void {
    void this.theme.select((event.target as HTMLSelectElement).value);
  }
}
