import { DOCUMENT } from '@angular/common';
import { Component, computed, inject, input, signal } from '@angular/core';
import { CardComponent, CardFace } from '../cards/card.component';
import { CardRect, groupArea, layoutNormalGroup } from '../cards/card-group-layout';
import { CardRef } from '../../core/cards/card-ref';
import { CardImageService } from '../../core/cards/card-image.service';
import { foilOverlayImage, readFoilPresentation } from '../../core/cards/foil-presentation';
import { PacksService, ShownCard } from '../../core/packs/packs.service';
import { UiDialogComponent } from '../shared/ui-dialog.component';

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
