import { DOCUMENT } from '@angular/common';
import { ApplicationRef, Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { DeckStartup } from '../../core/deck/deck-startup.service';
import { provideDeckbuilderState } from '../../core/deckbuilder-state';
import { PacksService } from '../../core/packs/packs.service';
import { CompactLayout } from '../../core/platform/compact-layout.service';
import { readCookie, writeCookie } from '../../core/platform/cookies';
import { AutoZoomComponent } from '../shared/auto-zoom/auto-zoom.component';
import { CardInfoComponent } from '../shared/card-info/card-info.component';
import { CollectionFilterComponent } from './collection/collection-filter.component';
import { CollectionGridComponent } from './collection/collection-grid.component';
import { CollectionMenuComponent } from './collection/collection-menu.component';
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

/** In the compact layout: the deck's share of the page's height (%), at first, at least and at most. */
export const SPLIT_DEFAULT = 50;
export const SPLIT_MIN = 15;
export const SPLIT_MAX = 85;
/** Arrow keys on the resizer move the split by this much (%) in the compact layout. */
export const SPLIT_STEP = 5;
/** Remembers the split on this device: the deck builder's own setting. */
export const SPLIT_COOKIE = 'gempDeckbuilderSplit';

function clampSplit(share: number): number {
  return Math.min(SPLIT_MAX, Math.max(SPLIT_MIN, Math.round(share * 10) / 10));
}

/** The remembered split, or the default when there is none (or it isn't a number). */
export function readSplit(cookies: string): number {
  const share = Number.parseFloat(readCookie(cookies, SPLIT_COOKIE) ?? '');
  return Number.isFinite(share) ? clampSplit(share) : SPLIT_DEFAULT;
}

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
 *
 * In the compact layout (CompactLayout: the modern theme on a portrait or narrow screen) the page has
 * `data-layout=compact`, `data-split` (rows, or columns on a landscape screen) and `--gd-split` (the deck's share,
 * 0 to 1): the deck is above the collection (or left of it), and the separator (`aria-orientation` horizontal for rows,
 * with `aria-valuenow` = the share in %, and no toggle) moves the split by dragging or with the arrow keys. The split is remembered on the device (SPLIT_COOKIE). The
 * collection is always shown, with its bar (`gd-collection-menu`: the menu and the filter's parts) instead of the filter.
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
    CollectionMenuComponent,
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
    '[attr.data-layout]': "compact() ? 'compact' : null",
    '[attr.data-split]': "sideBySide() ? 'columns' : compact() ? 'rows' : null",
    '[style.--gd-split]': 'compact() ? split() / 100 : null',
    '[style.--gd-collection-width]': "collectionWidth() === null ? null : collectionWidth() + 'px'",
    '[style.--gd-drag-width]': "dragWidth() === null ? null : dragWidth() + 'px'",
  },
  template: `
    <section class="gd-page__deck" aria-label="Deck"><gd-deck /></section>
    <div
      class="gd-page__separator"
      role="separator"
      [attr.aria-orientation]="compact() && !sideBySide() ? 'horizontal' : 'vertical'"
      aria-label="Resize the collection"
      [attr.aria-valuenow]="compact() ? split() : null"
      [attr.aria-valuemin]="compact() ? splitMin : null"
      [attr.aria-valuemax]="compact() ? splitMax : null"
      [attr.title]="collectionOpen() || compact() ? 'Resize' : null"
      tabindex="0"
      [attr.data-dragging]="dragging() ? '' : null"
      [gdPointerDrag]="resizeDrag"
      (keydown)="resizeWithKeys($event)"
    >
      @if (!compact()) {
        <button
          type="button"
          class="gd-page__toggle"
          gdNoPressFocus
          [attr.aria-expanded]="collectionOpen()"
          [attr.aria-label]="collectionOpen() ? 'Close the collection' : 'Open the collection'"
          [title]="collectionOpen() ? 'Close' : 'Open'"
          (click)="toggleWithKeys($event)"
        ></button>
      }
    </div>
    @if (dragWidth() !== null) {
      <div #dragPreview class="gd-page__drag-preview" aria-hidden="true" title="Resize"></div>
    }
    <section class="gd-page__collection" aria-label="Collection" [hidden]="!collectionOpen() && !compact()">
      @if (compact()) {
        <gd-collection-menu />
      } @else {
        <gd-collection-filter />
      }
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
  private readonly app = inject(ApplicationRef);
  private readonly document = inject(DOCUMENT);
  protected readonly compact = inject(CompactLayout).active;
  protected readonly sideBySide = inject(CompactLayout).sideBySide;
  /** The compact layout's split: the deck's share of the page's height, in %. */
  protected readonly split = signal(readSplit(this.document.cookie));
  protected readonly splitMin = SPLIT_MIN;
  protected readonly splitMax = SPLIT_MAX;
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
    if (this.compact()) {
      return this.splitDrag(event);
    }
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
    const follow = (e: PointerEvent) => {
      if (!open || (onToggle && !moved && e.clientX === pressedAt)) {
        return;
      }
      moved = true;
      this.dragging.set(true);
      const right = page.getBoundingClientRect().right;
      this.dragWidth.set(Math.round(right - (e.clientX - grabbedAt) - separator.offsetWidth));
    };
    return {
      move: follow,
      end: (e) => {
        if (onToggle && !moved) {
          this.toggleWithPointer(e.timeStamp);
        } else if (moved) {
          // Released where it was last moved to, also when the page wasn't drawn since (a quick flick): the preview is
          // drawn now (with the page's own styles, which a change detection of this component alone leaves out)
          follow(e);
          this.app.tick();
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

  /**
   * The compact layout's separator follows the pointer (up and down, or sideways when the halves are side by side), and
   * the split is remembered when it is let go.
   */
  private splitDrag(event: PointerEvent): DragHandlers {
    const separator = event.currentTarget as HTMLElement;
    const across = this.sideBySide();
    const at = (e: PointerEvent) => (across ? e.clientX : e.clientY);
    const start = (box: DOMRect) => (across ? box.left : box.top);
    // Where on the separator it was grabbed stays under the pointer
    const grabbedAt = at(event) - start(separator.getBoundingClientRect());
    this.dragging.set(true);
    const follow = (e: PointerEvent) => {
      const page = this.host.getBoundingClientRect();
      const room = across ? page.width - separator.offsetWidth : page.height - separator.offsetHeight;
      if (room > 0) {
        this.split.set(clampSplit(((at(e) - grabbedAt - start(page)) / room) * 100));
      }
    };
    const finish = () => {
      this.dragging.set(false);
      this.rememberSplit();
    };
    return {
      move: follow,
      end: (e) => {
        follow(e);
        finish();
      },
      cancel: finish,
    };
  }

  private rememberSplit(): void {
    writeCookie(this.document, SPLIT_COOKIE, String(this.split()));
  }

  /**
   * Left makes the collection wider, Right narrower; Enter opens or closes it. In the compact layout Up makes the deck
   * smaller, Down larger (Left and Right, when the halves are side by side).
   */
  protected resizeWithKeys(event: KeyboardEvent): void {
    if (event.target !== event.currentTarget) {
      return;
    }
    if (this.compact()) {
      const [less, more] = this.sideBySide() ? ['ArrowLeft', 'ArrowRight'] : ['ArrowUp', 'ArrowDown'];
      const step = event.key === less ? -SPLIT_STEP : event.key === more ? SPLIT_STEP : 0;
      if (step) {
        event.preventDefault();
        this.split.update((share) => clampSplit(share + step));
        this.rememberSplit();
      }
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
