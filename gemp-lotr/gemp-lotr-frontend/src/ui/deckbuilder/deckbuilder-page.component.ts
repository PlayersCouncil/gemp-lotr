import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { DeckStartup } from '../../core/deck/deck-startup.service';
import { PacksService } from '../../core/packs/packs.service';
import { AutoZoomComponent } from './auto-zoom/auto-zoom.component';
import { CardInfoComponent } from './card-info/card-info.component';
import { CollectionFilterComponent } from './collection/collection-filter.component';
import { CollectionGridComponent } from './collection/collection-grid.component';
import { DeckAreaComponent } from './deck/deck-area.component';
import { DeckListDialogComponent } from './dialogs/deck-list-dialog.component';
import { ImportDialogComponent } from './dialogs/import-dialog.component';
import { NotesDialogComponent } from './dialogs/notes-dialog.component';
import { PackDialogsComponent } from './dialogs/pack-dialogs.component';
import { NotifierDialogComponent } from '../shared/notifier/notifier-dialog.component';
import { DragHandlers, PointerDragDirective } from '../shared/pointer-drag/pointer-drag.directive';

/** Arrow keys on the resizer move it by this much (px). */
export const RESIZE_STEP = 10;

/** The pane a theme that shows one pane at a time (phones) shows: the tab bar's choice. */
export type PagePane = 'deck' | 'collection';

/**
 * The deck builder page frame: the deck (left), the collection (right) and the separator between them, which resizes
 * the collection by dragging or with the arrow keys and closes or opens it with its toggle. The page only records the width asked for; the theme keeps it within its limits.
 *
 * DOM contract: `.gd-page` with `data-collection` (open, closed) and `--gd-collection-width` (px, once resized);
 * `data-pane` (deck, collection: the tab bar's choice, for themes that show one pane at a time);
 * `section.gd-page__deck`, `div.gd-page__separator` (`role=separator`, `data-dragging` while dragged) holding
 * `button.gd-page__toggle` (`aria-expanded`), `section.gd-page__collection` holding the filter and the cards, and
 * `div.gd-page__tabs` (`role=tablist`) with a `button.gd-page__tab` (`role=tab`, `data-pane`, `aria-selected`) per pane;
 * then
 * the dialogs (deck list and library, import, notes, selection pack, new items), the card information, the hover zoom
 * and the notifier's dialog.
 */
@Component({
  selector: 'deckbuilder-app',
  imports: [
    PointerDragDirective,
    DeckAreaComponent,
    CollectionFilterComponent,
    CollectionGridComponent,
    CardInfoComponent,
    AutoZoomComponent,
    DeckListDialogComponent,
    ImportDialogComponent,
    NotesDialogComponent,
    PackDialogsComponent,
    NotifierDialogComponent,
  ],
  host: {
    class: 'gd-page',
    '[attr.data-collection]': "collectionOpen() ? 'open' : 'closed'",
    '[attr.data-pane]': 'pane()',
    '[style.--gd-collection-width]': "collectionWidth() === null ? null : collectionWidth() + 'px'",
    '[style.--gd-drag-width]': "dragWidth() === null ? null : dragWidth() + 'px'",
  },
  template: `
    <section class="gd-page__deck" id="gd-pane-deck" aria-label="Deck"><gd-deck /></section>
    <div
      class="gd-page__separator"
      role="separator"
      aria-orientation="vertical"
      aria-label="Resize the collection"
      tabindex="0"
      [attr.data-dragging]="dragging() ? '' : null"
      [gdPointerDrag]="resizeDrag"
      (keydown)="resizeWithKeys($event)"
    >
      <button
        type="button"
        class="gd-page__toggle"
        [attr.aria-expanded]="collectionOpen()"
        [attr.aria-label]="collectionOpen() ? 'Close the collection' : 'Open the collection'"
        [title]="collectionOpen() ? 'Close' : 'Open'"
        (click)="toggleCollection()"
      ></button>
    </div>
    @if (dragWidth() !== null) {
      <div #dragPreview class="gd-page__drag-preview" aria-hidden="true"></div>
    }
    <section class="gd-page__collection" id="gd-pane-collection" aria-label="Collection" [hidden]="!collectionOpen()">
      <gd-collection-filter />
      <gd-collection-grid />
    </section>
    <div class="gd-page__tabs" role="tablist" aria-label="Show">
      @for (tab of tabs; track tab.pane) {
        <button
          type="button"
          role="tab"
          class="gd-page__tab"
          [attr.data-pane]="tab.pane"
          [attr.aria-controls]="'gd-pane-' + tab.pane"
          [attr.aria-selected]="pane() === tab.pane"
          (click)="showPane(tab.pane)"
        >
          {{ tab.label }}
        </button>
      }
    </div>
    <gd-deck-list-dialog />
    <gd-import-dialog />
    <gd-notes-dialog />
    <gd-pack-dialogs />
    <gd-card-info />
    <gd-auto-zoom />
    <gd-notifier />
  `,
})
export class DeckbuilderPageComponent {
  protected readonly tabs: readonly { pane: PagePane; label: string }[] = [
    { pane: 'deck', label: 'Deck' },
    { pane: 'collection', label: 'Collection' },
  ];
  /** The pane chosen in the tab bar, for themes that show one pane at a time. */
  protected readonly pane = signal<PagePane>('deck');
  protected readonly collectionOpen = signal(true);
  /** The collection's width as last dragged to, or null for the theme's default. */
  protected readonly collectionWidth = signal<number | null>(null);
  protected readonly dragging = signal(false);
  /** Where the drag would leave the collection's width, while the separator is being dragged. */
  protected readonly dragWidth = signal<number | null>(null);
  private readonly dragPreview = viewChild<ElementRef<HTMLElement>>('dragPreview');

  constructor() {
    inject(DeckStartup).init();
    inject(PacksService).start();
  }

  /** Choosing the collection's tab also opens the collection, in case the separator closed it. */
  protected showPane(pane: PagePane): void {
    this.pane.set(pane);
    if (pane === 'collection') {
      this.collectionOpen.set(true);
    }
  }

  protected toggleCollection(): void {
    this.collectionOpen.update((open) => !open);
  }

  /** Dragging the separator: a preview of it follows the pointer, and the panes resize on release (as on the legacy page). */
  protected readonly resizeDrag = (event: PointerEvent): DragHandlers | null => {
    if (!this.collectionOpen() || (event.target as Element).closest('.gd-page__toggle')) {
      return null;
    }
    const separator = event.currentTarget as HTMLElement;
    const page = separator.closest('.gd-page') as HTMLElement;
    this.dragging.set(true);
    // Where on the separator it was grabbed stays under the pointer
    const grabbedAt = event.clientX - separator.getBoundingClientRect().left;
    const finish = () => {
      this.dragging.set(false);
      this.dragWidth.set(null);
    };
    return {
      move: (e) => {
        const right = page.getBoundingClientRect().right;
        this.dragWidth.set(Math.round(right - (e.clientX - grabbedAt) - separator.offsetWidth));
      },
      end: () => {
        // The preview is where the theme allowed it: the width kept is what it shows, so the next drag or key press
        // starts from there
        const preview = this.dragPreview()?.nativeElement.getBoundingClientRect();
        if (preview) {
          this.collectionWidth.set(Math.round(page.getBoundingClientRect().right - preview.right));
        }
        finish();
      },
      cancel: finish,
    };
  };

  /** Left makes the collection wider, Right narrower; Enter opens or closes it. */
  protected resizeWithKeys(event: KeyboardEvent): void {
    if (event.target !== event.currentTarget) {
      return;
    }
    if (event.key === 'Enter') {
      event.preventDefault();
      this.toggleCollection();
      return;
    }
    const step = event.key === 'ArrowLeft' ? RESIZE_STEP : event.key === 'ArrowRight' ? -RESIZE_STEP : 0;
    if (step && this.collectionOpen()) {
      event.preventDefault();
      const collection = (event.currentTarget as HTMLElement)
        .closest('.gd-page')
        ?.querySelector<HTMLElement>('.gd-page__collection');
      const current = this.collectionWidth() ?? collection?.getBoundingClientRect().width ?? 0;
      this.collectionWidth.set(Math.round(current + step));
    }
  }
}
