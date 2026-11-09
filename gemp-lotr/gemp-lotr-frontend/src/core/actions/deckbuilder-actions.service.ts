import { inject, Injectable } from '@angular/core';
import { CardActions } from '../cards/card-actions';
import { DeckEditing } from '../deck/deck-editing.service';
import { PacksService } from '../packs/packs.service';
import { DeckbuilderCardRef } from './deckbuilder-card-ref';

/**
 * What using a card means on the deck builder: activating a collection card adds it to the deck; a deck card is removed
 * (one copy); a pack is opened; a selection pack's choice is taken. Copies behind a card and the Race card do nothing.
 */
@Injectable()
export class DeckbuilderActions extends CardActions {
  private readonly editing = inject(DeckEditing);
  private readonly packs = inject(PacksService);

  activate(ref: DeckbuilderCardRef): void {
    switch (ref.kind) {
      case 'collection':
        if (ref.role === 'card') {
          this.editing.addFromCollection(ref.blueprintId, ref.item.kind === 'card' ? ref.item.side : null);
        } else if (ref.role === 'pack' || ref.role === 'selection') {
          this.packs.packClicked(ref.blueprintId, ref.item.kind === 'pack' ? ref.item.selection : null);
        }
        break;
      case 'shown':
        if (ref.choosable) {
          this.packs.choose(ref.blueprintId);
        }
        break;
      case 'slot':
        this.editing.removeSlotCard(ref.slot, ref.card.id);
        break;
      case 'draw':
        this.editing.removeDrawCard(ref.card.id);
        break;
      default:
      // Copies behind a card and the Race card do nothing
    }
  }
}
