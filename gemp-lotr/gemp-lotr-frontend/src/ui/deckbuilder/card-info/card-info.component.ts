import { DOCUMENT } from '@angular/common';
import { afterRenderEffect, Component, computed, ElementRef, inject, linkedSignal, signal, viewChild } from '@angular/core';
import { parseBlueprint } from '../../../core/cards/card-blueprint';
import { cardDisplayData } from '../../../core/cards/card-display';
import { CardImageService } from '../../../core/cards/card-image.service';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { zoneOf } from '../../../core/cards/card-ref';
import { readCookie, writeCookie } from '../../../core/platform/cookies';

/** Shared with the legacy card information dialog (the zoom slider's last value). */
export const ZOOM_COOKIE = 'card-info-dialog-slider-last-value';
export const ZOOM_MIN = 0.4;
export const ZOOM_MAX = 1;
export const ZOOM_STEP = 0.1;
const DEFAULT_ZOOM = 0.5;

/** RTMD meta-site modifiers (sets 90-94) can't be flipped. */
function isMetaSiteModifier(blueprintId: string): boolean {
  const { setNo } = parseBlueprint(blueprintId);
  return setNo >= 90 && setNo <= 94;
}

/**
 * The card information (legacy CardInfoDialog): the card, large, with its wiki link and a size slider (remembered in a
 * cookie shared with the legacy page). A modal native `<dialog>`: clicking the card flips it (to its back, or upside
 * down when it has none); clicking anywhere else, the close button or Escape closes it, unless the size slider was
 * just used.
 *
 * DOM contract: `dialog.gd-card-info` with `--gd-card-info-zoom` (0.4 to 1, the slider) for the theme to size the
 * card with; `.gd-card-info__header` (`.gd-card-info__title`, `button.gd-card-info__close`),
 * `input[type=range].gd-card-info__zoom` (focused when the dialog opens, as the legacy one focused its handle),
 * `.gd-card-info__card` (`data-orientation`, `data-flipped` = back or
 * upside-down, `data-flippable`, `data-foil`) holding `img.gd-card-info__image`, `img.gd-card-info__overlay` and
 * `img.gd-card-info__foil`, then `a.gd-card-info__wiki` when the card has a wiki page.
 */
@Component({
  selector: 'gd-card-info',
  host: { style: 'display: contents' },
  template: `
    <dialog
      #dialog
      class="gd-card-info"
      aria-labelledby="gd-card-info-title"
      [style.--gd-card-info-zoom]="zoom()"
      (close)="info.close()"
      (click)="onClick($event)"
    >
      @if (view(); as v) {
        <header class="gd-card-info__header">
          <h2 id="gd-card-info-title" class="gd-card-info__title">Card Information</h2>
          <button type="button" class="gd-card-info__close" aria-label="Close" (click)="info.close()"></button>
        </header>
        <input
          class="gd-card-info__zoom"
          type="range"
          autofocus
          aria-label="Card size"
          title="Card Size"
          [min]="zoomMin"
          [max]="zoomMax"
          [step]="zoomStep"
          [value]="zoom()"
          (input)="setZoom($event)"
          (pointerdown)="sliding = true"
          (pointerup)="slideStopped()"
          (pointercancel)="slideStopped()"
        />
        <div
          class="gd-card-info__card"
          [attr.data-orientation]="v.data.horizontal ? 'horizontal' : 'vertical'"
          [attr.data-flipped]="flipped() ? (v.data.back ? 'back' : 'upside-down') : null"
          [attr.data-flippable]="v.flippable ? '' : null"
          [attr.data-foil]="v.data.foil ? '' : null"
        >
          <img class="gd-card-info__image" [src]="flipped() && v.data.back ? v.data.back : v.data.image" alt="" />
          @if (v.data.overlayImage; as overlay) {
            <img class="gd-card-info__overlay" [src]="overlay" alt="" />
          }
          @if (v.data.foil) {
            <img class="gd-card-info__foil" src="/gemp-lotr/images/holo.jpg" alt="" />
          }
        </div>
        @if (v.wikiUrl) {
          <a class="gd-card-info__wiki" [href]="v.wikiUrl" target="_blank">Go to Wiki Page</a>
        }
      }
    </dialog>
  `,
})
export class CardInfoComponent {
  protected readonly info = inject(CardInfoService);
  private readonly images = inject(CardImageService);
  private readonly document = inject(DOCUMENT);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  protected readonly zoomMin = ZOOM_MIN;
  protected readonly zoomMax = ZOOM_MAX;
  protected readonly zoomStep = ZOOM_STEP;
  protected readonly zoom = signal(this.readZoom());
  /** The size slider is being used: the click that ends it doesn't close the dialog (CardInfoDialog.mouseUp). */
  protected sliding = false;

  protected readonly view = computed(() => {
    const ref = this.info.card();
    if (!ref) {
      return null;
    }
    const image = this.images.resolve(ref.blueprintId, zoneOf(ref));
    const overlay = ref.kind === 'race' && ref.overlayBlueprintId ? this.images.imageUrl(ref.overlayBlueprintId) : null;
    return {
      data: cardDisplayData(image, overlay),
      wikiUrl: image.hasWiki ? image.wikiUrl : null,
      flippable: !isMetaSiteModifier(image.bareBlueprint),
    };
  });

  /** A newly shown card starts face up. */
  protected readonly flipped = linkedSignal({ source: this.info.card, computation: () => false });

  constructor() {
    // After rendering, so the dialog's content is there when showModal focuses the size slider
    afterRenderEffect(() => {
      const dialog = this.dialog().nativeElement;
      const open = this.view() !== null;
      if (open && !dialog.open) {
        // jsdom has no showModal
        if (typeof dialog.showModal === 'function') {
          dialog.showModal();
        } else {
          dialog.setAttribute('open', '');
        }
      } else if (!open && dialog.open) {
        if (typeof dialog.close === 'function') {
          dialog.close();
        } else {
          dialog.removeAttribute('open');
        }
      }
    });
  }

  protected setZoom(event: Event): void {
    const value = parseFloat((event.target as HTMLInputElement).value);
    if (Number.isFinite(value)) {
      writeCookie(this.document, ZOOM_COOKIE, `${value}`);
      this.zoom.set(value);
    }
  }

  protected slideStopped(): void {
    setTimeout(() => (this.sliding = false), 1);
  }

  protected onClick(event: MouseEvent): void {
    const target = event.target as Element;
    if (target.closest('a, input, .gd-card-info__close')) {
      return;
    }
    const view = this.view();
    if (view?.flippable && target.closest('.gd-card-info__card')) {
      this.flipped.update((flipped) => !flipped);
      return;
    }
    if (!this.sliding) {
      this.info.close();
    }
  }

  private readZoom(): number {
    const value = parseFloat(readCookie(this.document.cookie, ZOOM_COOKIE) ?? '');
    return Number.isFinite(value) ? Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, value)) : DEFAULT_ZOOM;
  }
}
