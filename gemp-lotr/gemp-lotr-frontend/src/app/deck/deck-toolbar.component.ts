import { Component, inject, input } from '@angular/core';
import { BASE_COLLECTION_TYPES, CollectionService } from '../core/collection/collection.service';
import { UiButtonDirective } from '../shared/ui-button.directive';

/**
 * The deck builder's top bar (#manageDecks): deck buttons, format and collection selectors, the deck name and the
 * auto-zoom toggle. Same markup as the legacy page after jQuery UI had turned the buttons into widgets.
 * The collection selector works; the buttons get their actions in later phases.
 */
@Component({
  selector: '[gempDeckToolbar]',
  imports: [UiButtonDirective],
  templateUrl: './deck-toolbar.component.html',
})
export class DeckToolbarComponent {
  readonly deckName = input('New deck');
  readonly convertErrataEnabled = input(false);
  readonly autoZoomEnabled = input(true);

  private readonly collection = inject(CollectionService);
  protected readonly baseCollections = BASE_COLLECTION_TYPES;
  protected readonly limitedCollections = this.collection.limitedCollections;
  protected readonly collectionType = this.collection.collectionType;

  protected selectCollection(event: Event): void {
    this.collection.selectCollectionType((event.target as HTMLSelectElement).value);
  }
}
