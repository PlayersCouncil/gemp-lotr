import { DOCUMENT } from '@angular/common';
import { Component, computed, DestroyRef, inject, Injectable, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CardComponent, CardFace } from '../cards/card.component';
import { CardRect, groupArea, layoutNormalGroup } from '../cards/card-group-layout';
import { CardRef } from '../cards/card-registry';
import { ApiError, GENERIC_COMMUNICATION_ERROR } from '../core/api/api-error';
import { DeckbuilderApi } from '../core/api/deckbuilder-api.service';
import { PackDelivery } from '../core/api/models';
import { CardImageService } from '../core/cards/card-image.service';
import { foilOverlayImage, readFoilPresentation } from '../core/cards/foil-presentation';
import { CollectionService } from '../core/collection/collection.service';
import { PackDeliveryService } from '../core/delivery/pack-delivery.service';
import { UiDialogComponent } from '../shared/ui-dialog.component';

/** The legacy page asked for the opened pack's contents a second after opening it, besides the server's notice. */
const DELIVERY_DELAY_MS = 1000;

export interface ShownCard {
  blueprintId: string;
  count: number | null;
  pack: boolean;
}

/** A dialog laying out cards; sized to 80% of the window when first opened (openSizeDialog), then as left. */
export interface CardsDialog {
  title: string;
  cards: ShownCard[];
}

/**
 * Opening packs from the collection (deckBuildingUi.clickCardFunction), the "Choose one" dialog of selection packs,
 * and the "New items - <collection>" dialogs showing what was opened (deckbuilderDeliveryService.js).
 */
@Injectable({ providedIn: 'root' })
export class PacksService {
  private readonly api = inject(DeckbuilderApi);
  private readonly collection = inject(CollectionService);
  private readonly delivery = inject(PackDeliveryService);

  /** The selection pack being opened and its choices. */
  readonly selection = signal<{ packId: string; choices: string[] } | null>(null);
  /** The open "New items" dialogs, by collection name. */
  readonly deliveries = signal<ReadonlyMap<string, ShownCard[]>>(new Map());

  constructor() {
    this.delivery.deliveries$
      .pipe(takeUntilDestroyed(inject(DestroyRef)))
      .subscribe((deliveries) => this.delivered(deliveries));
  }

  /** As the legacy page did on start: shows anything opened elsewhere and not yet seen. */
  start(): void {
    this.delivery.fetch();
  }

  packClicked(blueprintId: string, selection: string[] | null): void {
    if (blueprintId.startsWith('(S)')) {
      this.selection.set({ packId: blueprintId, choices: selection ?? [] });
      return;
    }
    this.api.openPack(this.collection.collectionType(), blueprintId).subscribe({
      next: () => this.opened(),
      error: (error: unknown) =>
        alert(
          ApiError.from(error).is(404)
            ? 'You have no pack of this type in your collection.'
            : GENERIC_COMMUNICATION_ERROR,
        ),
    });
  }

  /** A card of the "Choose one" dialog was picked: the dialog closes and the pack is opened with it. */
  choose(blueprintId: string): void {
    const selection = this.selection();
    if (!selection) {
      return;
    }
    this.selection.set(null);
    this.api.openSelectionPack(this.collection.collectionType(), selection.packId, blueprintId).subscribe({
      next: () => this.opened(),
      error: (error: unknown) =>
        alert(
          ApiError.from(error).is(404)
            ? 'You have no pack of this type in your collection or that selection is not available for this pack.'
            : GENERIC_COMMUNICATION_ERROR,
        ),
    });
  }

  closeDelivery(collectionName: string): void {
    this.deliveries.update((open) => {
      const next = new Map(open);
      next.delete(collectionName);
      return next;
    });
  }

  private opened(): void {
    setTimeout(() => this.delivery.fetch(), DELIVERY_DELAY_MS);
    this.collection.load();
  }

  /** New contents go into the collection's dialog, after anything it still shows. */
  private delivered(deliveries: PackDelivery[]): void {
    this.deliveries.update((open) => {
      const next = new Map(open);
      for (const delivery of deliveries) {
        next.set(delivery.collectionName, [
          ...(next.get(delivery.collectionName) ?? []),
          ...delivery.packs.map((p) => ({ blueprintId: p.blueprintId, count: p.count, pack: true })),
          ...delivery.cards.map((c) => ({ blueprintId: c.blueprintId, count: c.count, pack: false })),
        ]);
      }
      return next;
    });
  }
}

interface ShownView {
  face: CardFace;
  rect: CardRect;
  ref: CardRef;
  role: string;
}

/** The cards of a pack dialog, laid out like the legacy NormalCardGroup over the dialog's content. */
@Component({
  selector: 'gemp-shown-cards',
  imports: [CardComponent],
  host: { style: 'display: contents' },
  template: `
    @for (view of views(); track $index) {
      <div [gempCard]="view.face" [rect]="view.rect" [cardRef]="view.ref" [class]="view.role"></div>
    }
  `,
})
export class ShownCardsComponent {
  private readonly images = inject(CardImageService);
  private readonly foilImage = foilOverlayImage(readFoilPresentation(inject(DOCUMENT).cookie));

  readonly cards = input<ShownCard[]>([]);
  readonly zone = input('selection');
  /** "cardToSelect" for selection pack choices. */
  readonly role = input('');
  /** Opened contents show their count and are never borderless; selection choices are drawn like collection cards. */
  readonly delivered = input(false);
  /** The dialog content's size, set by the dialog. */
  readonly contentSize = signal({ width: 0, height: 0 });

  protected readonly views = computed<ShownView[]>(() => {
    const { width, height } = this.contentSize();
    const images = this.cards().map((card) => this.images.resolve(card.blueprintId, this.zone()));
    // The legacy dialogs laid the cards out at (2, 2) with the content's size plus 10px, less 2px on each side
    const layout = layoutNormalGroup(
      images.map((image) => ({ horizontal: image.horizontal })),
      groupArea({ left: 2, top: 2, width: width + 6, height: height + 6 }),
    );
    return this.cards().map((card, i) => {
      const image = images[i];
      const delivered = this.delivered();
      return {
        face: {
          imageUrl: image.imageUrl,
          errata: delivered ? !card.pack && image.errata : image.errata,
          foilImage: image.foil ? this.foilImage : null,
          tokens: delivered,
          noBorder: delivered ? false : image.pack,
          count: delivered ? card.count : null,
        },
        rect: layout[i].card,
        ref: { kind: 'shown', blueprintId: card.blueprintId, zone: this.zone(), role: this.role() },
        role: this.role(),
      };
    });
  });
}

/** The selection pack and "New items" dialogs. */
@Component({
  selector: 'gemp-pack-dialogs',
  imports: [UiDialogComponent, ShownCardsComponent],
  template: `
    @if (packs.selection(); as selection) {
      <gemp-ui-dialog
        title="Choose one"
        [modal]="true"
        [width]="sizes['selection']?.width ?? defaultWidth"
        [height]="sizes['selection']?.height ?? defaultHeight"
        (closed)="packs.selection.set(null)"
        (contentResized)="choices.contentSize.set($event)"
        (sizeChanged)="sizes['selection'] = $event"
      >
        <gemp-shown-cards #choices [cards]="selectionCards()" role="cardToSelect" />
      </gemp-ui-dialog>
    }
    @for (delivery of deliveryList(); track delivery.name) {
      <gemp-ui-dialog
        [title]="'New items - ' + delivery.name"
        closeText=""
        [width]="sizes['delivery:' + delivery.name]?.width ?? defaultWidth"
        [height]="sizes['delivery:' + delivery.name]?.height ?? defaultHeight"
        (closed)="packs.closeDelivery(delivery.name)"
        (contentResized)="shown.contentSize.set($event)"
        (sizeChanged)="sizes['delivery:' + delivery.name] = $event"
      >
        <gemp-shown-cards #shown [cards]="delivery.cards" zone="player" [delivered]="true" />
      </gemp-ui-dialog>
    }
  `,
})
export class PackDialogsComponent {
  protected readonly packs = inject(PacksService);
  private readonly window = inject(DOCUMENT).defaultView!;
  /** openSizeDialog: 80% of the window the first time. */
  protected readonly defaultWidth = this.window.innerWidth * 0.8;
  protected readonly defaultHeight = this.window.innerHeight * 0.8;
  /** Sizes the player gave the dialogs, kept for the next time they open (as jQuery UI kept its options). */
  protected readonly sizes: Record<string, { width: number; height: number } | undefined> = {};

  protected readonly deliveryList = computed(() =>
    [...this.packs.deliveries().entries()].map(([name, cards]) => ({ name, cards })),
  );

  protected readonly selectionCards = computed<ShownCard[]>(() =>
    (this.packs.selection()?.choices ?? []).map((blueprintId) => ({ blueprintId, count: null, pack: false })),
  );
}
