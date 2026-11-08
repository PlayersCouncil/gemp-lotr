import { Component, inject, signal } from '@angular/core';
import { DeckService } from '../../core/deck/deck.service';
import { PacksService } from '../../core/packs/packs.service';
import { AutoZoomComponent } from './auto-zoom/auto-zoom.component';
import { CardInfoComponent } from './card-info/card-info.component';
import { CollectionFilterComponent } from './collection/collection-filter.component';
import { CollectionGridComponent } from './collection/collection-grid.component';
import { DeckAreaComponent } from './deck/deck-area.component';

/** Arrow keys on the resizer move it by this much (px). */
export const RESIZE_STEP = 10;

/**
 * The deck builder page frame: the deck (left), the collection (right) and the separator between them, which resizes
 * the collection by dragging or with the arrow keys and closes or opens it with its toggle. The page only records the width asked for; the theme keeps it within its limits.
 *
 * DOM contract: `.gd-page` with `data-collection` (open, closed) and `--gd-collection-width` (px, once resized);
 * `section.gd-page__deck`, `div.gd-page__separator` (`role=separator`, `data-dragging` while dragged) holding
 * `button.gd-page__toggle` (`aria-expanded`), and `section.gd-page__collection` holding the filter and the cards; then
 * the card information dialog and the hover zoom.
 */
@Component({
  selector: 'deckbuilder-app',
  imports: [DeckAreaComponent, CollectionFilterComponent, CollectionGridComponent, CardInfoComponent, AutoZoomComponent],
  host: {
    class: 'gd-page',
    '[attr.data-collection]': "collectionOpen() ? 'open' : 'closed'",
    '[style.--gd-collection-width]': "collectionWidth() === null ? null : collectionWidth() + 'px'",
  },
  template: `
    <section class="gd-page__deck" aria-label="Deck"><gd-deck /></section>
    <div
      class="gd-page__separator"
      role="separator"
      aria-orientation="vertical"
      aria-label="Resize the collection"
      tabindex="0"
      [attr.data-dragging]="dragging() ? '' : null"
      (pointerdown)="startResize($event)"
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
    <section class="gd-page__collection" aria-label="Collection" [hidden]="!collectionOpen()">
      <gd-collection-filter />
      <gd-collection-grid />
    </section>
    <gd-card-info />
    <gd-auto-zoom />
  `,
})
export class DeckbuilderPageComponent {
  protected readonly collectionOpen = signal(true);
  /** The collection's width as last dragged to, or null for the theme's default. */
  protected readonly collectionWidth = signal<number | null>(null);
  protected readonly dragging = signal(false);

  constructor() {
    inject(DeckService).init();
    inject(PacksService).start();
  }

  protected toggleCollection(): void {
    this.collectionOpen.update((open) => !open);
  }

  /** Dragging the separator: the collection's left edge follows the pointer. */
  protected startResize(event: PointerEvent): void {
    if (!this.collectionOpen() || event.button !== 0 || (event.target as Element).closest('.gd-page__toggle')) {
      return;
    }
    event.preventDefault();
    const separator = event.currentTarget as HTMLElement;
    const page = separator.closest('.gd-page') as HTMLElement;
    separator.setPointerCapture?.(event.pointerId);
    this.dragging.set(true);
    const move = (e: PointerEvent) => {
      const right = page.getBoundingClientRect().right;
      this.collectionWidth.set(Math.round(right - e.clientX - separator.offsetWidth / 2));
    };
    const stop = () => {
      separator.removeEventListener('pointermove', move);
      separator.removeEventListener('pointerup', stop);
      separator.removeEventListener('pointercancel', stop);
      this.dragging.set(false);
      // Keep what the theme allowed, so the next drag or key press starts from what is shown
      const shown = page.querySelector<HTMLElement>('.gd-page__collection')?.getBoundingClientRect().width;
      if (shown) {
        this.collectionWidth.set(Math.round(shown));
      }
    };
    separator.addEventListener('pointermove', move);
    separator.addEventListener('pointerup', stop);
    separator.addEventListener('pointercancel', stop);
  }

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
