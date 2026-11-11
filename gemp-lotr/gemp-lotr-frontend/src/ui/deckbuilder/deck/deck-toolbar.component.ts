import { Component, computed, inject } from '@angular/core';
import { BASE_COLLECTION_TYPES, CollectionBrowser } from '../../../core/collection/collection-browser.service';
import { DeckLibraryService } from '../../../core/deck/deck-library.service';
import { DeckChanges } from '../../../core/deck/deck-changes.service';
import { DeckDraft } from '../../../core/deck/deck-draft.service';
import { DeckFormatSelection } from '../../../core/deck/deck-format-selection.service';
import { DeckOpener } from '../../../core/deck/deck-opener.service';
import { DeckSaver } from '../../../core/deck/deck-saver.service';
import { AutoZoomSetting } from '../../../core/platform/auto-zoom-setting';
import { THEME_LABELS, THEME_PICKER, THEMES, ThemeService } from '../../../core/platform/theme.service';

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
 * and, when players may switch themes (THEME_PICKER, on), `button.gd-button.gd-toolbar__theme[data-action=theme]`
 * (drawn after the deck's name): it switches to the next theme, which its `title` and `aria-label` name.
 */
@Component({
  selector: 'gd-deck-toolbar',
  styleUrl: './deck-toolbar.component.css',
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
          [disabled]="button.action === 'convert-errata' && !changes.convertErrataEnabled()"
          (click)="button.run()"
        ></button>
      }
    </div>
    <select class="gd-toolbar__format" aria-label="Format" (change)="selectFormat($event)">
      @for (option of formats.formatOptions(); track $index) {
        <option
          [value]="option.value"
          [disabled]="option.disabled"
          [selected]="$index === formats.selectedFormatIndex()"
        >
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
    <span class="gd-toolbar__deck-name" [attr.data-modified]="draft.label().modified ? '' : null">{{
      deckName()
    }}</span>
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
    @if (themePicker) {
      <button
        type="button"
        class="gd-button gd-toolbar__theme"
        data-action="theme"
        [title]="nextThemeLabel()"
        [attr.aria-label]="nextThemeLabel()"
        (click)="theme.select(nextTheme())"
      ></button>
    }
  `,
})
export class DeckToolbarComponent {
  protected readonly draft = inject(DeckDraft);
  protected readonly formats = inject(DeckFormatSelection);
  protected readonly changes = inject(DeckChanges);
  private readonly saver = inject(DeckSaver);
  private readonly opener = inject(DeckOpener);
  protected readonly library = inject(DeckLibraryService);
  protected readonly zoom = inject(AutoZoomSetting);
  protected readonly theme = inject(ThemeService);
  protected readonly themePicker = inject(THEME_PICKER);
  private readonly collection = inject(CollectionBrowser);

  protected readonly actions: readonly ToolbarAction[] = [
    { action: 'new', label: 'New deck', run: () => this.saver.newDeck() },
    { action: 'save', label: 'Save deck', run: () => this.saver.saveCurrentDeck() },
    { action: 'rename', label: 'Rename deck', run: () => this.saver.renameCurrentDeck() },
    { action: 'copy', label: 'Copy deck to new', run: () => this.saver.copyDeck() },
    { action: 'import', label: 'Import Deck', run: () => this.library.openImport() },
    { action: 'library', label: 'Open Library', run: () => this.library.openLibrary() },
    { action: 'deck-list', label: 'Deck list', run: () => this.library.openDeckList() },
    { action: 'convert-errata', label: 'Convert to PC Errata', run: () => this.opener.convertErrata() },
  ];

  protected readonly deckName = computed(() => {
    const { text, modified } = this.draft.label();
    return modified ? `*${text} - modified` : text;
  });
  protected readonly collectionType = this.collection.collectionType;
  protected readonly collections = computed(() => [...BASE_COLLECTION_TYPES, ...this.collection.limitedCollections()]);
  /** The theme the picker switches to: the next one, after the last the first. */
  protected readonly nextTheme = computed(
    () => THEMES[(THEMES.indexOf(this.theme.theme()) + 1) % THEMES.length] ?? THEMES[0],
  );
  protected readonly nextThemeLabel = computed(() => `Switch to the ${THEME_LABELS[this.nextTheme()]} theme`);

  protected selectFormat(event: Event): void {
    this.changes.selectFormat((event.target as HTMLSelectElement).selectedIndex);
  }

  protected selectCollection(event: Event): void {
    this.collection.selectCollectionType((event.target as HTMLSelectElement).value);
  }
}
