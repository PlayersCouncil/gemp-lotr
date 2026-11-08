import { DOCUMENT } from '@angular/common';
import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { DeckService } from '../../core/deck/deck.service';
import { computeDeckLayout, DeckLayout } from './deck-layout';
import { computePageLayout, EAST_MIN_WIDTH, hasRoomForEast, PageLayout } from './page-layout';

/**
 * Sizes of the page frame and the deck pane, recomputed when the window resizes, the resizer is dragged or the
 * collection pane is closed (what jquery.layout's onresize -> layoutUI did in the legacy page).
 */
@Injectable({ providedIn: 'root' })
export class PageLayoutService {
  private readonly window = inject(DOCUMENT).defaultView!;

  readonly viewportWidth = signal(this.window.innerWidth);
  readonly viewportHeight = signal(this.window.innerHeight);
  /** Outer width of the collection pane, as last set by the user (clamped when used). */
  readonly eastWidth = signal(EAST_MIN_WIDTH);
  readonly eastClosed = signal(false);
  private readonly deckService = inject(DeckService);
  /** Whether the Map and Race slots are shown: the deck's format decides. */
  readonly showMap = computed(() => this.deckService.usesMap());
  readonly showRace = computed(() => this.deckService.usesRace());

  readonly page = computed<PageLayout>(() =>
    computePageLayout(this.viewportWidth(), this.viewportHeight(), this.eastWidth(), this.eastClosed()),
  );

  readonly deck = computed<DeckLayout>(() => {
    const page = this.page();
    return computeDeckLayout(page.centerContentWidth, page.centerContentHeight, this.showMap(), this.showRace());
  });

  constructor() {
    const onResize = () => {
      this.viewportWidth.set(this.window.innerWidth);
      this.viewportHeight.set(this.window.innerHeight);
    };
    this.window.addEventListener('resize', onResize);
    inject(DestroyRef).onDestroy(() => this.window.removeEventListener('resize', onResize));
  }

  /** Closes or reopens the collection pane (nothing happens when the window has no room for it). */
  toggleEast(): void {
    if (hasRoomForEast(this.viewportWidth())) {
      this.eastClosed.update((closed) => !closed);
    }
  }
}
