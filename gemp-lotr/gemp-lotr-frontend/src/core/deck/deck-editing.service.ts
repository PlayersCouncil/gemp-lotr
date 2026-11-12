import { inject, Injectable } from '@angular/core';
import { CollectionQuery, DEFAULT_FILTER_OVERRIDE } from '../collection/collection-query.service';
import { DeckChanges } from './deck-changes.service';
import { DeckDraft } from './deck-draft.service';
import { addBySide, addToSlot, DeckSlot, removeDrawCopy, removeFromSlot } from './deck-model';

/** The predefined collection filters for the special slots. */
const SLOT_FILTERS: Partial<Record<DeckSlot, string>> = {
  ringBearer: 'canStartWithRing:true product:card',
  ring: 'cardType:THE_ONE_RING product:card',
  map: 'cardType:MAP product:card',
};

/** The player changing the open deck by hand: adding and removing cards, filling a special slot, editing the notes. */
@Injectable()
export class DeckEditing {
  private readonly draft = inject(DeckDraft);
  private readonly changes = inject(DeckChanges);
  private readonly collection = inject(CollectionQuery);

  /** Where a collection card clicked now goes: a special slot after clicking it, otherwise by the card's side. */
  private selectionSlot: DeckSlot | null = null;

  /** A collection card was clicked: it goes to the slot being filled, or by its side. */
  addFromCollection(blueprintId: string, side: string | null): void {
    const slot = this.selectionSlot;
    if (slot) {
      this.draft.deck.update((deck) => addToSlot(deck, slot, blueprintId));
      this.showNormalFilter();
    } else {
      this.draft.deck.update((deck) => addBySide(deck, blueprintId, side));
    }
    this.changes.deckModified(true);
  }

  removeSlotCard(slot: DeckSlot, id: number): void {
    this.draft.deck.update((deck) => removeFromSlot(deck, slot, id));
    this.changes.deckModified(true);
  }

  removeDrawCard(shownId: number): void {
    this.draft.deck.update((deck) => removeDrawCopy(deck, shownId));
    this.changes.deckModified(true);
  }

  /**
   * A slot was clicked. An empty Ring-bearer, Ring or Map slot shows the cards that fit it; the next collection card
   * clicked goes there. Clicking the sites (even on a site) shows the sites, by site number.
   */
  slotClicked(slot: DeckSlot): void {
    if (slot === 'sites') {
      this.collection.setField('cardType', 'SITE');
      this.collection.setField('sort', 'siteNumber,name');
      return;
    }
    const filter = SLOT_FILTERS[slot];
    if (filter && this.draft.deck()[slot].length === 0) {
      this.selectionSlot = slot;
      this.collection.setFilterOverride(filter);
    }
  }

  /** Back to the normal collection, with collection cards going to the deck by their side. */
  showNormalFilter(): void {
    this.selectionSlot = null;
    this.collection.setFilterOverride(DEFAULT_FILTER_OVERRIDE);
  }

  /** The notes were edited. */
  setNotes(notes: string): void {
    if (notes !== this.draft.notes()) {
      this.draft.notes.set(notes);
      this.changes.deckModified(true);
    }
  }
}
