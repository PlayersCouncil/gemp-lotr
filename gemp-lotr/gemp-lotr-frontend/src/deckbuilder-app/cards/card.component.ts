import { Component, computed, effect, ElementRef, inject, input } from '@angular/core';
import { CardRect } from './card-group-layout';
import { CardRef, CardRegistry } from './card-registry';

/** What a card element shows: the arguments of the legacy Card.CreateCardDiv. */
export interface CardFace {
  imageUrl: string;
  /** Errata marker over the card. */
  errata: boolean;
  /** Foil layer image, or null for none. */
  foilImage: string | null;
  /** Whether the card has a token layer (the collection's copy count lives there). */
  tokens: boolean;
  /** Packs and other products are drawn without the card border. */
  noBorder: boolean;
  /** The "count" token in the middle of the card, or null for none. */
  count: number | string | null;
  /**
   * Image drawn over the lower part of the card: a Race to Mount Doom meta-site's modifier over its position card.
   */
  overlayImage?: string | null;
}

/** Card.MetaSiteOverlayHeight: the share of the card (in %) the meta-site modifier covers. */
export const META_SITE_OVERLAY_HEIGHT = 27;

/** Inline style layoutCardElem gives every overlay: the card's full size at its top left. */
function fullSize(rect: CardRect): Record<string, string> {
  return {
    position: 'absolute',
    left: '0px',
    top: '0px',
    width: `${rect.width}px`,
    height: `${rect.height}px`,
  };
}

/**
 * A card as the legacy Card.CreateCardDiv built it and layoutCardElem/layoutTokens sized it: the image, the errata,
 * foil and token layers, the border (the click target, "actionArea") and the pile count. Same classes, so the legacy
 * stylesheets apply. The host is the ".card" element; the caller adds its role class (e.g. cardInCollection).
 */
@Component({
  selector: 'div[gempCard]',
  host: {
    class: 'card',
    '[style]': 'hostStyle()',
  },
  template: `
    <img
      [src]="face().imageUrl"
      width="100%"
      height="100%"
      style="transform: rotate(0deg) translate(0px, 0px); width: 100%; height: 100%; margin-bottom: 0px"
    />
    @if (face().errata) {
      <div class="errataOverlay" [style]="overlayStyle()">
        <img src="/gemp-lotr/images/errata-vertical.png" width="100%" height="100%" />
      </div>
    }
    @if (face().foilImage; as foilImage) {
      <div class="foilOverlay" [style]="overlayStyle()">
        <img [src]="foilImage" width="100%" height="100%" />
      </div>
    }
    @if (face().tokens) {
      <div class="tokenOverlay" [style]="overlayStyle()">
        @if (face().count !== null) {
          <div class="cardCount token" [style]="countStyle()">{{ face().count }}</div>
        }
      </div>
    }
    <div
      class="borderOverlay actionArea"
      [class.noBorder]="face().noBorder"
      width="100%"
      height="100%"
      [style]="borderStyle()"
    ></div>
    <div class="cardPileCount"></div>
    @if (face().overlayImage; as overlayImage) {
      <div
        class="metaSiteOverlay"
        style="position: absolute; bottom: 0; width: 100%; overflow: hidden; pointer-events: none"
        [style.height.%]="metaSiteOverlayHeight"
      >
        <img [src]="overlayImage" style="width: 100%; height: 100%; object-fit: cover; object-position: bottom" />
      </div>
    }
  `,
})
export class CardComponent {
  readonly face = input.required<CardFace>({ alias: 'gempCard' });
  readonly rect = input.required<CardRect>();
  /** What the card stands for, for the page-wide click handling. */
  readonly cardRef = input<CardRef | null>(null);

  protected readonly metaSiteOverlayHeight = META_SITE_OVERLAY_HEIGHT;

  constructor() {
    const registry = inject(CardRegistry);
    const host = inject<ElementRef<HTMLElement>>(ElementRef).nativeElement;
    effect(() => registry.set(host, this.cardRef()));
  }

  protected readonly hostStyle = computed(() => {
    const rect = this.rect();
    return {
      position: 'absolute',
      left: `${rect.left}px`,
      top: `${rect.top}px`,
      width: `${rect.width}px`,
      height: `${rect.height}px`,
      'z-index': `${rect.zIndex}`,
    };
  });

  protected readonly overlayStyle = computed(() => fullSize(this.rect()));

  /** layoutTokens: the count sits in the middle (positions not rounded, as in the legacy code). */
  protected readonly countStyle = computed(() => {
    const rect = this.rect();
    return {
      position: 'absolute',
      left: `${(rect.width - 20) / 2}px`,
      top: `${(rect.height - 18) / 2}px`,
    };
  });

  /** The border is 1/30 of the card's larger side, inside the card. */
  protected readonly borderStyle = computed(() => {
    const rect = this.rect();
    const border = this.face().noBorder ? 0 : Math.floor(Math.max(rect.width, rect.height) / 30);
    return {
      position: 'absolute',
      left: '0px',
      top: '0px',
      width: `${rect.width - 2 * border}px`,
      height: `${rect.height - 2 * border}px`,
      'border-width': `${border}px`,
    };
  });
}
