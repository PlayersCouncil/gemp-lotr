import { DOCUMENT } from '@angular/common';
import {
  afterRenderEffect,
  Component,
  computed,
  ElementRef,
  inject,
  linkedSignal,
  signal,
  viewChild,
} from '@angular/core';
import { parseBlueprint } from '../../../core/cards/card-blueprint';
import { cardDisplayData } from '../../../core/cards/card-display';
import { CardImageService } from '../../../core/cards/card-image.service';
import { CardInfoService } from '../../../core/cards/card-info.service';
import { siteImage } from '../../../core/platform/app-paths';
import { ThemeService } from '../../../core/platform/theme.service';
import { readCookie, writeCookie } from '../../../core/platform/cookies';
import { cardElementOf } from '../card/card.component';
import { DocumentPressDirective } from '../document-press/document-press.directive';

/** Shared with the legacy card information dialog (the zoom slider's last value). */
export const ZOOM_COOKIE = 'card-info-dialog-slider-last-value';
const ZOOM_MIN = 0.4;
const ZOOM_MAX = 1;
const ZOOM_STEP = 0.1;
const DEFAULT_ZOOM = 0.5;

/** RTMD meta-site modifiers (sets 90-94) can't be flipped. */
function isMetaSiteModifier(blueprintId: string): boolean {
  const { setNo } = parseBlueprint(blueprintId);
  return setNo >= 90 && setNo <= 94;
}

/**
 * The card information: the card, large, with its wiki link and a size slider (remembered in a
 * cookie shared with the legacy page). A native `<dialog>`: clicking the card flips it (to its back, or upside down
 * when it has none); clicking anywhere else, the close button or Escape closes it, unless the size slider was just
 * used. A click or right-click on a card behind it only closes it (other controls still work).
 *
 * Modal if the theme says so (ThemeBehaviour), and always over another modal dialog (a card in the selection dialog),
 * which would otherwise cover it.
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
  styleUrl: './card-info.component.css',
  imports: [DocumentPressDirective],
  host: { style: 'display: contents' },
  template: `
    <!-- eslint-disable-next-line @angular-eslint/template/interactive-supports-focus -- a <dialog> takes the focus -->
    <dialog
      #dialog
      class="gd-card-info"
      aria-labelledby="gd-card-info-title"
      [style.--gd-card-info-zoom]="zoom()"
      (close)="info.close()"
      (keydown.escape)="escape($event)"
      (gdDocumentPress)="documentPress($event)"
      (mousedown)="keepFocus($event)"
      (click)="onClick($event)"
    >
      @if (view(); as v) {
        <header class="gd-card-info__header">
          <h2 id="gd-card-info-title" class="gd-card-info__title">Card Information</h2>
          <button type="button" class="gd-card-info__close" aria-label="Close" (click)="info.close()"></button>
        </header>
        <input
          class="gd-card-info__zoom gd-slider"
          type="range"
          autofocus
          aria-label="Card size"
          title="Card Size"
          [min]="zoomMin"
          [max]="zoomMax"
          [step]="zoomStep"
          [value]="zoom()"
          (input)="setZoom($event)"
          (pointerdown)="slideStarted($event)"
          (pointermove)="slideMoved($event)"
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
            <img class="gd-card-info__foil" [src]="holoImage" alt="" />
          }
          <span class="gd-card-info__flip"></span>
        </div>
        @if (v.wikiUrl) {
          <a class="gd-card-info__wiki" [href]="v.wikiUrl" target="_blank" rel="noopener noreferrer">Go to Wiki Page</a>
        }
      }
    </dialog>
  `,
})
export class CardInfoComponent {
  protected readonly info = inject(CardInfoService);
  private readonly images = inject(CardImageService);
  private readonly theme = inject(ThemeService);
  private readonly document = inject(DOCUMENT);
  private readonly dialog = viewChild.required<ElementRef<HTMLDialogElement>>('dialog');

  protected readonly holoImage = siteImage('holo.jpg');
  protected readonly zoomMin = ZOOM_MIN;
  protected readonly zoomMax = ZOOM_MAX;
  protected readonly zoomStep = ZOOM_STEP;
  protected readonly zoom = signal(this.readZoom());
  /** The size slider is being used: the click that ends it doesn't close the dialog (CardInfoDialog.mouseUp). */
  protected sliding = false;
  /** Where the slider was pressed, and the value the press gave it, until the pointer moves. */
  private press: { x: number; value: number | null } | null = null;

  protected readonly view = computed(() => {
    const card = this.info.card();
    if (!card) {
      return null;
    }
    const image = this.images.resolve(card.blueprintId, card.zone);
    return {
      data: cardDisplayData(image, card.overlayImage),
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
        // jsdom has no showModal/show
        if (typeof dialog.showModal !== 'function') {
          dialog.setAttribute('open', '');
        } else if (this.opensModal()) {
          dialog.showModal();
        } else {
          dialog.show();
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
    const input = event.target as HTMLInputElement;
    const value = parseFloat(input.value);
    if (this.press) {
      // The press resized the card, so the dialog moved under the pointer; Firefox then reads the value again where
      // the pointer is on the moved slider. Chromium, and the legacy slider, keep the press's value until it moves.
      if (this.press.value !== null && value !== this.press.value) {
        input.value = `${this.press.value}`;
        return;
      }
      this.press.value = value;
    }
    if (Number.isFinite(value)) {
      writeCookie(this.document, ZOOM_COOKIE, `${value}`);
      this.zoom.set(value);
    }
  }

  protected slideStarted(event: PointerEvent): void {
    this.sliding = true;
    this.press = { x: event.clientX, value: null };
  }

  protected slideMoved(event: PointerEvent): void {
    if (this.press && event.clientX !== this.press.x) {
      this.press = null;
    }
  }

  protected slideStopped(): void {
    // Firefox reads the value again after the release (an input event after pointerup)
    setTimeout(() => {
      this.press = null;
      this.sliding = false;
    }, 1);
  }

  /** Pressing on the dialog's content leaves the focus where it is, as on the legacy dialog. */
  protected keepFocus(event: MouseEvent): void {
    if (!(event.target as Element).closest('button, input, select, textarea, a')) {
      event.preventDefault();
    }
  }

  protected escape(event: Event): void {
    event.preventDefault();
    this.info.close();
  }

  protected documentPress(event: MouseEvent): void {
    if (event.type === 'mousedown') {
      this.pressStarted(event);
    } else {
      this.pressedAnywhere(event);
    }
  }

  /** A click or right-click anywhere, which onClick doesn't get. */
  private pressedAnywhere(event: MouseEvent): void {
    const target = event.target as Element;
    if (event.type === 'click' && this.info.takeDragRelease()) {
      return;
    }
    if (!this.info.isOpen() || target.closest('a')) {
      return;
    }
    const inside = this.dialog().nativeElement.contains(target);
    if (event.type === 'contextmenu') {
      event.preventDefault();
    } else if (inside) {
      return;
    }
    if (!this.sliding) {
      this.info.close();
    }
    if (cardElementOf(target)) {
      event.stopPropagation();
    }
  }

  /** A press on a card behind the dialog leaves the focus on its slider, as the legacy page's press on a card did. */
  private pressStarted(event: MouseEvent): void {
    this.info.forgetDragRelease();
    if (this.info.isOpen() && cardElementOf(event.target)) {
      event.preventDefault();
    }
  }

  private opensModal(): boolean {
    return this.theme.behaviour().cardInfoModal || this.document.querySelector('dialog:modal') !== null;
  }

  protected onClick(event: MouseEvent): void {
    const target = event.target as Element;
    if (target.closest('a, input, .gd-card-info__close')) {
      return;
    }
    const view = this.view();
    if (view?.flippable && target.closest('.gd-card-info__flip')) {
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
