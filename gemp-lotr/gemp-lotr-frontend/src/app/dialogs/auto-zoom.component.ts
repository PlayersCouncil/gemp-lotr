import { DOCUMENT } from '@angular/common';
import {
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  Injectable,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import {
  CardDisplayComponent,
  CardDisplayData,
  cardDisplayData,
  cardDisplaySize,
  TARGET_LONG,
  TARGET_SHORT,
  TARGET_VERT_RATIO,
} from '../cards/card-display.component';
import { CardRegistry, zoneOf } from '../cards/card-registry';
import { stripBlueprintId } from '../core/cards/card-blueprint';
import { CardImageService } from '../core/cards/card-image.service';
import { foilOverlayImage, readFoilPresentation } from '../core/cards/foil-presentation';
import { readCookie, writeCookie } from '../core/cookies';
import { CardInfoService } from './card-info.component';

export const AUTO_ZOOM_COOKIE = 'autoZoomInDeckbuilder';
/** The mouse has to rest this long on a card before its zoom shows (AutoZoom.HoverDelay). */
export const HOVER_DELAY_MS = 500;
/** After leaving a zoomed card, the next card zooms at once for this long (AutoZoom.HoverCooldown). */
export const HOVER_COOLDOWN_MS = 500;

export interface PreviewState {
  data: CardDisplayData;
  maxWidth: number;
  maxHeight: number;
  left: number;
  top: number;
}

/**
 * Hovering a card shows it large next to it (legacy AutoZoom, toggled with the toolbar's magnifier and remembered in a
 * cookie). Not on touch devices. Pressing a mouse button hides it; Shift flips the zoomed card.
 */
@Injectable({ providedIn: 'root' })
export class AutoZoomService {
  private readonly document = inject(DOCUMENT);
  private readonly window = this.document.defaultView!;
  private readonly registry = inject(CardRegistry);
  private readonly images = inject(CardImageService);
  private readonly cardInfo = inject(CardInfoService);
  private readonly destroyRef = inject(DestroyRef);

  readonly touchDevice = 'ontouchstart' in this.document.documentElement;
  /** An unset cookie means on. */
  readonly enabled = signal(readCookie(this.document.cookie, AUTO_ZOOM_COOKIE) !== 'false');
  readonly preview = signal<PreviewState | null>(null);
  /** Shift over a zoomed card (handled by the preview component). */
  readonly flipRequests = signal(0);

  private hoverTimer: ReturnType<typeof setTimeout> | null = null;
  private cooldownTimer: ReturnType<typeof setTimeout> | null = null;
  private hoverValid = false;

  toggle(): void {
    this.enabled.update((on) => !on);
    writeCookie(this.document, AUTO_ZOOM_COOKIE, `${this.enabled()}`);
  }

  /** Starts listening on the page (once, from the app). */
  listen(): void {
    const body = this.document.body;
    const over = (e: MouseEvent) => this.mouseOver(e);
    const out = () => this.mouseOut();
    const down = () => this.mouseDown();
    const key = (e: KeyboardEvent) => this.keyDown(e);
    body.addEventListener('mouseover', over);
    body.addEventListener('mouseout', out);
    body.addEventListener('mousedown', down);
    body.addEventListener('keydown', key);
    this.destroyRef.onDestroy(() => {
      body.removeEventListener('mouseover', over);
      body.removeEventListener('mouseout', out);
      body.removeEventListener('mousedown', down);
      body.removeEventListener('keydown', key);
      this.abortHoverTimer();
      this.abortCooldownTimer();
    });
  }

  private mouseOver(event: MouseEvent): void {
    const target = event.target as Element | null;
    const onCard = !!target?.classList.contains('actionArea');
    if (this.touchDevice || !this.enabled() || !onCard || this.cardInfo.isOpen()) {
      this.hide();
      return;
    }
    this.abortCooldownTimer();
    if (this.hoverValid) {
      this.show(target!);
    } else if (!this.hoverTimer) {
      this.hoverTimer = setTimeout(() => {
        this.hoverTimer = null;
        this.hoverValid = true;
        this.show(target!);
      }, HOVER_DELAY_MS);
    }
  }

  private mouseOut(): void {
    if (this.hoverTimer) {
      this.abortHoverTimer();
    } else if (this.hoverValid && !this.cooldownTimer) {
      this.startCooldownTimer();
    }
  }

  private mouseDown(): void {
    this.abortHoverTimer();
    this.startCooldownTimer();
    this.hide();
  }

  private keyDown(event: KeyboardEvent): void {
    if (!event.repeat && this.enabled() && !this.touchDevice && event.key === 'Shift' && this.preview()) {
      this.flipRequests.update((n) => n + 1);
    }
  }

  private show(target: Element): void {
    const cardElement = target.closest('.card');
    const ref = this.registry.get(cardElement);
    if (!ref || !cardElement) {
      return;
    }
    const image = this.images.resolve(ref.blueprintId, zoneOf(ref));
    if (image.bareBlueprint === '-1_1' || image.bareBlueprint === '-1_2') {
      return;
    }
    const overlay = ref.kind === 'race' && ref.overlayBlueprintId ? this.images.imageUrl(ref.overlayBlueprintId) : null;
    const data = cardDisplayData(image, overlay, true, this.images.imageUrl(stripBlueprintId(ref.blueprintId)));
    this.preview.set(
      previewPlacement(data, cardElement.getBoundingClientRect(), this.window.innerWidth, this.window.innerHeight),
    );
  }

  private hide(): void {
    this.preview.set(null);
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

/**
 * AutoZoom.displayPreviewImage: the zoomed card's size (as tall as 90% of the window allows, up to the remastered
 * size) and where it goes: right of the hovered card, else left of it, vertically centred on it within the window.
 */
export function previewPlacement(
  data: CardDisplayData,
  cardRect: { left: number; top: number; right: number; bottom: number },
  windowWidth: number,
  windowHeight: number,
): PreviewState {
  const maxLongSide = (windowWidth / windowHeight <= 1 ? windowWidth : windowHeight) * 0.9;
  const targetLong = Math.min(maxLongSide, TARGET_LONG);
  const targetShort = Math.min(maxLongSide * TARGET_VERT_RATIO, TARGET_SHORT);
  // The legacy code passed (width, height) limits in this order
  const maxWidth = data.horizontal ? targetLong : targetShort;
  const maxHeight = data.horizontal ? targetShort : targetLong;
  const size = cardDisplaySize(data.horizontal, maxWidth, maxHeight);

  const srcWidth = cardRect.right - cardRect.left;
  const srcHeight = cardRect.bottom - cardRect.top;
  let previewWidth = size.width;
  let previewHeight = size.height;
  const ratio = previewWidth / previewHeight;
  const rightEdge = cardRect.left + srcWidth - 15;
  const leftEdge = cardRect.left;
  const pastRight = rightEdge + previewWidth > windowWidth;
  const pastLeft = leftEdge - previewWidth < 0;
  let left = rightEdge;
  if (pastRight && pastLeft) {
    // Neither side has room: use the larger one (the legacy code only used the smaller size for placing it)
    const rightSpace = windowWidth - (leftEdge + srcWidth);
    const leftSpace = leftEdge;
    if (rightSpace > leftSpace) {
      previewWidth = rightSpace;
      left = rightEdge;
    } else {
      previewWidth = leftSpace;
      left = leftEdge - previewWidth;
    }
    previewHeight = previewWidth / ratio;
  } else if (pastRight) {
    left = leftEdge - previewWidth;
  }
  let top = cardRect.top + srcHeight / 2 - previewHeight / 2;
  if (top + previewHeight + 15 > windowHeight) {
    top = windowHeight - previewHeight - 15;
  } else if (top < 0) {
    top = 0;
  }
  return { data, maxWidth, maxHeight, left, top };
}

/** The zoomed card (#previewImage), above everything and ignoring the mouse. */
@Component({
  selector: 'gemp-auto-zoom',
  imports: [CardDisplayComponent],
  host: { style: 'display: contents' },
  template: `
    <div id="previewImage" class="previewImage" style="">
      <gemp-card-display
        [data]="zoom.preview()?.data ?? null"
        [maxWidth]="zoom.preview()?.maxWidth ?? 0"
        [maxHeight]="zoom.preview()?.maxHeight ?? 0"
        [foilImage]="foilImage"
        [placement]="placement()"
        [flipOnClick]="false"
        ><div id="auto-zoom-message" style="display: none"></div
      ></gemp-card-display>
    </div>
  `,
})
export class AutoZoomComponent {
  protected readonly zoom = inject(AutoZoomService);
  private readonly display = viewChild.required(CardDisplayComponent);
  /** CardDisplay.GetFoilImage: a transparent pixel when foils are shown plain. */
  protected readonly foilImage =
    foilOverlayImage(readFoilPresentation(inject(DOCUMENT).cookie)) ?? '/gemp-lotr/images/pixel.png';
  protected readonly placement = computed(() => {
    const preview = this.zoom.preview();
    return preview ? { left: preview.left, top: preview.top } : { left: 0, top: 0 };
  });

  constructor() {
    // Shift flips a zoomed card that has a back
    let handled = this.zoom.flipRequests();
    effect(() => {
      const requests = this.zoom.flipRequests();
      if (requests !== handled) {
        handled = requests;
        untracked(() => this.display().flip(true));
      }
    });
  }
}
