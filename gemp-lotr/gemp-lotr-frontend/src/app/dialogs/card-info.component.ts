import { DOCUMENT } from '@angular/common';
import { Component, computed, inject, Injectable, signal, viewChild } from '@angular/core';
import { CardDisplayComponent, cardDisplayData, cardDisplaySize, TARGET_LONG } from '../cards/card-display.component';
import { CardRef, zoneOf } from '../cards/card-registry';
import { parseBlueprint } from '../core/cards/card-blueprint';
import { CardImageService } from '../core/cards/card-image.service';
import { readCookie, writeCookie } from '../core/cookies';
import { UiDialogComponent } from '../shared/ui-dialog.component';
import { UiSliderComponent } from '../shared/ui-slider.component';

/** CardInfoDialog's sizes. */
const DETAILS_WIDTH = 400;
const DIALOG_PADDING = 25;
const FOOTER_HEIGHT = 20;
/** Any narrower and the title wraps. */
const MIN_WIDTH = 175;
export const ZOOM_COOKIE = 'card-info-dialog-slider-last-value';
const DEFAULT_ZOOM = 0.5;

/** RTMD meta-site modifiers (sets 90-94) can't be flipped in the dialog. */
function isMetaSiteModifier(blueprintId: string): boolean {
  const { setNo } = parseBlueprint(blueprintId);
  return setNo >= 90 && setNo <= 94;
}

/** Whether the card info dialog is open, and its card (CardInfoDialog's state). */
@Injectable({ providedIn: 'root' })
export class CardInfoService {
  readonly card = signal<CardRef | null>(null);
  /** The slider is being used: a click then doesn't close the dialog. */
  sliding = false;

  isOpen(): boolean {
    return this.card() !== null;
  }

  show(ref: CardRef): void {
    this.card.set(ref);
  }

  close(): void {
    this.card.set(null);
  }

  /** A click anywhere on the page closes the dialog, unless the zoom slider was just used (CardInfoDialog.mouseUp). */
  pageClicked(): void {
    if (!this.sliding) {
      this.close();
    }
  }
}

/**
 * The "Card Information" dialog (legacy CardInfoDialog): the card as large as the zoom slider (remembered in a
 * cookie) and the window allow, and a link to its wiki page. Clicking the card flips it.
 */
@Component({
  selector: 'gemp-card-info',
  imports: [UiDialogComponent, UiSliderComponent, CardDisplayComponent],
  template: `
    @if (view(); as v) {
      <gemp-ui-dialog
        title="Card Information"
        closeText=""
        [resizable]="false"
        [width]="v.width"
        [height]="v.height"
        (closed)="info.close()"
      >
        <div
          id="card-info-outer"
          style="display: flex; flex-direction: column; position: relative; width: 100%; height: 100%"
        >
          <div
            id="card-info-slider"
            title="Card Size"
            style="flex-grow: 0"
            [style.display]="touch ? 'none' : null"
            gempUiSlider
            [min]="0.4"
            [max]="1"
            [step]="0.1"
            range="min"
            [value]="zoom()"
            (slide)="setZoom($event)"
            (slideStart)="info.sliding = true"
            (slideStop)="slideStopped()"
          ></div>
          <div id="card-info-inner" style="display: flex; flex-direction: row; flex-grow: 1">
            <div id="card-image-content" style="flex-grow: 0">
              <gemp-card-display
                [data]="v.data"
                [maxWidth]="v.maxLong"
                [maxHeight]="v.maxLong"
                [foilImage]="'/gemp-lotr/images/holo.jpg'"
                [flipOnClick]="v.flippable"
              />
            </div>
            <div id="cardEffects" style="flex-grow: 1; padding: 0px"></div>
          </div>
          <div
            class="card-info-footer"
            style="flex-grow: 0; padding: 5px; text-align: center"
            [style.display]="v.wikiUrl ? null : 'none'"
          >
            @if (v.wikiUrl) {
              <div><a [href]="v.wikiUrl" target="_blank">Go to Wiki Page</a></div>
            }
          </div>
        </div>
      </gemp-ui-dialog>
    }
  `,
})
export class CardInfoComponent {
  protected readonly info = inject(CardInfoService);
  private readonly images = inject(CardImageService);
  private readonly document = inject(DOCUMENT);
  private readonly dialog = viewChild(UiDialogComponent);
  protected readonly touch = 'ontouchstart' in this.document.documentElement;

  /** The legacy dialog took its limits from the window size when the page loaded. */
  private readonly maxWidth = this.document.defaultView!.innerWidth - DETAILS_WIDTH - DIALOG_PADDING;
  private readonly maxHeight = this.document.defaultView!.innerHeight - DIALOG_PADDING * 3;

  protected readonly zoom = signal(this.readZoom());

  protected readonly view = computed(() => {
    const ref = this.info.card();
    if (!ref) {
      return null;
    }
    const image = this.images.resolve(ref.blueprintId, zoneOf(ref));
    const overlay = ref.kind === 'race' && ref.overlayBlueprintId ? this.images.imageUrl(ref.overlayBlueprintId) : null;
    const data = cardDisplayData(image, overlay);
    const maxLong = Math.min(this.maxHeight, this.maxWidth, this.zoom() * TARGET_LONG);
    const size = cardDisplaySize(data.horizontal, maxLong, maxLong);
    const wikiUrl = image.hasWiki ? image.wikiUrl : null;
    return {
      data,
      maxLong,
      width: Math.max(size.width + DIALOG_PADDING, MIN_WIDTH),
      height: size.height + DIALOG_PADDING * 2 + (wikiUrl ? FOOTER_HEIGHT : 0),
      wikiUrl,
      flippable: !isMetaSiteModifier(image.bareBlueprint),
    };
  });

  protected setZoom(value: number): void {
    writeCookie(this.document, ZOOM_COOKIE, `${value}`);
    this.zoom.set(value);
    const view = this.view();
    const dialog = this.dialog();
    if (view && dialog) {
      dialog.setSize(view.width, view.height);
      // Re-centre when the bigger dialog would leave the area it may use
      const rect = dialog.rect();
      if (rect.top + view.height + 10 > this.maxHeight || rect.left + view.width + 10 > this.maxWidth) {
        dialog.recenter();
      }
    }
  }

  protected slideStopped(): void {
    setTimeout(() => (this.info.sliding = false), 1);
  }

  private readZoom(): number {
    const value = parseFloat(readCookie(this.document.cookie, ZOOM_COOKIE) ?? '');
    return Number.isFinite(value) ? value : DEFAULT_ZOOM;
  }
}
