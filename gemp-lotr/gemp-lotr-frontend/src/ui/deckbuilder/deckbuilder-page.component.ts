import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { DeckStartup } from '../../core/deck/deck-startup.service';
import { provideDeckbuilderState } from '../../core/deckbuilder-state';
import { PacksService } from '../../core/packs/packs.service';
import { AutoZoomComponent } from '../shared/auto-zoom/auto-zoom.component';
import { CardInfoComponent } from '../shared/card-info/card-info.component';
import { CollectionFilterComponent } from './collection/collection-filter.component';
import { CollectionGridComponent } from './collection/collection-grid.component';
import { DeckAreaComponent } from './deck/deck-area.component';
import { DeckListDialogComponent } from './dialogs/deck-list-dialog.component';
import { ImportDialogComponent } from './dialogs/import-dialog.component';
import { NotesDialogComponent } from './dialogs/notes-dialog.component';
import { PackDialogsComponent } from './dialogs/pack-dialogs.component';
import { NotifierDialogComponent } from '../shared/notifier/notifier-dialog.component';
import { NoPressFocusDirective } from '../shared/no-press-focus/no-press-focus.directive';
import { DragHandlers, PointerDragDirective } from '../shared/pointer-drag/pointer-drag.directive';

/** Arrow keys on the resizer move it by this much (px). */
export const RESIZE_STEP = 10;

/** How long the legacy pane took to slide open or closed (jQuery's "normal" speed). */
const TOGGLE_SLIDE_MS = 400;

/**
 * The deck builder page frame: the deck (left), the collection (right) and the separator between them, which resizes
 * the collection by dragging or with the arrow keys and closes or opens it with its toggle. The page only records the
 * width asked for; its styles keep it within the limits.
 *
 * DOM contract: `.gd-page` with `data-collection` (open, closed) and `--gd-collection-width` (px, once resized);
 * `section.gd-page__deck`, `div.gd-page__separator` (`role=separator`, `data-dragging` while dragged) holding
 * `button.gd-page__toggle` (`aria-expanded`), `section.gd-page__collection` holding the filter and the cards; then the dialogs (deck list and library, import, notes, selection pack, new items), the card information, the hover zoom
 * and the notifier's dialog.
 */
@Component({
  selector: 'deckbuilder-app',
  styleUrl: './deckbuilder-page.component.css',
  providers: provideDeckbuilderState(),
  imports: [
    PointerDragDirective,
    NoPressFocusDirective,
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
    '[style.--gd-collection-width]': "collectionWidth() === null ? null : collectionWidth() + 'px'",
    '[style.--gd-drag-width]': "dragWidth() === null ? null : dragWidth() + 'px'",
  },
  template: `
    <section class="gd-page__deck" aria-label="Deck"><gd-deck /></section>
    <div
      class="gd-page__separator"
      role="separator"
      aria-orientation="vertical"
      aria-label="Resize the collection"
      [attr.title]="collectionOpen() ? 'Resize' : null"
      tabindex="0"
      [attr.data-dragging]="dragging() ? '' : null"
      [gdPointerDrag]="resizeDrag"
      (keydown)="resizeWithKeys($event)"
    >
      <button
        type="button"
        class="gd-page__toggle"
        gdNoPressFocus
        [attr.aria-expanded]="collectionOpen()"
        [attr.aria-label]="collectionOpen() ? 'Close the collection' : 'Open the collection'"
        [title]="collectionOpen() ? 'Close' : 'Open'"
        (click)="toggleWithKeys($event)"
      ></button>
    </div>
    @if (dragWidth() !== null) {
      <div #dragPreview class="gd-page__drag-preview" aria-hidden="true" title="Resize"></div>
    }
    <section class="gd-page__collection" aria-label="Collection" [hidden]="!collectionOpen()">
      <gd-collection-filter />
      <gd-collection-grid />
    </section>
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
  protected readonly collectionOpen = signal(true);
  /** The collection's width as last dragged to, or null for the default (its styles'). */
  protected readonly collectionWidth = signal<number | null>(null);
  protected readonly dragging = signal(false);
  /** Where the drag would leave the collection's width, while the separator is being dragged. */
  protected readonly dragWidth = signal<number | null>(null);
  private readonly dragPreview = viewChild<ElementRef<HTMLElement>>('dragPreview');
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
  private lastPointerToggle = -Infinity;

  constructor() {
    inject(DeckStartup).init();
    inject(PacksService).start();
  }

  protected toggleCollection(): void {
    this.collectionOpen.update((open) => !open);
  }

  /** Enter or Space on the toggle: a press of the mouse or a finger is handled with the drag (resizeDrag). */
  protected toggleWithKeys(event: MouseEvent): void {
    if (event.detail === 0) {
      this.toggleCollection();
    }
  }

  /** The legacy pane was still sliding for this long, and ignored a second click (the second of a double click). */
  private toggleWithPointer(time: number): void {
    if (time - this.lastPointerToggle >= TOGGLE_SLIDE_MS) {
      this.lastPointerToggle = time;
      this.toggleCollection();
    }
  }

  /**
   * Dragging the separator: a preview of it follows the pointer, and the panes resize on release (as on the legacy
   * page). It can also be dragged by its toggle, which is pressed like a button if the pointer doesn't move.
   */
  protected readonly resizeDrag = (event: PointerEvent): DragHandlers | null => {
    const onToggle = (event.target as Element).closest('.gd-page__toggle') !== null;
    const open = this.collectionOpen();
    if (!open && !onToggle) {
      return null;
    }
    const separator = event.currentTarget as HTMLElement;
    const page = this.host;
    const pressedAt = event.clientX;
    let moved = false;
    if (!onToggle) {
      this.dragging.set(true);
    }
    // Where on the separator it was grabbed stays under the pointer
    const grabbedAt = event.clientX - separator.getBoundingClientRect().left;
    const finish = () => {
      this.dragging.set(false);
      this.dragWidth.set(null);
    };
    return {
      move: (e) => {
        if (!open || (onToggle && !moved && e.clientX === pressedAt)) {
          return;
        }
        moved = true;
        this.dragging.set(true);
        const right = page.getBoundingClientRect().right;
        this.dragWidth.set(Math.round(right - (e.clientX - grabbedAt) - separator.offsetWidth));
      },
      end: (e) => {
        if (onToggle && !moved) {
          this.toggleWithPointer(e.timeStamp);
        }
        // The preview is where the styles allowed it: the width kept is what it shows, so the next drag or key press
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
