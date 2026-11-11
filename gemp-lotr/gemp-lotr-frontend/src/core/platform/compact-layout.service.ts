import { DOCUMENT } from '@angular/common';
import { computed, inject, Injectable, signal } from '@angular/core';
import { ThemeService } from './theme.service';

/**
 * Where the modern theme stacks the page (the deck above, the collection below): a portrait screen up to a tablet's
 * width, or any screen too narrow for the collection beside a deck (where the legacy page hid the collection).
 */
export const COMPACT_QUERY = '(max-width: 825px), (orientation: portrait) and (max-width: 1100px)';

/**
 * Whether the page uses its compact layout: the modern theme on a portrait or narrow screen (COMPACT_QUERY). The
 * classic theme never does, as the legacy page didn't. Follows the screen as it turns or the window resizes.
 */
@Injectable({ providedIn: 'root' })
export class CompactLayout {
  private readonly theme = inject(ThemeService).theme;
  private readonly narrow = signal(false);

  readonly active = computed(() => this.theme() === 'modern' && this.narrow());

  constructor() {
    const window = inject(DOCUMENT).defaultView;
    const query = typeof window?.matchMedia === 'function' ? window.matchMedia(COMPACT_QUERY) : null;
    if (query) {
      this.narrow.set(query.matches);
      // Its own list: the only handler; also where a test's stand-in has no addEventListener
      query.onchange = (event) => this.narrow.set(event.matches);
    }
  }
}
