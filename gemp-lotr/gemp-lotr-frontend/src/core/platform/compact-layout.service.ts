import { DOCUMENT } from '@angular/common';
import { computed, inject, Injectable, signal, WritableSignal } from '@angular/core';
import { ThemeService } from './theme.service';

/**
 * Where the modern theme uses its compact layout: a portrait screen up to a tablet's width, any screen too narrow for
 * the collection beside a deck (where the legacy page hid the collection), or too low for the desktop layout (a phone
 * held sideways).
 */
export const COMPACT_QUERY = '(max-width: 825px), (orientation: portrait) and (max-width: 1100px), (max-height: 500px)';

/** Where the compact layout puts the deck and the collection side by side rather than one above the other. */
export const SIDE_BY_SIDE_QUERY = '(orientation: landscape)';

/** A screen at least a small tablet's width (the game's phone layout gives a tablet bigger cards). */
export const TABLET_QUERY = '(min-width: 600px)';

/**
 * Whether the page uses its compact layout: the modern theme on a portrait, narrow or low screen (COMPACT_QUERY), and
 * whether it puts the halves side by side (a landscape screen). The classic theme never does, as the legacy page
 * didn't. Follows the screen as it turns or the window resizes.
 */
@Injectable({ providedIn: 'root' })
export class CompactLayout {
  private readonly theme = inject(ThemeService).theme;
  private readonly narrow = signal(false);
  private readonly landscape = signal(false);
  private readonly wide = signal(false);

  readonly active = computed(() => this.theme() === 'modern' && this.narrow());
  /** The compact layout on a landscape screen: the deck left, the collection right. */
  readonly sideBySide = computed(() => this.active() && this.landscape());
  /**
   * What the compact layout is shown on: a phone held upright, a phone held sideways (any landscape screen), or a
   * tablet held upright; null outside the compact layout.
   */
  readonly device = computed<'phone' | 'sideways' | 'tablet' | null>(() =>
    !this.active() ? null : this.landscape() ? 'sideways' : this.wide() ? 'tablet' : 'phone',
  );

  constructor() {
    const window = inject(DOCUMENT).defaultView;
    const follow = (media: string, state: WritableSignal<boolean>) => {
      const query = typeof window?.matchMedia === 'function' ? window.matchMedia(media) : null;
      if (query) {
        state.set(query.matches);
        // Its own list: the only handler; also where a test's stand-in has no addEventListener
        query.onchange = (event) => state.set(event.matches);
      }
    };
    follow(COMPACT_QUERY, this.narrow);
    follow(SIDE_BY_SIDE_QUERY, this.landscape);
    follow(TABLET_QUERY, this.wide);
  }
}
