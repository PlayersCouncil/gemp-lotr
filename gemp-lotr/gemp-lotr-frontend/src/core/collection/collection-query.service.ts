import { computed, inject, Injectable, signal } from '@angular/core';
import { BlockFilter } from '../api/models';
import { CardImageService } from '../cards/card-image.service';
import { DeckDraft } from '../deck/deck-draft.service';
import { shownDeckBlueprints } from '../deck/deck-model';
import { CollectionBrowser } from './collection-browser.service';
import { blockOptions, setOptions } from './filter-block-sets';
import { CultureGroupId, hiddenCultureGroups, visibleCultures } from './filter-cultures';
import { EMPTY_FILTER, FilterFields, NUMERIC_FILTERS, NumericFilter, resetAdvanced, resetAll } from './filter-fields';
import { INITIAL_BLOCK_OPTION, INITIAL_SET_OPTION, SelectOption } from './filter-options';
import { buildFilterString } from './filter-string';

/** What the deck builder shows by default: everything except The One Ring. */
export const DEFAULT_FILTER_OVERRIDE = 'cardType:-THE_ONE_RING';

/** A filter field holding text (every field but the cultures). */
export type FilterFieldName = Exclude<keyof FilterFields, 'cultures'>;

/** Like a native select: a value with no matching option selects the first one. */
function valueOrFirst(value: string, options: readonly SelectOption[]): string {
  return options.some((option) => option.value === value) ? value : (options[0]?.value ?? '');
}

/**
 * What the collection is filtered by: the filter fields and their rules (the Block and Set lists, the cultures a format
 * shows, comparisons, the predefined filters), turned into the filter string the browser loads.
 */
@Injectable()
export class CollectionQuery {
  private readonly browser = inject(CollectionBrowser);
  private readonly draft = inject(DeckDraft);
  private readonly images = inject(CardImageService);

  readonly fields = signal<FilterFields>(EMPTY_FILTER);
  readonly blockOptions = signal<readonly SelectOption[]>([INITIAL_BLOCK_OPTION]);
  readonly setOptions = signal<readonly SelectOption[]>([INITIAL_SET_OPTION]);
  /** Culture groups the format hides; none until a format is set. */
  readonly hiddenCultureGroups = signal<ReadonlySet<CultureGroupId>>(new Set());
  readonly isPackProduct = computed(() => this.fields().product === 'pack');

  private format: string | null = null;
  private blocks: readonly BlockFilter[] = [];
  private override = '';
  private siteOverride = false;
  private filter: string;

  constructor() {
    this.filter = this.calculateFilter();
    this.browser.useFilter(this.filter);
  }

  /** The deck's format changed: rebuilds the Block and Set lists and reloads. */
  updateFormat(formatCode: string, blocks: readonly BlockFilter[] | null): void {
    this.format = formatCode;
    this.blocks = blocks ?? [];
    this.hiddenCultureGroups.set(hiddenCultureGroups(this.blocks));
    const { options, selected } = blockOptions(this.blocks);
    this.blockOptions.set(options);
    this.fields.update((fields) => ({ ...fields, block: selected }));
    this.blockChanged();
  }

  /** Shows the collection with a predefined filter (e.g. only possible Ring-bearers) after resetting every field. */
  setFilterOverride(override: string): void {
    this.override = override;
    this.browser.goToFirstPage();
    this.resetAll();
  }

  /** RTMD: a meta-site lets other sites be used; site searches then ask for them. */
  setSiteOverride(siteOverride: boolean): void {
    this.siteOverride = siteOverride;
  }

  /** A filter field was changed by the player. */
  setField(field: FilterFieldName, value: string): void {
    this.fields.update((fields) => ({ ...fields, [field]: value }));
    if (field === 'block') {
      this.blockChanged();
    } else if (field === 'product' || (NUMERIC_FILTERS as readonly string[]).includes(field.replace(/Compare$/, ''))) {
      this.compareTypeChanged();
    } else {
      this.filterChanged();
    }
  }

  toggleCulture(code: string): void {
    this.fields.update((fields) => ({
      ...fields,
      cultures: fields.cultures.includes(code) ? fields.cultures.filter((c) => c !== code) : [...fields.cultures, code],
    }));
    this.filterChanged();
  }

  resetAdvanced(): void {
    this.fields.update(resetAdvanced);
    this.filterChanged();
  }

  resetAll(): void {
    this.fields.update(resetAll);
    this.blockChanged();
  }

  /** Rebuilds the Set list for the chosen block, then applies the culture visibility. */
  private blockChanged(): void {
    const block = valueOrFirst(this.fields().block, this.blockOptions());
    // Before the first format arrives, the lists keep their initial "Any" option
    const sets = this.format !== null ? setOptions(this.blocks, block) : null;
    if (sets) {
      this.setOptions.set(sets.options);
    }
    this.fields.update((fields) => ({
      ...fields,
      block,
      set: sets ? sets.selected : valueOrFirst(fields.set, this.setOptions()),
      cultures: visibleCultures(fields.cultures, this.hiddenCultureGroups()),
    }));
    this.filterChanged();
  }

  /** A comparison set back to "Any" clears its number. */
  private compareTypeChanged(): void {
    this.fields.update((fields) => {
      const cleared: Partial<Record<`${NumericFilter}Value`, string>> = {};
      for (const stat of NUMERIC_FILTERS) {
        if (fields[`${stat}Compare`] === '') {
          cleared[`${stat}Value`] = '';
        }
      }
      return { ...fields, ...cleared };
    });
    this.filterChanged();
  }

  /** Reloads from the first page if the filter string changed. */
  private filterChanged(): void {
    const filter = this.calculateFilter();
    if (filter !== this.filter) {
      this.filter = filter;
      this.browser.showFilter(filter);
    }
  }

  private calculateFilter(): string {
    return buildFilterString(this.fields(), {
      format: this.format,
      override: this.override,
      siteOverride: this.siteOverride,
      cardsInDeck: () =>
        shownDeckBlueprints(this.draft.deck()).map((blueprintId) => this.images.resolve(blueprintId).bareBlueprint),
    });
  }
}
