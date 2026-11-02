import { DOCUMENT } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { CardFace, CardComponent } from '../cards/card.component';
import { CardRect, groupArea, layoutNormalGroup, layoutVerticalBar } from '../cards/card-group-layout';
import { CardRef } from '../cards/card-registry';
import { CardImageService } from '../core/cards/card-image.service';
import { foilOverlayImage, readFoilPresentation } from '../core/cards/foil-presentation';
import { DeckSlot, drawPile, DrawSide } from '../core/deck/deck-model';
import { DeckService } from '../core/deck/deck.service';
import { PageLayoutService } from '../layout/page-layout.service';
import { Rect, rectStyle } from '../layout/rect';
import { DeckToolbarComponent } from './deck-toolbar.component';

/** NormalCardGroup.maxCardHeight of the draw deck piles (the slots keep the default). */
const PILE_MAX_CARD_HEIGHT = 200;

export interface DeckCardView {
  /** Card id (keeps a card's element while the deck changes). */
  key: number | string;
  face: CardFace;
  rect: CardRect;
  ref: CardRef;
  /** "cardInDeck" for cards that can be removed by clicking; copies behind a card and the Race card have none. */
  role: string;
}

/**
 * The deck pane (#deckDiv): toolbar, Ring-bearer, Ring, Map and Race slots, the sites, the Free Peoples and Shadow
 * piles and the deck stats, positioned like the legacy layoutUI with the cards laid out by the legacy card groups.
 */
@Component({
  selector: 'div[gempDeckArea]',
  imports: [DeckToolbarComponent, CardComponent],
  templateUrl: './deck-area.component.html',
})
export class DeckAreaComponent {
  protected readonly layout = inject(PageLayoutService);
  protected readonly deck = inject(DeckService);
  private readonly images = inject(CardImageService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly foilImage = foilOverlayImage(readFoilPresentation(inject(DOCUMENT).cookie));
  protected readonly rectStyle = rectStyle;

  /** The server's stats HTML, shown as-is (as the legacy page did). */
  protected readonly statsHtml = computed(() => this.sanitizer.bypassSecurityTrustHtml(this.deck.statsHtml()));

  protected readonly ringBearerCards = computed(() => this.slotViews('ringBearer', this.layout.deck().ringBearer));
  protected readonly ringCards = computed(() => this.slotViews('ring', this.layout.deck().ring));
  protected readonly mapCards = computed(() => {
    const deck = this.layout.deck();
    // The legacy page laid the hidden Map slot out at the Ring-bearer's size
    return this.slotViews('map', deck.map ?? deck.ringBearer);
  });
  protected readonly raceCards = computed<DeckCardView[]>(() => {
    const race = this.deck.raceCard();
    if (!race) {
      return [];
    }
    const box = this.layout.deck().race ?? this.layout.deck().ringBearer;
    const face = this.face(race.blueprintId, 'deck');
    const [layout] = layoutNormalGroup([{ horizontal: face.horizontal }], groupArea(this.local(box)));
    return [
      {
        key: `race-${race.blueprintId}`,
        face: {
          ...face.face,
          overlayImage: race.overlayBlueprintId ? this.images.imageUrl(race.overlayBlueprintId) : null,
        },
        rect: layout.card,
        ref: { kind: 'race', blueprintId: race.blueprintId },
        role: '',
      },
    ];
  });

  protected readonly siteCards = computed<DeckCardView[]>(() => {
    const sites = this.deck.deck().sites;
    const faces = sites.map((card) => this.face(card.blueprintId, 'deck'));
    const rects = layoutVerticalBar(
      faces.map((f) => ({ horizontal: f.horizontal })),
      groupArea(this.local(this.layout.deck().sites)),
    );
    return sites.map((card, i) => ({
      key: card.id,
      face: faces[i].face,
      rect: rects[i],
      ref: { kind: 'slot', slot: 'sites', blueprintId: card.blueprintId, card },
      role: 'cardInDeck',
    }));
  });

  protected readonly drawCards = computed<DeckCardView[]>(() => [
    ...this.pileViews('FREE_PEOPLE', this.layout.deck().freePeoples),
    ...this.pileViews('SHADOW', this.layout.deck().shadow),
  ]);

  /** The Map and Race slots are hidden when the format has none. */
  protected optionalStyle(rect: Rect | null): Record<string, string> {
    return rect ? rectStyle(rect) : { display: 'none' };
  }

  protected slotClicked(slot: DeckSlot): void {
    this.deck.slotClicked(slot);
  }

  private slotViews(slot: Exclude<DeckSlot, 'sites'>, box: Rect): DeckCardView[] {
    const cards = this.deck.deck()[slot];
    const faces = cards.map((card) => this.face(card.blueprintId, 'deck'));
    const layout = layoutNormalGroup(
      faces.map((f) => ({ horizontal: f.horizontal })),
      groupArea(this.local(box)),
    );
    return cards.map((card, i) => ({
      key: card.id,
      face: faces[i].face,
      rect: layout[i].card,
      ref: { kind: 'slot', slot, blueprintId: card.blueprintId, card },
      role: 'cardInDeck',
    }));
  }

  /** A pile of the draw deck, laid out in its half of the deck region; extra copies are drawn behind each card. */
  private pileViews(side: DrawSide, box: Rect): DeckCardView[] {
    const pile = drawPile(this.deck.deck(), side);
    const shown = pile.map((entry) => this.face(entry.card.blueprintId, side));
    const behind = pile.map((entry) => entry.behind.map((copy) => this.face(copy.blueprintId, 'attached')));
    const layout = layoutNormalGroup(
      pile.map((_, i) => ({
        horizontal: shown[i].horizontal,
        attached: behind[i].map((f) => ({ horizontal: f.horizontal })),
      })),
      groupArea(box),
      { maxCardHeight: PILE_MAX_CARD_HEIGHT },
    );
    return pile.flatMap((entry, i) => [
      ...entry.behind.map((copy, j) => ({
        key: copy.id,
        face: behind[i][j].face,
        rect: layout[i].attached[j],
        ref: { kind: 'behind', blueprintId: copy.blueprintId, card: copy } as CardRef,
        role: '',
      })),
      {
        key: entry.card.id,
        face: shown[i].face,
        rect: layout[i].card,
        ref: { kind: 'draw', blueprintId: entry.card.blueprintId, card: entry.card } as CardRef,
        role: 'cardInDeck',
      },
    ]);
  }

  /** The legacy groups of a slot were laid out from the slot's own corner. */
  private local(box: Rect): Rect {
    return { left: 0, top: 0, width: box.width, height: box.height };
  }

  /** A deck card (Card.CreateCardDiv without tokens). */
  private face(blueprintId: string, zone: string): { face: CardFace; horizontal: boolean } {
    const image = this.images.resolve(blueprintId, zone);
    return {
      horizontal: image.horizontal,
      face: {
        imageUrl: image.imageUrl,
        errata: image.errata,
        foilImage: image.foil ? this.foilImage : null,
        tokens: false,
        noBorder: image.pack,
        count: null,
      },
    };
  }
}
