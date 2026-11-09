import { computed, inject, Injectable } from '@angular/core';
import { DeckDraft } from '../deck/deck-draft.service';
import { DeckFormatSelection } from '../deck/deck-format-selection.service';
import { deckCardCounts } from '../deck/deck-model';

/** How many copies of a card are left to add: what is on the deck side of the page is taken out of the collection. */
@Injectable()
export class CollectionCounts {
  private readonly draft = inject(DeckDraft);
  private readonly formats = inject(DeckFormatSelection);

  private readonly inDeck = computed(() => {
    const race = this.formats.raceCard();
    return deckCardCounts(this.draft.deck(), race ? [race.blueprintId] : []);
  });

  available(blueprintId: string, count: number): number {
    return count - (this.inDeck().get(blueprintId) ?? 0);
  }
}
