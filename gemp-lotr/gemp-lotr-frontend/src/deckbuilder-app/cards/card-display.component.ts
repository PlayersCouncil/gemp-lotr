import { Component, computed, input, linkedSignal } from '@angular/core';
import { CardDisplayData } from '../../core/cards/card-display';
import { META_SITE_OVERLAY_HEIGHT } from './card.component';

export { cardDisplayData, type CardDisplayData } from '../../core/cards/card-display';

/** CardDisplay.TargetLong / TargetShort: the size of the remastered card images. */
export const TARGET_LONG = 1039;
export const TARGET_SHORT = 745;
export const TARGET_VERT_RATIO = TARGET_SHORT / TARGET_LONG;
const PIXEL = '/gemp-lotr/images/pixel.png';

export interface CardDisplaySize {
  width: number;
  height: number;
  border: number;
  testBorderWidth: number;
  testBorderHeight: number;
}

/** CardDisplay.resize: the card within the limits (never above the remastered size), and its border. */
export function cardDisplaySize(horizontal: boolean, maxWidth: number, maxHeight: number): CardDisplaySize {
  const maxLong = Math.min(maxWidth, maxHeight, TARGET_LONG);
  const maxShort = maxLong * TARGET_VERT_RATIO;
  const width = Math.floor(horizontal ? Math.min(maxLong, maxWidth) : Math.min(maxShort, maxWidth));
  const height = Math.floor(horizontal ? Math.min(maxShort, maxHeight) : Math.min(maxLong, maxHeight));
  const long = Math.max(width, height);
  return {
    width,
    height,
    border: Math.floor(long / 28),
    testBorderWidth: Math.floor(long / 24),
    testBorderHeight: Math.floor(long / 27.6),
  };
}

/**
 * The legacy CardDisplay: the card image with the meta-site, foil, testing-text and border layers. Clicking it
 * flips the card (the info dialog); `flip()` does the same (Shift over the hover zoom).
 */
@Component({
  selector: 'gemp-card-display',
  host: { style: 'display: contents' },
  template: `
    <div [style]="baseStyle()" (click)="onClick($event)">
      <div class="fullcard" style="position: absolute">
        @if (shown(); as data) {
          <img
            [src]="flipped() && data.back ? data.back : data.image"
            [attr.width]="size().width"
            [attr.height]="size().height"
            [style.transform]="flipped() && !data.back ? 'rotate(180deg)' : 'rotate(0deg)'"
          />
        } @else {
          <img src="" style="transform: rotate(0deg)" />
        }
      </div>
      <div
        class="metaSiteOverlay"
        style="position: absolute; bottom: 0px; overflow: hidden"
        [style.display]="shown()?.overlayImage ? 'block' : 'none'"
        [style.width.px]="shown() ? size().width : null"
        [style.height.px]="shown() ? overlayHeight() : null"
      >
        <img
          [attr.src]="shown()?.overlayImage ?? null"
          style="width: 100%; height: 100%; object-fit: cover; object-position: center bottom"
        />
      </div>
      <div
        class="foilOverlay"
        style="position: absolute"
        [style.display]="shown()?.foil ? 'initial' : 'none'"
        [style.width.px]="shown() ? size().width : null"
        [style.height.px]="shown() ? size().height : null"
      >
        <img [src]="foilImage()" />
      </div>
      <div
        class="testingTextOverlay"
        style="position: absolute"
        [style.display]="shown() ? 'none' : null"
        [style.inset]="shown() ? size().testBorderHeight + 'px ' + size().testBorderWidth + 'px' : null"
      ></div>
      <div
        class="borderOverlay"
        style="position: absolute"
        [style.border-width.px]="shown() ? size().border : 18"
        [style.width.px]="shown() ? size().width - 2 * size().border + 2 : null"
        [style.height.px]="shown() ? size().height - 2 * size().border + 2 : null"
      >
        <img class="actionArea" [src]="pixel" width="100%" height="100%" style="width: 100%; height: 100%" />
      </div>
      <ng-content />
    </div>
  `,
})
export class CardDisplayComponent {
  /** The card, or null for the cleared (hidden) view. */
  readonly data = input<CardDisplayData | null>(null);
  readonly maxWidth = input(0);
  readonly maxHeight = input(0);
  /** Foil layer image (a transparent pixel when foils are shown plain). */
  readonly foilImage = input('/gemp-lotr/images/holo.jpg');
  /** "relative" in the info dialog; the hover zoom places it absolutely with left/top. */
  readonly placement = input<{ left: number; top: number } | null>(null);
  /** Whether a click flips the card (not for RTMD meta-sites). */
  readonly flipOnClick = input(true);

  protected readonly pixel = PIXEL;
  protected readonly shown = this.data;
  /** Showing the back (or, for a card without one, turned upside down). A new card starts face up. */
  protected readonly flipped = linkedSignal({ source: this.data, computation: () => false });

  readonly size = computed(() => {
    const data = this.data();
    return data
      ? cardDisplaySize(data.horizontal, this.maxWidth(), this.maxHeight())
      : { width: 0, height: 0, border: 18, testBorderWidth: 0, testBorderHeight: 0 };
  });

  protected readonly overlayHeight = computed(() => Math.floor((this.size().height * META_SITE_OVERLAY_HEIGHT) / 100));

  protected readonly baseStyle = computed(() => {
    const placement = this.placement();
    const data = this.shown();
    const style: Record<string, string> = { position: placement ? 'absolute' : 'relative' };
    if (!data) {
      style['display'] = 'none';
      return style;
    }
    style['display'] = 'inline-block';
    style['width'] = `${this.size().width}px`;
    style['height'] = `${this.size().height}px`;
    if (placement) {
      style['left'] = `${placement.left}px`;
      style['top'] = `${placement.top}px`;
    }
    return style;
  });

  /** Shows the back; a card without one is turned upside down, unless `onlyWithBack` (Shift over the hover zoom). */
  flip(onlyWithBack = false): void {
    const data = this.data();
    if (data && (data.back || !onlyWithBack)) {
      this.flipped.update((f) => !f);
    }
  }

  protected onClick(event: MouseEvent): void {
    if (this.flipOnClick() && this.data()) {
      this.flip();
      event.stopPropagation();
    }
  }
}
