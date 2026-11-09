import { siteImage } from '../../../core/platform/app-paths';
import { DOCUMENT } from '@angular/common';
import { Component, computed, DestroyRef, effect, inject, linkedSignal, signal, untracked } from '@angular/core';
import { stripBlueprintId } from '../../../core/cards/card-blueprint';
import { cardDisplayData } from '../../../core/cards/card-display';
import { CardImageService } from '../../../core/cards/card-image.service';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { zoneOf } from '../../../core/cards/card-ref';
import { AutoZoomSetting } from '../../../core/platform/auto-zoom-setting';
import { CardHover, HoveredCard } from '../../shared/card/card-hover';
import { zoomPlacement } from './zoom-placement';

/** The mouse has to rest this long on a card before its zoom shows (AutoZoom.HoverDelay). */
export const HOVER_DELAY_MS = 500;
/** After leaving a zoomed card, the next card zooms at once for this long (AutoZoom.HoverCooldown). */
export const HOVER_COOLDOWN_MS = 500;

/** Card backs (the face-down blueprints) aren't zoomed. */
const NO_ZOOM = new Set(['-1_1', '-1_2']);

/**
 * The hover zoom (legacy AutoZoom): resting the mouse on a card for half a second shows it large beside it; moving on
 * to another card soon after shows that one at once. Pressing a mouse button hides it; Shift flips a card that has a
 * back. Off when the toolbar's magnifier is off (AutoZoomSetting), on touch devices and while the card information is
 * open. It ignores the mouse.
 *
 * DOM contract: `div.gd-zoom` (`aria-hidden`, `hidden` when nothing is zoomed) with `--gd-zoom-left`, `--gd-zoom-top`,
 * `--gd-zoom-width` and `--gd-zoom-height` (px: where it goes, and its size), `data-orientation`, `data-flipped`
 * (back) and `data-foil`, holding `img.gd-zoom__image`, then `img.gd-zoom__overlay` and `img.gd-zoom__foil` when
 * they apply. The theme positions it (fixed) and draws its frame.
 */
@Component({
  selector: 'gd-auto-zoom',
  host: { style: 'display: contents' },
  template: `
    <div
      class="gd-zoom"
      aria-hidden="true"
      [hidden]="!zoom()"
      [attr.data-orientation]="zoom()?.data?.horizontal ? 'horizontal' : 'vertical'"
      [attr.data-flipped]="flipped() ? 'back' : null"
      [attr.data-foil]="zoom()?.data?.foil ? '' : null"
      [style.--gd-zoom-left.px]="zoom()?.place?.left"
      [style.--gd-zoom-top.px]="zoom()?.place?.top"
      [style.--gd-zoom-width.px]="zoom()?.place?.width"
      [style.--gd-zoom-height.px]="zoom()?.place?.height"
    >
      @if (zoom(); as z) {
        <img class="gd-zoom__image" [src]="flipped() && z.data.back ? z.data.back : z.data.image" alt="" />
        @if (z.data.overlayImage; as overlay) {
          <img class="gd-zoom__overlay" [src]="overlay" alt="" />
        }
        @if (z.data.foil) {
          <img class="gd-zoom__foil" [src]="holoImage" alt="" />
        }
      }
    </div>
  `,
})
export class AutoZoomComponent {
  protected readonly holoImage = siteImage('holo.jpg');
  private readonly setting = inject(AutoZoomSetting);
  private readonly hover = inject(CardHover);
  private readonly images = inject(CardImageService);
  private readonly info = inject(CardInfoService);
  private readonly window = inject(DOCUMENT).defaultView;

  /** The hovered card being shown large. */
  private readonly shown = signal<HoveredCard | null>(null);
  private hoverTimer: ReturnType<typeof setTimeout> | null = null;
  private cooldownTimer: ReturnType<typeof setTimeout> | null = null;
  /** Within the cooldown after a zoom: the next card zooms at once. */
  private hoverValid = false;

  protected readonly zoom = computed(() => {
    const shown = this.shown();
    if (!shown || !this.window) {
      return null;
    }
    const ref = shown.card.ref;
    const image = this.images.resolve(ref.blueprintId, zoneOf(ref));
    if (NO_ZOOM.has(image.bareBlueprint)) {
      return null;
    }
    const overlay = ref.kind === 'race' && ref.overlayBlueprintId ? this.images.imageUrl(ref.overlayBlueprintId) : null;
    const data = cardDisplayData(image, overlay, true, this.images.imageUrl(stripBlueprintId(ref.blueprintId)));
    return {
      data,
      place: zoomPlacement(data.horizontal, shown.box, this.window.innerWidth, this.window.innerHeight),
    };
  });

  /** Shift flips the zoomed card to its back; a newly zoomed card starts face up. */
  protected readonly flipped = linkedSignal({ source: this.shown, computation: () => false });

  constructor() {
    const destroyRef = inject(DestroyRef);
    destroyRef.onDestroy(() => {
      this.abortHoverTimer();
      this.abortCooldownTimer();
    });

    effect(() => {
      const hovered = this.hover.hovered();
      const allowed = this.setting.available && this.setting.enabled() && this.info.card() === null;
      untracked(() => (hovered && allowed ? this.cardEntered(hovered) : this.cardLeft(hovered === null)));
    });

    let presses = this.hover.presses();
    effect(() => {
      const count = this.hover.presses();
      if (count !== presses) {
        presses = count;
        untracked(() => this.pressed());
      }
    });

    // Shift is read from the page while a card is zoomed (the mouse is on a card, which doesn't have the focus)
    effect((onCleanup) => {
      if (!this.zoom()?.data.back || !this.window) {
        return;
      }
      const window = this.window;
      const keyDown = (event: KeyboardEvent) => {
        if (event.key === 'Shift' && !event.repeat) {
          this.flipped.update((flipped) => !flipped);
        }
      };
      window.addEventListener('keydown', keyDown);
      onCleanup(() => window.removeEventListener('keydown', keyDown));
    });
  }

  private cardEntered(hovered: HoveredCard): void {
    this.abortCooldownTimer();
    if (this.hoverValid) {
      this.shown.set(hovered);
    } else {
      this.shown.set(null);
      this.abortHoverTimer();
      this.hoverTimer = setTimeout(() => {
        this.hoverTimer = null;
        this.hoverValid = true;
        this.shown.set(this.hover.hovered());
      }, HOVER_DELAY_MS);
    }
  }

  /** The mouse left the card (or the zoom was turned off, or the card information opened). */
  private cardLeft(mouseLeft: boolean): void {
    this.shown.set(null);
    if (this.hoverTimer) {
      this.abortHoverTimer();
    } else if (mouseLeft && this.hoverValid && !this.cooldownTimer) {
      this.startCooldownTimer();
    }
  }

  private pressed(): void {
    this.abortHoverTimer();
    this.startCooldownTimer();
    this.shown.set(null);
  }

  private abortHoverTimer(): void {
    if (this.hoverTimer) {
      clearTimeout(this.hoverTimer);
    }
    this.hoverTimer = null;
  }

  private startCooldownTimer(): void {
    this.abortCooldownTimer();
    this.cooldownTimer = setTimeout(() => {
      this.cooldownTimer = null;
      this.hoverValid = false;
    }, HOVER_COOLDOWN_MS);
  }

  private abortCooldownTimer(): void {
    if (this.cooldownTimer) {
      clearTimeout(this.cooldownTimer);
    }
    this.cooldownTimer = null;
  }
}
