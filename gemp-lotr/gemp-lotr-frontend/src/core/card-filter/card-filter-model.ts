import { Signal } from '@angular/core';
import { CultureGroupId } from './filter-cultures';
import { FilterFields } from './filter-fields';
import { SelectOption } from './filter-options';

/** A filter field holding text (every field but the cultures). */
export type FilterFieldName = Exclude<keyof FilterFields, 'cultures'>;

/**
 * What the shared card filter panel (`ui/shared/card-filter`) shows and changes: the filter fields, the Block and Set
 * lists, the culture groups to hide, and what the player does to them. The deck builder's CollectionQuery is one; the
 * game page's card search (core/game/table-edit/card-search.ts) another. The page provides it under this class.
 */
export abstract class CardFilterModel {
  abstract readonly fields: Signal<FilterFields>;
  abstract readonly blockOptions: Signal<readonly SelectOption[]>;
  abstract readonly setOptions: Signal<readonly SelectOption[]>;
  /** Culture groups not shown (a format without them). */
  abstract readonly hiddenCultureGroups: Signal<ReadonlySet<CultureGroupId>>;
  /** The Product filter is on packs: only it and the name can filter then. */
  abstract readonly isPackProduct: Signal<boolean>;
  abstract setField(field: FilterFieldName, value: string): void;
  abstract toggleCulture(code: string): void;
  abstract resetAdvanced(): void;
  abstract resetAll(): void;
}

/** The pager above the filter: previous and next page, and how many cards a page holds (a slider). Optional. */
export abstract class CardFilterPager {
  abstract readonly previousDisabled: Signal<boolean>;
  abstract readonly nextDisabled: Signal<boolean>;
  abstract readonly sliderDisabled: Signal<boolean>;
  /** The slider's range and where it starts. */
  abstract readonly pageSizes: { readonly min: number; readonly max: number; readonly start: number };
  abstract previousPage(): void;
  abstract nextPage(): void;
  abstract setPageSize(count: number): void;
}

/**
 * The deck builder's compact layout: which filters are kept shown above the cards (the rest are in its menu). Only a
 * panel shown in parts (`part` pane or menu) needs it. Optional.
 */
export abstract class CardFilterPins {
  /** The filters that can be kept shown, in the filter's order ("cultures" for the culture buttons). */
  abstract readonly pinnable: readonly string[];
  abstract isPinned(name: string): boolean;
  abstract toggle(name: string): void;
}

/** The deck builder's compact layout: the card size the pane's slider sets, in %. Optional. */
export abstract class CardFilterSize {
  abstract readonly percent: Signal<number>;
  abstract readonly limits: { readonly min: number; readonly max: number; readonly step: number };
  abstract set(percent: number): void;
}
