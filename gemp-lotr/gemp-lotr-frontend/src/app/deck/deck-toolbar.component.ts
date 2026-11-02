import { Component, inject, input } from '@angular/core';
import { BASE_COLLECTION_TYPES, CollectionService } from '../core/collection/collection.service';
import { DeckService } from '../core/deck/deck.service';
import { UiButtonDirective } from '../shared/ui-button.directive';

/**
 * The deck builder's top bar (#manageDecks): deck buttons, format and collection selectors, the deck name and the
 * auto-zoom toggle. Same markup as the legacy page after jQuery UI had turned the buttons into widgets.
 * Working: new, save, rename, copy, convert to PC errata, the format and collection selectors. The deck list,
 * library, import, notes and auto-zoom buttons get their actions in Phase 6.
 */
@Component({
  selector: '[gempDeckToolbar]',
  imports: [UiButtonDirective],
  templateUrl: './deck-toolbar.component.html',
})
export class DeckToolbarComponent {
  readonly autoZoomEnabled = input(true);

  protected readonly deck = inject(DeckService);
  private readonly collection = inject(CollectionService);
  protected readonly baseCollections = BASE_COLLECTION_TYPES;
  protected readonly limitedCollections = this.collection.limitedCollections;
  protected readonly collectionType = this.collection.collectionType;

  protected selectFormat(event: Event): void {
    this.deck.selectFormat((event.target as HTMLSelectElement).selectedIndex);
  }

  protected selectCollection(event: Event): void {
    this.collection.selectCollectionType((event.target as HTMLSelectElement).value);
  }
}
