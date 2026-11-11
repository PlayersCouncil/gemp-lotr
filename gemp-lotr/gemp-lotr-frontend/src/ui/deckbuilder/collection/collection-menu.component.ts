import { Component, computed, inject } from '@angular/core';
import { BASE_COLLECTION_TYPES, CollectionBrowser } from '../../../core/collection/collection-browser.service';
import { DrawerComponent } from '../../shared/drawer/drawer.component';

/**
 * The collection's bar in the compact layout (the modern theme on a portrait or narrow screen), above the filter: the
 * collection menu, which holds the collection list (in the toolbar otherwise).
 *
 * DOM contract: `.gd-collection-bar` holding the menu (`gd-drawer`, `data-drawer=collection`) with
 * `label.gd-menu__field` holding `select.gd-toolbar__collection`.
 */
@Component({
  selector: 'gd-collection-menu',
  styleUrl: './collection-menu.component.css',
  imports: [DrawerComponent],
  host: { class: 'gd-collection-bar' },
  template: `
    <gd-drawer name="collection" label="Collection menu">
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
    </gd-drawer>
  `,
})
export class CollectionMenuComponent {
  private readonly collection = inject(CollectionBrowser);

  protected readonly collectionType = this.collection.collectionType;
  protected readonly collections = computed(() => [...BASE_COLLECTION_TYPES, ...this.collection.limitedCollections()]);

  protected selectCollection(event: Event): void {
    this.collection.selectCollectionType((event.target as HTMLSelectElement).value);
  }
}
