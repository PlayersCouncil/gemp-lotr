import { inject, Injectable } from '@angular/core';
import { CardInfoService } from '../cards/card-info.service';
import { CardRef } from '../cards/card-ref';
import { DeckService } from '../deck/deck.service';
import { PacksService } from '../packs/packs.service';

/**
 * What using a card means (the legacy deckBuildingUi.clickCardFunction), whatever gesture the view maps to it:
 *  - activating a collection card adds it to the deck; a deck card is removed (one copy); a pack is opened; a
 *    selection pack's choice is taken. Copies behind a card and the Race card do nothing;
 *  - asking for details shows the card information.
 */
@Injectable({ providedIn: 'root' })
export class DeckbuilderActions {
  private readonly deck = inject(DeckService);
  private readonly packs = inject(PacksService);
  private readonly cardInfo = inject(CardInfoService);

  activate(ref: CardRef): void {
    switch (ref.kind) {
      case 'collection':
        if (ref.role === 'cardInCollection') {
          this.deck.addFromCollection(ref.blueprintId, ref.item.kind === 'card' ? ref.item.side : null);
        } else if (ref.role === 'packInCollection' || ref.role === 'selectionInCollection') {
          this.packs.packClicked(ref.blueprintId, ref.item.kind === 'pack' ? ref.item.selection : null);
        }
        break;
      case 'shown':
        if (ref.role === 'cardToSelect') {
          this.packs.choose(ref.blueprintId);
        }
        break;
      case 'slot':
        this.deck.removeSlotCard(ref.slot, ref.card.id);
        break;
      case 'draw':
        this.deck.removeDrawCard(ref.card.id);
        break;
      default:
      // Copies behind a card and the Race card do nothing
    }
  }

  showInfo(ref: CardRef): void {
    this.cardInfo.show(ref);
  }
}
