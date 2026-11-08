import { computed, DestroyRef, inject, Injectable, signal } from '@angular/core';
import { catchError, EMPTY, from, map, Observable, of, Subject, switchMap } from 'rxjs';
import { ApiError, GENERIC_COMMUNICATION_ERROR } from '../api/api-error';
import { DeckbuilderApi } from '../api/deckbuilder-api.service';
import { BlockFilter, CollectionPage, CollectionTypeInfo } from '../api/models';
import { CardImageService } from '../cards/card-image.service';
import { ImagePreloader } from '../cards/image-preloader.service';
import { Notifier } from '../notifier/notifier';
import {
  blockOptions,
  buildFilterString,
  CultureGroupId,
  EMPTY_FILTER,
  FilterFields,
  hiddenCultureGroups,
  INITIAL_BLOCK_OPTION,
  INITIAL_SET_OPTION,
  NUMERIC_FILTERS,
  NumericFilter,
  resetAdvanced,
  resetAll,
  SelectOption,
  setOptions,
  visibleCultures,
} from './card-filter';

/** Collections every player has, before the limited ones from GET /collection (the #collectionSelect options). */
export const BASE_COLLECTION_TYPES: readonly { type: string; name: string }[] = [
  { type: 'default', name: 'All cards' },
  { type: 'permanent', name: 'My cards' },
  { type: 'trophy', name: 'Trophies' },
];

/** Page size slider limits, and its starting position. */
export const PAGE_SIZE_MIN = 4;
export const PAGE_SIZE_MAX = 40;
export const PAGE_SIZE_SLIDER_START = 18;
/**
 * The page size until the slider is moved. The legacy CardFilter loaded 12 cards per page although its slider starts
 * at 18; kept as it was, so players see the same pages.
 */
export const INITIAL_PAGE_SIZE = 12;

/** What the deck builder shows by default: everything except The One Ring (deckBuildingUi.defaultSelection). */
export const DEFAULT_FILTER_OVERRIDE = 'cardType:-THE_ONE_RING';

interface CollectionRequest {
  type: string;
  filter: string;
  start: number;
  count: number;
}

/**
 * State of the collection pane, ported from the legacy CardFilter and the collection parts of deckBuildingUi.js:
 * the collection type, the filter fields, paging, and the page of cards from GET /collection/{type}.
 *
 * Unlike the legacy page, only the newest request is shown: answers to older ones (e.g. while the page size slider is
 * dragged) are dropped instead of possibly arriving last and replacing the newer page. And a new page appears all at
 * once when its card images have loaded, instead of card by card as each image arrives.
 */
@Injectable({ providedIn: 'root' })
export class CollectionService {
  private readonly api = inject(DeckbuilderApi);
  private readonly images = inject(CardImageService);
  private readonly preloader = inject(ImagePreloader);
  private readonly notifier = inject(Notifier);

  /** The selected collection (#collectionSelect). */
  readonly collectionType = signal('default');
  /** The limited collections the player has, appended to #collectionSelect (loaded by DeckService). */
  readonly limitedCollections = signal<readonly CollectionTypeInfo[]>([]);

  readonly fields = signal<FilterFields>(EMPTY_FILTER);
  readonly blockOptions = signal<readonly SelectOption[]>([INITIAL_BLOCK_OPTION]);
  readonly setOptions = signal<readonly SelectOption[]>([INITIAL_SET_OPTION]);
  /** Culture groups the format hides; none until a format is set. */
  readonly hiddenCultureGroups = signal<ReadonlySet<CultureGroupId>>(new Set());

  /** First item of the page, and items per page. */
  readonly start = signal(0);
  readonly count = signal(INITIAL_PAGE_SIZE);
  readonly page = signal<CollectionPage | null>(null);
  readonly previousDisabled = signal(true);
  readonly nextDisabled = signal(true);
  /** The page size slider is enabled once the first page has arrived. */
  readonly sliderDisabled = signal(true);

  /**
   * Copies of each blueprint in the deck, subtracted from the collection counts (set by the deck area in Phase 5).
   */
  readonly deckCounts = signal<ReadonlyMap<string, number>>(new Map());
  /** Bare blueprint IDs of the deck's cards, for the "Special in Deck" product filter (set by the deck area). */
  cardsInDeck: () => readonly string[] = () => [];

  private format: string | null = null;
  private blocks: readonly BlockFilter[] = [];
  private override = '';
  private siteOverride = false;
  /** The filter string of the shown collection (CardFilter.filter). */
  private filter = '';
  private readonly requests = new Subject<CollectionRequest>();

  readonly isPackProduct = computed(() => this.fields().product === 'pack');

  constructor() {
    this.filter = this.calculateFilter();
    const subscription = this.requests
      .pipe(
        switchMap((request) =>
          this.api.getCollection(request.type, request.filter, request.start, request.count).pipe(
            // The new page shows when its card images are ready, all at once (a newer request still replaces it)
            switchMap((page) => this.withImagesLoaded(page)),
            catchError((error: unknown) => {
              void this.notifier.alert(
                ApiError.from(error).is(404) ? "You don't have collection of that type." : GENERIC_COMMUNICATION_ERROR,
              );
              return EMPTY;
            }),
          ),
        ),
      )
      .subscribe((page) => this.display(page));
    inject(DestroyRef).onDestroy(() => subscription.unsubscribe());
  }

  /** The deck's format changed (CardFilter.updateFormat): rebuilds the Block and Set lists and reloads. */
  updateFormat(formatCode: string, blocks: readonly BlockFilter[] | null): void {
    this.format = formatCode;
    this.blocks = blocks ?? [];
    this.hiddenCultureGroups.set(hiddenCultureGroups(this.blocks));
    const { options, selected } = blockOptions(this.blocks);
    this.blockOptions.set(options);
    this.fields.update((fields) => ({ ...fields, block: selected }));
    this.blockChanged();
  }

  /**
   * Shows the collection with a predefined filter (e.g. only cards that can be a Ring-bearer) after resetting every
   * field (CardFilter.setFilterOverride).
   */
  setFilterOverride(override: string): void {
    this.override = override;
    this.start.set(0);
    this.resetAll();
  }

  /** RTMD: a meta-site lets other sites be used; site searches then ask for them. */
  setSiteOverride(siteOverride: boolean): void {
    this.siteOverride = siteOverride;
  }

  selectCollectionType(type: string): void {
    this.collectionType.set(type);
    this.load();
  }

  /** A filter field was changed by the player. */
  setField(field: Exclude<keyof FilterFields, 'cultures'>, value: string): void {
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

  previousPage(): void {
    this.disableNavigation();
    this.start.update((start) => start - this.count());
    this.load();
  }

  nextPage(): void {
    this.disableNavigation();
    this.start.update((start) => start + this.count());
    this.load();
  }

  setPageSize(count: number): void {
    this.start.set(0);
    this.count.set(count);
    this.load();
  }

  /** Reloads the current page (CardFilter.getCollection), e.g. after opening a pack. */
  load(): void {
    this.requests.next({
      type: this.collectionType(),
      filter: this.filter,
      start: this.start(),
      count: this.count(),
    });
  }

  /** Copies of a blueprint left to add: the collection count minus the copies in the deck. */
  availableCount(blueprintId: string, count: number): number {
    return count - (this.deckCounts().get(blueprintId) ?? 0);
  }

  private disableNavigation(): void {
    this.previousDisabled.set(true);
    this.nextDisabled.set(true);
  }

  /** Rebuilds the Set list for the chosen block, then applies the culture visibility (setFilterChanged). */
  private blockChanged(): void {
    const block = this.valueOrFirst(this.fields().block, this.blockOptions());
    // Before the first format arrives, the lists keep their initial "Any" option
    const sets = this.format !== null ? setOptions(this.blocks, block) : null;
    if (sets) {
      this.setOptions.set(sets.options);
    }
    this.fields.update((fields) => ({
      ...fields,
      block,
      set: sets ? sets.selected : this.valueOrFirst(fields.set, this.setOptions()),
      cultures: visibleCultures(fields.cultures, this.hiddenCultureGroups()),
    }));
    this.filterChanged();
  }

  /** A comparison set back to "Any" clears its number (CardFilter.compareTypeChanged). */
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

  /** Reloads from the first page if the filter string changed (CardFilter.filterChanged). */
  private filterChanged(): void {
    const filter = this.calculateFilter();
    if (filter !== this.filter) {
      this.filter = filter;
      this.start.set(0);
      this.load();
    }
  }

  private calculateFilter(): string {
    return buildFilterString(this.fields(), {
      format: this.format,
      override: this.override,
      siteOverride: this.siteOverride,
      cardsInDeck: this.cardsInDeck,
    });
  }

  /** Waits for the page's card images (and the errata/foil layers), up to IMAGE_PRELOAD_TIMEOUT_MS. */
  private withImagesLoaded(page: CollectionPage): Observable<CollectionPage> {
    if (!this.preloader.supported) {
      return of(page);
    }
    const urls = new Set<string>();
    for (const item of page.items) {
      const image = this.images.resolve(item.blueprintId, item.kind === 'pack' ? 'pack' : item.side);
      urls.add(image.imageUrl);
      if (item.kind === 'card' && image.errata) urls.add('/gemp-lotr/images/errata-vertical.png');
    }
    return from(this.preloader.load(urls)).pipe(map(() => page));
  }

  private display(page: CollectionPage): void {
    this.page.set(page);
    this.previousDisabled.set(this.start() === 0);
    this.nextDisabled.set(this.start() + this.count() >= page.total);
    this.sliderDisabled.set(false);
  }

  /** Like jQuery's .val() on a select: a value with no matching option selects the first one. */
  private valueOrFirst(value: string, options: readonly SelectOption[]): string {
    return options.some((option) => option.value === value) ? value : (options[0]?.value ?? '');
  }
}
