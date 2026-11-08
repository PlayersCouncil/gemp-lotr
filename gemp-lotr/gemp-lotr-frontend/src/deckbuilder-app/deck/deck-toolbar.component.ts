import { Component, inject } from '@angular/core';
import { BASE_COLLECTION_TYPES, CollectionService } from '../../core/collection/collection.service';
import { DeckService } from '../../core/deck/deck.service';
import { AutoZoomService } from '../dialogs/auto-zoom.component';
import { DeckLibraryService } from '../../core/deck/deck-library.service';
import { THEME_LABELS, THEMES, ThemeService } from '../../core/platform/theme.service';
import { UiButtonDirective } from '../shared/ui-button.directive';

/**
 * The deck builder's top bar (#manageDecks): deck buttons, format and collection selectors, the deck name and the
 * auto-zoom toggle. Same markup as the legacy page after jQuery UI had turned the buttons into widgets, plus the theme
 * picker.
 */
@Component({
  selector: '[gempDeckToolbar]',
  imports: [UiButtonDirective],
  templateUrl: './deck-toolbar.component.html',
})
export class DeckToolbarComponent {
  protected readonly deck = inject(DeckService);
  protected readonly dialogs = inject(DeckLibraryService);
  protected readonly zoom = inject(AutoZoomService);
  private readonly collection = inject(CollectionService);
  protected readonly baseCollections = BASE_COLLECTION_TYPES;
  protected readonly limitedCollections = this.collection.limitedCollections;
  protected readonly collectionType = this.collection.collectionType;
  protected readonly themes = THEMES.map((name) => ({ name, label: THEME_LABELS[name] }));
  protected readonly theme = inject(ThemeService);

  protected selectFormat(event: Event): void {
    this.deck.selectFormat((event.target as HTMLSelectElement).selectedIndex);
  }

  protected selectCollection(event: Event): void {
    this.collection.selectCollectionType((event.target as HTMLSelectElement).value);
  }

  protected selectTheme(event: Event): void {
    void this.theme.select((event.target as HTMLSelectElement).value);
  }
}
