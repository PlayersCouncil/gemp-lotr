import { Component, computed, inject } from '@angular/core';
import { BASE_COLLECTION_TYPES, CollectionBrowser } from '../../../core/collection/collection-browser.service';
import { FilterPins } from '../../../core/collection/filter-pins';
import { DrawerComponent } from '../../shared/drawer/drawer.component';
import { CollectionFilterComponent } from './collection-filter.component';

/**
 * The collection's bar in the compact layout (the modern theme on a portrait or narrow screen), above the cards: the
 * collection menu and the filter's pane part (the card name beside the menu's button, then the filters kept shown and
 * the pager). The menu holds the collection list (in the toolbar otherwise) and the filter's menu part; its button
 * shows how many filters in it narrow the cards.
 *
 * DOM contract: `.gd-collection-bar` holding the menu (`gd-drawer`, `data-drawer=collection`) with
 * `label.gd-menu__field` holding `select.gd-toolbar__collection`, then `gd-collection-filter[data-part=menu]`; then
 * `gd-collection-filter[data-part=pane]`.
 */
@Component({
  selector: 'gd-collection-menu',
  styleUrl: './collection-menu.component.css',
  imports: [DrawerComponent, CollectionFilterComponent],
  host: { class: 'gd-collection-bar' },
  template: `
    <gd-drawer
      name="collection"
      label="Collection menu"
      [badge]="pins.hiddenSet()"
      [note]="pins.hiddenSet() === 1 ? '1 filter set' : pins.hiddenSet() + ' filters set'"
    >
      <label class="gd-menu__field">
        <span>Collection</span>
        <select class="gd-toolbar__collection" (change)="selectCollection($event)">
          @for (collection of collections(); track collection.type) {
            <option [value]="collection.type" [selected]="collection.type === collectionType()">
              {{ collection.name }}
            </option>
          }
        </select>
      </label>
      <gd-collection-filter part="menu" />
    </gd-drawer>
    <gd-collection-filter part="pane" />
  `,
})
export class CollectionMenuComponent {
  private readonly collection = inject(CollectionBrowser);
  protected readonly pins = inject(FilterPins);

  protected readonly collectionType = this.collection.collectionType;
  protected readonly collections = computed(() => [...BASE_COLLECTION_TYPES, ...this.collection.limitedCollections()]);

  protected selectCollection(event: Event): void {
    this.collection.selectCollectionType((event.target as HTMLSelectElement).value);
  }
}
