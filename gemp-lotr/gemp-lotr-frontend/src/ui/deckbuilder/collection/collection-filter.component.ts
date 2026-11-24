/**
 * The deck builder's collection filter is the shared card filter (ui/shared/card-filter: the game page's card search
 * uses it too), under the name the deck builder has always used for it. Its state is the deck builder's
 * (provideDeckbuilderState provides CollectionQuery, CollectionBrowser, FilterPins and CardSize as the filter's model,
 * pager, pins and card size).
 */
export { CardFilterComponent as CollectionFilterComponent } from '../../shared/card-filter/card-filter.component';
export type { FilterPart } from '../../shared/card-filter/card-filter.component';
