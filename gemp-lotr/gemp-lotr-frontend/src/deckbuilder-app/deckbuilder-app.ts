import { Component, computed, inject, signal } from '@angular/core';
import { CardFilterComponent } from './collection/card-filter.component';
import { CollectionDisplayComponent } from './collection/collection-display.component';
import { DeckService } from './core/deck/deck.service';
import { CardClicksService } from './deck/card-clicks.service';
import { DeckAreaComponent } from './deck/deck-area.component';
import { AutoZoomComponent, AutoZoomService } from './dialogs/auto-zoom.component';
import { CardInfoComponent } from './dialogs/card-info.component';
import { DeckDialogsComponent } from './dialogs/deck-dialogs.component';
import { PackDialogsComponent, PacksService } from './dialogs/pack-dialogs.component';
import { eastWidthForResizerAt, PANE_BORDER, PANE_PADDING, RESIZER_SIZE, TOGGLER_LENGTH } from './layout/page-layout';
import { PageLayoutService } from './layout/page-layout.service';
import { rectStyle } from './layout/rect';

/** Inline styles jquery.layout's default styles put on both panes (plus the black background deckBuilder.js set). */
const PANE_STYLE = {
  position: 'absolute',
  margin: '0px',
  'z-index': '1',
  padding: `${PANE_PADDING}px`,
  background: 'rgb(0, 0, 0)',
  border: `${PANE_BORDER}px solid rgb(187, 187, 187)`,
  overflow: 'auto',
  display: 'block',
  visibility: 'visible',
};

/**
 * The deck builder page: the deck pane (center), the collection pane (east) and the resizer bar between them, laid out
 * like the legacy page's jquery.layout.
 */
@Component({
  selector: 'deckbuilder-app',
  imports: [
    DeckAreaComponent,
    CardFilterComponent,
    CollectionDisplayComponent,
    CardInfoComponent,
    DeckDialogsComponent,
    PackDialogsComponent,
    AutoZoomComponent,
  ],
  templateUrl: './deckbuilder-app.html',
  host: { style: 'display: contents' },
})
export class DeckbuilderApp {
  protected readonly layout = inject(PageLayoutService);
  protected readonly rectStyle = rectStyle;

  /** Left edge of the resizer copy that follows the mouse while dragging; null when not dragging. */
  protected readonly dragLeft = signal<number | null>(null);

  constructor() {
    inject(DeckService).init();
    inject(CardClicksService).listen();
    inject(AutoZoomService).listen();
    inject(PacksService).start();
  }

  protected readonly centerStyle = computed(() => {
    const page = this.layout.page();
    return {
      ...PANE_STYLE,
      inset: `0px ${page.centerRight}px 0px 0px`,
      width: `${page.centerContentWidth}px`,
      height: `${page.centerContentHeight}px`,
    };
  });

  protected readonly eastStyle = computed(() => {
    const page = this.layout.page();
    return {
      ...PANE_STYLE,
      inset: '0px 0px 0px auto',
      width: `${page.eastContentWidth}px`,
      height: `${page.eastContentHeight}px`,
      display: this.layout.page().eastHidden ? 'none' : 'block',
    };
  });

  protected readonly resizerStyle = computed(() => {
    const page = this.layout.page();
    return {
      ...RESIZER_BASE_STYLE,
      cursor: this.layout.page().eastHidden ? 'pointer' : 'e-resize',
      right: `${page.resizerRight}px`,
      height: `${page.resizerHeight}px`,
      width: `${RESIZER_SIZE}px`,
      top: '0px',
    };
  });

  protected readonly togglerStyle = computed(() => ({
    ...TOGGLER_BASE_STYLE,
    height: `${TOGGLER_LENGTH}px`,
    width: `${RESIZER_SIZE}px`,
    top: `${this.layout.page().togglerTop}px`,
    left: '0px',
  }));

  protected readonly dragHelperStyle = computed(() => ({
    ...RESIZER_BASE_STYLE,
    cursor: 'e-resize',
    'z-index': '10000',
    left: `${this.dragLeft()}px`,
    height: `${this.layout.page().resizerHeight}px`,
    width: `${RESIZER_SIZE}px`,
    top: '0px',
  }));

  /** Dragging the open resizer bar (not its toggler) shows a copy following the mouse; the pane resizes on release. */
  protected startResize(event: MouseEvent): void {
    if (
      this.layout.page().eastHidden ||
      event.button !== 0 ||
      (event.target as Element).closest('.ui-layout-toggler')
    ) {
      return;
    }
    event.preventDefault();
    const viewportWidth = this.layout.viewportWidth();
    const offset = event.clientX - (viewportWidth - this.layout.page().resizerRight - RESIZER_SIZE);
    // Keep the copy within the pane's size limits, as jquery.layout did
    const clampLeft = (left: number) => viewportWidth - RESIZER_SIZE - eastWidthForResizerAt(left, viewportWidth);
    const move = (e: MouseEvent) => this.dragLeft.set(clampLeft(e.clientX - offset));
    const stop = (e: MouseEvent) => {
      document.removeEventListener('mousemove', move);
      document.removeEventListener('mouseup', stop);
      if (this.dragLeft() !== null) {
        this.layout.eastWidth.set(eastWidthForResizerAt(clampLeft(e.clientX - offset), viewportWidth));
      }
      this.dragLeft.set(null);
    };
    document.addEventListener('mousemove', move);
    document.addEventListener('mouseup', stop);
  }

  /** The toggler closes/opens the collection pane; clicking the closed bar opens it again. */
  protected onResizerClick(event: MouseEvent): void {
    if ((event.target as Element).closest('.ui-layout-toggler') || this.layout.page().eastHidden) {
      this.layout.toggleEast();
    }
  }
}

const RESIZER_BASE_STYLE = {
  position: 'absolute',
  padding: '0px',
  margin: '0px',
  'font-size': '1px',
  'text-align': 'left',
  overflow: 'hidden',
  'z-index': '2',
  background: 'rgb(221, 221, 221)',
  border: 'none',
};

const TOGGLER_BASE_STYLE = {
  position: 'absolute',
  display: 'block',
  padding: '0px',
  margin: '0px',
  overflow: 'hidden',
  'text-align': 'center',
  'font-size': '1px',
  cursor: 'pointer',
  'z-index': '1',
  background: 'rgb(170, 170, 170)',
  visibility: 'visible',
};
